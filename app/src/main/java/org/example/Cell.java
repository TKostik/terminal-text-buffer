package org.example;

public record Cell(
        Character character,
        CellAttributes attributes
) {
    public static final Cell EMPTY = new Cell(
            null,
            new CellAttributes(
                    TerminalColor.DEFAULT,
                    TerminalColor.DEFAULT,
                    CellStyle.DEFAULT
            )
    );

    public Cell {
        if (attributes == null) throw new IllegalArgumentException("attributes must not be null");
    }

    public Cell(Character character, TerminalColor fg, TerminalColor bg, CellStyle style) {
        this(character, new CellAttributes(fg, bg, style));
    }
}
