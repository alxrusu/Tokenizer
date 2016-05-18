package main;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class Main {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://85.122.23.50/stefania.baincescu";
    static Map tokens=new HashMap<>();
    //  Database credentials
    static final String USER = "stefania.baincescu";
    static final String PASS = "Alex0974";

    static Path ignorespath= Paths.get("ignore.txt");
    static Path featurespath= Paths.get("features.txt");
    static Path attributespath= Paths.get("attributes.txt");
    static Charset charset = Charset.forName("ISO-8859-1");

    public static void InitTokValues() throws IOException {

        List<String> lines = Files.readAllLines(featurespath, charset);
        for (String line : lines) {
            //System.out.println(line);
            String tok=line.substring(0,line.indexOf(" : "));
            Integer value=Integer.parseInt(line.substring(line.indexOf(" : ")+3));
            tokens.put(tok,new Token(tok,Token.TYPE_FEATURE,value));
        }


        lines = Files.readAllLines(attributespath, charset);
        for (String line : lines) {
            //System.out.println(line);
            String tok=line.substring(0,line.indexOf(" : "));
            Integer value=Integer.parseInt(line.substring(line.indexOf(" : ")+3));
            tokens.put(tok,new Token(tok,Token.TYPE_ATTRIBUTE,value));
        }


        lines = Files.readAllLines(ignorespath, charset);
        for (String line : lines) {
            tokens.put(line,new Token(line,Token.TYPE_IGNORE,0));
        }

    }


    public static boolean isInteger(String s) {
        return isInteger(s,10);
    }

    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }

    public static int EvalTokens(ArrayList<String> tklist) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        int sum=0;
        int current_tok=0;
        int num_toks=0;

        for (String tk : tklist) {

            Token temptk;

            if (tokens.containsKey(tk)) {
                temptk = (Token) tokens.get(tk);
            } else {
                temptk = HeurToken.generateHeurToken(tk);
                if (temptk == null)
                    continue;
            }


            switch (temptk.getType()) {
                case Token.TYPE_FEATURE:
                    sum += current_tok;
                    num_toks++;
                    current_tok = temptk.getScore();
                    System.out.print("F");
                    break;

                case Token.TYPE_ATTRIBUTE:
                    current_tok += temptk.getScore(); //PLUS SI VEDEM NOI MAI INCOLO - Lucian - 2016
                    if (current_tok == 0) {
                        current_tok = temptk.getScore();
                        num_toks++;
                    }
                    System.out.print("A");
                    break;

                case Token.TYPE_IGNORE:
                    System.out.print("I");

            }

        }

        sum += current_tok;
        System.out.print("\n");

        if (num_toks==0)
            return 0;
        return sum/num_toks;
    }

    public static int Digest(String descriere) throws Exception {
        while (descriere.contains(","))
            descriere=descriere.substring(0,descriere.indexOf(","))+descriere.substring(descriere.indexOf(",")+1);
        int scor=0;
        int nr_propozitii=0;
        while (descriere.length()>0) {
            int findex=descriere.indexOf(".");
            if (descriere.indexOf("!")<findex && descriere.contains("!"))
                findex=descriere.indexOf("!");
            if (descriere.indexOf("?")<findex && descriere.contains("?"))
                findex=descriere.indexOf("?");
            if (descriere.indexOf(";")<findex  && descriere.contains(";"))
                findex=descriere.indexOf(";");
            if (findex==-1)
                break;
            String prop=descriere.substring(0,findex);
            descriere=descriere.substring(findex+1);

            //------------------------------------
            //-----Tokenizare pe propozitii-------
            //System.out.println("------------------------------");
            StringTokenizer st = new StringTokenizer(prop);
            ArrayList<String> tklist=new ArrayList<>();
            while (st.hasMoreTokens()) {
                String ctok=st.nextToken().toLowerCase();
                tklist.add(ctok);
            }

            //System.out.println(tklist);
            int scor_partial=EvalTokens(tklist);
            //System.out.println(scor_partial);
            scor+=scor_partial;
            nr_propozitii++;
            //System.out.println(prop);
            //------------------------------------
        }
        if (nr_propozitii<=1)
            return scor;
        return (int) (scor/(1 + Math.log (nr_propozitii)));
    }


    public static void main(String[] args) throws Exception {
        Main app = new Main();
        Connection conn = null;
        Statement stmt = null;

        InitTokValues();

        int avg_scores=0;
        int numscores=0;

        try {

            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            stmt = conn.createStatement();
            String sql;

            sql = "SELECT id_proprietate, descriere, evaluare FROM Proprietati";
            ResultSet rs = stmt.executeQuery(sql);

            int rc=0;
            int scor_minim=9999999;
            int scor_maxim=-1;

            Map scores=new HashMap<>();
            int counter=0;
            while (rs.next() && rc<10) {
                System.out.println("Computing Score for : "+counter++);
                int scor_anterior=rs.getInt("evaluare");
                int id = rs.getInt("id_proprietate");
                String descriere = rs.getString("descriere");
                //rc++;
                //System.out.println("OLD : "+scor_anterior);
                int scor=Digest(descriere);

                if (scor<scor_minim) scor_minim=scor;
                if (scor>scor_maxim) scor_maxim=scor;

                avg_scores+=scor;
                numscores++;
                System.out.println( "Total Score : " + scor);
                scores.put(id,scor);
            }

            System.out.println("Scor Minim : "+ scor_minim);
            avg_scores/=numscores;
            System.out.println("Scor Mediu : "+ avg_scores);
            System.out.println("Scor Maxim : "+ scor_maxim);

            counter=0;
            for (Object id:scores.keySet())
            {
                System.out.println("Updating Score for : "+counter++);
                int db_score=(Integer) scores.get(id);
                if (db_score>avg_scores)
                {
                    db_score= (int) ((db_score-avg_scores)*5.5/(scor_maxim - avg_scores));
                }
                else
                {
                    db_score= -(int) ((-db_score+avg_scores)*5.5/(-scor_minim + avg_scores));
                }
                String SQLString1 = "UPDATE Proprietati "
                        + "SET evaluare = ? "
                        + "WHERE id_proprietate = ?";
                PreparedStatement statement = null;
                try {
                    statement = conn.prepareStatement(SQLString1);
                    statement.setInt(1, db_score);
                    statement.setInt(2, (Integer) id);
                    statement.executeUpdate();
                    System.out.println("DB score for "+id+" is "+db_score);
                }catch (Exception e){
                    System.out.println("Update error!");
                    System.out.println(e.getMessage());
                }finally {
                    try {
                        assert statement != null;
                        statement.close();
                    } catch (SQLException e) {
                        System.out.println("Statement close error!");
                        System.out.println(e.getMessage());
                    }
                }

            }


            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException ignored) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

    }
}
