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
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myidealclassapp.Adapters.AdminStudentAdapter;
import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Classes.Student;
import com.example.myidealclassapp.Dropdown_menu.Admin_dropdown_menu;
import com.example.myidealclassapp.R;
import com.example.myidealclassapp.Utilits.LoadingUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Admin_student extends AppCompatActivity {

    private static final int ADD_STUDENT_REQUEST_CODE = 102;
    private String employeeId;
    private RecyclerView recyclerView;
    private AdminStudentAdapter adapter;
    private List<Student> studentList = new ArrayList<>();
    private Map<String, String> classIdToNameMap = new HashMap<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_student);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                Admin_dropdown_menu.showCustomPopupMenu(view, this, employeeId)
        );
        hideSystemUI();
        com.example.myidealclassapp.Utilits.LoadingUtil.showLoading(this);
        recyclerView = findViewById(R.id.homeworkRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Загружаем классы, потом студентов
        loadClassMapThenStudents();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("employeeId")) {
            employeeId = intent.getStringExtra("employeeId");
            Log.d("DEBUG", "Employee ID: " + employeeId);
        } else {
            employeeId = "";
            Log.d("DEBUG", "Employee ID не передан");
        }
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
    public void moreButton(View view) {
        Intent intent = new Intent(this, Admin_student_add.class);
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
    private void loadClassMapThenStudents() {
        db.collection("Class")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String id = String.valueOf(doc.getLong("id")); // Firestore хранит id как Long
                        String name = doc.getString("name"); // Или "number", если ты хранишь номер класса в другом поле
                        classIdToNameMap.put(id, name != null ? name : "Неизвестно");
                    }
                    loadStudents(); // Загружаем студентов после того, как есть мапа классов
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Не удалось загрузить классы", e);
                    loadStudents(); // даже если ошибка — всё равно загрузи студентов
                });
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadStudents(); // обновляем данные каждый раз при возвращении
    }
    private void loadStudents() {
        db.collection("Student")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    studentList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Student student = doc.toObject(Student.class);
                        studentList.add(student);
                    }
                    adapter = new AdminStudentAdapter(this, studentList, classIdToNameMap);
                    recyclerView.setAdapter(adapter);
                    LoadingUtil.hideLoadingWithAnimation(this);
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Ошибка при загрузке учеников", e));
    }

}
