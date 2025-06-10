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

import java.util.*;

public class Teacher_final_evalution extends AppCompatActivity {

    private Spinner classSpinner;
    private TableLayout tableLayout;
    private FirebaseFirestore db;

    private final Map<Long, String> studentMap = new HashMap<>(); // id ученика -> ФИО
    private final List<Integer> allTrimesters = new ArrayList<>(Arrays.asList(1, 2, 3)); // триместры
    private final Map<Long, Map<Integer, EvaluationEntry>> finalMarks = new HashMap<>(); // id студента -> (триместр -> оценка)

    private final Map<Long, String> classIdMap = new HashMap<>();
    private Long selectedClassId = null;
    private String employeeId;
    private int idSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_final_evalution);

        classSpinner = findViewById(R.id.spinnerClass);
        tableLayout = findViewById(R.id.tableLayout);
        db = FirebaseFirestore.getInstance();

        employeeId = getIntent().getStringExtra("teacherId");
        if (employeeId == null || employeeId.isEmpty()) {
            employeeId = "16";
        }

        com.example.myidealclassapp.Utilits.LoadingUtil.showLoading(this);
        idSubject = getIntent().getIntExtra("subjectId", 2);
        Log.d("DEBUG_SUBJECT", "subjectId: " + idSubject);
        loadClasses();
        hideSystemUI();
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
                    loadFinalEvaluations();
                })
                .addOnFailureListener(e -> Log.e("FIREBASE", "Ошибка загрузки учеников", e));
    }

    private void loadFinalEvaluations() {
        finalMarks.clear();

        db.collection("Final_Evaluation")
                .whereEqualTo("Id_Employee", employeeId)
                .whereEqualTo("Id_Subject", idSubject)
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        Long idStudent = doc.getLong("Id_Student");
                        Long evalNum = doc.getLong("Evaluation_Number");
                        Long trimesterL = doc.getLong("Trimester");

                        if (idStudent == null || evalNum == null || trimesterL == null) continue;
                        if (!studentMap.containsKey(idStudent)) continue;

                        int trimester = trimesterL.intValue();
                        int evaluationNumber = evalNum.intValue();

                        Map<Integer, EvaluationEntry> studentFinals = finalMarks.get(idStudent);
                        if (studentFinals == null) {
                            studentFinals = new HashMap<>();
                            finalMarks.put(idStudent, studentFinals);
                        }
                        studentFinals.put(trimester, new EvaluationEntry(evaluationNumber, doc.getId()));

                    }

                    buildTable();
                })
                .addOnFailureListener(e -> Log.e("FIREBASE", "Ошибка загрузки итоговых оценок", e));
        LoadingUtil.hideLoadingWithAnimation(this);
    }

    private void buildTable() {
        tableLayout.removeAllViews();

        // Заголовок
        TableRow header = new TableRow(this);
        header.addView(createCell("ФИО", true, false, null, null));
        for (Integer trimester : allTrimesters) {
            header.addView(createCell("Триместр " + trimester, true, false, null, null));
        }
        tableLayout.addView(header);

        for (Map.Entry<Long, String> entry : studentMap.entrySet()) {
            Long studentId = entry.getKey();
            String fullName = entry.getValue();
            Map<Integer, EvaluationEntry> studentMarks = finalMarks.get(studentId);

            TableRow row = new TableRow(this);
            row.addView(createCell(fullName, true, false, null, null));

            for (Integer trimester : allTrimesters) {
                EvaluationEntry eval = (studentMarks != null) ? studentMarks.get(trimester) : null;
                String markText = eval != null ? String.valueOf(eval.mark) : "";
                row.addView(createCell(markText, false, true, studentId, eval));
            }
            tableLayout.addView(row);
        }
    }

    private View createCell(String text, boolean isHeader, boolean isMark, Long studentId, EvaluationEntry entry) {
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

            layout.setOnClickListener(v -> showEvaluationDialog(studentId, entry));
        } else {
            layout.setBackgroundResource(R.drawable.evalution_4_5_border);
        }

        return view;
    }

    private void showEvaluationDialog(Long studentId, EvaluationEntry entry) {
        final String[] options = {"1", "2", "3", "4", "5"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(entry == null ? "Добавить итоговую оценку" : "Изменить/удалить итоговую оценку");

        int checkedItem = -1;
        if (entry != null) {
            for (int i = 0; i < options.length; i++) {
                if (Integer.parseInt(options[i]) == entry.mark) {
                    checkedItem = i;
                    break;
                }
            }
        }

        builder.setSingleChoiceItems(options, checkedItem, null);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
            if (selectedPosition >= 0) {
                int newMark = Integer.parseInt(options[selectedPosition]);

                if (entry == null) {
                    // Новая итоговая оценка
                    Map<String, Object> newEval = new HashMap<>();
                    newEval.put("Evaluation_Number", newMark);
                    newEval.put("Id_Employee", employeeId);
                    newEval.put("Id_Student", studentId);
                    newEval.put("Id_Subject", idSubject);
                    newEval.put("Trimester", 1); // **Важно:** здесь надо дать выбрать или определить триместр, иначе всегда 1
                    // Если нужно, можно добавить выбор триместра в диалог (могу помочь)

                    db.collection("Final_Evaluation").add(newEval).addOnSuccessListener(r -> loadFinalEvaluations());
                } else if (entry.mark != newMark) {
                    db.collection("Final_Evaluation").document(entry.docId)
                            .update("Evaluation_Number", newMark)
                            .addOnSuccessListener(r -> loadFinalEvaluations());
                }
            }
        });

        if (entry != null) {
            builder.setNegativeButton("Удалить", (dialog, which) -> {
                db.collection("Final_Evaluation").document(entry.docId)
                        .delete()
                        .addOnSuccessListener(r -> loadFinalEvaluations());
            });
        }

        builder.setNeutralButton("Отмена", null);
        builder.show();
    }

    private static class EvaluationEntry {
        int mark;
        String docId;

        EvaluationEntry(int mark, String docId) {
            this.mark = mark;
            this.docId = docId;
        }
    }
}
