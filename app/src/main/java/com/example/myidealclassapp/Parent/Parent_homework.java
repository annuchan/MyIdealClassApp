package com.example.myidealclassapp.Parent;

import android.app.DatePickerDialog;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Dropdown_menu.Parent_dropdown_menu;
import com.example.myidealclassapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Parent_homework extends AppCompatActivity {

    private TableLayout tableLayout;
    private ProgressBar progressBar;
    private Button btnSelectDate;

    private FirebaseFirestore db;
    private int currentStudentId;
    private int currentStudentClassId = -1;
    private final Map<String, Map<String, String>> homeworkMap = new TreeMap<>();

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_homework);

        tableLayout = findViewById(R.id.tableLayout);
        progressBar = findViewById(R.id.progressBar);
        btnSelectDate = findViewById(R.id.date_homework);
        db = FirebaseFirestore.getInstance();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);
        currentStudentId = getIntent().getIntExtra("EXTRA_STUDENT_ID", -1);
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                Parent_dropdown_menu.showCustomPopupMenu(view, this, currentStudentId)
        );
        hideSystemUI();

        if (currentStudentId == -1) {
            Toast.makeText(this, "Ошибка: студент не выбран", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("Student").document(String.valueOf(currentStudentId))
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.getLong("Id_Class") != null) {
                        currentStudentClassId = doc.getLong("Id_Class").intValue();
                        String today = sdf.format(new Date());
                        btnSelectDate.setText(today);
                        loadHomeworkWeek(currentStudentClassId, today);
                    } else {
                        Toast.makeText(this, "Ошибка: класс не найден", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("HOMEWORK", "Ошибка получения класса студента", e);
                    Toast.makeText(this, "Ошибка при загрузке данных", Toast.LENGTH_SHORT).show();
                });

        btnSelectDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String selectedDate = String.format(Locale.getDefault(), "%02d.%02d.%d", dayOfMonth, month + 1, year);
                btnSelectDate.setText(selectedDate);
                if (currentStudentClassId != -1) {
                    loadHomeworkWeek(currentStudentClassId, selectedDate);
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void loadHomeworkWeek(int classId, String selectedDateStr) {
        progressBar.setVisibility(View.VISIBLE);
        tableLayout.removeAllViews();
        homeworkMap.clear();

        List<String> weekDates = getWeekDates(selectedDateStr);

        db.collection("Homework")
                .whereEqualTo("Id_Class", classId)
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        String date = doc.getString("Date_Homework");
                        if (date != null && weekDates.contains(date)) {
                            String subjectId = String.valueOf(doc.getLong("Id_Subject"));
                            String task = doc.getString("Task");
                            if (!homeworkMap.containsKey(date)) {
                                homeworkMap.put(date, new HashMap<>());
                            }

                            Map<String, String> subjectTasks = homeworkMap.get(date);
                            if (subjectTasks.containsKey(subjectId)) {
                                subjectTasks.put(subjectId, subjectTasks.get(subjectId) + "\n" + task);
                            } else {
                                subjectTasks.put(subjectId, task);
                            }
                        }
                    }
                    loadSubjectsAndBuildTable(weekDates);
                })
                .addOnFailureListener(e -> {
                    Log.e("HOMEWORK", "Ошибка загрузки домашки", e);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Ошибка при загрузке домашнего задания", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadSubjectsAndBuildTable(List<String> weekDates) {
        db.collection("Subjects")
                .get()
                .addOnSuccessListener(query -> {
                    Map<String, String> subjectNames = new HashMap<>();
                    for (QueryDocumentSnapshot doc : query) {
                        Long id = doc.getLong("Id");
                        String title = doc.getString("Title");
                        if (id != null && title != null) {
                            subjectNames.put(String.valueOf(id), title);
                        }
                    }
                    buildTable(weekDates, subjectNames);
                })
                .addOnFailureListener(e -> {
                    Log.e("HOMEWORK", "Ошибка загрузки предметов", e);
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void buildTable(List<String> weekDates, Map<String, String> subjectNames) {
        progressBar.setVisibility(View.GONE);
        tableLayout.removeAllViews();

        if (homeworkMap.isEmpty()) {
            TableRow row = new TableRow(this);
            row.addView(createCustomCell("На этой неделе нет домашнего задания"));
            tableLayout.addView(row);
            return;
        }

        for (String date : weekDates) {
            String dayOfWeek = getDayOfWeek(date);
            Map<String, String> tasks = homeworkMap.get(date);
            if (tasks == null) {
                tasks = new HashMap<>();
            }


            // Заголовок: день и дата
            TableRow header = new TableRow(this);
            header.addView(createCustomCell(dayOfWeek));
            header.addView(createCustomCell(date));
            tableLayout.addView(header);

            if (tasks.isEmpty()) {
                TableRow row = new TableRow(this);
                row.addView(createCustomCell("—"));
                row.addView(createCustomCell("Нет домашнего задания"));
                tableLayout.addView(row);
            } else {
                for (String subjectId : tasks.keySet()) {
                    String subject = subjectNames.get(subjectId);
                    if (subject == null) {
                        subject = "Предмет?";
                    }

                    String task = tasks.get(subjectId);

                    TableRow row = new TableRow(this);
                    row.addView(createCustomCell(subject));
                    row.addView(createCustomCell(task));
                    tableLayout.addView(row);
                }
            }
        }
    }

    private View createCustomCell(String text) {
        View view = getLayoutInflater().inflate(R.layout.item_table, null);
        TextView tv = view.findViewById(R.id.cellText);
        tv.setText(text);

        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1f);
        view.setLayoutParams(params);

        view.setMinimumHeight(dpToPx(48)); // минимальная высота
        return view;
    }


    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private List<String> getWeekDates(String selectedDateStr) {
        List<String> weekDates = new ArrayList<>();
        try {
            Date selectedDate = sdf.parse(selectedDateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(selectedDate);

            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int diff = (dayOfWeek == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - dayOfWeek;
            calendar.add(Calendar.DAY_OF_MONTH, diff);

            for (int i = 0; i < 6; i++) {
                weekDates.add(sdf.format(calendar.getTime()));
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        } catch (ParseException e) {
            Log.e("HOMEWORK", "Ошибка парсинга даты", e);
        }
        return weekDates;
    }

    private String getDayOfWeek(String dateStr) {
        try {
            Date date = sdf.parse(dateStr);
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("ru"));
            return capitalize(dayFormat.format(date));
        } catch (ParseException e) {
            return "";
        }
    }

    private String capitalize(String s) {
        return s == null || s.length() == 0 ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
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
