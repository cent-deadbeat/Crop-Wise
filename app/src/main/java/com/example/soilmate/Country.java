package com.example.soilmate;

public class Country {
    private String code;
    private int flagResId;

    public Country(String code, int flagResId) {
        this.code = code;
        this.flagResId = flagResId;
    }

    public String getCode() {
        return code;
    }

    public int getFlagResId() {
        return flagResId;
    }

    @Override
    public String toString() {
        return code; // Return the country code as the string representation
    }
}

