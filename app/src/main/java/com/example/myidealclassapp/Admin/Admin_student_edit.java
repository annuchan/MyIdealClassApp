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
import com.example.myidealclassapp.Classes.ClassModel;
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

public class Admin_student_edit extends AppCompatActivity {

    private EditText lastNameEditText, firstNameEditText, middleNameEditText,
            dateOfBirthEditText, addressEditText, phoneEditText;
    private Spinner classSpinner;
    private Button saveButton;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private List<ClassModel> classList = new ArrayList<>();
    private List<String> classNames = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private String employeeId;
    private long studentId; // id студента, которого редактируем (примитив long)
    private Long originalClassId; // исходный класс студента для выбора в Spinner (Long)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_student_edit);

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

        // Получаем данные студента из Intent
        studentId = getIntent().getLongExtra("Id", -1L);
        if (studentId == -1L) {
            Toast.makeText(this, "Ошибка: неверный ID студента", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d("Admin_student_edit", "studentId from intent = " + studentId);

        String origFirst = getIntent().getStringExtra("FirstName");
        String origLast = getIntent().getStringExtra("LastName");
        String origMiddle = getIntent().getStringExtra("MiddleName");
        String origDOB = getIntent().getStringExtra("Date_Of_Birth");
        String origAddress = getIntent().getStringExtra("Address");
        String origPhone = getIntent().getStringExtra("Phone");
        originalClassId = getIntent().getLongExtra("Id_Class", -1L);

        // Заполняем поля
        firstNameEditText.setText(origFirst);
        lastNameEditText.setText(origLast);
        middleNameEditText.setText(origMiddle);
        dateOfBirthEditText.setText(origDOB);
        addressEditText.setText(origAddress);
        phoneEditText.setText(origPhone);

        loadClasses(); // загружаем классы

        saveButton.setOnClickListener(v -> updateStudent());
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
        db.collection("Class")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    classList.clear();
                    classNames.clear();

                    int indexToSelect = -1;
                    int idx = 0;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        ClassModel classItem = doc.toObject(ClassModel.class);
                        classList.add(classItem);
                        classNames.add(classItem.getNumber());

                        // Сравниваем id класса с исходным id студента
                        if (originalClassId != null && originalClassId != -1 && classItem.getId() == originalClassId) {
                            indexToSelect = idx;
                        }
                        idx++;
                    }
                    spinnerAdapter.notifyDataSetChanged();

                    if (indexToSelect != -1) {
                        classSpinner.setSelection(indexToSelect);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки списка классов", Toast.LENGTH_SHORT).show());
    }

    private void updateStudent() {
        String lastName = lastNameEditText.getText().toString().trim();
        String firstName = firstNameEditText.getText().toString().trim();
        String middleName = middleNameEditText.getText().toString().trim();
        String dateOfBirth = dateOfBirthEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        // Проверка на пустые поля
        if (lastName.isEmpty() || firstName.isEmpty() || middleName.isEmpty()
                || dateOfBirth.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        // Регулярные выражения для валидации
        String namePattern = "^[a-zA-Zа-яА-ЯёЁ]{1,50}$";  // только буквы (латиница и кириллица), до 50 символов
        String phonePattern = "^[0-9]{10,15}$";            // только цифры, 10-15 символов
        String dobPattern = "^([0-2][0-9]|3[0-1])\\.(0[1-9]|1[0-2])\\.\\d{4}$"; // формат dd.mm.yyyy

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
        if (!phone.matches(phonePattern)) {
            Toast.makeText(this, "Номер телефона должен содержать только цифры (10-15 символов)", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!dateOfBirth.matches(dobPattern)) {
            Toast.makeText(this, "Дата рождения должна быть в формате dd.mm.yyyy", Toast.LENGTH_SHORT).show();
            return;
        }
        if (address.length() > 100) {
            Toast.makeText(this, "Адрес не должен превышать 100 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        int pos = classSpinner.getSelectedItemPosition();
        if (pos == Spinner.INVALID_POSITION) {
            Toast.makeText(this, "Выберите класс из списка", Toast.LENGTH_SHORT).show();
            return;
        }

        int newClassId = classList.get(pos).getId();

        int studentIdInt = (int) studentId;  // кастинг long в int

        // Поиск документа студента по полю Id
        db.collection("Student")
                .whereEqualTo("Id", studentIdInt)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(this, "Студент не найден", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);

                    Map<String, Object> updated = new HashMap<>();
                    updated.put("LastName", lastName);
                    updated.put("FirstName", firstName);
                    updated.put("MiddleName", middleName);
                    updated.put("Date_Of_Birth", dateOfBirth);
                    updated.put("Address", address);
                    updated.put("Phone", phone);
                    updated.put("Id_Class", newClassId);

                    db.collection("Student")
                            .document(doc.getId())
                            .update(updated)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Данные студента обновлены", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Ошибка обновления: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка поиска студента: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

}
