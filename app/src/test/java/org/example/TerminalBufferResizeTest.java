package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TerminalBufferResizeTest {

    @Test
    void resizeThrowsForInvalidDimensions() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 3);

        assertThrows(IllegalArgumentException.class, () -> buffer.resize(0, 2));
        assertThrows(IllegalArgumentException.class, () -> buffer.resize(4, 0));
        assertThrows(IllegalArgumentException.class, () -> buffer.resize(-1, 2));
        assertThrows(IllegalArgumentException.class, () -> buffer.resize(4, -1));
    }

    @Test
    void resizeLargerPullsFromScrollbackAndKeepsVisibleContentAtBottom() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2, 5);
        buffer.setCursorPosition(0, 0);
        buffer.writeTextOnLine("ABC");
        buffer.setCursorPosition(1, 0);
        buffer.writeTextOnLine("DEF");
        buffer.insertEmptyLineAtBottom();
        buffer.setCursorPosition(1, 2);

        buffer.resize(5, 3);

        assertEquals(5, buffer.getWidth());
        assertEquals(3, buffer.getHeight());
        assertEquals("ABC  ", buffer.getScreenLineAsString(0));
        assertEquals("DEF  ", buffer.getScreenLineAsString(1));
        assertEquals("     ", buffer.getScreenLineAsString(2));
        assertEquals(0, buffer.getScrollbackSize());
        assertEquals(new CursorPosition(2, 2), buffer.getCursorPosition());
    }

    @Test
    void resizeSmallerKeepsBottomRowsAndMovesTopRowsToScrollback() {
        TerminalBuffer buffer = new TerminalBuffer(5, 3, 5);
        buffer.setCursorPosition(0, 0);
        buffer.writeTextOnLine("ABCDE");
        buffer.setCursorPosition(1, 0);
        buffer.writeTextOnLine("FGHIJ");
        buffer.setCursorPosition(2, 0);
        buffer.writeTextOnLine("KLMNO");
        buffer.setCursorPosition(2, 4);

        buffer.resize(3, 2);

        assertEquals(3, buffer.getWidth());
        assertEquals(2, buffer.getHeight());
        assertEquals("FGH", buffer.getScreenLineAsString(0));
        assertEquals("KLM", buffer.getScreenLineAsString(1));
        assertEquals("ABC", buffer.getScrollbackLineAsString(0));
        assertEquals(new CursorPosition(1, 2), buffer.getCursorPosition());
    }

    @Test
    void resizeAlsoResizesScrollbackLineWidth() {
        TerminalBuffer buffer = new TerminalBuffer(4, 2, 5);
        buffer.setCursorPosition(0, 0);
        buffer.writeTextOnLine("1111");
        buffer.setCursorPosition(1, 0);
        buffer.writeTextOnLine("2222");
        buffer.insertEmptyLineAtBottom();

        assertEquals("1111", buffer.getScrollbackLineAsString(0));

        buffer.resize(2, 2);
        assertEquals("11", buffer.getScrollbackLineAsString(0));

        buffer.resize(5, 2);
        assertEquals("11   ", buffer.getScrollbackLineAsString(0));
    }
}
