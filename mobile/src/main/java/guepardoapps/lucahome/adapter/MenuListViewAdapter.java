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

import com.rey.material.app.Dialog;
import com.rey.material.app.ThemeManager;

import java.util.Locale;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.common.classes.LucaMenu;
import guepardoapps.lucahome.common.dto.MenuDto;
import guepardoapps.lucahome.common.service.MenuService;
import guepardoapps.lucahome.service.NavigationService;
import guepardoapps.lucahome.views.MenuEditActivity;

public class MenuListViewAdapter extends BaseAdapter {
    private class Holder {
        private TextView _titleText;
        private TextView _descriptionText;
        private TextView _dateText;
        private FloatingActionButton _clearButton;
        private FloatingActionButton _updateButton;
    }

    private static final String TAG = MenuListViewAdapter.class.getSimpleName();
    private Logger _logger;

    private Context _context;

    private MenuService _menuService;
    private NavigationService _navigationService;

    private static LayoutInflater _inflater = null;

    private SerializableList<LucaMenu> _listViewItems;

    public MenuListViewAdapter(@NonNull Context context, @NonNull SerializableList<LucaMenu> listViewItems) {
        _logger = new Logger(TAG);
        _logger.Debug("Created...");

        _context = context;

        _listViewItems = listViewItems;

        _menuService = MenuService.getInstance();
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

        View rowView = _inflater.inflate(R.layout.listview_card_menu, null);

        holder._titleText = rowView.findViewById(R.id.menuCardTitleText);
        holder._descriptionText = rowView.findViewById(R.id.menuDescriptionText);
        holder._dateText = rowView.findViewById(R.id.menuDateText);
        holder._clearButton = rowView.findViewById(R.id.menuCardClearButton);
        holder._updateButton = rowView.findViewById(R.id.menuCardUpdateButton);

        final LucaMenu menu = _listViewItems.getValue(index);

        holder._titleText.setText(menu.GetTitle());
        holder._descriptionText.setText(menu.GetDescription());
        holder._dateText.setText(menu.GetDate().DDMMYYYY());

        holder._clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug("_clearButton setOnClickListener onClick");

                boolean isLightTheme = ThemeManager.getInstance().getCurrentTheme() == 0;

                final Dialog deleteDialog = new Dialog(_context);
                deleteDialog
                        .title(String.format(Locale.getDefault(), "Clear menu for %s?", menu.GetDateString()))
                        .positiveAction("Clear")
                        .negativeAction("Cancel")
                        .applyStyle(isLightTheme ? R.style.SimpleDialogLight : R.style.SimpleDialog)
                        .setCancelable(true);

                deleteDialog.positiveActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        _menuService.ClearMenu(menu);
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

        holder._updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _logger.Debug(String.format(Locale.getDefault(), "setOnClickListener onClick for menu %s", menu));
                Bundle data = new Bundle();
                data.putSerializable(MenuService.MenuIntent, new MenuDto(menu.GetId(), menu.GetTitle(), menu.GetDescription(), menu.GetWeekday(), menu.GetDate()));
                _navigationService.NavigateToActivityWithData(_context, MenuEditActivity.class, data);
            }
        });

        return rowView;
    }
}
