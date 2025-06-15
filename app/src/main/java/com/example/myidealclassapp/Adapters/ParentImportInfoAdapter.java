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
import com.example.myidealclassapp.Classes.Important_information;
import com.example.myidealclassapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ParentImportInfoAdapter extends RecyclerView.Adapter<ParentImportInfoAdapter.ParentImportInfoViewHolder> {

    private Context context;
    private List<Important_information> infoList;

    public ParentImportInfoAdapter(Context context, List<Important_information> infoList) {
        this.context = context;
        this.infoList = infoList;
    }

    public static class ParentImportInfoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView title, description, date;

        public ParentImportInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageImportant_information);
            title = itemView.findViewById(R.id.titleImportant_information);
            description = itemView.findViewById(R.id.DescriptionImportant_information);
            date = itemView.findViewById(R.id.DateImportant_information);
        }
    }

    @NonNull
    @Override
    public ParentImportInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_parent_important_information, parent, false);
        return new ParentImportInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParentImportInfoViewHolder holder, int position) {
        Important_information info = infoList.get(position);
        holder.title.setText(info.getTitle());
        holder.description.setText(info.getDescribe());
        holder.date.setText(formatDate(info.getDate_imp_info()));
        String imageData = info.getImageBase64();

        if (imageData == null ||
                imageData.trim().isEmpty() ||
                imageData.trim().equals("0")) {

            // Используем картинку по умолчанию ph_1
            holder.imageView.setImageResource(R.drawable.school3);
        } else if (imageData.startsWith("http")) {
            // Загрузка из URL с помощью Glide
            Glide.with(context)
                    .load(imageData)
                    .placeholder(R.drawable.school3)
                    .error(R.drawable.school3)
                    .into(holder.imageView);
        } else {
            // Загрузка из Base64
            try {
                byte[] bytes = Base64.decode(imageData, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.imageView.setImageResource(R.drawable.school3);
            }
        }
    }


    private String formatDate(String input) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd.MM.yyyy H:mm", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm", new Locale("ru"));
            Date date = inputFormat.parse(input);
            return outputFormat.format(date);
        } catch (Exception e) {
            return input;
        }
    }

    @Override
    public int getItemCount() {
        return infoList.size();
    }
}
