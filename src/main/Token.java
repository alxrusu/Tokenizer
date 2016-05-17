package main;

public class Token {

    public static final int TYPE_FEATURE = 1;
    public static final int TYPE_ATTRIBUTE = 2;
    public static final int TYPE_IGNORE =0;

    private String value;
    private int type;
    private int score;

    public Token(String value, int type, int score) {
        this.value = value;
        this.type = type;
        this.score = score;
    }

    public String getValue() {
        return value;
    }

    public int getType() {
        return type;
    }

    public int getScore() {
        return score;
    }
}
