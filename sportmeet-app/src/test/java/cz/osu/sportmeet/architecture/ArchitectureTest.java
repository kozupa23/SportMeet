
package cz.osu.sportmeet.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest
{
    private static final JavaClasses CLASSES =
            new ClassFileImporter()
                    .withImportOption(new ImportOption.DoNotIncludeTests())
                    .importPackages("cz.osu.sportmeet");

    @Test
    void otherModulesMustNotAccessUserInternals()
    {
        noClasses()
                .that()
                .resideOutsideOfPackage("cz.osu.sportmeet.user..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "cz.osu.sportmeet.user.application..",
                        "cz.osu.sportmeet.user.domain..",
                        "cz.osu.sportmeet.user.persistence.."
                )
                .check(CLASSES);
    }

    @Test
    void otherModulesMustNotAccessGroupInternals()
    {
        noClasses()
                .that()
                .resideOutsideOfPackage("cz.osu.sportmeet.group..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "cz.osu.sportmeet.group.application..",
                        "cz.osu.sportmeet.group.domain..",
                        "cz.osu.sportmeet.group.persistence.."
                )
                .check(CLASSES);
    }
}
