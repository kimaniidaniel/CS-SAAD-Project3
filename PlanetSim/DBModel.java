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

import PlanetSim.util.Tools;
import Utils.QueryResult;
import Utils.TemperatureReading;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            + "Reading_Date NUMERIC,"
            + "Reading_Time NUMERIC,"
            + "TransActionTime TEXT,"
            + "FOREIGN KEY(CONFIG_ID) REFERENCES " + SIM_CONFIG_TBL + "(CONFIG_ID))";

    private boolean debug = false;							//used for debugging
    private long transactionCounter = 0;					//used to adjust performance counter for comparison
    private long MAX_NUMBER_TRANSACTION_COUNTER = 50000;	//used to adjust performance max size of the counter
    private long MAX_NUM_TRANS_TILL_COMMIT = 10000;	//used to adjust performance affects CPU
    private int ITER_FOR_GARBAGE_COLLECTION = 100000;	//used to adjust performance affects CPU
    // current simulation settings
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
    private int geoInterval;
    private long tempInterval;
    private int geoPrecCtr = 0;							//used to track precision
    private int tempPrecCtr = 0;						//used to track precision
    private long currentStep;

    private Connection conn;

    public DBModel()
    {
        try {
            this.conn = Connect(DATABASENAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DBModel(String Name, int TemporalPrecision, int GeographicalPrecision, String StartDate, double Orbit, double Tilt, int GridSpacing, int TimeStep, int Length)
    {
        //System.out.println("OPENNING DBMODEL THROUGH SIMCONFIGURATION");
        try {
           this.temporalPrecision = TemporalPrecision;
            this.geographicalPrecision = GeographicalPrecision;
           this.startDate = StartDate;
            this.orbit = Orbit;
            this.tilt=Tilt;
            this.gridSpacing=GridSpacing;
            this.timeStep=TimeStep;
            this.length=Length;
            this.name = Name;
            this.conn = Connect(DATABASENAME);
            this.name = ("".equals(Name)) ? "Sim_" + this.getTimeStamp() : Name;
            this.tempInterval = (Tools.getTotalLength(this.length)/this.timeStep);
            this.geoInterval = ((360 * 180)/this.gridSpacing);
            updateConfig(this.name, TemporalPrecision, GeographicalPrecision, StartDate, Orbit, Tilt, GridSpacing, TimeStep, Length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //used for debugging

    public DBModel(boolean debug, String Name, int TemporalPrecision, int GeographicalPrecision, String StartDate, double Orbit, double Tilt, int GridSpacing, int TimeStep, int Length)
    {

        this.debug = debug;
        try {

            this.name = ("".equals(Name)) ? "Sim" + this.getTimeStamp() : Name;
            this.temporalPrecision = TemporalPrecision;
            this.geographicalPrecision = GeographicalPrecision;
            this.startDate = StartDate;
            this.orbit = Orbit;
            this.tilt = Tilt;
            this.gridSpacing = GridSpacing;
            this.timeStep = TimeStep;
            this.length = Length;
            this.conn = Connect(DATABASENAME);
            this.tempInterval = (Tools.getTotalLength(this.length)/1000/this.timeStep);
            this.geoInterval = ((360 * 180)/this.gridSpacing);

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

    public void insertConfigData(String Name, int TemporalPrecision, int GeographicalPrecision, String StartDate, double Orbit, double Tilt, int GridSpacing, int TimeStep, int Length, String EntryTime) throws SQLException
    {

        String statement = "INSERT INTO " + SIM_CONFIG_TBL + " (Name,TemporalPrecision,GeographicalPrecision,StartDate,Orbit,Tilt,GridSpacing,TimeStep,Length,EntryTime) "
                + "VALUES ('" + Name + "'," + TemporalPrecision + "," + GeographicalPrecision + ",'" + StartDate + "'," + Orbit + "," + Tilt + "," + GridSpacing + "," + TimeStep + "," + Length + "," + EntryTime + ");";
        try {
            Statement stmt = this.conn.createStatement();
            stmt.executeUpdate(statement);
            this.conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        cleanUp();

    }

    public void insertGridData(double Latitude, double Longitude, double Temperature, int Step, long Reading_Date, long Reading_Time, int CONFIG_ID) throws SQLException
    {

        String statement = "INSERT INTO " + PLANET_CELLS_TBL + " (CONFIG_ID,Latitude,Longitude,Temperature,Step,Reading_Date,Reading_Time,TransActionTime) "
                + "VALUES (" + this.CONFIG_ID + "," + Latitude + "," + Longitude + "," + Temperature + "," + Step + ",'" + Reading_Date + "','" + Reading_Time + "','" + getTimeStamp() + "');";
        try {
            Statement stmt = this.conn.createStatement();
            stmt.executeUpdate(statement);
        } catch (Exception e) {
            e.printStackTrace();
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
            try {
                Statement stmt = c.createStatement();
                if (this.isDebug()) {
                    //System.out.println("Creating:" + SIM_CONFIG_TBL);
                }
                stmt.execute(CREATE_SIM_CONFIG_TBL);								// creates table SIM_CONFIG_TBL if not present
                if (this.isDebug()) {
                    //System.out.println("Creating:" + PLANET_CELLS_TBL);
                }
                stmt.execute(CREATE_PLANET_CELLS_TBL);							// creates table PLANET_CELLS_TBL if not present
                c.commit();
            }
            catch (Exception e) {
                throw e;
            }
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        if (this.isDebug()) {
            //System.out.println("Opened database successfully");
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
        String statement = "SELECT CONFIG_ID FROM " + SIM_CONFIG_TBL + " WHERE TemporalPrecision = " + this.temporalPrecision
                + " AND GeographicalPrecision = " + this.temporalPrecision
                + " AND StartDate = '" + this.startDate
                + "' AND Orbit = " + this.orbit
                + " AND Tilt = " + this.tilt
                + " AND GridSpacing = " + this.gridSpacing
                + " AND TimeStep = " + this.timeStep
                + " AND Length = " + this.length + ";";
        try {
            try {
                Statement stmt = this.conn.createStatement();
                ResultSet rs = stmt.executeQuery(statement);
                //sets CONFIG_ID to the stored CONFIG_ID if found, or the next number in the index
                if (rs.next()) {
                    this.CONFIG_ID = rs.getInt("CONFIG_ID");
                    //sets previousSim to true so that the storage will be skipped
                    this.previousSimDetected = true;
                    //System.out.println("CONFIG EXISTS: CONFIG_ID=" + this.CONFIG_ID);
                } else {
                    //checks if the config exists if not adds to the table
                    this.CONFIG_ID = getCountTable(SIM_CONFIG_TBL);
                    //sets previousSim to false so that the data will be stored
                    this.previousSimDetected = false;
                    insertConfigData(name, temporalPrecision, geographicalPrecision, startDate, orbit, tilt, gridSpacing, timeStep, length, "'" + getTimeStamp() + "'");

                }
            } catch (Exception e) {
                e.printStackTrace();
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
            next_number = rs.getInt("rowcount") + 1;
            //System.out.println("NEXT INDEX FOR " + table + " is " + next_number);
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return next_number;
    }

    public void updateConfig(String Name, int TemporalPrecision, int GeographicalPrecision, String StartDate, double Orbit, double Tilt, int GridSpacing, int TimeStep, int Length)
    {
    	
        this.name = (Name == "") ? "Sim" + this.getTimeStamp() : Name;
        this.temporalPrecision = TemporalPrecision;
        this.geographicalPrecision = GeographicalPrecision;
        this.startDate = StartDate;
        this.orbit = Orbit;
        this.tilt = Tilt;
        this.gridSpacing = GridSpacing;
        this.timeStep = TimeStep;
        this.length = Length;
        this.geoInterval = ((360 * 180)/this.gridSpacing);
        this.tempInterval = Tools.getTotalLength(this.length)/100;
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
            if ( validTemporal((Integer) map.get("Iter"))  || temporalPrecision == 100) {			//will count down the number of allowable temporal saves
             //   System.out.println("T:VALID");

                if ((this.geoPrecCtr++ < ( this.geoInterval / 100 * (  ( this.geographicalPrecision * 1.0 ) ))) || (geographicalPrecision == 100)) {	    //will count down the number of allowable geographical saves

                    try {
                       //System.out.println(map);
                        insertGridData((Double) map.get("Lat"), (Double) map.get("Lon"), (Double) map.get("Temp"), (Integer) map.get("Iter"), (Long) map.get("Day"), (Long)map.get("Min"), this.CONFIG_ID);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        System.exit(0);
                    }
                } else if (this.geoPrecCtr == this.geoInterval) {  geoPrecCtr = 0; }

            }
        }
    }

    private boolean validTemporal(long x){
       //System.out.println(x+":"+this.currentStep+":"+tempPrecCtr+":"+this.tempInterval+":"+this.length*this.timeStep+":"+(int)(this.tempInterval/ 100*(  this.temporalPrecision  )));
        if ( this.currentStep == x ) { if ( tempPrecCtr < ( this.tempInterval/ 100 * (  this.temporalPrecision  ) ) ) { return true; } else {  return false; }  }
        else {
            this.currentStep = x;
            if (tempPrecCtr < (this.tempInterval / 100 * (this.temporalPrecision))) {
                tempPrecCtr++;
                if (tempPrecCtr < this.tempInterval) {
                    return true;
                } else {
                    return false;
                }
            } else if (tempPrecCtr == this.tempInterval) {
                tempPrecCtr = 0;
            }
        }
        return false;
    }

    public void manualCommit() throws SQLException
    {
        this.conn.commit();
    }

    /**
     * Query the database for simulated stored data
     */
    public QueryResult Query(int configId, String Name, int StoragePrecision, int TemporalPrecision, int GeographicalPrecision, String StartDate, double Orbit, double Tilt, int GridSpacing)
    {
        String sqlCommand = String.format("SELECT * FROM '%s' WHERE 1=1 ", SIM_CONFIG_TBL);
        ResultSet rs = null;
        // int configId = 0;
        Utils.QueryResult result = new Utils.QueryResult();
        List<Utils.SimulationConfig> SimulationConfigs = new ArrayList<Utils.SimulationConfig>();
        List<Utils.TemperatureReading> TemperaturReadings = new ArrayList<Utils.TemperatureReading>();
                                                 
        /* Build query SQL command */
        sqlCommand += (configId>0)     				?String.format(" AND CONFIG_ID = %d", configId):"";
        sqlCommand += !(isEmptyOrNull(Name))     ?String.format(" AND Name ='%s'", Name):"";
        sqlCommand += ((StoragePrecision)>0)    ?String.format(" AND StoragePrecision = %d", StoragePrecision):"";
        sqlCommand += ((TemporalPrecision)>0)   ?String.format(" AND TemporalPrecision = %d", TemporalPrecision):"";
        sqlCommand += ((GeographicalPrecision)>0)?String.format(" AND GeographicalPrecision = %d", GeographicalPrecision):"";
        sqlCommand += !(isEmptyOrNull(StartDate))?String.format(" AND StartDate <= '%s'", StartDate):"";
        sqlCommand += ((Orbit)>0)               ?String.format(" AND Orbit = %f", Orbit):"";
        sqlCommand += ((Tilt)>0)                ?String.format(" AND Tilt = %f", Tilt):"";
        sqlCommand += ((GridSpacing)>0)         ?String.format(" AND GridSpacing = %d", GridSpacing):"";
        sqlCommand += " ORDER BY CONFIG_ID DESC";
        
        try {
            Statement stmt = this.conn.createStatement();                                                           /* Execute SQL command */
            rs = stmt.executeQuery(sqlCommand);
            TemperatureReading minTemp = new TemperatureReading();
            TemperatureReading maxTemp = new TemperatureReading();
            double meanTemp = 0;
            TemperatureReading minTempTotal = new TemperatureReading();
            TemperatureReading maxTempTotal = new TemperatureReading();
            double meanTempTotal = 0;

            while ( rs.next() ) {                                                                                   /* Iterate through the resulting recordset and build the SimulationConfig object */
                Utils.SimulationConfig simConfig = new Utils.SimulationConfig();
                simConfig.setConfigId(rs.getInt("CONFIG_ID"));
                simConfig.Name             		= rs.getString("Name");
                simConfig.EntryTime             = null;
                simConfig.GeographicPrecision   = rs.getInt("GeographicalPrecision");
                simConfig.StoragePrecision      = rs.getInt("StoragePrecision");
                simConfig.TemporalPrecision     = rs.getInt("TemporalPrecision");
                simConfig.Orbit                 = rs.getDouble("Orbit");
                simConfig.Tilt                  = rs.getDouble("Tilt");
                simConfig.StartDate             = rs.getString("StartDate");
                simConfig.GridSpacing           = rs.getInt("GridSpacing");
                simConfig.TimeStep              = rs.getInt("TimeStep");
                simConfig.Length                = rs.getInt("Length");
                if(configId>0) {
                	simConfig.TemperatureReadings   = QueryGetSimCells(configId,minTemp,maxTemp, meanTemp);                                         /* Get all cells for this config and add it to the collection */
                }
                simConfig.MinTemp               = minTemp;
                simConfig.MaxTemp               = maxTemp;
                minTempTotal                    = (minTempTotal.Temperatue < minTemp.Temperatue)?minTempTotal:minTemp;      /* get minimum temperature over times */
                maxTempTotal                    = (maxTempTotal.Temperatue > maxTemp.Temperatue)?maxTempTotal:maxTemp;      /* get maximum temperature over times */
                simConfig.MeanTempOverRegion    = meanTemp;
                meanTempTotal                   += meanTemp;                                                                /* aggregate mean temp over times */
                SimulationConfigs.add(simConfig);
            }
            meanTempTotal                       /= (meanTempTotal > 0)?SimulationConfigs.size():1;                          /* sum(meanTemps) / Count(time steps) */
            result.MeanTemperatureOverTimes     = meanTempTotal;
            result.MaxTemperatureReading        = maxTempTotal;
            result.MinTemperatureReading        = minTempTotal;
        } catch (SQLException ex) {
            Logger.getLogger(DBModel.class.getName()).log(Level.SEVERE, null, ex);                                    /* Log errors */
        } finally {
            try {
                rs.close();
            } catch (SQLException ex) {
                Logger.getLogger(DBModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        result.Simulation = SimulationConfigs;

        return result;
    }

    /**
     * Queries Cell information based on configId and returns a list of TemperatureReading
     */
    private List<TemperatureReading> QueryGetSimCells(int configId, TemperatureReading minTemp, TemperatureReading maxTemp, double meanTemp ) {

    	String sqlCommand;
        ResultSet rs = null;
        List<TemperatureReading> TemperaturReadings = new ArrayList<TemperatureReading>();
        sqlCommand = String.format("SELECT * FROM '%s' WHERE CONFIG_ID = %d ", PLANET_CELLS_TBL, configId);         /* query cell details */
        try {
            Statement stmt = this.conn.createStatement();
            rs = stmt.executeQuery(sqlCommand);
            meanTemp = 0d;
            double c = 0;
            while ( rs.next() ) {                                                                                   /* fill TemperatureReading object */
                Utils.TemperatureReading reading = new Utils.TemperatureReading();
                reading.ConfigId        = rs.getInt("CONFIG_ID");
                reading.Latitude        = rs.getDouble("Latitude");
                reading.Longitude       = rs.getDouble("Longitude");
                reading.Step            = rs.getInt("Step");
                reading.Temperatue      = rs.getDouble("Temperature");
                reading.ReadingDate     = rs.getLong("Reading_Date");
                reading.ReadingTime     = rs.getLong("Reading_Time");
                reading.TransActionTime = rs.getNString("TransActionTime");
                TemperaturReadings.add(reading);
                meanTemp                += reading.Temperatue; c++;
                minTemp                 = (reading.Temperatue < minTemp.Temperatue)? reading : minTemp;
                maxTemp                 = (reading.Temperatue > maxTemp.Temperatue)? reading : maxTemp;
            }
            meanTemp = meanTemp / c;
        } catch (SQLException ex) {
            Logger.getLogger(DBModel.class.getName()).log(Level.SEVERE, null, ex);                                  /* log any error */
        } finally {
            try {
                rs.close();
            } catch (SQLException ex) {
                Logger.getLogger(DBModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return TemperaturReadings;
    }

    /**
     * Return true if string is empty or null
     */
    private boolean isEmptyOrNull(String s){
        return "".equals(s) || s==null;
    }

    /**
     * Return if String is empty
     */
    private boolean isEmpty(String s){
        return "".equals(s);
    }

}
