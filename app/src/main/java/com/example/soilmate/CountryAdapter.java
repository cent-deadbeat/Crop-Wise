package com.example.soilmate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CountryAdapter extends ArrayAdapter<Country> {
    private Context context;
    private List<Country> countries;

    public CountryAdapter(Context context, List<Country> countries) {
        super(context, R.layout.spinner_item, countries);
        this.context = context;
        this.countries = countries;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.spinner_item, parent, false);

        // Get views
        ImageView flagIcon = row.findViewById(R.id.flagIcon);
        TextView countryCode = row.findViewById(R.id.countryCode);

        // Set data
        Country country = countries.get(position);
        flagIcon.setImageResource(country.getFlagResId());
        countryCode.setText(country.getCode());

        return row;
    }
}
