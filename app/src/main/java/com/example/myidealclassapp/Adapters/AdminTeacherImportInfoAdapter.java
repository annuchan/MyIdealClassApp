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
import com.example.myidealclassapp.Classes.Important_information;
import com.example.myidealclassapp.R;
import com.example.myidealclassapp.Teacher.Teacher_important_imformation_edit;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminTeacherImportInfoAdapter extends RecyclerView.Adapter<AdminTeacherImportInfoAdapter.ViewHolder> {

    private Context context;
    private List<Important_information> infoList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AdminTeacherImportInfoAdapter(Context context, List<Important_information> infoList) {
        this.context = context;
        this.infoList = infoList;
    }

    @NonNull
    @Override
    public AdminTeacherImportInfoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_teacher_admin_important_information, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminTeacherImportInfoAdapter.ViewHolder holder, int position) {
        Important_information info = infoList.get(position);

        holder.title.setText(info.getTitle());
        holder.description.setText(info.getDescribe());
        holder.date.setText(info.getDate_imp_info());

        String imageData = info.getImageBase64();
        if (imageData != null && !imageData.isEmpty()) {
            if (imageData.startsWith("http")) {
                // Загружаем через Glide
                Glide.with(context)
                        .load(imageData)
                        .placeholder(R.drawable.school2)
                        .error(R.drawable.school2)
                        .into(holder.image);
            } else {
                try {
                    byte[] decodedBytes = Base64.decode(imageData, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    holder.image.setImageBitmap(bitmap);
                } catch (Exception e) {
                    holder.image.setImageResource(R.drawable.school2);
                }
            }
        } else {
            holder.image.setImageResource(R.drawable.school2);
        }

        // Обработка меню
        holder.menuButton.setOnClickListener(v -> showPopupMenu(v, info, position));
    }


    @Override
    public int getItemCount() {
        return infoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, date;
        ImageView image, menuButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.titleImportant_information);
            description = itemView.findViewById(R.id.DescriptionImportant_information);
            date = itemView.findViewById(R.id.DateImportant_information);
            image = itemView.findViewById(R.id.imageImportant_information);
            menuButton = itemView.findViewById(R.id.detailsImageView);
        }
    }

    private void showPopupMenu(View anchorView, Important_information info, int position) {
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
            Intent intent = new Intent(context, Teacher_important_imformation_edit.class);

            // Передаём int id (не documentId)
            intent.putExtra("Id", info.getId());
            intent.putExtra("Title", info.getTitle());
            intent.putExtra("Describe", info.getDescribe());
            intent.putExtra("Date_imp_info", info.getDate_imp_info());
            intent.putExtra("Id_Employee", info.getId_Employee());

            context.startActivity(intent);
        });

        delete.setOnClickListener(v -> {
            popupWindow.dismiss();
            showDeleteDialog(info, position);
        });
    }

    private void showDeleteDialog(Important_information info, int position) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.item_delete, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        // Начальная прозрачность 0
        dialogView.setAlpha(0f);
        dialog.show();

        // Плавное появление (fade in)
        dialogView.animate().alpha(1f).setDuration(300).start();

        ImageView yesButton = dialogView.findViewById(R.id.yesbutton);
        ImageView noButton = dialogView.findViewById(R.id.nobutton);

        yesButton.setOnClickListener(v -> {
            // Плавное исчезновение (fade out)
            dialogView.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                db.collection("Important_information")
                        .whereEqualTo("id", info.getId())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                queryDocumentSnapshots.getDocuments().get(0).getReference().delete()
                                        .addOnSuccessListener(aVoid -> {
                                            infoList.remove(position);
                                            notifyItemRemoved(position);
                                            notifyItemRangeChanged(position, infoList.size());
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
