package com.example.myidealclassapp.Admin;

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
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Classes.Asset;
import com.example.myidealclassapp.Dropdown_menu.Admin_dropdown_menu;
import com.example.myidealclassapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class Admin_asset_add extends AppCompatActivity {
    private ProgressBar progressBar;

    private String employeeId;
    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final String IMGBB_API_KEY = "972a14249ae8a675f7d1384d2a11bc0e";
    private static final String UPLOAD_URL = "https://api.imgbb.com/1/upload";
    private EditText titleEdit, descriptionEdit, placeEdit;
    private ImageView imageView;
    private Spinner teacherSpinner;
    private Button saveButton;
    private String encodedImage = "", uploadedImageUrl = "";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<String> teacherNames = new ArrayList<>();
    private Map<String, String> nameToIdMap = new HashMap<>();
    private String selectedTeacherId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_asset_add);

        titleEdit = findViewById(R.id.addTitle);
        descriptionEdit = findViewById(R.id.addDescrip);
        placeEdit = findViewById(R.id.addtype);
        imageView = findViewById(R.id.picture);
        teacherSpinner = findViewById(R.id.teacherSpinner);
        saveButton = findViewById(R.id.moreButton);


        progressBar = findViewById(R.id.progressBar);

        loadTeachers();
        imageView.setOnClickListener(v -> pickImageFromGallery());
        saveButton.setOnClickListener(v -> {
            saveButton.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            if (!encodedImage.isEmpty()) {
                uploadImageToImgBB();
            } else {
                saveAsset();
            }
        });
        teacherSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String name = teacherNames.get(pos);
                selectedTeacherId = nameToIdMap.get(name);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("employeeId")) {
            employeeId = intent.getStringExtra("employeeId");
            Log.d("DEBUG", "Employee ID: " + employeeId);
        } else {
            employeeId = "";
            Log.d("DEBUG", "Employee ID не передан");
        }
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
    private void loadTeachers() {
        db.collection("Employees")
                .get()
                .addOnSuccessListener(query -> {
                    teacherNames.clear();
                    nameToIdMap.clear();
                    for (DocumentSnapshot doc : query) {
                        String fullName = doc.getString("LastName");
                        String id = doc.getId();
                        if (fullName != null) {
                            teacherNames.add(fullName);
                            nameToIdMap.put(fullName, id);
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, teacherNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    teacherSpinner.setAdapter(adapter);
                });
    }
    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }
    @Override
    protected void onActivityResult(int reqCode, int resCode, @Nullable Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        if (reqCode == REQUEST_IMAGE_PICK && resCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);
                encodedImage = encodeImageToBase64(bitmap);
            } catch (IOException e) {
                Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private String encodeImageToBase64(Bitmap bitmap) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 800, 800, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 90;

        resized.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        while (baos.toByteArray().length > 500_000 && quality > 10) {
            baos.reset();
            quality -= 10;
            resized.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        }

        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }
    private void uploadImageToImgBB() {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, UPLOAD_URL,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        uploadedImageUrl = json.getJSONObject("data").getString("url");
                        saveAsset();
                    } catch (Exception e) {
                        Toast.makeText(this, "Ошибка обработки изображения", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        saveButton.setEnabled(true);
                        Log.e("ImgBB Upload", "JSON parse error", e);
                    }
                },
                error -> {
                    Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();

                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String body = new String(error.networkResponse.data);
                        Log.e("ImgBB Upload Error", "Status Code: " + error.networkResponse.statusCode + ", Body: " + body);
                    } else {
                        Log.e("ImgBB Upload Error", "No network response: " + error.toString());
                    }

                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                // Убираем префикс data:image/jpeg;base64, если есть
                String cleanImage = encodedImage.replaceAll("^data:image/[^;]+;base64,", "");
                params.put("key", IMGBB_API_KEY);
                params.put("image", cleanImage);

                return params;
            }
        };
        queue.add(request);
    }

    private void saveAsset() {
        String title = titleEdit.getText().toString().trim();
        String description = descriptionEdit.getText().toString().trim();
        String place = placeEdit.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || place.isEmpty() || selectedTeacherId == null) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("Asset")
                .orderBy("Id", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int nextId = 10; // стартуем с 10
                    if (!querySnapshot.isEmpty()) {
                        Long lastId = querySnapshot.getDocuments().get(0).getLong("id");
                        if (lastId != null) {
                            nextId = lastId.intValue() + 1;
                        }
                    }

                    Asset asset = new Asset(title, description, selectedTeacherId, place,  uploadedImageUrl);
                    asset.setId(nextId);

                    int finalNextId = nextId;
                    db.collection("Asset")
                            .add(asset)
                            .addOnSuccessListener(docRef -> {
                                Toast.makeText(this, "Секция добавлена с ID: " + finalNextId, Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Ошибка при добавлении", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка при получении ID", Toast.LENGTH_SHORT).show());
    }

}
