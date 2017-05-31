package guepardoapps.lucahome.views;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.views.controller.socket.SocketViewController;
import shortbread.Shortcut;

@Shortcut(id = "sockets", icon = R.drawable.socket, shortLabel = "Sockets")
public class SocketView extends AppCompatActivity {

    private static final String TAG = SocketView.class.getSimpleName();
    private LucaHomeLogger _logger;

    private SocketViewController _socketViewController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_skeleton_nested_list_carousel);

        _logger = new LucaHomeLogger(TAG);
        _logger.Debug("onCreate");

        _socketViewController = new SocketViewController(this);
        _socketViewController.onCreate();
    }

    @Override
    public void onResume() {
        super.onResume();
        _logger.Debug("onResume");
        _socketViewController.onResume();
    }

    @Override
    public void onPause() {
        _logger.Debug("onPause");
        _socketViewController.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        _logger.Debug("onDestroy");
        _socketViewController.onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return _socketViewController.NavigateToHome();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_basic_reload, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.buttonReload) {
            return _socketViewController.ReloadSockets();
        }
        return super.onOptionsItemSelected(item);
    }
}
