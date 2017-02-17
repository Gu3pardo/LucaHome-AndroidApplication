package guepardoapps.lucahome.view.controller;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.classes.SerializableList;
import guepardoapps.lucahome.common.constants.Broadcasts;
import guepardoapps.lucahome.common.constants.Bundles;
import guepardoapps.lucahome.common.dto.MapContentDto;
import guepardoapps.lucahome.common.dto.ScheduleDto;
import guepardoapps.lucahome.common.dto.TimerDto;
import guepardoapps.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.lucahome.common.dto.sensor.TemperatureDto;
import guepardoapps.lucahome.common.enums.MainServiceAction;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.lucahome.services.helper.DialogService;

import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.ReceiverController;

public class HomeViewMapController {

	private static final String TAG = HomeViewMapController.class.getName();
	private LucaHomeLogger _logger;

	private boolean _isInitialized;
	private Point _size;

	private Context _context;

	private BroadcastController _broadcastController;
	private DialogService _dialogService;
	private MapContentController _mapContentController;
	private ReceiverController _receiverController;

	@SuppressWarnings("unused")
	private ImageView _imageViewMapOverview;
	private RelativeLayout _relativeLayoutMapPaint;

	private BroadcastReceiver _mapDataReceiver = new BroadcastReceiver() {
		@SuppressWarnings("unchecked")
		@Override
		public void onReceive(Context context, Intent intent) {
			SerializableList<MapContentDto> mapContentList = (SerializableList<MapContentDto>) intent
					.getSerializableExtra(Bundles.MAP_CONTENT_LIST);
			SerializableList<WirelessSocketDto> wirelessSocketList = (SerializableList<WirelessSocketDto>) intent
					.getSerializableExtra(Bundles.SOCKET_LIST);
			SerializableList<ScheduleDto> scheduleAllList = (SerializableList<ScheduleDto>) intent
					.getSerializableExtra(Bundles.SCHEDULE_LIST);
			SerializableList<TimerDto> timerAllList = (SerializableList<TimerDto>) intent
					.getSerializableExtra(Bundles.TIMER_LIST);
			SerializableList<TemperatureDto> temperatureList = (SerializableList<TemperatureDto>) intent
					.getSerializableExtra(Bundles.TEMPERATURE_LIST);

			if (mapContentList != null && wirelessSocketList != null && scheduleAllList != null && timerAllList != null
					&& temperatureList != null) {
				for (int index = 0; index < mapContentList.getSize(); index++) {
					MapContentDto entry = mapContentList.getValue(index);
					addView(entry, wirelessSocketList, scheduleAllList, timerAllList, temperatureList);
				}
			} else {
				if (mapContentList == null) {
					_logger.Warn("mapContentList is null!");
				}
				if (wirelessSocketList == null) {
					_logger.Warn("wirelessSocketList is null!");
				}
				if (scheduleAllList == null) {
					_logger.Warn("scheduleAllList is null!");
				}
				if (timerAllList == null) {
					_logger.Warn("timerAllList is null!");
				}
				if (temperatureList == null) {
					_logger.Warn("temperatureList is null!");
				}
			}
		}
	};

	public HomeViewMapController(Context context) {
		_logger = new LucaHomeLogger(TAG);
		_context = context;

		_broadcastController = new BroadcastController(_context);
		_dialogService = new DialogService(_context);
		_mapContentController = new MapContentController(_context);
		_receiverController = new ReceiverController(_context);
	}

	public void onCreate() {
		_logger.Debug("onCreate");

		initializeView();
		getSizeOfMapLayout();
	}

	public void onResume() {
		_logger.Debug("onResume");
		if (!_isInitialized) {
			if (_receiverController != null) {
				_receiverController.RegisterReceiver(_mapDataReceiver,
						new String[] { Broadcasts.UPDATE_MAP_CONTENT_VIEW });
				_broadcastController.SendSerializableArrayBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
						new String[] { Bundles.MAIN_SERVICE_ACTION },
						new Object[] { MainServiceAction.GET_MAP_CONTENT });

				_isInitialized = true;
			}
		}
	}

	public void onPause() {
		_logger.Debug("onPause");
	}

	public void onDestroy() {
		_logger.Debug("onDestroy");
		_receiverController.UnregisterReceiver(_mapDataReceiver);
	}

	private void initializeView() {
		_logger.Debug("initializeView");

		_imageViewMapOverview = (ImageView) ((Activity) _context).findViewById(R.id.imageViewMapOverview);
		_relativeLayoutMapPaint = (RelativeLayout) ((Activity) _context).findViewById(R.id.relativeLayoutMapPaint);
	}

	private void getSizeOfMapLayout() {
		ViewTreeObserver observer = _relativeLayoutMapPaint.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				init();
				_relativeLayoutMapPaint.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}

			private void init() {
				_size = new Point(_relativeLayoutMapPaint.getWidth(), _relativeLayoutMapPaint.getHeight());
				_logger.Debug("Size is: " + _size.toString());
			}
		});
	}

	private void addView(final MapContentDto newMapContent,
			final SerializableList<WirelessSocketDto> wirelessSocketList,
			final SerializableList<ScheduleDto> scheduleAllList, final SerializableList<TimerDto> timerAllList,
			final SerializableList<TemperatureDto> temperatureList) {
		final TextView newTextView = _mapContentController.CreateEntry(newMapContent, newMapContent.GetPosition(),
				wirelessSocketList, _size, true);
		newTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showInformation(wirelessSocketList, scheduleAllList, timerAllList, temperatureList);
			}

			private void showInformation(SerializableList<WirelessSocketDto> wirelessSocketList,
					SerializableList<ScheduleDto> scheduleAllList, SerializableList<TimerDto> timerAllList,
					SerializableList<TemperatureDto> temperatureList) {
				_logger.Debug("onClick showInformation");

				switch (newMapContent.GetDrawingType()) {
				case RASPBERRY:
					Toasty.info(_context, "Here is a raspberry!", Toast.LENGTH_SHORT).show();
					// TODO show details
					break;
				case ARDUINO:
					Toasty.info(_context, "Here is an arduino!", Toast.LENGTH_SHORT).show();
					// TODO show details
					break;
				case SOCKET:
					showSocketDetailsDialog(newMapContent, wirelessSocketList, scheduleAllList, timerAllList);
					break;
				case TEMPERATURE:
					showTemperatureDetailsDialog(newMapContent, temperatureList);
					break;
				default:
					_logger.Warn("drawingType: " + newMapContent.toString() + " is not supported!");
					return;
				}
			}

			private void showSocketDetailsDialog(MapContentDto newMapContent,
					SerializableList<WirelessSocketDto> wirelessSocketList,
					SerializableList<ScheduleDto> scheduleAllList, SerializableList<TimerDto> timerAllList) {
				ArrayList<String> socketList = newMapContent.GetSockets();

				WirelessSocketDto socket = null;
				SerializableList<ScheduleDto> scheduleList = new SerializableList<ScheduleDto>();
				SerializableList<TimerDto> timerList = new SerializableList<TimerDto>();

				if (socketList != null) {
					if (socketList.size() == 1) {
						String socketName = socketList.get(0);

						for (int index = 0; index < wirelessSocketList.getSize(); index++) {
							if (wirelessSocketList.getValue(index).GetName().contains(socketName)) {
								socket = wirelessSocketList.getValue(index);
								break;
							}
						}

						if (socket == null) {
							_logger.Warn("Socket not found! " + socketName);
							return;
						}

						for (int index = 0; index < scheduleAllList.getSize(); index++) {
							if (scheduleAllList.getValue(index).GetSocket().GetName().contains(socketName)) {
								scheduleList.addValue(scheduleAllList.getValue(index));
							}
						}

						for (int index = 0; index < timerAllList.getSize(); index++) {
							if (timerAllList.getValue(index).GetSocket().GetName().contains(socketName)) {
								timerList.addValue(timerAllList.getValue(index));
							}
						}

						_dialogService.ShowMapSocketDialog(socket, scheduleList, timerList);
					} else {
						_logger.Warn("SocketList to big!" + String.valueOf(socketList.size()));
					}
				} else {
					_logger.Warn("SocketList is null!");
				}
			}

			private void showTemperatureDetailsDialog(MapContentDto newMapContent,
					SerializableList<TemperatureDto> temperatureList) {
				String temperatureArea = newMapContent.GetTemperatureArea();
				for (int index = 0; index < temperatureList.getSize(); index++) {
					if (temperatureList.getValue(index).GetArea().contains(temperatureArea)) {
						_dialogService.ShowTemperatureGraphDialog(temperatureList.getValue(index).GetGraphPath());
						break;
					}
				}
			}
		});

		_relativeLayoutMapPaint.addView(newTextView);
	}
}
