package cz.osu.sportmeet.common.application;

import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies detection of control and invisible characters in user-entered text.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
class TextInputTest
{
    @Test
    void detectsControlCharacters()
    {
        assertThat(TextInput.containsControlCharacter("Null\u0000Byte", false)).isTrue();
        assertThat(TextInput.containsControlCharacter("Bell\u0007", false)).isTrue();
        assertThat(TextInput.containsControlCharacter("Florbal Ostrava – Poruba", false)).isFalse();
    }

    @Test
    void lineBreaksAreAllowedOnlyForMultiLineText()
    {
        assertThat(TextInput.containsControlCharacter("First line\nSecond\tline\r\n", false)).isTrue();
        assertThat(TextInput.containsControlCharacter("First line\nSecond\tline\r\n", true)).isFalse();
        assertThat(TextInput.containsControlCharacter("Line\u0000", true)).isTrue();
    }

    @Test
    void textOfInvisibleCharactersHasNothingVisible()
    {
        assertThat(TextInput.hasVisibleCharacter("")).isFalse();
        assertThat(TextInput.hasVisibleCharacter(" \t\n")).isFalse();
        assertThat(TextInput.hasVisibleCharacter(" ")).isFalse();
        assertThat(TextInput.hasVisibleCharacter("​")).isFalse();
        assertThat(TextInput.hasVisibleCharacter("​  ⁠")).isFalse();
    }

    @Test
    void lettersDigitsAndSymbolsAreVisible()
    {
        assertThat(TextInput.hasVisibleCharacter("Č")).isTrue();
        assertThat(TextInput.hasVisibleCharacter(" 7 ")).isTrue();
        assertThat(TextInput.hasVisibleCharacter("​-")).isTrue();
        assertThat(TextInput.hasVisibleCharacter("🏀")).isTrue();
    }
}
