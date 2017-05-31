package guepardoapps.library.lucahome.customadapter;

import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.openweather.common.enums.ForecastListType;
import guepardoapps.library.openweather.common.model.ForecastWeatherModel;

public class ForecastListAdapter extends BaseAdapter {

    private static final String TAG = ForecastListAdapter.class.getSimpleName();
    private LucaHomeLogger _logger;

    private List<ForecastWeatherModel> _forecastList;
    private static LayoutInflater _inflater = null;

    public ForecastListAdapter(
            @NonNull Context context,
            @NonNull List<ForecastWeatherModel> forecastList) {
        _logger = new LucaHomeLogger(TAG);

        _forecastList = forecastList;
        for (int index = 0; index < _forecastList.size(); index++) {
            _logger.Debug(_forecastList.get(index).toString());
        }

        _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return _forecastList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class Holder {
        private ImageView _image;
        private TextView _description;
        private TextView _temperature;
        private TextView _humidity;
        private TextView _pressure;
        private TextView _time;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        final Holder holder = new Holder();
        ForecastWeatherModel entry = _forecastList.get(index);
        View rowView;
        _logger.Debug(String.format("ForecastWeatherModel is %s!", entry));

        if (entry.GetForecastListType() == ForecastListType.FORECAST) {
            rowView = _inflater.inflate(R.layout.list_forecast_item, null);

            holder._image = (ImageView) rowView.findViewById(R.id.weather_item_image);
            holder._image.setImageResource(entry.GetCondition().GetIcon());

            holder._description = (TextView) rowView.findViewById(R.id.weather_item_description);
            holder._description.setText(entry.GetWeatherDescription());

            holder._temperature = (TextView) rowView.findViewById(R.id.weather_item_temperatures);
            holder._temperature.setText(entry.GetTempMin() + "-" + entry.GetTempMax());

            holder._humidity = (TextView) rowView.findViewById(R.id.weather_item_humidity);
            holder._humidity.setText(String.format(Locale.getDefault(), "Humidity: %s", entry.GetHumidity()));

            holder._pressure = (TextView) rowView.findViewById(R.id.weather_item_pressure);
            holder._pressure.setText(String.format(Locale.getDefault(), "Pressure: %s", entry.GetPressure()));

            holder._time = (TextView) rowView.findViewById(R.id.weather_item_time);
            holder._time.setText(entry.GetTime());
        } else if (entry.GetForecastListType() == ForecastListType.DATE_DIVIDER) {
            rowView = _inflater.inflate(R.layout.list_forecast_divider_item, null);

            holder._time = (TextView) rowView.findViewById(R.id.date_textView);
            holder._time.setText(entry.GetDate());
        } else {
            _logger.Error(String.format("Invalid ForecastListType %s!", entry.GetForecastListType()));
            rowView = _inflater.inflate(R.layout.list_empty_item, null);
        }

        return rowView;
    }
}