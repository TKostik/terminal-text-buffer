package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TerminalBufferExceptionPathsTest {

    @Test
    void constructorThrowsForInvalidDimensionsAndScrollback() {
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(0, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(1, 1, -1));
    }

    @Test
    void setAttributesThrowsForNullArguments() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2, 1);

        assertThrows(IllegalArgumentException.class,
                () -> buffer.setAttributes(null, TerminalColor.DEFAULT, CellStyle.DEFAULT));
        assertThrows(IllegalArgumentException.class,
                () -> buffer.setAttributes(TerminalColor.DEFAULT, null, CellStyle.DEFAULT));
        assertThrows(IllegalArgumentException.class,
                () -> buffer.setAttributes(TerminalColor.DEFAULT, TerminalColor.DEFAULT, null));
    }

    @Test
    void moveCursorThrowsForNegativeDistanceInAllDirections() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2, 1);

        assertThrows(IllegalArgumentException.class, () -> buffer.moveCursorUp(-1));
        assertThrows(IllegalArgumentException.class, () -> buffer.moveCursorDown(-1));
        assertThrows(IllegalArgumentException.class, () -> buffer.moveCursorLeft(-1));
        assertThrows(IllegalArgumentException.class, () -> buffer.moveCursorRight(-1));
    }

    @Test
    void textEditingThrowsForNullText() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2, 1);

        assertThrows(IllegalArgumentException.class, () -> buffer.writeTextOnLine(null));
        assertThrows(IllegalArgumentException.class, () -> buffer.insertTextOnLine(null));
    }

    @Test
    void screenLineAndContentAccessThrowForOutOfBoundsCoordinates() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2, 1);

        assertThrows(IllegalArgumentException.class, () -> buffer.screenLine(-1));
        assertThrows(IllegalArgumentException.class, () -> buffer.screenLine(2));
        assertThrows(IllegalArgumentException.class, () -> buffer.getScreenLineAsString(-1));

        assertThrows(IllegalArgumentException.class, () -> buffer.getScreenCharacterAt(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> buffer.getScreenCharacterAt(0, -1));
        assertThrows(IllegalArgumentException.class, () -> buffer.getScreenAttributesAt(2, 0));
        assertThrows(IllegalArgumentException.class, () -> buffer.getScreenAttributesAt(0, 3));
    }

    @Test
    void scrollbackAccessThrowsForOutOfBoundsIndexAndColumn() {
        TerminalBuffer buffer = new TerminalBuffer(3, 2, 1);

        assertThrows(IllegalArgumentException.class, () -> buffer.scrollbackLine(0));
        assertThrows(IllegalArgumentException.class, () -> buffer.getScrollbackLineAsString(0));
        assertThrows(IllegalArgumentException.class, () -> buffer.getScrollbackCharacterAt(0, 0));
        assertThrows(IllegalArgumentException.class, () -> buffer.getScrollbackAttributesAt(0, 0));

        buffer.setCursorPosition(0, 0);
        buffer.writeTextOnLine("111");
        buffer.setCursorPosition(1, 0);
        buffer.writeTextOnLine("222");
        buffer.insertEmptyLineAtBottom();

        assertThrows(IllegalArgumentException.class, () -> buffer.scrollbackLine(-1));
        assertThrows(IllegalArgumentException.class, () -> buffer.scrollbackLine(1));
        assertThrows(IllegalArgumentException.class, () -> buffer.getScrollbackCharacterAt(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> buffer.getScrollbackCharacterAt(0, -1));
        assertThrows(IllegalArgumentException.class, () -> buffer.getScrollbackAttributesAt(1, 0));
        assertThrows(IllegalArgumentException.class, () -> buffer.getScrollbackAttributesAt(0, 3));
    }
}
