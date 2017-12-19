package guepardoapps.lucahome.common.builder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rey.material.app.Dialog;
import com.rey.material.app.ThemeManager;

import java.util.ArrayList;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.classes.MapContent;
import guepardoapps.lucahome.common.classes.Temperature;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.service.MenuService;
import guepardoapps.lucahome.common.service.ShoppingListService;
import guepardoapps.lucahome.common.service.WirelessSocketService;

public class MapContentBuilder {
    public static int GetDrawable(@NonNull MapContent mapContent) {
        switch (mapContent.GetDrawingType()) {
            case Socket:
                if (mapContent.GetWirelessSocket() == null) {
                    return R.drawable.drawing_socket_off;
                }

                if (mapContent.GetWirelessSocket().IsActivated()) {
                    return R.drawable.drawing_socket_on;
                } else {
                    return R.drawable.drawing_socket_off;
                }

            case LAN:
                // TODO add drawable for LAN
                return R.drawable.drawing_socket_off;

            case MediaServer:
                if (mapContent.GetWirelessSocket() == null) {
                    return R.drawable.drawing_mediamirror_off;
                }

                if (mapContent.GetWirelessSocket().IsActivated()) {
                    return R.drawable.drawing_mediamirror_on;
                } else {
                    return R.drawable.drawing_mediamirror_off;
                }

            case RaspberryPi:
                // TODO add drawable for RaspberryPi
                return R.drawable.drawing_socket_off;

            case NAS:
                // TODO add drawable for NAS
                return R.drawable.drawing_socket_off;

            case LightSwitch:
                // TODO add drawable for LightSwitch
                return R.drawable.drawing_socket_off;

            case Temperature:
                return (mapContent.GetTemperature() != null ? mapContent.GetTemperature().GetDrawable() : R.drawable.drawing_temperature);

            case PuckJS:
                return R.drawable.drawing_puckjs;

            case Menu:
                return R.drawable.drawing_menu;

            case ShoppingList:
                return R.drawable.drawing_shoppinglist;

            case Camera:
                return R.drawable.drawing_camera;

            case Null:
            default:
                return R.drawable.drawing_socket_off;
        }
    }

    public static Runnable GetRunnable(@NonNull MapContent mapContent, @NonNull Context context) {
        switch (mapContent.GetDrawingType()) {
            case Socket:
                return (mapContent.GetWirelessSocket() != null ? createSocketRunnable(context, mapContent.GetWirelessSocket()) : null);

            case LAN:
                // TODO add Runnable for LAN
                return null;

            case MediaServer:
                // TODO add Runnable for MediaServer
                return (mapContent.GetWirelessSocket() != null ? createSocketRunnable(context, mapContent.GetWirelessSocket()) : null);

            case RaspberryPi:
                // TODO add Runnable for RaspberryPi
                return null;

            case NAS:
                // TODO add Runnable for NAS
                return null;

            case LightSwitch:
                // TODO add Runnable for LightSwitch
                return null;

            case Temperature:
                return (mapContent.GetTemperature() != null ? createTemperatureRunnable(context, mapContent.GetTemperature()) : null);

            case PuckJS:
                // TODO add Runnable for PuckJS
                return null;

            case Menu:
                return createMenuRunnable(context);

            case ShoppingList:
                return createShoppingListRunnable(context);

            case Camera:
                return createCameraRunnable(context);

            case Null:
            default:
                return null;
        }
    }

    private static Runnable createSocketRunnable(@NonNull final Context context, @NonNull final WirelessSocket wirelessSocket) {
        return () -> {
            boolean isLightTheme = ThemeManager.getInstance().getCurrentTheme() == 0;

            final Dialog dialog = new Dialog(context);
            dialog
                    .title(String.format(Locale.getDefault(), "Change state of %s?", wirelessSocket.GetName()))
                    .positiveAction("Activate")
                    .negativeAction("Deactivate")
                    .applyStyle(isLightTheme ? R.style.SimpleDialogLight : R.style.SimpleDialog)
                    .setCancelable(true);

            dialog.positiveActionClickListener(view -> {
                try {
                    WirelessSocketService.getInstance().SetWirelessSocketState(wirelessSocket, true);
                } catch (Exception exception) {
                    // Perhaps log exception
                }
                dialog.dismiss();
            });

            dialog.negativeActionClickListener(view -> {
                try {
                    WirelessSocketService.getInstance().SetWirelessSocketState(wirelessSocket, false);
                } catch (Exception exception) {
                    // Perhaps log exception
                }
                dialog.dismiss();
            });

            dialog.show();
        };
    }

    @SuppressLint("SetJavaScriptEnabled")
    private static Runnable createTemperatureRunnable(@NonNull final Context context, @NonNull final Temperature temperature) {
        return () -> {
            if (temperature.GetGraphPath().length() > 0) {
                final android.app.Dialog dialog = new android.app.Dialog(context);

                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_webview);

                TextView titleView = dialog.findViewById(R.id.dialog_title_text_view);
                titleView.setText(temperature.GetArea());

                com.rey.material.widget.Button closeButton = dialog.findViewById(R.id.dialog_button_close);
                closeButton.setOnClickListener(v -> dialog.dismiss());

                String url = temperature.GetGraphPath();

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

                dialog.setCancelable(true);
                dialog.show();

                Window window = dialog.getWindow();
                if (window != null) {
                    window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                }
            } else {
                Toasty.info(context, String.format(Locale.getDefault(), "No action for temperature in area %s", temperature.GetArea()), Toast.LENGTH_LONG).show();
            }
        };
    }

    private static Runnable createShoppingListRunnable(@NonNull final Context context) {
        return () -> displayListViewDialog(context, "Shopping list", ShoppingListService.getInstance().GetShoppingDetailList());
    }

    private static Runnable createMenuRunnable(@NonNull final Context context) {
        return () -> displayListViewDialog(context, "Menu", MenuService.getInstance().GetMenuNameList());
    }

    private static Runnable createCameraRunnable(@NonNull final Context context) {
        return () -> Toasty.error(context, "Method for camera needs to be implemented!", Toast.LENGTH_LONG).show();
    }

    private static void displayListViewDialog(@NonNull Context context, @NonNull String title, @NonNull ArrayList<String> list) {
        final android.app.Dialog dialog = new android.app.Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_listview);

        TextView titleView = dialog.findViewById(R.id.dialog_title_text_view);
        titleView.setText(title);

        com.rey.material.widget.Button closeButton = dialog.findViewById(R.id.dialog_button_close);
        closeButton.setOnClickListener(v -> dialog.dismiss());

        ListView listView = dialog.findViewById(R.id.dialog_list_view);
        listView.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, list));

        dialog.setCancelable(true);
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
    }
}
