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
import com.example.myidealclassapp.Classes.Employees;
import com.example.myidealclassapp.R;

import java.util.List;

public class ParentTeacherAdapter extends RecyclerView.Adapter<ParentTeacherAdapter.ViewHolder> {

    private List<Employees> teacherList;
    private Context context;

    public ParentTeacherAdapter(Context context, List<Employees> teacherList) {
        this.context = context;
        this.teacherList = teacherList;
    }
 
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView foto_teacher;
        TextView subject_teacher, teacherfirstname, teachersecondname, teacherthirsdname;
        TextView Specialty_Teacher, Expirience, Title_teacher, Professional_development;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            foto_teacher = itemView.findViewById(R.id.foto_teacher);
            teacherfirstname = itemView.findViewById(R.id.teacherfirstname);
            teachersecondname = itemView.findViewById(R.id.teachersecondname);
            teacherthirsdname = itemView.findViewById(R.id.teacherthirsdname);
            Specialty_Teacher = itemView.findViewById(R.id.Specialty_Teacher);
            Expirience = itemView.findViewById(R.id.Expirience);
            Title_teacher = itemView.findViewById(R.id.Title_teacher);
            Professional_development = itemView.findViewById(R.id.Professional_development);
        }
    }

    @NonNull
    @Override
    public ParentTeacherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_parent_teacher, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ParentTeacherAdapter.ViewHolder holder, int position) {
        Employees teacher = teacherList.get(position);
        holder.teacherfirstname.setText(teacher.getFirstName());
        holder.teachersecondname.setText(teacher.getLastName());
        holder.teacherthirsdname.setText(teacher.getMiddleName());
        holder.Specialty_Teacher.setText(teacher.getSpecialty());
        holder.Expirience.setText(String.valueOf(teacher.getExperience()));
        holder.Title_teacher.setText(teacher.getCertificate());
        holder.Professional_development.setText(teacher.getQualification());

        String imageData = teacher.getImageBase64();
        if (imageData != null && !imageData.trim().isEmpty()) {
            if (imageData.startsWith("http")) {
                // Загрузка из URL с помощью Glide
                Glide.with(context)
                        .load(imageData)
                        .placeholder(android.R.drawable.ic_menu_report_image)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(holder.foto_teacher);
            } else {
                // Загрузка из Base64
                try {
                    byte[] decoded = Base64.decode(imageData, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                    holder.foto_teacher.setImageBitmap(bitmap);
                } catch (Exception e) {
                    holder.foto_teacher.setImageResource(android.R.drawable.ic_menu_report_image);
                }
            }
        } else {
            holder.foto_teacher.setImageResource(android.R.drawable.ic_menu_report_image);
        }
    }


    @Override
    public int getItemCount() {
        return teacherList.size();
    }
}
