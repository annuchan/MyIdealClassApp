package com.example.myidealclassapp.Classes;

import com.google.firebase.firestore.PropertyName;

public class Student {
    private Long Id;
    private Long Id_Asset;
    private Long Id_Class;
    private String FirstName;
    private String LastName;
    private String MiddleName;
    private String Date_Of_Birth;
    private String Address;
    private String Phone;

    public Student() {
    }

    public Student(Long id, Long id_Asset, Long id_Class, String firstName, String lastName,
                   String middleName, String date_Of_Birth, String address, String phone) {
        this.Id = id;
        this.Id_Asset = id_Asset;
        this.Id_Class = id_Class;
        this.FirstName = firstName;
        this.LastName = lastName;
        this.MiddleName = middleName;
        this.Date_Of_Birth = date_Of_Birth;
        this.Address = address;
        this.Phone = phone;
    }
    @PropertyName("Id")
    public Long getId() {
        return Id;
    }
    @PropertyName("Id")
    public void setId(Long id) {
        Id = id;
    }
    @PropertyName("Id_Asset")
    public Long getId_Asset() {
        return Id_Asset;
    }
    @PropertyName("Id_Asset")
    public void setId_Asset(Long id_Asset) {
        Id_Asset = id_Asset;
    }
    @PropertyName("Id_Class")
    public Long getId_Class() {
        return Id_Class;
    }
    @PropertyName("Id_Class")
    public void setId_Class(Long id_Class) {
        Id_Class = id_Class;
    }
    @PropertyName("FirstName")
    public String getFirstName() {
        return FirstName;
    }
    @PropertyName("FirstName")
    public void setFirstName(String firstName) {
        FirstName = firstName;
    }
    @PropertyName("LastName")
    public String getLastName() {
        return LastName;
    }
    @PropertyName("LastName")
    public void setLastName(String lastName) {
        LastName = lastName;
    }
    @PropertyName("MiddleName")
    public String getMiddleName() {
        return MiddleName;
    }
    @PropertyName("MiddleName")
    public void setMiddleName(String middleName) {
        MiddleName = middleName;
    }
    @PropertyName("Date_Of_Birth")
    public String getDate_Of_Birth() {
        return Date_Of_Birth;
    }
    @PropertyName("Date_Of_Birth")
    public void setDate_Of_Birth(String date_Of_Birth) {
        Date_Of_Birth = date_Of_Birth;
    }
    @PropertyName("Address")
    public String getAddress() {
        return Address;
    }
    @PropertyName("Address")
    public void setAddress(String address) {
        Address = address;
    }
    @PropertyName("Phone")
    public String getPhone() {
        return Phone;
    }
    @PropertyName("Phone")
    public void setPhone(String phone) {
        Phone = phone;
    }
}
