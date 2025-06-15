package com.example.myidealclassapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myidealclassapp.Admin.Admin_asset_edit;
import com.example.myidealclassapp.Classes.Asset;
import com.example.myidealclassapp.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminAssetAdapter extends RecyclerView.Adapter<AdminAssetAdapter.ViewHolder> {

    private final Context context;
    private final List<Asset> assetList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AdminAssetAdapter(Context context, List<Asset> assetList) {
        this.context = context;
        this.assetList = assetList;
    }

    @NonNull
    @Override
    public AdminAssetAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_asset, parent, false);
        return new ViewHolder(view);
    }
/// Вывод данных в Ресайклвью
    @Override
    public void onBindViewHolder(@NonNull AdminAssetAdapter.ViewHolder holder, int position) {
        Asset asset = assetList.get(position);

        holder.title.setText(asset.getTitle());
        holder.description.setText(asset.getDescribe());
        holder.place.setText(asset.getPlace());

        String imageData = asset.getImageBase64();
        if (imageData != null && !imageData.isEmpty()) {
            if (imageData.startsWith("http")) {
                Glide.with(context)
                        .load(imageData)
                        .placeholder(R.drawable.school2)
                        .error(R.drawable.school2)
                        .into(holder.image);
            } else {
                if (imageData.equals("0")) {
                    // Если imageData == "0", ставим ph_1
                    int resId = context.getResources().getIdentifier("school3", "drawable", context.getPackageName());
                    holder.image.setImageResource(resId);
                } else {
                    try {
                        byte[] decodedBytes = Base64.decode(imageData, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        holder.image.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        holder.image.setImageResource(R.drawable.school3);
                    }
                }
            }
        } else if (imageData == null || imageData.isEmpty()) {
            holder.image.setImageResource(R.drawable.school2);
        }

        holder.menuButton.setOnClickListener(v -> showPopupMenu(v, asset, position));

    }

    @Override
    public int getItemCount() {
        return assetList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, place;
        ImageView image, menuButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.titleApplication_form);
            description = itemView.findViewById(R.id.DescriptionApplication_form);
            place = itemView.findViewById(R.id.Place_asset);
            image = itemView.findViewById(R.id.imageApplication_form);
            menuButton = itemView.findViewById(R.id.detailsImageView);
        }
    }
    ///Перенаправление данных на окно для редактирования
    private void showPopupMenu(View anchorView, Asset asset, int position) {
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
            Intent intent = new Intent(context, Admin_asset_edit.class);
            intent.putExtra("Id", asset.getId());
            intent.putExtra("Title", asset.getTitle());
            intent.putExtra("Describe", asset.getDescribe());
            intent.putExtra("Place", asset.getPlace());
            intent.putExtra("Id_Employee", asset.getId_Employee());
            context.startActivity(intent);
        });

        delete.setOnClickListener(v -> {
            popupWindow.dismiss();
            showDeleteDialog(asset, position);
        });
    }
/// Плавный показ всплывающего меню для удаления и удаление объекта
    private void showDeleteDialog(Asset asset, int position) {
        View deleteView = LayoutInflater.from(context).inflate(R.layout.item_delete, null);
        PopupWindow popupWindow = new PopupWindow(deleteView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        deleteView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in));
        popupWindow.showAtLocation(deleteView, Gravity.CENTER, 0, 0);

        ImageView yesButton = deleteView.findViewById(R.id.yesbutton);
        ImageView noButton = deleteView.findViewById(R.id.nobutton);

        yesButton.setOnClickListener(v -> {
            int idToDelete = asset.getId();

            db.collection("Asset")
                    .whereEqualTo("Id", idToDelete)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            for (var doc : querySnapshot.getDocuments()) {
                                doc.getReference().delete()
                                        .addOnSuccessListener(aVoid -> {
                                            assetList.remove(position);
                                            notifyItemRemoved(position);
                                            notifyItemRangeChanged(position, assetList.size());
                                            Toast.makeText(context, "Удалено успешно", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(context, "Ошибка удаления: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                        );
                            }
                        } else {
                            Toast.makeText(context, "Документ с Id " + idToDelete + " не найден", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Ошибка поиска документа: " + e.getMessage(), Toast.LENGTH_SHORT).show());

            deleteView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out));
            deleteView.postDelayed(popupWindow::dismiss, 300);
        });

        noButton.setOnClickListener(v -> {
            deleteView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out));
            deleteView.postDelayed(popupWindow::dismiss, 300);
        });
    }

}
