package com.example.myidealclassapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myidealclassapp.Classes.Measure;
import com.example.myidealclassapp.R;
import com.example.myidealclassapp.Admin.Admin_measure_edit;  // <-- поменяй на реальный класс, если он существуют
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminMeasureAdapter extends RecyclerView.Adapter<AdminMeasureAdapter.ViewHolder> {

    private final Context context;
    private final List<Measure> measureList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AdminMeasureAdapter(Context context, List<Measure> measureList) {
        this.context = context;
        this.measureList = measureList;
    }

    @NonNull
    @Override
    public AdminMeasureAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_teacher_admin_measure, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminMeasureAdapter.ViewHolder holder, int position) {
        Measure measure = measureList.get(position);

        holder.title.setText(measure.getTitle());
        holder.description.setText(measure.getDescribe());
        holder.type.setText(measure.getType_Measure());
        holder.dateTime.setText(measure.getDate_Measure());


        String imageData = measure.getImageBase64();

        // Если поле ImageBase64 равно "0", вставляем картинку ph_1
        if ("0".equals(imageData)) {
            imageData = "school3";
            measure.setImageBase64(imageData);
        }

        if (imageData.startsWith("http")) {
            Glide.with(context)
                    .load(imageData)
                    .placeholder(R.drawable.school2)
                    .error(R.drawable.school2)
                    .into(holder.imageEvent);
        } else if (imageData.startsWith("school3")) {
            int resId = context.getResources().getIdentifier(imageData, "drawable", context.getPackageName());
            if (resId != 0) {
                holder.imageEvent.setImageResource(resId);
            } else {
                holder.imageEvent.setImageResource(R.drawable.school2);
            }
        } else if (imageData.length() > 0) {
            try {
                byte[] decodedBytes = Base64.decode(imageData, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.imageEvent.setImageBitmap(decodedBitmap);
            } catch (Exception e) {
                holder.imageEvent.setImageResource(R.drawable.school2);
            }
        } else {
            holder.imageEvent.setImageResource(R.drawable.school2);
        }

        holder.detailsImage.setOnClickListener(v -> showPopupMenu(v, measure, position));

    }

    @Override
    public int getItemCount() {
        return measureList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, type, dateTime;
        ImageView imageEvent, detailsImage;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.titleEvent);
            description = itemView.findViewById(R.id.DescriptionEvent);
            type = itemView.findViewById(R.id.TypeEvent);
            dateTime = itemView.findViewById(R.id.DateTimeEvent);
            imageEvent = itemView.findViewById(R.id.imageevent);
            detailsImage = itemView.findViewById(R.id.detailsImageView);
        }
    }

    private void showPopupMenu(View anchorView, Measure measure, int position) {
        View popupView = LayoutInflater.from(context).inflate(R.layout.item_edit_delete, null);
        final PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setElevation(10);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(anchorView, -100, 0);

        LinearLayout edit = popupView.findViewById(R.id.edit);
        LinearLayout delete = popupView.findViewById(R.id.delete);

        edit.setOnClickListener(v -> {
            popupWindow.dismiss();
            Intent intent = new Intent(context, Admin_measure_edit.class);
            intent.putExtra("Id", measure.getId());
            intent.putExtra("Title", measure.getTitle());
            intent.putExtra("Describe", measure.getDescribe());
            intent.putExtra("Date_Measure", measure.getDate_Measure());
            intent.putExtra("Type_Measure", measure.getType_Measure());
            intent.putExtra("ImageBase64", measure.getImageBase64());
            context.startActivity(intent);
        });

        delete.setOnClickListener(v -> {
            popupWindow.dismiss();
            showDeleteDialog(measure, position);
        });
    }

    private void showDeleteDialog(Measure measure, int position) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.item_delete, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        dialogView.setAlpha(0f);

        dialog.show();

        dialogView.animate().alpha(1f).setDuration(300).start();

        ImageView yesButton = dialogView.findViewById(R.id.yesbutton);
        ImageView noButton = dialogView.findViewById(R.id.nobutton);

        yesButton.setOnClickListener(v -> {
            dialogView.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                db.collection("Measure")
                        .whereEqualTo("id", measure.getId())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                queryDocumentSnapshots.getDocuments().get(0).getReference().delete()
                                        .addOnSuccessListener(aVoid -> {
                                            measureList.remove(position);
                                            notifyItemRemoved(position);
                                            notifyItemRangeChanged(position, measureList.size());
                                        });
                            }
                        });
                dialog.dismiss();
            }).start();
        });

        noButton.setOnClickListener(v -> {
            dialogView.animate().alpha(0f).setDuration(300).withEndAction(dialog::dismiss).start();
        });
    }

}
