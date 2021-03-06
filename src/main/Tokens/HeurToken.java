package main.Tokens;

import main.Tokens.Token;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class HeurToken {

    private static String logFile = "unclassified.txt";
    private static String negativeWords[] = {" nu ", " lipsit ", " lipsita ", " fara "};

    private static String AskDexonline (String word) throws Exception {

        URL dex = new URL("https://dexonline.ro/definitie/" + word);
        URLConnection con = dex.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

        String inputLine;
        String resp_body = new String();

        while ((inputLine = in.readLine()) != null)
            resp_body+=inputLine;
        in.close();

        return resp_body.substring(resp_body.indexOf("<b>"),resp_body.indexOf("Sursa: <a"));

    }

    private static void logToken (Token token, String word) {
        try {
            BufferedWriter bw = null;
            bw = new BufferedWriter(new FileWriter(logFile, true));
            if (token == null)
                bw.write("(E) ");
            else {
                switch (token.getType()) {
                    case Token.TYPE_FEATURE:
                        bw.write("(F) ");
                        break;
                    case Token.TYPE_ATTRIBUTE:
                        bw.write("(A) ");
                        break;
                    case Token.TYPE_MODIFIER:
                        bw.write("(M) ");
                        break;
                    case Token.TYPE_IGNORE:
                        bw.write("(I) ");
                        break;
                }
            }
            bw.write(word);
            if (token != null && token.getType() != Token.TYPE_IGNORE) {
                bw.write(" : ");
                bw.write(String.valueOf(token.getTokenScore()));
            }
            bw.newLine();
            bw.flush();
            bw.close();
        } catch (IOException e) {
            System.out.println ("Error logging! " + e.toString());
        }
    }


    private static float generateScore (int wordLength, int defLength) {
        return (float) Math.sqrt(wordLength + Math.sqrt(defLength/2));
    }

    public static Token generateHeurToken (String word) {

        if (word.length() < 3)
            return null;

        try {
            String def = AskDexonline(word);
            int type, signum = 1;
            float score;
            switch (def.substring(def.indexOf("title=") + 7, def.indexOf("title=") + 10)) {
                case "sub":
                    type = Token.TYPE_FEATURE;
                    break;
                case "adj":
                case "adv":
                    type = Token.TYPE_ATTRIBUTE;
                    break;
                default:
                    type = Token.TYPE_IGNORE;
            }

            for (String lookFor : negativeWords) {
                if (def.contains(lookFor)) {
                    signum = -1;
                    break;
                }
            }

            score = signum * generateScore(word.length(), def.length());
            Token token = new Token (word, type, score);
            logToken (token, word);
            return token;

        } catch (Exception e) {
            logToken (null, word);
            return null;
        }
    }


}
