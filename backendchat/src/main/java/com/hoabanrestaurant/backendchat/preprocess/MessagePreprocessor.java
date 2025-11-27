package com.hoabanrestaurant.backendchat.preprocess;

import org.springframework.stereotype.Component;

@Component
public class MessagePreprocessor {

    private final TextNormalizer normalizer = new TextNormalizer();
    private final StopWordRemover stopWordRemover = new StopWordRemover();
    private final WordLemmatizer lemmatizer = new WordLemmatizer();

    public String process(String text) {
        text = normalizer.normalize(text);
        text = stopWordRemover.remove(text);
        text = lemmatizer.lemmatize(text);
        return text.trim();
    }
}
