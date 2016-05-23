package main.Utilities;

public class WordCleaner {

    static final String removeCharacters = "0123456789:()\"+";

    public static String clean (String word) {

        word = word.toLowerCase().trim();
        for (char c : removeCharacters.toCharArray()) {
            word = word.replace(Character.toString(c), "");
        }

        return word;

    }

}
