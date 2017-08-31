package guepardoapps.lucahome.common.builder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
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
    private static final String TAG = MapContentBuilder.class.getSimpleName();

    public static String GetButtonText(@NonNull MapContent.DrawingType drawingType, WirelessSocket wirelessSocket, Temperature temperature) {
        switch (drawingType) {
            case MediaServer:
            case Socket:
                return (wirelessSocket != null ? wirelessSocket.GetShortName() : "");
            case Temperature:
                return (temperature != null ? temperature.GetTemperatureString() : "T");
            case ShoppingList:
                return "S";
            case Menu:
                return "M";
            case Camera:
                return "C";
            case PuckJS:
                return "P";
            case Raspberry:
                return "R";
            case Arduino:
                return "A";
            case Null:
            default:
                return "";
        }
    }

    public static int GetDrawable(@NonNull MapContent.DrawingType drawingType, WirelessSocket wirelessSocket, Temperature temperature) {
        switch (drawingType) {
            case Socket:
                if (wirelessSocket == null) {
                    return R.drawable.drawing_socket_off;
                }

                if (wirelessSocket.IsActivated()) {
                    return R.drawable.drawing_socket_on;
                } else {
                    return R.drawable.drawing_socket_off;
                }

            case MediaServer:
                if (wirelessSocket == null) {
                    return R.drawable.drawing_mediamirror_off;
                }

                if (wirelessSocket.IsActivated()) {
                    return R.drawable.drawing_mediamirror_on;
                } else {
                    return R.drawable.drawing_mediamirror_off;
                }

            case Temperature:
                return (temperature != null ? temperature.GetDrawable() : R.drawable.drawing_temperature);
            case Raspberry:
                return R.drawable.drawing_raspberry;
            case Arduino:
                return R.drawable.drawing_arduino;
            case ShoppingList:
                return R.drawable.drawing_shoppinglist;
            case Menu:
                return R.drawable.drawing_menu;
            case Camera:
                return R.drawable.drawing_camera;
            case PuckJS:
                return R.drawable.drawing_puckjs;
            case Null:
            default:
                return R.drawable.drawing_socket_off;
        }
    }

    public static Runnable GetRunnable(@NonNull MapContent.DrawingType drawingType, WirelessSocket wirelessSocket, Temperature temperature, @NonNull Context context) {
        switch (drawingType) {
            case MediaServer:
            case Socket:
                return (wirelessSocket != null ? createSocketRunnable(context, wirelessSocket) : null);
            case Temperature:
                return (temperature != null ? createTemperatureRunnable(context, temperature) : null);
            case ShoppingList:
                return createShoppingListRunnable(context);
            case Menu:
                return createMenuRunnable(context);
            case Camera:
                return createCameraRunnable(context);
            case PuckJS:
                return null;
            case Raspberry:
                return null;
            case Arduino:
                return null;
            case Null:
            default:
                return null;
        }
    }

    private static Runnable createSocketRunnable(@NonNull final Context context, @NonNull final WirelessSocket wirelessSocket) {
        return new Runnable() {
            @Override
            public void run() {
                boolean isLightTheme = ThemeManager.getInstance().getCurrentTheme() == 0;

                final Dialog dialog = new Dialog(context);
                dialog
                        .title(String.format(Locale.getDefault(), "Change state of %s?", wirelessSocket.GetName()))
                        .positiveAction("Activate")
                        .negativeAction("Deactivate")
                        .applyStyle(isLightTheme ? R.style.SimpleDialogLight : R.style.SimpleDialog)
                        .setCancelable(true);

                dialog.positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        WirelessSocketService.getInstance().SetWirelessSocketState(wirelessSocket, true);
                        dialog.dismiss();
                    }
                });

                dialog.negativeActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        WirelessSocketService.getInstance().SetWirelessSocketState(wirelessSocket, false);
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        };
    }

    @SuppressLint("SetJavaScriptEnabled")
    private static Runnable createTemperatureRunnable(@NonNull final Context context, @NonNull final Temperature temperature) {
        return new Runnable() {
            @Override
            public void run() {
                if (temperature.GetGraphPath().length() > 0) {
                    final android.app.Dialog dialog = new android.app.Dialog(context);

                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.dialog_webview);

                    TextView titleView = dialog.findViewById(R.id.dialog_title_text_view);
                    titleView.setText(temperature.GetArea());

                    com.rey.material.widget.Button closeButton = dialog.findViewById(R.id.dialog_button_close);
                    closeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

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
                        window.setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
                    }
                } else {
                    Toasty.info(context, String.format(Locale.getDefault(), "No action for temperature in area %s", temperature.GetArea()), Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    private static Runnable createShoppingListRunnable(@NonNull final Context context) {
        return new Runnable() {
            @Override
            public void run() {
                displayListViewDialog(context, "Shopping list", ShoppingListService.getInstance().GetShoppingDetailList());
            }
        };
    }

    private static Runnable createMenuRunnable(@NonNull final Context context) {
        return new Runnable() {
            @Override
            public void run() {
                displayListViewDialog(context, "Menu", MenuService.getInstance().GetMenuNameList());
            }
        };
    }

    private static Runnable createCameraRunnable(@NonNull final Context context) {
        return new Runnable() {
            @Override
            public void run() {
                Toasty.error(context, "Method for camera needs to be implemented!", Toast.LENGTH_LONG).show();
            }
        };
    }

    private static void displayListViewDialog(@NonNull Context context, @NonNull String title, @NonNull ArrayList<String> list) {
        final android.app.Dialog dialog = new android.app.Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_listview);

        TextView titleView = dialog.findViewById(R.id.dialog_title_text_view);
        titleView.setText(title);

        com.rey.material.widget.Button closeButton = dialog.findViewById(R.id.dialog_button_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ListView listView = dialog.findViewById(R.id.dialog_list_view);
        listView.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, list));

        dialog.setCancelable(true);
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        }
    }
}
