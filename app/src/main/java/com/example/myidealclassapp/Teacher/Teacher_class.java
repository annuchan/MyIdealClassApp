package com.example.myidealclassapp.Teacher;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;

import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Dropdown_menu.Teacher_dropdown_menu;
import com.example.myidealclassapp.R;
import com.example.myidealclassapp.Utilits.LoadingUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class Teacher_class extends AppCompatActivity {

    private String employeeId;
    private int idSubject;
    private int idClass;

    private TableLayout tableLayout;
    private LinearLayout studentContainer;
    private LinearLayout parentContainer;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_teacher_class);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);

        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                Teacher_dropdown_menu.showCustomPopupMenu(view, this, employeeId, idSubject)
        );
        com.example.myidealclassapp.Utilits.LoadingUtil.showLoading(this);
        tableLayout = findViewById(R.id.tableLayout);
        studentContainer = findViewById(R.id.studentContainer);
        parentContainer = findViewById(R.id.parentContainer);
        db = FirebaseFirestore.getInstance();

        hideSystemUI();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("employeeId")) {
            employeeId = intent.getStringExtra("employeeId");
        }
        idSubject = intent.getIntExtra("subjectId", 2);

        getTeacherClassId();
    }

    private void getTeacherClassId() {
        db.collection("Employees").document(employeeId).get().addOnSuccessListener(document -> {
            if (document.exists() && document.contains("Id_Class")) {
                idClass = ((Long) document.get("Id_Class")).intValue();
                loadStudents();
            }
        });
    }

    private void loadStudents() {
        db.collection("Student").whereEqualTo("Id_Class", idClass).get().addOnSuccessListener(query -> {
            int index = 1;
            tableLayout.removeAllViews();
            for (QueryDocumentSnapshot doc : query) {
                String fullName = doc.getString("LastName") + " " +
                        doc.getString("FirstName") + " " +
                        doc.getString("MiddleName");

                TableRow row = new TableRow(this);
                row.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                ));

                View cell1 = LayoutInflater.from(this).inflate(R.layout.item_evalution_2, row, false);
                TextView numberView = cell1.findViewById(R.id.cellText);
                numberView.setText(String.valueOf(index));

                View cell2 = LayoutInflater.from(this).inflate(R.layout.item_evalution_2, row, false);
                TextView nameView = cell2.findViewById(R.id.cellText);
                nameView.setText(fullName);

                row.addView(cell1);
                row.addView(cell2);

                String studentId = doc.getId();
                row.setOnClickListener(v -> showStudentInfo(studentId));

                tableLayout.addView(row);
                index++;
            }
            LoadingUtil.hideLoadingWithAnimation(this);
        });
    }

    private void showStudentInfo(String studentId) {
        studentContainer.removeAllViews();
        parentContainer.removeAllViews();

        db.collection("Student").document(studentId).get().addOnSuccessListener(studentDoc -> {
            if (studentDoc.exists()) {
                // Отображаем данные ученика
                View studentView = LayoutInflater.from(this).inflate(R.layout.item_teacher_student, null);
                setText(studentView, R.id.LastName, studentDoc.getString("LastName"));
                setText(studentView, R.id.FirstName, studentDoc.getString("FirstName"));
                setText(studentView, R.id.MiddleName, studentDoc.getString("MiddleName"));
                setText(studentView, R.id.Date_Of_Birth, studentDoc.getString("Date_Of_Birth"));
                setText(studentView, R.id.Address, studentDoc.getString("Address"));
                setText(studentView, R.id.Phone, studentDoc.getString("Phone"));
                setText(studentView, R.id.Id_Class, String.valueOf(studentDoc.getLong("Id_Class")));

                studentContainer.addView(studentView);

                // Получаем числовой идентификатор ученика, который хранится в поле "id" (пример)
                Long studentNumberId = studentDoc.getLong("Id"); // заменить "id" на правильное имя поля, если другое

                if (studentNumberId != null) {
                    db.collection("Parent")
                            .whereEqualTo("Id_Student", studentNumberId.intValue())
                            .get()
                            .addOnSuccessListener(parents -> {
                                Log.d("DEBUG", "Найдено родителей: " + parents.size());
                                for (DocumentSnapshot parentDoc : parents) {
                                    View parentView = LayoutInflater.from(this).inflate(R.layout.item_teacher_parent, null);
                                    setText(parentView, R.id.LastName, parentDoc.getString("LastName"));
                                    setText(parentView, R.id.FirstName, parentDoc.getString("FirstName"));
                                    setText(parentView, R.id.MiddleName, parentDoc.getString("MiddleName"));
                                    setText(parentView, R.id.Date_Of_Birth, parentDoc.getString("Date_Of_Birth"));
                                    setText(parentView, R.id.Address, parentDoc.getString("Address"));
                                    setText(parentView, R.id.Phone, parentDoc.getString("Phone"));
                                    setText(parentView, R.id.Email, parentDoc.getString("Email"));

                                    parentContainer.addView(parentView);
                                }
                            });
                } else {
                    Log.d("DEBUG", "У ученика нет числового идентификатора 'id'");
                }
            }
        });
    }

    private void setText(View view, int resId, String text) {
        TextView tv = view.findViewById(resId);
        tv.setText(text != null ? text : "");
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