package com.example.myidealclassapp.Classes;

import com.google.firebase.firestore.PropertyName;

public class Asset {
    private int Id;
    private String Title;
    private String Describe;
    private String Id_Employee;
    private String Place;
    private String ImageBase64;

    public Asset() {}

    public Asset(String title, String describe, String id_employee, String place, String imageBase64) {
        Title = title;
        Describe = describe;
        Id_Employee = id_employee;
        Place = place;
        ImageBase64 = imageBase64;
    }

    @PropertyName("Id")
    public int getId() { return Id; }

    @PropertyName("Id")
    public void setId(int id) { this.Id = id; }

    @PropertyName("Title")
    public String getTitle() { return Title; }

    @PropertyName("Title")
    public void setTitle(String title) { Title = title; }

    @PropertyName("Describe")
    public String getDescribe() { return Describe; }

    @PropertyName("Describe")
    public void setDescribe(String describe) { Describe = describe; }

    @PropertyName("Id_Employee")
    public String getId_Employee() { return Id_Employee; }

    @PropertyName("Id_Employee")
    public void setId_Employee(String id_employee) { Id_Employee = id_employee; }

    @PropertyName("Place")
    public String getPlace() { return Place; }

    @PropertyName("Place")
    public void setPlace(String place) { Place = place; }

    @PropertyName("ImageBase64")
    public String getImageBase64() { return ImageBase64; }

    @PropertyName("ImageBase64")
    public void setImageBase64(String imageBase64) { ImageBase64 = imageBase64; }
}