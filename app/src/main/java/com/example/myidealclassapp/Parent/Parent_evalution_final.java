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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Dropdown_menu.Parent_dropdown_menu;
import com.example.myidealclassapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parent_evalution_final extends AppCompatActivity {
    private TableLayout tableLayout;
    private FirebaseFirestore db;
    private int currentStudentId = -1;  // инициализируем значением по умолчанию

    private final Map<Long, String> subjectMap = new HashMap<>();
    private final List<String> trimesterHeaders = Arrays.asList("1 триместр", "2 триместр", "3 триместр");


    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_parent_evalution_final);

        tableLayout = findViewById(R.id.tableLayout);
        progressBar = findViewById(R.id.progressBar);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);
        currentStudentId = getIntent().getIntExtra("EXTRA_STUDENT_ID", -1);
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                Parent_dropdown_menu.showCustomPopupMenu(view, this, currentStudentId)
        );
        hideSystemUI();

        db = FirebaseFirestore.getInstance();

        if (currentStudentId != -1) {
            loadSubjectsThenFinalEvaluations();
        } else {
            Log.w("PARENT_EVALUTION_FINAL", "currentStudentId не установлен");
            progressBar.setVisibility(View.GONE);
        }
    }

    private void loadSubjectsThenFinalEvaluations() {
        subjectMap.clear();

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

                    Log.d("DEBUG", "Загруженные предметы: " + subjectMap);
                    loadFinalEvaluations();
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE", "Ошибка загрузки предметов", e);
                    progressBar.setVisibility(View.GONE);
                });
    }

    private final Map<String, Map<Integer, Long>> finalMarks = new HashMap<>();


    private void loadFinalEvaluations() {
        if (currentStudentId == -1) {
            Log.w("PARENT_EVALUTION_FINAL", "currentStudentId не установлен");
            progressBar.setVisibility(View.GONE);
            return;
        }

        db.collection("Final_Evaluation")
                .whereEqualTo("Id_Student", currentStudentId)
                .get()
                .addOnSuccessListener(query -> {
                    finalMarks.clear();

                    Log.d("DEBUG_FIRESTORE", "Всего документов: " + query.size()); // <-- ДОБАВЬ ЭТО

                    for (QueryDocumentSnapshot doc : query) {
                        Log.d("DEBUG_FIRESTORE", "Документ: " + doc.getData()); // <-- И ЭТО

                        Long subjectIdLong = doc.getLong("Id_Subject");
                        Long trimesterLong = doc.getLong("Trimester");
                        Long mark = doc.getLong("Evaluation_Number");

                        if (subjectIdLong == null || trimesterLong == null || mark == null) continue;

                        String subjectTitle = subjectMap.get(subjectIdLong);
                        if (subjectTitle == null) subjectTitle = "Неизвестно";

                        if (!finalMarks.containsKey(subjectTitle)) {
                            finalMarks.put(subjectTitle, new HashMap<>());
                        }

                        finalMarks.get(subjectTitle).put(trimesterLong.intValue(), mark);
                    }

                    buildFinalTable();
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE", "Ошибка при загрузке итоговых оценок", e);
                    progressBar.setVisibility(View.GONE);
                });
    }



    private void buildFinalTable() {
        progressBar.setVisibility(View.GONE);
        tableLayout.setVisibility(View.VISIBLE);
        tableLayout.removeAllViews();

        // Заголовок таблицы
        TableRow header = new TableRow(this);
        header.addView(createCell("", true, false));  // пустая первая ячейка
        for (String trimester : trimesterHeaders) {
            header.addView(createCell(trimester, false, false));
        }
        tableLayout.addView(header);

        // Строки по предметам
        for (String subject : finalMarks.keySet()) {
            TableRow row = new TableRow(this);
            row.addView(createCell(subject, true, false)); // название предмета

            Map<Integer, Long> trimesterMap = finalMarks.get(subject);

            for (int i = 1; i <= 3; i++) {
                Long mark = trimesterMap.get(i);
                row.addView(createCell(mark != null ? String.valueOf(mark) : "", false, true));
            }

            tableLayout.addView(row);
        }
    }

    private View createCell(String text, boolean alignLeft, boolean isMark) {
        View view = getLayoutInflater().inflate(R.layout.item_evalution, null);
        TextView cellText = view.findViewById(R.id.cellText);
        cellText.setText(text);

        LinearLayout layout = (LinearLayout) view;
        layout.setGravity(alignLeft ? (Gravity.START | Gravity.CENTER_VERTICAL) : Gravity.CENTER);

        if (isMark) {
            switch (text) {
                case "5":
                case "4":
                    layout.setBackgroundResource(R.drawable.evalution_4_5_border);
                    break;
                case "3":
                    layout.setBackgroundResource(R.drawable.evalution_3);
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

}
