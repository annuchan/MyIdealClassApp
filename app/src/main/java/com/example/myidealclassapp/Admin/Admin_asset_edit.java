package com.example.myidealclassapp.Admin;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

public class Admin_asset_edit extends AppCompatActivity {
    private String employeeId;
    private EditText editName, editDescription, editPlace;
    private Spinner spinnerTeacher;
    private Button saveButton;
    private ImageView imageView;
    private static final int MAX_UPLOAD_ATTEMPTS = 20;
    private FirebaseFirestore db;

    private int assetId = -1;
    private String originalName, originalDescription, originalPlace, idTeacher;
    private String imageBase64OrUrl = "";
    private boolean imageChanged = false;

    private final String IMGBB_API_KEY = "972a14249ae8a675f7d1384d2a11bc0e";

    private List<String> teacherNames = new ArrayList<>();
    private List<String> teacherIds = new ArrayList<>();
    private RequestManager glideRequestManager;
    private int selectedTeacherIndex = 0;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_asset_edit);

        editName = findViewById(R.id.addTitle);
        editDescription = findViewById(R.id.addDescrip);
        editPlace = findViewById(R.id.addtype);
        spinnerTeacher = findViewById(R.id.teacherSpinner);
        saveButton = findViewById(R.id.moreButton);
        imageView = findViewById(R.id.picture);
        db = FirebaseFirestore.getInstance();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);
        glideRequestManager = Glide.with(this);
        Intent intent = getIntent();
        assetId = intent.getIntExtra("Id", -1);
        originalName = intent.getStringExtra("Name");
        originalDescription = intent.getStringExtra("Description");
        originalPlace = intent.getStringExtra("Place");
        idTeacher = intent.getStringExtra("Id_Employee");
        imageBase64OrUrl = intent.getStringExtra("ImageBase64");

        editName.setText(originalName);
        editDescription.setText(originalDescription);
        editPlace.setText(originalPlace);
        loadImage(imageBase64OrUrl);


        imageView.setOnClickListener(v -> openImagePicker());
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                            saveButton.setEnabled(false); // Блокируем кнопку перед началом загрузки
                            uploadImageToImgBB(bitmap, 1); // Начинаем загрузку с первой попытки
                        } catch (IOException e) {
                            Toast.makeText(this, "Ошибка выбора изображения", Toast.LENGTH_SHORT).show();
                            saveButton.setEnabled(true); // Разблокируем кнопку при ошибке выбора
                        }
                    }
                });

        loadTeachers();
        saveButton.setOnClickListener(v -> saveChanges());
        if (intent != null && intent.hasExtra("employeeId")) {
            employeeId = intent.getStringExtra("employeeId");
            Log.d("DEBUG", "Employee ID: " + employeeId);
        } else {
            employeeId = "";
            Log.d("DEBUG", "Employee ID не передан");
        }
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                Admin_dropdown_menu.showCustomPopupMenu(view, this, employeeId)
        );
        hideSystemUI();
    }

    private void loadTeachers() {
        db.collection("Employees")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    teacherNames.clear();
                    teacherIds.clear();

                    int index = 0;
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String fullName = doc.getString("LastName");
                        String uid = doc.getId();

                        teacherNames.add(fullName);
                        teacherIds.add(uid);

                        if (uid.equals(idTeacher)) {
                            selectedTeacherIndex = index;
                        }

                        index++;
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, teacherNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerTeacher.setAdapter(adapter);
                    spinnerTeacher.setSelection(selectedTeacherIndex);
                });
    }

    private void saveChanges() {
        String newName = editName.getText().toString().trim();
        String newDescription = editDescription.getText().toString().trim();
        String newPlace = editPlace.getText().toString().trim();
        String selectedTeacherId = teacherIds.get(spinnerTeacher.getSelectedItemPosition());

        if (newName.isEmpty() || newDescription.isEmpty() || newPlace.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (assetId == -1) {
            Toast.makeText(this, "ID секции не найден", Toast.LENGTH_SHORT).show();
            return;
        }
// Сначала пробуем найти документ по "Id"
        db.collection("Asset")
                .whereEqualTo("Id", assetId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        updateAssetDocument(querySnapshot.getDocuments().get(0), newName, newDescription, newPlace, selectedTeacherId);
                    } else {
                        // Если по "Id" не нашли — пробуем по "id"
                        db.collection("Asset")
                                .whereEqualTo("id", assetId)
                                .get()
                                .addOnSuccessListener(snapshot2 -> {
                                    if (!snapshot2.isEmpty()) {
                                        updateAssetDocument(snapshot2.getDocuments().get(0), newName, newDescription, newPlace, selectedTeacherId);
                                    } else {
                                        Toast.makeText(this, "Документ не найден", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка поиска документа (id): " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка поиска документа (Id): " + e.getMessage(), Toast.LENGTH_SHORT).show());


       }
    private void updateAssetDocument(DocumentSnapshot document, String newName, String newDescription, String newPlace, String teacherId) {
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("Title", newName);
        updatedData.put("Description", newDescription);
        updatedData.put("Place", newPlace);
        updatedData.put("Id_Employee", teacherId);

        if (imageChanged && imageBase64OrUrl != null && !imageBase64OrUrl.isEmpty()) {
            updatedData.put("ImageBase64", imageBase64OrUrl);
        }

        db.collection("Asset")
                .document(document.getId())
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Секция обновлена", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка при обновлении: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void loadImage(String base64OrUrl) {
        if (base64OrUrl == null || base64OrUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.school2);
            return;
        }

        if (base64OrUrl.startsWith("http")) {
            glideRequestManager
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

    private void uploadImageToImgBB(Bitmap bitmap, int attempt) {
        if (attempt > MAX_UPLOAD_ATTEMPTS) {
            runOnUiThread(() -> {
                Log.d("DEBUG", "Max upload attempts (" + MAX_UPLOAD_ATTEMPTS + ") reached");
                Toast.makeText(this, "Не удалось загрузить изображение после " + MAX_UPLOAD_ATTEMPTS + " попыток", Toast.LENGTH_LONG).show();
                saveButton.setEnabled(true);
            });
            return;
        }

        Log.d("DEBUG", "Attempting to upload image, attempt #" + attempt);
        runOnUiThread(() -> Toast.makeText(this, "Загрузка изображения (попытка " + attempt + ")...", Toast.LENGTH_SHORT).show());

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
                Log.e("DEBUG", "Image upload failed on attempt #" + attempt + ": " + e.getMessage());
                // Добавляем задержку перед следующей попыткой
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    uploadImageToImgBB(bitmap, attempt + 1); // Рекурсивный вызов
                }, 1000); // Задержка 1 секунда
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("DEBUG", "Image upload failed on attempt #" + attempt + ": HTTP " + response.code());
                    // Добавляем задержку перед следующей попыткой
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        uploadImageToImgBB(bitmap, attempt + 1); // Рекурсивный вызов
                    }, 1000); // Задержка 1 секунда
                    return;
                }

                String res = response.body().string();
                String url = parseImgBBUrl(res);

                if (url != null) {
                    runOnUiThread(() -> {
                        Log.d("DEBUG", "Image uploaded successfully on attempt #" + attempt + ": " + url);
                        imageBase64OrUrl = url;
                        imageChanged = true;
                        glideRequestManager
                                .load(url)
                                .placeholder(R.drawable.school2)
                                .error(R.drawable.school2)
                                .into(imageView);
                        Toast.makeText(Admin_asset_edit.this, "Изображение загружено", Toast.LENGTH_SHORT).show();
                        saveButton.setEnabled(true); // Разблокируем кнопку после успешной загрузки
                    });
                } else {
                    Log.e("DEBUG", "Failed to parse ImgBB URL on attempt #" + attempt);
                    // Добавляем задержку перед следующей попыткой
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        uploadImageToImgBB(bitmap, attempt + 1); // Рекурсивный вызов
                    }, 1000); // Задержка 1 секунда
                }
            }
        });
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
    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(Color.parseColor("#D5BDAF"));
        }
    }
}
