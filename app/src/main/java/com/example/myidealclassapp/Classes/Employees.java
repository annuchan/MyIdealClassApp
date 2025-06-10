package com.example.myidealclassapp.Classes;

import com.google.firebase.firestore.PropertyName;

public class Employees {
    private Long Id;
    private String FirstName;
    private String LastName;
    private String MiddleName;
    private String Specialty;
    private Long Experience;
    private String Certificate;
    private String Qualification;
    private Long Id_Subject;
    private String subjectTitle;
    private String ImageBase64;

    // Добавляем недостающие поля из XML:
    private String Date_Of_Birth;
    private String Address;
    private String Phone;
    private String Email;

    public Employees() {
    }

    public Employees(Long id, String firstName, String lastName, String middleName, String specialty, String subjectTitle,
                     Long experience, String certificate, String qualification, Long Id_Subject, String imageBase64,
                     String date_Of_Birth, String address, String phone, String email) {
        this.Id = id;
        this.FirstName = firstName;
        this.LastName = lastName;
        this.MiddleName = middleName;
        this.Specialty = specialty;
        this.subjectTitle = subjectTitle;
        this.Experience = experience;
        this.Certificate = certificate;
        this.Qualification = qualification;
        this.Id_Subject = Id_Subject;
        this.ImageBase64 = imageBase64;

        this.Date_Of_Birth = date_Of_Birth;
        this.Address = address;
        this.Phone = phone;
        this.Email = email;
    }

    // Геттеры и сеттеры
    @PropertyName("Id")
    public Long getId() { return Id; }
    @PropertyName("Id")
    public void setId(Long id) { Id = id; }
    @PropertyName("FirstName")
    public String getFirstName() { return FirstName; }
    @PropertyName("FirstName")
    public void setFirstName(String firstName) { FirstName = firstName; }
    @PropertyName("LastName")
    public String getLastName() { return LastName; }
    @PropertyName("LastName")
    public void setLastName(String lastName) { LastName = lastName; }
    @PropertyName("MiddleName")
    public String getMiddleName() { return MiddleName; }
    @PropertyName("MiddleName")
    public void setMiddleName(String middleName) { MiddleName = middleName; }
    @PropertyName("Specialty")
    public String getSpecialty() { return Specialty; }
    @PropertyName("Specialty")
    public void setSpecialty(String specialty) { Specialty = specialty; }
    @PropertyName("Experience")
    public long getExperience() { return Experience; }
    @PropertyName("Experience")
    public void setExperience(long experience) { Experience = experience; }
    @PropertyName("Certificate")
    public String getCertificate() { return Certificate; }
    @PropertyName("Certificate")
    public void setCertificate(String certificate) { Certificate = certificate; }
    @PropertyName("Qualification")
    public String getQualification() { return Qualification; }
    @PropertyName("Qualification")
    public void setQualification(String qualification) { Qualification = qualification; }
    @PropertyName("Id_Subject")
    public Long getSubject() { return Id_Subject; }
    @PropertyName("Id_Subject")
    public void setSubject(Long subject) { Id_Subject = Id_Subject; }
    public String getSubjectTitle() { return subjectTitle; }
    public void setSubjectTitle(String subjectTitle) { this.subjectTitle = subjectTitle; }
    @PropertyName("ImageBase64")
    public String getImageBase64() { return ImageBase64; }
    @PropertyName("ImageBase64")
    public void setImageBase64(String imageBase64) { ImageBase64 = imageBase64; }
    @PropertyName("Date_Of_Birth")
    // Геттеры и сеттеры для новых полей
    public String getDate_Of_Birth() { return Date_Of_Birth; }
    @PropertyName("Date_Of_Birth")
    public void setDate_Of_Birth(String date_Of_Birth) { Date_Of_Birth = date_Of_Birth; }
    @PropertyName("Address")
    public String getAddress() { return Address; }
    @PropertyName("Address")
    public void setAddress(String address) { Address = address; }
    @PropertyName("Phone")
    public String getPhone() { return Phone; }
    @PropertyName("Phone")
    public void setPhone(String phone) { Phone = phone; }
    @PropertyName("Email")
    public String getEmail() { return Email; }
    @PropertyName("Email")
    public void setEmail(String email) { Email = email; }
}
