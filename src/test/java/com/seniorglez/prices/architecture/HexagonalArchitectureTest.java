package com.seniorglez.prices.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * FROZEN architecture tests: human-authored and part of the baseline. Do not modify this file.
 *
 * <p>They enforce the hexagonal rules of {@code specs/00-constitution.md} (section 2) and the package
 * layout of {@code specs/04-design.md}. They use the ArchUnit core library with plain JUnit tests (not the
 * ArchUnit JUnit engine), which keeps them independent of the JUnit Platform version.
 *
 * <p>Empty layers or packages make these rules fail. That is intended: the structure must exist.
 * Annotation and type names are given as strings so this file compiles without production code.
 */
class HexagonalArchitectureTest {

    private static final String BASE = "com.seniorglez.prices";

    private static final String DOMAIN = BASE + ".domain..";
    private static final String APPLICATION = BASE + ".application..";
    private static final String INBOUND_PORTS = BASE + ".application.port.in..";
    private static final String OUTBOUND_PORTS = BASE + ".application.port.out..";
    private static final String APPLICATION_SERVICES = BASE + ".application.service..";
    private static final String INFRASTRUCTURE = BASE + ".infrastructure..";
    private static final String ADAPTERS = BASE + ".infrastructure.adapter..";
    private static final String INBOUND_ADAPTERS = BASE + ".infrastructure.adapter.in..";
    private static final String OUTBOUND_ADAPTERS = BASE + ".infrastructure.adapter.out..";
    private static final String REST_ADAPTER = BASE + ".infrastructure.adapter.in.rest..";
    private static final String PERSISTENCE_ADAPTER = BASE + ".infrastructure.adapter.out.persistence..";

    private static JavaClasses productionClasses;

    @BeforeAll
    static void importProductionClasses() {
        productionClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE);
    }

    @Test
    void domainDependsOnlyOnItselfAndTheJdk() {
        classes().that().resideInAPackage(DOMAIN)
                .should().onlyDependOnClassesThat().resideInAnyPackage(DOMAIN, "java..")
                .because("the domain is pure Java (constitution 2.2)")
                .check(productionClasses);
    }

    @Test
    void applicationDependsOnlyOnDomainAndTheJdk() {
        classes().that().resideInAPackage(APPLICATION)
                .should().onlyDependOnClassesThat().resideInAnyPackage(APPLICATION, DOMAIN, "java..")
                .because("the application layer is pure Java and only knows the domain (constitution 2.3)")
                .check(productionClasses);
    }

    @Test
    void layersRespectTheDependencyRule() {
        layeredArchitecture().consideringOnlyDependenciesInLayers()
                .layer("Domain").definedBy(DOMAIN)
                .layer("Application").definedBy(APPLICATION)
                .layer("Infrastructure").definedBy(INFRASTRUCTURE)
                .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure")
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
                .because("dependencies point inwards: infrastructure -> application -> domain (constitution 2.1)")
                .check(productionClasses);
    }

    @Test
    void inboundAdaptersDoNotDependOnOutboundAdapters() {
        noClasses().that().resideInAPackage(INBOUND_ADAPTERS)
                .should().dependOnClassesThat().resideInAPackage(OUTBOUND_ADAPTERS)
                .because("adapters never depend on each other (constitution 2.5)")
                .check(productionClasses);
    }

    @Test
    void outboundAdaptersDoNotDependOnInboundAdapters() {
        noClasses().that().resideInAPackage(OUTBOUND_ADAPTERS)
                .should().dependOnClassesThat().resideInAPackage(INBOUND_ADAPTERS)
                .because("adapters never depend on each other (constitution 2.5)")
                .check(productionClasses);
    }

    @Test
    void adaptersUsePortsNotApplicationServices() {
        noClasses().that().resideInAPackage(ADAPTERS)
                .should().dependOnClassesThat().resideInAPackage(APPLICATION_SERVICES)
                .because("adapters talk to the application only through ports (constitution 2.5)")
                .check(productionClasses);
    }

    @Test
    void useCasesAreInterfaces() {
        classes().that().resideInAPackage(INBOUND_PORTS).and().haveSimpleNameEndingWith("UseCase")
                .should().beInterfaces()
                .check(productionClasses);
    }

    @Test
    void outboundPortsAreInterfaces() {
        classes().that().resideInAPackage(OUTBOUND_PORTS)
                .should().beInterfaces()
                .check(productionClasses);
    }

    @Test
    void restControllersLiveInTheRestAdapter() {
        classes().that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().resideInAPackage(REST_ADAPTER)
                .check(productionClasses);
    }

    @Test
    void jpaEntitiesLiveInThePersistenceAdapter() {
        classes().that().areAnnotatedWith("jakarta.persistence.Entity")
                .should().resideInAPackage(PERSISTENCE_ADAPTER)
                .check(productionClasses);
    }

    @Test
    void springDataRepositoriesLiveInThePersistenceAdapter() {
        classes().that().areAssignableTo("org.springframework.data.repository.Repository")
                .should().resideInAPackage(PERSISTENCE_ADAPTER)
                .check(productionClasses);
    }

    @Test
    void noFieldInjection() {
        noFields().should().beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
                .because("constructor injection only (constitution 3)")
                .check(productionClasses);
    }
}
