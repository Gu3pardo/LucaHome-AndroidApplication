package guepardoapps.library.lucahome.customadapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.plattysoft.leonids.ParticleSystem;

import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.constants.Color;
import guepardoapps.library.lucahome.common.dto.BirthdayDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaDialogController;

import guepardoapps.library.toolset.common.classes.SerializableList;

public class BirthdayListAdapter extends BaseAdapter {

    private static final String TAG = BirthdayListAdapter.class.getSimpleName();
    private LucaHomeLogger _logger;

    private SerializableList<BirthdayDto> _birthdayList;
    private boolean _isOnWear;

    private Context _context;
    private LucaDialogController _dialogController;

    private static LayoutInflater _inflater = null;

    public BirthdayListAdapter(@NonNull Context context,
                               @NonNull SerializableList<BirthdayDto> birthdayList,
                               boolean isOnWear) {
        _logger = new LucaHomeLogger(TAG);

        _birthdayList = birthdayList;
        for (int index = 0; index < _birthdayList.getSize(); index++) {
            _logger.Debug(_birthdayList.getValue(index).toString());
        }
        _isOnWear = isOnWear;

        _context = context;
        _dialogController = new LucaDialogController(_context);

        _inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return _birthdayList.getSize();
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
        private TextView _age;
        private TextView _date;
        private Button _name;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView = _inflater.inflate(R.layout.list_birthday_item, null);

        if (_birthdayList.getValue(index).HasBirthday()) {
            holder._image = (ImageView) rowView.findViewById(R.id.birthday_item_image);
            holder._image.setImageResource(R.drawable.birthday_hd);

            rowView.setBackgroundColor(Color.BIRTHDAY_BACKGROUND);
            new ParticleSystem((Activity) _context, 150, R.drawable.particle, 1250)
                    .setSpeedRange(0.2f, 0.5f).oneShot(rowView, 150);
        }

        if (_isOnWear) {
            holder._image.setVisibility(View.GONE);


            holder._border = (TextView) rowView.findViewById(R.id.birthday_item_border);
            holder._border.setVisibility(View.GONE);
        }

        holder._age = (TextView) rowView.findViewById(R.id.birthday_item_age);
        holder._age.setText(String.valueOf(_birthdayList.getValue(index).GetAge()));

        holder._date = (TextView) rowView.findViewById(R.id.birthday_item_date);
        holder._date.setText(_birthdayList.getValue(index).GetBirthdayString());

        holder._name = (Button) rowView.findViewById(R.id.birthday_item_name);
        holder._name.setText(_birthdayList.getValue(index).GetName());
        holder._name.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View arg0) {
                _logger.Debug("onLongClick _name button: " + _birthdayList.getValue(index).GetName());
                if (!_isOnWear) {
                    _dialogController.ShowUpdateBirthdayDialog(_birthdayList.getValue(index));
                } else {
                    _logger.Warn("Not supported on wearable device!");
                }
                return true;
            }
        });

        return rowView;
    }
}