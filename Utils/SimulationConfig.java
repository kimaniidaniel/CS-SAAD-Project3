/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author kimanii
 */
public class SimulationConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private int ConfigId;
    public String Name;
    public int StoragePrecision;
    public int GeographicPrecision;
    public int TemporalPrecision;
    public String StartDate;
    public double Orbit;
    public double Tilt;
    public int GridSpacing;
    public int TimeStep;
    public int Length;
    public Date EntryTime;
    public List<TemperatureReading> TemperatureReadings;
    public TemperatureReading MinTemp;
    public TemperatureReading MaxTemp;
    public double MeanTempRegion;
    public double MeanTempTime;

    public int getConfigId() {
        return ConfigId;
    }

    public void setConfigId(int SimulationSettingId) {
        this.ConfigId = SimulationSettingId;
    }

    @Override
    public String toString() {
        return "Utils.SimulationConfig[ id=" + ConfigId + " ]";
    }

}
