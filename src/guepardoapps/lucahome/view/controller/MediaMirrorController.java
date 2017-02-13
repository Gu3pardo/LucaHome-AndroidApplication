package guepardoapps.lucahome.view.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import guepardoapps.lucahome.common.constants.Broadcasts;
import guepardoapps.lucahome.common.constants.Bundles;
import guepardoapps.lucahome.common.constants.Keys;
import guepardoapps.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.lucahome.services.helper.DialogService;

import guepardoapps.mediamirror.client.ClientTask;
import guepardoapps.mediamirror.common.dto.YoutubeVideoDto;
import guepardoapps.mediamirror.common.enums.ServerAction;

import guepardoapps.toolset.controller.ReceiverController;

public class MediaMirrorController {

	public static final List<String> SERVER_IPS = Arrays.asList("192.168.178.147", "192.168.178.20");
	public static final List<String> PLAYER = Arrays.asList("1", "2");
	public static final int SLEEP_MEDIA_SERVER_INDEX = 1;
	public static final int SERVERPORT = 8080;

	private static final String TAG = MediaMirrorController.class.getName();
	private LucaHomeLogger _logger;

	private static final String UP = "UP";
	private static final String DOWN = "DOWN";
	private static final String RIGHT = "RIGHT";
	private static final String LEFT = "LEFT";
	private static final String ROTATE = "ROTATE";

	private Context _context;

	private DialogService _dialogService;
	private ReceiverController _receiverController;

	private ProgressDialog _loadingVideosDialog;

	private ClientTask _clientTask;

	private String _selectedServerIp = "192.168.178.147";

	private boolean _pongIsRunning;
	private String _selectedPongPlayer = "1";

	private BroadcastReceiver _youtubeIdReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			_logger.Debug("_youtubeIdReceiver onReceive");
			String youtubeId = intent.getStringExtra(Bundles.YOUTUBE_ID);
			if (youtubeId != null) {
				SendYoutubeId(youtubeId);
				_dialogService.CloseDialogCallback.run();
			}
		}
	};

	public MediaMirrorController(Context context) {
		_logger = new LucaHomeLogger(TAG);

		_context = context;

		_dialogService = new DialogService(_context);
		_receiverController = new ReceiverController(_context);
		_receiverController.RegisterReceiver(_youtubeIdReceiver, new String[] { Broadcasts.YOUTUBE_ID });
	}

	public void Dispose() {
		_logger.Debug("Dispose");
		_receiverController.UnregisterReceiver(_youtubeIdReceiver);
	}

	public void SelectServer(String serverIp) {
		_logger.Debug("SelectServer");
		if (serverIp != null) {
			_logger.Debug("serverIp: " + serverIp);
			_selectedServerIp = serverIp;
		}
	}

	public void SendCenterText(String data) {
		_logger.Debug("SendCenterText");
		if (data.length() < 5) {
			_logger.Warn("Data is too short!");
			Toast.makeText(_context, "Data is too short!", Toast.LENGTH_SHORT).show();
			return;
		}
		sendServerCommand(ServerAction.SHOW_CENTER_TEXT.toString(), data);
	}

	public void SendPlayYoutube() {
		_logger.Debug("SendPlayYoutube");
		sendServerCommand(ServerAction.PLAY_YOUTUBE_VIDEO.toString(), "");
	}

	public void SendStopYoutube() {
		_logger.Debug("SendStopYoutube");
		sendServerCommand(ServerAction.STOP_YOUTUBE_VIDEO.toString(), "");
	}

	public void SendVolumeIncrease() {
		_logger.Debug("SendVolumeIncrease");
		sendServerCommand(ServerAction.INCREASE_VOLUME.toString(), "");
	}

	public void SendVolumeDecrease() {
		_logger.Debug("SendVolumeDecrease");
		sendServerCommand(ServerAction.DECREASE_VOLUME.toString(), "");
	}

	public void SendVolumeMute() {
		_logger.Debug("SendVolumeMute");
		sendServerCommand(ServerAction.MUTE_VOLUME.toString(), "");
	}

	public void SendVolumeUnmute() {
		_logger.Debug("SendVolumeUnmute");
		sendServerCommand(ServerAction.UNMUTE_VOLUME.toString(), "");
	}

	public void GetCurrentVolume() {
		_logger.Debug("GetCurrentVolume");
		sendServerCommand(ServerAction.GET_CURRENT_VOLUME.toString(), "");
	}

	public void GetPlayedYoutubeVideos() {
		_logger.Debug("GetPlayedYoutubeVideos");
		sendServerCommand(ServerAction.GET_SAVED_YOUTUBE_IDS.toString(), "");
	}

	public void SendIncreaseScreenBrightness() {
		_logger.Debug("SendIncreaseScreenBrightness");
		sendServerCommand(ServerAction.INCREASE_SCREEN_BRIGHTNESS.toString(), "");
	}

	public void SendDecreaseScreenBrightness() {
		_logger.Debug("SendDecreaseScreenBrightness");
		sendServerCommand(ServerAction.DECREASE_SCREEN_BRIGHTNESS.toString(), "");
	}

	public void SendYoutubeId(String id) {
		_logger.Debug("SendYoutubeId");
		if (id.length() < 0 || id.length() > 3 && id.length() < 11 || id.length() > 11) {
			_logger.Warn("ID is invalid! Length: " + String.valueOf(id.length()));
			Toast.makeText(_context, "ID is invalid! Length: " + String.valueOf(id.length()), Toast.LENGTH_SHORT)
					.show();
			return;
		}
		sendServerCommand(ServerAction.SHOW_YOUTUBE_VIDEO.toString(), String.valueOf(id));
	}

	public void SendRssFeedId(int id) {
		_logger.Debug("SendRssFeedId");
		if (id < 0) {
			_logger.Warn("Id is invalid!");
			Toast.makeText(_context, "ID is invalid!", Toast.LENGTH_SHORT).show();
			return;
		}
		sendServerCommand(ServerAction.SET_RSS_FEED.toString(), String.valueOf(id));
	}

	public void SendWebviewUrl(String data) {
		_logger.Debug("SendWebviewUrl");
		if (data.length() < 5) {
			_logger.Warn("Data is too short!");
			Toast.makeText(_context, "Data is too short!", Toast.LENGTH_SHORT).show();
			return;
		}
		sendServerCommand(ServerAction.SHOW_WEBVIEW.toString(), data);
	}

	public void SendUpdateCurrentWeather() {
		_logger.Debug("SendUpdateCurrentWeather");
		sendServerCommand(ServerAction.UPDATE_CURRENT_WEATHER.toString(), "");
	}

	public void SendUpdateForecastWeather() {
		_logger.Debug("SendUpdateForecastWeather");
		sendServerCommand(ServerAction.UPDATE_FORECAST_WEATHER.toString(), "");
	}

	public void SendUpdateRaspiTemperature() {
		_logger.Debug("SendUpdateRaspiTemperature");
		sendServerCommand(ServerAction.UPDATE_RASPBERRY_TEMPERATURE.toString(), "");
	}

	public void SendUpdateIpAddress() {
		_logger.Debug("SendUpdateIpAddress");
		sendServerCommand(ServerAction.UPDATE_IP_ADDRESS.toString(), "");
	}

	public void SendUpdateBirthdayAlarm() {
		_logger.Debug("SendUpdateBirthdayAlarm");
		sendServerCommand(ServerAction.UPDATE_BIRTHDAY_ALARM.toString(), "");
	}

	public void SendScreenOn() {
		_logger.Debug("SendScreenOn");
		sendServerCommand(ServerAction.SCREEN_ON.toString(), "");
	}

	public void SendScreenOff() {
		_logger.Debug("SendScreenOff");
		sendServerCommand(ServerAction.SCREEN_OFF.toString(), "");
	}

	public void SendScreenNormal() {
		_logger.Debug("SendScreenNormal");
		sendServerCommand(ServerAction.SCREEN_NORMAL.toString(), "");
	}

	public void SendScreenSaver() {
		_logger.Debug("SendScreenSaver");
		sendServerCommand(ServerAction.SCREEN_SAVER.toString(), "");
	}

	public void LoadVideos(String searchValue, int results) {
		searchValue = searchValue.replace(" ", "+");

		String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=" + String.valueOf(results)
				+ "&q=" + searchValue + "&key=" + Keys.YOUTUBE_API;

		_loadingVideosDialog = null;
		_loadingVideosDialog = ProgressDialog.show(_context, "Loading Videos...", "");
		_loadingVideosDialog.setCancelable(false);

		DownloadYoutubeVideoTask task = new DownloadYoutubeVideoTask();
		task.execute(new String[] { url });
	}

	public void SelectPongPlayer(String player) {
		_logger.Debug("SelectPlayer: " + player);
		_selectedPongPlayer = player;
	}

	public void SendPongCommandStart() {
		_logger.Debug("SendPongCommandStart");
		_pongIsRunning = true;
		sendServerCommand(ServerAction.GAME_PONG_START.toString(), "");
	}

	public void SendPongCommandRestart() {
		_logger.Debug("SendPongCommandRestart");
		sendServerCommand(ServerAction.GAME_PONG_RESTART.toString(), "");
	}

	public void SendPongCommandStop() {
		_logger.Debug("SendPongCommandStop");
		_pongIsRunning = false;
		sendServerCommand(ServerAction.GAME_PONG_STOP.toString(), "");
	}

	public void SendPongCommandPause() {
		_logger.Debug("SendPongCommandPause");
		sendServerCommand(ServerAction.GAME_PONG_PAUSE.toString(), "");
	}

	public void SendPongCommandResume() {
		_logger.Debug("SendPongCommandResume");
		sendServerCommand(ServerAction.GAME_PONG_RESUME.toString(), "");
	}

	public void SendSnakeCommandStart() {
		_logger.Debug("SendSnakeCommandStart");
		sendServerCommand(ServerAction.GAME_SNAKE_START.toString(), "");
	}

	public void SendSnakeCommandStop() {
		_logger.Debug("SendSnakeCommandStop");
		sendServerCommand(ServerAction.GAME_SNAKE_STOP.toString(), "");
	}

	public void SendTetrisCommandStart() {
		_logger.Debug("SendTetrisCommandStart");
		sendServerCommand(ServerAction.GAME_TETRIS_START.toString(), "");
	}

	public void SendTetrisCommandStop() {
		_logger.Debug("SendTetrisCommandStop");
		sendServerCommand(ServerAction.GAME_TETRIS_STOP.toString(), "");
	}

	public void SendGameCommandUp() {
		_logger.Debug("SendGameCommandUp");
		sendServerCommand(ServerAction.GAME_COMMAND.toString(), UP);
	}

	public void SendGameCommandLeft() {
		_logger.Debug("SendGameCommandLeft");
		sendServerCommand(ServerAction.GAME_COMMAND.toString(), LEFT);
	}

	public void SendGameCommandRight() {
		_logger.Debug("SendGameCommandRight");
		sendServerCommand(ServerAction.GAME_COMMAND.toString(), RIGHT);
	}

	public void SendGameCommandDown() {
		_logger.Debug("SendGameCommandDown");
		sendServerCommand(ServerAction.GAME_COMMAND.toString(), DOWN);
	}

	public void SendGameCommandRotate() {
		_logger.Debug("SendGameCommandRotate");
		sendServerCommand(ServerAction.GAME_COMMAND.toString(), ROTATE);
	}

	public void SendRebootDevice() {
		_logger.Debug("SendRebootDevice");
		sendServerCommand(ServerAction.SYSTEM_REBOOT.toString(), "");
	}

	public void SendShutdownDevice() {
		_logger.Debug("SendShutdownDevice");
		sendServerCommand(ServerAction.SYSTEM_SHUTDOWN.toString(), "");
	}

	private void sendServerCommand(String command, String data) {
		_logger.Debug("sendServerCommand: " + command + " with data " + data);

		if (_pongIsRunning) {
			if (_selectedPongPlayer != null) {
				data = _selectedPongPlayer + ":" + data;
			} else {
				_logger.Warn("Pong is running, but selectedplayer is null!");
				Toast.makeText(_context, "Pong is running, but selectedplayer is null!", Toast.LENGTH_SHORT).show();
				return;
			}
		}
		String communication = "ACTION:" + command + "&DATA:" + data;
		_logger.Debug("Communication is: " + communication);

		_clientTask = new ClientTask(_context, _selectedServerIp, SERVERPORT);
		_clientTask.SetCommunication(communication);
		_clientTask.execute();
	}

	private class DownloadYoutubeVideoTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			if (urls.length > 1) {
				_logger.Warn("Entered too many urls!");
				return "Error:Entered too many urls!";
			}

			Document document = null;
			try {
				String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.56 Safari/537.17";
				document = Jsoup.connect(urls[0]).ignoreContentType(true).timeout(60 * 1000).userAgent(userAgent).get();
			} catch (IOException e) {
				_logger.Error(e.toString());
			}

			if (document != null) {
				String getJson = document.text();

				JSONObject jsonObject = null;
				try {
					jsonObject = (JSONObject) new JSONTokener(getJson).nextValue();
				} catch (JSONException e) {
					_logger.Error(e.toString());
				}

				if (jsonObject != null) {
					try {
						final ArrayList<YoutubeVideoDto> youtubeVideoList = new ArrayList<YoutubeVideoDto>();

						JSONArray items = jsonObject.getJSONArray("items");
						for (int index = 0; index < items.length(); index++) {
							JSONObject object = items.getJSONObject(index);

							try {
								JSONObject id = object.getJSONObject("id");
								String videoId = id.getString("videoId");

								JSONObject snippet = object.getJSONObject("snippet");
								String title = snippet.getString("title");
								String description = snippet.getString("description");

								if (videoId != null && title != null && description != null) {
									YoutubeVideoDto modelDto = new YoutubeVideoDto(videoId, title, description);
									_logger.Debug("New Dto: " + modelDto.toString());
									youtubeVideoList.add(modelDto);
								} else {
									_logger.Warn("Error in parsing data!");
								}
							} catch (Exception e) {
								_logger.Error(e.toString());
							}
						}

						((Activity) _context).runOnUiThread(new Runnable() {
							public void run() {
								_loadingVideosDialog.dismiss();
							}
						});
						if (youtubeVideoList.size() > 0) {
							((Activity) _context).runOnUiThread(new Runnable() {
								public void run() {
									_dialogService.ShowSelectYoutubeIdDialog(youtubeVideoList);
								}
							});
						}
					} catch (JSONException e) {
						_logger.Error(e.toString());
					}
				}
			}

			return "";
		}

		@Override
		protected void onPostExecute(String result) {
		}
	}
}
