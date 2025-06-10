package com.example.myidealclassapp.Teacher;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myidealclassapp.Admin.Admin_about_the_app;
import com.example.myidealclassapp.Admin.Admin_asset;
import com.example.myidealclassapp.Admin.Admin_important_information;
import com.example.myidealclassapp.Admin.Admin_main_window;
import com.example.myidealclassapp.Admin.Admin_measure;
import com.example.myidealclassapp.Admin.Admin_parent;
import com.example.myidealclassapp.Admin.Admin_student;
import com.example.myidealclassapp.Admin.Admin_teacher;
import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Dropdown_menu.Admin_dropdown_menu;
import com.example.myidealclassapp.Dropdown_menu.Teacher_dropdown_menu;
import com.example.myidealclassapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class Teacher_main_window_more extends AppCompatActivity {
    private String employeeId;
    private int idSubject;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_main_window_more);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                Teacher_dropdown_menu.showCustomPopupMenu(view, this, employeeId, idSubject)
        );
        hideSystemUI();
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("employeeId")) {
                employeeId = intent.getStringExtra("employeeId");
            }
        }
        idSubject = getIntent().getIntExtra("subjectId", 2); // читаем subjectId из интента, по умолчанию 2
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
        Intent intent = new Intent(this, Teacher_main_window.class);
        intent.putExtra("employeeId", employeeId);
        intent.putExtra("subjectId", idSubject);
        startActivity(intent);
    }
    public void about_the_app(View view) {
        Intent intent = new Intent(this, Teacher_about_the_app.class);      intent.putExtra("employeeId", employeeId);
        intent.putExtra("subjectId", idSubject);

        startActivity(intent);
    }
    public void toImportInfo(View view) {
        Intent intent = new Intent(this, Teacher_Important_information.class);
        intent.putExtra("employeeId", employeeId);
        intent.putExtra("subjectId", idSubject);
        startActivity(intent);
    }
    public void toMeasure(View view) {
        Intent intent = new Intent(this, Teacher_measure.class);
        intent.putExtra("employeeId", employeeId);
        intent.putExtra("subjectId", idSubject);
        startActivity(intent);
    }
    public void toAsset(View view) {
        Intent intent = new Intent(this, Teacher_asset.class);
        intent.putExtra("employeeId", employeeId);
        intent.putExtra("subjectId", idSubject);
        startActivity(intent);
    }
    public void toEvalution(View view) {
        Intent intent = new Intent(this, teacher_evalution.class);
        intent.putExtra("employeeId", employeeId);
        intent.putExtra("subjectId", idSubject);
        startActivity(intent);
    }
    public void toFinalEvalution(View view) {
        Intent intent = new Intent(this, Teacher_final_evalution.class);
        intent.putExtra("employeeId", employeeId);
        intent.putExtra("subjectId", idSubject);
        startActivity(intent);
    }
    public void toHomework(View view) {
        Intent intent = new Intent(this, Teacher_homework.class);
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