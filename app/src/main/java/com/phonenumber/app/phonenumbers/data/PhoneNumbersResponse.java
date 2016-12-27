package com.phonenumber.app.phonenumbers.data;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "phoneNumber",
        "phoneNumberPrice",
        "phoneNumberOwner"
})
public class PhoneNumbersResponse {

    @JsonProperty("phoneNumber")
    private String phoneNumber;
    @JsonProperty("phoneNumberPrice")
    private String phoneNumberPrice;
    @JsonProperty("phoneNumberOwner")
    private String phoneNumberOwner;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The phoneNumber
     */
    @JsonProperty("phoneNumber")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * @param phoneNumber The phoneNumber
     */
    @JsonProperty("phoneNumber")
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * @return The phoneNumberPrice
     */
    @JsonProperty("phoneNumberPrice")
    public String getPhoneNumberPrice() {
        return phoneNumberPrice;
    }

    /**
     * @param phoneNumberPrice The phoneNumberPrice
     */
    @JsonProperty("phoneNumberPrice")
    public void setPhoneNumberPrice(String phoneNumberPrice) {
        this.phoneNumberPrice = phoneNumberPrice;
    }

    /**
     * @return The phoneNumberOwner
     */
    @JsonProperty("phoneNumberOwner")
    public String getPhoneNumberOwner() {
        return phoneNumberOwner;
    }

    /**
     * @param phoneNumberOwner The phoneNumberOwner
     */
    @JsonProperty("phoneNumberOwner")
    public void setPhoneNumberOwner(String phoneNumberOwner) {
        this.phoneNumberOwner = phoneNumberOwner;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}