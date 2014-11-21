package PlanetSim;

import PlanetSim.util.Tools;

/**
The Heated Planet simulation program should be invoked as follows:

        java PlanetSim.Demo [-p #] [-g #] [-t #]
        PlanetSim.Demo should be invoked with the following Invocation Parameters to allow the designer to control the persisted results.

        -p #: The precision of the data to be stored, in decimal digits after the decimal point. The default is to use the number of digits storable in a normalized float variable. The maximum is the number of digits storable in a normalized double variable. The minimum is zero.
        -g #: The geographic precision (sampling rate) of the temperature data to be stored, as an integer percentage of the number of grid cells saved versus the number simulated. The default is 100%; that is, a value is stored for each grid cell.
        -t #: The temporal precision of the temperature data to be stored, as an integer percentage of the number of time periods saved versus the number computed. The default is 100%; that is, all computed values should be stored.

 * Created by amounib on 11/20/2014.
 */
public class Demo {

    public static void main(String[] args) {

        Controller controller = new Controller(Tools.validateInput(args));
        new Thread (controller).start();
    }
}
