package com.example.myidealclassapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myidealclassapp.Classes.Student;
import com.example.myidealclassapp.R;
import com.example.myidealclassapp.Admin.Admin_student_edit;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class AdminStudentAdapter extends RecyclerView.Adapter<AdminStudentAdapter.ViewHolder> {

    private final Context context;
    private final List<Student> studentList;
    private final Map<String, String> classIdToNameMap; // ключ — строковый id класса, значение — номер класса/имя
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AdminStudentAdapter(Context context, List<Student> studentList, Map<String, String> classIdToNameMap) {
        this.context = context;
        this.studentList = studentList;
        this.classIdToNameMap = classIdToNameMap;
    }

    @NonNull
    @Override
    public AdminStudentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_student, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminStudentAdapter.ViewHolder holder, int position) {
        Student student = studentList.get(position);

        holder.lastName.setText(student.getLastName());
        holder.firstName.setText(student.getFirstName());
        holder.middleName.setText(student.getMiddleName());
        holder.dateOfBirth.setText(student.getDate_Of_Birth());
        holder.address.setText(student.getAddress());
        holder.phone.setText(student.getPhone());

        // Приводим Long к String, чтобы мапа нормально работала


        holder.detailsImage.setOnClickListener(v -> showPopupMenu(v, student, position));
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView lastName, firstName, middleName, dateOfBirth, address, phone, classNumber;
        ImageView detailsImage;

        public ViewHolder(View itemView) {
            super(itemView);
            lastName = itemView.findViewById(R.id.LastName);
            firstName = itemView.findViewById(R.id.FirstName);
            middleName = itemView.findViewById(R.id.MiddleName);
            dateOfBirth = itemView.findViewById(R.id.Date_Of_Birth);
            address = itemView.findViewById(R.id.Address);
            phone = itemView.findViewById(R.id.Phone);
            detailsImage = itemView.findViewById(R.id.detailsImageView);
        }
    }

    private void showPopupMenu(View anchorView, Student student, int position) {
        View popupView = LayoutInflater.from(context).inflate(R.layout.item_edit_delete, null);
        final PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setElevation(10);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(anchorView, -100, 0);

        LinearLayout edit = popupView.findViewById(R.id.edit);
        LinearLayout delete = popupView.findViewById(R.id.delete);

        edit.setOnClickListener(v -> {
            popupWindow.dismiss();
            Intent intent = new Intent(context, Admin_student_edit.class);
            intent.putExtra("Id", student.getId());
            intent.putExtra("FirstName", student.getFirstName());
            intent.putExtra("LastName", student.getLastName());
            intent.putExtra("MiddleName", student.getMiddleName());
            intent.putExtra("Date_Of_Birth", student.getDate_Of_Birth());
            intent.putExtra("Address", student.getAddress());
            intent.putExtra("Phone", student.getPhone());
            intent.putExtra("ClassId", student.getId_Class()); // передаем ID класса именно так, как есть
            context.startActivity(intent);
        });

        delete.setOnClickListener(v -> {
            popupWindow.dismiss();
            showDeleteDialog(student, position);
        });
    }

    private void showDeleteDialog(Student student, int position) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.item_delete, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        // Начальная прозрачность 0
        dialogView.setAlpha(0f);
        dialog.show();

        // Плавное появление (fade in)
        dialogView.animate().alpha(1f).setDuration(300).start();

        ImageView yesButton = dialogView.findViewById(R.id.yesbutton);
        ImageView noButton  = dialogView.findViewById(R.id.nobutton);

        yesButton.setOnClickListener(v -> {
            // Плавное исчезновение (fade out)
            dialogView.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                db.collection("Student")
                        .whereEqualTo("Id", student.getId())
                        .get()
                        .addOnSuccessListener(snap -> {
                            if (!snap.isEmpty()) {
                                snap.getDocuments().get(0).getReference().delete()
                                        .addOnSuccessListener(aVoid -> {
                                            studentList.remove(position);
                                            notifyItemRemoved(position);
                                            notifyItemRangeChanged(position, studentList.size());
                                        });
                            }
                        });
                dialog.dismiss();
            }).start();
        });

        noButton.setOnClickListener(v -> {
            // Плавное исчезновение (fade out) без удаления
            dialogView.animate().alpha(0f).setDuration(300).withEndAction(dialog::dismiss).start();
        });
    }

}
