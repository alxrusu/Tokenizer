package main;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class Main {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://85.122.23.50/stefania.baincescu";
    static Map tokvalues=new HashMap<>();
    static Map features=new HashMap<>();
    static Map attributes =new HashMap<>();
    static ArrayList<String> ignoredtoks=new ArrayList<>();
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
            features.put(tok,value);

        }


        lines = Files.readAllLines(attributespath, charset);
        for (String line : lines) {
            //System.out.println(line);
            String tok=line.substring(0,line.indexOf(" : "));
            Integer value=Integer.parseInt(line.substring(line.indexOf(" : ")+3));
            attributes.put(tok,value);
        }


        List<String> lines2 = Files.readAllLines(ignorespath, charset);
        for (String line : lines) {
            ignoredtoks.add(line);
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

    public static void FilterTokens(ArrayList<String> tklist) throws Exception {
        System.out.println(tklist);
        for (int i=0;i<tklist.size();i++)
        {
            String tk=tklist.get(i);

            if (!isInteger(tk))
            {
                if ((!features.containsKey(tk) && !attributes.containsKey(tk)) || ignoredtoks.contains(tk))
                {
                    tklist.remove(i);
                    i--;
                    if (!ignoredtoks.contains(tk))
                    {
                        //System.out.println("no data found for token: \""+tk+"\" ... Sending it for Classification");
                        //HeurToken.generateHeurToken(tk);
                    }
                }
            }
        }
        System.out.println(tklist);
    }

    public static int EvalTokens(ArrayList<String> tklist)
    {

        int sum=0;
        int current_tok=0;
        int num_toks=0;

        for (int i=0;i<tklist.size();i++)
        {
            String tk=tklist.get(i);
            if (features.containsKey(tk)) {
                sum += current_tok;
                num_toks++;
                current_tok= (int) features.get(tk);
                System.out.print("F");
            }
            else
            if (attributes.containsKey(tk)) {
                current_tok*=(int)attributes.get(tk);
                if (current_tok==0) {
                    current_tok = (int) attributes.get(tk);
                    num_toks++;
                }
                System.out.print("A");

            }
        }

        sum += current_tok;
        System.out.print("\n");

        return sum;
    }

    public static void Digest(String descriere) throws Exception {
        while (descriere.contains(","))
            descriere=descriere.substring(0,descriere.indexOf(","))+descriere.substring(descriere.indexOf(",")+1);
        int scor=0;
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
            System.out.println("------------------------------");
            StringTokenizer st = new StringTokenizer(prop);
            ArrayList<String> tklist=new ArrayList<>();
            while (st.hasMoreTokens()) {
                String ctok=st.nextToken().toLowerCase();
                tklist.add(ctok);
                //System.out.println(ctok);
            }

            System.out.println(tklist);
            //FilterTokens(tklist);
            int scor_partial=EvalTokens(tklist);
            System.out.println(scor_partial);
            scor+=scor_partial;
            //System.out.println(prop);
            //------------------------------------
        }
        System.out.println(scor);

    }


    public static void main(String[] args) throws Exception {
        Main app = new Main();
        Connection conn = null;
        Statement stmt = null;
        //System.out.print(AskDexonline("cal"));
        InitTokValues();

        try {
            //STEP 2: Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            stmt = conn.createStatement();
            String sql;

            sql = "SELECT id_proprietate, descriere FROM Proprietati";

            ResultSet rs = stmt.executeQuery(sql);

            int rc=0;
            while (rs.next() && rc<1) {

                int id = rs.getInt("id_proprietate");
                String descriere = rs.getString("descriere");
                rc++;
                Digest(descriere);
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
