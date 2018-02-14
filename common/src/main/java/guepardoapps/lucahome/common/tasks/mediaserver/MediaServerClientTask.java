package guepardoapps.lucahome.common.tasks.mediaserver;

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

import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class MediaServerClientTask extends AsyncTask<Void, Void, Void> implements IMediaServerClientTask {
    private static final String Tag = MediaServerClientTask.class.getSimpleName();

    private static final int DefaultTimeoutMs = 500;

    private BroadcastController _broadcastController;

    private String _address;
    private int _port;
    private String _communication;
    private int _timeoutMSec;

    private String _response;
    private boolean _responseError;

    public MediaServerClientTask(@NonNull Context context, @NonNull String address, int port, @NonNull String communication, int timeoutMSec) {
        _broadcastController = new BroadcastController(context);

        _address = address;
        _port = port;
        _communication = communication + "\n";

        if (timeoutMSec <= 0) {
            Logger.getInstance().Warning(Tag, "TimeOut was set lower then 1ms! Setting to default!");
            timeoutMSec = DefaultTimeoutMs;
        }

        _timeoutMSec = timeoutMSec;
        _response = "";
    }

    public MediaServerClientTask(@NonNull Context context, @NonNull String address, int port, @NonNull String communication) {
        this(context, address, port, communication, DefaultTimeoutMs);
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        Socket socket = null;
        try {
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
            Logger.getInstance().Error(Tag, exception.getMessage());
            _response = "UnknownHostException: " + exception.getMessage();
            _responseError = true;

        } catch (IOException exception) {
            Logger.getInstance().Error(Tag, exception.getMessage());
            _response = "IOException: " + exception.getMessage();
            _responseError = true;

        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException exception) {
                    Logger.getInstance().Error(Tag, exception.getMessage());
                }
            }
            _communication = "";
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (_response == null) {
            Logger.getInstance().Error(Tag, "Response is null!");
            return;
        }

        if (_responseError) {
            Logger.getInstance().Error(Tag, "An response error appeared!");
            return;
        }

        _broadcastController.SendStringBroadcast(MediaServerClientTaskBroadcast, MediaServerClientTaskBundle, _response);

        super.onPostExecute(result);
    }
}