package guepardoapps.lucahome.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rey.material.app.Dialog;
import com.rey.material.app.ThemeManager;

import java.util.Locale;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.common.classes.LucaTimer;
import guepardoapps.lucahome.common.service.TimerService;

public class TimerListViewAdapter extends BaseAdapter {
    private class Holder {
        private ImageView _socketImageView;
        private TextView _titleText;
        private TextView _dateText;
        private TextView _timeText;
        private TextView _socketText;
        private TextView _switchText;
        private TextView _actionText;
        private FloatingActionButton _deleteButton;

        private void displayDeleteDialog(@NonNull final LucaTimer timer) {
            final Dialog deleteDialog = new Dialog(_context);

            deleteDialog
                    .title(String.format(Locale.getDefault(), "Delete %s?", timer.GetName()))
                    .positiveAction("Delete")
                    .negativeAction("Cancel")
                    .applyStyle(_isLightTheme ? R.style.SimpleDialogLight : R.style.SimpleDialog)
                    .setCancelable(true);

            deleteDialog.positiveActionClickListener(view -> {
                TimerService.getInstance().DeleteTimer(timer);
                deleteDialog.dismiss();
            });

            deleteDialog.negativeActionClickListener(view -> deleteDialog.dismiss());

            deleteDialog.show();
        }
    }

    private Context _context;
    private SerializableList<LucaTimer> _listViewItems;

    private static LayoutInflater _inflater = null;
    private boolean _isLightTheme;

    public TimerListViewAdapter(@NonNull Context context, @NonNull SerializableList<LucaTimer> listViewItems) {
        _context = context;
        _listViewItems = listViewItems;

        _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _isLightTheme = ThemeManager.getInstance().getCurrentTheme() == 0;
    }

    @Override
    public int getCount() {
        return _listViewItems.getSize();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        final Holder holder = new Holder();

        View rowView = _inflater.inflate(R.layout.listview_card_timer, null);

        holder._socketImageView = rowView.findViewById(R.id.timer_card_image);
        holder._titleText = rowView.findViewById(R.id.timerCardTitleText);
        holder._dateText = rowView.findViewById(R.id.timerDateText);
        holder._timeText = rowView.findViewById(R.id.timerTimeText);
        holder._socketText = rowView.findViewById(R.id.timerSocketText);
        holder._switchText = rowView.findViewById(R.id.timerSwitchText);
        holder._actionText = rowView.findViewById(R.id.timerActionText);
        holder._deleteButton = rowView.findViewById(R.id.timerDeleteButton);

        final LucaTimer timer = _listViewItems.getValue(index);

        holder._socketImageView.setImageResource(timer.GetWirelessSocket().GetDrawable());

        holder._titleText.setText(timer.GetName());
        holder._dateText.setText(timer.GetWeekday().toString());
        holder._timeText.setText(timer.GetTime().HHMM());
        holder._socketText.setText(timer.GetWirelessSocket().GetName());
        holder._switchText.setText(timer.GetWirelessSwitch().GetName());
        holder._actionText.setText(timer.GetAction().toString());

        holder._deleteButton.setOnClickListener(view -> holder.displayDeleteDialog(timer));

        return rowView;
    }
}
