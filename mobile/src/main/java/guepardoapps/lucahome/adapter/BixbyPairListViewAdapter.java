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

import guepardoapps.bixby.classes.BixbyPair;
import guepardoapps.bixby.classes.requirements.BixbyRequirement;

import guepardoapps.bixby.services.BixbyPairService;
import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.basic.utils.Logger;
import guepardoapps.lucahome.service.NavigationService;
import guepardoapps.lucahome.views.BixbyEditActivity;

@SuppressWarnings("WeakerAccess")
public class BixbyPairListViewAdapter extends BaseAdapter {
    private static final String TAG = BixbyPairListViewAdapter.class.getSimpleName();

    private class Holder {
        public static final int MAX_REQUIREMENTS = 5;

        private TextView _actionInformationTextView;
        private TextView[] _requirementInformationTextViewArray = new TextView[MAX_REQUIREMENTS];

        private FloatingActionButton _updateButton;
        private FloatingActionButton _deleteButton;

        private void navigateToEditActivity(@NonNull final BixbyPair bixbyPair) {
            Bundle data = new Bundle();
            bixbyPair.SetDatabaseAction(BixbyPair.DatabaseAction.Update);
            data.putSerializable(BixbyPairService.BIXBY_PAIR_INTENT, bixbyPair);
            NavigationService.getInstance().NavigateToActivityWithData(_context, BixbyEditActivity.class, data);
        }

        private void displayDeleteDialog(@NonNull final BixbyPair bixbyPair) {
            final Dialog deleteDialog = new Dialog(_context);

            deleteDialog
                    .title(String.format(Locale.getDefault(), "Delete %d?", bixbyPair.GetActionId()))
                    .positiveAction("Delete")
                    .negativeAction("Cancel")
                    .applyStyle(_isLightTheme ? R.style.SimpleDialogLight : R.style.SimpleDialog)
                    .setCancelable(true);

            deleteDialog.positiveActionClickListener(view -> {
                BixbyPairService.getInstance().DeleteBixbyPair(bixbyPair);
                deleteDialog.dismiss();
            });

            deleteDialog.negativeActionClickListener(view -> deleteDialog.dismiss());

            deleteDialog.show();
        }
    }

    private Context _context;
    private SerializableList<BixbyPair> _listViewItems;

    private static LayoutInflater _inflater = null;
    private boolean _isLightTheme;

    public BixbyPairListViewAdapter(@NonNull Context context, @NonNull SerializableList<BixbyPair> listViewItems) {
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
        int requirementIndex;

        View rowView = _inflater.inflate(R.layout.listview_card_bixbypair, null);

        holder._actionInformationTextView = rowView.findViewById(R.id.bixbyPair_card_actionInformation_text_view);

        holder._requirementInformationTextViewArray[0] = rowView.findViewById(R.id.bixbyPair_requirement1_text_view);
        holder._requirementInformationTextViewArray[1] = rowView.findViewById(R.id.bixbyPair_requirement2_text_view);
        holder._requirementInformationTextViewArray[2] = rowView.findViewById(R.id.bixbyPair_requirement3_text_view);
        holder._requirementInformationTextViewArray[3] = rowView.findViewById(R.id.bixbyPair_requirement4_text_view);
        holder._requirementInformationTextViewArray[4] = rowView.findViewById(R.id.bixbyPair_requirement5_text_view);

        holder._updateButton = rowView.findViewById(R.id.birthday_card_update_button);
        holder._deleteButton = rowView.findViewById(R.id.birthday_card_delete_button);

        final BixbyPair bixbyPair = _listViewItems.getValue(index);

        holder._actionInformationTextView.setText(bixbyPair.GetAction().GetInformationString());

        SerializableList<BixbyRequirement> requirementList = bixbyPair.GetRequirements();
        for (requirementIndex = 0; requirementIndex < requirementList.getSize(); requirementIndex++) {
            holder._requirementInformationTextViewArray[requirementIndex].setText(requirementList.getValue(requirementIndex).GetInformationString());
        }

        if (requirementIndex + 1 < Holder.MAX_REQUIREMENTS) {
            Logger.getInstance().Debug(TAG, String.format(Locale.getDefault(), "Making TextViews gone! Only %d needed!", requirementIndex + 1));
            for (int emptyIndex = requirementIndex + 1; emptyIndex < Holder.MAX_REQUIREMENTS; emptyIndex++) {
                holder._requirementInformationTextViewArray[emptyIndex].setVisibility(View.GONE);
            }
        }

        holder._updateButton.setOnClickListener(view -> holder.navigateToEditActivity(bixbyPair));
        holder._deleteButton.setOnClickListener(view -> holder.displayDeleteDialog(bixbyPair));

        return rowView;
    }
}
