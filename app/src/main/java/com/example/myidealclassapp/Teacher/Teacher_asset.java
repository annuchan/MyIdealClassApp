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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myidealclassapp.Adapters.ApplicationFormAdapter;
import com.example.myidealclassapp.Admin.Admin_about_the_app;
import com.example.myidealclassapp.Admin.Admin_main_window;
import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Classes.ApplicationForm;
import com.example.myidealclassapp.Dropdown_menu.Admin_dropdown_menu;
import com.example.myidealclassapp.Dropdown_menu.Teacher_dropdown_menu;
import com.example.myidealclassapp.R;
import com.example.myidealclassapp.Utilits.LoadingUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Teacher_asset extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private ApplicationFormAdapter adapter;
    private List<ApplicationForm> applicationList;
    private String employeeId;
    private  int idSubject;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_asset);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.application_form);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        applicationList = new ArrayList<>();
            com.example.myidealclassapp.Utilits.LoadingUtil.showLoading(this);
        adapter = new ApplicationFormAdapter(this, applicationList, new ApplicationFormAdapter.OnMenuClickListener() {
            @Override
            public void onAccept(ApplicationForm form) {
                acceptApplication(form);
            }

            @Override
            public void onDecline(ApplicationForm form) {
                declineApplication(form);
            }
        });

        recyclerView.setAdapter(adapter);
        loadApplications();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                Teacher_dropdown_menu.showCustomPopupMenu(view, this, employeeId, idSubject)
        );
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("employeeId")) {
            employeeId = intent.getStringExtra("employeeId");
            Log.d("DEBUG", "Employee ID: " + employeeId);
        } else {
            employeeId = "";
            Log.d("DEBUG", "Employee ID не передан");
        }
        hideSystemUI();
        idSubject = getIntent().getIntExtra("subjectId", 2); // читаем subjectId из интента, по умолчанию 2
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
    private void loadApplications() {
        db.collection("Application_form")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    applicationList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ApplicationForm form = doc.toObject(ApplicationForm.class);
                        form.setId(doc.getId());
                        applicationList.add(form);
                    }
                    adapter.notifyDataSetChanged();
                    LoadingUtil.hideLoadingWithAnimation(this);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка загрузки заявок", Toast.LENGTH_SHORT).show()
                );
    }

    private void acceptApplication(ApplicationForm form) {
        String studentId = form.getId_Student();
        DocumentReference studentRef = db.collection("Student").document(studentId);

        studentRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Map<String, Object> updates = new HashMap<>();
                String assetId = String.valueOf(form.getId_Asset()); // Long → String

                for (int i = 1; i <= 5; i++) {
                    String field = i == 1 ? "Id_Asset" : "Id_Asset" + i;
                    Object currentValue = documentSnapshot.get(field);

                    if (currentValue == null) {
                        updates.put(field, assetId);
                        break;
                    }
                }

                studentRef.update(updates).addOnSuccessListener(unused -> {
                    findParentAndSendNotification(form, true);
                    deleteApplication(form);
                }).addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка обновления данных студента", Toast.LENGTH_SHORT).show()
                );
            } else {
                Toast.makeText(this, "Студент не найден", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Ошибка получения данных студента", Toast.LENGTH_SHORT).show()
        );
    }

    private void declineApplication(ApplicationForm form) {
        findParentAndSendNotification(form, false);
        deleteApplication(form);
    }

    private void findParentAndSendNotification(ApplicationForm form, boolean isAccepted) {
        long studentIdAsLong = Long.parseLong(form.getId_Student());

        db.collection("Parent")
                .whereEqualTo("Id_Student", studentIdAsLong)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Берём первый родительский документ
                        String parentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        String title = isAccepted ? "Заявка одобрена" : "Заявка отклонена";
                        String message = isAccepted ?
                                "Здравствуйте! Ваша заявка на секцию принята. Секция ID: " + form.getId_Asset()
                                : "Здравствуйте! К сожалению, ваша заявка на секцию была отклонена.";

                        sendPushNotification(parentId, title, message);
                    } else {
                        Toast.makeText(this, "Родитель не найден", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка поиска родителя", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Отправляем пуш, если есть токен, иначе сохраняем уведомление в базу для показа позже
     */
    private void sendPushNotification(String parentId, String title, String body) {
        db.collection("Parent").document(parentId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String fcmToken = doc.getString("fcmToken");
                if (fcmToken != null && !fcmToken.isEmpty()) {
                    // Здесь должен быть вызов сервера или Firebase Function для отправки FCM push
                    // В демонстрационных целях просто выводим Toast (удалите или замените на реальный вызов)
                    Toast.makeText(this, "Отправляем пуш на токен: " + fcmToken, Toast.LENGTH_SHORT).show();

                    // TODO: Вызов сервера/Cloud Function для отправки push на fcmToken
                } else {
                    // Токена нет — сохраняем уведомление в подколлекцию PendingNotifications
                    saveNotificationForLater(parentId, title, body);
                }
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Ошибка получения данных родителя", Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Сохраняем уведомление, чтобы показать его при следующем входе пользователя
     */
    private void saveNotificationForLater(String parentId, String title, String body) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("body", body);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);

        db.collection("Parent").document(parentId)
                .collection("PendingNotifications")
                .add(notification)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Уведомление сохранено для показа позже", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка сохранения уведомления", Toast.LENGTH_SHORT).show()
                );
    }

    private void deleteApplication(ApplicationForm form) {
        db.collection("Application_form").document(form.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Заявка обработана", Toast.LENGTH_SHORT).show();
                    loadApplications();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка удаления заявки", Toast.LENGTH_SHORT).show()
                );
    }
}
