package guepardoapps.lucahome.basic.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Serializable;

public class Logger implements Serializable {

    private static final long serialVersionUID = -6278387904140268519L;

    private String _tag;
    private boolean _debuggingEnabled;

    public Logger(@NonNull String tag, boolean debuggingEnabled) {
        _tag = tag;
        _debuggingEnabled = debuggingEnabled;
        Debug("Created logger for " + _tag + "! Is Enabled: " + String.valueOf(_debuggingEnabled));
    }

    public Logger(@NonNull String tag) {
        this(tag, true);
    }

    public void SetEnable(boolean debuggingEnabled) {
        _debuggingEnabled = debuggingEnabled;
    }

    public boolean GetEnable() {
        return _debuggingEnabled;
    }

    public void Verbose(@NonNull String message) {
        if (_debuggingEnabled) {
            if (message.length() > 0) {
                Log.v(_tag, message);
            }
        }
    }

    public void Verbose(int message) {
        if (_debuggingEnabled) {
            Log.v(_tag, String.valueOf(message));
        }
    }

    public void Verbose(double message) {
        if (_debuggingEnabled) {
            Log.v(_tag, String.valueOf(message));
        }
    }

    public void Verbose(boolean message) {
        if (_debuggingEnabled) {
            Log.v(_tag, String.valueOf(message));
        }
    }

    public void Debug(@NonNull String message) {
        if (_debuggingEnabled) {
            if (message.length() > 0) {
                Log.d(_tag, message);
            }
        }
    }

    public void Debug(int message) {
        if (_debuggingEnabled) {
            Log.d(_tag, String.valueOf(message));
        }
    }

    public void Debug(double message) {
        if (_debuggingEnabled) {
            Log.d(_tag, String.valueOf(message));
        }
    }

    public void Debug(boolean message) {
        if (_debuggingEnabled) {
            Log.d(_tag, String.valueOf(message));
        }
    }

    public void Information(@NonNull String message) {
        if (_debuggingEnabled) {
            if (message.length() > 0) {
                Log.i(_tag, message);
            }
        }
    }

    public void Information(int message) {
        if (_debuggingEnabled) {
            Log.i(_tag, String.valueOf(message));
        }
    }

    public void Information(double message) {
        if (_debuggingEnabled) {
            Log.i(_tag, String.valueOf(message));
        }
    }

    public void Information(boolean message) {
        if (_debuggingEnabled) {
            Log.i(_tag, String.valueOf(message));
        }
    }

    public void Warning(@NonNull String message) {
        if (_debuggingEnabled) {
            if (message.length() > 0) {
                Log.w(_tag, message);
            }
        }
    }

    public void Warning(int message) {
        if (_debuggingEnabled) {
            Log.w(_tag, String.valueOf(message));
        }
    }

    public void Warning(double message) {
        if (_debuggingEnabled) {
            Log.w(_tag, String.valueOf(message));
        }
    }

    public void Warning(boolean message) {
        if (_debuggingEnabled) {
            Log.w(_tag, String.valueOf(message));
        }
    }

    public void Error(@NonNull String message) {
        if (_debuggingEnabled) {
            if (message.length() > 0) {
                Log.e(_tag, message);
            }
        }
    }

    public void Error(int message) {
        if (_debuggingEnabled) {
            Log.e(_tag, String.valueOf(message));
        }
    }

    public void Error(double message) {
        if (_debuggingEnabled) {
            Log.e(_tag, String.valueOf(message));
        }
    }

    public void Error(boolean message) {
        if (_debuggingEnabled) {
            Log.e(_tag, String.valueOf(message));
        }
    }
}
