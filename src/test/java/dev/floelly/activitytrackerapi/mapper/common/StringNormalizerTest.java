package dev.floelly.activitytrackerapi.mapper.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringNormalizerTest {

    private final StringNormalizer stringNormalizer = new StringNormalizer();

    @Test
    void normalizeSingleLineTextShouldReturnNullForNull() {
        assertThat(stringNormalizer.normalizeSingleLineText(null)).isNull();
    }

    @Test
    void normalizeSingleLineTextShouldTrimAndCollapseWhitespace() {
        assertThat(stringNormalizer.normalizeSingleLineText("  hello   world  "))
                .isEqualTo("hello world");
    }

    @Test
    void normalizeSingleLineTextShouldReplaceTabsAndLineBreaksWithSingleSpaces() {
        assertThat(stringNormalizer.normalizeSingleLineText("hello\t\tworld \n test"))
                .isEqualTo("hello world test");
    }

    @Test
    void normalizeParagraphTextShouldReturnNullForNull() {
        assertThat(stringNormalizer.normalizeParagraphText(null)).isNull();
    }

    @Test
    void normalizeParagraphTextShouldNormalizeLineBreaks() {
        assertThat(stringNormalizer.normalizeParagraphText("a\r\nb\rc"))
                .isEqualTo("a\nb\nc");
    }

    @Test
    void normalizeParagraphTextShouldTrimEachLine() {
        assertThat(stringNormalizer.normalizeParagraphText("  hello   \n  world\t "))
                .isEqualTo("hello\nworld");
    }

    @Test
    void normalizeParagraphTextShouldCollapseInnerSpacesPerLine() {
        assertThat(stringNormalizer.normalizeParagraphText("hello    world\nfoo\t\tbar"))
                .isEqualTo("hello world\nfoo bar");
    }

    @Test
    void normalizeParagraphTextShouldCollapseMultipleBlankLines() {
        assertThat(stringNormalizer.normalizeParagraphText("a\n\n\n\nb"))
                .isEqualTo("a\n\nb");
    }

    @Test
    void normalizeParagraphTextShouldTrimOuterBlankLines() {
        assertThat(stringNormalizer.normalizeParagraphText("\n\n  a\nb  \n\n"))
                .isEqualTo("a\nb");
    }
}