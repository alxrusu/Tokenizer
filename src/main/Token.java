package main;

public class Token {

    public static final int TYPE_FEATURE = 1;
    public static final int TYPE_ATTRIBUTE = 2;
    public static final int TYPE_MODIFIER = 3;
    public static final int TYPE_IGNORE =0;

    private String string;
    private int type;
    private float score;

    public Token(String string, int type, float score) {
        this.string = string;
        this.type = type;
        if (type == TYPE_IGNORE)
            this.score = 0;
        else
            this.score = score;
    }

    public String getString() {
        return string;
    }

    public int getType() {
        return type;
    }

    public float getTokenScore() {
        return score;
    }

}
