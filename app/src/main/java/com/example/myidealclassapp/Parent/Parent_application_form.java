package com.example.myidealclassapp.Parent;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myidealclassapp.Admin.Admin_about_the_app;
import com.example.myidealclassapp.Admin.Admin_main_window;
import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Dropdown_menu.Parent_dropdown_menu;
import com.example.myidealclassapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Parent_application_form extends AppCompatActivity {

    private FirebaseFirestore db;
    private Spinner spinnerClass, spinnerAsset;
    private Button submitButton;
    private int currentStudentId;
    private final ArrayList<String> classList = new ArrayList<>();
    private final ArrayList<Integer> classIds = new ArrayList<>();
    private final ArrayList<String> assetList = new ArrayList<>();
    private final ArrayList<Integer> assetIds = new ArrayList<>();

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_parent_application_form);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);
        currentStudentId = getIntent().getIntExtra("EXTRA_STUDENT_ID", -1);
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                Parent_dropdown_menu.showCustomPopupMenu(view, this, currentStudentId)
        );
        hideSystemUI();

        db = FirebaseFirestore.getInstance();
        spinnerClass = findViewById(R.id.spinnerClass);
        spinnerAsset = findViewById(R.id.spinnerAsset);
        submitButton = findViewById(R.id.Applicateformbutton);

        loadClassList();
        loadAssetList();

        submitButton.setOnClickListener(v -> sendApplicationForm());
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
        Intent intent = new Intent(this, Parent_main_window.class);
        intent.putExtra("EXTRA_STUDENT_ID", currentStudentId);
        startActivity(intent);
    }
    public void about_the_app(View view) {
        Intent intent = new Intent(this, Parent_about_the_app.class);
        intent.putExtra("EXTRA_STUDENT_ID", currentStudentId);
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
    private void loadClassList() {
        db.collection("Class")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    classList.clear();
                    classIds.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String number = doc.getString("Number");
                        Long id = doc.getLong("Id");
                        if (number != null && id != null) {
                            classList.add(number);
                            classIds.add(id.intValue());
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerClass.setAdapter(adapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка загрузки классов", Toast.LENGTH_SHORT).show()
                );
    }

    private void loadAssetList() {
        db.collection("Asset")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    assetList.clear();
                    assetIds.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String title = doc.getString("Title");
                        Long id = doc.getLong("Id");
                        if (title != null && id != null) {
                            assetList.add(title);
                            assetIds.add(id.intValue());
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, assetList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerAsset.setAdapter(adapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка загрузки секций", Toast.LENGTH_SHORT).show()
                );
    }

    private void sendApplicationForm() {
        int classIndex = spinnerClass.getSelectedItemPosition();
        int assetIndex = spinnerAsset.getSelectedItemPosition();

        if (classIndex < 0 || classIndex >= classIds.size()) {
            Toast.makeText(this, "Пожалуйста, выберите класс", Toast.LENGTH_SHORT).show();
            return;
        }

        if (assetIndex < 0 || assetIndex >= assetIds.size()) {
            Toast.makeText(this, "Пожалуйста, выберите секцию", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedClassId = classIds.get(classIndex);
        int selectedAssetId = assetIds.get(assetIndex);

        Map<String, Object> data = new HashMap<>();
        data.put("Id_Class", selectedClassId);
        data.put("Id_Asset", selectedAssetId);
        data.put("Id_Student", String.valueOf(currentStudentId)); // <-- как строка

        db.collection("Application_form")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Заявка успешно отправлена", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка при отправке заявки", Toast.LENGTH_SHORT).show()
                );
    }


}
