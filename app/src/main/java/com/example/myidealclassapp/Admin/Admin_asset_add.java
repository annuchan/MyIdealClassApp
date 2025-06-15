package com.example.myidealclassapp.Admin;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Classes.Asset;
import com.example.myidealclassapp.Dropdown_menu.Admin_dropdown_menu;
import com.example.myidealclassapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class Admin_asset_add extends AppCompatActivity {
    private ProgressBar progressBar;

    private String employeeId;

    private EditText titleEdit, descriptionEdit, placeEdit;

    private Spinner teacherSpinner;

    private Button saveButton;

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
        teacherSpinner = findViewById(R.id.teacherSpinner);
        saveButton = findViewById(R.id.moreButton);
        progressBar = findViewById(R.id.progressBar);

        loadTeachers();

        saveButton.setOnClickListener(v -> {
            saveAsset();
        });

        teacherSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String name = teacherNames.get(pos);
                selectedTeacherId = nameToIdMap.get(name);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
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
        dropdownMenu.setOnClickListener(view -> Admin_dropdown_menu.showCustomPopupMenu(view, this, employeeId));

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

    private void saveAsset() {
        String title = titleEdit.getText().toString().trim();
        String description = descriptionEdit.getText().toString().trim();
        String place = placeEdit.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() ||
                place.isEmpty() ||
                selectedTeacherId == null) {
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

                    Asset asset = new Asset(title, description, selectedTeacherId, place, "0");

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

