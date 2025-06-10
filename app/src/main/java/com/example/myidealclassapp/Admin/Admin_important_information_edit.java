package com.example.myidealclassapp.Admin;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Dropdown_menu.Admin_dropdown_menu;
import com.example.myidealclassapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Admin_important_information_edit extends AppCompatActivity {
    private String employeeId;
    private EditText addTitle, addDescrip;
    private Button moreButton;
    private ImageView imageView;

    private FirebaseFirestore db;
    private int id;
    private String originalTitle, originalDescribe, originalDate, idEmployee;
    private String selectedDate;

    private String imageBase64OrUrl = "";
    private boolean imageChanged = false;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private final String IMGBB_API_KEY = "972a14249ae8a675f7d1384d2a11bc0e";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_important_information_edit);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);
        addTitle = findViewById(R.id.addTitle);
        addDescrip = findViewById(R.id.addDescrip);
        moreButton = findViewById(R.id.moreButton);
        imageView = findViewById(R.id.picture);
        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        id = intent.getIntExtra("Id", -1);
        originalTitle = intent.getStringExtra("Title");
        originalDescribe = intent.getStringExtra("Describe");
        originalDate = intent.getStringExtra("Date_imp_info");
        employeeId = intent.getStringExtra("employeeId");

        selectedDate = originalDate;

        addTitle.setText(originalTitle);
        addDescrip.setText(originalDescribe);

        imageBase64OrUrl = intent.getStringExtra("image_base64");
        loadImage(imageBase64OrUrl);
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                Admin_dropdown_menu.showCustomPopupMenu(view, this, employeeId)
        );
        hideSystemUI();
        imageView.setOnClickListener(v -> openImagePicker());
        findViewById(R.id.calendar).setOnClickListener(v -> openDatePicker());
        moreButton.setOnClickListener(v -> saveChanges());
        if (intent != null && intent.hasExtra("employeeId")) {
            employeeId = intent.getStringExtra("employeeId");
            Log.d("DEBUG", "Employee ID: " + employeeId);
        } else {
            employeeId = "";
            Log.d("DEBUG", "Employee ID не передан");
        }
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                            uploadImageToImgBB(bitmap);
                        } catch (IOException e) {
                            Toast.makeText(this, "Ошибка выбора изображения", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loadImage(String base64OrUrl) {
        if (base64OrUrl == null || base64OrUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.school2);
            return;
        }

        if (base64OrUrl.startsWith("http")) {
            Glide.with(this)
                    .load(base64OrUrl)
                    .placeholder(R.drawable.school2)
                    .into(imageView);
        } else {
            try {
                byte[] decodedBytes = Base64.decode(base64OrUrl, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.school2);
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void openDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    selectedDate = String.format("%02d.%02d.%04d", dayOfMonth, month1 + 1, year1);
                    Toast.makeText(this, "Вы выбрали: " + selectedDate, Toast.LENGTH_SHORT).show();
                }, year, month, day);
        dialog.show();
    }

    private void saveChanges() {
        String newTitle = addTitle.getText().toString().trim();
        String newDescribe = addDescrip.getText().toString().trim();

        if (newTitle.isEmpty() || newDescribe.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (id == -1) {
            Toast.makeText(this, "Запись не найдена", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Important_information")
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        String docId = document.getId();

                        Map<String, Object> updatedData = new HashMap<>();
                        updatedData.put("Title", newTitle);
                        updatedData.put("Describe", newDescribe);
                        updatedData.put("Date_imp_info", selectedDate);
                        updatedData.put("Id_employee", employeeId);
                        updatedData.put("Id_type", 0);

                        if (imageChanged && !imageBase64OrUrl.isEmpty()) {
                            updatedData.put("image_base64", imageBase64OrUrl);
                        } else if (!imageChanged && imageBase64OrUrl != null) {
                            updatedData.put("image_base64", imageBase64OrUrl);
                        }

                        db.collection("Important_information")
                                .document(docId)
                                .update(updatedData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Информация обновлена", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка при обновлении: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                    } else {
                        Toast.makeText(this, "Документ с таким ID не найден", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка поиска документа: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void uploadImageToImgBB(Bitmap bitmap) {
        Toast.makeText(this, "Загрузка изображения...", Toast.LENGTH_SHORT).show();

        String base64Image = encodeImageToBase64(bitmap);

        OkHttpClient client = new OkHttpClient();

        FormBody formBody = new FormBody.Builder()
                .add("key", IMGBB_API_KEY)
                .add("image", base64Image)
                .build();

        Request request = new Request.Builder()
                .url("https://api.imgbb.com/1/upload")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(Admin_important_information_edit.this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(Admin_important_information_edit.this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show());
                    return;
                }

                String res = response.body().string();
                String url = parseImgBBUrl(res);

                if (url != null) {
                    runOnUiThread(() -> {
                        imageBase64OrUrl = url;
                        imageChanged = true;
                        Glide.with(Admin_important_information_edit.this)
                                .load(url)
                                .placeholder(R.drawable.school2)
                                .into(imageView);
                        Toast.makeText(Admin_important_information_edit.this, "Изображение загружено", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(Admin_important_information_edit.this, "Ошибка обработки ответа ImgBB", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        int maxSizeBytes = 1_000_000;
        int quality = 60;
        Bitmap scaledBitmap = bitmap;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);

        while (baos.toByteArray().length > maxSizeBytes && quality > 10) {
            baos.reset();
            quality -= 10;
            int newWidth = (int) (scaledBitmap.getWidth() * 0.9);
            int newHeight = (int) (scaledBitmap.getHeight() * 0.9);
            scaledBitmap = Bitmap.createScaledBitmap(scaledBitmap, newWidth, newHeight, true);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        }

        byte[] byteArray = baos.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private String parseImgBBUrl(String json) {
        try {
            int urlStart = json.indexOf("\"url\":\"") + 7;
            int urlEnd = json.indexOf("\"", urlStart);
            if (urlStart > 6 && urlEnd > urlStart) {
                return json.substring(urlStart, urlEnd).replace("\\/", "/");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public void back(View view) {
        finish();
    }
    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, Autorization.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    public void toMain(View view) {
        Intent intent = new Intent(this, Admin_main_window.class);
        intent.putExtra("employee_id", employeeId);
        startActivity(intent);
    }
    public void about_the_app(View view) {
        Intent intent = new Intent(this, Admin_about_the_app.class);
        intent.putExtra("employee_id", employeeId);
        startActivity(intent);
    }
    /// Скрытие всех баров
    private void hideSystemUI() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsetsCompat.Type.statusBars());
                insetsController.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // Только скрываем status bar
            );
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final Window window = getWindow();
            final View decorView = window.getDecorView();
            int flags = decorView.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            decorView.setSystemUiVisibility(flags);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(Color.parseColor("#D5BDAF"));
        }
    }
}
