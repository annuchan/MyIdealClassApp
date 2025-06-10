package com.example.myidealclassapp.Classes;

import com.google.firebase.firestore.PropertyName;

public class Shedule {

    private int id;               // Id записи расписания
    private int idClass;          // Id класса
    private String idEmployee;    // Id сотрудника (строка)
    private int idSubject;        // Id предмета
    private int office;           // Кабинет
    private String timeSchedule;  // Время в формате "HH:mm:ss"
    private String weekday;       // День недели (например, "Понедельник")



    public Shedule(int id, int idClass, String idEmployee, int idSubject, int office, String timeSchedule, String weekday) {
        this.id = id;
        this.idClass = idClass;
        this.idEmployee = idEmployee;
        this.idSubject = idSubject;
        this.office = office;
        this.timeSchedule = timeSchedule;
        this.weekday = weekday;
    }
    public Shedule() {
        // Пустой конструктор для Firestore или других ORM
    }

    // Геттеры и сеттеры
    @PropertyName("Id")
    public int getId() {
        return id;
    }
    @PropertyName("Id")
    public void setId(int id) {
        this.id = id;
    }

    public int getIdClass() {
        return idClass;
    }

    public void setIdClass(int idClass) {
        this.idClass = idClass;
    }

    public String getIdEmployee() {
        return idEmployee;
    }

    public void setIdEmployee(String idEmployee) {
        this.idEmployee = idEmployee;
    }

    public int getIdSubject() {
        return idSubject;
    }

    public void setIdSubject(int idSubject) {
        this.idSubject = idSubject;
    }

    public int getOffice() {
        return office;
    }

    public void setOffice(int office) {
        this.office = office;
    }

    public String getTimeSchedule() {
        return timeSchedule;
    }

    public void setTimeSchedule(String timeSchedule) {
        this.timeSchedule = timeSchedule;
    }

    public String getWeekday() {
        return weekday;
    }

    public void setWeekday(String weekday) {
        this.weekday = weekday;
    }


}
