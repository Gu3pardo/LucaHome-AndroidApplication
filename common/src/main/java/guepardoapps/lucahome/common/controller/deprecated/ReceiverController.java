package guepardoapps.lucahome.common.controller.deprecated;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class ReceiverController implements IReceiverController {
    private static String Tag = ReceiverController.class.getSimpleName();

    private Context _context;
    private List<BroadcastReceiver> _registeredReceiver;

    public ReceiverController(@NonNull Context context) {
        _context = context;
        _registeredReceiver = new ArrayList<>();
    }

    @Override
    public void RegisterReceiver(@NonNull BroadcastReceiver receiver, @NonNull String[] actions) {
        IntentFilter downloadStateFilter = new IntentFilter();
        for (String action : actions) {
            downloadStateFilter.addAction(action);
        }

        UnregisterReceiver(receiver);

        _context.registerReceiver(receiver, downloadStateFilter);
        _registeredReceiver.add(receiver);
    }

    @Override
    public void UnregisterReceiver(@NonNull BroadcastReceiver receiver) {
        for (int index = 0; index < _registeredReceiver.size(); index++) {
            if (_registeredReceiver.get(index) == receiver) {
                try {
                    _context.unregisterReceiver(receiver);
                    _registeredReceiver.remove(index);
                } catch (Exception exception) {
                    Logger.getInstance().Error(Tag, exception.toString());
                }
                break;
            }
        }
    }

    @Override
    public void Dispose() {
        for (int index = 0; index < _registeredReceiver.size(); index++) {
            try {
                _context.unregisterReceiver(_registeredReceiver.get(index));
                _registeredReceiver.remove(index);
            } catch (Exception exception) {
                Logger.getInstance().Error(Tag, exception.toString());
            }
        }
    }
}
