package pl.psnc.vtl.portal.services.pages;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import pl.psnc.vtl.portal.services.transcriptions.ParsePagesRangeException;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class PageHelperTest {

	@Test
	public void shouldSuccessfullyParsePagesComplex() {
		String pages = "1,2,5-6";

		List<Integer> pageNumbers = PageHelper.parsePages(pages, 10);

		Assert.assertEquals(4, pageNumbers.size());
		Assert.assertTrue(pageNumbers.containsAll(Arrays.asList(1,2,5,6)));
	}

	@Test
	public void shouldSuccessfullyParsePagesComplexWithManyRanges() {
		String pages = "1,2,5-8,10,12-15";

		List<Integer> pageNumbers = PageHelper.parsePages(pages, 20);

		Assert.assertEquals(11, pageNumbers.size());
		Assert.assertTrue(pageNumbers.containsAll(Arrays.asList(1,2,5,6,7,8,10,12,13,14,15)));
	}

	@Test
	public void shouldSuccessfullyParsePagesSingle() {
		String pages = "1";

		List<Integer> pageNumbers = PageHelper.parsePages(pages, 5);

		Assert.assertEquals(1, pageNumbers.size());
		Assert.assertTrue(pageNumbers.contains(1));
	}

	@Test
	public void shouldSuccessfullyParsePagesSingleSpreaded() {
		String pages = "1,5";

		List<Integer> pageNumbers = PageHelper.parsePages(pages, 5);

		Assert.assertEquals(2, pageNumbers.size());
		Assert.assertTrue(pageNumbers.containsAll(Arrays.asList(1,5)));
	}

	@Test
	public void shouldSuccessfullyParsePagesEmpty() {
		List<Integer> pageNumbers = PageHelper.parsePages("", 12);

		Assert.assertTrue(pageNumbers.isEmpty());
	}

	@Test
	public void shouldSuccessfullyParsePagesNull() {
		List<Integer> pageNumbers = PageHelper.parsePages(null, 13);

		Assert.assertTrue(pageNumbers.isEmpty());
	}

	@Test(expected = ParsePagesRangeException.class)
	public void shouldThrowExceptionWhenInvalidNumber() {
		String pages = "1.5";

		PageHelper.parsePages(pages, 1);

		Assert.fail();
	}

	@Test(expected = ParsePagesRangeException.class)
	public void shouldThrowExceptionWhenString() {
		String pages = "xyz";

		PageHelper.parsePages(pages, 2);

		Assert.fail();
	}

	@Test(expected = ParsePagesRangeException.class)
	public void shouldThrowExceptionWhenAnyRangeInvalid() {
		String pages = "1,5,7-9b";

		PageHelper.parsePages(pages, 10);

		Assert.fail();
	}

	@Test(expected = ParsePagesRangeException.class)
	public void shouldThrowExceptionWhenAnyPageIsGreaterThanMax() {
		String pages = "1,5,7";

		PageHelper.parsePages(pages, 3);

		Assert.fail();
	}

	@Test(expected = ParsePagesRangeException.class)
	public void shouldThrowExceptionWhenAnyRangeIsGreaterThanMax() {
		String pages = "1,5-50000000000";

		PageHelper.parsePages(pages, 6);

		Assert.fail();
	}
}
