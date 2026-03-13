package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModelValidationAndGettersTest {

    @Test
    void cellAttributesThrowsOnNullFields() {
        assertThrows(IllegalArgumentException.class,
                () -> new CellAttributes(null, TerminalColor.DEFAULT, CellStyle.DEFAULT));
        assertThrows(IllegalArgumentException.class,
                () -> new CellAttributes(TerminalColor.DEFAULT, null, CellStyle.DEFAULT));
        assertThrows(IllegalArgumentException.class,
                () -> new CellAttributes(TerminalColor.DEFAULT, TerminalColor.DEFAULT, null));
    }

    @Test
    void cellThrowsWhenAttributesAreNull() {
        assertThrows(IllegalArgumentException.class, () -> new Cell('x', (CellAttributes) null));
    }

    @Test
    void cellAndRecordAccessorsReturnExpectedValues() {
        CellStyle style = new CellStyle(true, false, true);
        CellAttributes attrs = new CellAttributes(TerminalColor.RED, TerminalColor.BLUE, style);
        Cell cell = new Cell('A', attrs);

        assertEquals('A', cell.character());
        assertEquals(attrs, cell.attributes());
        assertEquals(TerminalColor.RED, cell.attributes().fg());
        assertEquals(TerminalColor.BLUE, cell.attributes().bg());
        assertEquals(style, cell.attributes().style());

        CursorPosition cursor = new CursorPosition(4, 2);
        assertEquals(4, cursor.row());
        assertEquals(2, cursor.col());

        assertEquals(false, CellStyle.DEFAULT.bold());
        assertEquals(false, CellStyle.DEFAULT.italic());
        assertEquals(false, CellStyle.DEFAULT.underline());
    }
}
