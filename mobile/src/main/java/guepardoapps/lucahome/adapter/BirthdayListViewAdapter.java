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
import android.widget.TextView;

import java.util.Locale;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.LucaBirthday;
import guepardoapps.lucahome.common.dto.BirthdayDto;
import guepardoapps.lucahome.data.service.BirthdayService;
import guepardoapps.lucahome.service.NavigationService;
import guepardoapps.lucahome.views.BirthdayEditActivity;

public class BirthdayListViewAdapter extends BaseAdapter {
    private class Holder {
        private TextView _titleText;
        private TextView _dateText;
        private TextView _ageTextView;
        private FloatingActionButton _updateButton;
        private FloatingActionButton _deleteButton;
    }

    private static final String TAG = BirthdayListViewAdapter.class.getSimpleName();
    private Logger _logger;

    private Context _context;

    private NavigationService _navigationService;

    private static LayoutInflater _inflater = null;

    private SerializableList<LucaBirthday> _listViewItems;

    public BirthdayListViewAdapter(@NonNull Context context, @NonNull SerializableList<LucaBirthday> listViewItems) {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");

        _context = context;

        _listViewItems = listViewItems;

        _navigationService = NavigationService.getInstance();

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

        View rowView = _inflater.inflate(R.layout.listview_card_birthday, null);

        holder._titleText = rowView.findViewById(R.id.birthday_card_title_text_view);
        holder._dateText = rowView.findViewById(R.id.birthday_date_text_view);
        holder._ageTextView = rowView.findViewById(R.id.birthday_age_text_view);
        holder._updateButton = rowView.findViewById(R.id.birthday_card_update_button);
        holder._deleteButton = rowView.findViewById(R.id.birthday_card_delete_button);

        final LucaBirthday birthday = _listViewItems.getValue(index);

        holder._titleText.setText(birthday.GetName());
        holder._dateText.setText(birthday.GetDate().DDMMYYYY());
        holder._ageTextView.setText(birthday.GetAge());

        if (birthday.HasBirthday()) {
            holder._titleText.setBackgroundColor(_context.getResources().getColor(R.color.LightRed));
        }

        holder._updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("_updateButton setOnClickListener onClick");
                Bundle data = new Bundle();
                data.putSerializable(BirthdayService.BirthdayIntent, new BirthdayDto(birthday.GetId(), birthday.GetName(), birthday.GetDate(), BirthdayDto.Action.Update));
                _navigationService.NavigateToActivityWithData(_context, BirthdayEditActivity.class, data);
            }
        });

        holder._deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("_deleteButton setOnClickListener onClick");
                /*TODO handle delete birthday*/
            }
        });

        return rowView;
    }
}
