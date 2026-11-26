package cz.osu.sportmeet.common.application;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NullMarked;

/**
 * Checks of user-entered text shared by validators of all modules.
 * <p>
 * Control characters such as NUL cannot be stored by PostgreSQL (the insert fails with HTTP 500)
 * and have no place in visible text. Texts made up only of invisible characters (no-break space,
 * zero-width space and similar) pass a plain {@link String#isBlank()} check but look empty to users.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
public final class TextInput
{
    private TextInput()
    {
    }

    /**
     * Returns whether the text contains a control character.
     *
     * @param text            checked text
     * @param allowLineBreaks whether line feed, carriage return and tab are allowed (multi-line text)
     * @return true if the text contains a disallowed control character
     */
    public static boolean containsControlCharacter(String text, boolean allowLineBreaks)
    {
        Preconditions.checkNotNull(text, "text must not be null");

        return text.codePoints().anyMatch(codePoint -> Character.isISOControl(codePoint)
                && !(allowLineBreaks && (codePoint == '\n' || codePoint == '\r' || codePoint == '\t')));
    }

    /**
     * Returns whether the text contains at least one visible character, i.e. it is not made up only of
     * whitespace, space separators or invisible format characters.
     *
     * @param text checked text
     * @return true if at least one character is visible
     */
    public static boolean hasVisibleCharacter(String text)
    {
        Preconditions.checkNotNull(text, "text must not be null");

        return text.codePoints().anyMatch(codePoint -> !Character.isWhitespace(codePoint)
                && !Character.isSpaceChar(codePoint)
                && !Character.isISOControl(codePoint)
                && Character.getType(codePoint) != Character.FORMAT);
    }
}
