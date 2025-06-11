package com.example.myidealclassapp.Admin;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
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

public class Admin_measure_edit extends AppCompatActivity {

    private EditText addTitle, addDescrip, addType;
    private Button saveButton;
    private FirebaseFirestore db;
    private int id;
    private String originalTitle, originalDescribe, originalDate, originalType;
    private String idEmployee;
    private String selectedDate;
    private String employeeId;
    private ImageView imageView;
    private String imageBase64OrUrl = "";
    private boolean imageChanged = false;
    private boolean isUploading = false;
    private static final int MAX_UPLOAD_ATTEMPTS = 20;
    private static final String IMGBB_API_KEY = "972a14249ae8a675f7d1384d2a11bc0e";
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private RequestManager glideRequestManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_measure_edit);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);
        glideRequestManager = Glide.with(this);
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(v -> Admin_dropdown_menu.showCustomPopupMenu(v, this, employeeId));
        hideSystemUI();


        addTitle = findViewById(R.id.addTitle);
        addDescrip = findViewById(R.id.addDescrip);
        addType = findViewById(R.id.addtype);
        saveButton = findViewById(R.id.moreButton);
        imageView = findViewById(R.id.picture);
        db = FirebaseFirestore.getInstance();


        Intent intent = getIntent();
        id = intent.getIntExtra("Id", -1);
        originalTitle = intent.getStringExtra("Title");
        originalDescribe = intent.getStringExtra("Describe");
        originalDate = intent.getStringExtra("Date_Measure");
        originalType = intent.getStringExtra("Type_Measure");
        idEmployee = intent.getStringExtra("Id_Employee");
        if (intent.hasExtra("employeeId")) {
            employeeId = intent.getStringExtra("employeeId");
            Log.d("DEBUG", "Employee ID: " + employeeId);
        } else {
            employeeId = idEmployee != null ? idEmployee : "";
            Log.d("DEBUG", "Employee ID not provided, using Id_Employee: " + idEmployee);
        }

        selectedDate = originalDate != null ? originalDate.replace("/", ".") : "";
        addTitle.setText(originalTitle);
        addDescrip.setText(originalDescribe);
        addType.setText(originalType);
        imageBase64OrUrl = intent.getStringExtra("ImageBase64");
        loadImage(imageBase64OrUrl);


        imageView.setOnClickListener(v -> openImagePicker());
        findViewById(R.id.calendar).setOnClickListener(v -> openDatePicker());
        saveButton.setOnClickListener(v -> {
            if (isUploading) {
                Toast.makeText(this, "Подождите, изображение загружается...", Toast.LENGTH_SHORT).show();
                return;
            }
            if (imageChanged && !imageBase64OrUrl.startsWith("http")) {
                saveButton.setEnabled(false);
                isUploading = true;
                uploadImageToImgBB(null, 1);
            } else {
                saveChanges();
            }
        });


        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                            imageView.setImageBitmap(bitmap);
                            imageBase64OrUrl = encodeImageToBase64(bitmap);
                            imageChanged = true;
                        } catch (IOException e) {
                            runOnUiThread(() -> Toast.makeText(this, "Ошибка выбора изображения", Toast.LENGTH_SHORT).show());
                        }
                    }
                });
    }

    private void loadImage(String base64OrUrl) {
        Log.d("DEBUG", "Loading image: " + base64OrUrl);
        if (base64OrUrl == null || base64OrUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.school2);
            return;
        }

        if (base64OrUrl.startsWith("http")) {
            if (!isFinishing()) {
                glideRequestManager
                        .load(base64OrUrl)
                        .placeholder(R.drawable.school2)
                        .error(R.drawable.school2)
                        .into(imageView);
            }
        } else {
            try {
                byte[] decodedBytes = Base64.decode(base64OrUrl, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.e("DEBUG", "Error decoding Base64: " + e.getMessage());
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
                    runOnUiThread(() -> Toast.makeText(this, "Выбрали: " + selectedDate, Toast.LENGTH_SHORT).show());
                }, year, month, day);
        dialog.show();
    }

    private void saveChanges() {
        String newTitle = addTitle.getText().toString().trim();
        String newDescribe = addDescrip.getText().toString().trim();
        String newType = addType.getText().toString().trim();

        if (newTitle.isEmpty() || newDescribe.isEmpty() || newType.isEmpty()) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                saveButton.setEnabled(true);
            });
            return;
        }

        if (id == -1) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Запись не найдена", Toast.LENGTH_SHORT).show();
                saveButton.setEnabled(true);
            });
            return;
        }

        saveButton.setEnabled(false);
        runOnUiThread(() -> Toast.makeText(this, "Сохранение данных...", Toast.LENGTH_SHORT).show());

        db.collection("Measure")
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);

                        Map<String, Object> updatedData = new HashMap<>();
                        updatedData.put("Title", newTitle);
                        updatedData.put("Describe", newDescribe);
                        updatedData.put("Date_Measure", selectedDate);
                        updatedData.put("Id_Employee", idEmployee != null ? idEmployee : employeeId);
                        updatedData.put("Type_Measure", newType);

                        if (imageChanged && !imageBase64OrUrl.isEmpty()) {
                            updatedData.put("ImageBase64", imageBase64OrUrl);
                        } else if (!imageChanged && imageBase64OrUrl != null) {
                            updatedData.put("ImageBase64", imageBase64OrUrl);
                        }

                        db.collection("Measure")
                                .document(document.getId())
                                .update(updatedData)
                                .addOnSuccessListener(aVoid -> {
                                    runOnUiThread(() -> {
                                        Toast.makeText(this, "Мероприятие обновлено", Toast.LENGTH_SHORT).show();
                                        saveButton.setEnabled(true);
                                        setResult(RESULT_OK);
                                        finish();
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    runOnUiThread(() -> {
                                        Log.e("DEBUG", "Error updating document: " + e.getMessage());
                                        Toast.makeText(this, "Ошибка обновления: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        saveButton.setEnabled(true);
                                    });
                                });
                    } else {
                        runOnUiThread(() -> {
                            Log.d("DEBUG", "No document found for id: " + id);
                            Toast.makeText(this, "Документ с таким ID не найден", Toast.LENGTH_SHORT).show();
                            saveButton.setEnabled(true);
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    runOnUiThread(() -> {
                        Log.e("DEBUG", "Error searching document: " + e.getMessage());
                        Toast.makeText(this, "Ошибка поиска: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        saveButton.setEnabled(true);
                    });
                });
    }

    private void uploadImageToImgBB(Bitmap bitmap, int attempt) {
        if (attempt > MAX_UPLOAD_ATTEMPTS) {
            if (!isFinishing()) {
                runOnUiThread(() -> {
                    Log.d("DEBUG", "Max upload attempts (" + MAX_UPLOAD_ATTEMPTS + ") reached");
                    Toast.makeText(this, "Не удалось загрузить изображение после " + MAX_UPLOAD_ATTEMPTS + " попыток", Toast.LENGTH_LONG).show();
                    saveButton.setEnabled(true);
                    isUploading = false;
                });
            }
            return;
        }

        if (!isNetworkAvailable()) {
            if (!isFinishing()) {
                runOnUiThread(() -> {
                    Log.d("DEBUG", "No internet connection");
                    Toast.makeText(this, "Отсутствует интернет-соединение", Toast.LENGTH_SHORT).show();
                    saveButton.setEnabled(true);
                    isUploading = false;
                });
            }
            return;
        }

        Log.d("DEBUG", "Attempting to upload image, attempt #" + attempt);
        if (!isFinishing()) {
            runOnUiThread(() -> Toast.makeText(this, "Загрузка изображения (попытка " + attempt + ")...", Toast.LENGTH_SHORT).show());
        }

        String base64Image = bitmap != null ? encodeImageToBase64(bitmap) : imageBase64OrUrl;

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
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (!isFinishing()) {
                        uploadImageToImgBB(bitmap, attempt + 1);
                    }
                }, 1000);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("DEBUG", "Image upload failed on attempt #" + attempt + ": HTTP " + response.code());
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (!isFinishing()) {
                            uploadImageToImgBB(bitmap, attempt + 1);
                        }
                    }, 1000);
                    return;
                }

                String res = response.body().string();
                String url = parseImgBBUrl(res);

                if (url != null) {
                    if (!isFinishing()) {
                        runOnUiThread(() -> {
                            Log.d("DEBUG", "Image uploaded successfully on attempt #" + attempt + ": " + url);
                            imageBase64OrUrl = url;
                            imageChanged = true;
                            isUploading = false;
                            glideRequestManager
                                    .load(url)
                                    .placeholder(R.drawable.school2)
                                    .error(R.drawable.school2)
                                    .into(imageView);
                            Toast.makeText(Admin_measure_edit.this, "Изображение загружено", Toast.LENGTH_SHORT).show();
                            saveChanges();
                        });
                    }
                } else {
                    Log.e("DEBUG", "Failed to parse ImgBB URL on attempt #" + attempt);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (!isFinishing()) {
                            uploadImageToImgBB(bitmap, attempt + 1);
                        }
                    }, 1000);
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
            Log.e("DEBUG", "Error parsing ImgBB URL: " + e.getMessage());
        }
        return null;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (glideRequestManager != null) {
            glideRequestManager.clear(imageView);
        }
    }
}