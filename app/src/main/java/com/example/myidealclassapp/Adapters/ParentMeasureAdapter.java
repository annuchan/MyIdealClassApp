package com.example.myidealclassapp.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myidealclassapp.Classes.Measure;
import com.example.myidealclassapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ParentMeasureAdapter extends RecyclerView.Adapter<ParentMeasureAdapter.ParentMeasureViewHolder> {

    private List<Measure> measureList;
    private Context context;

    public ParentMeasureAdapter(Context context, List<Measure> measureList) {
        this.context = context;
        this.measureList = measureList;
    }

    public static class ParentMeasureViewHolder extends RecyclerView.ViewHolder {
        ImageView imageEvent;
        TextView titleEvent, descriptionEvent, typeEvent, dateTimeEvent;

        public ParentMeasureViewHolder(@NonNull View itemView) {
            super(itemView);
            imageEvent = itemView.findViewById(R.id.imageevent);
            titleEvent = itemView.findViewById(R.id.titleEvent);
            descriptionEvent = itemView.findViewById(R.id.DescriptionEvent);
            typeEvent = itemView.findViewById(R.id.TypeEvent);
            dateTimeEvent = itemView.findViewById(R.id.DateTimeEvent);
        }
    }

    @NonNull
    @Override
    public ParentMeasureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_parent_measure, parent, false);
        return new ParentMeasureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParentMeasureViewHolder holder, int position) {
        Measure measure = measureList.get(position);

        holder.titleEvent.setText(measure.getTitle());
        holder.descriptionEvent.setText(measure.getDescribe());
        holder.typeEvent.setText(measure.getType_Measure());

        // Преобразование даты
        String formattedDate = formatDate(measure.getDate_Measure());
        holder.dateTimeEvent.setText(formattedDate);

        // Загрузка изображения: URL или Base64
        String imageData = measure.getImageBase64();
        if (imageData != null && !imageData.trim().isEmpty()) {
            if (imageData.startsWith("http")) {
                // Это URL — загружаем через Glide
                Glide.with(context)
                        .load(imageData)
                        .placeholder(android.R.drawable.ic_menu_report_image)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(holder.imageEvent);
            } else {
                // Это Base64
                try {
                    byte[] decodedBytes = Base64.decode(imageData, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    if (bitmap != null) {
                        holder.imageEvent.setImageBitmap(bitmap);
                    } else {
                        holder.imageEvent.setImageResource(android.R.drawable.ic_menu_report_image);
                    }
                } catch (IllegalArgumentException e) {
                    holder.imageEvent.setImageResource(android.R.drawable.ic_menu_report_image);
                }
            }
        } else {
            holder.imageEvent.setImageResource(android.R.drawable.ic_menu_report_image);
        }
    }

    private String formatDate(String input) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd.MM.yyyy H:mm", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm", new Locale("ru"));
            Date date = inputFormat.parse(input);
            return outputFormat.format(date);
        } catch (Exception e) {
            return input; // если ошибка — вернуть как есть
        }
    }


    @Override
    public int getItemCount() {
        return measureList.size();
    }
}
