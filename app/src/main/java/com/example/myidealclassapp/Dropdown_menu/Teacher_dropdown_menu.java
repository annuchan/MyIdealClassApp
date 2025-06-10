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

import com.example.myidealclassapp.R;
import com.example.myidealclassapp.Teacher.Teacher_Important_information;
import com.example.myidealclassapp.Teacher.Teacher_about_the_app;
import com.example.myidealclassapp.Teacher.Teacher_asset;
import com.example.myidealclassapp.Teacher.Teacher_class;
import com.example.myidealclassapp.Teacher.Teacher_final_evalution;
import com.example.myidealclassapp.Teacher.Teacher_homework;
import com.example.myidealclassapp.Teacher.Teacher_main_window;
import com.example.myidealclassapp.Teacher.Teacher_measure;
import com.example.myidealclassapp.Teacher.teacher_evalution;

public class Teacher_dropdown_menu {

    public static void showCustomPopupMenu(View view, Context context, String employeeId, int idSubject) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.menu_teacher, null);

        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        LinearLayout main_window = popupView.findViewById(R.id.main_window);
        LinearLayout important_information = popupView.findViewById(R.id.important_information);
        LinearLayout measure = popupView.findViewById(R.id.measure);
        LinearLayout asset = popupView.findViewById(R.id.asset);
        LinearLayout evalution = popupView.findViewById(R.id.evalution);
        LinearLayout final_evalution = popupView.findViewById(R.id.final_evalution);
        LinearLayout homework = popupView.findViewById(R.id.homework);
        LinearLayout about_app = popupView.findViewById(R.id.about_app);
        LinearLayout class_teacher = popupView.findViewById(R.id.class_teacher);

        main_window.setOnClickListener(v -> {
            Toast.makeText(context, "Вы перешли на главную страницу", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, Teacher_main_window.class);
            intent.putExtra("employeeId", employeeId);
            intent.putExtra("subjectId", idSubject);
            context.startActivity(intent);
            popupWindow.dismiss();
        });
        class_teacher.setOnClickListener(v -> {
            Toast.makeText(context, "Вы перешли на страницу своего класса", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, Teacher_class.class);
            intent.putExtra("employeeId", employeeId);
            intent.putExtra("subjectId", idSubject);
            context.startActivity(intent);
            popupWindow.dismiss();
        });

        important_information.setOnClickListener(v -> {
            Toast.makeText(context, "Вы перешли к важной информации", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, Teacher_Important_information.class);
            intent.putExtra("employeeId", employeeId);
            intent.putExtra("subjectId", idSubject);
            context.startActivity(intent);
            popupWindow.dismiss();
        });

        measure.setOnClickListener(v -> {
            Toast.makeText(context, "Вы перешли к мерам воздействия", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, Teacher_measure.class);
            intent.putExtra("employeeId", employeeId);
            intent.putExtra("subjectId", idSubject);
            context.startActivity(intent);
            popupWindow.dismiss();
        });

        asset.setOnClickListener(v -> {
            Toast.makeText(context, "Вы перешли к имуществу", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, Teacher_asset.class);
            intent.putExtra("employeeId", employeeId);
            intent.putExtra("subjectId", idSubject);
            context.startActivity(intent);
            popupWindow.dismiss();
        });

        evalution.setOnClickListener(v -> {
            Toast.makeText(context, "Вы перешли к оценкам", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, teacher_evalution.class);
            intent.putExtra("employeeId", employeeId);
            intent.putExtra("subjectId", idSubject);
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            try {
                context.startActivity(intent);
                popupWindow.dismiss();
            } catch (Exception e) {
                Log.d("TeacherDropdownMenu", "Ошибка при запуске teacher_evalution", e);
                Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        final_evalution.setOnClickListener(v -> {
            Toast.makeText(context, "Вы перешли к итоговым оценкам", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, Teacher_final_evalution.class);
            intent.putExtra("employeeId", employeeId);
            intent.putExtra("subjectId", idSubject);
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            try {
                context.startActivity(intent);
                popupWindow.dismiss();
            } catch (Exception e) {
                Log.d("TeacherDropdownMenu", "Ошибка при запуске Teacher_final_evalution", e);
                Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        homework.setOnClickListener(v -> {
            Toast.makeText(context, "Вы перешли к домашнему заданию", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, Teacher_homework.class);
            intent.putExtra("employeeId", employeeId);
            intent.putExtra("subjectId", idSubject);
            context.startActivity(intent);
            popupWindow.dismiss();
        });

        about_app.setOnClickListener(v -> {
            Toast.makeText(context, "Вы перешли на страницу о приложении", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, Teacher_about_the_app.class);
            intent.putExtra("employeeId", employeeId);
            intent.putExtra("subjectId", idSubject);
            context.startActivity(intent);
            popupWindow.dismiss();
        });
        popupWindow.showAtLocation(view, Gravity.START | Gravity.TOP, 0, 0);
        popupWindow.showAsDropDown(view);
    }
}
