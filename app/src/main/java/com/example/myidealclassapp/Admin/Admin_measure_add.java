package com.example.myidealclassapp.Admin;

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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Classes.Measure;
import com.example.myidealclassapp.Dropdown_menu.Admin_dropdown_menu;
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

public class Admin_measure_add extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final String IMGBB_API_KEY = "972a14249ae8a675f7d1384d2a11bc0e";
    private static final String UPLOAD_URL = "https://api.imgbb.com/1/upload";
    private String employeeId;
    private EditText addTitle, addDescrip;
    private ImageView imageView, calendarIcon;
    private Button saveButton;
    private String selectedDate = "";
    private String encodedImage = "";
    private String uploadedImageUrl = "";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_measure_add);

        // Edge to edge padding (как в твоем первом примере)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("employeeId")) {
            employeeId = intent.getStringExtra("employeeId");
            Log.d("DEBUG", "Employee ID: " + employeeId);
        } else {
            employeeId = "";
            Log.d("DEBUG", "Employee ID не передан");
        }
        addTitle = findViewById(R.id.addTitle);
        addDescrip = findViewById(R.id.addDescrip);
        imageView = findViewById(R.id.picture);
        calendarIcon = findViewById(R.id.calendar);
        saveButton = findViewById(R.id.moreButton);

        imageView.setOnClickListener(v -> pickImageFromGallery());
        calendarIcon.setOnClickListener(v -> showDatePicker());
        saveButton.setOnClickListener(v -> {
            if (!encodedImage.isEmpty()) {
                uploadImageToImgBB();
            } else {
                saveMeasure();
            }
        });
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                Admin_dropdown_menu.showCustomPopupMenu(view, this, employeeId)
        );
        hideSystemUI();
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
                        Toast.makeText(this, "Ошибка обработки ответа ImgBB", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show()) {

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
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 800, 800, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 90;

        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        while (baos.toByteArray().length > 500_000 && quality > 10) {
            baos.reset();
            quality -= 10;
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        }

        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
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

        if (title.isEmpty() || description.isEmpty() || selectedDate.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Measure")
                .orderBy("id", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int nextId = 1;
                    if (!querySnapshot.isEmpty()) {
                        Long lastId = querySnapshot.getDocuments().get(0).getLong("id");
                        if (lastId != null) {
                            nextId = lastId.intValue() + 1;
                        }
                    }

                    Measure measure = new Measure(
                            title,
                            description,
                            selectedDate,
                            employeeId, // подставь id админа, если надо
                            0,    // Id_Type всегда 0
                            uploadedImageUrl
                    );

                    measure.setId(nextId);

                    int finalNextId = nextId;
                    db.collection("Measure")
                            .add(measure)
                            .addOnSuccessListener(docRef -> {
                                docRef.update("id", finalNextId);
                                Toast.makeText(this, "Мероприятие добавлено", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка при получении ID", Toast.LENGTH_SHORT).show());
    }
}
