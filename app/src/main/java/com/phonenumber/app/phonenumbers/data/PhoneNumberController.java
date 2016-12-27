package com.phonenumber.app.phonenumbers.data;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.phonenumber.app.db.DbHelper;
import com.phonenumber.app.phonenumbers.db.PhoneNumberDao;
import com.phonenumber.app.phonenumbers.db.PhoneNumberEntry;
import com.phonenumber.app.phonenumbers.dictionary.DictionaryHelper;
import com.phonenumber.app.phonenumbers.ui.PhoneNumbersActivity;
import com.phonenumber.app.rest.PhoneNumberClient;

import java.sql.SQLException;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

public class PhoneNumberController {

    private static final String TAG = PhoneNumberController.class.getName();

    private Context context;
    private PhoneNumberClient phoneNumberClient;
    private PhoneNumberDao phoneNumberDao;
    private DictionaryHelper dictionaryHelper;

    public PhoneNumberController(Context context, PhoneNumberClient phoneNumberClient, PhoneNumberDao phoneNumberDao, DictionaryHelper dictionaryHelper) {
        this.context = context;
        this.phoneNumberClient = phoneNumberClient;
        this.phoneNumberDao = phoneNumberDao;
        this.dictionaryHelper = dictionaryHelper;
    }

    public Observable<Void> syncPhoneNumbers() {
        return Observable.just(dictionaryHelper.hasLoaded())
                .flatMap(hasLoaded -> {
                    if (hasLoaded) {
                        return Observable.just(null);
                    } else {
                        return dictionaryHelper.getDictionaryLoadedListener();
                    }
                })
                .flatMap(result -> Observable.merge(phoneNumberClient.getPhoneNumbers(),
                        phoneNumberClient.getRivalsPhoneNumbers()))
                .observeOn(Schedulers.io())
                .flatMap(Observable::from)
                //buffer by 100 items to save and notify UI
                .buffer(100)
                .flatMap(this::savePhoneNumberBatchAndNotify)
                .lastOrDefault(null);
    }

    private Observable<Void> savePhoneNumberBatchAndNotify(List<PhoneNumbersResponse> phoneNumberResponses) {
        return Observable.just(phoneNumberResponses)
                .observeOn(Schedulers.io())
                .flatMap(Observable::from)
                .flatMap(phoneNumberResponse -> phoneNumberDao
                        .entryExists(phoneNumberResponse.getPhoneNumber())
                        .filter(phoneNumberResponseExists -> !phoneNumberResponseExists)
                        .map(result -> phoneNumberResponse))
                .flatMap(phoneNumberResponse -> {
                    try {
                        //TODO: check if exists in db?
                        phoneNumberDao.createIfNotExists(new PhoneNumberEntry(phoneNumberResponse
                                , dictionaryHelper.getSentenceFromPhoneNumber(phoneNumberResponse.getPhoneNumber())));
                        return Observable.just(null);
                    } catch (SQLException e) {
                        if (!DbHelper.isUniqueConstraintViolation(e)) {
                            Log.e(TAG, "could not save synced phone numbers", e);
                        }
                    }
                    return Observable.<Void>empty();
                })
                .lastOrDefault(null)
                .doOnNext(result -> notifyPhoneNumbersAdded());
    }

    private void notifyPhoneNumbersAdded() {
        LocalBroadcastManager.getInstance(context).sendBroadcast(
                new Intent(PhoneNumbersActivity.DbChangeReceiver.ACTION_NUMBERS_ADDED));
    }

    public Observable<List<PhoneNumberEntry>> getPhoneNumbers(String text) {
        return phoneNumberDao.queryPhoneNumbers(text);
    }

    public Observable<Integer> createNewPhoneNumber(PhoneNumberEntry phoneNumberEntry) {
        return Observable.fromCallable(() -> phoneNumberDao.create(phoneNumberEntry));
    }
}