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

import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Classes.Parent;
import com.example.myidealclassapp.Classes.Student;
import com.example.myidealclassapp.Dropdown_menu.Admin_dropdown_menu;
import com.example.myidealclassapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Admin_parent_edit extends AppCompatActivity {

    private EditText lastNameEditText, firstNameEditText, middleNameEditText;
    private EditText dobEditText, addressEditText, phoneEditText, emailEditText;
    private Spinner studentSpinner;
    private Button saveButton;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String employeeId;
    private List<Student> studentList = new ArrayList<>();
    private List<String> studentNames = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    private Long parentId;
    private Long originalChildId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_parent_edit);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                Admin_dropdown_menu.showCustomPopupMenu(view, this, employeeId)
        );
        hideSystemUI();

        lastNameEditText    = findViewById(R.id.parentLastName);
        firstNameEditText   = findViewById(R.id.parentFirstName);
        middleNameEditText  = findViewById(R.id.parentMiddleName);
        dobEditText         = findViewById(R.id.parentDateOfBirth);
        addressEditText     = findViewById(R.id.parentAddress);
        phoneEditText       = findViewById(R.id.parentPhone);
        emailEditText       = findViewById(R.id.parentEmail);
        studentSpinner      = findViewById(R.id.studentSpinner);
        saveButton          = findViewById(R.id.saveButton);

        // Считываем переданные данные
        parentId = getIntent().getLongExtra("id", -1L);
        if (parentId == -1L) {
            Toast.makeText(this, "Ошибка: неверный ID родителя", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String origFirst   = getIntent().getStringExtra("FirstName");
        String origLast    = getIntent().getStringExtra("LastName");
        String origMiddle  = getIntent().getStringExtra("MiddleName");
        String origDOB     = getIntent().getStringExtra("Date_Of_Birth");
        String origAddress = getIntent().getStringExtra("Address");
        String origPhone   = getIntent().getStringExtra("Phone");
        String origEmail   = getIntent().getStringExtra("Email");
        originalChildId    = getIntent().getLongExtra("Id_Student", -1L);

        // Заполняем поля
        firstNameEditText.setText(origFirst);
        lastNameEditText.setText(origLast);
        middleNameEditText.setText(origMiddle);
        dobEditText.setText(origDOB);
        addressEditText.setText(origAddress);
        phoneEditText.setText(origPhone);
        emailEditText.setText(origEmail);

        // Настраиваем адаптер для Spinner — список учеников
        spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                studentNames
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        studentSpinner.setAdapter(spinnerAdapter);

        loadStudents(); // загрузим студентов, после этого установим позицию
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("employeeId")) {
            employeeId = intent.getStringExtra("employeeId");
            Log.d("DEBUG", "Employee ID: " + employeeId);
        } else {
            employeeId = "";
            Log.d("DEBUG", "Employee ID не передан");
        }
        saveButton.setOnClickListener(v -> updateParent());
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
    private void loadStudents() {
        db.collection("Student")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    studentList.clear();
                    studentNames.clear();

                    int indexToSelect = -1;
                    int idx = 0;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Student s = doc.toObject(Student.class);
                        studentList.add(s);
                        String fullName = s.getLastName() + " " + s.getFirstName() + " " + s.getMiddleName();
                        studentNames.add(fullName);

                        if (s.getId().equals(originalChildId)) {
                            indexToSelect = idx;
                        }
                        idx++;
                    }
                    spinnerAdapter.notifyDataSetChanged();

                    if (indexToSelect != -1) {
                        studentSpinner.setSelection(indexToSelect);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка загрузки списка студентов", Toast.LENGTH_SHORT).show()
                );
    }

    private void updateParent() {
        String lastName  = lastNameEditText.getText().toString().trim();
        String firstName = firstNameEditText.getText().toString().trim();
        String middle    = middleNameEditText.getText().toString().trim();
        String dob       = dobEditText.getText().toString().trim();
        String address   = addressEditText.getText().toString().trim();
        String phone     = phoneEditText.getText().toString().trim();
        String email     = emailEditText.getText().toString().trim();

        if (lastName.isEmpty() || firstName.isEmpty() || middle.isEmpty()
                || dob.isEmpty() || address.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        // Валидация ФИО: только буквы, до 50 символов
        String nameRegex = "^[а-яА-ЯёЁa-zA-Z]{1,50}$";
        if (!lastName.matches(nameRegex)) {
            Toast.makeText(this, "Фамилия должна содержать только буквы и не более 50 символов", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!firstName.matches(nameRegex)) {
            Toast.makeText(this, "Имя должно содержать только буквы и не более 50 символов", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!middle.matches(nameRegex)) {
            Toast.makeText(this, "Отчество должно содержать только буквы и не более 50 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        // Валидация телефона: цифры, 10-15 символов
        String phoneRegex = "^[0-9]{10,15}$";
        if (!phone.matches(phoneRegex)) {
            Toast.makeText(this, "Телефон должен содержать только цифры и быть длиной 10-15 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        // Валидация почты: длина <= 50, домены
        if (email.length() > 50) {
            Toast.makeText(this, "Адрес электронной почты не должен превышать 50 символов", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] allowedDomains = {"@gmail.com", "@mail.com", "@yandex.com", "@mail.ru", "@yandex.ru"};
        boolean validEmail = false;
        for (String domain : allowedDomains) {
            if (email.endsWith(domain)) {
                validEmail = true;
                break;
            }
        }
        if (!validEmail) {
            Toast.makeText(this, "Почта должна заканчиваться на: @gmail.com, @mail.com, @yandex.com, @mail.ru или @yandex.ru", Toast.LENGTH_SHORT).show();
            return;
        }

        // Валидация даты рождения: формат ДД.ММ.ГГГГ
        String dateRegex = "^([0-2][0-9]|3[0-1])\\.(0[1-9]|1[0-2])\\.\\d{4}$";
        if (!dob.matches(dateRegex)) {
            Toast.makeText(this, "Дата рождения должна быть в формате ДД.ММ.ГГГГ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Валидация адреса: не более 100 символов
        if (address.length() > 100) {
            Toast.makeText(this, "Адрес не должен превышать 100 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверка выбора ребенка
        int pos = studentSpinner.getSelectedItemPosition();
        if (pos == Spinner.INVALID_POSITION) {
            Toast.makeText(this, "Выберите ребенка из списка", Toast.LENGTH_SHORT).show();
            return;
        }
        Long newChildId = studentList.get(pos).getId();

        // Обновляем данные в Firestore
        db.collection("Parent")
                .whereEqualTo("id", parentId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(this, "Запись не найдена", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    Map<String, Object> updated = new HashMap<>();
                    updated.put("FirstName", firstName);
                    updated.put("LastName", lastName);
                    updated.put("MiddleName", middle);
                    updated.put("Date_Of_Birth", dob);
                    updated.put("Address", address);
                    updated.put("Phone", phone);
                    updated.put("Email", email);
                    updated.put("Id_Student", newChildId);

                    db.collection("Parent")
                            .document(doc.getId())
                            .update(updated)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Данные родителя обновлены", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Ошибка при обновлении: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка поиска записи: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

}
