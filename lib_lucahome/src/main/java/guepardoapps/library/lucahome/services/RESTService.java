package guepardoapps.library.lucahome.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.constants.Timeouts;
import guepardoapps.library.lucahome.common.dto.ActionDto;
import guepardoapps.library.lucahome.common.enums.LucaServerAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.DatabaseController;
import guepardoapps.library.lucahome.runnable.DeleteStoredActionRunnable;

import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.DialogController;
import guepardoapps.library.toolset.controller.NetworkController;
import guepardoapps.library.toolset.scheduler.*;

public class RESTService extends Service {

    private static final String TAG = RESTService.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final int TIMEOUT_MS = 3000;

    private Context _context;
    private DatabaseController _databaseController;
    private NetworkController _networkController;
    private ScheduleService _scheduleService;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (_logger == null) {
            _logger = new LucaHomeLogger(TAG);
        }

        if (_context == null) {
            _context = this;
        }
        if (_networkController == null) {
            _networkController = new NetworkController(_context,
                    new DialogController(
                            _context,
                            ContextCompat.getColor(_context, R.color.TextIcon),
                            ContextCompat.getColor(_context, R.color.Background)));
        }

        if (_scheduleService == null) {
            _scheduleService = ScheduleService.getInstance();
        }

        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            _logger.Warn("Bundle is null!");
            stopSelf();
            return Service.START_NOT_STICKY;
        }

        String action = bundle.getString(Bundles.ACTION);
        _logger.Debug("Action is: " + action);
        if (action == null) {
            _logger.Warn("Action is null!");
            stopSelf();
            return Service.START_NOT_STICKY;
        }
        _logger.Debug("Action: " + action);

        String name = bundle.getString(Bundles.NAME);
        _logger.Debug("Name is: " + name);
        String broadcast = bundle.getString(Bundles.BROADCAST);
        _logger.Debug("Broadcast is: " + broadcast);

        if (_databaseController == null) {
            _databaseController = DatabaseController.getInstance();
        }

        if (!_networkController.IsNetworkAvailable()) {
            _logger.Warn("No network available!");
            if (action.contains(LucaServerAction.SET_SOCKET.toString())) {
                storeAction(name, action, broadcast);
            }
            return Service.START_NOT_STICKY;
        }

        if (!_networkController.IsHomeNetwork(Constants.LUCAHOME_SSID)) {
            _logger.Warn("No LucaHome network! ...");
            if (action.contains(LucaServerAction.SET_SOCKET.toString())) {
                storeAction(name, action, broadcast);
            }
            return Service.START_NOT_STICKY;
        }

        String user = bundle.getString(Bundles.USER);
        _logger.Debug("User is: " + user);
        String password = bundle.getString(Bundles.PASSPHRASE);
        if (user == null) {
            _logger.Warn("No user!");
            return Service.START_NOT_STICKY;
        }
        if (password == null) {
            _logger.Warn("No password!");
            return Service.START_NOT_STICKY;
        }

        if (Constants.SERVER_URLs.length == 0) {
            _logger.Error("You did not enter server ips!");
            Toasty.error(_context, "You did not enter server ips!", Toast.LENGTH_LONG).show();
            return Service.START_NOT_STICKY;
        }

        String url = Constants.SERVER_URLs[0] + Constants.ACTION_PATH + user + "&password=" + password + "&action="
                + action;
        _logger.Debug("Url: " + url);
        String[] actions = new String[]{url};

        SendActionTask task = new SendActionTask();
        task.setValues(name, broadcast, actions.length);
        task.execute(actions);

        if (_databaseController != null) {
            _databaseController.Dispose();
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void storeAction(String name, String action, String broadcast) {
        _logger.Debug(String.format("Store action %s for name %s with broadcast %s!", action, name, broadcast));

        ActionDto storeAction = new ActionDto(-1, name, action, broadcast);
        if (_databaseController.SaveAction(storeAction)) {
            _scheduleService.AddSchedule(name, new DeleteStoredActionRunnable(name, _databaseController),
                    Timeouts.DELETE_STORED_ACTION, false);
            Toasty.success(_context, "Saved action!", Toast.LENGTH_SHORT).show();
        } else {
            Toasty.error(_context, "Could not save action!", Toast.LENGTH_SHORT).show();
        }
    }

    private class SendActionTask extends AsyncTask<String, Void, String> {
        private static final String TAG = "RESTService";

        private LucaHomeLogger _logger;

        private String _name;
        private String _broadcast;

        private String[] _answer;

        public void setValues(
                String name,
                String broadcast,
                int answerSize) {
            _logger = new LucaHomeLogger(TAG);

            _name = name;
            _broadcast = broadcast;
            _answer = new String[answerSize];

            _logger.Debug(String.format(Locale.getDefault(), "SetValues: %s, %s, %s", _name, _broadcast, _answer));
        }

        @Override
        protected String doInBackground(String... actions) {
            String response;
            int answerIndex = 0;
            boolean downloadSuccess = false;

            for (String action : actions) {
                try {
                    _logger.Info("action: " + action);
                    response = "";

                    URL url = new URL(action);
                    URLConnection connection = url.openConnection();
                    connection.setConnectTimeout(TIMEOUT_MS);
                    InputStream inputStream = connection.getInputStream();

                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader reader = new BufferedReader(inputStreamReader);

                    String line;
                    while ((line = reader.readLine()) != null) {
                        response += line;
                    }

                    reader.close();
                    inputStreamReader.close();
                    inputStream.close();

                    downloadSuccess = true;
                    _answer[answerIndex] = response;
                    _logger.Info(response);

                    if (response.contains("Error")) {
                        Toasty.error(_context, response, Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    downloadSuccess = false;
                    _logger.Error(e.getMessage());
                } finally {
                    answerIndex++;
                }
            }

            return String.valueOf(downloadSuccess);
        }

        @Override
        protected void onPostExecute(String result) {
            _logger.Debug("downloadSuccess: " + result);

            if (result.contains(String.valueOf(false))) {
                _logger.Error("Failed to download!");
                stopSelf();
                return;
            }

            // Hack for deactivating all sockets
            if (_name.contains("SHOW_NOTIFICATION_SOCKET")) {
                BroadcastController broadcastController = new BroadcastController(RESTService.this);
                broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_SOCKETS);
            }
            // End hack
            else {
                if (_broadcast != null && _broadcast.length() > 0) {
                    _logger.Info(String.format(Locale.getDefault(),
                            "Sending broadcast %s with bundle %s and data %s!",
                            _broadcast, _name, _answer));
                    Intent broadcastIntent = new Intent(_broadcast);
                    Bundle broadcastData = new Bundle();
                    broadcastData.putStringArray(_name, _answer);
                    broadcastIntent.putExtras(broadcastData);
                    sendBroadcast(broadcastIntent);
                } else {
                    _logger.Error("Broadcast is null or has length 0!");
                }
            }

            stopSelf();
        }
    }
}
