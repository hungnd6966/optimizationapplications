package com.hust.opt.optapp.teacherclassassignment.model;

import java.util.ArrayList;

public class Teacher {
    private String code;
    private String name;
    private int maxCredit;
    private ArrayList<CourseForTeacher> courses;

    
    public Teacher(String code, String name, int maxCredit,
			ArrayList<CourseForTeacher> courses) {
		super();
		this.code = code;
		this.name = name;
		this.maxCredit = maxCredit;
		this.courses = courses;
	}

	public Teacher() {
		super();
		// TODO Auto-generated constructor stub
	}

	public int getMaxCredit() {
        return maxCredit;
    }

    public void setMaxCredit(int maxCredit) {
        this.maxCredit = maxCredit;
    }

    public ArrayList<CourseForTeacher> getCourses() {
        return courses;
    }

    public void setCourses(ArrayList<CourseForTeacher> courses) {
        this.courses = courses;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " : " + code;
    }
}
