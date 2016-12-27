package com.phonenumber.app.rest;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.phonenumber.app.crypto.DecypherHelper;
import com.phonenumber.app.json.JacksonRequest;
import com.phonenumber.app.json.Mapper;
import com.phonenumber.app.phonenumbers.data.PhoneNumbersResponse;

import rx.Observable;
import rx.Subscriber;

public class PhoneNumberClient {

    private static final String TAG = PhoneNumberClient.class.getSimpleName();

    private static final String PHONE_NUMBER_URL = "http://test.devel.siriomedia.com/telemarketing/numbers.json";
    private static final String RIVAL_PHONE_NUMBER_URL = "http://test.devel.siriomedia.com/telemarketing/data.php";

    private final RequestQueue requestQueue;

    public PhoneNumberClient(Context context) {
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public Observable<PhoneNumbersResponse[]> getPhoneNumbers() {
        return Observable.create(new Observable.OnSubscribe<PhoneNumbersResponse[]>() {
            @Override
            public void call(final Subscriber<? super PhoneNumbersResponse[]> subscriber) {
                final Response.Listener responseListener = new Response.Listener<PhoneNumbersResponse[]>() {
                    @Override
                    public void onResponse(PhoneNumbersResponse[] response) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(response);
                            subscriber.onCompleted();
                        }
                    }
                };
                final Response.ErrorListener errorListener = error -> {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(error);
                        subscriber.onCompleted();
                    }
                };
                final String url = PHONE_NUMBER_URL;
                final JacksonRequest<PhoneNumbersResponse[]> request = new JacksonRequest<>(
                        Request.Method.GET, url, null, PhoneNumbersResponse[].class, responseListener, errorListener);
                requestQueue.add(request);
            }
        });
    }

    public Observable<PhoneNumbersResponse[]> getRivalsPhoneNumbers() {
        return Observable.create(subscriber -> {
            final StringRequest stringRequest = new StringRequest(Request.Method.GET, RIVAL_PHONE_NUMBER_URL,
                    response -> {
                        if (!subscriber.isUnsubscribed()) {

                            final String decodedResponse = DecypherHelper.decypherString(response);
                            try {
                                final PhoneNumbersResponse[] phoneNumbersResponses = Mapper.objectOrThrow(decodedResponse, PhoneNumbersResponse[].class);
                                subscriber.onNext(phoneNumbersResponses);
                                subscriber.onCompleted();
                            } catch (Exception e) {
                                subscriber.onError(e);
                            }
                        }
                    }, subscriber::onError);

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        });
    }

}