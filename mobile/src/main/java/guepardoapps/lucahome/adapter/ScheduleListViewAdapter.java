package guepardoapps.lucahome.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.rey.material.app.Dialog;
import com.rey.material.app.ThemeManager;

import java.util.Locale;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.Schedule;
import guepardoapps.lucahome.common.dto.ScheduleDto;
import guepardoapps.lucahome.common.service.ScheduleService;
import guepardoapps.lucahome.service.NavigationService;
import guepardoapps.lucahome.views.ScheduleEditActivity;

public class ScheduleListViewAdapter extends BaseAdapter {
    private class Holder {
        private TextView _titleText;
        private TextView _dateText;
        private TextView _timeText;
        private TextView _socketText;
        private TextView _socketActionText;
        private Switch _cardSwitch;
        private FloatingActionButton _updateButton;
        private FloatingActionButton _deleteButton;
    }

    private static final String TAG = ScheduleListViewAdapter.class.getSimpleName();
    private Logger _logger;

    private Context _context;

    private NavigationService _navigationService;
    private ScheduleService _scheduleService;

    private static LayoutInflater _inflater = null;

    private SerializableList<Schedule> _listViewItems;

    public ScheduleListViewAdapter(@NonNull Context context, @NonNull SerializableList<Schedule> listViewItems) {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");

        _context = context;

        _listViewItems = listViewItems;

        _navigationService = NavigationService.getInstance();
        _scheduleService = ScheduleService.getInstance();

        _inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        _logger.Debug(String.format(Locale.getDefault(), "getCount: %d", _listViewItems.getSize()));
        return _listViewItems.getSize();
    }

    @Override
    public Object getItem(int position) {
        _logger.Debug(String.format(Locale.getDefault(), "getItem: %d", position));
        return position;
    }

    @Override
    public long getItemId(int position) {
        _logger.Debug(String.format(Locale.getDefault(), "getItemId: %d", position));
        return position;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        Holder holder = new Holder();

        View rowView = _inflater.inflate(R.layout.listview_card_schedule, null);

        holder._titleText = rowView.findViewById(R.id.scheduleCardTitleText);
        holder._dateText = rowView.findViewById(R.id.scheduleDateText);
        holder._timeText = rowView.findViewById(R.id.scheduleTimeText);
        holder._socketText = rowView.findViewById(R.id.scheduleSocketText);
        holder._socketActionText = rowView.findViewById(R.id.scheduleSocketActionText);
        holder._cardSwitch = rowView.findViewById(R.id.scheduleCardSwitch);
        holder._updateButton = rowView.findViewById(R.id.schedule_card_update_button);
        holder._deleteButton = rowView.findViewById(R.id.schedule_card_delete_button);

        final Schedule schedule = _listViewItems.getValue(index);

        holder._titleText.setText(schedule.GetName());
        holder._dateText.setText(schedule.GetWeekday().toString());
        holder._timeText.setText(schedule.GetTime().HHMM());
        holder._socketText.setText(schedule.GetSocket().GetName());
        holder._socketActionText.setText(schedule.GetAction().toString());

        holder._cardSwitch.setChecked(schedule.IsActive());
        holder._cardSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean value) {
                _logger.Debug(String.format(
                        Locale.getDefault(),
                        "setOnCheckedChangeListener onCheckedChanged on CompoundButton %s with value %s",
                        compoundButton, value));
                _scheduleService.SetScheduleState(schedule, value);
            }
        });

        holder._updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("_updateButton setOnClickListener onClick");
                Bundle data = new Bundle();
                data.putSerializable(ScheduleService.ScheduleIntent, new ScheduleDto(schedule.GetId(), schedule.GetName(), schedule.GetSocket(), schedule.GetWeekday(), schedule.GetTime(), schedule.GetAction(), ScheduleDto.Action.Update));
                _navigationService.NavigateToActivityWithData(_context, ScheduleEditActivity.class, data);
            }
        });

        holder._deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("_deleteButton setOnClickListener onClick");

                boolean isLightTheme = ThemeManager.getInstance().getCurrentTheme() == 0;

                final Dialog deleteDialog = new Dialog(_context);
                deleteDialog
                        .title(String.format(Locale.getDefault(), "Delete %s?", schedule.GetName()))
                        .positiveAction("Delete")
                        .negativeAction("Cancel")
                        .applyStyle(isLightTheme ? R.style.SimpleDialogLight : R.style.SimpleDialog)
                        .setCancelable(true);

                deleteDialog.positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        _scheduleService.DeleteSchedule(schedule);
                        deleteDialog.dismiss();
                    }
                });

                deleteDialog.negativeActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteDialog.dismiss();
                    }
                });

                deleteDialog.show();
            }
        });

        return rowView;
    }
}
