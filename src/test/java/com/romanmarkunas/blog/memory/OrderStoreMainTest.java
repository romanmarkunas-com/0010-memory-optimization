package com.romanmarkunas.blog.memory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.romanmarkunas.blog.memory.address.Address;
import com.romanmarkunas.blog.memory.address.AlaskaAddressArchive;
import com.romanmarkunas.blog.memory.example1.OrderGenerator;
import com.romanmarkunas.blog.memory.example1.OrderStoreMain;
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
    void stuffOtherJvmUntilItDiesExample1Initial() throws IOException {
        OrderGenerator generator = new OrderGenerator(addresses);
        process = runInSeparateJvm(OrderStoreMain.class, "-Xmx64m");

        try (BufferedWriter writer = bufferedWriterTo(process)) {
            while (process.isAlive()) {
                String order = mapper.writeValueAsString(generator.next());
                writer.write(order + "\n");
                writer.flush();
            }
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
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000));
    }
}