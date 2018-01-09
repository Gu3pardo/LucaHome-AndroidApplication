package guepardoapps.lucahome.common.builder;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rey.material.app.Dialog;
import com.rey.material.app.ThemeManager;

import java.util.ArrayList;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.classes.MapContent;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.service.ListedMenuService;
import guepardoapps.lucahome.common.service.MenuService;
import guepardoapps.lucahome.common.service.ShoppingListService;
import guepardoapps.lucahome.common.service.WirelessSocketService;

public class MapContentBuilder {
    private static String TAG = MapContentBuilder.class.getSimpleName();

    public static MapContent.DrawingType GetDrawingType(@NonNull String typeString) {
        switch (typeString) {
            case "WirelessSocket":
                return MapContent.DrawingType.Socket;
            case "LAN":
                return MapContent.DrawingType.LAN;
            case "MediaServer":
                return MapContent.DrawingType.MediaServer;
            case "RaspberryPi":
                return MapContent.DrawingType.RaspberryPi;
            case "NAS":
                return MapContent.DrawingType.NAS;
            case "LightSwitch":
                return MapContent.DrawingType.LightSwitch;
            case "Temperature":
                return MapContent.DrawingType.Temperature;
            case "PuckJS":
                return MapContent.DrawingType.PuckJS;
            case "Menu":
                return MapContent.DrawingType.Menu;
            case "ShoppingList":
                return MapContent.DrawingType.ShoppingList;
            case "Camera":
                return MapContent.DrawingType.Camera;
            case "Meter":
                return MapContent.DrawingType.Meter;
            default:
                return MapContent.DrawingType.Null;
        }
    }

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
                return R.drawable.drawing_lan;

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
                return R.drawable.drawing_raspberry;

            case NAS:
                return R.drawable.drawing_nas;

            case LightSwitch:
                return R.drawable.drawing_lightswitch;

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

            case Meter:
                return R.drawable.drawing_meter;

            case Null:
            default:
                return R.drawable.drawing_socket_off;
        }
    }

    public static int GetTextColor(@NonNull MapContent mapContent) {
        switch (mapContent.GetDrawingType()) {
            case Socket:
                return Color.WHITE;

            case LAN:
                return Color.BLACK;

            case MediaServer:
                return Color.BLACK;

            case RaspberryPi:
                return Color.BLACK;

            case NAS:
                return Color.WHITE;

            case LightSwitch:
                return Color.BLACK;

            case Temperature:
                return Color.BLACK;

            case PuckJS:
                return Color.WHITE;

            case Menu:
                return Color.BLACK;

            case ShoppingList:
                return Color.WHITE;

            case Camera:
                return Color.WHITE;

            case Meter:
                return Color.WHITE;

            case Null:
            default:
                return Color.BLACK;
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
                return null;
                // Deactivated to navigate to TemperatureActivity instead of displaying dialog
                // return (mapContent.GetTemperature() != null ? createTemperatureRunnable(context, mapContent.GetTemperature()) : null);

            case PuckJS:
                // TODO add Runnable for PuckJS
                return null;

            case Menu:
                return createMenuRunnable(context, mapContent);

            case ShoppingList:
                return createShoppingListRunnable(context);

            case Camera:
                return createCameraRunnable(context);

            case Meter:
                // TODO add Runnable for Meter
                return null;

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
                    Logger.getInstance().Error(TAG, exception.getMessage());
                }
                dialog.dismiss();
            });

            dialog.negativeActionClickListener(view -> {
                try {
                    WirelessSocketService.getInstance().SetWirelessSocketState(wirelessSocket, false);
                } catch (Exception exception) {
                    Logger.getInstance().Error(TAG, exception.getMessage());
                }
                dialog.dismiss();
            });

            dialog.show();
        };
    }

    /*@SuppressLint("SetJavaScriptEnabled")
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
    }*/

    private static Runnable createShoppingListRunnable(@NonNull final Context context) {
        return () -> displayListViewDialog(context, "Shopping list", ShoppingListService.getInstance().GetShoppingDetailList());
    }

    private static Runnable createMenuRunnable(@NonNull final Context context, @NonNull final MapContent mapContent) {
        if (mapContent.GetListedMenuList() != null) {
            return () -> displayListViewDialog(context, "ListedMenu", ListedMenuService.getInstance().GetListedMenuNameList());
        } else if (mapContent.GetMenuList() != null) {
            return () -> displayListViewDialog(context, "Menu", MenuService.getInstance().GetMenuTitleList());
        } else {
            Logger.getInstance().Error(TAG, "Error in createMenuRunnable! ListedMenu and Menu are null!");
            return null;
        }
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
