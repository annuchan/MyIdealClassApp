package com.example.myidealclassapp.Teacher;

import static com.example.myidealclassapp.Dropdown_menu.Teacher_dropdown_menu.showCustomPopupMenu;

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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class Teacher_main_window extends AppCompatActivity {

    private String employeeId;
    private int idSubject; // 🔥 ID предмета

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_main_window);

        // 🔥 ПОЛУЧАЕМ ID СОТРУДНИКА И ID ПРЕДМЕТА ИЗ INTENT
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("employeeId")) {
                employeeId = intent.getStringExtra("employeeId");
            } else {
                employeeId = "";
            }

            if (intent.hasExtra("subjectId")) {
                idSubject = intent.getIntExtra("subjectId", -1);
            } else {
                idSubject = -1;
            }

            Log.d("DEBUG", "Employee ID: " + employeeId);
            Log.d("DEBUG_SUBJECT", "subjectId: " + idSubject); // ← он должен быть НЕ -1

        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Кнопка выпадающего меню
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                showCustomPopupMenu(view, this, employeeId, idSubject) // 🔥 employeeId передаётся в меню
        );

        hideSystemUI();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, Autorization.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void moreButton(View view) {
        // 🔥 ПЕРЕДАЁМ ID СОТРУДНИКА И ПРЕДМЕТА В ДРУГУЮ АКТИВНОСТЬ
        Intent intent = new Intent(this, Teacher_main_window_more.class);
        intent.putExtra("employeeId", employeeId);
        intent.putExtra("subjectId", idSubject);
        startActivity(intent);
    }

    public void toMain(View view) {
        // 🔥 Повторный запуск этой активности
        Intent intent = new Intent(this, Teacher_main_window.class);
        intent.putExtra("employeeId", employeeId);
        intent.putExtra("subjectId", idSubject);
        startActivity(intent);
    }

    public void about_the_app(View view) {
        // 🔥 ПЕРЕДАЁМ ID СОТРУДНИКА И ПРЕДМЕТА
        Intent intent = new Intent(this, Teacher_about_the_app.class);
        intent.putExtra("employeeId", employeeId);
        intent.putExtra("subjectId", idSubject);
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
}
