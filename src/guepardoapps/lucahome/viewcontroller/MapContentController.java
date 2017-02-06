package guepardoapps.lucahome.viewcontroller;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Point;
import android.widget.RelativeLayout;
import android.widget.TextView;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.classes.*;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.ServiceController;
import guepardoapps.lucahome.common.enums.LucaObject;
import guepardoapps.lucahome.common.enums.RaspberrySelection;
import guepardoapps.lucahome.dto.MapContentDto;
import guepardoapps.lucahome.dto.WirelessSocketDto;

public class MapContentController {

	private static final String TAG = MapContentController.class.getName();
	private LucaHomeLogger _logger;

	private Context _context;
	private ServiceController _serviceController;

	public MapContentController(Context context) {
		_logger = new LucaHomeLogger(TAG);
		_context = context;
		_serviceController = new ServiceController(_context);
	}

	public void LoadMapContents() {
		_logger.Debug("GetMapContents");
		_serviceController.StartRestService(TAG, Constants.ACTION_GET_MAP_CONTENTS,
				Constants.BROADCAST_UPDATE_MAP_CONTENT_VIEW, LucaObject.MAP_CONTENT, RaspberrySelection.BOTH);
	}

	public void AddMapContent(MapContentDto mapContent) {
		_logger.Debug("AddMapContent");
		_serviceController.StartRestService(TAG, mapContent.GetCommandAdd(), Constants.BROADCAST_RELOAD_MAP_CONTENT,
				LucaObject.MAP_CONTENT, RaspberrySelection.BOTH);
	}

	public void UpdateMapContent(MapContentDto mapContent) {
		_logger.Debug("UpdateMapContent");
		_serviceController.StartRestService(TAG, mapContent.GetCommandUpdate(), Constants.BROADCAST_RELOAD_MAP_CONTENT,
				LucaObject.MAP_CONTENT, RaspberrySelection.BOTH);
	}

	public void DeleteMapContent(MapContentDto mapContent) {
		_logger.Debug("DeleteMapContent");
		_serviceController.StartRestService(TAG, mapContent.GetCommandDelete(), Constants.BROADCAST_RELOAD_MAP_CONTENT,
				LucaObject.MAP_CONTENT, RaspberrySelection.BOTH);
	}

	public TextView CreateEntry(final MapContentDto newMapContent, int[] clickPosition,
			SerializableList<WirelessSocketDto> wirelessSocketList, Point size, boolean rotated) {
		final TextView newTextView = new TextView(_context);

		switch (newMapContent.GetDrawingType()) {
		case RASPBERRY:
			newTextView.setBackgroundResource(R.drawable.drawing_raspberry);
			break;
		case ARDUINO:
			newTextView.setBackgroundResource(R.drawable.drawing_arduino);
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
				}
			}

			if (socket != null) {
				if (socket.GetIsActivated()) {
					newTextView.setBackgroundResource(R.drawable.drawing_socket_on);
				} else {
					newTextView.setBackgroundResource(R.drawable.drawing_socket_off);
				}
			} else {
				_logger.Warn("No socket found!");
				newTextView.setBackgroundResource(R.drawable.drawing_socket_off);
			}

			break;
		case TEMPERATURE:
			newTextView.setBackgroundResource(R.drawable.drawing_temperature);
			break;
		default:
			_logger.Warn("drawingType: " + newMapContent.toString() + " is not supported!");
			return null;
		}

		int positionX = -1;
		int positionY = -1;

		if (rotated) {
			positionX = size.x - (size.x * clickPosition[1] / 100) - 50;
			positionY = (size.y * clickPosition[0] / 100) - 50;
		} else {
			positionX = (size.x * clickPosition[0] / 100) - 15;
			positionY = (size.y * clickPosition[1] / 100) - 15;
		}

		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(positionX, positionY, 0, 0);

		newTextView.setLayoutParams(layoutParams);

		return newTextView;
	}
}