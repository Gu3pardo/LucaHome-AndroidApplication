package guepardoapps.lucahome.common.tasks;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;

import guepardoapps.lucahome.common.R;
import guepardoapps.lucahome.common.adapter.YoutubeVideoListAdapter;
import guepardoapps.lucahome.common.classes.YoutubeVideo;
import guepardoapps.lucahome.common.controller.BroadcastController;
import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"WeakerAccess"})
public class DownloadYoutubeVideoTask extends AsyncTask<String, Void, String> implements IDownloadYoutubeVideoTask {
    private static final String Tag = DownloadYoutubeVideoTask.class.getSimpleName();

    private Context _context;
    private BroadcastController _broadcastController;
    private ProgressDialog _loadingVideosDialog;

    private boolean _sendFirstEntry = false;
    private boolean _displayDialog = false;
    private boolean _isInitialized = false;

    private ArrayList<YoutubeVideo> _youtubeVideoDataList;

    public DownloadYoutubeVideoTask(@NonNull Context context, ProgressDialog loadingVideosDialog, boolean sendFirstEntry, boolean displayDialog) {
        _context = context;
        _broadcastController = new BroadcastController(_context);
        _loadingVideosDialog = loadingVideosDialog;
        _sendFirstEntry = sendFirstEntry;
        _displayDialog = displayDialog;
        _isInitialized = true;
    }

    @Override
    protected String doInBackground(String... urls) {
        if (!_isInitialized) {
            Logger.getInstance().Error(Tag, "Not initialized!");
            return "Error:Not initialized!";
        }

        if (_context == null) {
            Logger.getInstance().Error(Tag, "_context is null!");
            return "Error:_context is null";
        }

        if (urls.length > 1) {
            Logger.getInstance().Warning(Tag, "Entered too many urls!");
            return "Error:Entered too many urls!";
        }

        Document document = null;
        try {
            String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.56 Safari/537.17";
            document = Jsoup.connect(urls[0]).ignoreContentType(true).timeout(60 * 1000).userAgent(userAgent).get();
        } catch (IOException exception) {
            Logger.getInstance().Error(Tag, exception.getMessage());
        }

        if (document != null) {
            String getJson = document.text();
            JSONObject jsonObject = null;
            try {
                jsonObject = (JSONObject) new JSONTokener(getJson).nextValue();
            } catch (JSONException exception) {
                Logger.getInstance().Error(Tag, exception.getMessage());
            }

            if (jsonObject != null) {
                try {
                    final ArrayList<YoutubeVideo> youtubeVideoDataList = new ArrayList<>();

                    JSONArray items = jsonObject.getJSONArray("items");
                    for (int index = 0; index < items.length(); index++) {
                        JSONObject object = items.getJSONObject(index);

                        try {
                            JSONObject id = object.getJSONObject("id");
                            String videoId = id.getString("videoId");

                            JSONObject snippet = object.getJSONObject("snippet");
                            String title = snippet.getString("title");
                            String description = snippet.getString("description");

                            JSONObject thumbnails = snippet.getJSONObject("thumbnails");
                            JSONObject mediumThumbnails = thumbnails.getJSONObject("medium");
                            String mediumUrl = mediumThumbnails.getString("url");

                            if (videoId != null && title != null && description != null) {
                                YoutubeVideo youtubeVideoData = new YoutubeVideo(videoId, title, description, mediumUrl);
                                youtubeVideoDataList.add(youtubeVideoData);
                            } else {
                                Logger.getInstance().Warning(Tag, "Error in parsing data!");
                            }
                        } catch (Exception exception) {
                            Logger.getInstance().Error(Tag, exception.getMessage());
                        }
                    }

                    if (_sendFirstEntry) {
                        _youtubeVideoDataList = youtubeVideoDataList;
                    }

                    ((Activity) _context).runOnUiThread(() -> {
                        if (_loadingVideosDialog != null) {
                            _loadingVideosDialog.dismiss();
                        }
                    });

                    if (youtubeVideoDataList.size() > 0 && _displayDialog) {
                        ((Activity) _context).runOnUiThread(() -> displayYoutubeIdDialog(youtubeVideoDataList));
                    }
                } catch (JSONException exception) {
                    Logger.getInstance().Error(Tag, exception.getMessage());
                }
            } else {
                Logger.getInstance().Warning(Tag, "JsonObject is null!");
            }
        }

        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        if (_sendFirstEntry) {
            if (_youtubeVideoDataList == null) {
                Logger.getInstance().Error(Tag, "_youtubeVideoDataList is null!");
                return;
            }

            if (_youtubeVideoDataList.size() == 0) {
                Logger.getInstance().Error(Tag, "_youtubeVideoDataList size is 0!");
                return;
            }

            if (_broadcastController == null) {
                Logger.getInstance().Error(Tag, "_broadcastController is null!");
                return;
            }

            _broadcastController.SendSerializableBroadcast(DownloadYoutubeVideoTaskBroadcast, DownloadYoutubeVideoTaskBundle, _youtubeVideoDataList.get(0));
            _sendFirstEntry = false;
        }
    }

    private void displayYoutubeIdDialog(@NonNull ArrayList<YoutubeVideo> youtubeVideoDataList) {
        final Dialog dialog = new Dialog(_context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_listview);

        TextView title = dialog.findViewById(R.id.dialog_list_view_title);
        title.setText(_context.getResources().getString(R.string.select_youtube_video));

        final YoutubeVideoListAdapter listAdapter = new YoutubeVideoListAdapter(_context, youtubeVideoDataList, dialog::dismiss);
        ListView listView = dialog.findViewById(R.id.dialog_list_view_list);
        listView.setAdapter(listAdapter);
        listView.setVisibility(View.VISIBLE);

        Button closeButton = dialog.findViewById(R.id.dialog_list_view_close);
        closeButton.setOnClickListener(view -> dialog.dismiss());

        dialog.setCancelable(true);
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        } else {
            Logger.getInstance().Warning(Tag, "Window is null!");
        }
    }
}
