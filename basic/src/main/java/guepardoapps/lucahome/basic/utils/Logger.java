package guepardoapps.lucahome.basic.utils;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Locale;

@SuppressWarnings({"unused"})
public class Logger implements Serializable {
    private static final long serialVersionUID = -6278387904140268519L;

    private static final int MAX_LOG_FILE_LINES = 16536;

    private static final Logger SINGLETON = new Logger();

    public static Logger getInstance() {
        return SINGLETON;
    }

    private File _documentDir;
    private File _logFile;
    private boolean _debuggingEnabled;
    private boolean _writeToFileEnabled;

    private Logger() {
        _documentDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        _debuggingEnabled = true;
        _writeToFileEnabled = true;
        createNewLogFile();
    }

    public void SetDebuggingEnable(boolean debuggingEnabled) {
        _debuggingEnabled = debuggingEnabled;
    }

    public boolean GetDebuggingEnable() {
        return _debuggingEnabled;
    }

    public void SetWriteToFileEnabled(boolean writeToFileEnabled) {
        _writeToFileEnabled = writeToFileEnabled;
        checkIfLogFileExists();
    }

    public boolean GetWriteToFileEnabled() {
        return _writeToFileEnabled;
    }

    public void Verbose(@NonNull String tag, String message) {
        if (_debuggingEnabled) {
            if (message == null) {
                message = "Message is null!!!";
            }

            if (message.length() > 0) {
                Log.v(tag, message);
                writeToLogFile(String.format(Locale.getDefault(), "%s : %s | %s | %s", getCurrentDateTimeString(), "Verbose    ", tag, message));
            }
        }
    }

    public void Verbose(@NonNull String tag, int message) {
        Verbose(tag, String.valueOf(message));
    }

    public void Verbose(@NonNull String tag, double message) {
        Verbose(tag, String.valueOf(message));
    }

    public void Verbose(@NonNull String tag, boolean message) {
        Verbose(tag, String.valueOf(message));
    }

    public void Debug(@NonNull String tag, String message) {
        if (_debuggingEnabled) {
            if (message == null) {
                message = "Message is null!!!";
            }

            if (message.length() > 0) {
                writeToLogFile(String.format(Locale.getDefault(), "%s : %s | %s | %s", getCurrentDateTimeString(), "Debug      ", tag, message));
                Log.d(tag, message);
            }
        }
    }

    public void Debug(@NonNull String tag, int message) {
        Debug(tag, String.valueOf(message));
    }

    public void Debug(@NonNull String tag, double message) {
        Debug(tag, String.valueOf(message));
    }

    public void Debug(@NonNull String tag, boolean message) {
        Debug(tag, String.valueOf(message));
    }

    public void Information(@NonNull String tag, String message) {
        if (_debuggingEnabled) {
            if (message == null) {
                message = "Message is null!!!";
            }

            if (message.length() > 0) {
                Log.i(tag, message);
                writeToLogFile(String.format(Locale.getDefault(), "%s : %s | %s | %s", getCurrentDateTimeString(), "Information", tag, message));
            }
        }
    }

    public void Information(@NonNull String tag, int message) {
        Information(tag, String.valueOf(message));
    }

    public void Information(@NonNull String tag, double message) {
        Information(tag, String.valueOf(message));
    }

    public void Information(@NonNull String tag, boolean message) {
        Information(tag, String.valueOf(message));
    }

    public void Warning(@NonNull String tag, String message) {
        if (_debuggingEnabled) {
            if (message == null) {
                message = "Message is null!!!";
            }

            if (message.length() > 0) {
                Log.w(tag, message);
                writeToLogFile(String.format(Locale.getDefault(), "%s : %s | %s | %s", getCurrentDateTimeString(), "Warning    ", tag, message));
            }
        }
    }

    public void Warning(@NonNull String tag, int message) {
        Warning(tag, String.valueOf(message));
    }

    public void Warning(@NonNull String tag, double message) {
        Warning(tag, String.valueOf(message));
    }

    public void Warning(@NonNull String tag, boolean message) {
        Warning(tag, String.valueOf(message));
    }

    public void Error(@NonNull String tag, String message) {
        if (_debuggingEnabled) {
            if (message == null) {
                message = "Message is null!!!";
            }

            if (message.length() > 0) {
                Log.e(tag, message);
                writeToLogFile(String.format(Locale.getDefault(), "%s : %s | %s | %s", getCurrentDateTimeString(), "Error      ", tag, message));
            }
        }
    }

    public void Error(@NonNull String tag, int message) {
        Error(tag, String.valueOf(message));
    }

    public void Error(@NonNull String tag, double message) {
        Error(tag, String.valueOf(message));
    }

    public void Error(@NonNull String tag, boolean message) {
        Error(tag, String.valueOf(message));
    }

    private void createNewLogFile() {
        _logFile = new File(_documentDir, getFileName());
        checkIfLogFileExists();
    }

    private String getFileName() {
        return String.format(Locale.getDefault(), "LucaHome-Log-%s.txt", getCurrentDateTimeString());
    }

    private String getCurrentDateTimeString() {
        Calendar now = Calendar.getInstance();
        return String.format(Locale.getDefault(), "%04d.%02d.%02d-%02d.%02d", now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR), now.get(Calendar.MINUTE));
    }

    private boolean checkIfLogFileExists() {
        if (!_logFile.exists()) {
            try {
                boolean success = _logFile.createNewFile();
                _writeToFileEnabled = success;
                return success;
            } catch (IOException ioException) {
                ioException.printStackTrace();
                _writeToFileEnabled = false;
                return false;
            }
        }
        _writeToFileEnabled = true;
        return true;
    }

    private boolean writeToLogFile(@NonNull String message) {
        if (!_writeToFileEnabled) {
            return false;
        }

        if (getLineCountInLogFile() >= MAX_LOG_FILE_LINES) {
            createNewLogFile();
        }

        try {
            // true to set append to file flag
            FileWriter fileWriter = new FileWriter(_logFile, true);
            // BufferedWriter for performance
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.append(message);
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return false;
        }

        return true;
    }

    private int getLineCountInLogFile() {
        int linenumber = 0;

        if (_logFile.exists()) {
            try {
                FileReader fileReader = new FileReader(_logFile);

                LineNumberReader lineNumberReader = new LineNumberReader(fileReader);
                while (lineNumberReader.readLine() != null) {
                    linenumber++;
                }
                lineNumberReader.close();

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        return linenumber;
    }
}
