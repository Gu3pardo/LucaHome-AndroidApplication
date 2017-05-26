package guepardoapps.library.lucahome.customadapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.dto.ShoppingEntryDto;
import guepardoapps.library.lucahome.common.enums.LucaObject;
import guepardoapps.library.lucahome.common.enums.MainServiceAction;
import guepardoapps.library.lucahome.common.enums.RaspberrySelection;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaDialogController;
import guepardoapps.library.lucahome.controller.ServiceController;
import guepardoapps.library.lucahome.services.helper.MessageSendHelper;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.BroadcastController;

public class ShoppingListAdapter extends BaseAdapter {

    private static final String TAG = ShoppingListAdapter.class.getSimpleName();
    private LucaHomeLogger _logger;

    private SerializableList<ShoppingEntryDto> _entryList;
    private boolean _isOnWear;

    private Context _context;

    private BroadcastController _broadcastController;
    private LucaDialogController _dialogController;
    private MessageSendHelper _messageSendHelper;
    private ServiceController _serviceController;

    private static LayoutInflater _inflater = null;

    private Runnable _getDataRunnable = new Runnable() {
        public void run() {
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.MAIN_SERVICE_COMMAND,
                    new String[]{Bundles.MAIN_SERVICE_ACTION},
                    new Object[]{MainServiceAction.GET_SHOPPING_LIST});
        }
    };

    public ShoppingListAdapter(@NonNull Context context,
                               @NonNull SerializableList<ShoppingEntryDto> entryList,
                               boolean isOnWear,
                               Class<?> phoneMessageService) {
        _logger = new LucaHomeLogger(TAG);

        _entryList = entryList;
        _isOnWear = isOnWear;

        _context = context;

        SerializableList<ShoppingEntryDto> sortedList = new SerializableList<>();
        for (int index = 0; index < _entryList.getSize(); index++) {
            ShoppingEntryDto entry = _entryList.getValue(index);
            if (!entry.IsBought()) {
                sortedList.setFirstValue(entry);
            } else {
                sortedList.addValue(entry);
            }
        }
        _entryList = sortedList;

        for (int index = 0; index < _entryList.getSize(); index++) {
            _logger.Debug(_entryList.getValue(index).toString());
        }

        _broadcastController = new BroadcastController(_context);
        _dialogController = new LucaDialogController(_context);
        if (phoneMessageService != null && _isOnWear) {
            _messageSendHelper = new MessageSendHelper(_context, phoneMessageService);
        }
        _serviceController = new ServiceController(_context);

        _inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return _entryList.getSize();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class Holder {
        private ImageView _image;
        private TextView _border;
        private TextView _name;
        private Button _quantity;
        private ImageButton _increase;
        private ImageButton _decrease;
        private ImageButton _delete;
        private CheckBox _bought;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView = _inflater.inflate(R.layout.list_shopping_item, null);

        final ShoppingEntryDto entry = _entryList.getValue(index);
        _logger.Info(String.format("entry is %s", entry.toString()));

        holder._image = (ImageView) rowView.findViewById(R.id.shopping_item_image);
        holder._image.setImageResource(entry.GetGroup().GetDrawable());

        if (_isOnWear) {
            holder._image.setVisibility(View.GONE);

            holder._border = (TextView) rowView.findViewById(R.id.shopping_item_border);
            holder._border.setVisibility(View.GONE);
        }

        holder._name = (TextView) rowView.findViewById(R.id.shopping_item_name);
        holder._name.setText(entry.GetName());

        holder._quantity = (Button) rowView.findViewById(R.id.shopping_button_quantity);
        holder._quantity.setText(String.valueOf(entry.GetQuantity()));
        holder._quantity.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                _logger.Debug("onClick _quantity button: " + entry.GetName());
                if (!_isOnWear) {
                    int size = 0;
                    if (_entryList != null) {
                        size = _entryList.getSize();
                    }
                    _dialogController.ShowAddShoppingEntryDialog(
                            (Activity) _context,
                            _getDataRunnable,
                            entry,
                            false,
                            true,
                            size);
                } else {
                    _logger.Warn("Not supported on wearable device!");
                }
            }
        });

        holder._increase = (ImageButton) rowView.findViewById(R.id.shopping_button_increase);
        holder._increase.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                _logger.Debug("onClick _increase button: " + entry.GetName());
                if (!_isOnWear) {
                    entry.IncreaseQuantity();
                    _serviceController.StartRestService(
                            Bundles.SHOPPING_LIST,
                            entry.GetCommandUpdate(),
                            Broadcasts.RELOAD_SHOPPING_LIST,
                            LucaObject.SHOPPING_ENTRY,
                            RaspberrySelection.BOTH);
                } else {
                    _logger.Warn("Not supported on wearable device!");
                }
            }
        });

        holder._decrease = (ImageButton) rowView.findViewById(R.id.shopping_button_decrease);
        holder._decrease.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                _logger.Debug("onClick _decrease button: " + entry.GetName());
                if (!_isOnWear) {
                    entry.DecreaseQuantity();
                    _serviceController.StartRestService(
                            Bundles.SHOPPING_LIST,
                            entry.GetCommandUpdate(),
                            Broadcasts.RELOAD_SHOPPING_LIST,
                            LucaObject.SHOPPING_ENTRY,
                            RaspberrySelection.BOTH);
                } else {
                    _logger.Warn("Not supported on wearable device!");
                }
            }
        });

        holder._delete = (ImageButton) rowView.findViewById(R.id.shopping_button_delete);
        holder._delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                _logger.Debug("onClick _delete button: " + entry.GetName());
                if (!_isOnWear) {
                    _serviceController.StartRestService(
                            Bundles.SHOPPING_LIST,
                            entry.GetCommandDelete(),
                            Broadcasts.RELOAD_SHOPPING_LIST,
                            LucaObject.SHOPPING_ENTRY,
                            RaspberrySelection.BOTH);
                } else {
                    _logger.Warn("Not supported on wearable device!");
                }
            }
        });

        holder._bought = (CheckBox) rowView.findViewById(R.id.shopping_checkbox_bought);
        holder._bought.setChecked(entry.IsBought());
        holder._bought.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean checked) {
                _logger.Debug("onCheckChanged _bought button: " + entry.GetName());
                entry.SetBought(checked);
                if (!_isOnWear) {
                    _broadcastController.SendStringBroadcast(
                            Broadcasts.UPDATE_BOUGHT_SHOPPING_LIST,
                            Bundles.SHOPPING_LIST,
                            String.valueOf(entry.GetName()) + ":" + (checked ? "1" : "0"));
                } else {
                    if (_messageSendHelper != null) {
                        _messageSendHelper.SendMessage(entry.GetCommandBoughtChanged());
                    } else {
                        _logger.Error("MessageSendHelper is null!");
                    }
                }
            }
        });

        return rowView;
    }
}