package guepardoapps.lucahome.basic.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Locale;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;
import guepardoapps.lucahome.basic.utils.Logger;

@SuppressWarnings({"unused", "WeakerAccess"})
public class CommandController {
    private static final String TAG = CommandController.class.getSimpleName();

    private Context _context;

    private Handler _commandHandler = new Handler();
    private CommandRunnable _commandRunnable = new CommandRunnable();

    private static final int RUNNABLE_TIMEOUT = 1000;
    private int _secondsSinceCmdReceived;
    private int _secondsToPerformCmd;

    private Handler _performCmdHandler = new Handler();
    private Runnable _performCmdCountdownRunnable = new Runnable() {
        @Override
        public void run() {
            _secondsSinceCmdReceived++;
            int timeToCmdPerform = _secondsToPerformCmd - _secondsSinceCmdReceived;

            if (timeToCmdPerform < 0) {
                _secondsSinceCmdReceived = -1;
                return;
            }

            Toasty.info(_context, String.format(Locale.GERMAN, "Performing command in %d seconds!", timeToCmdPerform), 750).show();
            _performCmdHandler.postDelayed(_performCmdCountdownRunnable, RUNNABLE_TIMEOUT);
        }
    };

    public CommandController(@NonNull Context context) {
        _context = context;
    }

    public boolean RebootDevice(int timeout) {
        if (!HasRoot()) {
            Logger.getInstance().Warning(TAG, "Device is not rooted!");
            return false;
        }

        if (timeout < 0) {
            Logger.getInstance().Error(TAG, "Timeout cannot be negative!");
            return false;
        }

        if (timeout % RUNNABLE_TIMEOUT != 0) {
            Logger.getInstance().Error(TAG, "Invalid timeout!");
            return false;
        }

        _secondsToPerformCmd = timeout / RUNNABLE_TIMEOUT;

        _commandRunnable.SetCommand(new String[]{"su", "-c", "reboot"});
        _commandHandler.postDelayed(_commandRunnable, timeout);

        _performCmdHandler.post(_performCmdCountdownRunnable);

        return true;
    }

    public boolean ShutDownDevice(int timeout) {
        if (!HasRoot()) {
            Logger.getInstance().Warning(TAG, "Device is not rooted!");
            return false;
        }

        if (timeout < 0) {
            Logger.getInstance().Error(TAG, "Timeout cannot be negative!");
            return false;
        }

        if (timeout % RUNNABLE_TIMEOUT != 0) {
            Logger.getInstance().Error(TAG, "Invalid timeout!");
            return false;
        }

        _secondsToPerformCmd = timeout / RUNNABLE_TIMEOUT;

        _commandRunnable.SetCommand(new String[]{"su", "-c", "reboot -p"});
        _commandHandler.postDelayed(_commandRunnable, timeout);

        _performCmdHandler.post(_performCmdCountdownRunnable);

        return true;
    }

    public boolean HasRoot() {
        return checkRootMethodBuildTags() || checkRootMethodFilePath() || checkRootMethodProcess();
    }

    public boolean PerformCommand(@NonNull String[] command) {
        if (!HasRoot()) {
            Logger.getInstance().Warning(TAG, "Device is not rooted!");
            return false;
        }

        try {
            Process proc = Runtime.getRuntime().exec(command);
            proc.waitFor();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean checkRootMethodBuildTags() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private boolean checkRootMethodFilePath() {
        String[] paths = {"/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su",
                "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su",
                "/data/local/su", "/su/bin/su"};

        for (String path : paths) {
            if (new File(path).exists()) {
                return true;
            }
        }

        return false;
    }

    private boolean checkRootMethodProcess() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return bufferedReader.readLine() != null;
        } catch (Throwable throwable) {
            Logger.getInstance().Debug(TAG, "No root detected with method process and error detected!");
            return false;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private class CommandRunnable implements Runnable {
        private String[] _command = null;

        public void SetCommand(@NonNull String[] command) {
            _command = command;
        }

        @Override
        public void run() {
            if (_command == null) {
                Logger.getInstance().Error(TAG, "Command is null!");
                return;
            }

            if (_command.length == 0) {
                Logger.getInstance().Error(TAG, "Command has no data!");
                return;
            }

            if (!PerformCommand(_command)) {
                Toasty.error(_context, "Performing of command failed!", Toast.LENGTH_SHORT).show();
            }

            _command = null;
        }
    }
}
