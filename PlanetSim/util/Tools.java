package PlanetSim.util;

/**
 * Created by amounib on 11/20/2014.
 */
import PlanetSim.Model;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Tools {
    private static String start_date_string = "04-Jan-2014";
    private static SimpleDateFormat start_date_formatter = new SimpleDateFormat("dd-MMM-yyyy");
    private static SimpleDateFormat start_minute_formatter = new SimpleDateFormat("HH:mm:ss a");
    private Date startDate = getStartDate();
    private long MILLSEC_PER_MINUTE = 60 * 1000;

    public static ArrayList<Map> validateInput(String args[])
    {
        ArrayList<Map> config = new ArrayList<Map>();
        if (args.length % 2 != 0) { errorMsg(); }                // checks if there are an even number of arguments
        for (int i = 0; i<args.length-1; i+=2){
            if (!inList(args[i])){ errorMsg(); }                 // checks if the it is in the approved character list
            Map<String, Number> map = new HashMap<String, Number>();
            if (args[i].equals( "-p" ) && isNumeric(args[i+1])) { map.put("p",parseInt(args[i+1])); config.add(map); }
            if (args[i].equals( "-g" ) && isNumeric(args[i+1])) { map.put("g",parseInt(args[i+1])); config.add(map); }
            if (args[i].equals( "-t" ) && isNumeric(args[i+1])) { map.put("t",parseInt(args[i+1])); config.add(map); }
        }

        return config;
    }

    private static int parseInt(String input)
    {
        int number = -1;

        try
        {
            number = Integer.parseInt(input);
            if (number < 0) { errorMsg(); }
        }
        catch (NumberFormatException nfe)
        {
            System.err.println("ERROR: Could not parse " + input);
        }

        return number;
    }

    private static void errorMsg(){
        System.out.println("Error in input:");
        System.out.println("\t\tjava PlanetSim.Demo [-p #] [-g #] [-t #]");
        System.exit(0);
    }

    public long convertIterationToLong(long target, long step){
        return  startDate.getTime() + (target* step * this.MILLSEC_PER_MINUTE);
    }
    public String getDate(long target){
        //pass in the start date, number of iterations and the steps
        //returns string value in dd-MMM-yyyy format
        return this.start_date_formatter.format(new Date(target));
    }
    public String getTime(long target){
        //pass in the
        //returns string value in HH:mm:ss a

        return this.start_minute_formatter.format(new Date(target));
    }

    public static Date getStartDate(){
        try {
            return start_date_formatter.parse(start_date_string);
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }
    public static boolean isNumeric(String str){					//	this method checks if a string is an Integer
        try
        {
            int n = Integer.parseInt(str);
        }
        catch(NumberFormatException nfe)
        {
            errorMsg();

        }
        return true;
    }

    public static boolean inList(String x){

        String validArgs[] = { "-p" , "-g" , "-t"};
        for(String str: validArgs) {
            if(str.trim().contains(x))
                return true;
        }return false;
    }

    public static long getTotalLength(int length){
        final Calendar date = Calendar.getInstance();
        date.setTime(getStartDate());
        date.add(Calendar.MONTH,length);
        return date.getTime().getTime();
    }

    //   final Calendar date = Calendar.getInstance();
    //   date.set(2012, Calendar.SEPTEMBER, 17);

//    int prevDayOfWeekInMonth = date.get(Calendar.DAY_OF_WEEK_IN_MONTH);
//    int prevDayOfWeek = date.get(Calendar.DAY_OF_WEEK);

//    date.add(Calendar.MONTH, 1);
}
