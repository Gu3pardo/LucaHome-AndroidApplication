package guepardoapps.lucahome.basic.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.Serializable;

import guepardoapps.lucahome.basic.utils.Logger;

public class BroadcastController {
    private static String TAG = BroadcastController.class.getSimpleName();

    private Context _context;

    public BroadcastController(@NonNull Context context) {
        _context = context;
    }

    public void SendSimpleBroadcast(@NonNull String broadcast) {
        Intent broadcastIntent = new Intent(broadcast);
        _context.sendBroadcast(broadcastIntent);
    }

    public void SendIntBroadcast(
            @NonNull String broadcast,
            @NonNull String bundleName,
            int data) {
        Intent broadcastIntent = new Intent(broadcast);
        Bundle broadcastData = new Bundle();
        broadcastData.putInt(bundleName, data);
        broadcastIntent.putExtras(broadcastData);
        _context.sendBroadcast(broadcastIntent);
    }

    public void SendIntParcelableBroadcast(
            @NonNull String broadcast,
            @NonNull String[] bundleNamesInteger,
            @NonNull int[] dataInteger,
            @NonNull String[] bundleNamesParcelable,
            @NonNull Object[] dataParcelables) {
        Intent broadcastIntent = new Intent(broadcast);
        Bundle broadcastData = new Bundle();
        for (int index = 0; index < bundleNamesInteger.length; index++) {
            broadcastData.putInt(bundleNamesInteger[index], dataInteger[index]);
        }
        for (int index = 0; index < bundleNamesParcelable.length; index++) {
            broadcastData.putParcelable(bundleNamesParcelable[index], (Parcelable) dataParcelables[index]);
        }
        broadcastIntent.putExtras(broadcastData);
        _context.sendBroadcast(broadcastIntent);
    }

    public void SendIntArrayBroadcast(
            @NonNull String broadcast,
            @NonNull String[] bundleNames,
            @NonNull int[] data) {
        Intent broadcastIntent = new Intent(broadcast);
        Bundle broadcastData = new Bundle();
        for (int index = 0; index < bundleNames.length; index++) {
            broadcastData.putInt(bundleNames[index], data[index]);
        }
        broadcastIntent.putExtras(broadcastData);
        _context.sendBroadcast(broadcastIntent);
    }

    public void SendDoubleBroadcast(
            @NonNull String broadcast,
            @NonNull String bundleName,
            double data) {
        Intent broadcastIntent = new Intent(broadcast);
        Bundle broadcastData = new Bundle();
        broadcastData.putDouble(bundleName, data);
        broadcastIntent.putExtras(broadcastData);
        _context.sendBroadcast(broadcastIntent);
    }

    public void SendStringBroadcast(
            @NonNull String broadcast,
            @NonNull String bundleName,
            @NonNull String data) {
        Intent broadcastIntent = new Intent(broadcast);
        Bundle broadcastData = new Bundle();
        broadcastData.putString(bundleName, data);
        broadcastIntent.putExtras(broadcastData);
        _context.sendBroadcast(broadcastIntent);
    }

    public void SendStringArrayBroadcast(
            @NonNull String broadcast,
            @NonNull String[] bundleNames,
            @NonNull String[] data) {
        Intent broadcastIntent = new Intent(broadcast);
        Bundle broadcastData = new Bundle();
        for (int index = 0; index < bundleNames.length; index++) {
            broadcastData.putString(bundleNames[index], data[index]);
        }
        broadcastIntent.putExtras(broadcastData);
        _context.sendBroadcast(broadcastIntent);
    }

    public void SendSerializableBroadcast(
            @NonNull String broadcast,
            @NonNull String bundleName,
            @NonNull Object model) {
        Intent broadcastIntent = new Intent(broadcast);
        Bundle broadcastData = new Bundle();
        broadcastData.putSerializable(bundleName, (Serializable) model);
        broadcastIntent.putExtras(broadcastData);
        _context.sendBroadcast(broadcastIntent);
    }

    public void SendSerializableArrayBroadcast(
            @NonNull String broadcast,
            @NonNull String[] bundleNames,
            @NonNull Object[] models) {
        if (bundleNames.length != models.length) {
            Logger.getInstance().Warning(TAG, "Cannot send broadcast! length are not equal!");
            return;
        }

        Intent broadcastIntent = new Intent(broadcast);
        Bundle broadcastData = new Bundle();
        for (int index = 0; index < bundleNames.length; index++) {
            broadcastData.putSerializable(bundleNames[index], (Serializable) models[index]);
        }
        broadcastIntent.putExtras(broadcastData);
        _context.sendBroadcast(broadcastIntent);
    }
}
