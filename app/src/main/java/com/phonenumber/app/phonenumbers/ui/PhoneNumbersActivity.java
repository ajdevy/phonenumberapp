package com.phonenumber.app.phonenumbers.ui;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.phonenumber.app.PhoneNumberSearchApp;
import com.phonenumber.app.R;
import com.phonenumber.app.db.DbHelper;
import com.phonenumber.app.phonenumbers.PhoneNumberSearchSuggestionProvider;
import com.phonenumber.app.phonenumbers.data.PhoneNumberController;
import com.phonenumber.app.phonenumbers.db.PhoneNumberEntry;
import com.phonenumber.app.phonenumbers.dictionary.DictionaryHelper;
import com.phonenumber.app.util.UiAvailabilityChecker;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PhoneNumbersActivity extends RxAppCompatActivity {

    private static final String TAG = PhoneNumbersActivity.class.getSimpleName();

    @Inject
    PhoneNumberController phoneNumberController;
    @Inject
    DictionaryHelper dictionaryHelper;

    @Bind(R.id.phone_numbers_recycler_view)
    RecyclerView phoneNumberRecyclerView;
    @Bind(R.id.search_view)
    MaterialSearchView searchView;

    private PhoneNumberAdapter phoneNumberAdapter;
    private List<PhoneNumberEntry> phoneNumbersResponses = new ArrayList<>();
    private SearchRecentSuggestions suggestions;
    private SearchView menuSearchView;
    private DbChangeReceiver dbChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_numbers);

        ((PhoneNumberSearchApp) getApplication()).getInjector().inject(this);
        ButterKnife.bind(this);

        setupPhoneNumberAdapter();
        setupSearch();

        handleSearchActionOnStart(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();

        dbChangeReceiver = new DbChangeReceiver(this);
        final IntentFilter intentFilter = new IntentFilter(DbChangeReceiver.ACTION_NUMBERS_ADDED);
        LocalBroadcastManager.getInstance(this).registerReceiver(dbChangeReceiver, intentFilter);

        phoneNumberController.syncPhoneNumbers()
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                }, throwable -> Log.e(TAG, "could not sync phone numbers", throwable));

        refreshNumbers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dbChangeReceiver);
    }

    private void setupPhoneNumberAdapter() {
        phoneNumberAdapter = new PhoneNumberAdapter(this, phoneNumbersResponses);
        phoneNumberAdapter.setHasStableIds(true);
        phoneNumberRecyclerView.setAdapter(phoneNumberAdapter);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        phoneNumberRecyclerView.setLayoutManager(layoutManager);
    }

    private void setupSearch() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.setDefaultKeyMode(Activity.DEFAULT_KEYS_SEARCH_LOCAL);

        suggestions = new SearchRecentSuggestions(this, PhoneNumberSearchSuggestionProvider.AUTHORITY,
                PhoneNumberSearchSuggestionProvider.MODE);

        searchView.setVoiceSearch(false);
        searchView.setEllipsize(true);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchForPhoneNumbers(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        final String searchQuery = handleSearchAction(intent);
        if (!TextUtils.isEmpty(searchQuery)) {
            setSearchQueryAndDismissSuggestions(searchQuery);
        }
    }

    private void setSearchQueryAndDismissSuggestions(@NonNull String searchQuery) {
        menuSearchView.setQuery(searchQuery, false);
        menuSearchView.clearFocus();
        searchView.dismissSuggestions();
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            final List<String> searchMatches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            handleSearchMatches(searchMatches);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSearchMatches(List<String> searchMatches) {
        if (searchMatches != null && searchMatches.size() > 0) {
            final String searchQuery = searchMatches.get(0);
            setSearchQuery(searchQuery);
        }
    }

    private void setSearchQuery(@Nullable String searchQuery) {
        if (!TextUtils.isEmpty(searchQuery)) {
            searchView.setQuery(searchQuery, false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photos, menu);
        setupSearchMenuItem(menu.findItem(R.id.action_search));
        return true;
    }

    private void setupSearchMenuItem(MenuItem item) {
        menuSearchView = null;
        if (item != null) {
            menuSearchView = (SearchView) item.getActionView();
        }
        if (menuSearchView != null) {
            final SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
            menuSearchView.setSearchableInfo(searchManager.getSearchableInfo(this.getComponentName()));
        }
        searchView.setMenuItem(item);
    }

    private String handleSearchAction(@Nullable Intent intent) {
        if (intent != null && Intent.ACTION_SEARCH.equals(intent.getAction())) {
            final String query = intent.getStringExtra(SearchManager.QUERY);
            searchForPhoneNumbers(query);
            return query;
        }
        return "";
    }

    private String handleSearchActionOnStart(@Nullable Intent intent) {
        if (intent != null && Intent.ACTION_SEARCH.equals(intent.getAction())) {
            return handleSearchAction(intent);
        } else {
            loadPhoneNumbers("");
        }
        return "";
    }

    private void searchForPhoneNumbers(String query) {
        searchView.closeSearch();
        suggestions.saveRecentQuery(query, null);
        loadPhoneNumbers(query);
    }

    public void loadPhoneNumbers(String text) {
        phoneNumberController.getPhoneNumbers(text)
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::clearAndDisplayPhoneNumbers,
                        error -> Log.e(TAG, "got an error while loading phone numbers", error));
    }

    private void clearAndDisplayPhoneNumbers(List<PhoneNumberEntry> phoneNumbersResponses) {
        if (phoneNumberAdapter != null && phoneNumberRecyclerView != null) {
            phoneNumberAdapter.clear();
            phoneNumberRecyclerView.scrollToPosition(0);
            if (phoneNumbersResponses != null) {
                phoneNumberAdapter.addAll(phoneNumbersResponses);
            }
            phoneNumberAdapter.sort();
            phoneNumberAdapter.notifyDataSetChanged();
        }
    }

    private void displayPhoneNumbers(List<PhoneNumberEntry> phoneNumbersResponses) {
        if (phoneNumberAdapter != null && phoneNumberRecyclerView != null) {
            if (phoneNumbersResponses != null) {
                phoneNumberAdapter.addAll(phoneNumbersResponses);
            }
            phoneNumberAdapter.sort();
            phoneNumberAdapter.notifyDataSetChanged();
        }
    }

    @OnClick(R.id.sort_button)
    public void onSortClicked(View view) {
        phoneNumberAdapter.toggleAscDscSort();
    }

    @OnClick(R.id.add_new_entry_button)
    public void onAddNewEntryClicked(View view) {
        showAddNewEntryDialog();
    }

    private void showAddNewEntryDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.add_new_entry);

        final LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View dialogView = layoutInflater.inflate(R.layout.dialog_create_new_entry, null);
        final EditText phoneNumberEditText = (EditText) dialogView.findViewById(R.id.phone_number_edit_text);
        final EditText ownerEditText = (EditText) dialogView.findViewById(R.id.email_edit_text);
        final EditText priceEditText = (EditText) dialogView.findViewById(R.id.price_edit_text);
        alertDialog.setView(dialogView);

        alertDialog.setPositiveButton(R.string.create,
                (dialog, which) -> {
                    final String phoneNumber = phoneNumberEditText.getText().toString();
                    final String price = priceEditText.getText().toString();
                    final String owner = ownerEditText.getText().toString();
                    final PhoneNumberEntry phoneNumberEntry = new PhoneNumberEntry(
                            phoneNumber, price, owner, dictionaryHelper.getSentenceFromPhoneNumber(phoneNumber));
                    phoneNumberController.createNewPhoneNumber(phoneNumberEntry)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(result -> {
                                        Toast.makeText(this, R.string.entry_created, Toast.LENGTH_SHORT).show();
                                        phoneNumberAdapter.add(phoneNumberEntry);
                                        phoneNumberAdapter.sort();
                                    }
                                    , throwable -> {
                                        if (DbHelper.isUniqueConstraintViolation(throwable)) {
                                            Toast.makeText(this, R.string.entry_already_exists, Toast.LENGTH_SHORT).show();
                                        } else {
                                            Log.e(TAG, "could not create new entry", throwable);
                                        }
                                    });
                });

        alertDialog.setNegativeButton(R.string.cancel,
                (dialog, which) -> dialog.cancel());

        alertDialog.show();
    }

    private void refreshNumbers() {
        String searchQuery = null;
        if (menuSearchView != null) {
            searchQuery = menuSearchView.getQuery().toString();
        }
        phoneNumberController.getPhoneNumbers(searchQuery)
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::displayPhoneNumbers,
                        error -> Log.e(TAG, "got an error while loading phone numbers", error));
    }

    public static class DbChangeReceiver extends BroadcastReceiver {

        public static final String ACTION_NUMBERS_ADDED = "ACTION_NUMBERS_ADDED";
        private final WeakReference<PhoneNumbersActivity> activityWeakReference;

        public DbChangeReceiver(PhoneNumbersActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && ACTION_NUMBERS_ADDED.equals(intent.getAction())) {
                final PhoneNumbersActivity phoneNumbersActivity = activityWeakReference.get();
                if (UiAvailabilityChecker.isUiAvailable(phoneNumbersActivity)) {
                    phoneNumbersActivity.refreshNumbers();
                }
            }
        }
    }
}