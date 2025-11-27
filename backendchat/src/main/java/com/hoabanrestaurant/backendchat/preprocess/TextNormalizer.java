package com.hoabanrestaurant.backendchat.preprocess;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class TextNormalizer {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}");
    private static final Pattern EMOJI = Pattern.compile("[\\p{So}\\p{Cn}\\p{Sk}]+");
    private static final Pattern MULTISPACE = Pattern.compile("\\s+");

    public String normalize(String text) {
        if (text == null) return "";

        // lower case
        text = text.toLowerCase();

        // remove emoji
        text = EMOJI.matcher(text).replaceAll("");

        // remove special characters
        text = text.replaceAll("[!.,?;:~%^*()\\[\\]{}…']", " ");

        // remove accents
        String nfd = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = DIACRITICS.matcher(nfd).replaceAll("");

        // remove multiple spaces
        text = MULTISPACE.matcher(text).replaceAll(" ").trim();

        return text;
    }
}
