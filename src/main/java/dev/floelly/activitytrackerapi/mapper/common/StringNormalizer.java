package dev.floelly.activitytrackerapi.mapper.common;

import org.springframework.stereotype.Component;

@Component
public class StringNormalizer {

    @NormalizedSingleLineText
    public String normalizeSingleLineText(String value) {
        if (value == null) {
            return null;
        }

        return value
                .replaceAll("\\s+", " ")
                .strip();
    }

    @NormalizedParagraphText
    public String normalizeParagraphText(String value) {
        if (value == null) {
            return null;
        }

        String normalizedLineBreaks = value.replace("\r\n", "\n").replace('\r', '\n');

        String[] lines = normalizedLineBreaks.split("\n", -1);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i]
                    .stripTrailing()
                    .replaceAll("[ \\t]{2,}", " ")
                    .replaceAll("(^[ \\t]+)|([ \\t]+$)", "");
            lines[i] = line;
        }

        String joined = String.join("\n", lines);

        joined = joined
                .replaceAll("\n{3,}", "\n\n")
                .replaceAll("(^[ \\t\\n]+)|([ \\t\\n]+$)", "");

        return joined;
    }
}
