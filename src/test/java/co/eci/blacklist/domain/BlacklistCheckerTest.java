package co.eci.blacklist.domain;

import co.eci.blacklist.infrastructure.HostBlackListsDataSourceFacade;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test 4 - Integration tests for BlacklistChecker domain service.
 * Tests the main BlacklistChecker implementation with virtual threads.
 *
 * @author ARSW-PANDILLA-2025
 * @version 1.0
 */
public class BlacklistCheckerTest {

    /**
     * Test 4.1: Verifies early stopping functionality to avoid scanning all servers.
     * Tests the main implementation with the production BlacklistChecker.
     */
    @Test
    void test4_1_earlyStopShouldAvoidScanningAllServers() {
        Policies policies = new Policies();
        policies.setAlarmCount(5);
        HostBlackListsDataSourceFacade facade = HostBlackListsDataSourceFacade.getInstance();
        
        String ip = "200.24.34.55";
        BlacklistChecker checker = new BlacklistChecker(facade, policies);
        MatchResult result = checker.checkHost(ip, Math.max(2, Runtime.getRuntime().availableProcessors()));

        assertNotNull(result);
        assertEquals(ip, result.ip());
        assertFalse(result.trustworthy(), "Should be NOT trustworthy when threshold reached");
        assertTrue(result.matches().size() >= policies.getAlarmCount());
        assertTrue(result.checkedServers() < result.totalServers(), "Should stop early and not scan all servers");
    }
}
