package com.example.myidealclassapp.Parent;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myidealclassapp.Adapters.ParentAssetAdapter;
import com.example.myidealclassapp.Autorization;
import com.example.myidealclassapp.Classes.Asset;
import com.example.myidealclassapp.Dropdown_menu.Parent_dropdown_menu;
import com.example.myidealclassapp.R;
import com.example.myidealclassapp.Utilits.LoadingUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Parent_asset extends AppCompatActivity {
    private int currentStudentId;
    private RecyclerView recyclerView;
    private ParentAssetAdapter adapter;
    private List<Asset> assetList;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_parent_asset);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> insets);
        currentStudentId = getIntent().getIntExtra("EXTRA_STUDENT_ID", -1);
        ImageView dropdownMenu = findViewById(R.id.dropdown_menu);
        dropdownMenu.setOnClickListener(view ->
                Parent_dropdown_menu.showCustomPopupMenu(view, this, currentStudentId)
        );
        hideSystemUI();
        com.example.myidealclassapp.Utilits.LoadingUtil.showLoading(this);
        recyclerView = findViewById(R.id.recyclerViewAssets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        assetList = new ArrayList<>();
        adapter = new ParentAssetAdapter(this, assetList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadAssetsFromFirestore();
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
    public void moreButton(View view) {
        Intent intent = new Intent(this, Parent_asset_select.class);
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
    private void loadAssetsFromFirestore() {
        CollectionReference assetRef = db.collection("Asset");

        assetRef.get().addOnSuccessListener(querySnapshot -> {
            assetList.clear();

            for (QueryDocumentSnapshot document : querySnapshot) {
                Asset asset = new Asset();
                asset.setTitle(document.getString("Title"));
                asset.setDescribe(document.getString("Describe"));
                asset.setPlace(document.getString("Place"));
                asset.setImageBase64(document.getString("ImageBase64")); // сразу из документа
                String idEmployee = document.getString("Id_Employee");
                asset.setId_Employee(idEmployee);

                // Загрузим ФИО сотрудника по id
                db.collection("Employees").document(idEmployee).get().addOnSuccessListener(employeeSnap -> {
                    if (employeeSnap.exists()) {
                        String lastName = employeeSnap.getString("LastName");
                        String firstName = employeeSnap.getString("FirstName");
                        String middleName = employeeSnap.getString("MiddleName");

                        String fullName = ((lastName != null) ? lastName : "") + " " +
                                ((firstName != null) ? firstName : "") + " " +
                                ((middleName != null) ? middleName : "");

                        // Добавим ФИО руководителя к описанию ассета или отдельным полем
                        asset.setDescribe(asset.getDescribe() + "\nРуководитель: " + fullName);
                    }

                    assetList.add(asset);
                    adapter.notifyDataSetChanged();
                    LoadingUtil.hideLoadingWithAnimation(this);
                });
            }
        }).addOnFailureListener(e -> Log.e("Firestore", "Ошибка при получении данных: ", e));
    }

}
