package guepardoapps.mediamirror.helper;

import android.support.annotation.NonNull;

import java.io.Serializable;

import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.mediamirror.R;

public class RaspberryTemperatureHelper implements Serializable {
    private static final long serialVersionUID = -8359166934848014777L;

    private static final String TAG = RaspberryTemperatureHelper.class.getSimpleName();

    public RaspberryTemperatureHelper() {
    }

    public int GetIcon(@NonNull String temperature) {
        String pureDouble = temperature.replace("°C", "");

        double parsedDouble;
        try {
            parsedDouble = Double.parseDouble(pureDouble);
        } catch (Exception e) {
            Logger.getInstance().Warning(TAG, "Parsing ot string to double failed! Setting to -1");
            parsedDouble = -1;
        }

        int drawable = 0;
        if (parsedDouble < 12) {
            drawable = R.xml.circle_blue;
        } else if (parsedDouble >= 12 && parsedDouble < 15) {
            drawable = R.xml.circle_red;
        } else if (parsedDouble >= 15 && parsedDouble < 18) {
            drawable = R.xml.circle_yellow;
        } else if (parsedDouble >= 18 && parsedDouble <= 25) {
            drawable = R.xml.circle_green;
        } else if (parsedDouble > 25 && parsedDouble < 30) {
            drawable = R.xml.circle_yellow;
        } else if (parsedDouble >= 30) {
            drawable = R.xml.circle_red;
        }

        return drawable;
    }
}
