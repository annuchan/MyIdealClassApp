package com.example.myidealclassapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myidealclassapp.Admin.Admin_main_window;
import com.example.myidealclassapp.Parent.Parent_main_window;
import com.example.myidealclassapp.Teacher.Teacher_main_window;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Autorization extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private int idSubject = 0;  // Changed from Long to int, default 0
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_autorization);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.loginEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButtonEnter);

        loginButton.setOnClickListener(v -> loginUser());
        hideSystemUI();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
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

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Введите email и пароль", Toast.LENGTH_SHORT).show();
            return;
        }
        com.example.myidealclassapp.Utilits.LoadingUtil.showLoading(this);
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            loadUserRole(user.getUid());
                        }
                    } else {
                        Toast.makeText(this, "Ошибка входа: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadUserRole(String uid) {
        db.collection("Users").document(uid).get()
                .addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists()) {
                        Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Long roleIdLong = userDoc.getLong("Id_Role");
                    if (roleIdLong == null) {
                        Toast.makeText(this, "Роль не указана", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int roleId = roleIdLong.intValue();

                    switch (roleId) {
                        case 1:
                            // Родитель
                            handleParentLogin(userDoc.getString("Id_parent"));
                            break;
                        case 2:
                        case 3:
                            // Администратор или Учитель
                            handleEmployeeLogin(userDoc.getString("Id_Employee"));
                            break;
                        default:
                            Toast.makeText(this, "Неизвестная роль: " + roleId, Toast.LENGTH_SHORT).show();
                            break;
                    }
                })
                .addOnFailureListener(e -> {
                    com.example.myidealclassapp.Utilits.LoadingUtil.hideLoadingWithAnimation(this);
                    Toast.makeText(this, "Ошибка загрузки пользователя", Toast.LENGTH_SHORT).show();
                });
    }

    private void handleParentLogin(String idParent) {
        if (idParent == null || idParent.isEmpty()) {
            Toast.makeText(this, "Id_parent отсутствует", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Parent").document(idParent).get()
                .addOnSuccessListener(parentDoc -> {
                    if (!parentDoc.exists()) {
                        Toast.makeText(this, "Родитель не найден", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Number idStudentNum = (Number) parentDoc.get("Id_Student");
                    if (idStudentNum == null) {
                        Toast.makeText(this, "Id_Student отсутствует", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int idStudent = idStudentNum.intValue();
                    openParentWindow(idStudent);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки родителя", Toast.LENGTH_SHORT).show());
    }

    private void handleEmployeeLogin(String idEmployee) {
        if (idEmployee == null || idEmployee.isEmpty()) {
            Toast.makeText(this, "Id_Employee отсутствует", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Employees").document(idEmployee).get()
                .addOnSuccessListener(empDoc -> {
                    if (!empDoc.exists()) {
                        Toast.makeText(this, "Сотрудник не найден", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Long adminFlagLong = empDoc.getLong("Administration");
                    Long idSubjectLong = empDoc.getLong("Id_Subject");

                    if (idSubjectLong == null) {
                        Toast.makeText(this, "Id_Subject не найден", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    idSubject = idSubjectLong.intValue();
                    int adminFlag = adminFlagLong != null ? adminFlagLong.intValue() : 0;

                    if (adminFlag == 1) {
                        showAccountSelectDialog(idEmployee, idSubject);
                    } else {
                        openTeacherWindow(idEmployee, idSubject);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка загрузки сотрудника", Toast.LENGTH_SHORT).show());
    }

    private void openParentWindow(int currentStudentId) {
        Intent intent = new Intent(this, Parent_main_window.class);
        intent.putExtra("EXTRA_STUDENT_ID", currentStudentId);
        startActivity(intent);
        finish();
    }

    private void openTeacherWindow(String employeeId, int subjectId) {
        Log.d("LOGIN_DEBUG", "Открываем Teacher_main_window для сотрудника: " + employeeId);
        Intent intent = new Intent(this, Teacher_main_window.class);
        intent.putExtra("employeeId", employeeId);
        intent.putExtra("subjectId", subjectId);
        startActivity(intent);
        finish();
    }

    private void openAdminWindow(String employeeId) {
        Intent intent = new Intent(this, Admin_main_window.class);
        intent.putExtra("Id_Employee", employeeId);
        startActivity(intent);
        finish();
    }

    private void showAccountSelectDialog(String employeeId, int subjectId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.item_select_account, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        // Начальная прозрачность 0
        dialogView.setAlpha(0f);
        dialog.show();

        // Плавное появление (fade in)
        dialogView.animate().alpha(1f).setDuration(300).start();

        Button adminBtn = dialogView.findViewById(R.id.admin_select);
        Button teacherBtn = dialogView.findViewById(R.id.teacher_select);

        adminBtn.setOnClickListener(v -> {
            // Плавное исчезновение (fade out)
            dialogView.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                openAdminWindow(employeeId);
                dialog.dismiss();
            }).start();
        });

        teacherBtn.setOnClickListener(v -> {
            // Плавное исчезновение (fade out)
            dialogView.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                openTeacherWindow(employeeId, subjectId);
                dialog.dismiss();
            }).start();
        });
    }

}
