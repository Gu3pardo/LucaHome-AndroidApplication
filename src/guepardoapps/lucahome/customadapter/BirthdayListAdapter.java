package guepardoapps.lucahome.customadapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.Constants;
import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.classes.SerializableList;
import guepardoapps.lucahome.dto.BirthdayDto;
import guepardoapps.lucahome.services.helper.DialogService;
import guepardoapps.lucahome.viewcontroller.BirthdayController;
import guepardoapps.particles.ParticleSystem;

public class BirthdayListAdapter extends BaseAdapter {

	private static final String TAG = BirthdayListAdapter.class.getName();
	private LucaHomeLogger _logger;

	private SerializableList<BirthdayDto> _birthdayList;

	private Context _context;

	private BirthdayController _birthdayController;
	private DialogService _dialogService;

	private static LayoutInflater _inflater = null;

	public BirthdayListAdapter(Context context, SerializableList<BirthdayDto> birthdayList) {
		_logger = new LucaHomeLogger(TAG);

		_birthdayList = birthdayList;
		for (int index = 0; index < _birthdayList.getSize(); index++) {
			_logger.Debug(_birthdayList.getValue(index).toString());
		}

		_context = context;

		_birthdayController = new BirthdayController();
		_dialogService = new DialogService(_context);

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

	public class Holder {
		private ImageView _image;
		private TextView _age;
		private TextView _date;
		private Button _name;
	}

	@SuppressLint({ "InflateParams", "ViewHolder" })
	@Override
	public View getView(final int index, View convertView, ViewGroup parent) {
		Holder holder = new Holder();
		View rowView = _inflater.inflate(R.layout.list_birthday_item, null);

		if (_birthdayController.HasBirthday(_birthdayList.getValue(index))) {
			holder._image = (ImageView) rowView.findViewById(R.id.birthday_item_image);
			holder._image.setImageResource(R.drawable.birthday_hd);

			rowView.setBackgroundColor(Constants.BIRTHDAY_BACKGROUND_COLOR);
			new ParticleSystem((Activity) _context, 150, R.drawable.particle, 1250, (float) 1.5, 255)
					.setSpeedRange(0.2f, 0.5f).oneShot(rowView, 150);
		}

		holder._age = (TextView) rowView.findViewById(R.id.birthday_item_age);
		holder._age.setText(String.valueOf(_birthdayController.GetAge(_birthdayList.getValue(index))));

		holder._date = (TextView) rowView.findViewById(R.id.birthday_item_date);
		holder._date.setText(_birthdayList.getValue(index).GetBirthdayString());

		holder._name = (Button) rowView.findViewById(R.id.birthday_item_name);
		holder._name.setText(_birthdayList.getValue(index).GetName());
		holder._name.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				_logger.Debug("onLongClick _name button: " + _birthdayList.getValue(index).GetName());
				_dialogService.ShowUpdateBirthdayDialog(_birthdayList.getValue(index));
				return true;
			}
		});

		return rowView;
	}
}