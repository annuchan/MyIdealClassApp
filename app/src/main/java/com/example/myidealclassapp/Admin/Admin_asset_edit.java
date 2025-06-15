package com.example.myidealclassapp.Admin;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Dropdown_menu.Admin_dropdown_menu;
import com.example.myidealclassapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Admin_asset_edit extends AppCompatActivity {
    private String employeeId;
    private EditText editName, editDescription, editPlace;
    private Spinner spinnerTeacher;
    private Button saveButton;

    private FirebaseFirestore db;

    private int assetId = -1;
    private String originalName, originalDescription, originalPlace, idTeacher;

    private List<String> teacherNames = new ArrayList<>();
    private List<String> teacherIds = new ArrayList<>();
    private int selectedTeacherIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_asset_edit);

        editName = findViewById(R.id.addTitle);
        editDescription = findViewById(R.id.addDescrip);
        editPlace = findViewById(R.id.addtype);
        spinnerTeacher = findViewById(R.id.teacherSpinner);
        saveButton = findViewById(R.id.moreButton);
        db = FirebaseFirestore.getInstance();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);

        Intent intent = getIntent();
        assetId = intent.getIntExtra("Id", -1);
        originalName = intent.getStringExtra("Name");

        originalDescription = intent.getStringExtra("Description");

        originalPlace = intent.getStringExtra("Place");

        idTeacher = intent.getStringExtra("Id_Employee");

        editName.setText(originalName);
        editDescription.setText(originalDescription);
        editPlace.setText(originalPlace);

        loadTeachers();

        saveButton.setOnClickListener(v -> saveChanges());

        if (intent != null && intent.hasExtra("employeeId")) {
            employeeId = intent.getStringExtra("employeeId");

            Log.d("DEBUG", "Employee ID: " + employeeId);
        } else {
            employeeId = "";
            Log.d("DEBUG", "Employee ID не передан");

        }

        View imageView = findViewById(R.id.dropdown_menu);
        imageView.setOnClickListener(view ->
                Admin_dropdown_menu.showCustomPopupMenu(view, this, employeeId));

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

        if (newName.isEmpty() ||
                newDescription.isEmpty() ||
                newPlace.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (assetId == -1) {
            Toast.makeText(this, "ID секции не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Asset")
                .whereEqualTo("Id", assetId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        updateAssetDocument(querySnapshot.getDocuments().get(0), newName, newDescription, newPlace, selectedTeacherId);
                    } else {
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
