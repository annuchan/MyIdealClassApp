package com.example.myidealclassapp.Teacher;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myidealclassapp.Admin.Admin_about_the_app;
import com.example.myidealclassapp.Admin.Admin_important_information_add;
import com.example.myidealclassapp.Admin.Admin_main_window;
import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Classes.Measure;
import com.example.myidealclassapp.Dropdown_menu.Teacher_dropdown_menu;
import com.example.myidealclassapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Teacher_measure_add extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final String IMGBB_API_KEY = "972a14249ae8a675f7d1384d2a11bc0e";
    private static final String UPLOAD_URL = "https://api.imgbb.com/1/upload";
    private String employeeId;
    private EditText addTitle, addDescrip, addtype;
    private ImageView imageView, calendarIcon;
    private Button saveButton;
    private String selectedDate = "";
    private int idSubject;
    private ProgressBar progressBar;
    private String encodedImage = "";
    private String uploadedImageUrl = "";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_measure_add);
        addtype = findViewById(R.id.addtype);
        addTitle = findViewById(R.id.addTitle);
        addDescrip = findViewById(R.id.addDescrip);
        imageView = findViewById(R.id.picture);
        calendarIcon = findViewById(R.id.calendarIcon);
        saveButton = findViewById(R.id.moreButton);
        progressBar = findViewById(R.id.progressBar);
        imageView.setOnClickListener(v -> pickImageFromGallery());
        calendarIcon.setOnClickListener(v -> showDatePicker());
        saveButton.setOnClickListener(v -> {
            if (!encodedImage.isEmpty()) {
                uploadImageToImgBB();
            } else {
                saveMeasure();
            }
        });
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("employeeId")) {
                employeeId = intent.getStringExtra("employeeId");
            }
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                Teacher_dropdown_menu.showCustomPopupMenu(view, this, employeeId, idSubject)
        );
        hideSystemUI();
        idSubject = getIntent().getIntExtra("subjectId", 2); // читаем subjectId из интента, по умолчанию 2
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
        Intent intent = new Intent(this, Teacher_main_window.class);
        intent.putExtra("employeeId", employeeId);
        intent.putExtra("subjectId", idSubject);
        startActivity(intent);
    }
    public void about_the_app(View view) {
        Intent intent = new Intent(this, Teacher_about_the_app.class);
        intent.putExtra("employeeId", employeeId);
        intent.putExtra("subjectId", idSubject);
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
    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);
                encodedImage = encodeImageToBase64Strong(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToImgBB() {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        uploadedImageUrl = jsonObject.getJSONObject("data").getString("url");
                        saveMeasure();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Ошибка разбора ответа от ImgBB", Toast.LENGTH_SHORT).show();
                        saveButton.setEnabled(false);
                        progressBar.setVisibility(View.VISIBLE);
                    }
                },
                error -> {
                    Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                    Log.e("ImgBB Upload Error", error.toString());
                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("key", IMGBB_API_KEY);
                params.put("image", encodedImage);
                return params;
            }
        };

        queue.add(stringRequest);
    }

    private String encodeImageToBase64Strong(Bitmap bitmap) {
        Bitmap resizedBitmap = resizeBitmap(bitmap, 800, 800);
        int maxSizeBytes = 500_000;
        int quality = 90;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);

        while (baos.toByteArray().length > maxSizeBytes && quality > 10) {
            baos.reset();
            quality -= 10;
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        }

        while (baos.toByteArray().length > maxSizeBytes) {
            baos.reset();
            int newWidth = (int) (resizedBitmap.getWidth() * 0.75);
            int newHeight = (int) (resizedBitmap.getHeight() * 0.75);
            resizedBitmap = Bitmap.createScaledBitmap(resizedBitmap, newWidth, newHeight, true);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        }

        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
    }

    private Bitmap resizeBitmap(Bitmap original, int maxWidth, int maxHeight) {
        int width = original.getWidth();
        int height = original.getHeight();

        float ratioBitmap = (float) width / height;
        float ratioMax = (float) maxWidth / maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;

        if (ratioMax > ratioBitmap) {
            finalWidth = (int) (maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) (maxWidth / ratioBitmap);
        }

        return Bitmap.createScaledBitmap(original, finalWidth, finalHeight, true);
    }
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    Toast.makeText(this, "Выбрана дата: " + selectedDate, Toast.LENGTH_SHORT).show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void saveMeasure() {
        String title = addTitle.getText().toString().trim();
        String description = addDescrip.getText().toString().trim();
        String type_measure = addtype.getText().toString().trim();
        if (title.isEmpty() || description.isEmpty() || selectedDate.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        getMaxIdFromFirestore(nextId -> {
            Map<String, Object> data = new HashMap<>();
            data.put("Title", title);              // маленькая буква
            data.put("Describe", description);
            data.put("Date_Measure", selectedDate);
            data.put("Id_Employee", employeeId);       // тут можешь подставить актуальный id
            data.put("ImageBase64", uploadedImageUrl);
            data.put("Type_Measure", type_measure);
            data.put("id", nextId);

            db.collection("Measure")
                    .add(data)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Информация успешно добавлена", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show());
        });
    }
    private void getMaxIdFromFirestore(Teacher_measure_add.IdCallback callback) {
        db.collection("Measure")
                .orderBy("id", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int nextId = 1;
                    if (!querySnapshot.isEmpty()) {
                        Long maxIdLong = querySnapshot.getDocuments().get(0).getLong("id");
                        if (maxIdLong != null) {
                            nextId = maxIdLong.intValue() + 1;
                        }
                    }
                    callback.onCallback(nextId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка при получении max ID", Toast.LENGTH_SHORT).show();
                    callback.onCallback(1);
                });
    }
    interface IdCallback {
        void onCallback(int nextId);
    }
}
