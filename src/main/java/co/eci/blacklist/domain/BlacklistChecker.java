package co.eci.blacklist.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import co.eci.blacklist.infrastructure.HostBlackListsDataSourceFacade;

/**
 * Blacklist checker with multi-thread processing.
 *
 * @author ARSW-PANDILLA-2025
 * @version 1.0
 */
public class BlacklistChecker {

    private static final Logger logger = Logger.getLogger(BlacklistChecker.class.getName());

    private final HostBlackListsDataSourceFacade facade;
    private final Policies policies;

    /**
     * Constructor of the BlacklistChecker class.
     *
     * @param facade Facade to access blacklist servers.
     * @param policies Policy configuration including alarm threshold.
     */
    public BlacklistChecker(HostBlackListsDataSourceFacade facade, Policies policies) {
        this.facade = Objects.requireNonNull(facade, "Facade cannot be null");
        this.policies = Objects.requireNonNull(policies, "Policies cannot be null");
    }

    /**
     * Checks if the IP address appears in blacklist servers using multiple threads.
     *
     * @param ip The IP address to verify.
     * @param nThreads The number of threads to use for parallel processing.
     * @return Verification result with timing information.
     */
    public MatchResult checkHost(String ip, int nThreads) {
        int threshold = policies.getAlarmCount();
        int total = facade.getRegisteredServersCount();

        long start = System.currentTimeMillis();
        AtomicInteger found = new AtomicInteger(0);
        AtomicInteger checked = new AtomicInteger(0);
        AtomicBoolean stop = new AtomicBoolean(false);
        List<Integer> matches = Collections.synchronizedList(new ArrayList<>());

        int threads = Math.max(1, nThreads);

        int segmentSize = total / threads;
        int remainder = total % threads;

        List<BlacklistWorkerThread> workerThreads = new ArrayList<>(threads);

        try {
            // Create and start threads
            for (int i = 0; i < threads; i++) {
                int startIdx = i * segmentSize;
                
                int endIdx;
                if (i < remainder) {
                    startIdx += i;
                    endIdx = startIdx + segmentSize + 1;
                } else {
                    startIdx += remainder;
                    endIdx = startIdx + segmentSize;
                }

                if (endIdx > total) {
                    endIdx = total;
                }

                BlacklistWorkerThread workerThread = new BlacklistWorkerThread(
                        ip, startIdx, endIdx, facade, matches, found, checked, stop, threshold);
                workerThreads.add(workerThread);
                workerThread.start();
            }

            // Join
            for (BlacklistWorkerThread thread : workerThreads) {
                thread.join();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread was interrupted during blacklist checking", e);
        }

        boolean trustworthy = found.get() < threshold;

        logger.log(Level.INFO, "Checked blacklists: {0} of {1}", new Object[]{checked.get(), total});

        if (trustworthy) {
            facade.reportAsTrustworthy(ip);
        } else {
            facade.reportAsNotTrustworthy(ip);
        }

        long elapsed = System.currentTimeMillis() - start;
        return new MatchResult(ip, trustworthy, List.copyOf(matches), checked.get(), total, elapsed, threads);
    }

    /**
     * Worker thread that processes a specific segment of blacklist servers.
     */
    private static class BlacklistWorkerThread extends Thread {
        
        private final String ip;
        private final int startIdx;
        private final int endIdx;
        private final HostBlackListsDataSourceFacade facade;
        private final List<Integer> matches;
        private final AtomicInteger found;
        private final AtomicInteger checked;
        private final AtomicBoolean stop;
        private final int threshold;

        /**
         * Constructor of the BlacklistWorkerThread class.
         *
         * @param ip The target IP address to verify.
         * @param startIdx The initial server index.
         * @param endIdx The final server index.
         * @param facade The blacklist data source facade.
         * @param matches The shared list to collect matches.
         * @param found The shared counter for total matches.
         * @param checked The shared counter for verified servers.
         * @param stop The shared stop flag.
         * @param threshold The alarm threshold for early stopping.
         */
        public BlacklistWorkerThread(String ip, int startIdx, int endIdx,
                HostBlackListsDataSourceFacade facade,
                List<Integer> matches, AtomicInteger found,
                AtomicInteger checked, AtomicBoolean stop, int threshold) {
            this.ip = ip;
            this.startIdx = startIdx;
            this.endIdx = endIdx;
            this.facade = facade;
            this.matches = matches;
            this.found = found;
            this.checked = checked;
            this.stop = stop;
            this.threshold = threshold;
        }

        /**
         * Executes blacklist verification for the assigned server segment.
         */
        @Override
        public void run() {
            for (int s = startIdx; s < endIdx && !stop.get(); s++) {
                if (stop.get()) {
                    break;
                }

                if (facade.isInBlackListServer(s, ip)) {
                    matches.add(s);
                    if (found.incrementAndGet() >= threshold) {
                        stop.set(true);
                    }
                }
                checked.incrementAndGet();
            }
        }
    }
}
