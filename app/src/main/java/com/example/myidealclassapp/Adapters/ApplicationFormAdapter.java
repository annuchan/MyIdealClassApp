package com.example.myidealclassapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myidealclassapp.Classes.ApplicationForm;
import com.example.myidealclassapp.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ApplicationFormAdapter extends RecyclerView.Adapter<ApplicationFormAdapter.ViewHolder> {

    public interface OnMenuClickListener {
        void onAccept(ApplicationForm applicationForm);
        void onDecline(ApplicationForm applicationForm);
    }

    private Context context;
    private List<ApplicationForm> applicationList;
    private OnMenuClickListener listener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ApplicationFormAdapter(Context context, List<ApplicationForm> applicationList, OnMenuClickListener listener) {
        this.context = context;
        this.applicationList = applicationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ApplicationFormAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_teacher_application_form, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicationFormAdapter.ViewHolder holder, int position) {
        ApplicationForm item = applicationList.get(position);

        holder.requestNumber.setText("Заявка №" + (position + 1));

        // Загрузка секции по Id_Asset
        db.collection("Asset")
                .whereEqualTo("Id", item.getId_Asset())
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        String name = query.getDocuments().get(0).getString("Title");
                        holder.assetName.setText("Секция: " + name);
                    } else {
                        holder.assetName.setText("Секция: ID " + item.getId_Asset());
                    }
                });

        // Загрузка класса по Id_Class
        db.collection("Class")
                .whereEqualTo("Id", item.getId_Class())
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        String name = query.getDocuments().get(0).getString("Number");
                        holder.classText.setText("Класс: " + name);
                    } else {
                        holder.classText.setText("Класс: ID " + item.getId_Class());
                    }
                });

        // Загрузка ученика по Id_Student
        try {
            int studentId = Integer.parseInt(item.getId_Student());
            db.collection("Student")
                    .whereEqualTo("Id", studentId)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            var doc = query.getDocuments().get(0);
                            holder.firstName.setText(doc.getString("FirstName"));
                            holder.lastName.setText(doc.getString("LastName"));
                            holder.middleName.setText(doc.getString("MiddleName"));
                        } else {
                            holder.firstName.setText("Не найден");
                            holder.lastName.setText("");
                            holder.middleName.setText("");
                        }
                    });
        } catch (NumberFormatException e) {
            holder.firstName.setText("Ошибка ID");
            holder.lastName.setText("");
            holder.middleName.setText("");
        }

        // Обработка меню
        holder.detailsImageView.setOnClickListener(v -> {
            View popupView = LayoutInflater.from(context).inflate(R.layout.item_teacher_menu_application_form, null);
            final PopupWindow popupWindow = new PopupWindow(popupView,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    true);

            LinearLayout acceptRequest = popupView.findViewById(R.id.accept_request);
            LinearLayout rejectRequest = popupView.findViewById(R.id.reject_request);

            acceptRequest.setOnClickListener(view -> {
                listener.onAccept(item);
                popupWindow.dismiss();
            });

            rejectRequest.setOnClickListener(view -> {
                listener.onDecline(item);
                popupWindow.dismiss();
            });

            popupWindow.showAsDropDown(holder.detailsImageView, 0, 0);
        });
    }

    @Override
    public int getItemCount() {
        return applicationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView requestNumber, assetName, classText, firstName, lastName, middleName;
        ImageView detailsImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            requestNumber = itemView.findViewById(R.id.Application_form);
            assetName = itemView.findViewById(R.id.NameAsset);
            classText = itemView.findViewById(R.id.ClassStudent);
            firstName = itemView.findViewById(R.id.FirstNameStudent);
            lastName = itemView.findViewById(R.id.LastNameStudent);
            middleName = itemView.findViewById(R.id.MiddleNameStudent);
            detailsImageView = itemView.findViewById(R.id.detailsImageView);
        }
    }
}
