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
import com.example.myidealclassapp.Classes.ClassModel;
import com.example.myidealclassapp.Dropdown_menu.Admin_dropdown_menu;
import com.example.myidealclassapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Admin_student_add extends AppCompatActivity {

    private EditText lastNameEditText, firstNameEditText, middleNameEditText,
            dateOfBirthEditText, addressEditText, phoneEditText;
    private Spinner classSpinner;
    private Button saveButton;
    private String employeeId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private List<ClassModel> classList = new ArrayList<>();
    private List<String> classNames = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_student_add);

        lastNameEditText = findViewById(R.id.studentLastName);
        firstNameEditText = findViewById(R.id.studentFirstName);
        middleNameEditText = findViewById(R.id.studentMiddleName);
        dateOfBirthEditText = findViewById(R.id.studentDateOfBirth);
        addressEditText = findViewById(R.id.studentAddress);
        phoneEditText = findViewById(R.id.studentPhone);

        classSpinner = findViewById(R.id.ClassSpinner);
        saveButton = findViewById(R.id.savebutton);

        spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                classNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classSpinner.setAdapter(spinnerAdapter);

        loadClasses();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                Admin_dropdown_menu.showCustomPopupMenu(view, this, employeeId)
        );
        hideSystemUI();
        saveButton.setOnClickListener(v -> saveStudent());
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
    private void loadClasses() {
        db.collection("Class")  // коллекция с классами (замени на своё название)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    classList.clear();
                    classNames.clear();

                    for (var doc : querySnapshot) {
                        ClassModel classItem = doc.toObject(ClassModel.class);
                        // К примеру, класс отображаем как "5А" или "7Б" — зависит от у тебя структуры
                        String className = classItem.getNumber();  // предполагаем, что есть getName()
                        classList.add(classItem);
                        classNames.add(className);
                    }
                    spinnerAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки списка классов", Toast.LENGTH_SHORT).show());
    }

    private void saveStudent() {
        String lastName = lastNameEditText.getText().toString().trim();
        String firstName = firstNameEditText.getText().toString().trim();
        String middleName = middleNameEditText.getText().toString().trim();
        String dateOfBirth = dateOfBirthEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        // Регулярки для проверки
        String namePattern = "^[a-zA-Zа-яА-ЯёЁ]{1,50}$";  // только буквы, до 50 символов
        String phonePattern = "^[0-9]{10,15}$";            // только цифры, от 10 до 15 символов
        String dobPattern = "^([0-2][0-9]|3[0-1])\\.(0[1-9]|1[0-2])\\.\\d{4}$";  // dd.mm.yyyy

        // Проверяем фамилию, имя, отчество
        if (!lastName.matches(namePattern)) {
            Toast.makeText(this, "Фамилия должна содержать только буквы (до 50 символов)", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!firstName.matches(namePattern)) {
            Toast.makeText(this, "Имя должно содержать только буквы (до 50 символов)", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!middleName.matches(namePattern)) {
            Toast.makeText(this, "Отчество должно содержать только буквы (до 50 символов)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверяем телефон
        if (!phone.matches(phonePattern)) {
            Toast.makeText(this, "Номер телефона должен содержать только цифры (10-15 символов)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверяем дату рождения
        if (!dateOfBirth.matches(dobPattern)) {
            Toast.makeText(this, "Дата рождения должна быть в формате dd.mm.yyyy", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверяем адрес
        if (address.length() > 100 || address.isEmpty()) {
            Toast.makeText(this, "Адрес должен быть не пустым и не длиннее 100 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        int pos = classSpinner.getSelectedItemPosition();
        if (pos == Spinner.INVALID_POSITION) {
            Toast.makeText(this, "Выберите класс из списка", Toast.LENGTH_SHORT).show();
            return;
        }

        Long classId = Long.valueOf(classList.get(pos).getId());

        db.collection("Student")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    long maxId = 0L;
                    for (var doc : querySnapshot.getDocuments()) {
                        Long currentId = doc.getLong("Id");
                        if (currentId != null && currentId > maxId) {
                            maxId = currentId;
                        }
                    }
                    long nextId = maxId + 1;

                    HashMap<String, Object> student = new HashMap<>();
                    student.put("Id", nextId);
                    student.put("Id_Class", classId);
                    student.put("FirstName", firstName);
                    student.put("LastName", lastName);
                    student.put("MiddleName", middleName);
                    student.put("Date_Of_Birth", dateOfBirth);
                    student.put("Address", address);
                    student.put("Phone", phone);
                    student.put("Id_Asset", 0L);

                    db.collection("Student")
                            .add(student)
                            .addOnSuccessListener(docRef -> {
                                Toast.makeText(this, "Студент добавлен с Id " + nextId, Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Ошибка при добавлении студента", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка при получении данных студентов", Toast.LENGTH_SHORT).show());
    }

}
