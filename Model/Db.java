/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author kimanii
 */
public class Db {

    /**
     * Used to retrieve simulated settings
     */
    public List<SimulationSetting> getSimulationSettings(){
        return null;
    }

    /**
     * Used to retrieve saved simulated settings
     */
    public List<SimulationSetting> getSimulationSettingsByName(String name){
        return null;
    }

    /**
     * Used to save simulated grid
     */
    public void saveSimulationSettings(SimulationSetting ss){

    }

    /**
     * Used to connect to the database
     */
    private Connection Connect(String databaseName) throws Exception{
        Connection c = null;
        databaseName = databaseName.isEmpty() || databaseName.equals(null)?"HeatedPlannet.db":databaseName;
        try {
          Class.forName("org.sqlite.JDBC");
          c = DriverManager.getConnection("jdbc:sqlite:"+databaseName);
        } catch ( ClassNotFoundException | SQLException e ) {
          System.err.println( e.getClass().getName() + ": " + e.getMessage() );
          throw e;
        }
        System.out.println("Opened database successfully");
        return c;
    }

}
