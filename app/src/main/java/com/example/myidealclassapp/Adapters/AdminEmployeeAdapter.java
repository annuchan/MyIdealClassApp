package com.example.myidealclassapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myidealclassapp.Admin.Admin_teacher_edit;
import com.example.myidealclassapp.Classes.Employees;
import com.example.myidealclassapp.R;
import com.example.myidealclassapp.Utilits.LoadingDelete;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminEmployeeAdapter extends RecyclerView.Adapter<AdminEmployeeAdapter.ViewHolder> {

    private final Context context;
    private final List<Employees> employeeList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Employees employeeToDelete;
    private int positionToDelete;

    public AdminEmployeeAdapter(Context context, List<Employees> employeeList) {
        this.context = context;
        this.employeeList = employeeList;
    }

    @NonNull
    @Override
    public AdminEmployeeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_emploee, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminEmployeeAdapter.ViewHolder holder, int position) {
        Employees employee = employeeList.get(position);

        holder.lastName.setText(employee.getLastName());
        holder.firstName.setText(employee.getFirstName());
        holder.middleName.setText(employee.getMiddleName());
        holder.dateOfBirth.setText(employee.getDate_Of_Birth());
        holder.address.setText(employee.getAddress());
        holder.phone.setText(employee.getPhone());
        holder.email.setText(employee.getEmail());

        holder.detailsImage.setOnClickListener(v -> showPopupMenu(v, employee, position));
    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView lastName, firstName, middleName, dateOfBirth, address, phone, email;
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
            detailsImage = itemView.findViewById(R.id.detailsImageView);
        }
    }

    private void showPopupMenu(View anchorView, Employees employee, int position) {
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
            Intent intent = new Intent(context, Admin_teacher_edit.class);

            intent.putExtra("Id", employee.getId());
            intent.putExtra("LastName", employee.getLastName());
            intent.putExtra("FirstName", employee.getFirstName());
            intent.putExtra("MiddleName", employee.getMiddleName());
            intent.putExtra("Date_Of_Birth", employee.getDate_Of_Birth());
            intent.putExtra("Address", employee.getAddress());
            intent.putExtra("Phone", employee.getPhone());
            intent.putExtra("Email", employee.getEmail());

            context.startActivity(intent);
        });

        delete.setOnClickListener(v -> {
            popupWindow.dismiss();
            deleteEmployee(employee, position);
        });
    }

    private void deleteEmployee(Employees employee, int position) {
        Long idToDelete = employee.getId();

        db.collection("Employees")
                .whereEqualTo("Id", idToDelete)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        querySnapshot.getDocuments().get(0).getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    employeeList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, employeeList.size());
                                    Toast.makeText(context, "Удалено успешно", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Ошибка удаления: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(context, "Документ с Id " + idToDelete + " не найден", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Ошибка поиска документа: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
