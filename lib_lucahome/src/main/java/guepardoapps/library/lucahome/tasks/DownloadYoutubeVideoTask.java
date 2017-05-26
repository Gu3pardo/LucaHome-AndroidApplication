package guepardoapps.library.lucahome.tasks;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.dto.YoutubeVideoDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaDialogController;

import guepardoapps.library.toolset.controller.BroadcastController;

public class DownloadYoutubeVideoTask extends AsyncTask<String, Void, String> {

    private static final String TAG = DownloadYoutubeVideoTask.class.getSimpleName();
    private LucaHomeLogger _logger;

    private Context _context;
    private BroadcastController _broadcastController;
    private LucaDialogController _dialogController;

    private ProgressDialog _loadingVideosDialog;

    private boolean _isInitialized = false;
    private boolean _sendFirstEntry = false;

    private String _serverIp;
    private ArrayList<YoutubeVideoDto> _youtubeVideoList;

    public DownloadYoutubeVideoTask(
            @NonNull Context context,
            @NonNull BroadcastController broadcastController,
            LucaDialogController dialogController,
            ProgressDialog loadingVideosDialog,
            @NonNull String serverIp) {
        _logger = new LucaHomeLogger(TAG);

        _context = context;

        _broadcastController = broadcastController;
        _dialogController = dialogController;
        _loadingVideosDialog = loadingVideosDialog;

        _serverIp = serverIp;

        _isInitialized = true;
    }

    public DownloadYoutubeVideoTask(
            @NonNull Context context,
            @NonNull BroadcastController broadcastController,
            @NonNull String serverIp) {
        this(context, broadcastController, null, null, serverIp);
    }

    public DownloadYoutubeVideoTask(
            @NonNull Context context,
            @NonNull BroadcastController broadcastController) {
        this(context, broadcastController, null, null, "");
    }

    public void SetSendFirstEntry(boolean sendFirstEntry) {
        _logger.Debug("SetSendFirstEntry to " + String.valueOf(sendFirstEntry));
        _sendFirstEntry = sendFirstEntry;
    }

    public void SetLoadingVideosDialog(@NonNull ProgressDialog loadingVideosDialog) {
        _logger.Debug("SetLoadingVideosDialog");
        _loadingVideosDialog = loadingVideosDialog;
    }

    @Override
    protected String doInBackground(String... urls) {
        _logger.Debug("doInBackground");

        if (!_isInitialized) {
            _logger.Error(TAG + " is not initialized!");
            return "Error:Not initialized!";
        }

        if (_context == null) {
            _logger.Error("_context is null!");
            return "Error:_context is null";
        }

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
                    final ArrayList<YoutubeVideoDto> youtubeVideoList = new ArrayList<>();

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

                    if (_sendFirstEntry) {
                        _youtubeVideoList = youtubeVideoList;
                    }

                    ((Activity) _context).runOnUiThread(new Runnable() {
                        public void run() {
                            if (_loadingVideosDialog != null) {
                                _loadingVideosDialog.dismiss();
                            }
                        }
                    });

                    if (youtubeVideoList.size() > 0) {
                        ((Activity) _context).runOnUiThread(new Runnable() {
                            public void run() {
                                if (_dialogController != null) {
                                    _dialogController.ShowSelectYoutubeIdDialog(_serverIp, youtubeVideoList);
                                }
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
        _logger.Debug("onPostExecute: result: " + result);

        if (_sendFirstEntry) {
            _logger.Debug("Sending first entry");

            _sendFirstEntry = false;

            if (_youtubeVideoList == null) {
                _logger.Error("_youtubeVideoList is null!");
                return;
            }

            if (_youtubeVideoList.size() == 0) {
                _logger.Error("_youtubeVideoList size is 0!");
                return;
            }

            if (_broadcastController == null) {
                _logger.Error("_broadcastController is null!");
                return;
            }

            _broadcastController.SendStringBroadcast(
                    Broadcasts.YOUTUBE_ID,
                    Bundles.YOUTUBE_ID,
                    _youtubeVideoList.get(0).GetYoutubeId());

            _broadcastController.SendSerializableBroadcast(
                    Broadcasts.YOUTUBE_VIDEO,
                    Bundles.YOUTUBE_VIDEO,
                    _youtubeVideoList.get(0));
        }
    }
}
