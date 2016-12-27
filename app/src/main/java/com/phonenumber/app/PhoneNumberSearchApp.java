package com.phonenumber.app;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;
import com.phonenumber.app.inject.AppInjectionComponent;
import com.phonenumber.app.inject.AppInjectionModule;
import com.phonenumber.app.inject.DaggerAppInjectionComponent;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class PhoneNumberSearchApp extends Application {

    private AppInjectionComponent injectionComponent;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        createInjectionComponent();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setupImageLoader();
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build());
    }

    private void setupImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().
                cacheInMemory(true).cacheOnDisk(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);
    }

    public AppInjectionComponent getInjector() {
        return injectionComponent;
    }

    public void createInjectionComponent() {
        injectionComponent = DaggerAppInjectionComponent.builder()
                .appInjectionModule(new AppInjectionModule(this))
                .build();
    }
}