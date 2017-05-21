package guepardoapps.library.lucahome.customadapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import es.dmoral.toasty.Toasty;

import guepardoapps.library.lucahome.R;
import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.dto.ListedMenuDto;
import guepardoapps.library.lucahome.common.dto.MenuDto;
import guepardoapps.library.lucahome.common.enums.LucaObject;
import guepardoapps.library.lucahome.common.enums.RaspberrySelection;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaDialogController;
import guepardoapps.library.lucahome.controller.ServiceController;

import guepardoapps.library.toolset.common.classes.SerializableList;

public class MenuListAdapter extends BaseAdapter {

    private static final String TAG = MenuListAdapter.class.getSimpleName();
    private LucaHomeLogger _logger;

    private SerializableList<MenuDto> _menu;
    private SerializableList<ListedMenuDto> _listedMenu;

    private boolean _isOnMediaMirror;
    private boolean _isOnWatch;

    private Context _context;
    private LucaDialogController _dialogController;
    private ServiceController _serviceController;

    private static LayoutInflater _inflater = null;

    public MenuListAdapter(@NonNull Context context,
                           @NonNull SerializableList<MenuDto> menu,
                           SerializableList<ListedMenuDto> listedMenu,
                           boolean isOnMediaMirror,
                           boolean isOnWatch) {
        _logger = new LucaHomeLogger(TAG);

        int dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int startIndex = -1;
        for (int index = 0; index < menu.getSize(); index++) {
            if (menu.getValue(index).GetDay() == dayOfMonth) {
                startIndex = index;
                break;
            }
        }
        _menu = menu;
        _listedMenu = listedMenu;

        if (startIndex != -1) {
            SerializableList<MenuDto> sortedList = new SerializableList<>();
            int selectedIndex = startIndex;
            for (int index = 0; index < menu.getSize(); index++) {
                if (selectedIndex >= menu.getSize()) {
                    selectedIndex = selectedIndex - menu.getSize();
                }
                sortedList.addValue(menu.getValue(selectedIndex));
                selectedIndex++;
            }
            _menu = sortedList;
        }

        _isOnMediaMirror = isOnMediaMirror;
        _isOnWatch = isOnWatch;

        _context = context;
        _dialogController = new LucaDialogController(_context);
        _serviceController = new ServiceController(_context);

        _inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return _menu.getSize();
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
        private TextView _title;
        private TextView _description;
        private TextView _weekday;
        private TextView _date;
        private ImageButton _random;
        private ImageButton _update;
        private ImageButton _clear;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView = _inflater.inflate(R.layout.list_menu_item, null);

        if (_isOnWatch) {
            holder._image = (ImageView) rowView.findViewById(R.id.menu_item_image);
            holder._image.setVisibility(View.GONE);
            holder._image.setEnabled(false);

            holder._border = (TextView) rowView.findViewById(R.id.menu_item_border);
            holder._border.setVisibility(View.GONE);
            holder._border.setEnabled(false);
        }

        holder._title = (TextView) rowView.findViewById(R.id.menu_item_title);
        holder._title.setText(String.valueOf(_menu.getValue(index).GetTitle()));

        holder._description = (TextView) rowView.findViewById(R.id.menu_item_description);
        holder._description.setText(String.valueOf(_menu.getValue(index).GetDescription()));

        holder._weekday = (TextView) rowView.findViewById(R.id.menu_item_weekday);
        holder._weekday.setText(_menu.getValue(index).GetWeekday());

        holder._date = (TextView) rowView.findViewById(R.id.menu_item_date);
        holder._date.setText(_menu.getValue(index).GetDate());

        holder._random = (ImageButton) rowView.findViewById(R.id.menu_item_random);
        holder._random.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("onClick _random button: " + _menu.getValue(index).GetTitle());
                if (_listedMenu != null) {
                    if (_listedMenu.getSize() > 0) {
                        _dialogController.ShowRandomMenuDialog(_menu.getValue(index), _listedMenu);
                    } else {
                        _logger.Warn("No entries for ListedMenu!");
                        Toasty.warning(_context, "No entries for random menu!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    _logger.Warn("No entries for ListedMenu!");
                    Toasty.warning(_context, "No entries for random menu!", Toast.LENGTH_LONG).show();
                }
            }
        });
        if (_isOnMediaMirror || _isOnWatch) {
            holder._update.setVisibility(View.INVISIBLE);
            holder._update.setEnabled(false);
        }

        holder._update = (ImageButton) rowView.findViewById(R.id.menu_item_update);
        holder._update.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("onClick _update button: " + _menu.getValue(index).GetTitle());
                _dialogController.ShowUpdateMenuDialog(_menu.getValue(index), _listedMenu);
            }
        });
        if (_isOnMediaMirror || _isOnWatch) {
            holder._update.setVisibility(View.INVISIBLE);
            holder._update.setEnabled(false);
        }

        holder._clear = (ImageButton) rowView.findViewById(R.id.menu_item_clear);
        holder._clear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("onClick _clear button: " + _menu.getValue(index).GetTitle());
                _serviceController.StartRestService(Bundles.MENU, _menu.getValue(index).GetCommandClear(),
                        Broadcasts.RELOAD_MENU, LucaObject.MENU, RaspberrySelection.BOTH);
            }
        });
        if (_isOnWatch) {
            holder._clear.setVisibility(View.INVISIBLE);
            holder._clear.setEnabled(false);
        }

        return rowView;
    }
}