package com.employeeFinderByDataForSEO.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Turns "Google Inc.", "google inc", "Google LLC", "  Google  "
 * into the same cache key ("google") so we don't re-fetch and
 * re-store the same company's profiles under multiple keys.
 *
 * This is a heuristic, not a company-identity resolver - it won't
 * catch every naming variation, but it removes the most common
 * legal-suffix / punctuation / whitespace noise.
 */
@Component
public class CompanyKeyNormalizer {

    private static final List<String> LEGAL_SUFFIXES = List.of(
            "private limited",
            "pvt ltd",
            "pvt. ltd.",
            "pvt ltd.",
            "limited",
            "llc",
            "l.l.c.",
            "inc.",
            "inc",
            "corp.",
            "corp",
            "corporation",
            "co.",
            "ltd.",
            "ltd"
    );

    private static final Pattern NON_ALNUM_SPACE =
            Pattern.compile("[^a-z0-9 ]");

    private static final Pattern MULTI_SPACE =
            Pattern.compile("\\s+");

    public String normalize(String rawCompany) {

        String key = rawCompany.trim().toLowerCase();

        key = NON_ALNUM_SPACE.matcher(key).replaceAll(" ");
        key = MULTI_SPACE.matcher(key).replaceAll(" ").trim();

        for (String suffix : LEGAL_SUFFIXES) {

            String suffixNoDots = suffix.replace(".", "");

            if (key.endsWith(" " + suffixNoDots)) {

                key = key.substring(
                        0,
                        key.length() - suffixNoDots.length() - 1
                ).trim();
            }
        }

        return key;
    }
}
