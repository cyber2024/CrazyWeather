package com.ggg.crazyweather;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by Russell Elfenbein on 10/13/2015.
 */
public class ForecastAdapter extends CursorAdapter{
    private Context mContext;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    private String formatTempRange(double high, double low){

        String highLow = Utility.formatTemperature(high, Utility.whatUnits(mContext)) + "/" +
                Utility.formatTemperature(low, Utility.whatUnits(mContext));
        return highLow;
    }

    private String convertCursoRowToUXFormat(Cursor cursor){

        String highAndLow = formatTempRange(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP)
        );

        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.forecast_list_item_layout, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv = (TextView) view.findViewById(R.id.sdfDay);
        tv.setText(convertCursoRowToUXFormat(cursor));
    }
}
