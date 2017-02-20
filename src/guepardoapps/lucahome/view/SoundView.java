package guepardoapps.lucahome.view;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.constants.Broadcasts;
import guepardoapps.lucahome.common.constants.Bundles;
import guepardoapps.lucahome.common.constants.SharedPrefConstants;

import guepardoapps.lucahomelibrary.common.classes.SerializableList;
import guepardoapps.lucahomelibrary.common.classes.Sound;
import guepardoapps.lucahomelibrary.common.constants.Color;
import guepardoapps.lucahomelibrary.common.constants.ServerActions;
import guepardoapps.lucahomelibrary.common.controller.LucaNotificationController;
import guepardoapps.lucahomelibrary.common.controller.ServiceController;
import guepardoapps.lucahomelibrary.common.converter.json.JsonDataToSocketConverter;
import guepardoapps.lucahomelibrary.common.converter.json.JsonDataToSoundConverter;
import guepardoapps.lucahomelibrary.common.dto.WirelessSocketDto;
import guepardoapps.lucahomelibrary.common.enums.LucaObject;
import guepardoapps.lucahomelibrary.common.enums.RaspberrySelection;
import guepardoapps.lucahomelibrary.common.tools.LucaHomeLogger;
import guepardoapps.lucahomelibrary.services.helper.DialogService;
import guepardoapps.lucahomelibrary.services.helper.NavigationService;
import guepardoapps.lucahomelibrary.view.controller.SoundController;
import guepardoapps.lucahomelibrary.view.customadapter.SoundListAdapter;

import guepardoapps.toolset.controller.ReceiverController;
import guepardoapps.toolset.controller.SharedPrefController;

public class SoundView extends Activity {

	private static final String TAG = SoundView.class.getName();
	private LucaHomeLogger _logger;

	private ArrayList<ArrayList<Sound>> _soundLists = new ArrayList<ArrayList<Sound>>(2);
	private ArrayList<Sound> _activeSoundList;
	private Sound _soundPlaying;
	private boolean _isPlaying = false;;
	private int _volume = -1;
	private RaspberrySelection _raspberrySelection;

	private SerializableList<WirelessSocketDto> _wirelessSocketList;

	private ProgressBar _progressBar;
	private ListView _listView;
	private TextView _informationText;
	private TextView _volumeText;
	private ImageButton _buttonPlay;
	private ImageButton _buttonStop;
	private ImageButton _buttonIncreaseVolume;
	private ImageButton _buttonDecreaseVolume;
	private Button _buttonSelectRaspberry;

	private ListAdapter _listAdapter;

	private Context _context;

	private DialogService _dialogService;
	private LucaNotificationController _notificationController;
	private NavigationService _navigationService;
	private ReceiverController _receiverController;
	private ServiceController _serviceController;
	private SharedPrefController _sharedPrefController;
	private SoundController _soundController;

	private BroadcastReceiver _isPlayingReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_receiverController.UnregisterReceiver(_isPlayingReceiver);

			String[] isPlayingStringArray = intent.getStringArrayExtra("SoundController");
			if (isPlayingStringArray != null) {
				if (isPlayingStringArray[0] != null) {
					String isPlayingString = isPlayingStringArray[0].replace("{IsPlaying:", "").replace("};", "");
					_isPlaying = isPlayingString.contains("1");
				}
			}

			if (_isPlaying) {
				_receiverController.RegisterReceiver(_currentPlayingSoundReceiver,
						new String[] { Broadcasts.PLAYING_FILE });
				_serviceController.StartRestService(TAG, ServerActions.GET_PLAYING_FILE, Broadcasts.PLAYING_FILE,
						LucaObject.SOUND, _raspberrySelection);
			} else {
				_informationText.setText("-/-");
				startDownloadingSoundFiles();
			}
		}
	};

	private BroadcastReceiver _currentPlayingSoundReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_receiverController.UnregisterReceiver(_currentPlayingSoundReceiver);

			String[] playingFileStringArray = intent.getStringArrayExtra(TAG);
			if (playingFileStringArray != null) {
				if (playingFileStringArray[0] != null) {
					String playingFileString = playingFileStringArray[0].replace("{PlayingFile:", "").replace("};", "");
					_soundPlaying = new Sound(playingFileString, true);
					_informationText.setText(_soundPlaying.GetFileName());
				} else {
					_informationText.setText("Error loading!;");
				}
			} else {
				_informationText.setText("Error loading!;");
			}

			startDownloadingSoundFiles();
		}
	};

	private BroadcastReceiver _soundsDownloadedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_receiverController.UnregisterReceiver(_soundsDownloadedReceiver);

			String[] soundStringArray = intent.getStringArrayExtra(TAG);
			if (soundStringArray != null) {
				_soundLists.add(JsonDataToSoundConverter.GetList(soundStringArray[0]));
				_soundLists.add(JsonDataToSoundConverter.GetList(soundStringArray[1]));
				_activeSoundList = _soundLists.get(_raspberrySelection.GetInt() - 1);

				showList();

				_serviceController.StartRestService(TAG, ServerActions.GET_VOLUME, Broadcasts.GET_VOLUME,
						LucaObject.SOUND, _raspberrySelection);
			} else {
				Toasty.error(_context, "Failed to download sound list", Toast.LENGTH_LONG).show();
			}
		}
	};

	private BroadcastReceiver _raspberryChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			RaspberrySelection newRaspberrySelection = (RaspberrySelection) intent
					.getSerializableExtra(Bundles.RASPBERRY_SELECTION);
			if (newRaspberrySelection != null) {
				_raspberrySelection = newRaspberrySelection;
				_activeSoundList = _soundLists.get(_raspberrySelection.GetInt() - 1);
				_buttonSelectRaspberry.setText(String.valueOf(_raspberrySelection.GetInt()));

				_serviceController.StartRestService(TAG, ServerActions.GET_VOLUME, Broadcasts.GET_VOLUME,
						LucaObject.SOUND, _raspberrySelection);

				showList();
			}
		}
	};

	private BroadcastReceiver _startPlayingReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String[] soundFileArray = intent.getStringArrayExtra(TAG);
			if (soundFileArray == null) {
				soundFileArray = intent.getStringArrayExtra("SoundController");
				if (soundFileArray == null) {
					Toasty.error(_context, "Failed to start sound!", Toast.LENGTH_LONG).show();
					return;
				} else {
					handleSendStartIntent(soundFileArray);
				}
			} else {
				handleSendStartIntent(soundFileArray);
			}
		}

		private void handleSendStartIntent(String[] soundFileArray) {
			if (soundFileArray != null) {
				String fileName = soundFileArray[0];
				if (fileName.contains("Error")) {
					_informationText.setText(fileName);
					return;
				}

				if (fileName != "") {
					boolean foundFile = false;
					for (int index = 0; index < _activeSoundList.size(); index++) {
						if (_activeSoundList.get(index).GetFileName().contains(fileName)) {
							_activeSoundList.get(index).SetIsPlaying(true);
							_soundPlaying = _activeSoundList.get(index);
							foundFile = true;
						} else {
							_activeSoundList.get(index).SetIsPlaying(false);
						}
					}

					if (foundFile) {
						_informationText.setText(_soundPlaying.GetFileName());
					} else {
						_informationText.setText("File not found: " + fileName);
					}
				}
			} else {
				Toasty.error(_context, "Failed to start sound!", Toast.LENGTH_LONG).show();
			}
		}
	};

	private BroadcastReceiver _stopPlayingReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String[] stopStringArray = intent.getStringArrayExtra("SoundController");
			if (stopStringArray != null) {
				if (stopStringArray[0] != null) {
					if (stopStringArray[0].contains("1")) {
						_isPlaying = false;
						_soundPlaying = null;
						_informationText.setText("-/-");
					}
				}
			}

			if (_isPlaying) {
				_logger.Error(stopStringArray[0]);
				Toasty.error(_context, "Failed to stop sound!", Toast.LENGTH_LONG).show();
			}
		}
	};

	private BroadcastReceiver _volumeDownloadedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String[] volumeStringArray = intent.getStringArrayExtra("SoundController");
			if (volumeStringArray == null) {
				volumeStringArray = intent.getStringArrayExtra(TAG);
			}

			if (volumeStringArray != null) {
				if (volumeStringArray[0] != null) {
					String volumeString = volumeStringArray[0].replace("{Volume:", "").replace("};", "");
					if (!volumeString.contains("Error")) {
						try {
							_volume = Integer.parseInt(volumeString);
							_volumeText.setText(String.valueOf(_volume));
						} catch (Exception ex) {
							_logger.Error(volumeStringArray[0]);
							_logger.Error(ex.toString());
							Toasty.error(_context, "Failed to set volume!", Toast.LENGTH_SHORT).show();
						}
					} else {
						_logger.Error(volumeStringArray[0]);
						Toasty.error(_context, "Failed to set volume!", Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
	};

	private BroadcastReceiver _activateSoundSocketReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			RaspberrySelection raspberrySelection = (RaspberrySelection) intent
					.getSerializableExtra(Bundles.RASPBERRY_SELECTION);

			if (raspberrySelection != null) {
				if (_wirelessSocketList != null) {
					for (int index = 0; index < _wirelessSocketList.getSize(); index++) {
						if (_wirelessSocketList.getValue(index).GetName().contains("Sound")) {
							if (_wirelessSocketList.getValue(index).GetArea().contains(raspberrySelection.GetArea())) {
								_serviceController.StartRestService(TAG,
										_wirelessSocketList.getValue(index).GetCommandSet(true),
										Broadcasts.RELOAD_SOCKETS, LucaObject.WIRELESS_SOCKET, RaspberrySelection.BOTH);
							}
						}
					}
				}
			}
		}
	};

	private BroadcastReceiver _deactivateSoundSocketReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			RaspberrySelection raspberrySelection = (RaspberrySelection) intent
					.getSerializableExtra(Bundles.RASPBERRY_SELECTION);

			if (raspberrySelection != null) {
				if (_wirelessSocketList != null) {
					for (int index = 0; index < _wirelessSocketList.getSize(); index++) {
						if (_wirelessSocketList.getValue(index).GetName().contains("Sound")) {
							if (_wirelessSocketList.getValue(index).GetArea().contains(raspberrySelection.GetArea())) {
								_serviceController.StartRestService(TAG,
										_wirelessSocketList.getValue(index).GetCommandSet(false),
										Broadcasts.RELOAD_SOCKETS, LucaObject.WIRELESS_SOCKET, RaspberrySelection.BOTH);
							}
						}
					}
				}
			}
		}
	};

	private BroadcastReceiver _reloadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			LucaObject lucaObject = (LucaObject) intent.getSerializableExtra(Bundles.LUCA_OBJECT);
			if (lucaObject == null) {
				_logger.Error("_downloadReceiver received data with null lucaobject!");
				return;
			}

			switch (lucaObject) {
			case WIRELESS_SOCKET:
				_receiverController.RegisterReceiver(_downloadReceiver,
						new String[] { Broadcasts.DOWNLOAD_SOCKET_FINISHED });
				_serviceController.StartRestService(Bundles.SOCKET_DOWNLOAD, ServerActions.GET_SOCKETS,
						Broadcasts.DOWNLOAD_SOCKET_FINISHED, LucaObject.WIRELESS_SOCKET, RaspberrySelection.BOTH);
				break;
			default:
				_logger.Debug("Nothing to do in _reloadReceiver for: " + lucaObject.toString());
				break;
			}
		}
	};

	private BroadcastReceiver _downloadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_receiverController.UnregisterReceiver(_downloadReceiver);

			LucaObject lucaObject = (LucaObject) intent.getSerializableExtra(Bundles.LUCA_OBJECT);
			if (lucaObject == null) {
				_logger.Error("_downloadReceiver received data with null lucaobject!");
				return;
			}

			switch (lucaObject) {
			case WIRELESS_SOCKET:
				String[] socketStringArray = intent.getStringArrayExtra(Bundles.SOCKET_DOWNLOAD);
				if (socketStringArray != null) {
					_wirelessSocketList = JsonDataToSocketConverter.GetList(socketStringArray);
					_notificationController.CreateSocketNotification(_wirelessSocketList);
				}
				break;
			default:
				_logger.Error("Cannot parse object: " + lucaObject.toString());
				break;
			}
		}
	};

	private Runnable _selectRaspberry1 = new Runnable() {
		@Override
		public void run() {
			if (checkFileInList(_soundPlaying, _soundLists.get(0))) {
				_soundController.SelectRaspberry(_raspberrySelection, RaspberrySelection.RASPBERRY_1, _soundPlaying);
			} else {
				Toasty.error(_context,
						"Cannot change raspberry! Raspberry 1 has not file " + _soundPlaying.GetFileName(),
						Toast.LENGTH_LONG).show();
			}
			_dialogService.CloseDialogCallback.run();
		}
	};

	private Runnable _selectRaspberry2 = new Runnable() {
		@Override
		public void run() {
			if (checkFileInList(_soundPlaying, _soundLists.get(1))) {
				_soundController.SelectRaspberry(_raspberrySelection, RaspberrySelection.RASPBERRY_2, _soundPlaying);
			} else {
				Toasty.error(_context,
						"Cannot change raspberry! Raspberry 2 has not file " + _soundPlaying.GetFileName(),
						Toast.LENGTH_LONG).show();
			}
			_dialogService.CloseDialogCallback.run();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_context = this;

		_logger = new LucaHomeLogger(TAG);
		_logger.Debug("onCreate");

		_dialogService = new DialogService(_context);
		_navigationService = new NavigationService(_context);
		_notificationController = new LucaNotificationController(_context);
		_receiverController = new ReceiverController(_context);
		_serviceController = new ServiceController(_context);
		_sharedPrefController = new SharedPrefController(_context, SharedPrefConstants.SHARED_PREF_NAME);
		_soundController = new SoundController(_context);

		setContentView(R.layout.view_sound);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.ACTION_BAR));

		initializeViews();
		loadValues();

		_buttonSelectRaspberry.setText(String.valueOf(_raspberrySelection.GetInt()));
	}

	@Override
	public void onResume() {
		super.onResume();
		_logger.Debug("onResume");

		_receiverController.RegisterReceiver(_activateSoundSocketReceiver,
				new String[] { Broadcasts.ACTIVATE_SOUND_SOCKET });
		_receiverController.RegisterReceiver(_deactivateSoundSocketReceiver,
				new String[] { Broadcasts.DEACTIVATE_SOUND_SOCKET });
		_receiverController.RegisterReceiver(_isPlayingReceiver, new String[] { Broadcasts.IS_SOUND_PLAYING });
		_receiverController.RegisterReceiver(_raspberryChangedReceiver, new String[] { Broadcasts.SET_RASPBERRY });
		_receiverController.RegisterReceiver(_reloadReceiver, new String[] { Broadcasts.RELOAD_SOCKETS });
		_receiverController.RegisterReceiver(_startPlayingReceiver, new String[] { Broadcasts.START_SOUND });
		_receiverController.RegisterReceiver(_stopPlayingReceiver, new String[] { Broadcasts.STOP_SOUND });
		_receiverController.RegisterReceiver(_volumeDownloadedReceiver, new String[] { Broadcasts.GET_VOLUME });
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		_logger.Debug("onDestroy");

		_receiverController.UnregisterReceiver(_activateSoundSocketReceiver);
		_receiverController.UnregisterReceiver(_currentPlayingSoundReceiver);
		_receiverController.UnregisterReceiver(_deactivateSoundSocketReceiver);
		_receiverController.UnregisterReceiver(_downloadReceiver);
		_receiverController.UnregisterReceiver(_isPlayingReceiver);
		_receiverController.UnregisterReceiver(_raspberryChangedReceiver);
		_receiverController.UnregisterReceiver(_reloadReceiver);
		_receiverController.UnregisterReceiver(_soundsDownloadedReceiver);
		_receiverController.UnregisterReceiver(_startPlayingReceiver);
		_receiverController.UnregisterReceiver(_stopPlayingReceiver);
		_receiverController.UnregisterReceiver(_volumeDownloadedReceiver);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			_navigationService.NavigateTo(HomeView.class, true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void loadValues() {
		_raspberrySelection = RaspberrySelection.GetById(_sharedPrefController
				.LoadIntegerValueFromSharedPreferences(SharedPrefConstants.SOUND_RASPBERRY_SELECTION));
		_soundController.CheckPlaying(_raspberrySelection);
	}

	private void initializeViews() {
		_progressBar = (ProgressBar) findViewById(R.id.progressBarSoundListView);
		_listView = (ListView) findViewById(R.id.soundListView);

		_informationText = (TextView) findViewById(R.id.soundInformation);
		if (_soundPlaying != null) {
			_informationText.setText(_soundPlaying.GetFileName());
		} else {
			_informationText.setText("Loading...");
		}

		_volumeText = (TextView) findViewById(R.id.soundVolumeView);
		if (_volume == -1) {
			_volumeText.setText("Loading...");
		} else {
			_volumeText.setText(String.valueOf(_volume));
		}

		_buttonPlay = (ImageButton) findViewById(R.id.soundButtonPlay);
		_buttonStop = (ImageButton) findViewById(R.id.soundButtonStop);
		_buttonIncreaseVolume = (ImageButton) findViewById(R.id.soundButtonIncreaseVolume);
		_buttonDecreaseVolume = (ImageButton) findViewById(R.id.soundButtonDecreaseVolume);
		_buttonSelectRaspberry = (Button) findViewById(R.id.soundSwitchRaspberry);

		_buttonPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (_soundPlaying != null) {
					_soundController.StartSound(_soundPlaying.GetFileName(), _raspberrySelection);
				}
			}
		});
		_buttonStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				_soundController.StopSound(_raspberrySelection);
			}
		});
		_buttonIncreaseVolume.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				_soundController.IncreaseVolume(_raspberrySelection);
			}
		});
		_buttonDecreaseVolume.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				_soundController.DecreaseVolume(_raspberrySelection);
			}
		});
		_buttonSelectRaspberry.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				_dialogService.ShowDialogDouble("Select a raspberry", "", "Raspberry 1", _selectRaspberry1,
						"Raspberry 2", _selectRaspberry2, true);
			}
		});
	}

	private void showList() {
		_progressBar.setVisibility(View.GONE);
		_listAdapter = new SoundListAdapter(_context, _activeSoundList, _raspberrySelection);
		_listView.setAdapter(_listAdapter);
		_listView.setVisibility(View.VISIBLE);
	}

	private boolean checkFileInList(Sound sound, ArrayList<Sound> soundList) {
		if (sound == null) {
			return true;
		}

		for (Sound entry : soundList) {
			if (entry.GetFileName().contains(sound.GetFileName())) {
				return true;
			}
		}

		return false;
	}

	private void startDownloadingSoundFiles() {
		_receiverController.RegisterReceiver(_soundsDownloadedReceiver, new String[] { Broadcasts.GET_SOUNDS });
		_serviceController.StartRestService(TAG, ServerActions.GET_SOUNDS, Broadcasts.GET_SOUNDS, LucaObject.SOUND,
				RaspberrySelection.BOTH);
	}
}
