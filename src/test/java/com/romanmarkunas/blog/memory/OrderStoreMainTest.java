package com.romanmarkunas.blog.memory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.romanmarkunas.blog.memory.address.Address;
import com.romanmarkunas.blog.memory.address.AlaskaAddressArchive;
import com.romanmarkunas.blog.memory.example1.OrderGenerator;
import com.romanmarkunas.blog.memory.example1.OrderStoreMain;
import com.romanmarkunas.blog.memory.example10.BreakingUpAddressOrderStoreMain;
import com.romanmarkunas.blog.memory.example11.CustomStringEncodingOrderStoreMain;
import com.romanmarkunas.blog.memory.example12.LessReferencesOrderStoreMain;
import com.romanmarkunas.blog.memory.example13.CompressingOrderStoreMain;
import com.romanmarkunas.blog.memory.example14.CustomStringPoolOrderStoreMain;
import com.romanmarkunas.blog.memory.example4.TwoGCRootsOrderStoreMain;
import com.romanmarkunas.blog.memory.example8.EfficientCollectionsOrderStoreMain;
import com.romanmarkunas.blog.memory.example9.AvoidBoxingOrderStoreMain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

import static java.lang.ProcessBuilder.Redirect.INHERIT;
import static java.util.Arrays.asList;

class OrderStoreMainTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final List<Address> addresses = AlaskaAddressArchive.read();

    private Process process;


    @AfterEach
    void tearDown() {
        giveTimeToPipeOutputFromChildProcess();
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
    }


    @Test
    void example1Initial() {
        OrderGenerator generator = new OrderGenerator(addresses);
        process = runInSeparateJvm(
                OrderStoreMain.class,
                "-Xmx64m"
        );
        stuffOtherJvmUntilItDies(process, generator::next);
    }

    @Test
    void example2UnderstandingMemoryProfile() {
        OrderGenerator generator = new OrderGenerator(addresses);
        process = runInSeparateJvm(
                OrderStoreMain.class,
                "-Xmx64m",
//                "-Xlog:gc:gc.log",
                "-Xlog:gc*"
        );
        stuffOtherJvmUntilItDies(process, generator::next);
    }

    @Test
    void example3ProfilingHeap() {
        OrderGenerator generator = new OrderGenerator(addresses);
        process = runInSeparateJvm(
                OrderStoreMain.class,
                "-Xmx64m",
                "-XX:+HeapDumpOnOutOfMemoryError",
                "-XX:HeapDumpPath=dump.hprof"
        );
        stuffOtherJvmUntilItDies(process, generator::next);
    }

    @Test
    void example4ProfilingHeapWithLeakBetween2GCRoots() {
        OrderGenerator generator = new OrderGenerator(addresses);
        process = runInSeparateJvm(
                TwoGCRootsOrderStoreMain.class,
                "-Xmx64m",
                "-XX:+HeapDumpOnOutOfMemoryError",
                "-XX:HeapDumpPath=dump3.hprof"
        );
        stuffOtherJvmUntilItDies(process, generator::next);
    }

    @Test
    void example5ProfilingHeapWithJfr() {
        OrderGenerator generator = new OrderGenerator(addresses);
        process = runInSeparateJvm(
                OrderStoreMain.class,
                "-Xmx64m",
                "-XX:+FlightRecorder",
                "-XX:+UnlockDiagnosticVMOptions",
                "-XX:+DebugNonSafepoints", // improves accuracy
                "-XX:FlightRecorderOptions:memorysize=100m,stackdepth=32",
                "-XX:StartFlightRecording:name=SampleRecording,settings=memory.jfc,disk=false,dumponexit=true,filename=dump.jfr,maxage=1h",
                "-Xlog:jfr*"
        );
        stuffOtherJvmUntilItDies(process, generator::next);
    }

    @Test
    void example6ProfilingHeapWithAsyncProfiler() {
        OrderGenerator generator = new OrderGenerator(addresses);
        process = runInSeparateJvm(
                OrderStoreMain.class,
                "-Xmx64m",
                "-XX:+UnlockDiagnosticVMOptions",
                "-XX:+DebugNonSafepoints",
                "-agentpath:lib/async-profiler-1.7.1-linux-x64/build/libasyncProfiler.so=start,event=alloc,file=dump.svg,width=5000"
        );
        stuffOtherJvmUntilItDies(process, generator::next);
    }

    @Test
    void example7TLABInfluenceOnSampleCount() {
        OrderGenerator generator = new OrderGenerator(addresses);
        process = runInSeparateJvm(
                OrderStoreMain.class,
                "-Xmx64m",
                "-XX:TLABSize=64K",
                "-XX:-ResizeTLAB",
                "-XX:+FlightRecorder",
                "-XX:+UnlockDiagnosticVMOptions",
                "-XX:+DebugNonSafepoints",
                "-XX:FlightRecorderOptions:memorysize=100m,stackdepth=32",
                "-XX:StartFlightRecording:name=SampleRecording,settings=memory.jfc,disk=false,dumponexit=true,filename=dump-small-tlab.jfr,maxage=1h"
        );
        stuffOtherJvmUntilItDies(process, generator::next);
    }

    @Test
    void example8EfficientCollections() {
        OrderGenerator generator = new OrderGenerator(addresses);
        process = runInSeparateJvm(
                EfficientCollectionsOrderStoreMain.class,
                "-Xmx64m"
        );
        stuffOtherJvmUntilItDies(process, generator::next);
    }

    @Test
    void example9AvoidAutoboxing() {
        OrderGenerator generator = new OrderGenerator(addresses);
        process = runInSeparateJvm(
                AvoidBoxingOrderStoreMain.class,
                "-Xmx64m"
        );
        stuffOtherJvmUntilItDies(process, generator::next);
    }

    @Test
    void example10SplittingAddress() {
        com.romanmarkunas.blog.memory.example10.OrderGenerator generator
                = new com.romanmarkunas.blog.memory.example10.OrderGenerator(addresses);
        process = runInSeparateJvm(
                BreakingUpAddressOrderStoreMain.class,
                "-Xmx64m"
//                "-XX:+UnlockDiagnosticVMOptions",
//                "-XX:NativeMemoryTracking=summary",
//                "-XX:+PrintNMTStatistics"
        );
        stuffOtherJvmUntilItDies(process, generator::next);
    }

    @Test
    void example11CustomEncoding() {
        com.romanmarkunas.blog.memory.example11.OrderGenerator generator
                = new com.romanmarkunas.blog.memory.example11.OrderGenerator(addresses);
        process = runInSeparateJvm(
                CustomStringEncodingOrderStoreMain.class,
                "-Xmx64m"
        );
        stuffOtherJvmUntilItDies(process, generator::next);
    }

    @Test
    void example12LessReferences() {
        com.romanmarkunas.blog.memory.example11.OrderGenerator generator
                = new com.romanmarkunas.blog.memory.example11.OrderGenerator(addresses);
        process = runInSeparateJvm(
                LessReferencesOrderStoreMain.class,
                "-Xmx64m"
        );
        stuffOtherJvmUntilItDies(process, generator::next);
    }

    @Test
    void example13ObjectCompression() {
        com.romanmarkunas.blog.memory.example11.OrderGenerator generator
                = new com.romanmarkunas.blog.memory.example11.OrderGenerator(addresses);
        process = runInSeparateJvm(
                CompressingOrderStoreMain.class,
                "-Xmx64m"
        );
        stuffOtherJvmUntilItDies(process, generator::next);
    }

    @Test
    void example14CustomStringPool() {
        com.romanmarkunas.blog.memory.example11.OrderGenerator generator
                = new com.romanmarkunas.blog.memory.example11.OrderGenerator(addresses);
        process = runInSeparateJvm(
                CustomStringPoolOrderStoreMain.class,
                "-Xmx64m"
        );
        stuffOtherJvmUntilItDies(process, generator::next);
    }


    private void stuffOtherJvmUntilItDies(final Process process, Supplier<Object> generator) {
        try (BufferedWriter writer = bufferedWriterTo(process)) {
            while (this.process.isAlive()) {
                String order = mapper.writeValueAsString(generator.get());
                writer.write(order + "\n");
                writer.flush();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Process runInSeparateJvm(Class<?> clazz, String... jvmArgs)
    {
        List<String> command = new ArrayList<>();
        command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        command.addAll(asList(jvmArgs));
        command.addAll(asList("-cp", System.getProperty("java.class.path"), clazz.getName()));

        ProcessBuilder builder = new ProcessBuilder(command);

        try {
            return builder
                    // to see child process output as test output
                    .redirectError(INHERIT)
                    .redirectOutput(INHERIT)
                    .start();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static BufferedWriter bufferedWriterTo(final Process process) {
        return new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
    }

    private void giveTimeToPipeOutputFromChildProcess() {
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(5000));
    }
}