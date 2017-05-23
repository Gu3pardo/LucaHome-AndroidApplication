package guepardoapps.lucahome.views;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.customadapter.SocketListAdapter;
import guepardoapps.library.lucahome.services.helper.MessageSendHelper;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.services.PhoneMessageService;

public class SocketView extends Activity {

    private static final String TAG = SocketView.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final String COMMAND = "ACTION:GET:SOCKETS";

    private Context _context;
    private MessageSendHelper _messageSendHelper;
    private ReceiverController _receiverController;

    private boolean _isInitialized;
    private SerializableList<WirelessSocketDto> _itemList = new SerializableList<>();

    private ListAdapter _listAdapter;
    private ListView _listView;
    private TextView _noDataHintView;

    private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateReceiver onReceive");
            SerializableList<WirelessSocketDto> itemList = (SerializableList<WirelessSocketDto>) intent
                    .getSerializableExtra(Bundles.SOCKET_LIST);
            if (itemList != null) {
                _itemList = itemList;
                if (_itemList.getSize() == 0) {
                    _noDataHintView.setVisibility(View.VISIBLE);
                    _listView.setVisibility(View.GONE);
                } else {
                    _noDataHintView.setVisibility(View.GONE);
                    _listView.setVisibility(View.VISIBLE);

                    _listAdapter = new SocketListAdapter(_context, _itemList, true, PhoneMessageService.class);
                    _listView.setAdapter(_listAdapter);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_basic_list);

        _logger = new LucaHomeLogger(TAG);
        _logger.Debug("onCreate");

        _context = this;
        _messageSendHelper = new MessageSendHelper(_context, PhoneMessageService.class);
        _receiverController = new ReceiverController(_context);

        _itemList.addValue(new WirelessSocketDto(-1, R.xml.circle_yellow, "Loading...", false));

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.basicListWatchViewStub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                _listView = (ListView) stub.findViewById(R.id.basicListView);
                _listAdapter = new SocketListAdapter(_context, _itemList, true, PhoneMessageService.class);
                _listView.setAdapter(_listAdapter);
                _noDataHintView = (TextView) stub.findViewById(R.id.noDataTextView);

                if (!_isInitialized) {
                    _receiverController.RegisterReceiver(_updateReceiver,
                            new String[]{Broadcasts.UPDATE_SOCKET_LIST});
                    _messageSendHelper.SendMessage(COMMAND);
                    _isInitialized = true;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        _logger.Debug("onResume");
    }

    @Override
    protected void onPause() {
        _logger.Debug("onPause");
        _receiverController.Dispose();
        _isInitialized = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        _logger.Debug("onDestroy");
        _receiverController.Dispose();
        _isInitialized = false;
        super.onDestroy();
    }
}