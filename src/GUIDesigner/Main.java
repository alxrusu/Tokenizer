package GUIDesigner;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
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




    public static void InitTokValues() throws IOException {

        features.put("decomandat",2);
        features.put("acces",1);
        features.put("centru",2);
        features.put("zona",1);
        features.put("vecini",1);
        features.put("living",2);
        features.put("terasa",3);
        features.put("birou",2);
        features.put("bucatarie",2);
        features.put("baie",1);
        features.put("bai",2);
        features.put("semineu",2);
        features.put("centrala",1);
        features.put("tamplarie",2);
        features.put("geam",1);
        features.put("geamuri",1);
        features.put("usa",1);


        attributes.put("superb",2);
        attributes.put("deosebit",2);
        attributes.put("rara",1);
        attributes.put("separat",1);
        attributes.put("linistita",2);
        attributes.put("linistiti",2);
        attributes.put("spatios",3);
        attributes.put("calitate",2);
        attributes.put("termopan",2);
        attributes.put("antiefractie",2);

        Path fpath= Paths.get("tokvalues.txt");
        Charset charset = Charset.forName("ISO-8859-1");

        List<String> lines = Files.readAllLines(fpath, charset);

        for (String line : lines) {
            //System.out.println(line);
            String tok=line.substring(0,line.indexOf(" : "));
            Integer value=Integer.parseInt(line.substring(line.indexOf(" : ")+3));
            tokvalues.put(tok,value);
        }

        Path fpath2= Paths.get("ignore.txt");

        List<String> lines2 = Files.readAllLines(fpath2, charset);

        for (String line : lines) {
            ignoredtoks.add(line);
        }

    }

    public static String AskDexonline(String cuvant) throws Exception {
        URL dex = new URL("https://dexonline.ro/definitie/"+cuvant);
        URLConnection con = dex.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        String resp_body="";
        while ((inputLine = in.readLine()) != null)
            resp_body+=inputLine;
        in.close();
        return resp_body.substring(resp_body.indexOf("<p>"),resp_body.indexOf("</p>"));
    }


    public static void LogUnclassified(String tk)
    {
        BufferedWriter bw = null;
        try {
            // APPEND MODE SET HERE
            bw = new BufferedWriter(new FileWriter("unclassified.txt", true));
            bw.write(tk);
            bw.newLine();
            bw.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {                       // always close the file
            if (bw != null) try {
                bw.close();
            } catch (IOException ioe2) {
                // just ignore it
            }
        } // end try/catch/finally
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
                if (!tokvalues.containsKey(tk))
                {
                    tklist.remove(i);
                    i--;
                    if (!ignoredtoks.contains(tk))
                    {
                        //System.out.println("no data found for token: \""+tk+"\" ... Sending it for Classification");
                        //LogUnclassified(tk);
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
