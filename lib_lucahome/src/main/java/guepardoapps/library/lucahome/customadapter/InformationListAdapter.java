package guepardoapps.library.lucahome.customadapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.dto.InformationDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaDialogController;

public class InformationListAdapter extends BaseAdapter {

    private static final String TAG = InformationListAdapter.class.getSimpleName();
    private LucaHomeLogger _logger;

    private InformationDto _information;
    private LucaDialogController _dialogController;
    private static LayoutInflater _inflater = null;

    public InformationListAdapter(
            @NonNull Context context,
            @NonNull InformationDto information) {
        _logger = new LucaHomeLogger(TAG);

        _information = information;
        _logger.Debug(_information.toString());

        _dialogController = new LucaDialogController(context);
        _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return _information.GetInformationList().size();
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
        private TextView _key;
        private TextView _value;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView = _inflater.inflate(R.layout.list_information_item, null);

        holder._key = (TextView) rowView.findViewById(R.id.information_item_key);
        holder._key.setText(_information.GetKey(index));
        if (_information.GetKey(index).contains("contact")) {
            holder._key.setLongClickable(true);
            holder._key.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    _logger.Debug("_key onLongClick");
                    _dialogController.ShowSendInformationMailDialog();
                    return true;
                }
            });
        }

        holder._value = (TextView) rowView.findViewById(R.id.information_item_value);
        holder._value.setText(_information.GetValue(index));
        if (_information.GetKey(index).contains("contact")) {
            holder._value.setLongClickable(true);
            holder._value.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    _logger.Debug("_value onLongClick");
                    _dialogController.ShowSendInformationMailDialog();
                    return true;
                }
            });
        }

        return rowView;
    }
}