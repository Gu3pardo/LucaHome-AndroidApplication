package guepardoapps.lucahome.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rey.material.app.Dialog;
import com.rey.material.app.ThemeManager;
import com.rey.material.widget.FloatingActionButton;

import java.util.Locale;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.common.classes.LucaMenu;
import guepardoapps.lucahome.common.dto.MenuDto;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
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

        private void displayDeleteDialog(@NonNull final LucaMenu menu) {
            final Dialog deleteDialog = new Dialog(_context);

            deleteDialog
                    .title(String.format(Locale.getDefault(), "Clear menu for %s?", menu.GetDateString()))
                    .positiveAction("Clear")
                    .negativeAction("Cancel")
                    .applyStyle(_isLightTheme ? R.style.SimpleDialogLight : R.style.SimpleDialog)
                    .setCancelable(true);

            deleteDialog.positiveActionClickListener(view -> {
                _menuService.ClearMenu(menu);
                deleteDialog.dismiss();
            });

            deleteDialog.negativeActionClickListener(view -> deleteDialog.dismiss());

            deleteDialog.show();
        }
    }

    private Context _context;
    private MenuService _menuService;
    private NavigationService _navigationService;

    private SerializableList<LucaMenu> _listViewItems;

    private static LayoutInflater _inflater = null;
    private boolean _isLightTheme;

    public MenuListViewAdapter(@NonNull Context context, @NonNull SerializableList<LucaMenu> listViewItems) {
        _context = context;
        _menuService = MenuService.getInstance();
        _navigationService = NavigationService.getInstance();

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

        holder._clearButton.setOnClickListener(view -> holder.displayDeleteDialog(menu));

        holder._updateButton.setOnClickListener(view -> {
            Bundle data = new Bundle();
            data.putSerializable(MenuService.MenuIntent, new MenuDto(menu.GetId(), menu.GetTitle(), menu.GetDescription(), menu.GetWeekday(), menu.GetDate()));
            _navigationService.NavigateToActivityWithData(_context, MenuEditActivity.class, data);
        });

        rowView.setVisibility((menu.GetServerDbAction() == ILucaClass.LucaServerDbAction.Delete) ? View.GONE : View.VISIBLE);

        return rowView;
    }
}
