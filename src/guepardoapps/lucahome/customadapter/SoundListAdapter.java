package guepardoapps.lucahome.customadapter;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.classes.Sound;
import guepardoapps.lucahome.common.enums.RaspberrySelection;
import guepardoapps.lucahome.viewcontroller.SoundController;

public class SoundListAdapter extends BaseAdapter {

	private static final String TAG = SoundListAdapter.class.getName();
	private LucaHomeLogger _logger;

	private ArrayList<Sound> _soundList;
	private RaspberrySelection _raspberrySelection;

	private Context _context;
	private SoundController _soundController;

	private static LayoutInflater _inflater = null;

	public SoundListAdapter(Context context, ArrayList<Sound> soundList, RaspberrySelection raspberrySelection) {
		_logger = new LucaHomeLogger(TAG);

		_soundList = soundList;

		_raspberrySelection = raspberrySelection;
		_logger.Debug(_raspberrySelection.toString());

		_context = context;
		_soundController = new SoundController(_context);

		_inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return _soundList.size();
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
		private TextView _name;
		private ImageButton _start;
	}

	@SuppressLint({ "InflateParams", "ViewHolder" })
	@Override
	public View getView(final int index, View convertView, ViewGroup parent) {
		Holder holder = new Holder();
		View rowView = _inflater.inflate(R.layout.list_sound_item, null);

		holder._name = (TextView) rowView.findViewById(R.id.sound_item_name);
		holder._name.setText(_soundList.get(index).GetFileName());

		holder._start = (ImageButton) rowView.findViewById(R.id.sound_item_play);
		holder._start.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				_logger.Debug("onClick _start: " + _soundList.get(index).GetFileName());
				_soundController.StartSound(_soundList.get(index).GetFileName(), _raspberrySelection);
			}
		});

		return rowView;
	}
}