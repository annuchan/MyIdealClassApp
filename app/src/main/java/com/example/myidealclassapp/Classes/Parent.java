package com.example.myidealclassapp.Classes;

import com.google.firebase.firestore.PropertyName;

public class Parent {
    private Long Id;
    private String Address;
    private String Date_Of_Birth;
    private String Email;
    private String FirstName;
    private String LastName;
    private String MiddleName;
    private String Phone;
    private Long Id_Student;

    public Parent() {
    }

    public Parent(Long id, String address, String date_Of_Birth, String email,
                  String firstName, String lastName, String middleName, String phone, Long id_Student) {
        this.Id = id;
        this.Address = address;
        this.Date_Of_Birth = date_Of_Birth;
        this.Email = email;
        this.FirstName = firstName;
        this.LastName = lastName;
        this.MiddleName = middleName;
        this.Phone = phone;
        this.Id_Student = id_Student;
    }
    @PropertyName("id")
    public Long getId() {
        return Id;
    }
    @PropertyName("id")
    public void setId(Long id) {
        Id = id;
    }
    @PropertyName("Address")
    public String getAddress() {
        return Address;
    }
    @PropertyName("Address")
    public void setAddress(String address) {
        Address = address;
    }
    @PropertyName("Date_Of_Birth")
    public String getDate_Of_Birth() {
        return Date_Of_Birth;
    }
    @PropertyName("Date_Of_Birth")
    public void setDate_Of_Birth(String date_Of_Birth) {
        Date_Of_Birth = date_Of_Birth;
    }
    @PropertyName("Email")
    public String getEmail() {
        return Email;
    }
    @PropertyName("Email")
    public void setEmail(String email) {
        Email = email;
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
    @PropertyName("Phone")
    public String getPhone() {
        return Phone;
    }
    @PropertyName("Phone")
    public void setPhone(String phone) {
        Phone = phone;
    }
    @PropertyName("Id_Student")
    public Long getId_Student() {
        return Id_Student;
    }
    @PropertyName("Id_Student")
    public void setId_Student(Long id_Student) {
        Id_Student = id_Student;
    }
}
