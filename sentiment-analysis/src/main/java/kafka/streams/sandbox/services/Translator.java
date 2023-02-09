package kafka.streams.sandbox.services;

public class Translator {
    public static String translate(String source, String target, String input) {
        return "TRANSLATED[from '%s' to '%s'] -> %s".formatted(source, target, input);
    }
}
