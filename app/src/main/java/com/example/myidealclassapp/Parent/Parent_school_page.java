package com.example.myidealclassapp.Parent;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Classes.School;
import com.example.myidealclassapp.Dropdown_menu.Parent_dropdown_menu;
import com.example.myidealclassapp.R;
import com.example.myidealclassapp.Utilits.LoadingUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Parent_school_page extends AppCompatActivity {
    private int currentStudentId;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_school_page);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);
        currentStudentId = getIntent().getIntExtra("EXTRA_STUDENT_ID", -1);
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                Parent_dropdown_menu.showCustomPopupMenu(view, this, currentStudentId)
        );
        hideSystemUI();
        com.example.myidealclassapp.Utilits.LoadingUtil.showLoading(this);
        // Загрузка данных из Firestore
        db.collection("School").document("1")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        School school = documentSnapshot.toObject(School.class);
                        if (school != null) {
                            // Заполняем вью через адаптер
                            bindSchoolData(school);
                        }
                    } else {
                        Log.e("Firestore", "Документ школы не найден");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Ошибка при загрузке школы", e));
        LoadingUtil.hideLoadingWithAnimation(this);
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
    private void bindSchoolData(School school) {
        ((TextView) findViewById(R.id.SchoolTitle)).setText(school.getTitle());
        ((TextView) findViewById(R.id.SchoolAbbreviature)).setText(school.getAbbreviation());
        ((TextView) findViewById(R.id.SchoolAdress)).setText(school.getAdress_School());
        ((TextView) findViewById(R.id.SchoolPhone)).setText(school.getPhone_School());
        ((TextView) findViewById(R.id.ditrectorname)).setText(school.getDirector());
        ((TextView) findViewById(R.id.schoolemail)).setText(school.getEmail_School());
        ((TextView) findViewById(R.id.schoolstudents)).setText(String.valueOf(school.getHow_Student()));
        ((TextView) findViewById(R.id.schoolyear)).setText(school.getDate_of_creation());
        ((TextView) findViewById(R.id.schoollanguage)).setText(school.getLanguage_Study());
        ((TextView) findViewById(R.id.schoolformaobycheniya)).setText(school.getForm_Education());
        ((TextView) findViewById(R.id.schoolwork)).setText(school.getWork_schedule());
        ((TextView) findViewById(R.id.schoolychreditel)).setText(school.getPlace());

        // Декодируем изображение директора из Base64
        try {
            byte[] imageBytes = Base64.decode(school.getDirectorImage(), Base64.DEFAULT);
            Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            ((ImageView) findViewById(R.id.directorimg)).setImageBitmap(decodedImage);
        } catch (Exception e) {
            Log.e("Parent_school_page", "Ошибка при декодировании изображения директора", e);
        }
    }
}
