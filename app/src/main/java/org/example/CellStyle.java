package org.example;


public record CellStyle(boolean bold, boolean italic, boolean underline) {

    public static final CellStyle DEFAULT = new CellStyle(false, false, false);
}
