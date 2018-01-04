package guepardoapps.lucahome.classes;

import android.support.annotation.NonNull;

import java.util.Locale;

public class MainListViewItem {
    public enum Type {Birthday, Coin, MediaServer, Menu, Meter, MoneyMeter, Movie, PuckJs, Schedule, Security, Settings, Shopping, Timer, Weather, WirelessSocket, WirelessSwitch}

    private static final String TAG = MainListViewItem.class.getSimpleName();

    private String _title;
    private String _description;

    private int _imageResource;

    private Runnable _mainTouchRunnable;
    private Runnable _addTouchRunnable;

    private boolean _addVisibility;

    private Type _type;

    public MainListViewItem(
            @NonNull String title,
            @NonNull String description,
            int imageResource,
            @NonNull Runnable mainTouchRunnable,
            Runnable addTouchRunnable,
            boolean addVisibility,
            @NonNull Type type) {
        _title = title;
        _description = description;
        _imageResource = imageResource;
        _mainTouchRunnable = mainTouchRunnable;
        _addTouchRunnable = addTouchRunnable;
        _addVisibility = addVisibility;
        _type = type;
    }

    public MainListViewItem(
            @NonNull String title,
            @NonNull String description,
            int imageResource,
            @NonNull Runnable mainTouchRunnable,
            Runnable addTouchRunnable,
            @NonNull Type type) {
        this(
                title,
                description,
                imageResource,
                mainTouchRunnable,
                addTouchRunnable,
                (addTouchRunnable != null),
                type);
    }

    public MainListViewItem(
            @NonNull String title,
            @NonNull String description,
            int imageResource,
            @NonNull Runnable mainTouchRunnable,
            @NonNull Type type) {
        this(
                title,
                description,
                imageResource,
                mainTouchRunnable,
                null,
                type);
    }

    public String GetTitle() {
        return _title;
    }

    public String GetDescription() {
        return _description;
    }

    public void SetDescription(@NonNull String description) {
        _description = description;
    }

    public int GetImageResource() {
        return _imageResource;
    }

    public void SetImageResource(int imageResource) {
        _imageResource = imageResource;
    }

    public Runnable GetMainTouchRunnable() {
        return _mainTouchRunnable;
    }

    public Runnable GetAddTouchRunnable() {
        return _addTouchRunnable;
    }

    public boolean IsAddVisibility() {
        return _addVisibility;
    }

    public Type GetType() {
        return _type;
    }

    @Override
    public String toString() {
        return String.format(
                Locale.getDefault(),
                "{%s:{Title: %s}{Description: %s}{ImageResource: %d}{AddVisibility: %s}{Type: %s}}",
                TAG, _title, _description, _imageResource, _addVisibility, _type);
    }
}
