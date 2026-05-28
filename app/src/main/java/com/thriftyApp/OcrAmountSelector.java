package com.thriftyApp;

import java.util.List;
import java.util.Locale;

/**
 * Pure selection of the most likely "total" amount from OCR candidates.
 *
 * <p>Amounts on a line that also contains an amount-keyword (e.g. "total") are
 * preferred; otherwise the largest numeric value is used. Extracting this from
 * {@code scanActivity} removes the deeply nested scoring loop from the ML Kit
 * success callback and makes the heuristic unit-testable.
 */
public final class OcrAmountSelector {

    /** A single parsed amount candidate and whether its line had a keyword. */
    public static final class Candidate {
        final String amount;
        final boolean keywordPresent;

        public Candidate(String amount, boolean keywordPresent) {
            this.amount = amount;
            this.keywordPresent = keywordPresent;
        }
    }

    private OcrAmountSelector() {
        // Utility class: no instances.
    }

    /** Mutable scoring state while scanning candidates. */
    private static final class Best {
        double highestKeywordScore = -1.0;
        double maxNumericValue = -1.0;
        String amount = "";

        boolean hasKeywordMatch() {
            return highestKeywordScore != -1.0;
        }
    }

    /**
     * Selects the best amount string from the candidates.
     *
     * @param candidates parsed amount candidates (may be empty)
     * @return the chosen amount, or an empty string when none is usable
     */
    public static String select(List<Candidate> candidates) {
        Best best = new Best();
        for (Candidate candidate : candidates) {
            consider(candidate, best);
        }
        return best.amount;
    }

    // Folds a single candidate into the running best-score state.
    private static void consider(Candidate candidate, Best best) {
        Double value = parse(candidate.amount);
        if (value == null) {
            return;
        }
        if (candidate.keywordPresent) {
            if (value > best.highestKeywordScore) {
                best.highestKeywordScore = value;
                best.amount = candidate.amount;
            }
            return;
        }
        if (best.hasKeywordMatch() || value <= best.maxNumericValue) {
            return;
        }
        best.maxNumericValue = value;
        best.amount = candidate.amount;
    }

    /**
     * Case-insensitive check for whether {@code line} contains any keyword.
     *
     * @param line     the OCR line text
     * @param keywords amount keywords (e.g. "total", "amount")
     * @return true when at least one keyword is present
     */
    public static boolean hasKeyword(String line, String[] keywords) {
        if (line == null || keywords == null) {
            return false;
        }
        String lower = line.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (keyword != null && lower.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private static Double parse(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
