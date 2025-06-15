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

import com.example.myidealclassapp.Classes.Asset;
import com.example.myidealclassapp.R;

import java.util.List;
import java.util.Random;

public class ParentAssetAdapter extends RecyclerView.Adapter<ParentAssetAdapter.AssetViewHolder> {

    private Context context;
    private List<Asset> assetList;

    public ParentAssetAdapter(Context context, List<Asset> assetList) {
        this.context = context;
        this.assetList = assetList;
    }

    public static class AssetViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView title, description, place;

        public AssetViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageApplication_form);
            title = itemView.findViewById(R.id.titleApplication_form);
            description = itemView.findViewById(R.id.DescriptionApplication_form);
            place = itemView.findViewById(R.id.Place_asset);
        }
    }

    @NonNull
    @Override
    public AssetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_parent_asset, parent, false);
        return new AssetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssetViewHolder holder, int position) {
        Asset asset = assetList.get(position);

        holder.title.setText(asset.getTitle());
        holder.description.setText(asset.getDescribe());
        holder.place.setText(asset.getPlace());


        String imageBase64 = asset.getImageBase64();

        if (imageBase64 == null ||
                imageBase64.trim().isEmpty() ||
                imageBase64.trim().equals("0")) {

            // Используем картинку по умолчанию ph_1
            holder.imageView.setImageResource(R.drawable.school3);
        } else {
            try {
                byte[] decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    holder.imageView.setImageBitmap(bitmap);
                } else {
                    holder.imageView.setImageResource(R.drawable.school3);
                }
            } catch (Exception e) {
                holder.imageView.setImageResource(R.drawable.school3);
            }
        }
    }

    @Override
    public int getItemCount() {
        return assetList.size();
    }
}
