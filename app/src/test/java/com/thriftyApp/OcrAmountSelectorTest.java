package com.thriftyApp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/** Unit tests for the pure {@link OcrAmountSelector} heuristic. */
public class OcrAmountSelectorTest {

    private static List<OcrAmountSelector.Candidate> candidates(
            OcrAmountSelector.Candidate... items) {
        List<OcrAmountSelector.Candidate> list = new ArrayList<>();
        for (OcrAmountSelector.Candidate item : items) {
            list.add(item);
        }
        return list;
    }

    @Test
    public void emptyCandidatesYieldEmptyString() {
        assertEquals("", OcrAmountSelector.select(candidates()));
    }

    @Test
    public void keywordCandidateBeatsLargerNonKeyword() {
        String best = OcrAmountSelector.select(candidates(
                new OcrAmountSelector.Candidate("99.99", false),
                new OcrAmountSelector.Candidate("12.50", true)));
        assertEquals("12.50", best);
    }

    @Test
    public void highestKeywordValueWins() {
        String best = OcrAmountSelector.select(candidates(
                new OcrAmountSelector.Candidate("12.50", true),
                new OcrAmountSelector.Candidate("30.00", true)));
        assertEquals("30.00", best);
    }

    @Test
    public void picksLargestNumericWhenNoKeyword() {
        // Ascending then a smaller value exercises both the "new max" and the
        // "value <= current max" skip branches.
        String best = OcrAmountSelector.select(candidates(
                new OcrAmountSelector.Candidate("5", false),
                new OcrAmountSelector.Candidate("42", false),
                new OcrAmountSelector.Candidate("9", false)));
        assertEquals("42", best);
    }

    @Test
    public void smallerKeywordValueDoesNotReplaceLarger() {
        // Exercises the "value > highestKeywordScore" false branch.
        String best = OcrAmountSelector.select(candidates(
                new OcrAmountSelector.Candidate("30.00", true),
                new OcrAmountSelector.Candidate("12.50", true)));
        assertEquals("30.00", best);
    }

    @Test
    public void keywordAfterNonKeywordStillWins() {
        // A non-keyword sets the numeric max first, then a keyword overrides it.
        String best = OcrAmountSelector.select(candidates(
                new OcrAmountSelector.Candidate("99", false),
                new OcrAmountSelector.Candidate("7.00", true)));
        assertEquals("7.00", best);
    }

    @Test
    public void nonKeywordIgnoredOnceKeywordMatched() {
        // Keyword first means later non-keyword candidates are ignored, exercising
        // the hasKeywordMatch()==true branch inside the per-candidate fold.
        String best = OcrAmountSelector.select(candidates(
                new OcrAmountSelector.Candidate("7.00", true),
                new OcrAmountSelector.Candidate("99", false)));
        assertEquals("7.00", best);
    }

    @Test
    public void nullAndEmptyAmountsAreIgnored() {
        String best = OcrAmountSelector.select(candidates(
                new OcrAmountSelector.Candidate(null, false),
                new OcrAmountSelector.Candidate("", false),
                new OcrAmountSelector.Candidate("3", false)));
        assertEquals("3", best);
    }

    @Test
    public void unparseableCandidatesAreIgnored() {
        String best = OcrAmountSelector.select(candidates(
                new OcrAmountSelector.Candidate("abc", false),
                new OcrAmountSelector.Candidate("7", false)));
        assertEquals("7", best);
    }

    @Test
    public void hasKeywordIsCaseInsensitive() {
        assertTrue(OcrAmountSelector.hasKeyword("TOTAL: 12.00", new String[]{"total"}));
        assertFalse(OcrAmountSelector.hasKeyword("subtotal line", new String[]{"grand"}));
        assertFalse(OcrAmountSelector.hasKeyword(null, new String[]{"total"}));
        assertFalse(OcrAmountSelector.hasKeyword("line", null));
        assertFalse(OcrAmountSelector.hasKeyword("line", new String[]{null}));
    }
}
