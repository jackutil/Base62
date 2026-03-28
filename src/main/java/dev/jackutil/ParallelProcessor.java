/*
 * Copyright 2026 https://www.github.com/jackutil
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.jackutil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A reference implementation for processing massive datasets of Base62 conversions
 * using multithreading and batching.
 * <p><b>IMPORTANT WARNING:</b> This class is provided as a structural reference for 
 * mass data imports and should only be used with care. It is designed to be modified 
 * to fit your specific database or file I/O requirements (specifically by implementing 
 * the {@link #saveEncode(String[])} and {@link #saveDecode(Long[])} methods). 
 * Careless use of large thread pools or massive arrays can lead to memory exhaustion 
 * or CPU throttling.</p>
 */
public class ParallelProcessor {

    private final static int DEFAULT_THREAD_COUNT = 4;
    private final static int DEFAULT_BATCH_SIZE = 10_000;
    
    private final int threadCount;
    private final int batchSize;
    
    /**
     * Constructs a new ParallelProcessor with specified thread and batch configurations.
     * If invalid limits (<= 0) are provided, the processor will safely fall back to 
     * default values.
     * @param threadCount The number of concurrent threads to use in the executor pool.
     * @param batchSize   The number of elements to process in a single chunk.
     */
    public ParallelProcessor(final int threadCount, final int batchSize) {
        if (threadCount <= 0) {
            this.threadCount = DEFAULT_THREAD_COUNT;
        } else {
            this.threadCount = threadCount;
        }

        if (batchSize <= 0) {
            this.batchSize = DEFAULT_BATCH_SIZE;
        } else {
            this.batchSize = batchSize;
        }

        checkWarnings();
    }

    /**
     * Encodes a massive array of primitive longs by slicing them into batches
     * and processing them concurrently using an {@link ExecutorService}.
     * @param numbers The complete array of base-10 numbers to encode.
     * @throws InterruptedException If the current thread was interrupted while waiting for task execution.
     * @throws ExecutionException   If a task aborted due to an internal exception during encoding.
     */
    public void massEncode(long[] numbers) throws InterruptedException, ExecutionException {
        int totalSize = numbers.length;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<String[]>> futures = new ArrayList<>();

        for (int i = 0; i < totalSize; i += batchSize) {
            int start = i;
            int end = Math.min(totalSize, i + batchSize);
            
            long[] chunk = Arrays.copyOfRange(numbers, start, end);

            Callable<String[]> task = () -> Base62.encodeArray(chunk);
            
            futures.add(executor.submit(task));
        }

        executor.shutdown();

        for (Future<String[]> future : futures) {
            // Catch the result in here and process it further
            saveEncode(future.get());
        }
    }

    /**
     * A stub method intended to be implemented by the user to handle the saving 
     * or downstream processing of an encoded batch.
     * @param results An array of newly encoded Base62 strings.
     */
    protected void saveEncode(String[] results) {
        // TODO: Implement your logic in here for processing of the saved chunks
    }

    /**
     * Decodes a massive list of Base62 strings back into base-10 numbers
     * utilizing Java's built-in Parallel Streams.
     * @param base62Strings The list of Base62 encoded strings to decode.
     */
    public void massDecode(List<String> base62Strings) {
        Long[] results = base62Strings.parallelStream()
            .map(Base62::decode)
            .toArray(Long[]::new);

        // Catch the result in here and process it further
        saveDecode(results);
    }

    /**
     * A stub method intended to be implemented by the user to handle the saving
     * or downstream processing of decoded numbers.
     * @param results An array of decoded base-10 Long objects.
     */
    protected void saveDecode(Long[] results) {
        // TODO: Implement your logic in here for processing of the decoded numbers
    }

    /**
     * Evaluates the current configuration and outputs standard warnings if the 
     * settings fall outside of recommended performance thresholds.
     */
    private final void checkWarnings() {
        if (threadCount > 16) {
            System.out.println("[WARNING] Thread Count is higher than 16 and might impact JVM performance!");
        }

        if (batchSize < 100) {
            System.out.println("[WARNING] Batch size is set low at " + batchSize + " which might slow down performance.");
        }

        if (batchSize > 10_000) {
            System.out.println("[WARNING] Batch size is set high at " + batchSize + " which might slow down performance.");
        }
    }
}