package com.hust.opt.optapp.teacherclassassignment.model;

public class CourseForTeacher extends Course {
    private int priority;


	public CourseForTeacher() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CourseForTeacher(String code, String name, String type) {
		super(code, name, type);
		// TODO Auto-generated constructor stub
	}

	public CourseForTeacher(int priority) {
		super();
		this.priority = priority;
	}

	public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
