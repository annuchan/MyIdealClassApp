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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Classes.Parent;
import com.example.myidealclassapp.Classes.Student;
import com.example.myidealclassapp.Dropdown_menu.Admin_dropdown_menu;
import com.example.myidealclassapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class Admin_parent_add extends AppCompatActivity {

    private EditText lastNameEditText, firstNameEditText, middleNameEditText,
            phoneEditText, emailEditText, addressEditText, dateOfBirthEditText;

    private Spinner studentSpinner;
    private Button saveButton;
    private String employeeId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private List<Student> studentList = new ArrayList<>();
    private List<String> studentNames = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_admin_parent_add);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                Admin_dropdown_menu.showCustomPopupMenu(view, this, employeeId)
        );
        hideSystemUI();
        lastNameEditText = findViewById(R.id.parentLastName);
        firstNameEditText = findViewById(R.id.parentFirstName);
        middleNameEditText = findViewById(R.id.parentMiddleName);
        phoneEditText = findViewById(R.id.parentPhone);
        emailEditText = findViewById(R.id.parentEmail);
        addressEditText = findViewById(R.id.parentAddress);
        dateOfBirthEditText = findViewById(R.id.parentDateOfBirth);

        studentSpinner = findViewById(R.id.studentSpinner);
        saveButton = findViewById(R.id.saveButton);

        spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                studentNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        studentSpinner.setAdapter(spinnerAdapter);

        loadStudents();
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("employeeId")) {
            employeeId = intent.getStringExtra("employeeId");
            Log.d("DEBUG", "Employee ID: " + employeeId);
        } else {
            employeeId = "";
            Log.d("DEBUG", "Employee ID не передан");
        }
        saveButton.setOnClickListener(v -> saveParent());
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

                    for (var doc : querySnapshot) {
                        Student student = doc.toObject(Student.class);
                        String fullName = student.getLastName() + " " + student.getFirstName() + " " + student.getMiddleName();
                        studentList.add(student);
                        studentNames.add(fullName);
                    }
                    spinnerAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки списка студентов", Toast.LENGTH_SHORT).show());
    }

    private void saveParent() {
        String lastName = lastNameEditText.getText().toString().trim();
        String firstName = firstNameEditText.getText().toString().trim();
        String middleName = middleNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String dateOfBirth = dateOfBirthEditText.getText().toString().trim();

        // Проверка на пустые поля
        if (lastName.isEmpty() || firstName.isEmpty() || middleName.isEmpty()
                || phone.isEmpty() || email.isEmpty() || address.isEmpty() || dateOfBirth.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        // Валидация ФИО: только буквы, не более 50 символов
        String nameRegex = "^[а-яА-ЯёЁa-zA-Z]{1,50}$";
        if (!lastName.matches(nameRegex)) {
            Toast.makeText(this, "Фамилия должна содержать только буквы и не более 50 символов", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!firstName.matches(nameRegex)) {
            Toast.makeText(this, "Имя должно содержать только буквы и не более 50 символов", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!middleName.matches(nameRegex)) {
            Toast.makeText(this, "Отчество должно содержать только буквы и не более 50 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        // Валидация телефона: только цифры, длина от 10 до 15 (можно настроить)
        String phoneRegex = "^[0-9]{10,15}$";
        if (!phone.matches(phoneRegex)) {
            Toast.makeText(this, "Номер телефона должен содержать только цифры и быть длиной от 10 до 15 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        // Валидация почты: ограничение 50 символов и домены
        if (email.length() > 50) {
            Toast.makeText(this, "Адрес эл. почты не должен превышать 50 символов", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Адрес эл. почты должен оканчиваться на @gmail.com, @mail.com, @yandex.com, @mail.ru или @yandex.ru", Toast.LENGTH_SHORT).show();
            return;
        }

        // Валидация даты рождения: формат dd.mm.yyyy
        String dateRegex = "^([0-2][0-9]|3[0-1])\\.(0[1-9]|1[0-2])\\.\\d{4}$";
        if (!dateOfBirth.matches(dateRegex)) {
            Toast.makeText(this, "Дата рождения должна быть в формате ДД.ММ.ГГГГ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Валидация адреса: не более 100 символов
        if (address.length() > 100) {
            Toast.makeText(this, "Адрес не должен превышать 100 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверяем выбран ли ребенок из Spinner
        int pos = studentSpinner.getSelectedItemPosition();
        if (pos == Spinner.INVALID_POSITION) {
            Toast.makeText(this, "Выберите ребенка из списка", Toast.LENGTH_SHORT).show();
            return;
        }

        Long childId = studentList.get(pos).getId();

        // Генерация нового ID для родителя и сохранение в Firestore
        db.collection("Parent")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    long maxId = 0L;

                    for (var doc : querySnapshot.getDocuments()) {
                        Long currentId = doc.getLong("id");
                        if (currentId != null && currentId > maxId) {
                            maxId = currentId;
                        }
                    }

                    long nextId = maxId + 1;

                    var parent = new java.util.HashMap<String, Object>();
                    parent.put("id", nextId);
                    parent.put("FirstName", firstName);
                    parent.put("LastName", lastName);
                    parent.put("MiddleName", middleName);
                    parent.put("Phone", phone);
                    parent.put("Email", email);
                    parent.put("Address", address);
                    parent.put("Date_Of_Birth", dateOfBirth);
                    parent.put("Id_Student", childId);

                    db.collection("Parent")
                            .add(parent)
                            .addOnSuccessListener(docRef -> {
                                Toast.makeText(this, "Родитель добавлен с id " + nextId, Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Ошибка при добавлении родителя", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка при получении данных", Toast.LENGTH_SHORT).show());
    }


}