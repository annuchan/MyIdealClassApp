package com.example.myidealclassapp.Parent;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myidealclassapp.Adapters.ParentMeasureAdapter;
import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Classes.Measure;
import com.example.myidealclassapp.Dropdown_menu.Parent_dropdown_menu;
import com.example.myidealclassapp.R;
import com.example.myidealclassapp.Utilits.LoadingUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Parent_measure extends AppCompatActivity {

    private RecyclerView recyclerViewEvents;
    private ParentMeasureAdapter adapter;
    private List<Measure> measureList;

    private FirebaseFirestore db;
    private int currentStudentId;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_measure);

        recyclerViewEvents = findViewById(R.id.recyclerViewEvents);
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));

        measureList = new ArrayList<>();
        adapter = new ParentMeasureAdapter(this, measureList);
        recyclerViewEvents.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadMeasuresFromFirestore();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);
        currentStudentId = getIntent().getIntExtra("EXTRA_STUDENT_ID", -1);
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                Parent_dropdown_menu.showCustomPopupMenu(view, this, currentStudentId)
        );
        hideSystemUI();
        com.example.myidealclassapp.Utilits.LoadingUtil.showLoading(this);
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
    private void loadMeasuresFromFirestore() {
        db.collection("Measure")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        measureList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String title = document.getString("Title");
                            String describe = document.getString("Describe");
                            String type = document.getString("Type_Measure");
                            String date = document.getString("Date_Measure");
                            String imageBase64 = document.getString("ImageBase64"); // Если в Firestore поле с Base64 картинкой называется иначе — поправь здесь

                            Measure measure = new Measure(title, describe, type, date, imageBase64);
                            measureList.add(measure);
                        }
                        adapter.notifyDataSetChanged();
                        LoadingUtil.hideLoadingWithAnimation(this);
                    } else {
                        Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
