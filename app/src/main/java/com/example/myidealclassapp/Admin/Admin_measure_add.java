package com.example.myidealclassapp.Admin;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Classes.Measure;
import com.example.myidealclassapp.Dropdown_menu.Admin_dropdown_menu;
import com.example.myidealclassapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Admin_measure_add extends AppCompatActivity {

    private static final String IMGBB_API_KEY = "972a14249ae8a675f7d1384d2a11bc0e";
    private static final int MAX_UPLOAD_ATTEMPTS = 20;
    private String employeeId;
    private EditText addTitle, addDescrip, addType;
    private ImageView imageView, calendarIcon;
    private Button saveButton;
    private String selectedDate = "";
    private String imageBase64OrUrl = "";
    private boolean imageChanged = false;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_measure_add);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("employeeId")) {
            employeeId = intent.getStringExtra("employeeId");
            Log.d("DEBUG", "Employee ID: " + employeeId);
        } else {
            employeeId = "";
            Log.d("DEBUG", "Employee ID not provided");
        }

        addTitle = findViewById(R.id.addTitle);
        addDescrip = findViewById(R.id.addDescrip);
        addType = findViewById(R.id.addtype);
        imageView = findViewById(R.id.picture);
        calendarIcon = findViewById(R.id.calendar);
        saveButton = findViewById(R.id.moreButton);

        imageView.setOnClickListener(v -> openImagePicker());
        calendarIcon.setOnClickListener(v -> showDatePicker());
        saveButton.setOnClickListener(v -> {
            if (imageChanged && !imageBase64OrUrl.startsWith("http")) {
                saveButton.setEnabled(false);
                uploadImageToImgBB(null, 1);
            } else {
                saveMeasure();
            }
        });

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(v -> Admin_dropdown_menu.showCustomPopupMenu(v, this, employeeId));
        hideSystemUI();


        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                            imageView.setImageBitmap(bitmap);
                            imageBase64OrUrl = encodeImageToBase64Strong(bitmap);
                            imageChanged = true;
                        } catch (IOException e) {
                            runOnUiThread(() -> Toast.makeText(this, "Ошибка выбора изображения", Toast.LENGTH_SHORT).show());
                        }
                    }
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = String.format("%02d.%02d.%04d", dayOfMonth, month + 1, year);
                    runOnUiThread(() -> Toast.makeText(this, "Выбрана дата: " + selectedDate, Toast.LENGTH_SHORT).show());
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void uploadImageToImgBB(Bitmap bitmap, int attempt) {
        if (attempt > MAX_UPLOAD_ATTEMPTS) {
            runOnUiThread(() -> {
                Log.d("DEBUG", "Max upload attempts (" + MAX_UPLOAD_ATTEMPTS + ") reached");
                Toast.makeText(this, "Не удалось загрузить изображение после " + MAX_UPLOAD_ATTEMPTS + " попыток", Toast.LENGTH_LONG).show();
                saveButton.setEnabled(true);
            });
            return;
        }

        if (!isNetworkAvailable()) {
            runOnUiThread(() -> {
                Log.d("DEBUG", "No internet connection");
                Toast.makeText(this, "Отсутствует интернет-соединение", Toast.LENGTH_SHORT).show();
                saveButton.setEnabled(true);
            });
            return;
        }

        Log.d("DEBUG", "Attempting to upload image, attempt #" + attempt);
        runOnUiThread(() -> Toast.makeText(this, "Загрузка изображения (попытка " + attempt + ")...", Toast.LENGTH_SHORT).show());

        String base64Image = bitmap != null ? encodeImageToBase64Strong(bitmap) : imageBase64OrUrl;

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
                Log.e("DEBUG", "Image upload failed on attempt #" + attempt + ": " + e.getMessage());
                new Handler(Looper.getMainLooper()).postDelayed(() -> uploadImageToImgBB(bitmap, attempt + 1), 1000); // Рекурсия
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("DEBUG", "Image upload failed on attempt #" + attempt + ": HTTP " + response.code());
                    new Handler(Looper.getMainLooper()).postDelayed(() -> uploadImageToImgBB(bitmap, attempt + 1), 1000); // Рекурсия
                    return;
                }

                String res = response.body().string();
                String url = parseImgBBUrl(res);

                if (url != null) {
                    runOnUiThread(() -> {
                        Log.d("DEBUG", "Image uploaded successfully on attempt #" + attempt + ": " + url);
                        imageBase64OrUrl = url;
                        saveMeasure(); // Переходим к сохранению
                    });
                } else {
                    Log.e("DEBUG", "Failed to parse ImgBB URL on attempt #" + attempt);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> uploadImageToImgBB(bitmap, attempt + 1), 1000); // Рекурсия
                }
            }
        });
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

    private String parseImgBBUrl(String json) {
        try {
            int urlStart = json.indexOf("\"url\":\"") + 7;
            int urlEnd = json.indexOf("\"", urlStart);
            if (urlStart > 6 && urlEnd > urlStart) {
                return json.substring(urlStart, urlEnd).replace("\\/", "/");
            }
        } catch (Exception e) {
            Log.e("DEBUG", "Error parsing ImgBB URL: " + e.getMessage());
        }
        return null;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void saveMeasure() {
        String title = addTitle.getText().toString().trim();
        String description = addDescrip.getText().toString().trim();
        String type = addType != null ? addType.getText().toString().trim() : "Праздник";

        if (title.isEmpty() || description.isEmpty() || selectedDate.isEmpty()) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                saveButton.setEnabled(true); // Разблокируем кнопку
            });
            return;
        }

        if (addType != null && type.isEmpty()) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Укажите тип мероприятия", Toast.LENGTH_SHORT).show();
                saveButton.setEnabled(true); // Разблокируем кнопку
            });
            return;
        }

        saveButton.setEnabled(false); // Блокируем кнопку перед сохранением
        runOnUiThread(() -> Toast.makeText(this, "Сохранение данных...", Toast.LENGTH_SHORT).show());

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
                            employeeId,
                            imageBase64OrUrl
                    );
                    measure.setType_Measure(type);
                    measure.setId(nextId);
                    final int finalNextId = nextId;
                    db.collection("Measure")
                            .add(measure)
                            .addOnSuccessListener(docRef -> {
                                docRef.update("id", finalNextId);
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "Мероприятие добавлено", Toast.LENGTH_SHORT).show();
                                    saveButton.setEnabled(true); // Разблокируем кнопку
                                    finish();
                                });
                            })
                            .addOnFailureListener(e -> {
                                runOnUiThread(() -> {
                                    Log.e("DEBUG", "Error adding measure: " + e.getMessage());
                                    Toast.makeText(this, "Ошибка при добавлении: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    saveButton.setEnabled(true); // Разблокируем кнопку
                                });
                            });
                })
                .addOnFailureListener(e -> {
                    runOnUiThread(() -> {
                        Log.e("DEBUG", "Error getting last ID: " + e.getMessage());
                        Toast.makeText(this, "Ошибка при получении ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        saveButton.setEnabled(true); // Разблокируем кнопку
                    });
                });
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

    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsetsCompat.Type.statusBars());
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            View decorView = getWindow().getDecorView();
            int flags = decorView.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            decorView.setSystemUiVisibility(flags);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(Color.parseColor("#D5BDAF"));
        }
    }
}