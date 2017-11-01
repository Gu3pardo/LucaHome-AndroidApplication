package guepardoapps.lucahome.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.rey.material.app.Dialog;
import com.rey.material.app.ThemeManager;
import com.rey.material.widget.FloatingActionButton;

import java.util.Locale;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.common.classes.Schedule;
import guepardoapps.lucahome.common.dto.ScheduleDto;
import guepardoapps.lucahome.common.service.ScheduleService;
import guepardoapps.lucahome.service.NavigationService;
import guepardoapps.lucahome.views.ScheduleEditActivity;

public class ScheduleListViewAdapter extends BaseAdapter {
    private class Holder {
        private ImageView _socketImageView;
        private TextView _titleText;
        private TextView _dateText;
        private TextView _timeText;
        private TextView _socketText;
        private TextView _socketActionText;
        private Switch _cardSwitch;
        private FloatingActionButton _updateButton;
        private FloatingActionButton _deleteButton;

        private void displayDeleteDialog(@NonNull final Schedule schedule) {
            final Dialog deleteDialog = new Dialog(_context);

            deleteDialog
                    .title(String.format(Locale.getDefault(), "Delete %s?", schedule.GetName()))
                    .positiveAction("Delete")
                    .negativeAction("Cancel")
                    .applyStyle(_isLightTheme ? R.style.SimpleDialogLight : R.style.SimpleDialog)
                    .setCancelable(true);

            deleteDialog.positiveActionClickListener(view -> {
                _scheduleService.DeleteSchedule(schedule);
                deleteDialog.dismiss();
            });

            deleteDialog.negativeActionClickListener(view -> deleteDialog.dismiss());

            deleteDialog.show();
        }
    }

    private Context _context;
    private NavigationService _navigationService;
    private ScheduleService _scheduleService;

    private SerializableList<Schedule> _listViewItems;

    private static LayoutInflater _inflater = null;
    private boolean _isLightTheme;

    public ScheduleListViewAdapter(@NonNull Context context, @NonNull SerializableList<Schedule> listViewItems) {
        _context = context;
        _navigationService = NavigationService.getInstance();
        _scheduleService = ScheduleService.getInstance();

        _listViewItems = listViewItems;

        _inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        View rowView = _inflater.inflate(R.layout.listview_card_schedule, null);

        holder._socketImageView = rowView.findViewById(R.id.schedule_card_image);
        holder._titleText = rowView.findViewById(R.id.scheduleCardTitleText);
        holder._dateText = rowView.findViewById(R.id.scheduleDateText);
        holder._timeText = rowView.findViewById(R.id.scheduleTimeText);
        holder._socketText = rowView.findViewById(R.id.scheduleSocketText);
        holder._socketActionText = rowView.findViewById(R.id.scheduleSocketActionText);
        holder._cardSwitch = rowView.findViewById(R.id.scheduleCardSwitch);
        holder._updateButton = rowView.findViewById(R.id.schedule_card_update_button);
        holder._deleteButton = rowView.findViewById(R.id.schedule_card_delete_button);

        final Schedule schedule = _listViewItems.getValue(index);

        holder._socketImageView.setImageResource(schedule.GetSocket().GetDrawable());

        holder._titleText.setText(schedule.GetName());
        holder._dateText.setText(schedule.GetWeekday().toString());
        holder._timeText.setText(schedule.GetTime().HHMM());
        holder._socketText.setText(schedule.GetSocket().GetName());
        holder._socketActionText.setText(schedule.GetAction().toString());

        holder._cardSwitch.setChecked(schedule.IsActive());
        holder._cardSwitch.setOnCheckedChangeListener((compoundButton, value) -> _scheduleService.SetScheduleState(schedule, value));

        holder._updateButton.setOnClickListener(view -> {
            Bundle data = new Bundle();
            data.putSerializable(ScheduleService.ScheduleIntent, new ScheduleDto(schedule.GetId(), schedule.GetName(), schedule.GetSocket(), schedule.GetWeekday(), schedule.GetTime(), schedule.GetAction(), ScheduleDto.Action.Update));
            _navigationService.NavigateToActivityWithData(_context, ScheduleEditActivity.class, data);
        });

        holder._deleteButton.setOnClickListener(view -> holder.displayDeleteDialog(schedule));

        return rowView;
    }
}
