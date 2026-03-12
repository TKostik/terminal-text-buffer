package org.example;

public record Cell(
        Character character,
        TerminalColor fg,
        TerminalColor bg,
        CellStyle style
) {
    public static final Cell EMPTY = new Cell(
            null,
            TerminalColor.DEFAULT,
            TerminalColor.DEFAULT,
            CellStyle.DEFAULT
    );

    public Cell {
        if (fg == null) throw new IllegalArgumentException("fg must not be null");
        if (bg == null) throw new IllegalArgumentException("bg must not be null");
        if (style == null) throw new IllegalArgumentException("style must not be null");
    }
}
