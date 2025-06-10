package com.example.myidealclassapp.Parent;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
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

import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Dropdown_menu.Parent_dropdown_menu;
import com.example.myidealclassapp.R;
import com.example.myidealclassapp.Utilits.LoadingUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Parent_asset_select extends AppCompatActivity {
    private int currentStudentId;
    private static final String TAG = "ParentAssetSelect";

    private TableLayout tableLayoutSchedule;
    private TextView assetName;  // Добавляем TextView для названия кружка

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_asset_select);
        com.example.myidealclassapp.Utilits.LoadingUtil.showLoading(this);
        tableLayoutSchedule = findViewById(R.id.tableLayout);
        assetName = findViewById(R.id.assetName);

        db = FirebaseFirestore.getInstance();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);
        currentStudentId = getIntent().getIntExtra("EXTRA_STUDENT_ID", -1);
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                Parent_dropdown_menu.showCustomPopupMenu(view, this, currentStudentId)
        );
        hideSystemUI();
        Log.d(TAG, "Полученный EXTRA_STUDENT_ID: " + currentStudentId);

        db.collection("Student")
                .document(String.valueOf(currentStudentId))
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Log.e(TAG, "Студент с таким ID не найден");
                        finish();
                        return;
                    }

                    Long idAssetLong = doc.getLong("Id_Asset");
                    if (idAssetLong == null) {
                        Log.e(TAG, "У студента отсутствует поле Id_Asset");
                        finish();
                        return;
                    }

                    int idAsset = idAssetLong.intValue();

                    loadAssetName(idAsset);
                    loadScheduleForAsset(idAsset);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка при загрузке данных студента", e);
                    finish();
                });

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
    private void loadAssetName(int assetId) {
        db.collection("Asset")
                .document(String.valueOf(assetId))
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String title = doc.getString("Title");
                        if (title != null && !title.isEmpty()) {
                            assetName.setText(title);
                        } else {
                            assetName.setText("Название кружка отсутствует");
                        }
                    } else {
                        assetName.setText("Кружок не найден");
                    }
                })
                .addOnFailureListener(e -> {
                    assetName.setText("Ошибка загрузки названия");
                    Log.e(TAG, "Ошибка при загрузке названия кружка", e);
                });
    }

    private void loadScheduleForAsset(int assetId) {
        tableLayoutSchedule.removeAllViews();

        // Заголовок таблицы
        TableRow headerRow = new TableRow(this);
        headerRow.addView(createCustomHeaderCell("Время"));
        headerRow.addView(createCustomHeaderCell("День недели"));
        tableLayoutSchedule.addView(headerRow);

        db.collection("Schedule_Asset")
                .whereEqualTo("Id_Asset", assetId)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        TableRow emptyRow = new TableRow(this);
                        View emptyCell = createCustomCell("Расписание отсутствует", true);
                        emptyRow.addView(emptyCell);
                        tableLayoutSchedule.addView(emptyRow);
                        return;
                    }

                    for (DocumentSnapshot doc : query) {
                        String timeSchedule = doc.getString("Time_Schedule");
                        String weekday = doc.getString("Weekday");

                        if (timeSchedule == null) timeSchedule = "-";
                        if (weekday == null) weekday = "-";

                        TableRow row = new TableRow(this);
                        row.addView(createCustomCell(timeSchedule, false));
                        row.addView(createCustomCell(weekday, true));
                        tableLayoutSchedule.addView(row);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Ошибка при загрузке расписания", e));
        LoadingUtil.hideLoadingWithAnimation(this);
    }

    // Метод создания кастомной ячейки из layout cell_layout.xml
    private View createCustomCell(String text, boolean alignLeft) {
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout cellLayout = (LinearLayout) inflater.inflate(R.layout.item_table, null, false);

        TextView cellText = cellLayout.findViewById(R.id.cellText);
        cellText.setText(text);
        cellText.setGravity(alignLeft ? (Gravity.START | Gravity.CENTER_VERTICAL) : Gravity.CENTER);

        return cellLayout;
    }

    private View createCustomHeaderCell(String text) {
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout headerLayout = (LinearLayout) inflater.inflate(R.layout.item_table, null, false);

        TextView cellText = headerLayout.findViewById(R.id.cellText);
        cellText.setText(text);
        cellText.setTextSize(16f);
        cellText.setGravity(Gravity.CENTER);
        // Можно добавить цвет фона для заголовка, если нужно
        return headerLayout;
    }

    public static class EdgeToEdge {
        public static void enable(AppCompatActivity activity) {
            View decorView = activity.getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
    }
}
