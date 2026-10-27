package pl.psnc.vtl.portal.services.verses;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import pl.psnc.vtl.portal.model.verses.StudentVerseContent;

import java.util.Optional;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class StudentVerseContentCacheTest extends TestCase {

	private final StudentVerseContentCache studentVerseContentCache =  new StudentVerseContentCache();

	@Test(expected = NullPointerException.class)
	public void testGetStudentVerseContentByUserAndTranscriptionAndIdWhenNullUser() {
		studentVerseContentCache.getStudentVerseContentByUserAndTranscriptionAndId(null, 1L, 1L);
		fail();
	}

	@Test(expected = NullPointerException.class)
	public void testGetStudentVerseContentByUserAndTranscriptionAndIdWhenNullTranscription() {
		studentVerseContentCache.getStudentVerseContentByUserAndTranscriptionAndId(1L, null, 1L);
		fail();
	}

	@Test(expected = NullPointerException.class)
	public void testGetStudentVerseContentByUserAndTranscriptionAndIdWhenNullVerse() {
		studentVerseContentCache.getStudentVerseContentByUserAndTranscriptionAndId(1L, 1L, null);
		fail();
	}

	@Test
	public void testAddStudentVerseContent() {
		StudentVerseContent studentVerseContent = new StudentVerseContent(1L, "test");
		Optional<StudentVerseContent> verseContentOptional = studentVerseContentCache.getStudentVerseContentByUserAndTranscriptionAndId(1L, 1L, 1L);
		Assert.assertTrue(verseContentOptional.isEmpty());

		studentVerseContentCache.addStudentVerseContent(1L, 1L, studentVerseContent);

		verseContentOptional = studentVerseContentCache.getStudentVerseContentByUserAndTranscriptionAndId(1L, 1L, 1L);
		Assert.assertFalse(verseContentOptional.isEmpty());
		Assert.assertEquals(studentVerseContent, verseContentOptional.get());
	}

	@Test
	public void testAddStudentVerseContentForDifferentTranscription() {
		StudentVerseContent studentVerseContent = new StudentVerseContent(1L, "test");
		studentVerseContentCache.addStudentVerseContent(1L, 1L, studentVerseContent);
		Optional<StudentVerseContent> verseContentOptional = studentVerseContentCache.getStudentVerseContentByUserAndTranscriptionAndId(1L, 1L, 1L);
		Assert.assertFalse(verseContentOptional.isEmpty());

		StudentVerseContent otherStudentVerseContent = new StudentVerseContent(2L, "test2");
		studentVerseContentCache.addStudentVerseContent(1L, 2L, otherStudentVerseContent);
		verseContentOptional = studentVerseContentCache.getStudentVerseContentByUserAndTranscriptionAndId(1L, 1L, 1L);
		Assert.assertTrue(verseContentOptional.isEmpty());
		verseContentOptional = studentVerseContentCache.getStudentVerseContentByUserAndTranscriptionAndId(1L, 2L, 2L);
		Assert.assertFalse(verseContentOptional.isEmpty());
		Assert.assertEquals(otherStudentVerseContent, verseContentOptional.get());
	}

	@Test
	public void testAddStudentVerseContentForDifferentUsers() {
		StudentVerseContent studentVerseContent = new StudentVerseContent(1L, "test");
		studentVerseContentCache.addStudentVerseContent(1L, 1L, studentVerseContent);
		Optional<StudentVerseContent> verseContentOptional = studentVerseContentCache.getStudentVerseContentByUserAndTranscriptionAndId(1L, 1L, 1L);
		Assert.assertFalse(verseContentOptional.isEmpty());

		StudentVerseContent otherStudentVerseContent = new StudentVerseContent(1L, "test2");
		studentVerseContentCache.addStudentVerseContent(2L, 1L, otherStudentVerseContent);
		verseContentOptional = studentVerseContentCache.getStudentVerseContentByUserAndTranscriptionAndId(1L, 1L, 1L);
		Assert.assertFalse(verseContentOptional.isEmpty());
		Assert.assertEquals(studentVerseContent, verseContentOptional.get());
		verseContentOptional = studentVerseContentCache.getStudentVerseContentByUserAndTranscriptionAndId(2L, 1L, 1L);
		Assert.assertFalse(verseContentOptional.isEmpty());
		Assert.assertEquals(otherStudentVerseContent, verseContentOptional.get());
	}
}