/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package foodies.db;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author Rehan Ali Azeemi
 */
public class DbConnection {
    
    private static Connection con = null;
    
    private DbConnection(){};
    
    public static Connection getInstance(){
       
        if(con == null){
            try {
              makeConnection();
              con = DriverManager.getConnection("jdbc:mysql://localhost:3306/foodies","root","");       
            } catch (Exception ex) {
                ex.printStackTrace();
                //new MessageForm("Error",ex.toString(),"error.png");
            }
            
        }
        
        return con;
    }
    
    private static void makeConnection() throws Exception{
        Class.forName("com.mysql.jdbc.Driver");
    }
}
