package com.example.myidealclassapp.Classes;

import com.google.firebase.firestore.PropertyName;

public class Measure {
    private int id;
    private String Title;
    private String Describe;
    private String Type_Measure;
    private String Date_Measure;
    private String ImageBase64;

    private String id_teacher;
    private int id_type; // Всегда 0

    // Пустой конструктор для Firestore
    public Measure() {}

    public Measure(String title, String describe, String type_measure, String date_measure, String imageBase64) {
        this.Title = title;
        this.Describe = describe;
        this.Type_Measure = type_measure;
        this.Date_Measure = date_measure;
        this.ImageBase64 = imageBase64;
    }

    // Новый конструктор для удобного добавления
    public Measure(String title, String describe, String date_measure, String id_teacher, int id_type, String imageBase64) {
        this.Title = title;
        this.Describe = describe;
        this.Type_Measure = ""; // Можно оставить пустым или удалить из класса, если не нужен
        this.Date_Measure = date_measure;
        this.ImageBase64 = imageBase64;
        this.id_teacher = id_teacher;
        this.id_type = id_type;
    }

    // Геттеры и сеттеры
    @PropertyName("id")
    public int getId() {
        return id;
    }
    @PropertyName("id")
    public void setId(int id) {
        this.id = id;
    }
    @PropertyName("Title")
    public String getTitle() {
        return Title;
    }
    @PropertyName("Title")
    public void setTitle(String title) {
        Title = title;
    }
    @PropertyName("Describe")
    public String getDescribe() {
        return Describe;
    }
    @PropertyName("Describe")
    public void setDescribe(String describe) {
        Describe = describe;
    }
    @PropertyName("Type_Measure")
    public String getType_Measure() {
        return Type_Measure;
    }
    @PropertyName("Type_Measure")
    public void setType_Measure(String type_Measure) {
        Type_Measure = type_Measure;
    }
    @PropertyName("Date_Measure")
    public String getDate_Measure() {
        return Date_Measure;
    }
    @PropertyName("Date_Measure")
    public void setDate_Measure(String date_Measure) {
        Date_Measure = date_Measure;
    }
    @PropertyName("ImageBase64")
    public String getImageBase64() {
        return ImageBase64;
    }
    @PropertyName("ImageBase64")
    public void setImageBase64(String imageBase64) {
        ImageBase64 = imageBase64;
    }
    @PropertyName("Id_Employee")
    public String getId_teacher() {
        return id_teacher;
    }
    @PropertyName("Id_Employee")
    public void setId_teacher(String id_teacher) {
        this.id_teacher = id_teacher;
    }
   }
