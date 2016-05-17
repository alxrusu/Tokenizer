package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class HeurToken {

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

    private static int generateScore (int wordLength, int defLength) {
        return (int) Math.log(wordLength + Math.sqrt(defLength));
    }

    public static Token generateHeurToken (String word) {

        word = word.toLowerCase().trim();
        if (word.length() < 3)
            return null;

        try {
            String def = AskDexonline(word);
            int type = 0;
            switch (def.substring(def.indexOf("title=") + 7, def.indexOf("title=") + 10)) {
                case "sub":
                    type = Token.TYPE_FEATURE;
                    break;
                case "adv":
                    type = Token.TYPE_ATTRIBUTE;
                    break;
                case "ver":
                    type = Token.TYPE_IGNORE;
                    break;
                default:
                    return null;
            }

            return new Token (word, type, generateScore(word.length(), def.length()));

        } catch (Exception e) {
            System.out.println (word + " doesn't exist in the dictionary!");
            return null;
        }
    }


}
