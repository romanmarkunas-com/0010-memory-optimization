package com.romanmarkunas.blog.memory.example14.checkers;

import com.romanmarkunas.blog.memory.example14.checkers.checkerstestcases.immutablebytearray.ByteArrayWriteAccesses;
import org.assertj.core.util.Strings;
import org.checkerframework.framework.test.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

class ImmutableByteArrayCheckerTest {

    @Test
    public void run() {
        TestConfiguration config = TestConfigurationBuilder.buildDefaultConfiguration(
                "",
                singletonList(sourceFileOf(ByteArrayWriteAccesses.class)),
                emptyList(),
                singletonList(ImmutableByteArrayChecker.class.getName()),
                singletonList("-Anomsgtext"),
                false
        );
        TypecheckResult testResult = new TypecheckExecutor().runTest(config);
        TestUtilities.assertResultsAreValid(testResult);
    }

    private File sourceFileOf(Class<?> clazz) {
        var dirs = new ArrayList<>(List.of("src", "test", "java"));
        dirs.addAll(asList(clazz.getCanonicalName().split("\\.")));
        return new File(Strings.join(dirs).with(File.separator) + ".java");
    }
}
