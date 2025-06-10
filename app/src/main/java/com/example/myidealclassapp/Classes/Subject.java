package com.example.myidealclassapp.Classes;

public class Subject {
    public int Id;
    public String Title;

    public Subject(int id, String title) {
        this.Id = id;
        this.Title = title;
    }

    @Override
    public String toString() {
        return Title; // чтобы в Spinner отображалось название предмета
    }
}
