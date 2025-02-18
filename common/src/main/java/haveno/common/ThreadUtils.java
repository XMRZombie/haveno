/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package haveno.common;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadUtils.class);
    private static final Map<String, ExecutorService> EXECUTORS = new ConcurrentHashMap<>();
    private static final Map<String, AtomicReference<Thread>> THREADS = new ConcurrentHashMap<>();
    private static final int POOL_SIZE = 10;
    private static final ExecutorService POOL = Executors.newFixedThreadPool(POOL_SIZE);

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (String threadId : EXECUTORS.keySet()) {
                shutDown(threadId);
            }
            shutDownPool();
        }));
    }

    /**
     * Execute the given command in a thread with the given id.
     *
     * @param command the command to execute
     * @param threadId the thread id
     */

    public static Future<?> execute(Runnable command, String threadId) {
        EXECUTORS.computeIfAbsent(threadId, k -> Executors.newFixedThreadPool(1));
        return EXECUTORS.get(threadId).submit(() -> {
            THREADS.computeIfAbsent(threadId, k -> new AtomicReference<>()).set(Thread.currentThread());
            Thread.currentThread().setName(threadId);
            try {
                command.run();
            } finally {
                THREADS.remove(threadId);
            }
        });
    }

    /**
     * Awaits execution of the given command, but does not throw its exception.
     *
     * @param command the command to execute
     * @param threadId the thread id
     */
    public static void await(Runnable command, String threadId) {
        try {
            execute(command, threadId).get();
        } catch (Exception e) {
            LOGGER.error("Exception while awaiting command execution", e);
            throw new RuntimeException("Exception while awaiting command execution", e);
        }
    }

    public static void shutDown(String threadId) {
        shutDown(threadId, null);
    }

    public static void shutDown(String threadId, Long timeoutMs) {
        if (timeoutMs == null) timeoutMs = Long.MAX_VALUE;
        ExecutorService pool = EXECUTORS.get(threadId);
        if (pool == null) {
            LOGGER.warn("ExecutorService not found for threadId: {}", threadId);
            return;
        }
        pool.shutdown();
        try {
            if (!pool.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS)) {
                pool.shutdownNow();
                LOGGER.warn("ExecutorService did not terminate within the specified timeout for threadId: {}", threadId);
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted while awaiting termination for threadId: {}", threadId, e);
            throw new RuntimeException("Interrupted while awaiting termination", e);
        } finally {
            remove(threadId);
        }
    }

    public static void remove(String threadId) {
        EXECUTORS.remove(threadId);
        THREADS.remove(threadId);
    }

    // TODO: consolidate and cleanup apis

    private static void shutDownPool() {
        POOL.shutdown();
        try {
            if (!POOL.awaitTermination(60, TimeUnit.SECONDS)) {
                POOL.shutdownNow();
                LOGGER.warn("Shared thread pool did not terminate within the specified timeout");
            }
        } catch (InterruptedException e) {
            POOL.shutdownNow();
            Thread.currentThread().interrupt();
            LOGGER.error("Interrupted while awaiting termination of shared thread pool", e);
        }
    }

    public static Future<?> submitToPool(Runnable task) {
        return submitToPool(Arrays.asList(task)).get(0);
    }

    public static List<Future<?>> submitToPool(List<Runnable> tasks) {
        List<Future<?>> futures = new ArrayList<>();
        for (Runnable task : tasks) {
            futures.add(POOL.submit(task));
        }
        return futures;
    }

    public static Future<?> awaitTask(Runnable task) {
        return awaitTask(task, null);
    }

    public static Future<?> awaitTask(Runnable task, Long timeoutMs) {
        return awaitTasks(Arrays.asList(task), 1, timeoutMs).get(0);
    }

    public static List<Future<?>> awaitTasks(Collection<Runnable> tasks) {
        return awaitTasks(tasks, tasks.size());
    }

    public static List<Future<?>> awaitTasks(Collection<Runnable> tasks, int maxConcurrency) {
        return awaitTasks(tasks, maxConcurrency, null);
    }

    public static List<Future<?>> awaitTasks(Collection<Runnable> tasks, int maxConcurrency, Long timeoutMs) {
        if (timeoutMs == null) timeoutMs = Long.MAX_VALUE;
        if (tasks.isEmpty()) return new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(tasks.size());
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (Runnable task : tasks) {
                futures.add(executorService.submit(task, null));
            }
            for (Future<?> future : futures) {
                future.get(timeoutMs, TimeUnit.MILLISECONDS);
            }
            return futures;
        } catch (Exception e) {
            LOGGER.error("Exception while awaiting tasks", e);
            throw new RuntimeException("Exception while awaiting tasks", e);
        } finally {
            executorService.shutdownNow();
        }
    }

    private static boolean isCurrentThread(Thread thread, String threadId) {
        AtomicReference<Thread> threadRef = THREADS.get(threadId);
        return threadRef != null && threadRef.get() == thread;
    }
}

