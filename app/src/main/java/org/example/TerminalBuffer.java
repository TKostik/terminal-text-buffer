package org.example;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TerminalBuffer {

    private final int height;
    private final int width;
    private final int scrollbackMaxSize;
    private final Cell[][] screen;
    private final ArrayDeque<Cell[]> scrollback = new ArrayDeque<>();
    private CursorPosition cursor = new CursorPosition(0, 0);

    private TerminalColor currentFg = TerminalColor.DEFAULT;
    private TerminalColor currentBg = TerminalColor.DEFAULT;
    private CellStyle currentStyle = CellStyle.DEFAULT;

    public TerminalBuffer(int width, int height, int scrollbackMaxSize) {
        if (width <= 0) throw new IllegalArgumentException("width must be positive, got " + width);
        if (height <= 0) throw new IllegalArgumentException("height must be positive, got " + height);
        if (scrollbackMaxSize < 0) throw new IllegalArgumentException("scrollbackMaxSize must be non-negative, got " + scrollbackMaxSize);

        this.width = width;
        this.height = height;
        this.scrollbackMaxSize = scrollbackMaxSize;

        this.screen = new Cell[height][];
        for (int i = 0; i < height; i++) {
            screen[i] = blankLine();
        }
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getScrollbackMaxSize() { return scrollbackMaxSize; }
    public int getScrollbackSize() { return scrollback.size(); }

    // Attributes
    public TerminalColor getCurrentFg() { return currentFg; }
    public TerminalColor getCurrentBg() { return currentBg; }
    public CellStyle getCurrentStyle() { return currentStyle; }

    public void setAttributes(TerminalColor fg, TerminalColor bg, CellStyle style) {
        if (fg == null) throw new IllegalArgumentException("fg must not be null");
        if (bg == null) throw new IllegalArgumentException("bg must not be null");
        if (style == null) throw new IllegalArgumentException("style must not be null");
        this.currentFg = fg;
        this.currentBg = bg;
        this.currentStyle = style;
    }

    // Cursor
    public CursorPosition getCursorPosition() { return cursor; }
    public int getCursorColumn() { return cursor.col(); }
    public int getCursorRow() { return cursor.row(); }

    public void setCursorPosition(int column, int row) {
        cursor = new CursorPosition(clampCol(column), clampRow(row));
    }

    public void moveCursorUp(int n) {
        raiseExcIfNegative(n);
        setCursorPosition(cursor.row() - n, cursor.col());
    }

    public void moveCursorDown(int n) {
        raiseExcIfNegative(n);
        setCursorPosition(cursor.row() + n, cursor.col());
    }

    public void moveCursorLeft(int n) {
        raiseExcIfNegative(n);
        setCursorPosition(cursor.row(), cursor.col() - n);
    }

    public void moveCursorRight(int n) {
        raiseExcIfNegative(n);
        setCursorPosition(cursor.row(), cursor.col() + n);
    }

    // Helpers
    private int clampRow(int row) { return Math.max(0, Math.min(height - 1, row)); }
    private int clampCol(int col) { return Math.max(0, Math.min(width - 1, col)); }

    private void raiseExcIfNegative(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative, got " + n);
    }

    private Cell[] blankLine() {
        Cell[] line = new Cell[width];
        Arrays.fill(line, Cell.EMPTY);
        return line;
    }
}
