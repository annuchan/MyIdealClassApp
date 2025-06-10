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

        // Установим email в одно из полей
        String email = asset.getId_Employee();


        String imageBase64 = asset.getImageBase64();
        if (imageBase64 != null && !imageBase64.trim().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.imageView.setImageResource(android.R.drawable.ic_menu_report_image);
            }
        } else {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_report_image);
        }
    }

    @Override
    public int getItemCount() {
        return assetList.size();
    }
}
