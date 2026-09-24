package com.qa.restfulbooker.support;

import com.qa.restfulbooker.config.Environment;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

/** Escribe el ambiente de la ejecución en la sección "Environment" del reporte de Allure. */
public class AllureEnvironmentWriter implements TestExecutionListener {

    private static final Path RESULTS_DIRECTORY = Path.of("target", "allure-results");

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        Environment environment = Environment.current();
        Properties properties = new Properties();
        properties.setProperty("Ambiente", environment.name());
        properties.setProperty("Base URI", environment.baseUri());
        properties.setProperty("Java", Runtime.version().toString());
        properties.setProperty("Sistema operativo", System.getProperty("os.name"));

        try {
            Files.createDirectories(RESULTS_DIRECTORY);
            try (OutputStream file = Files.newOutputStream(RESULTS_DIRECTORY.resolve("environment.properties"))) {
                properties.store(file, null);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo escribir environment.properties", e);
        }
    }
}
