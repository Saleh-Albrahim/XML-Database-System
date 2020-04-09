/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataBase;

import Parsing.IntervalLabelling;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import Main.FXMLDocumentController;
import com.mysql.jdbc.Util;
import java.util.HashSet;

/**
 *
 * @author SAlbr
 */
public class DataBaseManger {

    public Statement st;

    public Connection con;

    public DataBaseManger() {
        try {
            Class.forName("org.h2.Driver");

            // con = DriverManager.getConnection("jdbc:h2:~/" + FXMLDocumentController.DatabaseFile.getName().substring(0, FXMLDocumentController.DatabaseFile.getName().length() - 4), "admin", "admin");
            con = DriverManager.getConnection("jdbc:h2:~/booksDB", "admin", "admin");

            st = con.createStatement();

        } catch (Exception ex) {
            Logger.getLogger(IntervalLabelling.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void createTables(String TableName) {
        try {
            String sql = " DROP TABLE IF EXISTS `" + TableName + "`;"
                    + " create table `" + TableName + "` ("
                    + " id int NOT NULL AUTO_INCREMENT,"
                    + " label varchar(255) ,"
                    + " value  longtext ,"
                    + " pathID int ,"
                    + " father varchar(255) )";
            st.execute(sql);
        } catch (SQLException ex) {
            Logger.getLogger(DataBaseManger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void insertData(String TableName, String Lable, String value, int pathID, String father) {
        try {
            while (value.contains("'")) {
                value = value.replace("'", "");
            }

            String sql = "insert into `" + TableName + "` (label,value,pathID,father) Values ('" + Lable + "','" + value + "','" + pathID + "','" + father + " ')";

            st.execute(sql);
        } catch (SQLException ex) {
            Logger.getLogger(DataBaseManger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList<String> query(String table, int index) {
        try {
            ArrayList<String> res = new ArrayList<String>();
            String sql = "select * from  `" + table + "` where pathID = '" + index + "'";
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                res.add(rs.getString("label") + "   " + rs.getString("value"));
            }
            con.close();
            st.close();
            return res;
        } catch (SQLException ex) {
            Logger.getLogger(DataBaseManger.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public ArrayList<String> query(String table, int index, String[] whereList, HashSet<String> FatherCheck, ArrayList<String> uniqueNode) {
        try {

            ArrayList<String> arrValues = new ArrayList<>();
            String create = "";
            for (int i = 0; i < uniqueNode.size(); i++) {
                System.out.println(FatherCheck.toArray()[i]);
                if (i == uniqueNode.size() - 1) {
                    create = create + uniqueNode.get(i) + " longtext )";
                    break;
                }
                create = create + uniqueNode.get(i) + " longtext,";

            }
            create = "DROP TABLE IF EXISTS `Query`; create table `Query` (" + create;
            st.execute(create);

            ArrayList<String> res = new ArrayList<String>();
            ArrayList<String> list = new ArrayList<String>();

            for (int i = 0; i < FatherCheck.size(); i++) {
                arrValues = new ArrayList<>();
                for (int j = 0; j < uniqueNode.size(); j++) {
                    String sql = "select value from `" + uniqueNode.get(j) + "`  where father = '" + FatherCheck.toArray()[i] + "'";
                    System.out.println(sql);
                    ResultSet rs = st.executeQuery(sql);
                    if (rs.next()) {
                        System.out.println(rs.getString(1));
                        arrValues.add(rs.getString(1));
                    }
                }
//                String insert = "";
//                for (int j = 0; j < arrValues.size(); j++) {
//                    if (j == arrValues.size() - 1) {
//                        insert = insert + arrValues.get(j) + ")";
//
//                    }
//                    insert = insert + " , " + arrValues.get(j);
//
//                }
//                insert = "insert into `Query` (" + insert;
//                System.out.println(insert);
//                st.execute(insert);

            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }

}
