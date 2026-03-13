package org.example;

import org.junit.jupiter.api.Test;

import java.util.List;

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
        buffer.setCursorPosition(2, 3);

        assertEquals(3, buffer.getCursorColumn());
        assertEquals(2, buffer.getCursorRow());
        assertEquals(new CursorPosition(2, 3), buffer.getCursorPosition());
    }

    @Test
    void cursorMovementStaysWithinBounds() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 100);
        buffer.setCursorPosition(2, 3);

        buffer.moveCursorUp(10);
        assertEquals(new CursorPosition(0, 3), buffer.getCursorPosition());

        buffer.moveCursorLeft(10);
        assertEquals(new CursorPosition(0, 0), buffer.getCursorPosition());

        buffer.moveCursorDown(20);
        assertEquals(new CursorPosition(4, 0), buffer.getCursorPosition());

        buffer.moveCursorRight(20);
        assertEquals(new CursorPosition(4, 9), buffer.getCursorPosition());
    }

    @Test
    void cursorMoveThrowsOnNegativeDistance() {
        TerminalBuffer buffer = new TerminalBuffer(10, 5, 100);

        assertThrows(IllegalArgumentException.class, () -> buffer.moveCursorRight(-1));
    }

    @Test
    void writeTextOnLineOverridesAndMovesCursor() {
        TerminalBuffer buffer = new TerminalBuffer(5, 2, 10);
        buffer.setAttributes(TerminalColor.RED, TerminalColor.BLUE, new CellStyle(true, false, false));
        buffer.setCursorPosition(0, 1);

        buffer.writeTextOnLine("abc");

        assertEquals(" abc ", lineToChars(buffer.screenLine(0)));
        assertEquals(new CursorPosition(0, 4), buffer.getCursorPosition());
        Cell cell = buffer.screenLine(0).get(1);
        assertEquals('a', cell.character());
        assertEquals(TerminalColor.RED, cell.attributes().fg());
        assertEquals(TerminalColor.BLUE, cell.attributes().bg());
        assertEquals(new CellStyle(true, false, false), cell.attributes().style());
    }

    @Test
    void insertTextOnLineWrapsAndMovesCursor() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 10);
        buffer.setCursorPosition(0, 0);
        buffer.writeTextOnLine("ABCD");
        buffer.setCursorPosition(1, 0);
        buffer.writeTextOnLine("WXYZ");

        buffer.setCursorPosition(0, 2);
        buffer.insertTextOnLine("12");

        assertEquals("AB12", lineToChars(buffer.screenLine(0)));
        assertEquals("CDWX", lineToChars(buffer.screenLine(1)));
        assertEquals(new CursorPosition(1, 0), buffer.getCursorPosition());
    }

    @Test
    void fillCurrentLineSupportsCharacterAndEmpty() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 10);
        buffer.setAttributes(TerminalColor.GREEN, TerminalColor.DEFAULT, CellStyle.DEFAULT);
        buffer.setCursorPosition(1, 2);

        buffer.fillCurrentLine('*');
        assertEquals("****", lineToChars(buffer.screenLine(1)));
        assertEquals(TerminalColor.GREEN, buffer.screenLine(1).get(0).attributes().fg());

        buffer.fillCurrentLine(null);
        assertEquals("    ", lineToChars(buffer.screenLine(1)));
        assertEquals(Cell.EMPTY, buffer.screenLine(1).get(0));
    }

    @Test
    void insertEmptyLineAtBottomShiftsAndAddsToScrollback() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 10);
        buffer.setCursorPosition(0, 0);
        buffer.writeTextOnLine("1111");
        buffer.setCursorPosition(1, 0);
        buffer.writeTextOnLine("2222");

        buffer.insertEmptyLineAtBottom();

        assertEquals(1, buffer.getScrollbackSize());
        assertEquals("1111", lineToChars(buffer.scrollbackLine(0)));
        assertEquals("2222", lineToChars(buffer.screenLine(0)));
        assertEquals("    ", lineToChars(buffer.screenLine(1)));
    }

    @Test
    void clearOperationsWorkAsExpected() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 10);
        buffer.setCursorPosition(0, 0);
        buffer.writeTextOnLine("AAAA");
        buffer.setCursorPosition(1, 0);
        buffer.writeTextOnLine("BBBB");
        buffer.insertEmptyLineAtBottom();

        buffer.clearScreen();
        assertEquals("    ", lineToChars(buffer.screenLine(0)));
        assertEquals("    ", lineToChars(buffer.screenLine(1)));
        assertEquals(new CursorPosition(0, 0), buffer.getCursorPosition());
        assertEquals(1, buffer.getScrollbackSize());

        buffer.clearScreenAndScrollback();
        assertEquals("    ", lineToChars(buffer.screenLine(0)));
        assertEquals("    ", lineToChars(buffer.screenLine(1)));
        assertEquals(0, buffer.getScrollbackSize());
    }

    private String lineToChars(List<Cell> line) {
        StringBuilder sb = new StringBuilder(line.size());
        for (Cell cell : line) {
            sb.append(cell.character() == null ? ' ' : cell.character());
        }
        return sb.toString();
    }
}
