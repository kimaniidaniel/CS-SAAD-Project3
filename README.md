CS-SAAD-Project3
================

Description: 
------------
This project simulates the diffusion of heat over a planet.

Any of the implementations can be run from command line using the instructions below.
Each of the implementations output the resulting temperatures across the planet after diffusion of heat takes place and temperatures stabilize.


Compilation Instructions:
-------------------------
1. Browse to this directory (CS-SAAD-Project3/)
2. Issue the following command for the program:

	javac PlanetSim/Demo.java

Command-line invocation:
---------------------
The Heated Planet simulation program should be invoked as follows:

Windows: java -classpath ".;libs/sqlite-jdbc-3.8.7.jar" PlanetSim.Demo [-p #] [-g #] [-t #]

Mac or Linux: java -classpath ".:libs/sqlite-jdbc-3.8.7.jar" PlanetSim.Demo [-p #] [-g #] [-t #]

PlanetSim.Demo should be invoked with the following Invocation Parameters to allow the designer to control the persisted results.

* -p #: The precision of the data to be stored, in decimal digits after the decimal point. The default is to use the number of digits storable in a normalized float variable. The maximum is the number of digits storable in a normalized double variable. The minimum is zero.
* -g #: The geographic precision (sampling rate) of the temperature data to be stored, as an integer percentage of the number of grid cells saved versus the number simulated. The default is 100%; that is, a value is stored for each grid cell.        -t #: The temporal precision of the temperature data to be stored, as an integer percentage of the number of time periods saved versus the number computed. The default is 100%; that is, all computed values should be stored.
* -t #: The temporal precision of the temperature data to be stored, as an integer percentage of the number of time periods saved versus the number computed. The default is 100%; that is, all computed values should be stored.
