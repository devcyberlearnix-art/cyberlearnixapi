package com.cyberlearnix.commonlibs.test;

import org.junit.jupiter.api.Tag;

/**
 * Base class for integration tests
 * Usage: extend this in service integration tests and add @SpringBootTest, @AutoConfigureMockMvc, etc.
 */
@Tag("integration")
public abstract class BaseIntegrationTest {
    // Services extend this and add Spring Boot test annotations
}
