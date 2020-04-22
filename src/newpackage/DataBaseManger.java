/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newpackage;

import DataBase.*;
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
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author SAlbr
 */
public class DataBaseManger {

    public Statement st;

    public Connection con;

    public DataBaseManger() {
        try {

            // con = DriverManager.getConnection("jdbc:h2:~/" + FXMLDocumentController.DatabasePath.substring(0, FXMLDocumentController.DatabasePath.length() - 4), "admin", "admin");
            con = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;", "sa", "7540");
            st = con.createStatement();
            String databasePath = FXMLDocumentController.DatabasePath;
            String database = StringUtils.substringAfterLast(databasePath, "\\");
            database = database.substring(0, database.length() - 4);
            String sql = "";
            try {
                sql = "CREATE DATABASE " + database + "";
                st.execute(sql);
            } catch (Exception e) {

            }
            con.setCatalog(database);
            st.close();
            st = con.createStatement();
        } catch (Exception ex) {
            Logger.getLogger(IntervalLabelling.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //create tabels
    public void createTables(String TableName) {
        try {

            String sql = "DROP TABLE IF EXISTS [" + TableName + "] ; "
                    + " create table [" + TableName + "] ("
                    + " id int IDENTITY(1,1) PRIMARY KEY,"
                    + " [start] int ,"
                    + " [finish] int ,"
                    + " [level] int ,"
                    + " [pathID] int ,"
                    + " [value]  VARCHAR(MAX))";

            st.execute(sql);
        } catch (SQLException ex) {
            Logger.getLogger(DataBaseManger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //insert data inside the tabels
    public void insertData(String TableName, String start, String end, String level, String value, int pathID) {
        try {
            while (value.contains("'")) {
                value = value.replace("'", "");
            }

            String sql = "insert into [" + TableName + "] ([start],[finish],[level],[pathID],[value]) "
                    + "Values ('" + start + "','" + end + "','" + level + "','" + pathID + "','" + value + " ')";

            st.execute(sql);
        } catch (SQLException ex) {
            Logger.getLogger(DataBaseManger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //basic query with out where
    public ArrayList<String> query(String query[], int pathID) {
        try {
            ArrayList<String> res = new ArrayList<String>();
            //get the last elemnt form the query
            String table = query[query.length - 1];
            //first part of the query
            String sql = "select [value]  from " + table;
            //query the database
            ResultSet rs = st.executeQuery(sql);
            //get all the result the match the condtion
            while (rs.next()) {
                // add all the result to arraylist
                res.add(rs.getString(1));
            }
            //close connection with the database
            con.close();
            st.close();
            return res;
            //catch if somthing gone wrong
        } catch (SQLException ex) {
            Logger.getLogger(DataBaseManger.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    // query with  where
    public ArrayList<String> query(String query[], int pathID, HashMap<String, String> whereMap, String father) {
        try {
            ArrayList<String> res = new ArrayList<String>();
            String sql = "";
            String table = "";
            //select part of query
            for (int i = 1; i < query.length; i++) {
                table = query[i];
                //we have where
                if (whereMap.get(table) != null) {
                    if (i == query.length - 1) {
                        sql = sql + "( select * from " + table + " where  pathID = " + pathID + " and [value] " + whereMap.get(table) + " ) as " + table + " where ";
                    } else {
                        sql = sql + "( select [start] , [finish] , [level] from " + table + " where [value] " + whereMap.get(table) + "  ) as " + table + " , ";
                    }
                } else {
                    if (i == query.length - 1) {
                        sql = sql + "( select * from " + table + " where  pathID = " + pathID + " ) as " + table + " where ";
                    } else {
                        sql = sql + "( select [start] , [finish] , [level] from " + table + "  ) as " + table + " , ";
                    }

                }
            }

            //where part of the query
            for (int i = 1; i < query.length; i++) {
                if (!father.equals(query[i])) {
                    if (i == query.length - 1) {
                        sql = sql + " " + father + ".start < " + query[i] + ".start and " + father + ".finish > " + query[i] + ".finish ";
                        break;
                    } else {
                        sql = sql + " " + father + ".start < " + query[i] + ".start and " + father + ".finish > " + query[i] + ".finish and";
                    }
                }
            }
            //first part of the query
            sql = "select " + query[query.length - 1] + ".value  from " + sql;
            System.out.println(sql);
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                res.add(rs.getString(1));
            }
            con.close();
            st.close();
            return res;
        } catch (SQLException ex) {
            Logger.getLogger(DataBaseManger.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
