package com.example.myidealclassapp.Classes;

import com.google.firebase.firestore.PropertyName;

public class Important_information {
    private int id;
    private String Title;
    private String Describe;
    private String Date_imp_info;
    private String Id_Employee;
    private String ImageBase64;

    public Important_information() {}

    public Important_information(String Title, String Describe, String Date_imp_info, String Id_Employee, String ImageBase64) {
        this.Title = Title;
        this.Describe = Describe;
        this.Date_imp_info = Date_imp_info;
        this.Id_Employee = Id_Employee;
        this.ImageBase64 = ImageBase64;
    }
    @PropertyName("id")
    public int getId() {
        return id;
    }
    @PropertyName("id")
    public void setId(int Id) {
        this.id = id;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String Title) {
        this.Title = Title;
    }

    public String getDescribe() {
        return Describe;
    }

    public void setDescribe(String Describe) {
        this.Describe = Describe;
    }

    public String getDate_imp_info() {
        return Date_imp_info;
    }

    public void setDate_imp_info(String Date_imp_info) {
        this.Date_imp_info = Date_imp_info;
    }

    public String getId_Employee() {
        return Id_Employee;
    }

    public void setId_Employee(String Id_Employee) {
        this.Id_Employee = Id_Employee;
    }


    public String getImageBase64() {
        return ImageBase64;
    }

    public void setImageBase64(String ImageBase64) {
        this.ImageBase64 = ImageBase64;
    }


}
