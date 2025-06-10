package com.example.myidealclassapp.Classes;

public class School {
    private String Abbreviation;
    private String Adress_School;
    private String Date_of_creation;
    private String Director;
    private String DirectorImage;
    private String Education_level;
    private String Email_School;
    private String Form_Education;
    private int How_Student;
    private String Language_Study;
    private String Phone_School;
    private String Place;
    private String Title;
    private String Work_schedule;

    // Конструктор
    public School() {
    }
    public School(String abbreviation, String adress_School, String date_of_creation, String director,
                       String directorImage, String education_level, String email_School, String form_Education,
                       int how_Student, String language_Study, String phone_School, String place,
                       String title, String work_schedule) {
        Abbreviation = abbreviation;
        Adress_School = adress_School;
        Date_of_creation = date_of_creation;
        Director = director;
        DirectorImage = directorImage;
        Education_level = education_level;
        Email_School = email_School;
        Form_Education = form_Education;
        How_Student = how_Student;
        Language_Study = language_Study;
        Phone_School = phone_School;
        Place = place;
        Title = title;
        Work_schedule = work_schedule;
    }

    // Геттеры
    public String getAbbreviation() { return Abbreviation; }
    public String getAdress_School() { return Adress_School; }
    public String getDate_of_creation() { return Date_of_creation; }
    public String getDirector() { return Director; }
    public String getDirectorImage() { return DirectorImage; }
    public String getEducation_level() { return Education_level; }
    public String getEmail_School() { return Email_School; }
    public String getForm_Education() { return Form_Education; }
    public int getHow_Student() { return How_Student; }
    public String getLanguage_Study() { return Language_Study; }
    public String getPhone_School() { return Phone_School; }
    public String getPlace() { return Place; }
    public String getTitle() { return Title; }
    public String getWork_schedule() { return Work_schedule; }
}
