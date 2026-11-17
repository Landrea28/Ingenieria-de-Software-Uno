package co.edu.konrad.consultaciudadano.arquitectura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class ArchitectureTest {
    private final JavaClasses clases = new ClassFileImporter().withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("co.edu.konrad.consultaciudadano");

    @Test
    void controladoresNoAccedenARepositorios() {
        noClasses().that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..repository..")
                .check(clases);
    }

    @Test
    void serviciosNoDependenDeControladores() {
        noClasses().that().resideInAPackage("..service..")
                .should().dependOnClassesThat().resideInAPackage("..controller..")
                .check(clases);
    }

    @Test
    void repositoriosNoDependanDeCapasSuperiores() {
        noClasses().that().resideInAPackage("..repository..")
                .should().dependOnClassesThat().resideInAnyPackage("..controller..", "..service..")
                .check(clases);
    }

    @Test
    void entidadesNoDependenDeCapasSuperiores() {
        noClasses().that().resideInAPackage("..entity..")
                .should().dependOnClassesThat().resideInAnyPackage("..controller..", "..service..", "..repository..")
                .check(clases);
    }

    @Test
    void sinCiclosEntreModulos() {
        slices().matching("co.edu.konrad.consultaciudadano.(*)..")
                .should().beFreeOfCycles().check(clases);
    }
}
