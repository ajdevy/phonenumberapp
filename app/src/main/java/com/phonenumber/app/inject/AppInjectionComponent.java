package com.phonenumber.app.inject;

import com.phonenumber.app.phonenumbers.ui.PhoneNumbersActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppInjectionModule.class})
public interface AppInjectionComponent {

    void inject(PhoneNumbersActivity timeFragment);
}