package co.eci.blacklist.labs.part3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.eci.blacklist.domain.BlacklistChecker;
import co.eci.blacklist.domain.MatchResult;
import co.eci.blacklist.domain.Policies;
import co.eci.blacklist.infrastructure.HostBlackListsDataSourceFacade;

/**
 * Test Part III - Performance Evaluation with Threading Configurations.
 * Evalúa el impacto del número de hilos en el tiempo de ejecución usando la IP dispersa (202.24.34.55).
 *
 * Escenarios probados:
 * - 1 hilo
 * - Núcleos físicos
 * - Doble de núcleos
 * - 50 hilos
 * - 100 hilos
 */
public class TestPartIII_PerformanceEvaluation {

    private static final String DISPERSED_IP = "202.24.34.55"; // IP con matches dispersos
    private BlacklistChecker checker;
    private HostBlackListsDataSourceFacade facade;
    private int physicalCores;

    @BeforeEach
    void setup() {
        // Obtener instancia de la fachada
        facade = HostBlackListsDataSourceFacade.getInstance();

        // ✅ CORREGIDO: pasar también un objeto Policies
        checker = new BlacklistChecker(facade, new Policies());

        // Detectar núcleos disponibles (procesadores lógicos)
        physicalCores = Runtime.getRuntime().availableProcessors();

        System.out.println("Setup completado: " + physicalCores + " núcleos disponibles, "
                + facade.getRegisteredServersCount() + " servidores registrados.");
    }

    @Test
    void testPartIII_completePerformanceEvaluation() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("PARTE III — EVALUACIÓN DE DESEMPEÑO");
        System.out.println("=".repeat(80));
        System.out.println("IP bajo prueba: " + DISPERSED_IP);
        System.out.println("Procesador: " + physicalCores + " núcleos disponibles");
        System.out.println("Total de servidores: " + facade.getRegisteredServersCount());
        System.out.println();

        int[] threadConfigurations = {
                1,
                physicalCores,
                physicalCores * 2,
                50,
                100
        };

        String[] configDescriptions = {
                "1 hilo (secuencial)",
                physicalCores + " hilos (núcleos físicos)",
                (physicalCores * 2) + " hilos (doble de núcleos)",
                "50 hilos (alta concurrencia)",
                "100 hilos (muy alta concurrencia)"
        };

        MatchResult[] results = new MatchResult[threadConfigurations.length];
        long[] executionTimes = new long[threadConfigurations.length];

        for (int i = 0; i < threadConfigurations.length; i++) {
            int threads = threadConfigurations[i];
            String description = configDescriptions[i];

            System.out.printf("Experimento %d: %s\n", i + 1, description);

            long startTime = System.nanoTime();
            MatchResult result = checker.checkHost(DISPERSED_IP, threads);
            long endTime = System.nanoTime();

            long executionTimeMs = (endTime - startTime) / 1_000_000;

            results[i] = result;
            executionTimes[i] = executionTimeMs;

            System.out.printf("   Resultado: %s\n", result.trustworthy() ? "CONFIABLE" : "NO CONFIABLE");
            System.out.printf("   Coincidencias: %d\n", result.matches().size());
            System.out.printf("   Servidores verificados: %,d / %,d (%.1f%%)\n",
                    result.checkedServers(), result.totalServers(),
                    (100.0 * result.checkedServers()) / result.totalServers());
            System.out.printf("   Tiempo reportado: %d ms\n", result.elapsedMs());
            System.out.printf("   Tiempo medido: %d ms\n", executionTimeMs);
            System.out.printf("   Hilos utilizados: %d\n\n", result.threads());

            // Validaciones básicas
            assertNotNull(result);
            assertEquals(DISPERSED_IP, result.ip());
            assertEquals(threads, result.threads());
            assertTrue(result.elapsedMs() >= 0);
            assertTrue(executionTimeMs >= 0);
        }

        performComparativeAnalysis(threadConfigurations, configDescriptions, results, executionTimes);
        performScalabilityAnalysis(threadConfigurations, executionTimes);

        assertTrue(true, "Evaluación de desempeño completada exitosamente");
    }

    private void performComparativeAnalysis(int[] threadConfigs, String[] descriptions,
            MatchResult[] results, long[] times) {
        System.out.println("ANÁLISIS COMPARATIVO:");
        System.out.println("-".repeat(80));

        System.out.printf("%-6s %-30s %-12s %-15s %-12s %-10s%n",
                "Exp.", "Configuración", "Tiempo (ms)", "Servidores", "Eficiencia", "Speedup");
        System.out.println("-".repeat(80));

        long baselineTime = times[0];

        for (int i = 0; i < threadConfigs.length; i++) {
            double efficiency = (100.0 * results[i].checkedServers()) / results[i].totalServers();
            double speedup = (double) baselineTime / times[i];

            System.out.printf("%-6d %-30s %-12d %-15s %-12.1f%% %-10.2fx%n",
                    i + 1,
                    descriptions[i],
                    times[i],
                    results[i].checkedServers() + "/" + results[i].totalServers(),
                    efficiency,
                    speedup);
        }
        System.out.println();
    }

    private void performScalabilityAnalysis(int[] threadConfigs, long[] times) {
        System.out.println("ANÁLISIS DE ESCALABILIDAD:");
        System.out.println("-".repeat(80));

        int fastestIndex = 0;
        long fastestTime = times[0];

        for (int i = 1; i < times.length; i++) {
            if (times[i] < fastestTime) {
                fastestTime = times[i];
                fastestIndex = i;
            }
        }

        System.out.printf("• Configuración más rápida: %d hilos (%d ms)\n",
                threadConfigs[fastestIndex], fastestTime);

        double theoreticalSpeedup = (double) threadConfigs[fastestIndex] / threadConfigs[0];
        double actualSpeedup = (double) times[0] / times[fastestIndex];
        double efficiency = (actualSpeedup / theoreticalSpeedup) * 100;

        System.out.printf("• Speedup teórico: %.2fx\n", theoreticalSpeedup);
        System.out.printf("• Speedup real: %.2fx\n", actualSpeedup);
        System.out.printf("• Eficiencia de paralelización: %.1f%%\n", efficiency);

        long time50 = times[3];
        long time100 = times[4];

        if (time100 > time50) {
            double degradation = ((double) time100 / time50 - 1) * 100;
            System.out.printf("  - Degradación con 100 vs 50 hilos: +%.1f%% tiempo\n", degradation);
        } else {
            System.out.println("  - No hay degradación significativa con 100 hilos");
        }
        System.out.println();
    }

    @Test
    void testPartIII_1_baselineWithSingleThread() {
        MatchResult result = checker.checkHost(DISPERSED_IP, 1);

        assertNotNull(result);
        assertEquals(DISPERSED_IP, result.ip());
        assertEquals(1, result.threads());
        assertTrue(result.elapsedMs() > 0);
    }

    @Test
    void testPartIII_2_optimalWithPhysicalCores() {
        MatchResult result = checker.checkHost(DISPERSED_IP, physicalCores);

        assertNotNull(result);
        assertEquals(DISPERSED_IP, result.ip());
        assertEquals(physicalCores, result.threads());
        assertTrue(result.elapsedMs() >= 0);
    }

    @Test
    void testPartIII_3_highConcurrencyWith100Threads() {
        MatchResult result = checker.checkHost(DISPERSED_IP, 100);

        assertNotNull(result);
        assertEquals(DISPERSED_IP, result.ip());
        assertEquals(100, result.threads());
        assertTrue(result.elapsedMs() >= 0);
    }
}
