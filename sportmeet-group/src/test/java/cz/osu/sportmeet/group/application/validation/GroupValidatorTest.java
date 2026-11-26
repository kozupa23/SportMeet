package cz.osu.sportmeet.group.application.validation;

import cz.osu.sportmeet.common.application.InvalidInputException;
import cz.osu.sportmeet.group.domain.GroupVisibility;
import cz.osu.sportmeet.group.persistence.entity.Group;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies normalization and business rules for group data.
 *
 * @author Radek Smiga
 * @since 0.1.0
 */
@NullMarked
class GroupValidatorTest
{
    private final GroupValidator validator = new GroupValidator();

    @Test
    void stripsName()
    {
        assertThat(validator.normalizeName("  Florbal Ostrava \t")).isEqualTo("Florbal Ostrava");
    }

    @Test
    void acceptsNameOfMaximumLengthAfterStripping()
    {
        String name = "x".repeat(Group.NAME_MAX_LENGTH);

        assertThat(validator.normalizeName("  " + name + "  ")).isEqualTo(name);
    }

    @Test
    void rejectsNameWithoutVisibleCharacters()
    {
        for (String name : new String[]{"", "   ", " ", "​", " ​  "})
        {
            assertThatThrownBy(() -> validator.normalizeName(name))
                    .as("name %s", name.codePoints().boxed().toList())
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Group name must not be blank.");
        }
    }

    @Test
    void rejectsTooLongName()
    {
        assertThatThrownBy(() -> validator.normalizeName("x".repeat(Group.NAME_MAX_LENGTH + 1)))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Group name must have at most 100 characters.");
    }

    /**
     * PostgreSQL cannot store NUL; without this check the insert would fail with HTTP 500.
     */
    @Test
    void rejectsControlCharactersInName()
    {
        assertThatThrownBy(() -> validator.normalizeName("Null\u0000Byte"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Group name must not contain control characters.");
        assertThatThrownBy(() -> validator.normalizeName("Two\nLines"))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void descriptionIsOptionalAndInvisibleMeansNone()
    {
        assertThat(validator.normalizeDescription(null)).isNull();
        assertThat(validator.normalizeDescription("   ")).isNull();
        assertThat(validator.normalizeDescription("​ ")).isNull();
        assertThat(validator.normalizeDescription("  Weekly training ")).isEqualTo("Weekly training");
    }

    @Test
    void descriptionMayHaveMultipleLinesButNoOtherControlCharacters()
    {
        assertThat(validator.normalizeDescription("Úterý 18:00\nČtvrtek 19:00")).isEqualTo("Úterý 18:00\nČtvrtek 19:00");
        assertThatThrownBy(() -> validator.normalizeDescription("Null\u0000Byte"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Group description must not contain control characters.");
    }

    @Test
    void limitsDescriptionLength()
    {
        String longest = "x".repeat(GroupValidator.DESCRIPTION_MAX_LENGTH);

        assertThat(validator.normalizeDescription(longest)).isEqualTo(longest);
        assertThatThrownBy(() -> validator.normalizeDescription(longest + "x"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Group description must have at most 2000 characters.");
    }

    @Test
    void visibilityDefaultsToPrivate()
    {
        assertThat(validator.resolveVisibility(null)).isEqualTo(GroupVisibility.PRIVATE);
        assertThat(validator.resolveVisibility(GroupVisibility.PUBLIC)).isEqualTo(GroupVisibility.PUBLIC);
    }
}
