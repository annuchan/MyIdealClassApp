package com.example.myidealclassapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myidealclassapp.Admin.Admin_important_information_edit;
import com.example.myidealclassapp.Classes.Important_information;
import com.example.myidealclassapp.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Random;

public class AdminImportInfoAdapter extends RecyclerView.Adapter<AdminImportInfoAdapter.ViewHolder> {

    private final Context context;
    private final List<Important_information> infoList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Important_information infoToDelete;
    private int positionToDelete;

    public AdminImportInfoAdapter(Context context, List<Important_information> infoList) {
        this.context = context;
        this.infoList = infoList;
    }

    @NonNull
    @Override
    public AdminImportInfoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_teacher_admin_important_information, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminImportInfoAdapter.ViewHolder holder, int position) {
        Important_information info = infoList.get(position);

        holder.title.setText(info.getTitle());
        holder.description.setText(info.getDescribe());
        holder.date.setText(info.getDate_imp_info());

        String imageData = info.getImageBase64();

        // Если поле пустое, null или "0" — ставим ph_1 жёстко
        if (imageData == null || imageData.isEmpty() || imageData.equals("0")) {
            imageData = "school3";
            info.setImageBase64(imageData);
        }

        if (imageData.startsWith("http")) {
            Glide.with(context)
                    .load(imageData)
                    .placeholder(R.drawable.school2)
                    .error(R.drawable.school2)
                    .into(holder.image);
        } else if (imageData.startsWith("school3")) {
            int resId = context.getResources().getIdentifier(imageData, "drawable", context.getPackageName());
            if (resId != 0) {
                holder.image.setImageResource(resId);
            } else {
                holder.image.setImageResource(R.drawable.school2);
            }
        } else {
            try {
                byte[] decodedBytes = Base64.decode(imageData, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.image.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.image.setImageResource(R.drawable.school2);
            }
        }

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
            Intent intent = new Intent(context, Admin_important_information_edit.class);
            intent.putExtra("Id", info.getId());
            intent.putExtra("Title", info.getTitle());
            intent.putExtra("Describe", info.getDescribe());
            intent.putExtra("Date_imp_info", info.getDate_imp_info());
            intent.putExtra("employeeId", info.getId_Employee());
            intent.putExtra("ImageBase64", info.getImageBase64());
            context.startActivity(intent);
        });

        delete.setOnClickListener(v -> {
            popupWindow.dismiss();
            showDeleteDialog(info, position);
        });
    }

    private void showDeleteDialog(Important_information info, int position) {
        infoToDelete = info;
        positionToDelete = position;

        // Создаем диалог и показываем его
        View dialogView = LayoutInflater.from(context).inflate(R.layout.item_delete, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        // Начинаем с прозрачности 0
        dialogView.setAlpha(0f);

        dialog.show();

        // Анимация появления (fade in)
        dialogView.animate().alpha(1f).setDuration(300).start();

        ImageView yesButton = dialogView.findViewById(R.id.yesbutton);
        ImageView noButton = dialogView.findViewById(R.id.nobutton);

        yesButton.setOnClickListener(v -> {
            if (infoToDelete == null) return;
            Long idToDelete = (long) infoToDelete.getId();

            // Анимация исчезновения (fade out)
            dialogView.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                db.collection("Important_information")
                        .whereEqualTo("id", idToDelete)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!querySnapshot.isEmpty()) {
                                querySnapshot.getDocuments().get(0).getReference().delete()
                                        .addOnSuccessListener(aVoid -> {
                                            infoList.remove(positionToDelete);
                                            notifyItemRemoved(positionToDelete);
                                            notifyItemRangeChanged(positionToDelete, infoList.size());
                                            Toast.makeText(context, "Удалено успешно", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(context, "Ошибка удаления: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                        );
                            } else {
                                Toast.makeText(context, "Документ с Id " + idToDelete + " не найден", Toast.LENGTH_SHORT).show();
                            }
                        });
                dialog.dismiss();
            }).start();
        });

        noButton.setOnClickListener(v -> {
            // Анимация исчезновения (fade out) при отмене
            dialogView.animate().alpha(0f).setDuration(300).withEndAction(dialog::dismiss).start();
        });
    }
}
