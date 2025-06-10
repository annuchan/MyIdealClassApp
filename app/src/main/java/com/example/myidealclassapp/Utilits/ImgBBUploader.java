package com.example.myidealclassapp.Utilits;

import okhttp3.*;

import org.json.JSONObject;

import java.io.IOException;

public class ImgBBUploader {

    private static final String API_KEY = "4c39a752d2d2dc89ef497f12d5f0bb1b";
    private static final String UPLOAD_URL = "https://api.imgbb.com/1/upload";

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String error);
    }

    public static void uploadBase64Image(String base64Image, UploadCallback callback) {
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("key", API_KEY)
                .add("image", base64Image)
                .build();

        Request request = new Request.Builder()
                .url(UPLOAD_URL)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onFailure("Upload failed: " + response.message());
                    return;
                }

                String responseBody = response.body().string();
                try {
                    JSONObject json = new JSONObject(responseBody);
                    String imageUrl = json.getJSONObject("data").getString("url");
                    callback.onSuccess(imageUrl);
                } catch (Exception e) {
                    callback.onFailure("Parse error: " + e.getMessage());
                }
            }
        });
    }
}
