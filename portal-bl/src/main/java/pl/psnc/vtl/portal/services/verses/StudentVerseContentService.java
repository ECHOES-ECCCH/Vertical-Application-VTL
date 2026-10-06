package pl.psnc.vtl.portal.services.verses;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.model.verses.LearningStatus;
import pl.psnc.vtl.portal.model.verses.StudentVerseContent;
import pl.psnc.vtl.portal.repositories.users.UserGroupRepository;
import pl.psnc.vtl.portal.repositories.verses.VerseRepository;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StudentVerseContentService {

	private static final String REMOVE_PARENTHESES_REGEX = "[()]*";

	private final StudentVerseContentCache studentVerseContentCache;

	private  final LoggedInUserHolder loggedInUserHolder;

	private final VerseRepository verseRepository;

	private final UserGroupRepository userGroupRepository;

	@Value("#{'${vtl.student.partial-match-groups:}'.split(',')}")
	private List<Long> studentPartialMatchGroups;

	/**
	 * Update verse content in learning mode. When this method is called for not logged used it will throw AccessDeniedException.
	 * Only verses that are not images can be updated. In case of image verses IllegalArgumentException is thrown.
	 * When verse with specified id has the same content as the one given in content input parameter the method will return
	 * {@code LearningStatus.CORRECT} otherwise {@code LearningStatus.INCORRECT}.
	 *
	 * @param verseId verse identifier
	 * @param content student content of the specified verse
	 * @return {@code LearningStatus.CORRECT} when given content and verse content are the same otherwise {@code LearningStatus.INCORRECT}
	 */
	public LearningStatus updateVerseContent(Long verseId, String content) {
		var user = Optional.ofNullable(loggedInUserHolder.getLoggedInUser())
				.orElseThrow(() -> new AccessDeniedException("Public user cannot use student view mode"));

		var verse = verseRepository.findById(verseId)
				.orElseThrow(() -> new EntityNotFoundException(String.format("Verse %d does not exist", verseId)));
		if (verse.isImage()) {
			throw new IllegalArgumentException("Image verse cannot be used for learning");
		}

		Long transcriptionId = verse.getPage().getTranscription().getId();

		Optional<StudentVerseContent> studentVerseContent = studentVerseContentCache
				.getStudentVerseContentByUserAndTranscriptionAndId(user.getId(), transcriptionId, verseId);

		studentVerseContent.ifPresentOrElse(verseContent -> verseContent.setContent(content),
				() -> studentVerseContentCache.addStudentVerseContent(user.getId(), transcriptionId, new StudentVerseContent(verseId, content)));

		return computeLearningStatus(content, verse.currentContent());
	}

	public LearningStatus computeLearningStatus(String studentContent, String content) {
		if (studentContent == null || studentContent.isEmpty()) {
			return LearningStatus.NONE;
		}

		var cleanStudentContent = removeFormatting(studentContent);
		var cleanContent = removeFormatting(content);

		if (isPartialMatchEnabled()) {
			String noParenthesesContent = removeParenthesesContent(cleanContent);
			if ((noParenthesesContent.equals(cleanContent) && cleanStudentContent.equals(noParenthesesContent))
				|| cleanStudentContent.equals(cleanContent.replaceAll(REMOVE_PARENTHESES_REGEX, ""))) {
				return LearningStatus.CORRECT;
			}
			if (cleanStudentContent.equals(noParenthesesContent)) {
				return LearningStatus.PARTIAL_CORRECT;
			}
		}
		if (cleanStudentContent.equals(cleanContent)) {
			return LearningStatus.CORRECT;
		}
		return LearningStatus.INCORRECT;
	}

	private String removeParenthesesContent(String content) {
		var result = new StringBuilder();

		int begin = content.indexOf('(');
		if (begin == -1) {
			return content;
		}
		int end = content.indexOf(')', begin);
		if (end == -1) {
			return content;
		}
		return removeParenthesesContent(result
				.append(content, 0, begin)
				.append(content, end + 1, content.length())
				.toString());
	}

	private boolean isPartialMatchEnabled() {
		var user = Optional.ofNullable(loggedInUserHolder.getLoggedInUser())
				.orElseThrow(() -> new AccessDeniedException("Public user cannot use student view mode"));
		return userGroupRepository.findByUserId(user.getId())
				.stream()
				.map(userGroup -> userGroup.getGroup().getId())
				.anyMatch(id -> studentPartialMatchGroups.contains(id));
	}

	private String removeFormatting(String content) {
		return content
				.replace("<em>","")
				.replace("</em>", "")
				.replace("<strong>", "")
				.replace("</strong>", "");
	}
}
