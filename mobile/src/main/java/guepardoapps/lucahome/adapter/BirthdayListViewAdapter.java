package guepardoapps.lucahome.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import com.rey.material.app.Dialog;
import com.rey.material.app.ThemeManager;
import com.rey.material.widget.FloatingActionButton;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.basic.classes.SerializableList;
import guepardoapps.lucahome.common.classes.LucaBirthday;
import guepardoapps.lucahome.common.dto.BirthdayDto;
import guepardoapps.lucahome.common.interfaces.classes.ILucaClass;
import guepardoapps.lucahome.common.service.BirthdayService;
import guepardoapps.lucahome.service.NavigationService;
import guepardoapps.lucahome.views.BirthdayEditActivity;

public class BirthdayListViewAdapter extends BaseAdapter {
    private class Holder {
        private ImageView _birthdayImageView;
        private TextView _titleText;
        private TextView _dateText;
        private TextView _ageTextView;
        private TextView _groupText;
        private CheckBox _remindMeCheckBox;
        private FloatingActionButton _updateButton;
        private FloatingActionButton _deleteButton;

        private void navigateToEditActivity(@NonNull final LucaBirthday birthday) {
            Bundle data = new Bundle();
            data.putSerializable(BirthdayService.BirthdayIntent, new BirthdayDto(birthday.GetId(), birthday.GetName(), birthday.GetDate(), birthday.GetGroup(), birthday.GetRemindMe(), BirthdayDto.Action.Update));
            NavigationService.getInstance().NavigateToActivityWithData(_context, BirthdayEditActivity.class, data);
        }

        private void displayDeleteDialog(@NonNull final LucaBirthday birthday) {
            final Dialog deleteDialog = new Dialog(_context);

            deleteDialog
                    .title(String.format(Locale.getDefault(), "Delete %s?", birthday.GetName()))
                    .positiveAction("Delete")
                    .negativeAction("Cancel")
                    .applyStyle(_isLightTheme ? R.style.SimpleDialogLight : R.style.SimpleDialog)
                    .setCancelable(true);

            deleteDialog.positiveActionClickListener(view -> {
                BirthdayService.getInstance().DeleteBirthday(birthday);
                deleteDialog.dismiss();
            });

            deleteDialog.negativeActionClickListener(view -> deleteDialog.dismiss());

            deleteDialog.show();
        }
    }

    private Context _context;
    private SerializableList<LucaBirthday> _listViewItems;

    private static LayoutInflater _inflater = null;
    private boolean _isLightTheme;

    public BirthdayListViewAdapter(@NonNull Context context, @NonNull SerializableList<LucaBirthday> listViewItems) {
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

        View rowView = _inflater.inflate(R.layout.listview_card_birthday, null);

        holder._birthdayImageView = rowView.findViewById(R.id.birthday_card_image);
        holder._titleText = rowView.findViewById(R.id.birthday_card_title_text_view);
        holder._dateText = rowView.findViewById(R.id.birthday_date_text_view);
        holder._ageTextView = rowView.findViewById(R.id.birthday_age_text_view);
        holder._groupText = rowView.findViewById(R.id.birthday_group_text_view);
        holder._remindMeCheckBox = rowView.findViewById(R.id.birthday_remind_me_checkbox);
        holder._updateButton = rowView.findViewById(R.id.birthday_card_update_button);
        holder._deleteButton = rowView.findViewById(R.id.birthday_card_delete_button);

        final LucaBirthday birthday = _listViewItems.getValue(index);

        holder._birthdayImageView.setImageBitmap(birthday.GetPhoto());

        holder._titleText.setText(birthday.GetName());
        holder._dateText.setText(birthday.GetDate().DDMMYYYY());
        holder._ageTextView.setText(String.format(Locale.getDefault(), "%d years", birthday.GetAge()));

        if (birthday.HasBirthday()) {
            holder._titleText.setBackgroundColor(ContextCompat.getColor(_context, R.color.LightRed));
        }

        holder._groupText.setText(birthday.GetGroup());

        holder._remindMeCheckBox.setChecked(birthday.GetRemindMe());
        holder._remindMeCheckBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            birthday.SetRemindMe(isChecked);
            BirthdayService.getInstance().UpdateBirthday(birthday);
        });

        holder._updateButton.setOnClickListener(view -> holder.navigateToEditActivity(birthday));
        holder._deleteButton.setOnClickListener(view -> holder.displayDeleteDialog(birthday));

        rowView.setVisibility((birthday.GetServerDbAction() == ILucaClass.LucaServerDbAction.Delete) ? View.GONE : View.VISIBLE);

        return rowView;
    }
}
