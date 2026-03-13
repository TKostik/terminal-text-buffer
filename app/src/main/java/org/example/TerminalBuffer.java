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

    public List<Cell> screenLine(int row) {
        if (row < 0 || row >= height) {
            throw new IllegalArgumentException("row out of bounds: " + row);
        }
        return Collections.unmodifiableList(Arrays.asList(screen[row].clone()));
    }

    public List<Cell> scrollbackLine(int index) {
        if (index < 0 || index >= scrollback.size()) {
            throw new IllegalArgumentException("scrollback index out of bounds: " + index);
        }
        Cell[][] snapshot = scrollback.toArray(new Cell[0][]);
        return Collections.unmodifiableList(Arrays.asList(snapshot[index].clone()));
    }

    public Character getScreenCharacterAt(int row, int col) {
        return screenCellAt(row, col).character();
    }

    public Character getScrollbackCharacterAt(int lineIndex, int col) {
        return scrollbackCellAt(lineIndex, col).character();
    }

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

    public void setCursorPosition(int row, int col) {
        cursor = new CursorPosition(clampRow(row), clampCol(col));
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

    // Editing operations
    public void writeTextOnLine(String text) {
        if (text == null) throw new IllegalArgumentException("text must not be null");

        int row = cursor.row();
        int col = cursor.col();
        for (int i = 0; i < text.length() && col < width; i++, col++) {
            screen[row][col] = new Cell(
                text.charAt(i), 
                this.currentFg, 
                this.currentBg, 
                this.currentStyle
            );
        }

        setCursorPosition(row, col);
    }

    public void insertTextOnLine(String text) {
        if (text == null) throw new IllegalArgumentException("text must not be null");

        for (int i = 0; i < text.length(); i++) {
            insertCharAtCursor(text.charAt(i));
            advanceCursorWithWrap();
        }
    }

    public void fillCurrentLine(Character character) {
        Cell fillCell = character == null
                ? Cell.EMPTY
                : new Cell(character, this.currentFg, this.currentBg, this.currentStyle);
        Arrays.fill(this.screen[cursor.row()], fillCell);
    }

    public void insertEmptyLineAtBottom() {
        pushToScrollback(this.screen[0].clone());
        for (int row = 0; row < height - 1; row++) {
            this.screen[row] = this.screen[row + 1];
        }
        this.screen[height - 1] = blankLine();
    }

    public void clearScreen() {
        for (int i = 0; i < height; i++) {
            screen[i] = blankLine();
        }
        setCursorPosition(0, 0);
    }

    public void clearScreenAndScrollback() {
        clearScreen();
        scrollback.clear();
    }

    // Helpers
    private int clampRow(int row) { return Math.max(0, Math.min(height - 1, row)); }
    private int clampCol(int col) { return Math.max(0, Math.min(width - 1, col)); }

    private void raiseExcIfNegative(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative, got " + n);
    }

    private Cell screenCellAt(int row, int col) {
        validateRow(row);
        validateCol(col);
        return screen[row][col];
    }

    private Cell scrollbackCellAt(int lineIndex, int col) {
        if (lineIndex < 0 || lineIndex >= scrollback.size()) {
            throw new IllegalArgumentException("scrollback index out of bounds: " + lineIndex);
        }
        validateCol(col);
        Cell[][] snapshot = scrollback.toArray(new Cell[0][]);
        return snapshot[lineIndex][col];
    }
    private void validateRow(int row) {
        if (row < 0 || row >= height) {
            throw new IllegalArgumentException("row out of bounds: " + row);
        }
    }

    private void validateCol(int col) {
        if (col < 0 || col >= width) {
            throw new IllegalArgumentException("col out of bounds: " + col);
        }
    }

    private void insertCharAtCursor(char ch) {
        int row = cursor.row();
        int col = cursor.col();
        Cell carry = new Cell(ch, this.currentFg, this.currentBg, this.currentStyle);

        for (int r = row; r < height; r++) {
            int startCol = (r == row) ? col : 0;
            for (int c = startCol; c < width; c++) {
                Cell tmp = screen[r][c];
                screen[r][c] = carry;
                carry = tmp;
            }
        }
    }

    private void advanceCursorWithWrap() {
        int row = cursor.row();
        int col = cursor.col();
        if (col < width - 1) {
            setCursorPosition(row, col + 1);
            return;
        }
        if (row < height - 1) {
            setCursorPosition(row + 1, 0);
            return;
        }
        setCursorPosition(height - 1, width - 1);
    }

    private void pushToScrollback(Cell[] line) {
        if (this.scrollbackMaxSize == 0) {
            return;
        }
        scrollback.addLast(line);
        while (scrollback.size() > this.scrollbackMaxSize) {
            scrollback.removeFirst();
        }
    }

    private Cell[] blankLine() {
        Cell[] line = new Cell[width];
        Arrays.fill(line, Cell.EMPTY);
        return line;
    }
}
