package com.wellsfargo.counselor.entity;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

public class TestRunner {
    public static void main(String[] args) {
        Class<?> testClass = InvestmentModelTest.class;
        int passed = 0;
        int failed = 0;

        System.out.println("Running " + testClass.getSimpleName() + " tests:");
        for (Method m : testClass.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Test.class)) {
                try {
                    Object instance = testClass.getDeclaredConstructor().newInstance();
                    m.invoke(instance);
                    System.out.println("  [OK] " + m.getName());
                    passed++;
                } catch (Throwable e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    System.err.println("  [FAIL] " + m.getName() + ": " + cause.getMessage());
                    failed++;
                }
            }
        }
        System.out.println("\nSummary: " + passed + " tests passed, " + failed + " failed.");
        if (failed > 0) System.exit(1);
    }
}
