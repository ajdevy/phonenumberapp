package com.phonenumber.app.phonenumbers.dictionary;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.phonenumber.app.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class DictionaryHelper {

    private static final String TAG = DictionaryHelper.class.getName();

    private final Context context;
    private final Set<String> dictionary = new HashSet<>();
    private final Set<DictionaryEntry> numberDictionary = new HashSet<>();
    private AtomicBoolean loading = new AtomicBoolean(false);
    private AtomicBoolean loadingError = new AtomicBoolean(false);
    private PublishSubject<Void> loadingListener = PublishSubject.create();

    public DictionaryHelper(Context context) {
        this.context = context;
        loadAsync();
    }

    private void loadAsync() {
        loading.set(true);
        Observable
                .create((Observable.OnSubscribe<Void>) subscriber -> {
                    try {
                        load(context);
                        subscriber.onNext(null);
                    } catch (IOException e) {
                        subscriber.onError(e);
                        loadingError.set(true);
                    }
                    loading.set(false);
                    subscriber.onCompleted();
                })
                .doOnNext(loaded -> loadingListener.onNext(loaded))
                .subscribeOn(Schedulers.io())
                .subscribe(result -> {
                }, throwable -> Log.e(TAG, "could not load dictionary", throwable));
    }

    public boolean hasLoaded() {
        return !loading.get();
    }

    public Observable<Void> getDictionaryLoadedListener() {
        return loadingListener.asObservable();
    }

    public String getSentenceFromPhoneNumber(@Nullable String phoneNumber) {
        final char[] charBuffer = new char[1];
        for (DictionaryEntry numberFromDictionary : numberDictionary) {
            //optimization for search
            if (phoneNumber.length() > 0) {
                if (firstCharsMatch(phoneNumber, numberFromDictionary.number, charBuffer)) {
                    //if starts with one of the numbers from the dictionary
                    if (phoneNumber.startsWith(numberFromDictionary.number)) {
                        if (phoneNumber.length() == numberFromDictionary.number.length()) {
                            //if the same length as the number, just return the word
                            return numberFromDictionary.word;
                        } else {
                            //get first part of the word in the number
                            final String restOfPhoneNumber = phoneNumber.substring(numberFromDictionary.number.length(), phoneNumber.length());
                            //get the rest of the sentence
                            final String restOfSentence = getSentenceFromPhoneNumber(restOfPhoneNumber);
                            if (restOfPhoneNumber.length() == restOfSentence.length()) {
                                return numberFromDictionary.word + restOfSentence;
                            }
                        }
                    }
                }
            }
        }
        return "";
    }

    private boolean firstCharsMatch(@NonNull String firstNumber, @NonNull String secondNumber, @NonNull char[] charBuffer) {
        firstNumber.getChars(0, 1, charBuffer, 0);
        final char firstCharOfPhoneNumberToSearch = charBuffer[0];
        secondNumber.getChars(0, 1, charBuffer, 0);
        final char firstCharOfDictionaryPhoneNumber = charBuffer[0];
        return firstCharOfPhoneNumberToSearch == firstCharOfDictionaryPhoneNumber;
    }

    private char getKeypadNumber(char characterToConvert) {
        if (Character.isDigit(characterToConvert))
            return characterToConvert;
        else {
            switch (Character.toUpperCase(characterToConvert)) {
                case 'A':
                case 'B':
                case 'C':
                    return '2';
                case 'D':
                case 'E':
                case 'F':
                    return '3';
                case 'G':
                case 'H':
                case 'I':
                    return '4';
                case 'J':
                case 'K':
                case 'L':
                    return '5';
                case 'M':
                case 'N':
                case 'O':
                    return '6';
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                    return '7';
                case 'T':
                case 'U':
                case 'V':
                    return '8';
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                    return '9';
                default:
                    return '?';
            }
        }
    }

    private void load(Context context) throws IOException {
        final InputStream inputStream = context.getResources().openRawResource(R.raw.dictionary);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                dictionary.add(line);
                final String phoneNumber = convertToPhoneNumber(line);

                if (!TextUtils.isEmpty(phoneNumber)) {
                    numberDictionary.add(new DictionaryEntry(line, phoneNumber));
                }
            }
        } finally {
            reader.close();
            inputStream.close();
        }
    }

    private String convertToPhoneNumber(String word) {
        String phoneNumberFromWord = "";
        if (!TextUtils.isEmpty(word)) {
            for (int index = 0; index < word.length(); index++) {
                phoneNumberFromWord += getKeypadNumber(word.charAt(index));
            }
        }
        return phoneNumberFromWord;
    }

    private static class DictionaryEntry {
        String word;
        String number;

        public DictionaryEntry(String word, String number) {
            this.word = word;
            this.number = number;
        }

        @Override
        public String toString() {
            return "DictionaryEntry{" +
                    "word='" + word + '\'' +
                    ", number='" + number + '\'' +
                    '}';
        }
    }
}