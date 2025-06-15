package com.example.myidealclassapp.Admin;

import android.app.DatePickerDialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Classes.Subject;
import com.example.myidealclassapp.Dropdown_menu.Admin_dropdown_menu;
import com.example.myidealclassapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Admin_teacher_edit extends AppCompatActivity {
    private String employeeId;
    private EditText lastNameEditText, firstNameEditText, middleNameEditText,
            specialtyEditText, educationEditText, stashEditText, gramotEditText,
            phoneEditText, emailEditText, addressEditText;
    private Button saveButton;
    private Spinner subjectSpinner;
    private List<Subject> subjectList = new ArrayList<>();
    private int selectedSubjectId = -1; // сюда запишем Id выбранного предмета

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText parentDateOfBirth;
    private Calendar calendar = Calendar.getInstance();
    private long teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_teacher_edit);
        lastNameEditText = findViewById(R.id.LastName);
        firstNameEditText = findViewById(R.id.Firstname);
        middleNameEditText = findViewById(R.id.middlename);
        specialtyEditText = findViewById(R.id.Specialty_Teacher);
        educationEditText = findViewById(R.id.education);
        stashEditText = findViewById(R.id.stash);
        gramotEditText = findViewById(R.id.gramot);
        phoneEditText = findViewById(R.id.phone);
        emailEditText = findViewById(R.id.email);
        addressEditText = findViewById(R.id.adress);
        saveButton = findViewById(R.id.savebutton);
        subjectSpinner = findViewById(R.id.subject_teacher);
        parentDateOfBirth = findViewById(R.id.parentDateOfBirth);

        parentDateOfBirth.setOnClickListener(v -> showDatePickerDialog()); // вызываем диалог с календарём


        loadSubjects();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Log.d("Admin_teacher_edit", "Получены extras: " + extras.keySet());
            // Получаем из extras с приведением к Long, если нет, ставим -1
            Object idObj = extras.get("Id");
            if (idObj instanceof Long) {
                teacherId = (Long) idObj;
            } else if (idObj instanceof Integer) {
                teacherId = ((Integer) idObj).longValue();
            } else {
                teacherId = -1;
            }
        } else {
            Log.d("Admin_teacher_edit", "Нет extras вообще!");
            teacherId = -1;
        }

        if (teacherId == -1) {
            Toast.makeText(this, "Ошибка: неверный ID учителя", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d("Admin_teacher_edit", "teacherId from intent = " + teacherId);

        loadTeacherData(teacherId);

        saveButton.setOnClickListener(v -> updateTeacher());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                Admin_dropdown_menu.showCustomPopupMenu(view, this, employeeId)
        );
        hideSystemUI();
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("employeeId")) {
            employeeId = intent.getStringExtra("employeeId");
            Log.d("DEBUG", "Employee ID: " + employeeId);
        } else {
            employeeId = "";
            Log.d("DEBUG", "Employee ID не передан");
        }
    }
    private void showDatePickerDialog(){
        DatePickerDialog picker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            // Записываем результат в поле с датой
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            parentDateOfBirth.setText(dayOfMonth + "." + (month + 1) + "." + year);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        picker.show();
    }
    private void loadSubjects() {
        db.collection("Subjects")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    subjectList.clear();
                    for (var doc : querySnapshot.getDocuments()) {
                        Long idLong = doc.getLong("Id");
                        String title = doc.getString("Title");
                        if (idLong != null && title != null) {
                            subjectList.add(new Subject(idLong.intValue(), title));
                        }
                    }
                    if (!subjectList.isEmpty()) {
                        ArrayAdapter<Subject> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, subjectList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        subjectSpinner.setAdapter(adapter);

                        // По умолчанию выберем первый предмет
                        selectedSubjectId = subjectList.get(0).Id;
                        subjectSpinner.setSelection(0);
                    }

                    subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedSubjectId = subjectList.get(position).Id;
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedSubjectId = -1;
                        }
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки предметов", Toast.LENGTH_SHORT).show());
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

    private void loadTeacherData(long id) {
        db.collection("Employees")
                .whereEqualTo("Id", id)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(this, "Учитель не найден", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    parentDateOfBirth.setText(doc.getString("Date_Of_Birth"));
                    lastNameEditText.setText(doc.getString("LastName"));
                    firstNameEditText.setText(doc.getString("FirstName"));
                    middleNameEditText.setText(doc.getString("MiddleName"));
                    specialtyEditText.setText(doc.getString("Specialty"));
                    educationEditText.setText(doc.getString("Education"));

                    Long experience = doc.getLong("Experience");
                    stashEditText.setText(experience != null ? String.valueOf(experience) : "");

                    gramotEditText.setText(doc.getString("Certificate"));
                    phoneEditText.setText(doc.getString("Phone"));
                    emailEditText.setText(doc.getString("Email"));
                    addressEditText.setText(doc.getString("Address"));

                    // Картинку не трогаем
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки данных: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void updateTeacher() {
        String lastName = lastNameEditText.getText().toString().trim();
        String firstName = firstNameEditText.getText().toString().trim();
        String middleName = middleNameEditText.getText().toString().trim();
        String specialty = specialtyEditText.getText().toString().trim();
        String education = educationEditText.getText().toString().trim();
        String stashStr = stashEditText.getText().toString().trim();
        String gramot = gramotEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String birthDate = parentDateOfBirth.getText().toString().trim();
        // Проверка на пустые поля
        if (lastName.isEmpty() || firstName.isEmpty() || middleName.isEmpty()
                || specialty.isEmpty() || education.isEmpty() || stashStr.isEmpty()
                || gramot.isEmpty() || phone.isEmpty() || email.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }
        if (birthDate.isEmpty()) {
            Toast.makeText(this, "Укажите дату рождения", Toast.LENGTH_SHORT).show();
            return;
        }
        // Проверка ФИО — только буквы, максимум 50 символов
        String nameRegex = "^[А-Яа-яЁёA-Za-z]+$";
        if (lastName.length() > 50 || !lastName.matches(nameRegex)) {
            Toast.makeText(this, "Фамилия должна содержать только буквы и не более 50 символов", Toast.LENGTH_SHORT).show();
            return;
        }
        if (firstName.length() > 50 || !firstName.matches(nameRegex)) {
            Toast.makeText(this, "Имя должно содержать только буквы и не более 50 символов", Toast.LENGTH_SHORT).show();
            return;
        }
        if (middleName.length() > 50 || !middleName.matches(nameRegex)) {
            Toast.makeText(this, "Отчество должно содержать только буквы и не более 50 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        // Специальность - максимум 20 символов
        if (specialty.length() > 20) {
            Toast.makeText(this, "Специальность не должна превышать 20 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        // Образование - максимум 20 символов
        if (education.length() > 20) {
            Toast.makeText(this, "Образование не должно превышать 20 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        // Стаж - только цифры, максимум 2 символа
        if (!stashStr.matches("^\\d{1,2}$")) {
            Toast.makeText(this, "Стаж должен быть числом не более 2 цифр", Toast.LENGTH_SHORT).show();
            return;
        }
        int experience = Integer.parseInt(stashStr);

        // Телефон - только цифры, длина от 10 до 15 символов
        if (!phone.matches("^\\d{10,15}$")) {
            Toast.makeText(this, "Телефон должен содержать от 10 до 15 цифр", Toast.LENGTH_SHORT).show();
            return;
        }

        // Email - максимум 50 символов, проверка домена
        if (email.length() > 50) {
            Toast.makeText(this, "Email не должен превышать 50 символов", Toast.LENGTH_SHORT).show();
            return;
        }
        String emailDomainRegex = "^[\\w.-]+@(gmail\\.com|mail\\.com|yandex\\.com|mail\\.ru|yandex\\.ru)$";
        if (!email.matches(emailDomainRegex)) {
            Toast.makeText(this, "Email должен оканчиваться на @gmail.com, @mail.com, @yandex.com, @mail.ru или @yandex.ru", Toast.LENGTH_SHORT).show();
            return;
        }

        // Адрес - максимум 100 символов
        if (address.length() > 100) {
            Toast.makeText(this, "Адрес не должен превышать 100 символов", Toast.LENGTH_SHORT).show();
            return;
        }
        // Обновляем данные в Firestore
        db.collection("Employees")
                .whereEqualTo("Id", teacherId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(this, "Учитель не найден для обновления", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);

                    Map<String, Object> updated = new HashMap<>();
                    updated.put("LastName", lastName);
                    updated.put("FirstName", firstName);
                    updated.put("MiddleName", middleName);
                    updated.put("Specialty", specialty);
                    updated.put("Education", education);
                    updated.put("Experience", experience);
                    updated.put("Certificate", gramot);
                    updated.put("Phone", phone);
                    updated.put("Email", email);
                    updated.put("Address", address);
                    updated.put("Id_Subject", selectedSubjectId);
                    updated.put("Date_Of_Birth", birthDate);
                    db.collection("Employees")
                            .document(doc.getId())
                            .update(updated)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Данные учителя обновлены", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Ошибка обновления: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка поиска учителя: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

}
