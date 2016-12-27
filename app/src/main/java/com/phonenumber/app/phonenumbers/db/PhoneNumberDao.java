package com.phonenumber.app.phonenumbers.db;

import android.support.annotation.Nullable;
import android.util.Log;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class PhoneNumberDao extends BaseDaoImpl<PhoneNumberEntry, Integer> {

    private static final String TAG = PhoneNumberDao.class.getName();

    public PhoneNumberDao() throws SQLException {
        super(PhoneNumberEntry.class);
    }

    public PhoneNumberDao(Class<PhoneNumberEntry> dataClass) throws SQLException {
        super(dataClass);
    }

    public PhoneNumberDao(ConnectionSource connectionSource, Class<PhoneNumberEntry> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public PhoneNumberDao(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, PhoneNumberEntry.class);
    }

    public PhoneNumberDao(ConnectionSource connectionSource, DatabaseTableConfig<PhoneNumberEntry> tableConfig) throws SQLException {
        super(connectionSource, tableConfig);
    }

    public Observable<List<PhoneNumberEntry>> queryPhoneNumbers(@Nullable String text) {
        final String queryText = text == null ? "" : text;
        return Observable.fromCallable(() -> {
            try {
                return queryBuilder()
                        .where()
                        .like(PhoneNumberEntry.COLUMN_PHONE_NUMBER, "%" + queryText + "%")
                        .or()
                        .like(PhoneNumberEntry.COLUMN_PHONE_NUMBER_OWNER, "%" + queryText + "%")
                        .or()
                        .like(PhoneNumberEntry.COLUMN_PHONE_NUMBER_PRICE, "%" + queryText + "%")
                        .query();
            } catch (SQLException e) {
                Log.e(TAG, "could not query all phone numbers", e);
            }
            return new ArrayList<PhoneNumberEntry>();
        });
    }

    public Observable<Boolean> entryExists(String phoneNumber) {
        return Observable.fromCallable(() -> {
            final long recordCount = queryBuilder()
                    .where()
                    .like(PhoneNumberEntry.COLUMN_PHONE_NUMBER, phoneNumber)
                    .countOf();
            return recordCount > 0;
        });
    }
}