/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import Utils.TemperatureReading;
import java.util.List;

/**
 *
 * @author kimaniidaniel
 */
public class Query
{
    public long StartDate;
    public long EndDate;
    public long StartTime;
    public long EndTime;
    public double LatitudeStart;
    public double LatitudeEnd;
    public long LongitudeStart;
    public long LongitudeEnd;
    public List<TemperatureReading> Result;
}
