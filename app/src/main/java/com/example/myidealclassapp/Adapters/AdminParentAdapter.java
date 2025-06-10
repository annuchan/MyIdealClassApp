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

import com.example.myidealclassapp.Classes.Parent;
import com.example.myidealclassapp.R;
import com.example.myidealclassapp.Admin.Admin_parent_edit;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminParentAdapter extends RecyclerView.Adapter<AdminParentAdapter.ViewHolder> {

    private final Context context;
    private final List<Parent> parentList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AdminParentAdapter(Context context, List<Parent> parentList) {
        this.context = context;
        this.parentList = parentList;
    }

    @NonNull
    @Override
    public AdminParentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_parent, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminParentAdapter.ViewHolder holder, int position) {
        Parent parent = parentList.get(position);

        holder.lastName.setText(parent.getLastName());
        holder.firstName.setText(parent.getFirstName());
        holder.middleName.setText(parent.getMiddleName());
        holder.dateOfBirth.setText(parent.getDate_Of_Birth());
        holder.address.setText(parent.getAddress());
        holder.phone.setText(parent.getPhone());
        holder.email.setText(parent.getEmail());

        // Загружаем ФИО ученика по Id_Student
        db.collection("Student")
                .whereEqualTo("Id", parent.getId_Student())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String firstName = queryDocumentSnapshots.getDocuments().get(0).getString("FirstName");
                        String lastName = queryDocumentSnapshots.getDocuments().get(0).getString("LastName");
                        String middleName = queryDocumentSnapshots.getDocuments().get(0).getString("MiddleName");

                        String fullName = lastName + " " + firstName + " " + middleName;
                        holder.studentId.setText(fullName);
                    } else {
                        holder.studentId.setText("Ученик не найден");
                    }
                })
                .addOnFailureListener(e -> holder.studentId.setText("Ошибка загрузки"));

        holder.detailsImage.setOnClickListener(v -> showPopupMenu(v, parent, position));
    }

    @Override
    public int getItemCount() {
        return parentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView lastName, firstName, middleName, dateOfBirth, address, phone, email, studentId;
        ImageView detailsImage;

        public ViewHolder(View itemView) {
            super(itemView);
            lastName = itemView.findViewById(R.id.LastName);
            firstName = itemView.findViewById(R.id.FirstName);
            middleName = itemView.findViewById(R.id.MiddleName);
            dateOfBirth = itemView.findViewById(R.id.Date_Of_Birth);
            address = itemView.findViewById(R.id.Address);
            phone = itemView.findViewById(R.id.Phone);
            email = itemView.findViewById(R.id.Email);
            studentId = itemView.findViewById(R.id.Id_Student);
            detailsImage = itemView.findViewById(R.id.detailsImageView);
        }
    }

    private void showPopupMenu(View anchorView, Parent parent, int position) {
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
            Intent intent = new Intent(context, Admin_parent_edit.class);
            intent.putExtra("id", parent.getId());
            intent.putExtra("FirstName", parent.getFirstName());
            intent.putExtra("LastName", parent.getLastName());
            intent.putExtra("MiddleName", parent.getMiddleName());
            intent.putExtra("Date_Of_Birth", parent.getDate_Of_Birth());
            intent.putExtra("Address", parent.getAddress());
            intent.putExtra("Phone", parent.getPhone());
            intent.putExtra("Email", parent.getEmail());
            intent.putExtra("Id_Student", parent.getId_Student());
            context.startActivity(intent);
        });

        delete.setOnClickListener(v -> {
            popupWindow.dismiss();
            showDeleteDialog(parent, position);
        });
    }

    private void showDeleteDialog(Parent parent, int position) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.item_delete, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        // Начинаем с прозрачности 0 на всем dialogView
        dialogView.setAlpha(0f);

        dialog.show();

        // Анимация появления (fade in)
        dialogView.animate().alpha(1f).setDuration(300).start();

        ImageView yesButton = dialogView.findViewById(R.id.yesbutton);
        ImageView noButton = dialogView.findViewById(R.id.nobutton);

        yesButton.setOnClickListener(v -> {
            // Анимация исчезновения (fade out)
            dialogView.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                db.collection("Parent")
                        .whereEqualTo("id", parent.getId())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                queryDocumentSnapshots.getDocuments().get(0).getReference().delete()
                                        .addOnSuccessListener(aVoid -> {
                                            parentList.remove(position);
                                            notifyItemRemoved(position);
                                            notifyItemRangeChanged(position, parentList.size());
                                        });
                            }
                        });
                dialog.dismiss();
            }).start();
        });

        noButton.setOnClickListener(v -> {
            // Анимация исчезновения (fade out)
            dialogView.animate().alpha(0f).setDuration(300).withEndAction(dialog::dismiss).start();
        });
    }

}
