package guepardoapps.lucahome.view;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.classes.*;
import guepardoapps.lucahome.common.constants.Broadcasts;
import guepardoapps.lucahome.common.constants.Bundles;
import guepardoapps.lucahome.common.constants.Color;
import guepardoapps.lucahome.common.dto.*;
import guepardoapps.lucahome.common.dto.sensor.TemperatureDto;
import guepardoapps.lucahome.common.enums.*;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.lucahome.services.helper.DialogService;
import guepardoapps.lucahome.services.helper.NavigationService;
import guepardoapps.lucahome.view.controller.MapContentController;

import guepardoapps.toolset.controller.BroadcastController;
import guepardoapps.toolset.controller.ReceiverController;

public class MapView extends Activity {

	private static final String TAG = MapView.class.getName();
	private LucaHomeLogger _logger;

	private Button _buttonAddRaspberry;
	private Button _buttonAddArduino;
	private Button _buttonAddSocket;
	private Button _buttonRemove;

	private RelativeLayout _mapPaintView;
	private ImageView _mapImageView;

	private boolean _addRaspberry;
	private boolean _addArduino;
	private boolean _addSocket;
	private boolean _remove;
	private boolean _isInitialized;

	private Context _context;

	private BroadcastController _broadcastController;
	private DialogService _dialogService;
	private MapContentController _mapContentController;
	private NavigationService _navigationService;
	private ReceiverController _receiverController;

	private BroadcastReceiver _newDataReceiver = new BroadcastReceiver() {
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

	private OnTouchListener _mapOnTouchListener = new View.OnTouchListener() {
		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			_logger.Debug("_mapOnTouchListener onTouch");
			_logger.Debug("view: " + view.toString());
			_logger.Debug("motionEvent: " + motionEvent.toString());

			if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
				Point clickPosition = new Point();
				clickPosition.set((int) motionEvent.getX(), (int) motionEvent.getY());

				int[] position = new int[2];
				position[0] = clickPosition.x;
				position[1] = clickPosition.y;

				if (_addRaspberry) {
					_logger.Debug("_addRaspberry");
					DrawingType drawingType = DrawingType.RASPBERRY;

					MapContentDto newMapContent = new MapContentDto(-1, position, drawingType, null, null, null);
					_logger.Debug("newMapContent: " + newMapContent.toString());

					// TODO create add raspberry dialog
					// addView(newMapContent, clickPosition, true);
					_logger.Warn("Reactivate addView while adding new raspberry via dialog!");
					Toasty.info(_context, "Reactivate addView while adding new raspberry via dialog!",
							Toast.LENGTH_SHORT).show();

					_addRaspberry = false;
					_buttonAddRaspberry.setBackgroundResource(R.drawable.add_round);

					return true;
				} else if (_addArduino) {
					_logger.Debug("_addArduino");
					DrawingType drawingType = DrawingType.ARDUINO;

					MapContentDto newMapContent = new MapContentDto(-1, position, drawingType, null, null, null);
					_logger.Debug("newMapContent: " + newMapContent.toString());

					// TODO create add arduino dialog
					// addView(newMapContent, clickPosition, true);
					_logger.Warn("Reactivate addView while adding new arduino via dialog!");
					Toasty.info(_context, "Reactivate addView while adding new arduino via dialog!", Toast.LENGTH_SHORT)
							.show();

					_addArduino = false;
					_buttonAddArduino.setBackgroundResource(R.drawable.add_round);

					return true;
				} else if (_addSocket) {
					_logger.Debug("_addSocket");
					DrawingType drawingType = DrawingType.SOCKET;

					MapContentDto newMapContent = new MapContentDto(-1, position, drawingType, null, null, null);
					_logger.Debug("newMapContent: " + newMapContent.toString());

					// TODO create add socket dialog
					// addView(newMapContent, clickPosition, true);
					_logger.Warn("Reactivate addView while adding new socket via dialog!");
					Toasty.info(_context, "Reactivate addView while adding new socket via dialog!", Toast.LENGTH_SHORT)
							.show();

					_addSocket = false;
					_buttonAddSocket.setBackgroundResource(R.drawable.add_round);

					return true;
				} else if (_remove) {
					_logger.Debug("_remove");

					return true;
				}
			}

			return false;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_flat_map);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.ACTION_BAR));

		_logger = new LucaHomeLogger(TAG);
		_logger.Debug("onCreate");

		_context = this;

		_broadcastController = new BroadcastController(_context);
		_dialogService = new DialogService(_context);
		_mapContentController = new MapContentController(_context);
		_navigationService = new NavigationService(_context);
		_receiverController = new ReceiverController(_context);

		_addRaspberry = false;
		_addArduino = false;
		_addSocket = false;
		_remove = false;

		_mapPaintView = (RelativeLayout) findViewById(R.id.mapPaintView);
		_mapPaintView.setOnTouchListener(_mapOnTouchListener);

		_mapImageView = (ImageView) findViewById(R.id.mapImageView);
		_mapImageView.setOnTouchListener(_mapOnTouchListener);

		initializeButtons();
	}

	@Override
	public void onResume() {
		super.onResume();
		_logger.Debug("onResume");
		if (!_isInitialized) {
			if (_receiverController != null) {
				_receiverController.RegisterReceiver(_newDataReceiver,
						new String[] { Broadcasts.UPDATE_MAP_CONTENT_VIEW });
				_broadcastController.SendSerializableArrayBroadcast(Broadcasts.MAIN_SERVICE_COMMAND,
						new String[] { Bundles.MAIN_SERVICE_ACTION },
						new Object[] { MainServiceAction.GET_MAP_CONTENT });

				_isInitialized = true;
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		_logger.Debug("onDestroy");
		_receiverController.UnregisterReceiver(_newDataReceiver);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			_navigationService.NavigateTo(HomeView.class, true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initializeButtons() {
		_buttonAddRaspberry = (Button) findViewById(R.id.buttonAddRaspberry);
		_buttonAddRaspberry.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (_addRaspberry) {
					_buttonAddRaspberry.setBackgroundResource(R.drawable.add_round);
				} else {
					if (_remove) {
						_remove = false;
						_buttonRemove.setBackgroundResource(R.drawable.remove_round);
					}
					if (_addArduino) {
						_addArduino = false;
						_buttonAddArduino.setBackgroundResource(R.drawable.add_round);
					}
					if (_addSocket) {
						_addSocket = false;
						_buttonAddSocket.setBackgroundResource(R.drawable.add_round);
					}
					_buttonAddRaspberry.setBackgroundResource(R.drawable.yellow_round);
				}
				_addRaspberry = !_addRaspberry;
				_logger.Debug("_addRaspberry: " + String.valueOf(_addRaspberry));
			}
		});

		_buttonAddArduino = (Button) findViewById(R.id.buttonAddArduino);
		_buttonAddArduino.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (_addArduino) {
					_buttonAddArduino.setBackgroundResource(R.drawable.add_round);
				} else {
					if (_remove) {
						_remove = false;
						_buttonRemove.setBackgroundResource(R.drawable.remove_round);
					}
					if (_addRaspberry) {
						_addRaspberry = false;
						_buttonAddRaspberry.setBackgroundResource(R.drawable.add_round);
					}
					if (_addSocket) {
						_addSocket = false;
						_buttonAddSocket.setBackgroundResource(R.drawable.add_round);
					}
					_buttonAddArduino.setBackgroundResource(R.drawable.yellow_round);
				}
				_addArduino = !_addArduino;
				_logger.Debug("_addArduino: " + String.valueOf(_addArduino));
			}
		});

		_buttonAddSocket = (Button) findViewById(R.id.buttonAddSocket);
		_buttonAddSocket.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (_addSocket) {
					_buttonAddSocket.setBackgroundResource(R.drawable.add_round);
				} else {
					if (_remove) {
						_remove = false;
						_buttonRemove.setBackgroundResource(R.drawable.remove_round);
					}
					if (_addRaspberry) {
						_addRaspberry = false;
						_buttonAddRaspberry.setBackgroundResource(R.drawable.add_round);
					}
					if (_addArduino) {
						_addArduino = false;
						_buttonAddArduino.setBackgroundResource(R.drawable.add_round);
					}
					_buttonAddSocket.setBackgroundResource(R.drawable.yellow_round);
				}
				_addSocket = !_addSocket;
				_logger.Debug("_addSocket: " + String.valueOf(_addSocket));
			}
		});

		_buttonRemove = (Button) findViewById(R.id.buttonRemove);
		_buttonRemove.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (_remove) {
					_buttonRemove.setBackgroundResource(R.drawable.remove_round);
				} else {
					if (_addSocket) {
						_addSocket = false;
						_buttonAddSocket.setBackgroundResource(R.drawable.add_round);
					}
					if (_addRaspberry) {
						_addRaspberry = false;
						_buttonAddRaspberry.setBackgroundResource(R.drawable.add_round);
					}
					if (_addArduino) {
						_addArduino = false;
						_buttonAddArduino.setBackgroundResource(R.drawable.add_round);
					}
					_buttonRemove.setBackgroundResource(R.drawable.yellow_round);
				}
				_remove = !_remove;
				_logger.Debug("_remove: " + String.valueOf(_remove));
			}
		});
	}

	private void addView(final MapContentDto newMapContent,
			final SerializableList<WirelessSocketDto> wirelessSocketList,
			final SerializableList<ScheduleDto> scheduleAllList, final SerializableList<TimerDto> timerAllList,
			final SerializableList<TemperatureDto> temperatureList) {

		DisplayMetrics displayMetrics = _context.getResources().getDisplayMetrics();
		Point size = new Point();
		size.x = displayMetrics.widthPixels;
		size.y = displayMetrics.heightPixels;

		final TextView newTextView = _mapContentController.CreateEntry(newMapContent, newMapContent.GetPosition(),
				wirelessSocketList, size, false);
		newTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (_remove) {
					delete();
				} else {
					showInformation(wirelessSocketList, scheduleAllList, timerAllList, temperatureList);
				}
			}

			private void delete() {
				_logger.Debug("onClick _remove");
				Runnable deleteRunnable = new Runnable() {
					@Override
					public void run() {
						// _mapContentController.DeleteMapContent(newMapContent);
						// _mapPaintView.removeView(newTextView);

						// TODO implement delete socket

						_logger.Warn("Reactivate delete!");
						Toasty.info(_context, "Reactivate delete!", Toast.LENGTH_SHORT).show();

						_dialogService.CloseDialogCallback.run();
					}
				};
				_dialogService.ShowDialogDouble("Delete this drawing?", "", "Yes", deleteRunnable, "No",
						_dialogService.CloseDialogCallback, true);
			}

			private void showInformation(SerializableList<WirelessSocketDto> wirelessSocketList,
					SerializableList<ScheduleDto> scheduleAllList, SerializableList<TimerDto> timerAllList,
					SerializableList<TemperatureDto> temperatureList) {
				_logger.Debug("onClick !_remove");

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

		_mapPaintView.addView(newTextView);
	}
}
