package com.ggg.crazyweather.helpers;

/**
 * Created by Russell Elfenbein on 10/13/2015.
 */

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.ggg.crazyweather.R;
import com.ggg.crazyweather.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/***
 *
 * Fetch Weather Async Task
 */
public class FetchWeatherTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    private Context mContext;
    private ArrayAdapter<String> listAdapter;

    public FetchWeatherTask(Context mContext){
        this.mContext = mContext;
    }

//    public static String getReadableDateString(long time){
//        SimpleDateFormat date = new SimpleDateFormat("EE, MMM dd");
//        return date.format(time);
//    }
//
//    public String formatHighLows(double high, double low){
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
//        String units = prefs.getString(mContext.getString(R.string.pref_temperature_units_key),
//                mContext.getString(R.string.pref_temperature_units_default));
//
//
//
//        if(units.equals(mContext.getString(R.string.pref_temperature_units_farenheit))){
//            high = (high*1.8) + 32;
//            low = (low*1.8) + 32;
//        }
//        if(units.equals(mContext.getString(R.string.pref_temperature_units_kelvin))){
//            high += 273.15;
//            low += 273.15;
//        }
//        //default to celcius
//        return Math.round(high) + "/" + Math.round(low);
//    }

    public long addLocation(String locationSetting, String cityName, double lat, double lon){
        long locationId;
        Cursor locationCursor = mContext.getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? ",
                new String[]{locationSetting},
                null
        );
        if(locationCursor.moveToFirst()){
            int locationIdIndex = locationCursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            locationId = locationCursor.getLong(locationIdIndex);
        } else {
            ContentValues locationValues = new ContentValues();

            locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

            Uri insertedUri = mContext.getContentResolver().insert(
                    WeatherContract.LocationEntry.CONTENT_URI,
                    locationValues
            );
            locationId = ContentUris.parseId(insertedUri);
        }
        locationCursor.close();
        return locationId;
    }

//    private String[] convertContentValuesToUXFormat(Vector<ContentValues> cvv){
//        String[] resultStrs = new String[cvv.size()];
//        for(int i = 0; i < cvv.size(); i++){
//            ContentValues weatherValues = cvv.elementAt(i);
//            String highAndlow = formatHighLows(
//                    weatherValues.getAsDouble(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP),
//                    weatherValues.getAsDouble(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));
//            resultStrs[i] = getReadableDateString(
//                    weatherValues.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE)) + " - "+
//                    weatherValues.getAsString(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC)+ " - " +
//                    highAndlow;
//        }
//        return resultStrs;
//    }


    private void getWeatherDataFromJson(String forecastJsonStr, String locationSetting) throws JSONException {

        final String
                OWM_CITY = "city",
                OWM_CITY_NAME = "name",
                OWM_COORD = "coord",
                OWM_LATITUDE = "lat",
                OWM_LONGITUDE = "lon",

                OWM_LIST = "list",
                OWM_PRESSURE = "pressure",
                OWM_HUMIDITY = "humidity",
                OWM_WINDSPEED = "speed",
                OWM_WIND_DIRECTION = "deg",

                OWM_TEMPERATURE = "temp",
                OWM_MAX = "max",
                OWM_MIN = "min",

                OWM_WEATHER = "weather",
                OWM_DESCRIPTION = "main",
                OWM_WEATHER_ID = "id";


        try {
            //Snag the JSON Array from the JSON string
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArrayJson = forecastJson.getJSONArray(OWM_LIST);

            JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
            String cityName = cityJson.getString(OWM_CITY_NAME);

            JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
            double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
            double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

            //this will check to see if location is already listed in location table
            //if not, it will be added prior to inserting other data into the Weather table
            long locationId = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArrayJson.length());


            //Get the current date and time
//            Calendar calendar = new GregorianCalendar();
//            calendar.setTime(new Date());
//            calendar.setTimeZone(TimeZone.getDefault());
            Time dayTime = new Time();
            dayTime.setToNow();
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
            dayTime = new Time();

            for(int i = 0; i < weatherArrayJson.length(); i++){
                long dateTime;
                int humidity;
                double pressure,
                        windspeed,
                        windDirection,
                        high,
                        low;
                String description;
                int weatherId;

                JSONObject dayForecast = weatherArrayJson.getJSONObject(i);

                dateTime = dayTime.setJulianDay(julianStartDay +i);

                pressure = dayForecast.getDouble(OWM_PRESSURE);
                humidity = dayForecast.getInt(OWM_HUMIDITY);
                windspeed = dayForecast.getDouble(OWM_WINDSPEED);
                windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                weatherId = weatherObject.getInt(OWM_WEATHER_ID);

                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                high = temperatureObject.getDouble(OWM_MAX);
                low = temperatureObject.getDouble(OWM_MIN);

                ContentValues weatherValues = new ContentValues();

                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTime);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windspeed);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

                cVVector.add(weatherValues);

            }
            int inserted = 0;
            Log.d(LOG_TAG, "FetchWeatherTask in progress. "+ cVVector.size()+ " in cvVector");
            if(cVVector.size() > 0){
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
               inserted =  mContext.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cvArray);
            }
            Log.d(LOG_TAG, "FetchWeatherTask in progress. "+ inserted+ " added in Bulk Insert");
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(String... params) {

        if(params.length == 0){
            Log.w(LOG_TAG, "Attempted FetWeatherAsyncTask without providing params");
            return null;
        }

        String locationQuery = params[0];

        //shall be closed on the finally part of catch block
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String forecastJsonStr = null;

        String format = "json";
        String units = "metric";
        int numDays = 14;

        try {

            final String
                    FORECAST_BASE_URL = "http://openweathermap.org/data/2.5/forecast/daily?",
                    QUERY_PARAM = "q",
                    FORMAT_PARAM = "mode",
                    UNITS_PARAM = "units",
                    API_KEY = "appid",
                    DAYS_PARAM = "cnt";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, params[0])
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .appendQueryParameter(API_KEY, mContext.getString(R.string.owm_api_key))
                    .build();

            Log.d(LOG_TAG,"Built Uri - "+builtUri.toString());

            //connect to server
            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //read input
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if(inputStream == null){
                //nothing to do, bail
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while((line = reader.readLine()) != null){
                buffer.append(line + "\n");
            }
            if(buffer.length() == 0){
                //nothing to do, bail
                return null;
            }
            forecastJsonStr = buffer.toString();
            getWeatherDataFromJson(forecastJsonStr, locationQuery);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if(urlConnection != null)
                urlConnection.disconnect();
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error closing reader stream");
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}