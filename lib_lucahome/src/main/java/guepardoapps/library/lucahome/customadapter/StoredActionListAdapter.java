package guepardoapps.library.lucahome.customadapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.dto.ActionDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.DatabaseController;

import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.controller.BroadcastController;

public class StoredActionListAdapter extends BaseAdapter {

    private static final String TAG = StoredActionListAdapter.class.getSimpleName();
    private LucaHomeLogger _logger;

    private SerializableList<ActionDto> _entryList;

    private BroadcastController _broadcastController;
    private DatabaseController _databaseController;

    private static LayoutInflater _inflater = null;

    public StoredActionListAdapter(
            @NonNull Context context,
            @NonNull SerializableList<ActionDto> entryList,
            @NonNull BroadcastController broadcastController,
            @NonNull DatabaseController databaseController) {
        _logger = new LucaHomeLogger(TAG);

        _entryList = entryList;

        _broadcastController = broadcastController;
        _databaseController = databaseController;

        _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        private TextView _action;
        private ImageButton _delete;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView = _inflater.inflate(R.layout.list_stored_action_item, null);

        final ActionDto entry = _entryList.getValue(index);
        _logger.Info(String.format("entry is %s", entry.toString()));

        holder._action = (TextView) rowView.findViewById(R.id.stored_action_action);
        holder._action.setText(entry.GetAction());

        holder._delete = (ImageButton) rowView.findViewById(R.id.stored_action_button_delete);
        holder._delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("onClick _delete button: " + entry.GetName());
                _databaseController.DeleteAction(entry);
                _broadcastController.SendSimpleBroadcast(Broadcasts.RELOAD_SHOPPING_LIST_FROM_DB);
            }
        });

        return rowView;
    }
}