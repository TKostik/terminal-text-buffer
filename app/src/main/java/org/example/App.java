package org.example;

public class App {
    public static void main(String[] args) {
        TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
        System.out.println(buffer.getWidth() + " " + buffer.getHeight() + " " + buffer.getScrollbackMaxSize());
    }
}
