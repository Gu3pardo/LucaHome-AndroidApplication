package guepardoapps.mediamirror.server;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import guepardoapps.lucahome.basic.controller.NetworkController;
import guepardoapps.lucahome.basic.utils.Logger;

public class ServerThread {
    private static final String TAG = ServerThread.class.getSimpleName();
    private Logger _logger;

    private int _socketServerPort;
    private ServerSocket _serverSocket;

    private DataHandler _dataHandler;

    private boolean _isRunning;

    public ServerThread(int port, @NonNull Context context) {
        NetworkController networkController = new NetworkController(context);

        _socketServerPort = port;
        _dataHandler = new DataHandler(context);

        _logger = new Logger(TAG);
        _logger.Debug("IpAddress: " + networkController.GetIpAddress());
        _logger.Debug("SocketServerPort: " + String.valueOf(_socketServerPort));
    }

    public void Start() {
        _logger.Debug("Start");
        if (_isRunning) {
            _logger.Warning("Already running!");
            return;
        }

        SocketServerThread socketServerThread = new SocketServerThread();
        Thread thread = new Thread(socketServerThread);
        thread.start();

        _isRunning = true;
    }

    public void Dispose() {
        _logger.Debug("Dispose");

        if (_serverSocket != null) {
            try {
                _serverSocket.close();
            } catch (IOException e) {
                _logger.Error(e.toString());
            }
        }

        _dataHandler.Dispose();

        _isRunning = false;
    }

    private class SocketServerThread extends Thread {
        @Override
        public void run() {
            try {
                _serverSocket = new ServerSocket(_socketServerPort);
                _logger.Debug("I'm waiting here: " + _serverSocket.getLocalPort());

                boolean isRunning = true;

                while (isRunning) {
                    Socket socket = _serverSocket.accept();

                    SocketServerReplyThread socketServerReplyThread = new SocketServerReplyThread(socket);
                    socketServerReplyThread.run();
                    isRunning = socketServerReplyThread.IsRunning();
                }

                _logger.Debug("No longer running!");
            } catch (IOException e) {
                _logger.Error(e.toString());
            }
        }
    }

    private class SocketServerReplyThread extends Thread {

        private Socket _hostThreadSocket;
        private BufferedReader _inputReader;
        private boolean _isRunning = true;

        SocketServerReplyThread(@NonNull Socket socket) {
            _logger.Debug("New SocketServerReplyThread");
            _hostThreadSocket = socket;
            _logger.Debug("_hostThreadSocket: " + _hostThreadSocket.toString());
        }

        public boolean IsRunning() {
            return _isRunning;
        }

        @Override
        public void run() {
            String response;
            boolean fail = false;
            _isRunning = true;

            try {
                InputStreamReader inputStreamReader = new InputStreamReader(_hostThreadSocket.getInputStream());
                _logger.Debug("inputStreamReader: " + inputStreamReader.toString());
                _inputReader = new BufferedReader(inputStreamReader);
                _logger.Debug("inputReader: " + _inputReader.toString());
                response = "OK";
            } catch (IOException e) {
                _logger.Error(e.toString());
                response = "Fail! " + e.toString();
                fail = true;
            }

            if (!fail) {
                try {
                    _logger.Debug("trying to read");
                    String read = _inputReader.readLine();
                    _logger.Information("read: " + read);
                    response = _dataHandler.PerformAction(read);
                } catch (IOException e) {
                    _logger.Error(e.toString());
                    response = "Fail! " + e.toString();
                }
            }

            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(_hostThreadSocket.getOutputStream());
                _logger.Information("outputStreamWriter is " + outputStreamWriter.toString());

                BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
                _logger.Information("bufferedWriter is " + bufferedWriter.toString());

                PrintWriter printWriter = new PrintWriter(bufferedWriter, true);
                _logger.Information("printWriter is " + printWriter.toString());

                printWriter.println(response);
                _logger.Information("printWriter println");
                printWriter.flush();
                _logger.Information("printWriter flush");

                printWriter.close();
                bufferedWriter.close();
                outputStreamWriter.close();
            } catch (IOException e) {
                _logger.Error(e.toString());
                _isRunning = false;
            } finally {
                _logger.Debug("response: " + response);
                try {
                    _hostThreadSocket.close();
                } catch (IOException e) {
                    _logger.Error(e.toString());
                }
            }
        }
    }
}