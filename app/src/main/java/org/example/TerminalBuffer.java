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
    public CursorPosition getCursor() { return cursor; }

    private Cell[] blankLine() {
        Cell[] line = new Cell[width];
        Arrays.fill(line, new Cell(null));
        return line;
    }
}
