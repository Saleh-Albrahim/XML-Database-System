/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataBase;

import Parsing.IntervalLabelling;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import xml.database.project.FXMLDocumentController;

/**
 *
 * @author SAlbr
 */
public class DataBaseManger {

    public Statement st;
    public Statement st2;
    public Connection con;

    public DataBaseManger() {
        try {
            Class.forName("org.h2.Driver");

            con = DriverManager.getConnection("jdbc:h2:~/" + FXMLDocumentController.DatabaseFile.getName().substring(0, FXMLDocumentController.DatabaseFile.getName().length() - 4), "admin", "admin");
            st = con.createStatement();
            st2 = con.createStatement();
        } catch (Exception ex) {
            Logger.getLogger(IntervalLabelling.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void createTables(String TableName) {
        try {

            String sql = "DROP TABLE IF EXISTS `" + TableName + "`;"
                    + "create table `" + TableName + "` ("
                    + " id int NOT NULL AUTO_INCREMENT,"
                    + " label varchar(255) ,"
                    + " value  longtext)";

            st.execute(sql);
        } catch (SQLException ex) {
            Logger.getLogger(DataBaseManger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void insertData(String TableName, String Lable, String value) {
        try {
            while (value.contains("'")) {
                value = value.replace("'", "");
            }

            String sql = "insert into `" + TableName + "` (label,value) Values ('" + Lable + "','" + value + "') ";

            st.execute(sql);
        } catch (SQLException ex) {
            Logger.getLogger(DataBaseManger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void query(String query) {
        try {
            String arr[] = query.split("//");
            String sql = "";
            int count = 0;
            ArrayList<String> sets = new ArrayList<>();
            sql = "select label from  `" + arr[0] + "`";
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                String rootIndex[] = rs.getString("label").split(",");
                count = 0;
                for (int i = 1; i < arr.length; i++) {
                    sql = "select label from `" + arr[i] + "` where "
                            + "SUBSTRING(LABEL  ,0, LOCATE(',', LABEL  ) - 1) > " + rootIndex[0] + " "
                            + "and (SUBSTRING(LABEL ,LOCATE (',', LABEL)+1,LOCATE(',', LABEL,LOCATE(',', LABEL)+1)-LOCATE (',', LABEL)-1) ) < " + rootIndex[1] + "";
                    ResultSet rs_value = st2.executeQuery(sql);
                    if (rs_value.next()) {
                        count++;
                        if (count == arr.length - 1) {
                            sets.add(rs.getString(1));
                        }

                    }

                }
            }
            System.out.print("The Answer of Your Query " + query + " is : ");

            System.out.println(Arrays.toString(sets.toArray()));
            st.close();
            st2.close();
        } catch (SQLException ex) {
            Logger.getLogger(DataBaseManger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
