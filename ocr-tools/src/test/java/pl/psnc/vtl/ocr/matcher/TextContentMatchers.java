package pl.psnc.vtl.ocr.matcher;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class TextContentMatchers {

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class FuzzyTextMatcher extends TypeSafeMatcher<String> {
        private static final double MATCH_PERCENTAGE_THRES = 0.2;

        private String expected;

        @Override
        protected boolean matchesSafely(String item) {
            int threshold = calculateDistanceThreshold(item);
            LevenshteinDistance distance = new LevenshteinDistance(threshold);

            return (distance.apply(expected, item) != -1);
        }

        private int calculateDistanceThreshold(String actual) {
            return (int) (actual.length() * MATCH_PERCENTAGE_THRES);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("fuzzy match text: " + expected);
        }
    }

    public static Matcher<String> fuzzyMatch(String expected) {
        return new FuzzyTextMatcher(expected);
    }
}
