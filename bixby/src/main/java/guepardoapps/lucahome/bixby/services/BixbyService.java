package guepardoapps.lucahome.bixby.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.lang.ref.WeakReference;

import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.controller.UserInformationController;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class BixbyService extends AccessibilityService implements IBixbyService {
    private static final String Tag = BixbyService.class.getSimpleName();

    private static final String BixbyPackage = "com.samsung.android.app.spage";
    public static final String ServiceId = String.format("guepardoapps.lucahome.bixby.services/.%s", Tag);

    private static BixbyService Singleton;

    private long _lastRunMillis = 0;
    private long _maxRunFrequencyMs = 500;

    private BroadcastController _broadcastController;
    private UserInformationController _userInformationController;

    private IBixbyPairService _bixbyPairService;

    public static BixbyService getInstance() {
        return Singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.getInstance().Debug(Tag, "onCreate");

        _broadcastController = new BroadcastController(this);
        _userInformationController = new UserInformationController(this);

        _bixbyPairService = BixbyPairService.getInstance();

        CheckIfBixbyIsAvailable();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.getInstance().Warning(Tag, "onDestroy");
    }

    @Override
    protected void onServiceConnected() {
        Logger.getInstance().Debug(Tag, "onServiceConnected");
        Singleton = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!_userInformationController.IsPackageInstalled(BixbyPackage)) {
            Logger.getInstance().Warning(Tag, "Bixby seems to be not available on this device!");
            return;
        }

        String activeWindowPackage = getActiveWindowPackage();

        long currentMillis = System.currentTimeMillis();
        long maxRunFrequencyMs = GetMaxRunFrequencyMs();
        boolean runTooSoon = (currentMillis - _lastRunMillis) < maxRunFrequencyMs;

        if (runTooSoon || !BixbyPackage.equals(activeWindowPackage)) {
            return;
        }

        try {
            _bixbyPairService.BixbyButtonPressed();
        } catch (Exception exception) {
            Logger.getInstance().Error(Tag, exception.toString());
        }

        _lastRunMillis = currentMillis;
        new DelayedBackButtonTask(this).execute();
    }

    @Override
    public void onInterrupt() {
        Logger.getInstance().Warning(Tag, "onInterrupt");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Logger.getInstance().Verbose(Tag, "onUnbind");
        Singleton = null;
        return false;
    }

    @Override
    public long GetMaxRunFrequencyMs() {
        return _maxRunFrequencyMs;
    }

    @Override
    public void SetMaxRunFrequencyMs(long maxRunFrequencyMs) {
        _maxRunFrequencyMs = maxRunFrequencyMs;
    }

    @Override
    public void CheckIfBixbyIsAvailable() {
        _broadcastController.SendBooleanBroadcast(BixbyAvailabilityBroadcast, BixbyAvailabilityBundle, _userInformationController.IsPackageInstalled(BixbyPackage));
    }

    @Override
    public boolean BixbyServiceIsAvailable() {
        return _userInformationController.IsPackageInstalled(BixbyPackage);
    }

    private String getActiveWindowPackage() {
        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
        return rootInActiveWindow != null ? rootInActiveWindow.getPackageName().toString() : null;
    }

    private static class DelayedBackButtonTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<BixbyService> _bixbyServiceWeakReference;

        DelayedBackButtonTask(BixbyService context) {
            _bixbyServiceWeakReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Logger.getInstance().Error(Tag, "interrupted");
            }

            BixbyService bixbyService = _bixbyServiceWeakReference.get();
            if (bixbyService != null) {
                bixbyService.performGlobalAction(GLOBAL_ACTION_BACK);
            }

            return null;
        }
    }
}
