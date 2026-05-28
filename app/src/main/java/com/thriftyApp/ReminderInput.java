package com.thriftyApp;

/**
 * Pure, framework-free checks for the reminder form inputs.
 *
 * <p>Keeping the "all fields present" rule here lets the activity stay below the
 * cyclomatic-complexity threshold and makes the rule unit-testable.
 */
public final class ReminderInput {

    private ReminderInput() {
        // Utility class: no instances.
    }

    /**
     * Returns {@code true} only when every provided value is non-null and
     * non-empty.
     *
     * @param values the reminder fields to check
     * @return whether all fields are present
     */
    public static boolean allPresent(String... values) {
        for (String value : values) {
            if (value == null || value.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
