package com.phonenumber.app.phonenumbers.ui;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.phonenumber.app.R;
import com.phonenumber.app.phonenumbers.db.PhoneNumberEntry;
import com.phonenumber.app.ui.RecyclerArrayAdapter;
import com.phonenumber.app.ui.RecyclerUniqueArrayAdapter;

import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

class PhoneNumberAdapter extends RecyclerUniqueArrayAdapter<PhoneNumberEntry, PhoneNumberAdapter.PhoneNumberViewHolder> {

    private static final String TAG = PhoneNumberAdapter.class.getName();

    private final LayoutInflater layoutInflater;
    private Comparator<PhoneNumberEntry> phoneNumberComparator;
    private boolean currentlySortingAscending = true;

    PhoneNumberAdapter(Context context, List<PhoneNumberEntry> objects) {
        super(objects);
        this.layoutInflater = LayoutInflater.from(context);
        setupAscendingSort();
    }

    @Override
    public PhoneNumberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemLayout = layoutInflater.inflate(R.layout.item_phone_number, parent, false);
        return new PhoneNumberViewHolder(itemLayout);
    }

    @Override
    public void onBindViewHolder(PhoneNumberViewHolder holder, int position) {
        holder.bindView(getItem(position));
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    public void sort() {
        sort(phoneNumberComparator);
    }

    public void setupAscendingSort() {
        currentlySortingAscending = true;
        phoneNumberComparator = (lhs, rhs) -> lhs.getPriceNumberSortValue().compareTo(rhs.getPriceNumberSortValue());
    }

    public void setupDescendingSort() {
        currentlySortingAscending = false;
        phoneNumberComparator = (lhs, rhs) -> rhs.getPriceNumberSortValue().compareTo(lhs.getPriceNumberSortValue());
    }

    public void toggleAscDscSort() {
        if (!currentlySortingAscending) {
            setupAscendingSort();
        } else {
            setupDescendingSort();
        }
        sort();
        notifyDataSetChanged();
    }

    static class PhoneNumberViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.phone_number)
        TextView phoneNumber;
        @Bind(R.id.phone_number_word)
        TextView phoneNumberWord;
        @Bind(R.id.phone_number_price)
        TextView phoneNumberPrice;
        @Bind(R.id.phone_number_owner)
        TextView phoneNumberOwner;
        @Bind(R.id.phone_number_item_container)
        View viewContainer;

        PhoneNumberViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            viewContainer.setClickable(true);
        }

        void bindView(@NonNull PhoneNumberEntry phoneNumberEntry) {
            phoneNumber.setText(phoneNumberEntry.getPhoneNumber());
            phoneNumberOwner.setText(phoneNumberEntry.getOwnerEmail());
            phoneNumberPrice.setText(phoneNumberEntry.getPrice());
            phoneNumberWord.setText(phoneNumberEntry.getPhoneNumberWord());
            viewContainer.setOnClickListener(view -> openEmail(phoneNumberEntry));
        }

        private void openEmail(@NonNull PhoneNumberEntry phoneNumberEntry) {
            //only had the emulator on the weekend with me, so did not test on real phone
            //on the emulator this is not shown
            final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{phoneNumberEntry.getOwnerEmail()});
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Hey, I want to buy a phone number");
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "I see that you have the phone number I need!");
            itemView.getContext().startActivity(Intent.createChooser(emailIntent, "Send offer email"));
        }
    }
}