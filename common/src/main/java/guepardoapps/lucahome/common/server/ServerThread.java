package guepardoapps.lucahome.common.server;

import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import guepardoapps.lucahome.common.server.handler.IDataHandler;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings("WeakerAccess")
public class ServerThread implements IServerThread {
    private static final String Tag = ServerThread.class.getSimpleName();

    private int _socketServerPort;
    private IDataHandler _dataHandler;

    private ServerSocket _serverSocket;
    private boolean _isRunning;

    @Override
    public void Start(int port, @NonNull IDataHandler dataHandler) {
        Logger.getInstance().Debug(Tag, "Start");
        if (_isRunning) {
            Logger.getInstance().Warning(Tag, "Already running!");
            return;
        }
        _socketServerPort = port;
        _dataHandler = dataHandler;

        SocketServerThread socketServerThread = new SocketServerThread();
        Thread thread = new Thread(socketServerThread);
        thread.start();

        _isRunning = true;
    }

    @Override
    public void Dispose() {
        Logger.getInstance().Debug(Tag, "Dispose");
        if (_serverSocket != null) {
            try {
                _serverSocket.close();
            } catch (IOException e) {
                Logger.getInstance().Error(Tag, e.getMessage());
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
            } catch (IOException ioException) {
                Logger.getInstance().Error(Tag, ioException.toString());
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
                Logger.getInstance().Error(Tag, e.getMessage());
                response = "Fail! " + e.toString();
                fail = true;
            }

            if (!fail) {
                try {
                    String read = _inputReader.readLine();
                    response = _dataHandler.PerformAction(read);
                } catch (Exception exception) {
                    Logger.getInstance().Error(Tag, exception.getMessage());
                    response = "Fail! " + exception.toString();
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
            } catch (IOException ioException) {
                Logger.getInstance().Error(Tag, ioException.toString());
                _isRunning = false;
            } finally {
                try {
                    _hostThreadSocket.close();
                } catch (IOException ioException) {
                    Logger.getInstance().Error(Tag, ioException.toString());
                }
            }
        }
    }
}