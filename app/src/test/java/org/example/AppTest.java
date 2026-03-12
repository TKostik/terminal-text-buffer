package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppTest {
    @Test
    void terminalBufferIsCreated() {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        assertNotNull(buffer);
    }

    @Test
    void cursorPositionCanBeSetAndReadAsColumnRow() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 100);
        buffer.setCursorPosition(3, 2);

        assertEquals(2, buffer.getCursorColumn());
        assertEquals(3, buffer.getCursorRow());
        assertEquals(new CursorPosition(3, 2), buffer.getCursorPosition());
    }

    @Test
    void cursorMovementStaysWithinBounds() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 100);
        buffer.setCursorPosition(3, 2);

        buffer.moveCursorUp(10);
        assertEquals(new CursorPosition(0, 2), buffer.getCursorPosition());

        buffer.moveCursorLeft(10);
        assertEquals(new CursorPosition(0, 0), buffer.getCursorPosition());

        buffer.moveCursorDown(20);
        assertEquals(new CursorPosition(9, 0), buffer.getCursorPosition());

        buffer.moveCursorRight(20);
        assertEquals(new CursorPosition(9, 4), buffer.getCursorPosition());
    }

    @Test
    void cursorMoveThrowsOnNegativeDistance() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 100);

        assertThrows(IllegalArgumentException.class, () -> buffer.moveCursorRight(-1));
    }
}
