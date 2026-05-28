package com.thriftyApp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps a transaction category code (the value persisted in the database, e.g.
 * {@code "Food"}) to the localized string resource used to display it.
 *
 * <p>Using a lookup table instead of a {@code switch} keeps the call sites at
 * cyclomatic complexity 1 and removes the duplicated category mapping that
 * previously lived in multiple activities.
 */
public final class CategoryLabels {

    private static final Map<String, Integer> CODE_TO_RES_ID;

    static {
        Map<String, Integer> map = new HashMap<>();
        map.put("Transport", R.string.cat_transport);
        map.put("Food", R.string.cat_food);
        map.put("Bills", R.string.cat_bills);
        map.put("Sports", R.string.cat_sports);
        map.put("Home", R.string.cat_home);
        map.put("Pets", R.string.cat_pets);
        map.put("Education", R.string.cat_education);
        map.put("Travel", R.string.cat_travel);
        map.put("Beauty", R.string.cat_beauty);
        map.put("Kids", R.string.cat_kids);
        map.put("Healthcare", R.string.cat_healthcare);
        map.put("Movie", R.string.cat_movie);
        CODE_TO_RES_ID = Collections.unmodifiableMap(map);
    }

    private CategoryLabels() {
        // Utility class: no instances.
    }

    /**
     * Returns the string-resource id for the given category code, or {@code 0}
     * when the code is unknown (callers should fall back to the raw code).
     *
     * @param code the persisted category code; may be {@code null}
     * @return the resource id, or {@code 0} if there is no mapping
     */
    public static int resourceIdFor(String code) {
        Integer resId = CODE_TO_RES_ID.get(code);
        return resId == null ? 0 : resId;
    }
}
