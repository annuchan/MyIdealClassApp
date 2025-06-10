package com.example.myidealclassapp.Classes;

public class ApplicationForm {
    private String id;           // Firestore ID (устанавливается вручную)
    private String Id_Student;
    private Long Id_Asset;
    private String EmailParent;
    private String FirstName;
    private String LastName;
    private String MiddleName;
    private Long Id_Class;

    // Пустой конструктор для Firestore
    public ApplicationForm() {}

    // Геттеры
    public String getId() {
        return id;
    }

    public String getId_Student() {
        return Id_Student;
    }

    public Long getId_Asset() {
        return Id_Asset;
    }

    public String getEmailParent() {
        return EmailParent;
    }

    public String getFirstName() {
        return FirstName;
    }

    public String getLastName() {
        return LastName;
    }

    public String getMiddleName() {
        return MiddleName;
    }

    public Long getId_Class() {
        return Id_Class;
    }

    // Сеттеры
    public void setId(String id) {
        this.id = id;
    }

    public void setId_Student(String id_Student) {
        this.Id_Student = id_Student;
    }

    public void  setId_Asset(Long id_Asset) {
        this.Id_Asset = id_Asset;
    }

    public void setEmailParent(String emailParent) {
        this.EmailParent = emailParent;
    }

    public void setFirstName(String firstName) {
        this.FirstName = firstName;
    }

    public void setLastName(String lastName) {
        this.LastName = lastName;
    }

    public void setMiddleName(String middleName) {
        this.MiddleName = middleName;
    }

    public void setId_Class(Long id_Class) {
        this.Id_Class = id_Class;
    }
}
