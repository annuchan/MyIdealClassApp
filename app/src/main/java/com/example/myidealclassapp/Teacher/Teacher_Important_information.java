package com.example.myidealclassapp.Teacher;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myidealclassapp.Adapters.AdminTeacherImportInfoAdapter;
import com.example.myidealclassapp.Adapters.ParentImportInfoAdapter;
import com.example.myidealclassapp.Admin.Admin_about_the_app;
import com.example.myidealclassapp.Admin.Admin_main_window;
import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Classes.Important_information;
import com.example.myidealclassapp.Dropdown_menu.Admin_dropdown_menu;
import com.example.myidealclassapp.Dropdown_menu.Teacher_dropdown_menu;
import com.example.myidealclassapp.R;
import com.example.myidealclassapp.Utilits.LoadingUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Teacher_Important_information extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminTeacherImportInfoAdapter adapter;
    private List<Important_information> infoList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String idSubject;
    private String employeeId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_important_information);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);
        idSubject = String.valueOf(getIntent().getIntExtra("subjectId", 5));
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);

        dropdownMenu.setOnClickListener(view ->
                Teacher_dropdown_menu.showCustomPopupMenu(view, this,employeeId, Integer.parseInt(idSubject))
        );
        com.example.myidealclassapp.Utilits.LoadingUtil.showLoading(this);
        hideSystemUI();
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("employeeId")) {
            employeeId = intent.getStringExtra("employeeId");
            Log.d("DEBUG", "Employee ID: " + employeeId);
        } else {
            employeeId = "";
            Log.d("DEBUG", "Employee ID не передан");
        }
        recyclerView = findViewById(R.id.recyclerViewImportant_information);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminTeacherImportInfoAdapter(this, infoList);
        recyclerView.setAdapter(adapter);



        db = FirebaseFirestore.getInstance();

        loadImportantInformation();


    }
    @Override
    protected void onResume() {
        super.onResume();
        loadImportantInformation(); // обновляем данные каждый раз при возвращении
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
        Intent intent = new Intent(this, Teacher_important_information_add.class);
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
    private void loadImportantInformation() {
        db.collection("Important_information")
                .whereEqualTo("Id_Employee", String.valueOf(employeeId))

                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    infoList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Important_information info = doc.toObject(Important_information.class);
                        infoList.add(info);
                    }
                    adapter.notifyDataSetChanged();
                    LoadingUtil.hideLoadingWithAnimation(this);
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Ошибка при загрузке информации", e));
    }
}
