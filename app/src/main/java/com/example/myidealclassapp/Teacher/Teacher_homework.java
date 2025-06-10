package com.example.myidealclassapp.Teacher;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;

import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Classes.ClassModel;
import com.example.myidealclassapp.Dropdown_menu.Teacher_dropdown_menu;
import com.example.myidealclassapp.R;
import com.example.myidealclassapp.Utilits.LoadingUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class Teacher_homework extends AppCompatActivity {

    private Spinner classSpinner;
    private TextView currentDateTextView;
    private TableLayout tableLayout;

    private FirebaseFirestore db;

    private int idSubject;
    private String subjectName = "";
    private String selectedDate = "";
    private String employeeId;
    private List<ClassModel> classItems = new ArrayList<>();

    // ID текущего документа домашнего задания (если есть)
    private String currentHomeworkDocId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_homework);

        db = FirebaseFirestore.getInstance();

        classSpinner = findViewById(R.id.classSpinner);
        currentDateTextView = findViewById(R.id.currentDate);
        tableLayout = findViewById(R.id.tableLayout);

        idSubject = getIntent().getIntExtra("subjectId", 2);
        com.example.myidealclassapp.Utilits.LoadingUtil.showLoading(this);
        loadSubjectNameById(idSubject);

        loadClassesFromFirestore();

        selectedDate = getTodayDateString();
        currentDateTextView.setText(selectedDate);
        hideSystemUI();
        currentDateTextView.setOnClickListener(v -> showDatePickerDialog());
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("employeeId")) {
            employeeId = intent.getStringExtra("employeeId");
        }
        idSubject = intent.getIntExtra("subjectId", 2);

        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadHomeworkIntoTable();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                Teacher_dropdown_menu.showCustomPopupMenu(view, this, employeeId, idSubject)
        );
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
    private void loadSubjectNameById(int subjectId) {
        db.collection("Subjects").document(String.valueOf(subjectId))
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        subjectName = doc.getString("Title");
                        loadHomeworkIntoTable(); // Обновляем таблицу после загрузки имени предмета
                    } else {
                        Toast.makeText(this, "Предмет не найден", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки предмета", Toast.LENGTH_SHORT).show());
    }

    private void loadClassesFromFirestore() {
        db.collection("Class")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    classItems.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        String classNumber = doc.getString("Number");
                        Long idLong = doc.getLong("Id");
                        if (classNumber != null && idLong != null) {
                            classItems.add(new ClassModel(idLong.intValue(), classNumber));
                        }
                    }
                    ArrayAdapter<ClassModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classItems);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    classSpinner.setAdapter(adapter);

                    if (!classItems.isEmpty()) {
                        classSpinner.setSelection(0);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки классов", Toast.LENGTH_SHORT).show());
    }

    private void showDatePickerDialog() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this, (view, y, m, d) -> {
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
            currentDateTextView.setText(selectedDate);
            loadHomeworkIntoTable();
        }, year, month, day);

        dpd.show();
    }

    private String getTodayDateString() {
        Calendar c = Calendar.getInstance();
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    private void loadHomeworkIntoTable() {
        tableLayout.removeAllViews();

        ClassModel selectedClass = (ClassModel) classSpinner.getSelectedItem();
        if (selectedClass == null || subjectName.isEmpty() || selectedDate.isEmpty()) return;

        // Создаем строку заголовка
        TableRow headerRow = new TableRow(this);

        // Заголовочная ячейка "Предмет"
        View headerSubjectView = getLayoutInflater().inflate(R.layout.item_evalution_2, null);
        TextView headerSubjectText = headerSubjectView.findViewById(R.id.cellText);
        headerSubjectText.setText("Предмет");
        headerSubjectText.setTypeface(null, Typeface.BOLD);
        headerRow.addView(headerSubjectView);

        // Заголовочная ячейка "Домашнее задание"
        View headerHomeworkView = getLayoutInflater().inflate(R.layout.item_evalution_2, null);
        TextView headerHomeworkText = headerHomeworkView.findViewById(R.id.cellText);
        headerHomeworkText.setText("Домашнее задание");
        headerHomeworkText.setTypeface(null, Typeface.BOLD);
        headerRow.addView(headerHomeworkView);

        tableLayout.addView(headerRow);

        db.collection("Homework")
                .whereEqualTo("Id_Class", selectedClass.getId())
                .whereEqualTo("Id_Subject", idSubject)
                .whereEqualTo("Date_Homework", selectedDate)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    TableRow row = new TableRow(this);

                    // Ячейка с названием предмета
                    View subjectView = getLayoutInflater().inflate(R.layout.item_evalution_2, null);
                    TextView tvSubject = subjectView.findViewById(R.id.cellText);
                    tvSubject.setText(subjectName);
                    row.addView(subjectView);

                    // Ячейка с домашним заданием
                    View homeworkView = getLayoutInflater().inflate(R.layout.item_evalution_2, null);
                    TextView tvHomework = homeworkView.findViewById(R.id.cellText);

                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        currentHomeworkDocId = doc.getId();
                        String homeworkText = doc.getString("Task");

                        if (homeworkText == null || homeworkText.isEmpty()) {
                            tvHomework.setText("");
                            tvHomework.setHint("Нажмите, чтобы добавить");
                            tvHomework.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        } else {
                            tvHomework.setText(homeworkText);
                            tvHomework.setTextColor(getResources().getColor(android.R.color.black));
                        }

                        tvHomework.setOnClickListener(v -> showEditHomeworkDialog(selectedClass, selectedDate, idSubject, subjectName, homeworkText));
                    } else {
                        currentHomeworkDocId = null;
                        tvHomework.setText("");
                        tvHomework.setHint("Нажмите, чтобы добавить");
                        tvHomework.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        tvHomework.setOnClickListener(v -> showEditHomeworkDialog(selectedClass, selectedDate, idSubject, subjectName, ""));
                    }

                    row.addView(homeworkView);
                    tableLayout.addView(row);

                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки домашнего задания", Toast.LENGTH_SHORT).show());
        LoadingUtil.hideLoadingWithAnimation(this);
    }


    private TextView createHeaderTextView(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(16, 16, 16, 16);
        return tv;
    }

    private void showEditHomeworkDialog(ClassModel selectedClass, String date, int subjectId, String subjectName, String currentHomework) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(subjectName + " - Домашнее задание");

        EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setLines(5);
        editText.setText(currentHomework != null ? currentHomework : "");
        editText.setSelection(editText.getText().length());

        builder.setView(editText);

        builder.setPositiveButton(currentHomework == null || currentHomework.isEmpty() ? "Добавить" : "Сохранить", (dialog, which) -> {
            String newHomework = editText.getText().toString().trim();

            if (!newHomework.isEmpty()) {
                saveHomeworkToFirestore(selectedClass.getId(), date, subjectId, newHomework);
            } else {
                if (currentHomeworkDocId != null) {
                    deleteHomeworkFromFirestore(currentHomeworkDocId);
                } else {
                    Toast.makeText(this, "Домашнее задание не задано", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (currentHomework != null && !currentHomework.isEmpty()) {
            builder.setNeutralButton("Удалить", (dialog, which) -> {
                if (currentHomeworkDocId != null) {
                    deleteHomeworkFromFirestore(currentHomeworkDocId);
                }
            });
        }

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void saveHomeworkToFirestore(int classId, String date, int subjectId, String homeworkText) {
        Map<String, Object> data = new HashMap<>();
        data.put("Id_Class", classId);
        data.put("Date_Homework", date);
        data.put("Id_Subject", subjectId);
        data.put("Task", homeworkText);

        if (currentHomeworkDocId != null) {
            db.collection("Homework").document(currentHomeworkDocId)
                    .set(data)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Домашнее задание сохранено", Toast.LENGTH_SHORT).show();
                        loadHomeworkIntoTable();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Ошибка сохранения домашнего задания", Toast.LENGTH_SHORT).show());
        } else {
            db.collection("Homework")
                    .add(data)
                    .addOnSuccessListener(documentReference -> {
                        currentHomeworkDocId = documentReference.getId();
                        Toast.makeText(this, "Домашнее задание добавлено", Toast.LENGTH_SHORT).show();
                        loadHomeworkIntoTable();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Ошибка добавления домашнего задания", Toast.LENGTH_SHORT).show());
        }
    }

    private void deleteHomeworkFromFirestore(String docId) {
        db.collection("Homework").document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Домашнее задание удалено", Toast.LENGTH_SHORT).show();
                    currentHomeworkDocId = null;
                    loadHomeworkIntoTable();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка удаления домашнего задания", Toast.LENGTH_SHORT).show());
    }
}
