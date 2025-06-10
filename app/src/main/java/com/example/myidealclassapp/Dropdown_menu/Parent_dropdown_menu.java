package com.example.myidealclassapp.Dropdown_menu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.myidealclassapp.Parent.Parent_Evalution;
import com.example.myidealclassapp.Parent.Parent_application_form;
import com.example.myidealclassapp.Parent.Parent_asset;
import com.example.myidealclassapp.Parent.Parent_asset_select;
import com.example.myidealclassapp.Parent.Parent_evalution_final;
import com.example.myidealclassapp.Parent.Parent_homework;
import com.example.myidealclassapp.Parent.Parent_important_information;
import com.example.myidealclassapp.Parent.Parent_measure;
import com.example.myidealclassapp.Parent.Parent_school_page;
import com.example.myidealclassapp.Parent.Parent_teacher;
import com.example.myidealclassapp.R;

public class Parent_dropdown_menu {
    public static void showCustomPopupMenu(View view, Context context, int currentStudentId) {
        // Раздуваем кастомное меню из XML
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.menu_parent, null);

        // Создаем PopupWindow
        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        // Находим элементы меню
        LinearLayout main_window = popupView.findViewById(R.id.main_window);
//        LinearLayout profile = popupView.findViewById(R.id.profile);
        LinearLayout important_information = popupView.findViewById(R.id.important_information);
        LinearLayout measure = popupView.findViewById(R.id.measure);
        LinearLayout asset = popupView.findViewById(R.id.asset);
        LinearLayout asset_select = popupView.findViewById(R.id.asset_select);
        LinearLayout application_form = popupView.findViewById(R.id.application_form);
        LinearLayout evalution = popupView.findViewById(R.id.evalution);
        LinearLayout final_evalution = popupView.findViewById(R.id.final_evalution);
        LinearLayout homework = popupView.findViewById(R.id.homework);
        LinearLayout school_page = popupView.findViewById(R.id.school_page);
        LinearLayout teacher = popupView.findViewById(R.id.teacher);

        // Обработчики кликов
        main_window.setOnClickListener(v -> {
            Toast.makeText(context, "Вы перешли на страницу", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, Parent_school_page.class);
            intent.putExtra("EXTRA_STUDENT_ID", currentStudentId);
            context.startActivity(intent);
            popupWindow.dismiss();
        });
//
//        profile.setOnClickListener(v -> {
//            Toast.makeText(context, "Вы перешли в профиль", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(context, Parent_profile.class);
//            intent.putExtra("student_id", studentId);
//            context.startActivity(intent);
//            popupWindow.dismiss();
//        });

        important_information.setOnClickListener(v -> {
            Toast.makeText(context, "Вы перешли к важной информации", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, Parent_important_information.class);
            intent.putExtra("EXTRA_STUDENT_ID", currentStudentId);
            context.startActivity(intent);
            popupWindow.dismiss();
        });

        measure.setOnClickListener(v -> {
            Toast.makeText(context, "Вы перешли к мерам воздействия", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, Parent_measure.class);
            intent.putExtra("EXTRA_STUDENT_ID", currentStudentId);
            context.startActivity(intent);
            popupWindow.dismiss();
        });

        asset.setOnClickListener(v -> {
            Toast.makeText(context, "Вы перешли к имуществу", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, Parent_asset.class);
            intent.putExtra("EXTRA_STUDENT_ID", currentStudentId);
            context.startActivity(intent);
            popupWindow.dismiss();
        });

        asset_select.setOnClickListener(v -> {
            Toast.makeText(context, "Вы выбрали имущество", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, Parent_asset_select.class);
            intent.putExtra("EXTRA_STUDENT_ID", currentStudentId);
            context.startActivity(intent);
            popupWindow.dismiss();
        });

        application_form.setOnClickListener(v -> {
            Toast.makeText(context, "Вы перешли к заявке", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, Parent_application_form.class);
            intent.putExtra("EXTRA_STUDENT_ID", currentStudentId);
            context.startActivity(intent);
            popupWindow.dismiss();
        });

        evalution.setOnClickListener(v -> {
            Toast.makeText(context, "Вы перешли к оценкам", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, Parent_Evalution.class);
            intent.putExtra("EXTRA_STUDENT_ID", currentStudentId);
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            try {
                context.startActivity(intent);
                popupWindow.dismiss();
            } catch (Exception e) {
                Log.d("ParentDropdownMenu", "Ошибка при запуске Parent_Evalution", e);
                Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        final_evalution.setOnClickListener(v -> {
            Toast.makeText(context, "Вы перешли к итоговым оценкам", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, Parent_evalution_final.class);
            intent.putExtra("EXTRA_STUDENT_ID", currentStudentId);
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            try {
                context.startActivity(intent);
                popupWindow.dismiss();
            } catch (Exception e) {
                Log.d("ParentDropdownMenu", "Ошибка при запуске Parent_Evalution", e);
                Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        homework.setOnClickListener(v -> {
            Toast.makeText(context, "Вы перешли к домашнему заданию", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, Parent_homework.class);
            intent.putExtra("EXTRA_STUDENT_ID", currentStudentId);
            context.startActivity(intent);
            popupWindow.dismiss();
        });
        school_page.setOnClickListener(v -> {
            Toast.makeText(context, "Вы перешли на страницу", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, Parent_school_page.class);
            intent.putExtra("EXTRA_STUDENT_ID", currentStudentId);
            context.startActivity(intent);
            popupWindow.dismiss();
        });
        teacher.setOnClickListener(v -> {
            Toast.makeText(context, "Вы перешли на страницу", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, Parent_teacher.class);
            intent.putExtra("EXTRA_STUDENT_ID", currentStudentId);
            context.startActivity(intent);
            popupWindow.dismiss();
        });
        popupWindow.showAtLocation(
                view,
                Gravity.START | Gravity.TOP,  // Gravity.START = левый край (для RTL тоже работает)
                0,  // X = 0 (отступ слева)
                0   // Y = 0 (отступ сверху)
        );
        // Показываем меню
        popupWindow.showAsDropDown(view);
    }

}

