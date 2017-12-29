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

import guepardoapps.lucahome.basic.utils.Logger;

public class ServerThread {
    private static final String TAG = ServerThread.class.getSimpleName();

    private int _socketServerPort;
    private ServerSocket _serverSocket;

    private DataHandler _dataHandler;

    private boolean _isRunning;

    public ServerThread(int port, @NonNull Context context) {
        _socketServerPort = port;
        _dataHandler = new DataHandler(context);
    }

    public void Start() {
        if (_isRunning) {
            Logger.getInstance().Warning(TAG, "Already running!");
            return;
        }

        SocketServerThread socketServerThread = new SocketServerThread();
        Thread thread = new Thread(socketServerThread);
        thread.start();

        _isRunning = true;
    }

    public void Dispose() {
        if (_serverSocket != null) {
            try {
                _serverSocket.close();
            } catch (IOException e) {
                Logger.getInstance().Error(TAG, e.getMessage());
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
                boolean isRunning = true;
                while (isRunning) {
                    Socket socket = _serverSocket.accept();
                    SocketServerReplyThread socketServerReplyThread = new SocketServerReplyThread(socket);
                    socketServerReplyThread.run();
                    isRunning = socketServerReplyThread.IsRunning();
                }
            } catch (IOException e) {
                Logger.getInstance().Error(TAG, e.getMessage());
            }
        }
    }

    private class SocketServerReplyThread extends Thread {

        private Socket _hostThreadSocket;
        private BufferedReader _inputReader;
        private boolean _isRunning = true;

        SocketServerReplyThread(@NonNull Socket socket) {
            _hostThreadSocket = socket;
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
                _inputReader = new BufferedReader(inputStreamReader);
                response = "OK";
            } catch (IOException e) {
                Logger.getInstance().Error(TAG, e.getMessage());
                response = "Fail! " + e.toString();
                fail = true;
            }

            if (!fail) {
                try {
                    String read = _inputReader.readLine();
                    response = _dataHandler.PerformAction(read);
                } catch (IOException e) {
                    Logger.getInstance().Error(TAG, e.getMessage());
                    response = "Fail! " + e.toString();
                }
            }

            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(_hostThreadSocket.getOutputStream());
                BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

                PrintWriter printWriter = new PrintWriter(bufferedWriter, true);
                printWriter.println(response);
                printWriter.flush();
                printWriter.close();

                bufferedWriter.close();
                outputStreamWriter.close();
            } catch (IOException e) {
                Logger.getInstance().Error(TAG, e.getMessage());
                _isRunning = false;
            } finally {
                try {
                    _hostThreadSocket.close();
                } catch (IOException e) {
                    Logger.getInstance().Error(TAG, e.getMessage());
                }
            }
        }
    }
}