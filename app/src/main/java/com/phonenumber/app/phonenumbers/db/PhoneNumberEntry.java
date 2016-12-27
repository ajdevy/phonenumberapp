package com.phonenumber.app.phonenumbers.db;

import android.text.TextUtils;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.phonenumber.app.phonenumbers.data.PhoneNumbersResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;

@DatabaseTable(daoClass = PhoneNumberDao.class)
public class PhoneNumberEntry {

    private static final String TAG = PhoneNumberEntry.class.getName();

    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_PHONE_NUMBER_OWNER = "phone_number_owner";
    public static final String COLUMN_PHONE_NUMBER_PRICE = "phone_number_price";

    @DatabaseField(generatedId = true)
    private long id;
    @DatabaseField(index = true, unique = true, columnName = COLUMN_PHONE_NUMBER)
    private String phoneNumber;
    @DatabaseField(index = true, columnName = COLUMN_PHONE_NUMBER_PRICE)
    private String price;
    @DatabaseField(index = true, columnName = COLUMN_PHONE_NUMBER_OWNER)
    private String ownerEmail;
    @DatabaseField(index = true)
    private String phoneNumberWord;
    private BigDecimal priceNumber;

    public PhoneNumberEntry() {
    }

    public PhoneNumberEntry(PhoneNumbersResponse phoneNumberResponse) {
        this.phoneNumber = phoneNumberResponse.getPhoneNumber();
        this.price = phoneNumberResponse.getPhoneNumberPrice();
        this.ownerEmail = phoneNumberResponse.getPhoneNumberOwner();
        this.phoneNumberWord = "";
        init();
    }

    public PhoneNumberEntry(String phoneNumber, String price, String ownerEmail) {
        this.phoneNumber = phoneNumber;
        this.price = price;
        this.ownerEmail = ownerEmail;
        this.phoneNumberWord = "";
        init();
    }

    public PhoneNumberEntry(PhoneNumbersResponse phoneNumberResponse, String phoneNumberWord) {
        this.phoneNumber = phoneNumberResponse.getPhoneNumber();
        this.price = phoneNumberResponse.getPhoneNumberPrice();
        this.ownerEmail = phoneNumberResponse.getPhoneNumberOwner();
        this.phoneNumberWord = phoneNumberWord;
        init();
    }

    public PhoneNumberEntry(String phoneNumber, String price, String ownerEmail, String phoneNumberWord) {
        this.phoneNumber = phoneNumber;
        this.price = price;
        this.ownerEmail = ownerEmail;
        this.phoneNumberWord = phoneNumberWord;
        init();
    }

    private void init() {
        initPriceNumber();
    }

    private void initPriceNumber() {
        priceNumber = createPriceNumber();
    }

    public String getPrice() {
        return price;
    }

    public BigDecimal createPriceNumber() {
        try {
            return new BigDecimal(price.replaceAll("[^-?0-9]+", ""));
        } catch (NumberFormatException ignored) {
        }
        return new BigDecimal(0);
    }

    public BigDecimal getPriceNumberSortValue() {
        if (priceNumber == null) {
            initPriceNumber();
        }
        final BigDecimal result;
        if (!TextUtils.isEmpty(phoneNumberWord)) {
            result = new BigDecimal(100000)
                    .multiply(new BigDecimal(phoneNumberWord.length()))
                    .divide(priceNumber, RoundingMode.HALF_EVEN);
        } else {
            result = new BigDecimal(1000)
                    .divide(priceNumber, RoundingMode.HALF_EVEN);
        }
        return result;
    }

    public void setPrice(String price) {
        this.price = price;
        initPriceNumber();
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumberWord() {
        return phoneNumberWord;
    }

    public void setPhoneNumberWord(String phoneNumberWord) {
        this.phoneNumberWord = phoneNumberWord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhoneNumberEntry)) return false;

        PhoneNumberEntry that = (PhoneNumberEntry) o;

        return phoneNumber != null ? phoneNumber.equals(that.phoneNumber) : that.phoneNumber == null;
    }

    @Override
    public int hashCode() {
        return phoneNumber != null ? phoneNumber.hashCode() : 0;
    }

    @Override
    public String toString() {
        return phoneNumber;
    }
}