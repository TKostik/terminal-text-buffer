package org.example;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TerminalBuffer {

    private static final char EMPTY_CHAR = '\0';

    private int height;
    private int width;
    private final int scrollbackMaxSize;
    private char[][] screenChars;
    private int[][] screenStyleIds;
    private final ArrayDeque<LineData> scrollback = new ArrayDeque<>();
    private CursorPosition cursor = new CursorPosition(0, 0);

    private TerminalColor currentFg = TerminalColor.DEFAULT;
    private TerminalColor currentBg = TerminalColor.DEFAULT;
    private CellStyle currentStyle = CellStyle.DEFAULT;
    private int currentAttributesId;

    private final List<CellAttributes> attributesById = new ArrayList<>();
    private final Map<CellAttributes, Integer> attributeIds = new HashMap<>();
    private final int defaultAttributesId;

    private static final class LineData {
        private final char[] chars;
        private final int[] styleIds;

        private LineData(char[] chars, int[] styleIds) {
            this.chars = chars;
            this.styleIds = styleIds;
        }
    }

    public TerminalBuffer(int width, int height, int scrollbackMaxSize) {
        if (width <= 0) throw new IllegalArgumentException("width must be positive, got " + width);
        if (height <= 0) throw new IllegalArgumentException("height must be positive, got " + height);
        if (scrollbackMaxSize < 0) throw new IllegalArgumentException("scrollbackMaxSize must be non-negative, got " + scrollbackMaxSize);

        this.width = width;
        this.height = height;
        this.scrollbackMaxSize = scrollbackMaxSize;

        this.defaultAttributesId = internAttributes(new CellAttributes(
                TerminalColor.DEFAULT,
                TerminalColor.DEFAULT,
                CellStyle.DEFAULT
        ));
        this.currentAttributesId = this.defaultAttributesId;

        this.screenChars = new char[height][];
        this.screenStyleIds = new int[height][];
        for (int i = 0; i < height; i++) {
            screenChars[i] = blankCharLine();
            screenStyleIds[i] = blankStyleLine();
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
        return Collections.unmodifiableList(Arrays.asList(cellsFromLine(screenChars[row], screenStyleIds[row])));
    }

    public List<Cell> scrollbackLine(int index) {
        LineData line = scrollbackLineDataAt(index);
        return Collections.unmodifiableList(Arrays.asList(cellsFromLine(line.chars, line.styleIds)));
    }

    public Character getScreenCharacterAt(int row, int col) {
        validateRow(row);
        validateCol(col);
        return charAsNullable(screenChars[row][col]);
    }

    public Character getScrollbackCharacterAt(int lineIndex, int col) {
        validateCol(col);
        return charAsNullable(scrollbackLineDataAt(lineIndex).chars[col]);
    }

    public CellAttributes getScreenAttributesAt(int row, int col) {
        validateRow(row);
        validateCol(col);
        return attributesById.get(screenStyleIds[row][col]);
    }

    public CellAttributes getScrollbackAttributesAt(int lineIndex, int col) {
        validateCol(col);
        return attributesById.get(scrollbackLineDataAt(lineIndex).styleIds[col]);
    }

    public String getScreenLineAsString(int row) {
        validateRow(row);
        return lineToString(screenChars[row]);
    }

    public String getScrollbackLineAsString(int index) {
        return lineToString(scrollbackLineDataAt(index).chars);
    }

    public String getScreenContentAsString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < height; row++) {
            if (row > 0) {
                sb.append('\n');
            }
            sb.append(lineToString(screenChars[row]));
        }
        return sb.toString();
    }

    public String getScreenAndScrollbackContentAsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < scrollback.size(); i++) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(lineToString(scrollbackLineDataAt(i).chars));
        }
        for (int row = 0; row < height; row++) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(lineToString(screenChars[row]));
        }
        return sb.toString();
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
        this.currentAttributesId = internAttributes(new CellAttributes(fg, bg, style));
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
            screenChars[row][col] = text.charAt(i);
            screenStyleIds[row][col] = currentAttributesId;
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
        int row = cursor.row();
        if (character == null) {
            Arrays.fill(this.screenChars[row], EMPTY_CHAR);
            Arrays.fill(this.screenStyleIds[row], defaultAttributesId);
            return;
        }

        Arrays.fill(this.screenChars[row], character);
        Arrays.fill(this.screenStyleIds[row], currentAttributesId);
    }

    public void insertEmptyLineAtBottom() {
        pushToScrollback(cloneScreenLine(0));
        for (int row = 0; row < height - 1; row++) {
            this.screenChars[row] = this.screenChars[row + 1];
            this.screenStyleIds[row] = this.screenStyleIds[row + 1];
        }
        this.screenChars[height - 1] = blankCharLine();
        this.screenStyleIds[height - 1] = blankStyleLine();
    }

    public void clearScreen() {
        for (int i = 0; i < height; i++) {
            screenChars[i] = blankCharLine();
            screenStyleIds[i] = blankStyleLine();
        }
        setCursorPosition(0, 0);
    }

    public void clearScreenAndScrollback() {
        clearScreen();
        scrollback.clear();
    }

    public void resize(int newWidth, int newHeight) {
        if (newWidth <= 0) throw new IllegalArgumentException("newWidth must be positive, got " + newWidth);
        if (newHeight <= 0) throw new IllegalArgumentException("newHeight must be positive, got " + newHeight);

        int oldHeight = this.height;
        char[][] oldScreenChars = this.screenChars;
        int[][] oldScreenStyleIds = this.screenStyleIds;

        char[][] newScreenChars = new char[newHeight][];
        int[][] newScreenStyleIds = new int[newHeight][];
        for (int row = 0; row < newHeight; row++) {
            newScreenChars[row] = blankCharLine(newWidth);
            newScreenStyleIds[row] = blankStyleLine(newWidth);
        }

        int pulledFromScrollback = 0;
        if (newHeight > oldHeight && !scrollback.isEmpty()) {
            pulledFromScrollback = Math.min(newHeight - oldHeight, scrollback.size());
            for (int targetRow = pulledFromScrollback - 1; targetRow >= 0; targetRow--) {
                LineData source = scrollback.removeLast();
                copyLineToWidth(source, newScreenChars[targetRow], newScreenStyleIds[targetRow], newWidth);
            }
        }

        int rowsToCopy = Math.min(oldHeight, newHeight);
        int sourceStartRow = oldHeight - rowsToCopy;
        int targetStartRow = pulledFromScrollback;

        if (newHeight < oldHeight) {
            for (int row = 0; row < sourceStartRow; row++) {
                pushToScrollback(cloneLineData(oldScreenChars[row], oldScreenStyleIds[row]));
            }
        }

        for (int rowOffset = 0; rowOffset < rowsToCopy && targetStartRow + rowOffset < newHeight; rowOffset++) {
            int sourceRow = sourceStartRow + rowOffset;
            int targetRow = targetStartRow + rowOffset;
            copyLineToWidth(
                    oldScreenChars[sourceRow],
                    oldScreenStyleIds[sourceRow],
                    newScreenChars[targetRow],
                    newScreenStyleIds[targetRow],
                    newWidth
            );
        }

        this.screenChars = newScreenChars;
        this.screenStyleIds = newScreenStyleIds;
        this.width = newWidth;
        this.height = newHeight;
        resizeScrollbackLinesToWidth(newWidth);

        int resizedCursorRow = cursor.row();
        if (newHeight < oldHeight) {
            int droppedRows = oldHeight - newHeight;
            resizedCursorRow = resizedCursorRow - droppedRows;
        } else if (newHeight > oldHeight) {
            resizedCursorRow = resizedCursorRow + pulledFromScrollback;
        }

        setCursorPosition(resizedCursorRow, cursor.col());
    }

    // Helpers
    private int clampRow(int row) { return Math.max(0, Math.min(height - 1, row)); }
    private int clampCol(int col) { return Math.max(0, Math.min(width - 1, col)); }

    private void raiseExcIfNegative(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative, got " + n);
    }

    private LineData scrollbackLineDataAt(int lineIndex) {
        if (lineIndex < 0 || lineIndex >= scrollback.size()) {
            throw new IllegalArgumentException("scrollback index out of bounds: " + lineIndex);
        }
        int i = 0;
        for (LineData line : scrollback) {
            if (i == lineIndex) {
                return line;
            }
            i++;
        }
        throw new IllegalStateException("scrollback index not found: " + lineIndex);
    }

    private String lineToString(char[] chars) {
        StringBuilder sb = new StringBuilder(chars.length);
        for (char ch : chars) {
            sb.append(ch == EMPTY_CHAR ? ' ' : ch);
        }
        return sb.toString();
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
        char carryChar = ch;
        int carryStyleId = currentAttributesId;

        for (int r = row; r < height; r++) {
            int startCol = (r == row) ? col : 0;
            for (int c = startCol; c < width; c++) {
                char tmpChar = screenChars[r][c];
                int tmpStyleId = screenStyleIds[r][c];

                screenChars[r][c] = carryChar;
                screenStyleIds[r][c] = (carryChar == EMPTY_CHAR) ? defaultAttributesId : carryStyleId;

                carryChar = tmpChar;
                carryStyleId = tmpStyleId;
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

    private void pushToScrollback(LineData line) {
        if (this.scrollbackMaxSize == 0) {
            return;
        }
        scrollback.addLast(line);
        while (scrollback.size() > this.scrollbackMaxSize) {
            scrollback.removeFirst();
        }
    }

    private char[] blankCharLine() {
        return blankCharLine(this.width);
    }

    private char[] blankCharLine(int targetWidth) {
        return new char[targetWidth];
    }

    private int[] blankStyleLine() {
        return blankStyleLine(this.width);
    }

    private int[] blankStyleLine(int targetWidth) {
        int[] line = new int[targetWidth];
        Arrays.fill(line, defaultAttributesId);
        return line;
    }

    private void copyLineToWidth(LineData source, char[] targetChars, int[] targetStyleIds, int targetWidth) {
        copyLineToWidth(source.chars, source.styleIds, targetChars, targetStyleIds, targetWidth);
    }

    private void copyLineToWidth(char[] sourceChars, int[] sourceStyleIds, char[] targetChars, int[] targetStyleIds, int targetWidth) {
        int copyLength = Math.min(sourceChars.length, targetWidth);
        System.arraycopy(sourceChars, 0, targetChars, 0, copyLength);
        System.arraycopy(sourceStyleIds, 0, targetStyleIds, 0, copyLength);
    }

    private void resizeScrollbackLinesToWidth(int targetWidth) {
        if (scrollback.isEmpty()) {
            return;
        }

        ArrayDeque<LineData> resized = new ArrayDeque<>(scrollback.size());
        for (LineData oldLine : scrollback) {
            char[] resizedChars = blankCharLine(targetWidth);
            int[] resizedStyleIds = blankStyleLine(targetWidth);
            int copyLength = Math.min(oldLine.chars.length, targetWidth);
            System.arraycopy(oldLine.chars, 0, resizedChars, 0, copyLength);
            System.arraycopy(oldLine.styleIds, 0, resizedStyleIds, 0, copyLength);
            resized.addLast(new LineData(resizedChars, resizedStyleIds));
        }

        scrollback.clear();
        scrollback.addAll(resized);
    }

    private LineData cloneScreenLine(int row) {
        return cloneLineData(screenChars[row], screenStyleIds[row]);
    }

    private LineData cloneLineData(char[] chars, int[] styleIds) {
        return new LineData(chars.clone(), styleIds.clone());
    }

    private Cell[] cellsFromLine(char[] chars, int[] styleIds) {
        Cell[] result = new Cell[chars.length];
        for (int i = 0; i < chars.length; i++) {
            result[i] = cellFromRaw(chars[i], styleIds[i]);
        }
        return result;
    }

    private Cell cellFromRaw(char ch, int styleId) {
        if (ch == EMPTY_CHAR) {
            return Cell.EMPTY;
        }
        return new Cell(ch, attributesById.get(styleId));
    }

    private Character charAsNullable(char ch) {
        if (ch == EMPTY_CHAR) {
            return null;
        }
        return ch;
    }

    private int internAttributes(CellAttributes attrs) {
        Integer existingId = attributeIds.get(attrs);
        if (existingId != null) {
            return existingId;
        }
        int newId = attributesById.size();
        attributesById.add(attrs);
        attributeIds.put(attrs, newId);
        return newId;
    }
}
