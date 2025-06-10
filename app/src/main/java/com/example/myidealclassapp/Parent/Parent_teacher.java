package com.example.myidealclassapp.Parent;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myidealclassapp.Adapters.ParentTeacherAdapter;
import com.example.myidealclassapp.Adapters.SubjectAdapter;
import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Classes.Employees;
import com.example.myidealclassapp.Dropdown_menu.Parent_dropdown_menu;
import com.example.myidealclassapp.R;
import com.example.myidealclassapp.Utilits.LoadingUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parent_teacher extends AppCompatActivity {
    private int currentStudentId;
    private Spinner spinnerSubject;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<String> subjectNames = new ArrayList<>();
    private final Map<Integer, String> subjectMap = new HashMap<>();
    private RecyclerView recyclerView;
    private ParentTeacherAdapter teacherAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_teacher);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);
        currentStudentId = getIntent().getIntExtra("EXTRA_STUDENT_ID", -1);
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                Parent_dropdown_menu.showCustomPopupMenu(view, this, currentStudentId)
        );
        hideSystemUI();
        recyclerView = findViewById(R.id.recyclerViewTeachers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        spinnerSubject = findViewById(R.id.spinner_subject);
        loadSubjects(); // Загружаем предметы
        com.example.myidealclassapp.Utilits.LoadingUtil.showLoading(this);
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
    private void loadSubjects() {
        subjectNames.clear();
        subjectNames.add("Все предметы");

        db.collection("Subjects")
                .get()
                .addOnSuccessListener(task -> {
                    for (DocumentSnapshot doc : task.getDocuments()) {
                        Long idLong = doc.getLong("Id");
                        String name = doc.getString("Title");
                        if (idLong != null && name != null) {
                            int id = idLong.intValue();
                            subjectMap.put(id, name);
                            subjectNames.add(name);
                        }
                    }

                    SubjectAdapter adapter = new SubjectAdapter(this, subjectNames);
                    spinnerSubject.setAdapter(adapter);

                    spinnerSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedSubject = subjectNames.get(position);
                            loadTeachers(selectedSubject);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });

                    loadTeachers("Все предметы");
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки предметов", Toast.LENGTH_SHORT).show());
    }

    private void loadTeachers(String selectedSubject) {
        db.collection("Employees")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Employees> teachers = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Employees emp = doc.toObject(Employees.class);
                        if (emp != null) {
                            // Найдём название предмета по Id_Subject
                            Long idSubj = doc.getLong("Id_Subject");
                            String subjectTitle = "Неизвестно";
                            if (idSubj != null) {
                                int subjectId = idSubj.intValue();
                                subjectTitle = subjectMap.containsKey(subjectId) ? subjectMap.get(subjectId) : "Неизвестно";

                            }

                            // Добавим это название в объект Employees (добавьте поле subjectTitle в модель)
                            emp.setSubjectTitle(subjectTitle);

                            if (selectedSubject.equals("Все предметы") ||
                                    subjectTitle.equalsIgnoreCase(selectedSubject)) {
                                teachers.add(emp);
                            }
                            LoadingUtil.hideLoadingWithAnimation(this);
                        }
                    }

                    teacherAdapter = new ParentTeacherAdapter(this, teachers);
                    recyclerView.setAdapter(teacherAdapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка загрузки учителей", Toast.LENGTH_SHORT).show());
    }
}
