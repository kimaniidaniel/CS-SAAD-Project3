package PlanetSim;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by amounib on 11/20/2014.
 */
public class Controller extends ThreadModel {
    private int precision;
    private int geographicPrecision;
    private int temporalPprecision;

    public Controller(ArrayList<Map> args){

        for (Map map : args){
            if (isKey("p",map)){ precision = (int)map.get("p"); }
            if (isKey("g",map)){ geographicPrecision = (int)map.get("g"); }
            if (isKey("t",map)){ temporalPprecision = (int)map.get("t"); }
        }

    }
    private boolean isKey(String targetKey,Map map){
        Object temp = map.get(targetKey);
        return temp != null;
    }
}
