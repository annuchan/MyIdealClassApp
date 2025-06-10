package com.example.myidealclassapp.Parent;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Dropdown_menu.Parent_dropdown_menu;
import com.example.myidealclassapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.*;

public class Parent_Evalution extends AppCompatActivity {

    private TableLayout tableLayout;
    private FirebaseFirestore db;
    private int currentStudentId;

    private final Map<Long, String> subjectMap = new HashMap<>();
    private final Set<String> allDates = new TreeSet<>();
    private final Set<String> allSubjects = new TreeSet<>();
    private final Map<String, Map<String, Long>> marks = new HashMap<>();

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_evalution);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        tableLayout = findViewById(R.id.tableLayout);
        progressBar = findViewById(R.id.progressBar);

        // Получаем student_id из интента, если есть
        currentStudentId = getIntent().getIntExtra("EXTRA_STUDENT_ID", -1);

        db = FirebaseFirestore.getInstance();

        if (currentStudentId != -1) {
            loadSubjectsThenEvaluations();
        } else {
            Log.w("PARENT_EVALUTION", "student_id не передан, таблица не загружена");
            progressBar.setVisibility(View.GONE);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                Parent_dropdown_menu.showCustomPopupMenu(view, this, currentStudentId)
        );
        hideSystemUI();
    }

    private void loadSubjectsThenEvaluations() {
        subjectMap.clear();
        allDates.clear();
        allSubjects.clear();
        marks.clear();
        tableLayout.removeAllViews();

        db.collection("Subjects")
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        Long id = doc.getLong("Id");
                        String title = doc.getString("Title");
                        if (id != null && title != null) {
                            subjectMap.put(id, title);
                        }
                    }
                    loadEvaluations();
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE", "Ошибка загрузки предметов", e);
                });
    }

    private void loadEvaluations() {
        if (currentStudentId == -1) {
            Log.w("PARENT_EVALUTION", "currentStudentId не установлен");
            progressBar.setVisibility(View.GONE);
            return;
        }

        db.collection("Evaluation")
                .whereEqualTo("Id_Student", currentStudentId)
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        String date = doc.getString("Date_Evalution");
                        Long mark = doc.getLong("Evaluation_Number");
                        Long subjectId = doc.getLong("Id_Subject");

                        if (date == null || mark == null || subjectId == null) continue;

                        String subject = subjectMap.get(subjectId);
                        if (subject == null) subject = "Неизвестно";

                        allDates.add(date);
                        allSubjects.add(subject);

                        if (!marks.containsKey(subject))
                            marks.put(subject, new HashMap<>());

                        marks.get(subject).put(date, mark);
                    }

                    buildTable();
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE", "Ошибка при загрузке оценок", e);
                    progressBar.setVisibility(View.GONE);
                });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
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

    private void buildTable() {
        progressBar.setVisibility(View.GONE);
        tableLayout.setVisibility(View.VISIBLE);

        Log.d("DEBUG", "Строим таблицу...");
        Log.d("DEBUG", "allDates: " + allDates);
        Log.d("DEBUG", "allSubjects: " + allSubjects);
        Log.d("DEBUG", "marks: " + marks);
        Log.d("DEBUG", "Текущий студент ID: " + currentStudentId);

        // Заголовок таблицы (пустая + даты)
        TableRow header = new TableRow(this);
        header.addView(createCell("", true, false)); // пустая первая ячейка

        for (String date : allDates) {
            header.addView(createCell(date, false, false)); // даты
        }
        tableLayout.addView(header);

        // Строки по предметам
        for (String subject : allSubjects) {
            TableRow row = new TableRow(this);
            row.addView(createCell(subject, true, false)); // предмет

            for (String date : allDates) {
                Long mark = null;
                if (marks.containsKey(subject)) {
                    Map<String, Long> dateMap = marks.get(subject);
                    if (dateMap.containsKey(date)) {
                        mark = dateMap.get(date);
                    }
                }
                row.addView(createCell(mark != null ? String.valueOf(mark) : "", false, true)); // оценка
            }
            tableLayout.addView(row);
        }
    }

    private View createCell(String text, boolean alignLeft, boolean isMark) {
        View view = getLayoutInflater().inflate(R.layout.item_evalution, null);
        TextView cellText = view.findViewById(R.id.cellText);
        cellText.setText(text);

        LinearLayout layout = (LinearLayout) view;
        layout.setGravity(alignLeft ? Gravity.START | Gravity.CENTER_VERTICAL : Gravity.CENTER);

        if (isMark) {
            switch (text) {
                case "5":
                case "4":
                    layout.setBackgroundResource(R.drawable.evalution_4_5_border);
                    break;
                case "3":
                    layout.setBackgroundResource(R.drawable.evalution_3);
                    break;
                case "2":
                case "1":
                    layout.setBackgroundResource(R.drawable.evalution_2_1);
                    break;
                default:
                    layout.setBackgroundResource(R.drawable.evalution_subjects_and_dates);
                    break;
            }
        } else {
            layout.setBackgroundResource(R.drawable.evalution_4_5_border);
        }

        return view;
    }
    public void final_evalution(View view) {
        Intent intent = new Intent(this, Parent_evalution_final.class);
        intent.putExtra("EXTRA_STUDENT_ID", currentStudentId);
        startActivity(intent);
    }
}
