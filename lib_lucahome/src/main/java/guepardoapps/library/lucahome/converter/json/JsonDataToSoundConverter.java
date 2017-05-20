package guepardoapps.library.lucahome.converter.json;

import java.util.ArrayList;
import java.util.Locale;

import guepardoapps.library.lucahome.common.classes.Sound;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.StringHelper;

public final class JsonDataToSoundConverter {

    private static final String TAG = JsonDataToSoundConverter.class.getSimpleName();
    private static LucaHomeLogger _logger;

    private static String _searchParameter = "{soundfile:";

    public static ArrayList<Sound> GetList(String value) {
        if (StringHelper.GetStringCount(value, _searchParameter) > 1) {
            if (value.contains(_searchParameter)) {
                ArrayList<Sound> list = new ArrayList<>();

                String[] entries = value.split("\\" + _searchParameter);
                for (String entry : entries) {
                    Sound newValue = Get(entry);
                    if (newValue != null) {
                        list.add(newValue);
                    }
                }
                return list;
            }
        }

        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }
        _logger.Error("GetList " + value + " has an error!");

        return new ArrayList<>();
    }

    public static Sound Get(String value) {
        value = value.replace(_searchParameter, "").replace("};};", "").replace("};", "");

        if (value.endsWith(".mp3") && !value.contains("{") && !value.contains("}")) {

            Sound newValue = new Sound(value, false);
            _logger.Debug(String.format(Locale.GERMAN, "New Sound %s", newValue));

            return newValue;
        }

        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }
        _logger.Error("Get: " + value + " has an error!");

        return null;
    }
}