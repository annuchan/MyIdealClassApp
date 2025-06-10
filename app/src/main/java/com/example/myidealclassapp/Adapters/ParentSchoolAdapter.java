package com.example.myidealclassapp.Adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myidealclassapp.Classes.School;
import com.example.myidealclassapp.R;

public class ParentSchoolAdapter {
    private final School school;
    private final Activity activity;

    public ParentSchoolAdapter(Activity activity, School school) {
        this.activity = activity;
        this.school = school;
    }

    public void bindDataToViews() {
        ((TextView) activity.findViewById(R.id.SchoolTitle)).setText(school.getTitle());
        ((TextView) activity.findViewById(R.id.SchoolAbbreviature)).setText(school.getAbbreviation());
        ((TextView) activity.findViewById(R.id.SchoolAdress)).setText(school.getAdress_School());
        ((TextView) activity.findViewById(R.id.SchoolPhone)).setText(school.getPhone_School());
        ((TextView) activity.findViewById(R.id.ditrectorname)).setText(school.getDirector());
        ((TextView) activity.findViewById(R.id.schoolemail)).setText(school.getEmail_School());
        ((TextView) activity.findViewById(R.id.schoolstudents)).setText(String.valueOf(school.getHow_Student()));
        ((TextView) activity.findViewById(R.id.schoolyear)).setText(school.getDate_of_creation());
        ((TextView) activity.findViewById(R.id.schoollanguage)).setText(school.getLanguage_Study());
        ((TextView) activity.findViewById(R.id.schoolformaobycheniya)).setText(school.getForm_Education());
        ((TextView) activity.findViewById(R.id.schoolwork)).setText(school.getWork_schedule());
        ((TextView) activity.findViewById(R.id.schoolychreditel)).setText(school.getPlace());

        // Преобразуем Base64 → Bitmap для фото директора
        try {
            byte[] imageBytes = Base64.decode(school.getDirectorImage(), Base64.DEFAULT);
            Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            ((ImageView) activity.findViewById(R.id.directorimg)).setImageBitmap(decodedImage);
        } catch (Exception e) {
            Log.e("ParentSchoolAdapter", "Ошибка при декодировании изображения", e);
        }
    }
}
