package pl.psnc.vtl.portal.repositories.transcriptions;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import pl.psnc.vtl.portal.dto.transcriptions.*;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.users.SecurityGroup;
import pl.psnc.vtl.portal.model.users.User;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import java.util.List;
import java.util.stream.Collectors;

public class TranscriptionRepositoryCustomImpl implements TranscriptionRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public Page<TranscriptionWithHitCount> searchTranscriptions(String searchQuery, SearchType type, User user,
            SearchScope scope, SearchVisibility visibility, SearchOrder order, Sort.Direction orderDirToImpose,
            int page, int perpage) {
        String queryS = "FROM Transcription t ";
        // make it possible to sort by transcription creator's email
        if (order == SearchOrder.EMAIL) {
            queryS += "INNER JOIN t.creator c ";
        }
        if (!StringUtils.isBlank(searchQuery)) {
            if (type == SearchType.ALL || type == SearchType.METADATA) {
                queryS += "LEFT JOIN t.profileDesc pd "
                        + "LEFT JOIN pd.keywords pdk "
                        + "LEFT JOIN t.analyticInfo ai "
                        + "LEFT JOIN ai.actors aia "
                        + "LEFT JOIN t.monographicInfo mi "
                        + "LEFT JOIN mi.actors mia "
                        + "LEFT JOIN t.transcriptionInfo ti ";
            }
            if (type == SearchType.ALL || type == SearchType.CONTENT) {
                queryS += "LEFT JOIN Page p ON p.transcription = t "
                        + "LEFT JOIN Verse v ON v.page = p ";
            }
            if (type == SearchType.ALL || type == SearchType.TITLE_EMAIL) {
                // add only if title-email search, otherwise it is already added above (in metadata related condition)
                if (type == SearchType.TITLE_EMAIL) {
                    queryS += "LEFT JOIN t.analyticInfo ai ";
                }
                // do not add this join if order by email (order by email already adds that)
                if (order != SearchOrder.EMAIL) {
                    queryS += "INNER JOIN t.creator c ";
                }
            }
        }

        queryS += toCondition(scope, visibility, user);
        if (!StringUtils.isBlank(searchQuery)) {
            queryS += "AND ((1 <> 1) ";
            if (type == SearchType.ALL || type == SearchType.METADATA) {
                queryS += "OR fts(polish, ti.searchVector, :q, true) = true "
                        + "OR fts(polish, ai.searchVector, :q, true) = true "
                        + "OR fts(polish, mi.searchVector, :q, true) = true "
                        + "OR fts(polish, pd.searchVector, :q, true) = true ";
            }
            if (type == SearchType.ALL || type == SearchType.CONTENT) {
                queryS += "OR fts(polish, v.searchVector, :q, true) = true "
                        + "OR v IN (SELECT ve FROM Index i LEFT JOIN i.verses ve "
                        + "  WHERE fts(polish, i.searchVector, :q, true) = true) ";
            }
            if (type == SearchType.ALL || type == SearchType.TITLE_EMAIL) {
                // add only when title-email search, otherwise it is already added by metadata related condition (ai.searchVector)
                if (type == SearchType.TITLE_EMAIL) {
                    queryS += "OR fts(polish, ai.title, :q) = true ";
                }
                queryS += "OR LOWER(c.email) LIKE LOWER(:qlike) ";
            }
            queryS += ") ";
        }

        long totalHits = (long) createQuery("SELECT count(distinct t) " + queryS, searchQuery, user, type).getSingleResult();

        queryS += "GROUP BY t.id ";
        if (order != null) {
            queryS += "ORDER BY ";
            boolean first = true;
            for (var it = order.toSort(orderDirToImpose).iterator(); it.hasNext();) {
                Order o = it.next();
                queryS += (first ? "" : ", ") + (order.isTranscriptionField() ? "t." : "") + o.getProperty() + (o.isAscending() ? " asc " : " desc ");
                first = false;
            }
        }

        List<Object[]> resultList = createQuery("SELECT t, count(t) as hitCount "
                + (order == SearchOrder.EMAIL ? ", max(c.email) as " + SearchOrder.EMAIL.getField() + " " : "")
                + queryS, searchQuery, user, type)
                .setFirstResult((page - 1) * perpage)
                .setMaxResults(perpage).getResultList();
        List<TranscriptionWithHitCount> results = resultList.stream()
                .map(r -> new TranscriptionWithHitCount((Transcription) r[0], (Long) r[1]))
                .collect(Collectors.toList());
        return new PageImpl<>(results, Pageable.unpaged(), totalHits);
    }

    private String toCondition(SearchScope scope, SearchVisibility visibility, User user) {
        if (user == null) {
            return "WHERE t.hidden = false ";
        }
        String isMember = "(EXISTS "
                + "  (SELECT tu FROM TranscriptionUser tu "
                + "    WHERE tu.transcription = t "
                + "    AND tu.user = :user) "
                + "OR EXISTS "
                + "  (SELECT tg FROM TranscriptionGroup tg "
                + "    WHERE tg.transcription = t"
                + "    AND tg.group IN"
                + "      (SELECT ug.group FROM UserGroup ug "
                + "        WHERE ug.user = :user))) ";
        String condition;
        if (scope == SearchScope.CREATOR) {
            condition = "WHERE t.creator = :user ";
        } else if (scope == SearchScope.MEMBER) {
            condition = "WHERE (t.creator <> :user AND " + isMember + ") ";
        } else if (scope == SearchScope.CREATOR_OR_MEMBER) {
            condition = "WHERE (t.creator = :user OR " + isMember + ") ";
        } else if (scope == SearchScope.ALL) {
            condition = SecurityGroup.SUPER_ADMIN.equals(user.getSecurityGroup()) ? "WHERE 1 = 1 "
                    : "WHERE (t.hidden = false OR t.creator = :user OR " + isMember + ") ";
        } else {
            throw new RuntimeException("Unknown scope " + scope);
        }

        if (visibility == SearchVisibility.PUBLIC || SecurityGroup.PUBLIC.equals(user.getSecurityGroup())) {
            condition += "AND t.hidden = false ";
        } else if (visibility == SearchVisibility.HIDDEN) {
            condition += "AND t.hidden = true ";
        }
        return condition;
    }

    private Query createQuery(String queryS, String searchQuery, User user, SearchType type) {
        Query q = entityManager.createQuery(queryS);
        if (!StringUtils.isBlank(searchQuery)) {
            q.setParameter("q", searchQuery);
            if (type == SearchType.ALL || type == SearchType.TITLE_EMAIL) {
                q.setParameter("qlike", "%" + searchQuery + "%");
            }
        }
        if (user != null && queryS.contains(":user"))
            q.setParameter("user", user);
        return q;
    }
}
