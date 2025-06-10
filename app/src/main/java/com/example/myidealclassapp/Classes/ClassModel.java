package com.example.myidealclassapp.Classes;

import com.google.firebase.firestore.PropertyName;

public class ClassModel {
    private int Id;
    private String Number;

    public ClassModel() {} // Firestore требует пустой конструктор

    public ClassModel(int id, String Number) {
        this.Id = id;
        this.Number = Number;
    }
    @PropertyName("Id")
    public int getId() {
        return Id;
    }
    @PropertyName("Number")
    public String getNumber() {
        return Number;
    }

    @Override
    public String toString() {
        return Number;
    }
}