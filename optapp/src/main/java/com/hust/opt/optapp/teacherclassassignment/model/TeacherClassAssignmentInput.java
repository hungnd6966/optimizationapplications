package com.hust.opt.optapp.teacherclassassignment.model;

import java.util.ArrayList;
import java.util.Random;

public class TeacherClassAssignmentInput {
    private Class[] classes;
    private Teacher[] teachers;

    public Class[] getClasses() {
        return classes;
    }

    public void setClasses(Class[] classes) {
        this.classes = classes;
    }

    public Teacher[] getTeachers() {
        return teachers;
    }

    public void setTeachers(Teacher[] teachers) {
        this.teachers = teachers;
    }

	public TeacherClassAssignmentInput(Class[] classes, Teacher[] teachers) {
		super();
		this.classes = classes;
		this.teachers = teachers;
	}

	public TeacherClassAssignmentInput() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void addMoreClass(int sz){
		ArrayList<Class> L = new ArrayList<Class>();
		for(int i = 0; i < classes.length; i++)
			L.add(classes[i]);
		
		Random R = new Random();
		
		for(int i = 0; i < sz; i++){
			int idx = R.nextInt(classes.length);
			Class cls = classes[idx].clone();
			L.add(cls);
		}
		classes = new Class[L.size()];
		for(int i = 0; i < L.size(); i++)
			classes[i] = L.get(i);
	}
}
