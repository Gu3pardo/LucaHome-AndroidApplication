package guepardoapps.lucahome.basic.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import guepardoapps.lucahome.basic.utils.Logger;

public class ReceiverController implements Serializable {

    private static final long serialVersionUID = 2288241732336744506L;

    private static String TAG = ReceiverController.class.getSimpleName();

    private Logger _logger;
    private Context _context;
    private List<BroadcastReceiver> _registeredReceiver;

    public ReceiverController(@NonNull Context context) {
        _logger = new Logger(TAG);
        _context = context;
        _registeredReceiver = new ArrayList<>();
    }

    public void RegisterReceiver(
            @NonNull BroadcastReceiver receiver,
            @NonNull String[] actions) {
        IntentFilter downloadStateFilter = new IntentFilter();
        for (String action : actions) {
            downloadStateFilter.addAction(action);
        }

        UnregisterReceiver(receiver);

        _context.registerReceiver(receiver, downloadStateFilter);
        _registeredReceiver.add(receiver);
    }

    public void UnregisterReceiver(@NonNull BroadcastReceiver receiver) {
        for (int index = 0; index < _registeredReceiver.size(); index++) {
            if (_registeredReceiver.get(index) == receiver) {
                try {
                    _context.unregisterReceiver(receiver);
                    _registeredReceiver.remove(index);
                } catch (Exception e) {
                    _logger.Error(e.toString());
                }
                break;
            }
        }
    }

    public void Dispose() {
        _logger.Debug("Dispose");

        for (int index = 0; index < _registeredReceiver.size(); index++) {
            try {
                _context.unregisterReceiver(_registeredReceiver.get(index));
                _registeredReceiver.remove(index);
            } catch (Exception e) {
                _logger.Error(e.toString());
            }
        }
    }
}
