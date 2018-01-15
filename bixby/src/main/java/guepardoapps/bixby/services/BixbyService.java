package guepardoapps.bixby.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.lang.ref.WeakReference;

import guepardoapps.lucahome.basic.utils.Logger;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class BixbyService extends AccessibilityService {
    private static final String TAG = BixbyService.class.getSimpleName();

    private static final String BIXBY_PACKAGE = "com.samsung.android.app.spage";
    public static final String SERVICE_ID = String.format("guepardoapps.bixby.services/.%s", TAG);

    private long _lastRunMillis = 0;
    private long _maxRunFrequencyMs = 500;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.getInstance().Debug(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.getInstance().Warning(TAG, "onDestroy");
    }

    @Override
    protected void onServiceConnected() {
        Logger.getInstance().Debug(TAG, "onServiceConnected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String activeWindowPackage = getActiveWindowPackage();

        long currentMillis = System.currentTimeMillis();
        long maxRunFrequencyMs = GetMaxRunFrequencyMs();
        boolean runTooSoon = (currentMillis - _lastRunMillis) < maxRunFrequencyMs;

        if (runTooSoon || !BIXBY_PACKAGE.equals(activeWindowPackage)) {
            return;
        }

        BixbyPairService.getInstance().BixbyButtonPressed();

        _lastRunMillis = currentMillis;
        new DelayedBackButtonTask(this).execute();
    }

    @Override
    public void onInterrupt() {
        Logger.getInstance().Warning(TAG, "onInterrupt");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Logger.getInstance().Verbose(TAG, "onUnbind");
        return false;
    }

    public long GetMaxRunFrequencyMs() {
        return _maxRunFrequencyMs;
    }

    public void SetMaxRunFrequencyMs(long maxRunFrequencyMs) {
        _maxRunFrequencyMs = maxRunFrequencyMs;
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
                Logger.getInstance().Error(TAG, "interrupted");
            }

            BixbyService bixbyService = _bixbyServiceWeakReference.get();
            if (bixbyService != null) {
                bixbyService.performGlobalAction(GLOBAL_ACTION_BACK);
            }

            return null;
        }
    }
}
