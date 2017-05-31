package guepardoapps.library.lucahome.controller;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.dto.MapContentDto;
import guepardoapps.library.lucahome.common.dto.TemperatureDto;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.common.enums.DrawingType;
import guepardoapps.library.lucahome.common.enums.LucaServerAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;

import guepardoapps.library.toolset.common.classes.SerializableList;

public class MapContentController {

    private static final String TAG = MapContentController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private Context _context;
    private ServiceController _serviceController;

    public MapContentController(@NonNull Context context) {
        _logger = new LucaHomeLogger(TAG);
        _context = context;
        _serviceController = new ServiceController(_context);
    }

    public void LoadMapContents() {
        _logger.Debug("GetMapContents");
        _serviceController.StartRestService(
                TAG,
                LucaServerAction.GET_MAP_CONTENTS.toString(),
                Broadcasts.UPDATE_MAP_CONTENT_VIEW);
    }

    public void AddMapContent(@NonNull MapContentDto mapContent) {
        _logger.Debug("AddMapContent");
        _serviceController.StartRestService(
                TAG,
                mapContent.GetCommandAdd(),
                Broadcasts.RELOAD_MAP_CONTENT);
    }

    public void UpdateMapContent(@NonNull MapContentDto mapContent) {
        _logger.Debug("UpdateMapContent");
        _serviceController.StartRestService(
                TAG,
                mapContent.GetCommandUpdate(),
                Broadcasts.RELOAD_MAP_CONTENT);
    }

    public void DeleteMapContent(@NonNull MapContentDto mapContent) {
        _logger.Debug("DeleteMapContent");
        _serviceController.StartRestService(
                TAG,
                mapContent.GetCommandDelete(),
                Broadcasts.RELOAD_MAP_CONTENT);
    }

    public TextView CreateEntry(
            @NonNull final MapContentDto newMapContent,
            int[] clickPosition,
            SerializableList<WirelessSocketDto> wirelessSocketList,
            SerializableList<TemperatureDto> temperatureList,
            @NonNull Point size,
            boolean rotated) {

        final TextView newTextView = new TextView(_context);
        newTextView.setVisibility(newMapContent.IsVisible() ? View.VISIBLE : View.GONE);

        switch (newMapContent.GetDrawingType()) {
            case ARDUINO:
                newTextView.setBackgroundResource(DrawingType.ARDUINO.GetDrawable());
                break;
            case CAMERA:
                newTextView.setBackgroundResource(DrawingType.CAMERA.GetDrawable());
                newTextView.setGravity(Gravity.CENTER);
                newTextView.setTextSize(10);
                newTextView.setText(R.string.mapCameraName);
                newTextView.setTextColor(Color.WHITE);
                break;
            case MEDIASERVER:
                ArrayList<String> mediaMirrorSocketList = newMapContent.GetSockets();
                WirelessSocketDto mediaMirrorSocket = null;

                if (mediaMirrorSocketList.size() == 1) {
                    if (wirelessSocketList != null) {
                        for (int index = 0; index < wirelessSocketList.getSize(); index++) {
                            if (wirelessSocketList.getValue(index).GetName().contains(mediaMirrorSocketList.get(0))) {
                                mediaMirrorSocket = wirelessSocketList.getValue(index);
                                break;
                            }
                        }
                    } else {
                        _logger.Warn("MediaMirrorSocketList is null!");
                    }
                } else {
                    _logger.Warn(String.format("MediaMirrorSocketList has invalid size %s", mediaMirrorSocketList.size()));
                }

                if (mediaMirrorSocket != null) {
                    newTextView.setGravity(Gravity.CENTER);
                    newTextView.setTextSize(10);
                    newTextView.setText(mediaMirrorSocket.GetShortName());
                    newTextView.setTextColor(Color.WHITE);

                    if (mediaMirrorSocket.IsActivated()) {
                        newTextView.setBackgroundResource(R.drawable.drawing_mediamirror_on);
                    } else {
                        newTextView.setBackgroundResource(R.drawable.drawing_mediamirror_off);
                    }
                } else {
                    _logger.Warn("No mediaMirrorSocket found!");
                    newTextView.setBackgroundResource(R.drawable.drawing_mediamirror_off);
                }

                break;
            case MENU:
                newTextView.setBackgroundResource(DrawingType.MENU.GetDrawable());
                newTextView.setGravity(Gravity.CENTER);
                newTextView.setTextSize(10);
                newTextView.setText("M");
                newTextView.setTextColor(Color.WHITE);
                break;
            case RASPBERRY:
                newTextView.setBackgroundResource(DrawingType.RASPBERRY.GetDrawable());
                break;
            case SHOPPING_LIST:
                newTextView.setBackgroundResource(DrawingType.SHOPPING_LIST.GetDrawable());
                newTextView.setGravity(Gravity.CENTER);
                newTextView.setTextSize(10);
                newTextView.setText("S");
                newTextView.setTextColor(Color.WHITE);
                break;
            case SOCKET:
                ArrayList<String> socketList = newMapContent.GetSockets();
                WirelessSocketDto socket = null;

                if (socketList.size() == 1) {
                    if (wirelessSocketList != null) {
                        for (int index = 0; index < wirelessSocketList.getSize(); index++) {
                            if (wirelessSocketList.getValue(index).GetName().contains(socketList.get(0))) {
                                socket = wirelessSocketList.getValue(index);
                                break;
                            }
                        }
                    } else {
                        _logger.Warn("SocketList is null!");
                    }
                } else {
                    _logger.Warn(String.format("SocketList has invalid size %s", socketList.size()));
                }

                if (socket != null) {
                    if (socket.IsActivated()) {
                        newTextView.setBackgroundResource(R.drawable.drawing_socket_on);
                    } else {
                        newTextView.setBackgroundResource(R.drawable.drawing_socket_off);
                    }

                    newTextView.setGravity(Gravity.CENTER);
                    newTextView.setTextSize(10);
                    newTextView.setText(socket.GetShortName());
                    newTextView.setTextColor(Color.WHITE);
                } else {
                    _logger.Warn("No socket found!");
                    newTextView.setBackgroundResource(R.drawable.drawing_socket_off);
                }

                break;
            case TEMPERATURE:
                String temperatureArea = newMapContent.GetTemperatureArea();
                TemperatureDto temperature = null;

                if (temperatureArea.length() > 0) {
                    if (temperatureList != null) {
                        for (int index = 0; index < temperatureList.getSize(); index++) {
                            if (temperatureList.getValue(index).GetArea().contains(temperatureArea)) {
                                temperature = temperatureList.getValue(index);
                                break;
                            }
                        }
                    } else {
                        _logger.Warn("TemperatureList is null!");
                    }
                } else {
                    _logger.Warn(String.format("TemperatureArea has invalid length %s", temperatureArea.length()));
                }

                if (temperature != null) {
                    String temperatureValue = String.valueOf(temperature.GetTemperatureValue());
                    int endIndex = temperatureValue.indexOf(".");
                    temperatureValue = temperatureValue.substring(0, endIndex);
                    temperatureValue += "°C";

                    newTextView.setGravity(Gravity.CENTER);
                    newTextView.setTextSize(10);
                    newTextView.setText(temperatureValue);
                    newTextView.setTextColor(Color.WHITE);
                } else {
                    _logger.Warn("No temperature found!");
                }

                newTextView.setBackgroundResource(DrawingType.TEMPERATURE.GetDrawable());
                break;
            default:
                _logger.Warn("drawingType: " + newMapContent.toString() + " is not supported!");
                return null;
        }

        int positionX;
        int positionY;

        if (rotated) {
            positionX = size.x - (size.x * clickPosition[1] / 100) - 50;
            positionY = (size.y * clickPosition[0] / 100) - 50;
        } else {
            positionX = (size.x * clickPosition[0] / 100) - 15;
            positionY = (size.y * clickPosition[1] / 100) - 15;
        }

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(positionX, positionY, 0, 0);

        newTextView.setLayoutParams(layoutParams);

        return newTextView;
    }
}