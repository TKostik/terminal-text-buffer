package org.example;

public record CellAttributes(
        TerminalColor fg,
        TerminalColor bg,
        CellStyle style
) {
    public CellAttributes {
        if (fg == null) throw new IllegalArgumentException("fg must not be null");
        if (bg == null) throw new IllegalArgumentException("bg must not be null");
        if (style == null) throw new IllegalArgumentException("style must not be null");
    }
}