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
import guepardoapps.lucahome.common.classes.PuckJs;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.service.PuckJsListService;
import guepardoapps.lucahome.service.NavigationService;
import guepardoapps.lucahome.views.PuckJsEditActivity;

public class PuckJsListViewAdapter extends BaseAdapter {
    private class Holder {
        private TextView _nameText;
        private TextView _areaText;
        private TextView _macText;
        private FloatingActionButton _updateButton;
        private FloatingActionButton _deleteButton;

        private void navigateToEditActivity(@NonNull final PuckJs puckJs) {
            Bundle data = new Bundle();
            puckJs.SetServerDbAction(PuckJs.LucaServerDbAction.Update);
            data.putSerializable(PuckJsListService.PuckJsIntent, puckJs);
            NavigationService.getInstance().NavigateToActivityWithData(_context, PuckJsEditActivity.class, data);
        }

        private void displayDeleteDialog(@NonNull final PuckJs puckJs) {
            final Dialog deleteDialog = new Dialog(_context);

            deleteDialog
                    .title(String.format(Locale.getDefault(), "Delete %s?", puckJs.GetName()))
                    .positiveAction("Delete")
                    .negativeAction("Cancel")
                    .applyStyle(_isLightTheme ? R.style.SimpleDialogLight : R.style.SimpleDialog)
                    .setCancelable(true);

            deleteDialog.positiveActionClickListener(view -> {
                PuckJsListService.getInstance().DeletePuckJs(puckJs);
                deleteDialog.dismiss();
            });

            deleteDialog.negativeActionClickListener(view -> deleteDialog.dismiss());

            deleteDialog.show();
        }
    }

    private Context _context;
    private SerializableList<PuckJs> _listViewItems;

    private static LayoutInflater _inflater = null;
    private boolean _isLightTheme;

    public PuckJsListViewAdapter(@NonNull Context context, @NonNull SerializableList<PuckJs> listViewItems) {
        _context = context;
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

        View rowView = _inflater.inflate(R.layout.listview_card_puckjs, null);

        holder._nameText = rowView.findViewById(R.id.puckJsCardNameText);
        holder._areaText = rowView.findViewById(R.id.puckJsAreaText);
        holder._macText = rowView.findViewById(R.id.puckJsMacText);
        holder._updateButton = rowView.findViewById(R.id.puckjs_card_update_button);
        holder._deleteButton = rowView.findViewById(R.id.puckjs_card_delete_button);

        final PuckJs puckJs = _listViewItems.getValue(index);

        holder._nameText.setText(puckJs.GetName());
        holder._areaText.setText(puckJs.GetArea());
        holder._macText.setText(puckJs.GetMac());

        holder._updateButton.setOnClickListener(view -> holder.navigateToEditActivity(puckJs));
        holder._deleteButton.setOnClickListener(view -> holder.displayDeleteDialog(puckJs));

        rowView.setVisibility((puckJs.GetServerDbAction() == ILucaClass.LucaServerDbAction.Delete) ? View.GONE : View.VISIBLE);

        return rowView;
    }
}
