package com.example.myidealclassapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myidealclassapp.R;

import java.util.List;

public class SubjectAdapter extends ArrayAdapter<String> {

    private final LayoutInflater inflater;

    public SubjectAdapter(@NonNull Context context, @NonNull List<String> items) {
        super(context, 0, items);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_spinner, parent, false);
        }

        TextView textView = view.findViewById(R.id.text);
        String subject = getItem(position);
        if (subject != null) {
            textView.setText(subject);
        }

        return view;
    }
}
