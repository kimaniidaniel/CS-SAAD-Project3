//Results from all simulations should be persistently stored. In addition to the Physical Factors, Simulation Settings, and Invocation Parameters saved for each simulation, the following data should be stored for each grid cell:
//
//Latitude: degrees North or South of the Equator
//Longitude: degrees East or West of the Prime Meridian
//Temperature: degrees Kelvin
//Reading date: simulated date at which the temperature reading was taken in terms of years and days since the start of the simulation
//Reading time: hours and minutes since the start of the Reading Date
//
//-p #: The precision of the data to be stored, in decimal digits after the decimal point. The default is to use the number of digits storable in a normalized float variable. The maximum is the number of digits storable in a normalized double variable. The minimum is zero.
//-g #: The geographic precision (sampling rate) of the temperature data to be stored, as an integer percentage of the number of grid cells saved versus the number simulated. The default is 100%; that is, a value is stored for each grid cell.
//-t #: The temporal precision of the temperature data to be stored, as an integer percentage of the number of time periods saved versus the number computed. The default is 100%; that is, all computed values should be stored.
package PlanetSim;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class DBModel
{

    private final String DATABASENAME = "PlanetSim.db";
    private final String PLANET_CELLS_TBL = "PLANET_CELLS_TBL";
    private final String SIM_CONFIG_TBL = "SIM_CONFIG_TBL";
    //	CREATE TABLE IF NOT EXISTS command checks and sees if the DB exists... if not it will create
    private final String CREATE_SIM_CONFIG_TBL
            = "CREATE TABLE IF NOT EXISTS " + SIM_CONFIG_TBL
            + "(CONFIG_ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
            + " Name TEXT NOT NULL,"
            + " StoragePrecision INTEGER,"
            + " TemporalPrecision INTEGER,"
            + " GeographicalPrecision INTEGER,"
            + " StartDate TEXT,"
            + " Orbit Double,"
            + " Tilt Double,"
            + " GridSpacing INTEGER,"
            + " TimeStep INTEGER,"
            + " Length INTEGER,"
            + " EntryTime TimeStamp )";
    //	CREATE TABLE IF NOT EXISTS command checks and sees if the DB exists... if not it will create
    private final String CREATE_PLANET_CELLS_TBL
            = "CREATE TABLE IF NOT EXISTS " + PLANET_CELLS_TBL
            + " (CELL_ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
            + "CONFIG_ID INTEGER NOT NULL,"
            + "Latitude DOUBLE,"
            + "Temperature DOUBLE,"
            + "Longitude DOUBLE,"
            + "Step INTEGER,"
            + "Reading_Date LONG,"
            + "Reading_Time LONG,"
            + "TransActionTime TEXT,"
            + "FOREIGN KEY(CONFIG_ID) REFERENCES " + SIM_CONFIG_TBL + "(CONFIG_ID))";

    private boolean debug = false;							//used for debugging
    private long transactionCounter = 0;					//used to adjust performance counter for comparison
    private long MAX_NUMBER_TRANSACTION_COUNTER = 50000;	//used to adjust performance max size of the counter
    private long MAX_NUM_TRANS_TILL_COMMIT = 100000;	//used to adjust performance affects CPU
    private int ITER_FOR_GARBAGE_COLLECTION = 10000;	//used to adjust performance affects CPU
    // current simulation settings
    private int storagePrecision;
    private int temporalPrecision = 100;
    private int geographicalPrecision = 100;
    private String startDate;
    private double orbit;
    private double tilt;
    private int gridSpacing;
    private int timeStep;
    private int length;
    private String name;
    private int CONFIG_ID;
    // current simulation settings
    private boolean previousSimDetected = false;			//flag to disable storage if previously stored
    private int geoPrecCtr = 0;							//used to track precision
    private int tempPrecCtr = 0;						//used to track precision

    private Connection conn;

    public DBModel()
    {
        try {
            this.conn = Connect(DATABASENAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DBModel(String Name, int StoragePrecision, int TemporalPrecision, int GeographicalPrecision, String StartDate, double Orbit, double Tilt, int GridSpacing, int TimeStep, int Length)
    {
        System.out.println("OPENNING DBMODEL THROUGH SIMCONFIGURATION");
        try {

            this.conn = Connect(DATABASENAME);
            this.name = ("".equals(Name)) ? "Sim_" + this.getTimeStamp() : Name;
            updateConfig(this.name, StoragePrecision, TemporalPrecision, GeographicalPrecision, StartDate, Orbit, Tilt, GridSpacing, TimeStep, Length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //used for debugging

    public DBModel(boolean debug, String Name, int StoragePrecision, int TemporalPrecision, int GeographicalPrecision, String StartDate, double Orbit, double Tilt, int GridSpacing, int TimeStep, int Length)
    {

        this.debug = debug;
        try {

            this.name = ("".equals(Name)) ? "Sim" + this.getTimeStamp() : Name;
            this.storagePrecision = StoragePrecision;
            this.temporalPrecision = TemporalPrecision;
            this.geographicalPrecision = GeographicalPrecision;
            this.startDate = StartDate;
            this.orbit = Orbit;
            this.tilt = Tilt;
            this.gridSpacing = GridSpacing;
            this.timeStep = TimeStep;
            this.length = Length;
            this.conn = Connect(DATABASENAME);
            getConfig_ID();//this will set the CONFIG_ID if found or add to the table and return index if not
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //used for debugging

    public DBModel(boolean debug)
    {
        this.debug = debug;
        try {
            this.conn = Connect(DATABASENAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertConfigData(String Name, int StoragePrecision, int TemporalPrecision, int GeographicalPrecision, String StartDate, double Orbit, double Tilt, int GridSpacing, int TimeStep, int Length, String EntryTime) throws SQLException
    {

        String statement = "INSERT INTO " + SIM_CONFIG_TBL + " (Name,StoragePrecision,TemporalPrecision,GeographicalPrecision,StartDate,Orbit,Tilt,GridSpacing,TimeStep,Length,EntryTime) "
                + "VALUES ('" + Name + "'," + StoragePrecision + "," + TemporalPrecision + "," + GeographicalPrecision + ",'" + StartDate + "'," + Orbit + "," + Tilt + "," + GridSpacing + "," + TimeStep + "," + Length + "," + EntryTime + ");";
        try (Statement stmt = this.conn.createStatement()) {
            stmt.executeUpdate(statement);
            this.conn.commit();
        }
        cleanUp();

    }

    public void insertGridData(double Latitude, double Longitude, double Temperature, int Step, String Reading_Date, String Reading_Time, int CONFIG_ID) throws SQLException
    {

        String statement = "INSERT INTO " + PLANET_CELLS_TBL + " (CONFIG_ID,Latitude,Longitude,Temperature,Step,Reading_Date,Reading_Time,TransActionTime) "
                + "VALUES (" + CONFIG_ID + "," + Latitude + "," + Latitude + "," + Temperature + "," + Step + ",'" + Reading_Date + "','" + Reading_Time + "','" + getTimeStamp() + "');";
        try (Statement stmt = this.conn.createStatement()) {
            stmt.executeUpdate(statement);
        }
        cleanUp();

    }

    private Connection Connect(String databaseName) throws Exception
    {
        Connection c = null;
        //this is for debugging creates new db with every connection
        if (this.isDebug()) {
            databaseName = databaseName.concat(getTimeStamp());
        }

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + databaseName);		// creates db if not present or open if it is
            c.setAutoCommit(false);											// turns off AutoCommit for performance issues
            try (Statement stmt = c.createStatement()) {
                if (this.isDebug()) {
                    System.out.println("Creating:" + SIM_CONFIG_TBL);
                }
                stmt.execute(CREATE_SIM_CONFIG_TBL);								// creates table SIM_CONFIG_TBL if not present
                if (this.isDebug()) {
                    System.out.println("Creating:" + PLANET_CELLS_TBL);
                }
                stmt.execute(CREATE_PLANET_CELLS_TBL);							// creates table PLANET_CELLS_TBL if not present
                c.commit();
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        if (this.isDebug()) {
            System.out.println("Opened database successfully");
        }
        return c;
    }

    //returns system.time for trouble shooting and diagnostics

    private String getTimeStamp()
    {
        return (new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()));
    }

    //returns the id of previous simulation or the new one created

    public void getConfig_ID()
    {
        String statement = "SELECT CONFIG_ID FROM " + SIM_CONFIG_TBL + " WHERE StoragePrecision = " + this.storagePrecision
                + " AND TemporalPrecision = " + this.temporalPrecision
                + " AND GeographicalPrecision = " + this.temporalPrecision
                + " AND StartDate = '" + this.startDate
                + "' AND Orbit = " + this.orbit
                + " AND Tilt = " + this.tilt
                + " AND GridSpacing = " + this.gridSpacing
                + " AND TimeStep = " + this.timeStep
                + " AND Length = " + this.length + ";";
        try {
            try (Statement stmt = this.conn.createStatement(); ResultSet rs = stmt.executeQuery(statement)) {
                //sets CONFIG_ID to the stored CONFIG_ID if found, or the next number in the index
                if (rs.next()) {
                    this.CONFIG_ID = rs.getInt("CONFIG_ID");
                    //sets previousSim to true so that the storage will be skipped
                    this.previousSimDetected = true;
                    System.out.println("CONFIG EXISTS: CONFIG_ID=" + this.CONFIG_ID);
                } else {
                    //checks if the config exists if not adds to the table
                    this.CONFIG_ID = getCountTable(SIM_CONFIG_TBL);
                    //sets previousSim to false so that the data will be stored
                    this.previousSimDetected = false;
                    insertConfigData(name, storagePrecision, temporalPrecision, geographicalPrecision, startDate, orbit, tilt, gridSpacing, timeStep, length, "'" + getTimeStamp() + "'");

                }
            }
            this.conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    //returns the number for the next index

    public int getCountTable(String table)
    {
        String statement = "SELECT Count(*) AS rowcount FROM " + table;
        int next_number = -1;
        try {
            Statement stmt = this.conn.createStatement();
            ResultSet rs = stmt.executeQuery(statement);
            next_number = rs.getInt("rowcount");
            System.out.println("NEXT INDEX FOR " + table + " is " + next_number);
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return next_number;
    }

    public void updateConfig(String Name, int StoragePrecision, int TemporalPrecision, int GeographicalPrecision, String StartDate, double Orbit, double Tilt, int GridSpacing, int TimeStep, int Length)
    {

        this.name = (Name == "") ? "Sim" + this.getTimeStamp() : Name;
        this.storagePrecision = StoragePrecision;
        this.temporalPrecision = TemporalPrecision;
        this.geographicalPrecision = GeographicalPrecision;
        this.startDate = StartDate;
        this.orbit = Orbit;
        this.tilt = Tilt;
        this.gridSpacing = GridSpacing;
        this.timeStep = TimeStep;
        this.length = Length;
        getConfig_ID();

    }

    //used for testing
    private boolean isDebug()
    {
        return this.debug;
    }

    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }

    //to be run after every transaction
    private void cleanUp() throws SQLException
    {
        if (transactionCounter++ > MAX_NUMBER_TRANSACTION_COUNTER) {
            transactionCounter = 0;
        }
        if (transactionCounter % MAX_NUM_TRANS_TILL_COMMIT == 0) {
            this.conn.commit();
        }
        if (transactionCounter % ITER_FOR_GARBAGE_COLLECTION == 0) {
            System.gc();
        }

    }

    public void closeDBSession()
    {
        try {
            this.conn.commit();
            this.conn.close();
            System.gc();
            transactionCounter = 0;
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }

    }

    public int getCONFIG_ID()
    {
        return CONFIG_ID;
    }

    public void storeMap(Map map)
    {
        if (!previousSimDetected) {																//will skip storage if previous simulation detected
            if ((temporalPrecision - tempPrecCtr++) > 0 || temporalPrecision == 100) {					//will count down the number of allowable temporal saves
                if ((geographicalPrecision - geoPrecCtr++) > 0 || (geographicalPrecision == 100)) {	//will count down the number of allowable geographical saves
                    try {
                        insertGridData((double) map.get("Lat"), (double) map.get("Lon"), (double) map.get("Temp"), (int) map.get("Iter"), Integer.toString((int) map.get("Day")), Integer.toString((int) map.get("Min")), 1);
                    } catch (SQLException e) {

                        e.printStackTrace();
                        System.exit(0);
                    }
                } else {
                    geoPrecCtr = 0;
                }

            } else {
                tempPrecCtr = 0;
            }
        }
    }

    public void manualCommit() throws SQLException
    {
        this.conn.commit();
    }

}