package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppTest {
    @Test
    void terminalBufferIsCreated() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        assertNotNull(buffer);
    }
}
