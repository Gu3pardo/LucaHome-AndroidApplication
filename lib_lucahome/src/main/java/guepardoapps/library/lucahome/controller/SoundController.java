package guepardoapps.library.lucahome.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import guepardoapps.library.lucahome.common.classes.Sound;
import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.ServerActions;
import guepardoapps.library.lucahome.common.constants.SharedPrefConstants;
import guepardoapps.library.lucahome.common.enums.LucaObject;
import guepardoapps.library.lucahome.common.enums.RaspberrySelection;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.controller.SharedPrefController;

public class SoundController {

    private static final String TAG = SoundController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private Context _context;
    private ServiceController _serviceController;
    private SharedPrefController _sharedPrefController;

    public SoundController(Context context) {
        _logger = new LucaHomeLogger(TAG);
        _context = context;
        _serviceController = new ServiceController(_context);
        _sharedPrefController = new SharedPrefController(_context, SharedPrefConstants.SHARED_PREF_NAME);
    }

    public void CheckPlaying(RaspberrySelection raspberrySelection) {
        _logger.Debug("CheckPlaying: " + raspberrySelection.toString());
        _serviceController.StartRestService(TAG, ServerActions.IS_SOUND_PLAYING, Broadcasts.IS_SOUND_PLAYING,
                LucaObject.SOUND, raspberrySelection);
    }

    public void StartSound(String soundFile, RaspberrySelection raspberrySelection) {
        _logger.Debug("StartSound: " + soundFile + " at " + raspberrySelection.toString());
        sendBroadCast(Broadcasts.ACTIVATE_SOUND_SOCKET, raspberrySelection);
        _serviceController.StartRestService(TAG, ServerActions.PLAY_SOUND + soundFile, Broadcasts.START_SOUND,
                LucaObject.SOUND, raspberrySelection);
    }

    public void StopSound(RaspberrySelection raspberrySelection) {
        _logger.Debug("StopSound: " + raspberrySelection.toString());
        sendBroadCast(Broadcasts.DEACTIVATE_SOUND_SOCKET, raspberrySelection);
        _serviceController.StartRestService(TAG, ServerActions.STOP_SOUND, Broadcasts.STOP_SOUND, LucaObject.SOUND,
                raspberrySelection);
    }

    public void IncreaseVolume(RaspberrySelection raspberrySelection) {
        _logger.Debug("IncreaseVolume: " + raspberrySelection.toString());
        _serviceController.StartRestService(TAG, ServerActions.INCREASE_VOLUME, Broadcasts.GET_VOLUME, LucaObject.SOUND,
                raspberrySelection);
    }

    public void DecreaseVolume(RaspberrySelection raspberrySelection) {
        _logger.Debug("DecreaseVolume: " + raspberrySelection.toString());
        _serviceController.StartRestService(TAG, ServerActions.DECREASE_VOLUME, Broadcasts.GET_VOLUME, LucaObject.SOUND,
                raspberrySelection);
    }

    public void SelectRaspberry(RaspberrySelection previousSelection, RaspberrySelection newSelection, Sound sound) {
        _logger.Debug("SelectRaspberry: previousSelection: " + previousSelection.toString() + " to newSelection: "
                + newSelection.toString());

        if (previousSelection == newSelection) {
            _logger.Warn("RaspberrySelection has to be different!");
            return;
        }

        if (newSelection == RaspberrySelection.BOTH || newSelection == RaspberrySelection.DUMMY) {
            _logger.Warn("RaspberrySelection not possible for new selection!");
            return;
        }

        StopSound(previousSelection);

        if (sound != null) {
            StartSound(sound.GetFileName(), newSelection);
        }

        sendBroadCast(Broadcasts.SET_RASPBERRY, newSelection);
        _sharedPrefController.SaveIntegerValue(SharedPrefConstants.SOUND_RASPBERRY_SELECTION, newSelection.GetInt());
    }

    private void sendBroadCast(String broadcast, RaspberrySelection selection) {
        Intent broadcastIntent = new Intent(broadcast);
        Bundle broadcastData = new Bundle();
        broadcastData.putSerializable(Bundles.RASPBERRY_SELECTION, selection);
        broadcastIntent.putExtras(broadcastData);
        _context.sendBroadcast(broadcastIntent);
    }
}
