package com.example.myidealclassapp.Dropdown_menu;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.example.myidealclassapp.Admin.Admin_about_the_app;
import com.example.myidealclassapp.Admin.Admin_asset;
import com.example.myidealclassapp.Admin.Admin_important_information;
import com.example.myidealclassapp.Admin.Admin_main_window;
import com.example.myidealclassapp.Admin.Admin_measure;
import com.example.myidealclassapp.Admin.Admin_parent;
import com.example.myidealclassapp.Admin.Admin_student;
import com.example.myidealclassapp.Admin.Admin_teacher;
import com.example.myidealclassapp.R;
import com.example.myidealclassapp.Teacher.Teacher_Important_information;

public class Admin_dropdown_menu {

    public static void showCustomPopupMenu(View view, Context context, String employeeId) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.menu_admin, null);

        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        LinearLayout mainWindow = popupView.findViewById(R.id.main_window);
        LinearLayout importantInformation = popupView.findViewById(R.id.important_information);
        LinearLayout measure = popupView.findViewById(R.id.measure);
        LinearLayout asset = popupView.findViewById(R.id.asset);

        LinearLayout teacher = popupView.findViewById(R.id.teacher);
        LinearLayout student = popupView.findViewById(R.id.student);
        LinearLayout parent = popupView.findViewById(R.id.parent);
        LinearLayout aboutApp = popupView.findViewById(R.id.about_app);


        mainWindow.setOnClickListener(v -> {
            Intent intent = new Intent(context, Admin_main_window.class);
            intent.putExtra("employee_id", employeeId);
            context.startActivity(intent);
            popupWindow.dismiss();
        });

        importantInformation.setOnClickListener(v -> {
            Intent intent = new Intent(context, Admin_important_information.class);
            intent.putExtra("employee_id", employeeId);
            context.startActivity(intent);
            popupWindow.dismiss();
        });

        measure.setOnClickListener(v -> {
            Intent intent = new Intent(context, Admin_measure.class);
            intent.putExtra("employee_id", employeeId);
            context.startActivity(intent);
            popupWindow.dismiss();
        });

        asset.setOnClickListener(v -> {
            Intent intent = new Intent(context, Admin_asset.class);
            intent.putExtra("employee_id", employeeId);
            context.startActivity(intent);
            popupWindow.dismiss();
        });



        teacher.setOnClickListener(v -> {
            Intent intent = new Intent(context, Admin_teacher.class);
            intent.putExtra("employee_id", employeeId);
            context.startActivity(intent);
            popupWindow.dismiss();
        });

        student.setOnClickListener(v -> {
            Intent intent = new Intent(context, Admin_student.class);
            intent.putExtra("employee_id", employeeId);
            context.startActivity(intent);
            popupWindow.dismiss();
        });

        parent.setOnClickListener(v -> {
            Intent intent = new Intent(context, Admin_parent.class);
            intent.putExtra("employee_id", employeeId);
            context.startActivity(intent);
            popupWindow.dismiss();
        });

        aboutApp.setOnClickListener(v -> {
            Intent intent = new Intent(context, Admin_about_the_app.class);
            intent.putExtra("employee_id", employeeId);
            context.startActivity(intent);
            popupWindow.dismiss();
        });


        // Показать меню
        popupWindow.showAtLocation(view, Gravity.START | Gravity.TOP, 0, 0);
        popupWindow.showAsDropDown(view);
    }
}
