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
    public long StartDateTime;
    public long EndDateTime;
    public double LatitudeStart;
    public double LatitudeEnd;
    public double LongitudeStart;
    public double LongitudeEnd;
    public List<TemperatureReading> Result;
}
