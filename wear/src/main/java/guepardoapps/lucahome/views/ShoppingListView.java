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
import guepardoapps.library.lucahome.common.dto.ShoppingEntryDto;
import guepardoapps.library.lucahome.common.enums.ShoppingEntryGroup;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.customadapter.ShoppingListAdapter;
import guepardoapps.library.lucahome.services.helper.MessageSendHelper;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.ReceiverController;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.services.PhoneMessageService;

public class ShoppingListView extends Activity {

    private static final String TAG = ShoppingListView.class.getSimpleName();
    private LucaHomeLogger _logger;

    private static final String COMMAND = "ACTION:GET:SHOPPING_LIST";

    private Context _context;
    private MessageSendHelper _messageSendHelper;
    private ReceiverController _receiverController;

    private boolean _isInitialized;
    private SerializableList<ShoppingEntryDto> _itemList = new SerializableList<>();

    private ListAdapter _listAdapter;
    private ListView _listView;
    private TextView _noDataHintView;

    private BroadcastReceiver _updateReceiver = new BroadcastReceiver() {
        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent) {
            _logger.Debug("_updateReceiver onReceive");
            SerializableList<ShoppingEntryDto> itemList = (SerializableList<ShoppingEntryDto>) intent
                    .getSerializableExtra(Bundles.SHOPPING_LIST);
            if (itemList != null) {
                _itemList = itemList;
                if (_itemList.getSize() == 0) {
                    _noDataHintView.setVisibility(View.VISIBLE);
                    _listView.setVisibility(View.GONE);
                } else {
                    _noDataHintView.setVisibility(View.GONE);
                    _listView.setVisibility(View.VISIBLE);

                    _listAdapter = new ShoppingListAdapter(_context, _itemList, true, PhoneMessageService.class);
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

        _itemList.addValue(new ShoppingEntryDto(-1, "Loading...", ShoppingEntryGroup.OTHER, 0, false));

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.basicListWatchViewStub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub watchViewStub) {
                _listView = (ListView) watchViewStub.findViewById(R.id.basicListView);
                _listAdapter = new ShoppingListAdapter(_context, _itemList, true, PhoneMessageService.class);
                _listView.setAdapter(_listAdapter);
                _noDataHintView = (TextView) watchViewStub.findViewById(R.id.noDataTextView);

                if (!_isInitialized) {
                    _receiverController.RegisterReceiver(_updateReceiver, new String[]{Broadcasts.UPDATE_SHOPPING_LIST});
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