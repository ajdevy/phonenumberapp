package com.phonenumber.app.inject;

import android.app.Application;

import com.j256.ormlite.dao.DaoManager;
import com.phonenumber.app.db.DbHelper;
import com.phonenumber.app.phonenumbers.data.PhoneNumberController;
import com.phonenumber.app.phonenumbers.db.PhoneNumberDao;
import com.phonenumber.app.phonenumbers.db.PhoneNumberEntry;
import com.phonenumber.app.phonenumbers.dictionary.DictionaryHelper;
import com.phonenumber.app.rest.PhoneNumberClient;

import java.sql.SQLException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppInjectionModule {

    private Application mApplication;

    public AppInjectionModule(Application application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    Application providesApplication() {
        return mApplication;
    }

    @Provides
    @Singleton
    PhoneNumberClient providePhoneNumberClient(Application app) {
        return new PhoneNumberClient(app);
    }

    @Provides
    @Singleton
    PhoneNumberController providePhoneNumberController(Application app, PhoneNumberDao phoneNumberDao,
                                                       PhoneNumberClient phoneNumberClient, DictionaryHelper dictionaryHelper) {
        return new PhoneNumberController(app, phoneNumberClient, phoneNumberDao, dictionaryHelper);
    }

    @Provides
    @Singleton
    DbHelper provideDbHelper(Application app) {
        return new DbHelper(app);
    }

    @Provides
    @Singleton
    DictionaryHelper provideDictionaryHelper(Application app) {
        return new DictionaryHelper(app);
    }

    @Provides
    @Singleton
    PhoneNumberDao providePhoneNumberDao(DbHelper dbHelper) {
        try {
            return DaoManager.createDao(dbHelper.getConnectionSource(), PhoneNumberEntry.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}