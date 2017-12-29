package guepardoapps.mediamirror.controller;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.mediamirror.R;
import guepardoapps.mediamirror.adapter.SocketListViewAdapter;

public class DialogController {
    // private static final String TAG = DialogController.class.getSimpleName();

    private Context _context;

    public DialogController(@NonNull Context context) {
        _context = context;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void DisplayTemperatureDialog(@NonNull String url) {
        final Dialog dialog = createDialog();
        dialog.setContentView(R.layout.dialog_webview);

        TextView titleView = dialog.findViewById(R.id.dialog_title_text_view);
        titleView.setText("Temperature Graph");

        com.rey.material.widget.Button closeButton = dialog.findViewById(R.id.dialog_button_close);
        closeButton.setOnClickListener(v -> dialog.dismiss());

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        WebView webview = dialog.findViewById(R.id.dialog_webview);

        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setSupportZoom(true);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.setWebViewClient(new WebViewClient());
        webview.setInitialScale(100);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(false);

        webview.loadUrl(url);

        displayDialog(dialog);
    }

    public void DisplayListViewDialog(@NonNull String title, @NonNull ArrayList<String> list) {
        final Dialog dialog = createDialog();
        dialog.setContentView(R.layout.dialog_listview);

        TextView titleView = dialog.findViewById(R.id.dialog_title_text_view);
        titleView.setText(title);

        com.rey.material.widget.Button closeButton = dialog.findViewById(R.id.dialog_button_close);
        closeButton.setOnClickListener(v -> dialog.dismiss());

        ListView listView = dialog.findViewById(R.id.dialog_list_view);
        listView.setAdapter(new ArrayAdapter<>(_context, R.layout.listview_single_line, list));

        displayDialog(dialog);
    }

    public void DisplaySocketListViewDialog(SerializableList<WirelessSocket> socketList) {
        final Dialog dialog = createDialog();
        dialog.setContentView(R.layout.dialog_listview);

        TextView titleView = dialog.findViewById(R.id.dialog_title_text_view);
        titleView.setText("Wireless Sockets");

        com.rey.material.widget.Button closeButton = dialog.findViewById(R.id.dialog_button_close);
        closeButton.setOnClickListener(v -> dialog.dismiss());

        ListView listView = dialog.findViewById(R.id.dialog_list_view);
        listView.setAdapter(new SocketListViewAdapter(_context, socketList, dialog));

        displayDialog(dialog);
    }

    private Dialog createDialog() {
        final Dialog dialog = new Dialog(_context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private void displayDialog(final Dialog dialog) {
        dialog.setCancelable(true);
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }
}
