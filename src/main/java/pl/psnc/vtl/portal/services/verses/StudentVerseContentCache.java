package pl.psnc.vtl.portal.services.verses;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.psnc.vtl.portal.model.verses.StudentVerseContent;

import java.util.*;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class StudentVerseContentCache {

	/** Stores identifiers of verses in learning mode for each user and transcription */
	private final Map<Long, Map<Long, Map<Long, StudentVerseContent>>> studentContentVerses = new HashMap<>();

	/** Cache evicting student verses 24 hours after creation */
	private final LoadingCache<Long, Map<Long, Map<Long, StudentVerseContent>>> cache = CacheBuilder.newBuilder()
			.maximumSize(10000)
			.expireAfterAccess(24, TimeUnit.HOURS)
			.removalListener(removalNotification -> {
				if (removalNotification.wasEvicted()) {
					studentContentVerses.remove(removalNotification.getKey());
				}
			})
			.build(
					new CacheLoader<>() {
						public Map<Long, Map<Long, StudentVerseContent>> load(Long key) {
							return studentContentVerses.computeIfAbsent(key, k -> new HashMap<>());
						}
					});
	/**
	 * Returns {@code Optional<StudentVerseContent>} if the student verse content exists in cache and repository.
	 * Otherwise {@code Optional.empty} is returned.
	 *
	 * @param userId identifier of the user in learning mode
	 * @param transcriptionId transcription identifier
	 * @param verseId verse identifier
	 * @return {@code Optional<StudentVerseContent>} or {@code Optional.empty}
	 */
	public Optional<StudentVerseContent> getStudentVerseContentByUserAndTranscriptionAndId(Long userId, Long transcriptionId, Long verseId) {
		Objects.requireNonNull(userId, "Cannot look for null user");
		Objects.requireNonNull(transcriptionId, "Cannot look for null transcription");
		Objects.requireNonNull(verseId, "Cannot look for null verse");
		return Optional.ofNullable(cache.getUnchecked(userId)
				.getOrDefault(transcriptionId, Collections.emptyMap())
				.get(verseId));
	}

	/**
	 * Adds student verse content object to cache and repository. If the given user already has student verse content objects
	 * associated with different transcription they are all removed and only current student verse content for the given
	 * transcription is stored.
	 *
	 * @param userId identifier of the user in learning mode
	 * @param transcriptionId transcription identifier
	 * @param studentVerseContent student verse content object to store
	 */
	public void addStudentVerseContent(Long userId, Long transcriptionId, StudentVerseContent studentVerseContent) {
		Objects.requireNonNull(userId, "Cannot save for null user");
		Objects.requireNonNull(transcriptionId, "Cannot save for null transcription");
		Objects.requireNonNull(studentVerseContent, "Cannot save null student verse content");

		Map<Long, Map<Long, StudentVerseContent>> studentVerses = cache.getUnchecked(userId);
		clearOtherTranscriptions(transcriptionId, studentVerses);
		studentVerses.computeIfAbsent(transcriptionId, k -> new HashMap<>());
		studentVerses.get(transcriptionId).put(studentVerseContent.getVerseId(), studentVerseContent);
	}

	private void clearOtherTranscriptions(Long transcriptionId, Map<Long, Map<Long, StudentVerseContent>> transcriptionVerses) {
		// if cache contains student verses for different transcription than the one currently saved they should be cleared
		if (!transcriptionVerses.keySet().isEmpty() && !transcriptionVerses.containsKey(transcriptionId)) {
			transcriptionVerses.clear();
		}
	}
}
