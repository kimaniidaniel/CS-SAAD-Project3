/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author kimanii
 */
public class QueryResult implements Serializable{
    public List<SimulationConfig> Simulation;
    public TemperatureReading MinTemperatureReading;
    public TemperatureReading MaxTemperatureReading;
    public double MeanTemperatureOverTimes;
}
