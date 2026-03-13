package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TerminalBufferGettersTest {

    @Test
    void dimensionAndScrollbackGettersReturnConfiguredValues() {
        TerminalBuffer buffer = new TerminalBuffer(7, 3, 5);

        assertEquals(7, buffer.getWidth());
        assertEquals(3, buffer.getHeight());
        assertEquals(5, buffer.getScrollbackMaxSize());
        assertEquals(0, buffer.getScrollbackSize());
    }

    @Test
    void currentAttributeGettersReturnLastSetValues() {
        TerminalBuffer buffer = new TerminalBuffer(7, 3, 5);
        CellStyle style = new CellStyle(true, true, true);

        buffer.setAttributes(TerminalColor.CYAN, TerminalColor.BRIGHT_BLACK, style);

        assertEquals(TerminalColor.CYAN, buffer.getCurrentFg());
        assertEquals(TerminalColor.BRIGHT_BLACK, buffer.getCurrentBg());
        assertEquals(style, buffer.getCurrentStyle());
    }

    @Test
    void cursorGettersReflectCurrentCursorPosition() {
        TerminalBuffer buffer = new TerminalBuffer(7, 3, 5);

        buffer.setCursorPosition(2, 6);

        assertEquals(2, buffer.getCursorRow());
        assertEquals(6, buffer.getCursorColumn());
        assertEquals(new CursorPosition(2, 6), buffer.getCursorPosition());
    }

    @Test
    void insertTextAtBottomRightKeepsCursorAtBottomRight() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2, 10);
        buffer.setCursorPosition(1, 2);

        buffer.insertTextOnLine("X");

        assertEquals(new CursorPosition(1, 2), buffer.getCursorPosition());
    }

    @Test
    void insertEmptyLineWithZeroScrollbackLeavesScrollbackEmpty() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2, 0);

        buffer.insertEmptyLineAtBottom();

        assertEquals(0, buffer.getScrollbackSize());
    }

    @Test
    void scrollbackIsCappedAtMaxSize() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2, 2);

        buffer.insertEmptyLineAtBottom();
        buffer.insertEmptyLineAtBottom();
        buffer.insertEmptyLineAtBottom();

        assertEquals(2, buffer.getScrollbackSize());
    }
}
