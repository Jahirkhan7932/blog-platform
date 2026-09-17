package com.example.blog_platform.util;

import java.text.Normalizer;
import java.util.Locale;

public class SlugUtil {

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }
}
