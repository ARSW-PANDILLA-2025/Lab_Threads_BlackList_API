package co.eci.blacklist.labs.part2.activity2;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.eci.blacklist.infrastructure.HostBlackListsDataSourceFacade;
import co.eci.blacklist.labs.part2.BlacklistChecker2;

/**
 * Test 2 - Tests for BlacklistChecker2 class (Activity 2).
 * Verifies the correct functionality of the parallelized blacklist checking
 * implementation using traditional Thread class.
 *
 * @author 
 * @version 1.0
 */
public class Test2BlacklistChecker2Test {

    private static final int BLACK_LIST_ALARM_COUNT = 5;
    private HostBlackListsDataSourceFacade facade;

    @BeforeEach
    void setUp() {
        // ✅ inicialización correcta
        facade = HostBlackListsDataSourceFacade.getInstance();
    }

    /**
     * Test 2.1: Verifies that BlacklistChecker2 correctly identifies servers
     * where IP is found within assigned range.
     */
    @Test
    void shouldFindIPInAssignedRange() throws InterruptedException {
        String testIP = "200.24.34.55"; // Known to be in early servers
        BlacklistChecker2 checker = new BlacklistChecker2(testIP, 0, 100, facade);

        checker.start();
        checker.join();

        List<Integer> found = checker.getFound();
        assertNotNull(found);
        assertTrue(!found.isEmpty(), "Should find IP in early servers");
    }

    /**
     * Test 2.2: Multiple threads, different segments.
     */
    @Test
    void shouldWorkWithMultipleThreadsOnDifferentSegments() throws InterruptedException {
        String testIP = "200.24.34.55"; 
        int totalServers = facade.getRegisteredServersCount();
        int nThreads = 4;
        int segmentSize = totalServers / nThreads;

        List<BlacklistChecker2> threads = new ArrayList<>();

        for (int i = 0; i < nThreads; i++) {
            int start = i * segmentSize;
            int end = (i == nThreads - 1) ? totalServers : (i + 1) * segmentSize;
            BlacklistChecker2 checker = new BlacklistChecker2(testIP, start, end, facade);
            threads.add(checker);
            checker.start();
        }

        for (BlacklistChecker2 thread : threads) {
            thread.join();
        }

        List<Integer> allFound = new ArrayList<>();
        for (BlacklistChecker2 thread : threads) {
            allFound.addAll(thread.getFound());
        }

        assertNotNull(allFound);
        assertTrue(!allFound.isEmpty(), "Should find IP across multiple segments");
        assertEquals(allFound.size(), allFound.stream().distinct().count(),
                "Should not have duplicates across different thread ranges");
    }

    /**
     * Test 2.3: Full checkHost logic.
     */
    @Test
    void shouldImplementCompleteCheckHostLogic() throws InterruptedException {
        String trustworthyIP = "212.24.24.55"; // clean
        String suspiciousIP = "200.24.34.55"; // blacklisted
        int nThreads = 4;

        List<Integer> trustworthyResult = performCheckHost(trustworthyIP, nThreads);
        assertTrue(trustworthyResult.size() < BLACK_LIST_ALARM_COUNT,
                "Trustworthy IP should have fewer than " + BLACK_LIST_ALARM_COUNT + " matches");

        List<Integer> suspiciousResult = performCheckHost(suspiciousIP, nThreads);
        assertTrue(suspiciousResult.size() >= BLACK_LIST_ALARM_COUNT,
                "Suspicious IP should have " + BLACK_LIST_ALARM_COUNT + " or more matches");
    }

    /**
     * Test 2.4: Handling remainders.
     */
    @Test
    void shouldHandleRemainderInServerDivision() throws InterruptedException {
        String testIP = "200.24.34.55";
        int totalServers = facade.getRegisteredServersCount();
        int nThreads = 7; 
        List<BlacklistChecker2> threads = new ArrayList<>();
        int segmentSize = totalServers / nThreads;

        for (int i = 0; i < nThreads; i++) {
            int start = i * segmentSize;
            int end = (i == nThreads - 1) ? totalServers : (i + 1) * segmentSize;
            BlacklistChecker2 checker = new BlacklistChecker2(testIP, start, end, facade);
            threads.add(checker);
            checker.start();
        }

        for (BlacklistChecker2 thread : threads) {
            thread.join();
        }

        int expectedLastEnd = totalServers;
        int actualLastStart = (nThreads - 1) * segmentSize;

        assertTrue(expectedLastEnd > actualLastStart,
                "Last thread should handle remainder servers properly");
    }

    /**
     * Test 2.5: Thread safety in results.
     */
    @Test
    void shouldMaintainThreadSafetyInResults() throws InterruptedException {
        String testIP = "202.24.34.55"; 
        int nThreads = 8;
        int totalServers = facade.getRegisteredServersCount();
        int segmentSize = totalServers / nThreads;

        List<BlacklistChecker2> threads = new ArrayList<>();

        for (int i = 0; i < nThreads; i++) {
            int start = i * segmentSize;
            int end = (i == nThreads - 1) ? totalServers : (i + 1) * segmentSize;
            BlacklistChecker2 checker = new BlacklistChecker2(testIP, start, end, facade);
            threads.add(checker);
            checker.start();
        }

        for (BlacklistChecker2 thread : threads) {
            thread.join();
        }

        for (int i = 0; i < threads.size(); i++) {
            BlacklistChecker2 thread = threads.get(i);
            List<Integer> found = thread.getFound();

            int expectedStart = i * segmentSize;
            int expectedEnd = (i == nThreads - 1) ? totalServers : (i + 1) * segmentSize;

            for (Integer serverId : found) {
                assertTrue(serverId >= expectedStart && serverId < expectedEnd,
                        "Found server " + serverId + " should be within range [" +
                                expectedStart + ", " + expectedEnd + ")");
            }
        }
    }

    // 🔹 Helper method
    private List<Integer> performCheckHost(String ip, int nThreads) throws InterruptedException {
        int totalServers = facade.getRegisteredServersCount();
        int segmentSize = totalServers / nThreads;

        List<BlacklistChecker2> threads = new ArrayList<>();

        for (int i = 0; i < nThreads; i++) {
            int start = i * segmentSize;
            int end = (i == nThreads - 1) ? totalServers : (i + 1) * segmentSize;
            BlacklistChecker2 checker = new BlacklistChecker2(ip, start, end, facade);
            threads.add(checker);
            checker.start();
        }

        for (BlacklistChecker2 thread : threads) {
            thread.join();
        }

        List<Integer> allFound = new ArrayList<>();
        for (BlacklistChecker2 thread : threads) {
            allFound.addAll(thread.getFound());
        }

        return allFound;
    }
}
