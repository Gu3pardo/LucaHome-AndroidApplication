package guepardoapps.lucahome.basic.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Serializable;

public class Logger implements Serializable {
    private static final long serialVersionUID = -6278387904140268519L;

    private static final Logger SINGLETON = new Logger();

    public static Logger getInstance() {
        return SINGLETON;
    }

    private boolean _debuggingEnabled;

    private Logger() {
    }

    public void SetEnable(boolean debuggingEnabled) {
        _debuggingEnabled = debuggingEnabled;
    }

    public boolean GetEnable() {
        return _debuggingEnabled;
    }

    public void Verbose(@NonNull String tag, @NonNull String message) {
        if (_debuggingEnabled) {
            if (message.length() > 0) {
                Log.v(tag, message);
            }
        }
    }

    public void Verbose(@NonNull String tag, int message) {
        if (_debuggingEnabled) {
            Log.v(tag, String.valueOf(message));
        }
    }

    public void Verbose(@NonNull String tag, double message) {
        if (_debuggingEnabled) {
            Log.v(tag, String.valueOf(message));
        }
    }

    public void Verbose(@NonNull String tag, boolean message) {
        if (_debuggingEnabled) {
            Log.v(tag, String.valueOf(message));
        }
    }

    public void Debug(@NonNull String tag, @NonNull String message) {
        if (_debuggingEnabled) {
            if (message.length() > 0) {
                Log.d(tag, message);
            }
        }
    }

    public void Debug(@NonNull String tag, int message) {
        if (_debuggingEnabled) {
            Log.d(tag, String.valueOf(message));
        }
    }

    public void Debug(@NonNull String tag, double message) {
        if (_debuggingEnabled) {
            Log.d(tag, String.valueOf(message));
        }
    }

    public void Debug(@NonNull String tag, boolean message) {
        if (_debuggingEnabled) {
            Log.d(tag, String.valueOf(message));
        }
    }

    public void Information(@NonNull String tag, @NonNull String message) {
        if (_debuggingEnabled) {
            if (message.length() > 0) {
                Log.i(tag, message);
            }
        }
    }

    public void Information(@NonNull String tag, int message) {
        if (_debuggingEnabled) {
            Log.i(tag, String.valueOf(message));
        }
    }

    public void Information(@NonNull String tag, double message) {
        if (_debuggingEnabled) {
            Log.i(tag, String.valueOf(message));
        }
    }

    public void Information(@NonNull String tag, boolean message) {
        if (_debuggingEnabled) {
            Log.i(tag, String.valueOf(message));
        }
    }

    public void Warning(@NonNull String tag, @NonNull String message) {
        if (_debuggingEnabled) {
            if (message.length() > 0) {
                Log.w(tag, message);
            }
        }
    }

    public void Warning(@NonNull String tag, int message) {
        if (_debuggingEnabled) {
            Log.w(tag, String.valueOf(message));
        }
    }

    public void Warning(@NonNull String tag, double message) {
        if (_debuggingEnabled) {
            Log.w(tag, String.valueOf(message));
        }
    }

    public void Warning(@NonNull String tag, boolean message) {
        if (_debuggingEnabled) {
            Log.w(tag, String.valueOf(message));
        }
    }

    public void Error(@NonNull String tag, @NonNull String message) {
        if (_debuggingEnabled) {
            if (message.length() > 0) {
                Log.e(tag, message);
            }
        }
    }

    public void Error(@NonNull String tag, int message) {
        if (_debuggingEnabled) {
            Log.e(tag, String.valueOf(message));
        }
    }

    public void Error(@NonNull String tag, double message) {
        if (_debuggingEnabled) {
            Log.e(tag, String.valueOf(message));
        }
    }

    public void Error(@NonNull String tag, boolean message) {
        if (_debuggingEnabled) {
            Log.e(tag, String.valueOf(message));
        }
    }
}
