package com.example.myidealclassapp.Teacher;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;

import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Dropdown_menu.Teacher_dropdown_menu;
import com.example.myidealclassapp.R;
import com.example.myidealclassapp.Utilits.LoadingUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class teacher_evalution extends AppCompatActivity {

    private Spinner classSpinner;
    private TableLayout tableLayout;
    private FirebaseFirestore db;

    private final Map<Long, String> studentMap = new HashMap<>();
    private final List<String> allDates = new ArrayList<>();
    private final Map<Long, Map<String, List<EvaluationEntry>>> marks = new HashMap<>();

    private final Map<Long, String> classIdMap = new HashMap<>();
    private Long selectedClassId = null;
    private String employeeId;
    private int idSubject; // теперь динамический предмет

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_evalution);

        classSpinner = findViewById(R.id.spinnerClass);
        tableLayout = findViewById(R.id.tableLayout);
        db = FirebaseFirestore.getInstance();

        employeeId = getIntent().getStringExtra("teacherId");
        if (employeeId == null || employeeId.isEmpty()) {
            employeeId = "16";
        }
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("employeeId")) {
            employeeId = intent.getStringExtra("employeeId");
            Log.d("DEBUG", "Employee ID: " + employeeId);
        } else {
            employeeId = "";
            Log.d("DEBUG", "Employee ID не передан");
        }
        idSubject = getIntent().getIntExtra("subjectId", 2); // читаем subjectId из интента, по умолчанию 2
        com.example.myidealclassapp.Utilits.LoadingUtil.showLoading(this);
        loadClasses();
        hideSystemUI();
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                Teacher_dropdown_menu.showCustomPopupMenu(view, this, employeeId, idSubject)
        );
    }

    private void loadClasses() {
        db.collection("Class")
                .get()
                .addOnSuccessListener(query -> {
                    List<String> classNames = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        Long id = doc.getLong("Id");
                        String name = doc.getString("Number");
                        if (id != null && name != null) {
                            classIdMap.put(id, name);
                            classNames.add(name);
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    classSpinner.setAdapter(adapter);

                    classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedClassName = classNames.get(position);
                            for (Map.Entry<Long, String> entry : classIdMap.entrySet()) {
                                if (entry.getValue().equals(selectedClassName)) {
                                    selectedClassId = entry.getKey();
                                    break;
                                }
                            }
                            loadStudents();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                })
                .addOnFailureListener(e -> Log.e("FIREBASE", "Ошибка загрузки классов", e));
    }

    private void loadStudents() {
        studentMap.clear();
        db.collection("Student")
                .whereEqualTo("Id_Class", selectedClassId)
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        Long id = doc.getLong("Id");
                        if (id == null) continue;

                        String lastName = doc.getString("LastName");
                        String firstName = doc.getString("FirstName");
                        String middleName = doc.getString("MiddleName");

                        String fullName = ((lastName != null) ? lastName : "") + " " +
                                ((firstName != null) ? firstName : "") + " " +
                                ((middleName != null) ? middleName : "");

                        studentMap.put(id, fullName.trim());
                    }
                    loadEvaluations();
                })
                .addOnFailureListener(e -> Log.e("FIREBASE", "Ошибка загрузки учеников", e));
    }

    private void loadEvaluations() {
        marks.clear();
        Set<String> dateSet = new TreeSet<>();

        db.collection("Evaluation")
                .whereEqualTo("Id_Employee", employeeId)
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        Long idStudent = doc.getLong("Id_Student");
                        String date = doc.getString("Date_Evalution");
                        Long mark = doc.getLong("Evaluation_Number");

                        if (idStudent == null || date == null || mark == null) continue;
                        if (!studentMap.containsKey(idStudent)) continue;

                        if (date.length() > 8) {
                            date = date.substring(0, 8); // обрезаем, если дата с временем
                        }

                        dateSet.add(date);

                        if (!marks.containsKey(idStudent)) {
                            marks.put(idStudent, new HashMap<>());
                        }

                        Map<String, List<EvaluationEntry>> studentMarks = marks.get(idStudent);
                        if (!studentMarks.containsKey(date)) {
                            studentMarks.put(date, new ArrayList<>());
                        }

                        studentMarks.get(date).add(new EvaluationEntry(mark, doc.getId()));
                    }

                    allDates.clear();
                    allDates.addAll(dateSet);

                    // Добавляем сегодняшнюю дату, если её ещё нет
                    String today = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
                    if (!allDates.contains(today)) {
                        allDates.add(today);
                    }

                    // Убедимся, что у всех учеников есть пустой список на сегодняшнюю дату
                    for (Long studentId : studentMap.keySet()) {
                        if (!marks.containsKey(studentId)) {
                            marks.put(studentId, new HashMap<>());
                        }
                        Map<String, List<EvaluationEntry>> studentMarks = marks.get(studentId);
                        if (!studentMarks.containsKey(today)) {
                            studentMarks.put(today, new ArrayList<>());
                        }
                    }

                    buildTable();
                })
                .addOnFailureListener(e -> Log.e("FIREBASE", "Ошибка загрузки оценок", e));
        LoadingUtil.hideLoadingWithAnimation(this);
    }

    private void buildTable() {
        tableLayout.removeAllViews();

        TableRow header = new TableRow(this);
        header.addView(createCell("ФИО", true, false, null, 0, null, null));
        for (String date : allDates) {
            header.addView(createCell(date, true, false, null, 0, null, null));
        }
        tableLayout.addView(header);

        for (Map.Entry<Long, String> entry : studentMap.entrySet()) {
            Long studentId = entry.getKey();
            String fullName = entry.getValue();
            Map<String, List<EvaluationEntry>> studentMarks = marks.get(studentId);

            int maxRows = 1;
            if (studentMarks != null) {
                for (String date : allDates) {
                    List<EvaluationEntry> marksOnDate = studentMarks.get(date);
                    if (marksOnDate != null && marksOnDate.size() > maxRows) {
                        maxRows = marksOnDate.size();
                    }
                }
            }

            for (int i = 0; i < maxRows; i++) {
                TableRow row = new TableRow(this);
                row.addView(createCell(i == 0 ? fullName : "", true, false, null, 0, null, null));

                for (String date : allDates) {
                    EvaluationEntry eval = null;
                    if (studentMarks != null && studentMarks.containsKey(date)) {
                        List<EvaluationEntry> list = studentMarks.get(date);
                        if (i < list.size()) {
                            eval = list.get(i);
                        }
                    }

                    String markText = eval != null ? String.valueOf(eval.mark) : "";
                    row.addView(createCell(markText, false, true, studentId, i, eval, date));
                }

                tableLayout.addView(row);
            }
        }
    }

    private View createCell(String text, boolean isHeader, boolean isMark, Long studentId, int index, EvaluationEntry entry, String date) {
        View view = getLayoutInflater().inflate(R.layout.item_evalution_2, null);
        TextView cellText = view.findViewById(R.id.cellText);
        LinearLayout layout = (LinearLayout) view;

        cellText.setText(text);
        layout.setGravity(Gravity.CENTER);
        cellText.setGravity(Gravity.CENTER);

        if (isMark) {
            switch (text.trim()) {
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

            layout.setOnClickListener(v -> showEvaluationDialog(studentId, entry, date));
        } else {
            layout.setBackgroundResource(R.drawable.evalution_4_5_border);
        }

        return view;
    }

    private void showEvaluationDialog(Long studentId, EvaluationEntry entry, String date) {
        final String[] options = {"1", "2", "3", "4", "5"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(entry == null ? "Добавить оценку" : "Изменить/удалить оценку");

        builder.setSingleChoiceItems(options, entry != null ? (int) entry.mark - 1 : -1, null);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
            if (selectedPosition >= 0) {
                long newMark = Long.parseLong(options[selectedPosition]);

                if (entry == null) {
                    Map<String, Object> newEval = new HashMap<>();
                    newEval.put("Date_Evalution", date);
                    newEval.put("Evaluation_Number", newMark);
                    newEval.put("Id_Employee", employeeId);
                    newEval.put("Id_Student", studentId);
                    newEval.put("Id_Subject", idSubject);
                    db.collection("Evaluation").add(newEval).addOnSuccessListener(r -> loadEvaluations());
                } else if (entry.mark != newMark) {
                    db.collection("Evaluation").document(entry.docId)
                            .update("Evaluation_Number", newMark)
                            .addOnSuccessListener(r -> loadEvaluations());
                }
            }
        });

        if (entry != null) {
            builder.setNegativeButton("Удалить", (dialog, which) -> {
                db.collection("Evaluation").document(entry.docId)
                        .delete()
                        .addOnSuccessListener(r -> loadEvaluations());
            });
        }

        builder.setNeutralButton("Отмена", null);
        builder.show();
    }

    private static class EvaluationEntry {
        long mark;
        String docId;

        EvaluationEntry(long mark, String docId) {
            this.mark = mark;
            this.docId = docId;
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
        Intent intent = new Intent(this, Teacher_main_window.class);
        intent.putExtra("employeeId", employeeId);
        intent.putExtra("subjectId", idSubject);
        startActivity(intent);
    }
    public void moreButton(View view) {
        Intent intent = new Intent(this, Teacher_final_evalution.class);
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
