package guepardoapps.lucahome.common.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rey.material.app.Dialog;
import com.rey.material.app.ThemeManager;
import com.rey.material.widget.Button;

import java.util.ArrayList;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.classes.MapContent;
import guepardoapps.lucahome.common.classes.WirelessSocket;
import guepardoapps.lucahome.common.classes.WirelessSwitch;
import guepardoapps.lucahome.common.services.MealService;
import guepardoapps.lucahome.common.services.ShoppingItemService;
import guepardoapps.lucahome.common.services.SuggestedMealService;
import guepardoapps.lucahome.common.services.TemperatureService;
import guepardoapps.lucahome.common.services.WirelessSocketService;
import guepardoapps.lucahome.common.services.WirelessSwitchService;

public class MapContentHelper {
    private static String Tag = MapContentHelper.class.getSimpleName();

    private static final int DefaultMapSize = 35;
    private static final int DefaultMapPadding = 0;

    public static MapContent.DrawingType GetDrawingType(@NonNull String typeString) {
        switch (typeString) {
            case "Camera":
                return MapContent.DrawingType.Camera;
            case "LAN":
                return MapContent.DrawingType.LAN;
            case "Meal":
                return MapContent.DrawingType.Meal;
            case "MediaServer":
                return MapContent.DrawingType.MediaServer;
            case "Meter":
                return MapContent.DrawingType.Meter;
            case "NAS":
                return MapContent.DrawingType.NAS;
            case "PuckJS":
                return MapContent.DrawingType.PuckJS;
            case "RaspberryPi":
                return MapContent.DrawingType.RaspberryPi;
            case "ShoppingList":
                return MapContent.DrawingType.ShoppingList;
            case "SuggestedMeal":
                return MapContent.DrawingType.SuggestedMeal;
            case "Temperature":
                return MapContent.DrawingType.Temperature;
            case "WirelessSocket":
                return MapContent.DrawingType.WirelessSocket;
            case "WirelessSwitch":
                return MapContent.DrawingType.WirelessSwitch;
            default:
                return MapContent.DrawingType.Null;
        }
    }

    public static int GetTextColor(@NonNull MapContent mapContent) {
        switch (mapContent.GetDrawingType()) {
            case Camera:
            case Meter:
            case NAS:
            case PuckJS:
            case ShoppingList:
            case WirelessSocket:
                return Color.WHITE;

            case LAN:
            case Meal:
            case MediaServer:
            case RaspberryPi:
            case SuggestedMeal:
            case Temperature:
            case WirelessSwitch:
            case Null:
            default:
                return Color.BLACK;
        }
    }

    public static Drawable GetDrawable(@NonNull MapContent mapContent) throws Exception {
        switch (mapContent.GetDrawingType()) {
            case Camera:
                return DrawableCreator.DrawCircle(DefaultMapSize, DefaultMapSize, Color.BLACK, DefaultMapPadding);
            case LAN:
                return DrawableCreator.DrawCircle(DefaultMapSize, DefaultMapSize, 0xffa500/*orange*/, DefaultMapPadding);
            case Meal:
                return DrawableCreator.DrawCircle(DefaultMapSize, DefaultMapSize, Color.YELLOW, DefaultMapPadding);
            case MediaServer:
                return DrawableCreator.DrawCircle(DefaultMapSize, DefaultMapSize, 0x483d8b/*darkslateblue*/, DefaultMapPadding);
            case Meter:
                return DrawableCreator.DrawCircle(DefaultMapSize, DefaultMapSize, 0x00008B/*darkblue*/, DefaultMapPadding);
            case NAS:
                return DrawableCreator.DrawCircle(DefaultMapSize, DefaultMapSize, Color.DKGRAY, DefaultMapPadding);
            case PuckJS:
                return DrawableCreator.DrawCircle(DefaultMapSize, DefaultMapSize, 0x006400/*darkgreen*/, DefaultMapPadding);
            case RaspberryPi:
                return DrawableCreator.DrawCircle(DefaultMapSize, DefaultMapSize, 0xc1a604/*darkyellow*/, DefaultMapPadding);
            case ShoppingList:
                return DrawableCreator.DrawCircle(DefaultMapSize, DefaultMapSize, 0x800080/*purple*/, DefaultMapPadding);
            case SuggestedMeal:
                return DrawableCreator.DrawCircle(DefaultMapSize, DefaultMapSize, 0xeaa651/*strange orange*/, DefaultMapPadding);
            case Temperature:
                return TemperatureService.getInstance().GetByUuid(mapContent.GetDrawingTypeUuid()).GetDrawable();
            case WirelessSocket:
                int color = WirelessSocketService.getInstance().GetByUuid(mapContent.GetDrawingTypeUuid()).IsActivated() ? Color.GREEN : Color.RED;
                return DrawableCreator.DrawCircle(DefaultMapSize, DefaultMapSize, color, DefaultMapPadding);
            case WirelessSwitch:
                return DrawableCreator.DrawCircle(DefaultMapSize, DefaultMapSize, 0xADD8E6/*lightblue*/, DefaultMapPadding);
            case Null:
            default:
                return DrawableCreator.DrawCircle(DefaultMapSize, DefaultMapSize, 0x20B2AA/*lightseagreen*/, DefaultMapPadding);
        }
    }

    public static Runnable GetRunnable(@NonNull MapContent mapContent, @NonNull Context context) {
        switch (mapContent.GetDrawingType()) {
            case Camera:
            case LAN:
            case MediaServer:
            case Meter:
            case NAS:
            case PuckJS:
            case RaspberryPi:
                // TODO add Runnable
                return () -> Toasty.error(context, String.format(Locale.getDefault(), "Method for %s needs to be implemented!", mapContent.GetDrawingType()), Toast.LENGTH_LONG).show();

            case Meal:
                return () -> displayListViewDialog(context, "Meal", MealService.getInstance().GetTitleList());

            case ShoppingList:
                return () -> displayListViewDialog(context, "Shopping list", ShoppingItemService.getInstance().GetDetailList());

            case SuggestedMeal:
                return () -> displayListViewDialog(context, "Suggested meal", SuggestedMealService.getInstance().GetTitleList());

            case WirelessSocket:
                return createWirelessSocketRunnable(mapContent, context);

            case WirelessSwitch:
                return createWirelessSwitchRunnable(mapContent, context);

            case Temperature:
                // Deactivated to navigate to TemperatureActivity instead of displaying dialog
            case Null:
            default:
                return null;
        }
    }

    private static Runnable createWirelessSocketRunnable(@NonNull MapContent mapContent, @NonNull Context context) {
        return () -> {
            WirelessSocket wirelessSocket = WirelessSocketService.getInstance().GetByUuid(mapContent.GetDrawingTypeUuid());
            if (wirelessSocket == null) {
                Toasty.error(context, String.format(Locale.getDefault(), "Found no wireless socket with uuid %s", mapContent.GetDrawingTypeUuid()), Toast.LENGTH_LONG).show();
                return;
            }

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
                    Logger.getInstance().Error(Tag, exception.getMessage());
                }
                dialog.dismiss();
            });

            dialog.negativeActionClickListener(view -> {
                try {
                    WirelessSocketService.getInstance().SetWirelessSocketState(wirelessSocket, false);
                } catch (Exception exception) {
                    Logger.getInstance().Error(Tag, exception.getMessage());
                }
                dialog.dismiss();
            });

            dialog.show();
        };
    }

    private static Runnable createWirelessSwitchRunnable(@NonNull MapContent mapContent, @NonNull Context context) {
        return () -> {
            WirelessSwitch wirelessSwitch = WirelessSwitchService.getInstance().GetByUuid(mapContent.GetDrawingTypeUuid());
            if (wirelessSwitch == null) {
                Toasty.error(context, String.format(Locale.getDefault(), "Found no wireless switch with uuid %s", mapContent.GetDrawingTypeUuid()), Toast.LENGTH_LONG).show();
                return;
            }

            boolean isLightTheme = ThemeManager.getInstance().getCurrentTheme() == 0;
            final Dialog dialog = new Dialog(context);
            dialog
                    .title(String.format(Locale.getDefault(), "Toggle %s?", wirelessSwitch.GetName()))
                    .positiveAction("Yes")
                    .negativeAction("No")
                    .applyStyle(isLightTheme ? R.style.SimpleDialogLight : R.style.SimpleDialog)
                    .setCancelable(true);

            dialog.positiveActionClickListener(view -> {
                try {
                    WirelessSwitchService.getInstance().ToggleWirelessSwitch(wirelessSwitch);
                } catch (Exception exception) {
                    Logger.getInstance().Error(Tag, exception.getMessage());
                }
                dialog.dismiss();
            });

            dialog.negativeActionClickListener(view -> dialog.dismiss());

            dialog.show();
        };
    }

    private static void displayListViewDialog(@NonNull Context context, @NonNull String title, @NonNull ArrayList<String> list) {
        final android.app.Dialog dialog = new android.app.Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_listview);

        TextView titleView = dialog.findViewById(R.id.dialog_list_view_title);
        titleView.setText(title);

        Button closeButton = dialog.findViewById(R.id.dialog_list_view_close);
        closeButton.setOnClickListener(v -> dialog.dismiss());

        ListView listView = dialog.findViewById(R.id.dialog_list_view_list);
        listView.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, list));

        dialog.setCancelable(true);
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
    }
}
