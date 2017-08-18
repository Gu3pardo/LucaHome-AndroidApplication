package guepardoapps.lucahome.common.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Locale;

import guepardoapps.lucahome.basic.controller.BroadcastController;
import guepardoapps.lucahome.basic.utils.Logger;

public class ClientTask extends AsyncTask<Void, Void, Void> {
    public static final String ClientTaskBroadcast = "guepardoapps.lucahome.common.tasks.clienttask.finished";
    public static final String ClientTaskBundle = "ClientTaskBundle";

    private static final String TAG = ClientTask.class.getSimpleName();
    private Logger _logger;

    private static final int DEFAULT_TIMEOUT_MS = 500;

    private String _address;
    private int _port;
    private String _communication;
    private int _timeoutMSec;

    private String _response;
    private boolean _responseError;

    private BroadcastController _broadcastController;

    public ClientTask(
            @NonNull Context context,
            @NonNull String address,
            int port,
            @NonNull String communication,
            int timeoutMSec) {
        _logger = new Logger(TAG);
        _broadcastController = new BroadcastController(context);

        _address = address;
        _port = port;
        _communication = communication + "\n";

        _logger.Information(String.format(Locale.getDefault(), "Address: %s; Port: %d, Communication: %s", _address, _port, _communication));

        if (timeoutMSec <= 0) {
            _logger.Warning("TimeOut was set lower then 1ms! Setting to default!");
            timeoutMSec = DEFAULT_TIMEOUT_MS;
        }

        _timeoutMSec = timeoutMSec;
        _response = "";
    }

    public ClientTask(
            @NonNull Context context,
            @NonNull String address,
            int port,
            @NonNull String communication) {
        this(context, address, port, communication, DEFAULT_TIMEOUT_MS);
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        _logger.Debug("executing Task");
        Socket socket = null;
        try {
            _logger.Debug("New communication is set");
            socket = new Socket();
            socket.connect(new InetSocketAddress(_address, _port), _timeoutMSec);

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            PrintWriter printWriter = new PrintWriter(bufferedWriter, true);
            printWriter.println(_communication);
            printWriter.flush();

            InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
            BufferedReader inputReader = new BufferedReader(inputStreamReader);

            _response = inputReader.readLine();
            _responseError = false;

            inputReader.close();
            inputStreamReader.close();
            printWriter.close();
            bufferedWriter.close();
            outputStreamWriter.close();

        } catch (UnknownHostException exception) {
            _logger.Error(exception.getMessage());
            _response = "UnknownHostException: " + exception.getMessage();
            _responseError = true;

        } catch (IOException exception) {
            _logger.Error(exception.getMessage());
            _response = "IOException: " + exception.getMessage();
            _responseError = true;

        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    _logger.Error(e.toString());
                }
            }
            _communication = "";
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (_response == null) {
            _logger.Error("Response is null!");
            return;
        }

        if (_responseError) {
            _logger.Error("An response error appeared!");
            return;
        }

        _broadcastController.SendStringBroadcast(ClientTaskBroadcast, ClientTaskBundle, _response);

        super.onPostExecute(result);
    }
}