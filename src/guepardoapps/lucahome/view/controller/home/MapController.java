package guepardoapps.lucahome.view.controller.home;

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

import guepardoapps.library.lucahome.common.dto.*;
import guepardoapps.library.lucahome.common.enums.MainServiceAction;
import guepardoapps.library.lucahome.common.enums.ServerAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaDialogController;
import guepardoapps.library.lucahome.controller.MediaMirrorController;

import guepardoapps.library.toastview.ToastView;

import guepardoapps.library.lucahome.controller.MapContentController;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.constants.Broadcasts;
import guepardoapps.lucahome.common.constants.Bundles;

import guepardoapps.toolset.common.classes.SerializableList;
import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.ReceiverController;

public class MapController {

	private static final String TAG = MapController.class.getSimpleName();
	private LucaHomeLogger _logger;

	private boolean _isInitialized;
	private Point _size;

	private Context _context;

	private BroadcastController _broadcastController;
	private LucaDialogController _dialogController;
	private MapContentController _mapContentController;
	private MediaMirrorController _mediaMirrorController;
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

	private BroadcastReceiver _mediaMirrorDataReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_mediaMirrorDataReceiver onReceive");
			String commandBundle = intent
					.getStringExtra(guepardoapps.library.lucahome.common.constants.Bundles.MEDIAMIRROR_COMMAND);
			if (commandBundle != null) {
				String[] data = commandBundle.split("\\&");
				if (data.length == 2) {
					String ip = data[0].replace("IP:", "");
					String command = data[1].replace("CMD:", "");
					switch (command) {
					case "PLAY":
						_mediaMirrorController.SendServerCommand(ip, ServerAction.PLAY_YOUTUBE_VIDEO.toString(), "");
						break;
					case "STOP":
						_mediaMirrorController.SendServerCommand(ip, ServerAction.STOP_YOUTUBE_VIDEO.toString(), "");
						break;
					case "VOL_INCREASE":
						_mediaMirrorController.SendServerCommand(ip, ServerAction.INCREASE_VOLUME.toString(), "");
						break;
					case "VOL_DECREASE":
						_mediaMirrorController.SendServerCommand(ip, ServerAction.DECREASE_VOLUME.toString(), "");
						break;
					default:
						_logger.Error(String.format("Cannot perform command %s", command));
						ToastView.error(_context, "Command failed!", Toast.LENGTH_LONG).show();
						break;
					}
				} else {
					_logger.Error(String.format("Invalid length %s for data!", data.length));
				}
			} else {
				_logger.Error("CommandBundle is null!");
			}
		}
	};

	public MapController(Context context) {
		_logger = new LucaHomeLogger(TAG);
		_context = context;

		_broadcastController = new BroadcastController(_context);
		_dialogController = new LucaDialogController(_context);
		_mapContentController = new MapContentController(_context);
		_mediaMirrorController = new MediaMirrorController(_context);
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
				_receiverController.RegisterReceiver(_mediaMirrorDataReceiver,
						new String[] { guepardoapps.library.lucahome.common.constants.Broadcasts.MEDIAMIRROR_COMMAND });
				_broadcastController.SendSerializableArrayBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
						new String[] { Bundles.MAIN_SERVICE_ACTION },
						new Object[] { MainServiceAction.GET_MAP_CONTENT });

				_isInitialized = true;
			}
		}
	}

	public void onPause() {
		_logger.Debug("onPause");
		_isInitialized = false;
		_receiverController.UnregisterReceiver(_mapDataReceiver);
		_receiverController.UnregisterReceiver(_mediaMirrorDataReceiver);
	}

	public void onDestroy() {
		_logger.Debug("onDestroy");
		_isInitialized = false;
		_receiverController.UnregisterReceiver(_mapDataReceiver);
		_receiverController.UnregisterReceiver(_mediaMirrorDataReceiver);
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
				wirelessSocketList, temperatureList, _size, true);
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
					ToastView.info(_context, "Here is a raspberry!", Toast.LENGTH_SHORT).show();
					// TODO show details
					break;
				case ARDUINO:
					ToastView.info(_context, "Here is an arduino!", Toast.LENGTH_SHORT).show();
					// TODO show details
					break;
				case SOCKET:
					showSocketDetailsDialog(newMapContent, wirelessSocketList, scheduleAllList, timerAllList);
					break;
				case TEMPERATURE:
					showTemperatureDetailsDialog(newMapContent, temperatureList);
					break;
				case MEDIASERVER:
					showMediaserverDetailsDialog(newMapContent, wirelessSocketList);
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
						_logger.Info(String.format("Socket name is %s", socketName));

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

						_logger.Info(String.format("Found %s schedules!", scheduleAllList.getSize()));
						for (int index = 0; index < scheduleAllList.getSize(); index++) {
							String scheduleName = scheduleAllList.getValue(index).GetSocket().GetName();
							_logger.Info(String.format("Schedule name is %s", scheduleName));
							if (scheduleName.contains(socketName)) {
								_logger.Info(
										String.format("Found schedule %s for socket %s", scheduleName, socketName));
								scheduleList.addValue(scheduleAllList.getValue(index));
							}
						}

						_logger.Info(String.format("Found %s timer!", timerAllList.getSize()));
						for (int index = 0; index < timerAllList.getSize(); index++) {
							String timerName = timerAllList.getValue(index).GetSocket().GetName();
							_logger.Info(String.format("Timer name is %s", timerName));
							if (timerAllList.getValue(index).GetSocket().GetName().contains(socketName)) {
								_logger.Info(String.format("Found timer %s for socket %s", timerName, socketName));
								timerList.addValue(timerAllList.getValue(index));
							}
						}

						_dialogController.ShowMapSocketDialog(socket, scheduleList, timerList);
					} else {
						_logger.Warn("SocketList too big!" + String.valueOf(socketList.size()));
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
						_dialogController.ShowTemperatureGraphDialog(temperatureList.getValue(index).GetGraphPath());
						break;
					}
				}
			}

			private void showMediaserverDetailsDialog(MapContentDto newMapContent,
					SerializableList<WirelessSocketDto> wirelessSocketList) {
				ArrayList<String> socketList = newMapContent.GetSockets();

				WirelessSocketDto socket = null;

				if (socketList != null) {
					if (socketList.size() == 1) {
						String socketName = socketList.get(0);
						_logger.Info(String.format("Socket name is %s", socketName));

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

						_dialogController.ShowMapMediaMirrorDialog(socket, newMapContent.GetMediaServerIp());
					} else {
						_logger.Warn("SocketList too big!" + String.valueOf(socketList.size()));
					}
				} else {
					_logger.Warn("SocketList is null!");
				}
			}
		});

		_relativeLayoutMapPaint.addView(newTextView);
	}
}
