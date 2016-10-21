package com.kojek.gasper.freeparkingberlin.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.vision.barcode.Barcode;

import java.util.List;

/**
 * Created by gaspe on 21. 10. 2016.
 */
public class StringSuggestion implements SearchSuggestion {
    private String text = "";
    private Address address;

    public StringSuggestion(String text) {
        this.text = text;
    }

    public StringSuggestion(Parcel source) {
        this.text = source.readString();
    }

    public StringSuggestion(Address address) {
        this.address = address;
        this.text = address.getAddressLine(0)
                + ","
                + address.getPostalCode()
                + " "
                + address.getLocality();
    }

    public static final Creator<StringSuggestion> CREATOR = new Creator<StringSuggestion>() {
        @Override
        public StringSuggestion createFromParcel(Parcel source) {
            return new StringSuggestion(source);
        }

        @Override
        public StringSuggestion[] newArray(int size) {
            return new StringSuggestion[size];
        }
    };

    @Override
    public String getBody() {
        return text;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
    }

    public Address getAddress() {
        return this.address;
    }

    public LatLng getLatLng() {
        if (address != null) {
            return new LatLng(address.getLatitude(), address.getLongitude());
        } else
            return null;
    }
}
