package guepardoapps.lucahome.voicerecognition.services;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import guepardoapps.library.openweather.converter.NotificationContentConverter;
import guepardoapps.library.openweather.datatransferobjects.NotificationContentDto;
import guepardoapps.library.openweather.models.ForecastModel;
import guepardoapps.library.openweather.models.WeatherModel;
import guepardoapps.library.openweather.service.OpenWeatherService;

import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.utils.Logger;
import guepardoapps.lucahome.voicerecognition.classes.RelationActionClass;
import guepardoapps.lucahome.voicerecognition.utils.IRelationshipHelper;

@SuppressWarnings({"WeakerAccess"})
public class VoiceRecognitionService implements RecognitionListener, ActivityCompat.OnRequestPermissionsResultCallback, IVoiceRecognitionService {
    private static final String Tag = VoiceRecognitionService.class.getSimpleName();

    private static final String KwsSearch = "wakeup";
    private static final String KeyPhrase = "hey luca";

    private static final int PermissionRequestRecordAudio = 345243854;

    private final VoiceRecognitionService Singleton = new VoiceRecognitionService();

    private boolean _isInitialized;
    private boolean _permissionRecordAudioGranted;

    private Context _context;
    private IRelationshipHelper _relationshipHelper;

    private BroadcastController _broadcastController;
    private SpeechRecognizer _speechRecognizer;

    private VoiceRecognitionService() {
    }

    @Override
    public VoiceRecognitionService getInstance() {
        return Singleton;
    }

    @Override
    public InitializeResult Initialize(@NonNull Context context, @NonNull IRelationshipHelper relationshipHelper) {
        if (_isInitialized) {
            Logger.getInstance().Debug(Tag, "Already initialized!");
            return InitializeResult.AlreadyInitialized;
        }

        if (!(context instanceof Activity)) {
            Logger.getInstance().Debug(Tag, "Context is not of type Activity!");
            return InitializeResult.ContextNotOfTypeActivity;
        }

        Logger.getInstance().Debug(Tag, "Initialize");

        _context = context;
        _relationshipHelper = relationshipHelper;
        _broadcastController = new BroadcastController(_context);

        try {
            _permissionRecordAudioGranted = checkRecordAudioPermission();
        } catch (Exception exception) {
            broadcastException(exception);
        }

        if (_permissionRecordAudioGranted) {
            new SetupTask(this).execute();
        }

        _isInitialized = true;
        return InitializeResult.InitializeSuccess;
    }

    @Override
    public void Dispose() {
        Logger.getInstance().Debug(Tag, "Dispose");
        if (_speechRecognizer != null) {
            _speechRecognizer.cancel();
            _speechRecognizer.shutdown();
        }
        _broadcastController = null;
        _isInitialized = false;
    }

    @Override
    public boolean RequestRecordAudioPermission() {
        Logger.getInstance().Verbose(Tag, "RequestRecordAudioPermission");
        if (_permissionRecordAudioGranted) {
            Logger.getInstance().Verbose(Tag, "Already granted!");
            return true;
        }

        try {
            _permissionRecordAudioGranted = checkRecordAudioPermission();
            return _permissionRecordAudioGranted;
        } catch (Exception exception) {
            broadcastException(exception);
            return false;
        }
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {
        if (!_speechRecognizer.getSearchName().equals(KwsSearch)) {
            switchSearch(KwsSearch);
        }
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String result = hypothesis.getHypstr();
            if (result != null) {
                RelationActionClass relationActionClass = _relationshipHelper.GetRelationActionClass(result);
                try {
                    handleSpeechResult(relationActionClass);
                } catch (Exception exception) {
                    broadcastException(exception);
                }
            }
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String result = hypothesis.getHypstr();
            if (result != null) {
                RelationActionClass relationActionClass = _relationshipHelper.GetRelationActionClass(result);
                try {
                    handleSpeechResult(relationActionClass);
                    return;
                } catch (Exception exception) {
                    broadcastException(exception);
                }
            }
        }
        broadcastException(new Exception("onResult received some null value!"));
    }

    @Override
    public void onError(Exception exception) {
        broadcastException(exception);
    }

    @Override
    public void onTimeout() {
        switchSearch(KwsSearch);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionRequestRecordAudio) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                _permissionRecordAudioGranted = true;
                // Recognizer initialization is a time-consuming and it involves IO, so we execute it in async task
                new SetupTask(this).execute();
            } else {
                _permissionRecordAudioGranted = false;
            }
            _broadcastController.SendBooleanBroadcast(BroadcastPermissionRecordAudioResult, BundlePermissionRecordAudioResult, _permissionRecordAudioGranted);
        }
    }

    private void broadcastException(Exception exception) {
        if (exception == null) {
            Logger.getInstance().Error(Tag, "broadcastException exception is null!");
            return;
        }

        Logger.getInstance().Error(Tag, exception.toString());

        if (_isInitialized && _broadcastController != null) {
            _broadcastController.SendStringBroadcast(BroadcastException, BundleException, exception.toString());
        }
    }

    private boolean checkRecordAudioPermission() {
        Logger.getInstance().Debug(Tag, "checkRecordAudioPermission");
        int permissionCheck = ContextCompat.checkSelfPermission(_context.getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) _context, new String[]{Manifest.permission.RECORD_AUDIO}, PermissionRequestRecordAudio);
        }
        if (_isInitialized && _broadcastController != null) {
            _broadcastController.SendBooleanBroadcast(BroadcastPermissionRecordAudioResult, BundlePermissionRecordAudioResult, permissionCheck == PackageManager.PERMISSION_GRANTED);
        }
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches of different kind and switch between them
        _speechRecognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                //.setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .getRecognizer();
        _speechRecognizer.addListener(this);

        /* In your application you might not need to add all those searches.
          They are added here for demonstration. You can leave just one. */

        // Create keyword-activation search.
        _speechRecognizer.addKeyphraseSearch(KwsSearch, KeyPhrase);
    }

    private void switchSearch(String searchValue) {
        if (searchValue == null) {
            return;
        }

        _speechRecognizer.stop();

        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        switch (searchValue) {
            case KwsSearch:
                _speechRecognizer.startListening(searchValue);
                break;
            default:
                _speechRecognizer.startListening(searchValue, 10000);
                break;
        }
    }

    private void handleSpeechResult(RelationActionClass relationActionClass) throws Exception {
        RelationActionClass.RelationAction relationAction = relationActionClass.GetRelationAction();
        ArrayList<String> resultParameter = relationActionClass.GetActionParameter();

        switch (relationAction) {
            case WirelessSocketOn:
                WirelessSocket wirelessSocketOn = _relationshipHelper.GetWirelessSocketByName(resultParameter, WirelessSocketService.getInstance().GetDataList());
                WirelessSocketService.getInstance().SetWirelessSocketState(wirelessSocketOn, true);
                break;
            case WirelessSocketOff:
                WirelessSocket wirelessSocketOff = _relationshipHelper.GetWirelessSocketByName(resultParameter, WirelessSocketService.getInstance().GetDataList());
                WirelessSocketService.getInstance().SetWirelessSocketState(wirelessSocketOff, false);
                break;
            case WirelessSocketToggle:
                WirelessSocket wirelessSocketToggle = _relationshipHelper.GetWirelessSocketByName(resultParameter, WirelessSocketService.getInstance().GetDataList());
                WirelessSocketService.getInstance().SetWirelessSocketState(wirelessSocketToggle, !wirelessSocketToggle.GetState());
                break;

            case WirelessSwitchToggle:
                WirelessSwitch wirelessSwitch = _relationshipHelper.GetWirelessSwitchByName(resultParameter, WirelessSwitchService.getInstance().GetDataList());
                WirelessSwitchService.getInstance().ToggleWirelessSwitch(wirelessSwitch);
                break;

            case PlayYoutube:
                _broadcastController.SendIntBroadcast(BroadcastAudioMedia, BundleAudioMedia, MediaState.PlayYoutube.ordinal());
                break;
            case PlayRadio:
                _broadcastController.SendIntBroadcast(BroadcastAudioMedia, BundleAudioMedia, MediaState.PlayRadio.ordinal());
                break;
            case Pause:
                _broadcastController.SendIntBroadcast(BroadcastAudioMedia, BundleAudioMedia, MediaState.Pause.ordinal());
                break;
            case Stop:
                _broadcastController.SendIntBroadcast(BroadcastAudioMedia, BundleAudioMedia, MediaState.Stop.ordinal());
                break;

            case WeatherCurrent:
                WeatherModel currentWeather = (WeatherModel) OpenWeatherService.getInstance().GetCurrentWeather();
                String ttsSpeakCurrentWeather = createTtsSpeak(currentWeather);
                _broadcastController.SendStringBroadcast(BroadcastTtsSpeak, BundleTtsSpeak, ttsSpeakCurrentWeather);
                break;

            case WeatherForecast:
                ForecastModel forecastWeather = (ForecastModel) OpenWeatherService.getInstance().GetForecastWeather();
                String ttsSpeakForecastWeather = createTtsSpeak(forecastWeather);
                _broadcastController.SendStringBroadcast(BroadcastTtsSpeak, BundleTtsSpeak, ttsSpeakForecastWeather);
                break;

            case GetLight:
                PuckJs puckJs = _relationshipHelper.GetPuckJsByArea(resultParameter, PuckJsService.getInstance().GetDataList());
                String ttsSpeakPuckJs = createTtsSpeak(puckJs);
                _broadcastController.SendStringBroadcast(BroadcastTtsSpeak, BundleTtsSpeak, ttsSpeakPuckJs);
                break;

            case GetTemperature:
                Temperature temperature = _relationshipHelper.GetTemperatureByArea(resultParameter, TemperatureService.getInstance().GetDataList());
                String ttsSpeakTemperature = createTtsSpeak(temperature);
                _broadcastController.SendStringBroadcast(BroadcastTtsSpeak, BundleTtsSpeak, ttsSpeakTemperature);
                break;

            case Unknown:
            default:
                broadcastException(new NullPointerException(String.format(Locale.getDefault(), "Unknown action for result %s", resultParameter)));
                break;
        }
    }

    private String createTtsSpeak(WeatherModel currentWeather) {
        if (currentWeather == null) {
            return "No current weather available";
        }

        return String.format(Locale.getDefault(), "Current weather in %s is %s. It has %.2f degree.", currentWeather.GetCity(), currentWeather.GetWeatherCondition().GetDescription(), currentWeather.GetTemperature());
    }

    private String createTtsSpeak(ForecastModel forecastWeather) {
        if (forecastWeather == null) {
            return "No forecast weather available";
        }

        NotificationContentDto notificationContent = NotificationContentConverter.GetForecastWeather(forecastWeather.GetList());
        return notificationContent.GetTitle() + notificationContent.GetText();
    }

    private String createTtsSpeak(PuckJs puckJs) {
        if (puckJs == null) {
            return "No valid puck found";
        }

        Room room = RoomService.getInstance().GetByUuid(puckJs.GetRoomUuid());
        return String.format(Locale.getDefault(), "There is a relative light value of %d in the %s", puckJs.GetLightValue(), room.GetName());
    }

    private String createTtsSpeak(Temperature temperature) {
        if (temperature == null) {
            return "No valid temperature found";
        }

        Room room = RoomService.getInstance().GetByUuid(temperature.GetRoomUuid());
        return String.format(Locale.getDefault(), "It has %.2f degree in the %s", temperature.GetTemperature(), room.GetName());
    }

    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
        private WeakReference<VoiceRecognitionService> _voiceRecognitionServiceWeakReference;

        SetupTask(@NonNull VoiceRecognitionService voiceRecognitionService) {
            _voiceRecognitionServiceWeakReference = new WeakReference<>(voiceRecognitionService);
        }

        @Override
        protected Exception doInBackground(Void... voids) {
            try {
                Assets assets = new Assets(_voiceRecognitionServiceWeakReference.get()._context);
                File assetDir = assets.syncAssets();
                _voiceRecognitionServiceWeakReference.get().setupRecognizer(assetDir);
            } catch (IOException exception) {
                Logger.getInstance().Error(Tag, exception.toString());
                return exception;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            _voiceRecognitionServiceWeakReference.get()._broadcastController.SendBooleanBroadcast(BroadcastSetupSpeechRecognizerResult, BundleSetupSpeechRecognizerResult, result == null);
        }
    }
}
