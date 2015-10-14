package com.ggg.crazyweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Russell Elfenbein on 10/14/2015.
 */
public class Utility {

public enum TemperatureUnits {CELCIUS, FARENHEIT, KELVIN};

    public static String getPreferredLocation(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    public static TemperatureUnits whatUnits(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String units = prefs.getString(
                 context.getString(R.string.pref_temperature_units_key),
                 context.getString(R.string.pref_temperature_units_default));
        if(units.equals("Celcius"))
            return TemperatureUnits.CELCIUS;
        else if (units.equals("Farenheit"))
            return TemperatureUnits.FARENHEIT;
        return TemperatureUnits.KELVIN;
    }

    public static String formatTemperature(double temperature, TemperatureUnits units){
        double temp;

        switch(units){
            case KELVIN:
                temp = temperature +273.15;
                break;
            case FARENHEIT:
                temp = 9/5 * temperature +32;
                break;
            case CELCIUS: default:
                temp = temperature;
        }
        return String.format("%.0f", temp);
    }

    public static String formatDate(long dateInMillis){
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance().format(date);
    }
}
