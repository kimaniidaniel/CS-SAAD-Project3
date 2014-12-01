/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kimanii
 */
public class Db {
    private final String DATABASENAME = "HeatedPlannet.db";
    private final String GRID_CELLS_TABLE = "SimulatiinGridCells";
    private final String SIMULATION_SETTINGS_TABLE = "SimulationSetttings";
    private final String CREATE_SIMUTATIONSETTINGS =
                                    "CREATE TABLE " + SIMULATION_SETTINGS_TABLE +
                                    "(SimulationId          Long    PRIMARY KEY NOT NULL," +
                                    " Name                  TEXT    NOT NULL" +
                                    " StoragePrecision      INT   "+
                                    " TemporalPrecision     INT   "+
                                    " GeographicaPrecision  INT   "+
                                    " StartDate             Date   "+
                                    " Orbit                 Double   "+
                                    " Tilt                  Double   "+
                                    " GridSpacing           INT   "+
                                    " TimeStep              INT   "+
                                    " Length                INT   "+
                                    " EntryTime             TimeStamp )";

    private final String CREATE_SIMUTATIONGRIDCELLS = 
                                    "CREATE TABLE " + GRID_CELLS_TABLE +
                                    "(GridCellId            Long    PRIMARY KEY NOT NULL," +
                                    " SimulationId          Long    NOT NULL," +
                                    " Latitude              Double " +
                                    " Temperature           Double " +
                                    " Longitude             Double " +
                                    " ReadingDate           Long   "+
                                    " ReadingTime           Long   "+
                                    " FOREIGN KEY(SimulationId) REFERENCES " + CREATE_SIMUTATIONSETTINGS + "(SimulationId))";

    /**
     * Used to retrieve simulated settings
     */
    public List<SimulationSetting> getSimulations(Query query){
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
        String queryString = "INSERT INTO ";
        try {
            Connection conn = Connect(DATABASENAME);
        } catch (Exception ex) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Used to connect to the database
     */
    private Connection Connect(String databaseName) throws Exception{
        Connection c = null;
        databaseName = databaseName.isEmpty() || databaseName.equals(null)?DATABASENAME:databaseName;
        try {
          Class.forName("org.sqlite.JDBC");
          c = DriverManager.getConnection("jdbc:sqlite:"+databaseName);
          verifyDatabase(c);
        } catch ( SQLException e ) {
          System.err.println( e.getClass().getName() + ": " + e.getMessage() );
          Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, e);
          throw e;
        }
        System.out.println("Opened database successfully");
        return c;
    }

    /**
     * This ensures the required data entities in the database exists
     */
    private void verifyDatabase(Connection conn){
        String sqlQuery = "SELECT name FROM sqlite_master WHERE type='@tablename' AND name='@tablename'";
        Statement stmt;
        try {
            stmt = conn.createStatement();
            ResultSet ss = stmt.executeQuery(sqlQuery.replace("@tablename", SIMULATION_SETTINGS_TABLE));
            ResultSet gc = stmt.executeQuery(sqlQuery.replace("@tablename", GRID_CELLS_TABLE));
            if(!ss.next()){
                stmt.execute(CREATE_SIMUTATIONSETTINGS);
            }
            if(!gc.next()){
                stmt.execute(CREATE_SIMUTATIONGRIDCELLS);
            }

        } catch (SQLException ex) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
