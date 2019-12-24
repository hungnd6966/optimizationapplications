package com.hust.opt.optapp.teacherclassassignment.model;

public class TeacherClassAssignmentElement {
	private Teacher teacher;
	private Class[] assignedClasses;

	public Teacher getTeacher() {
		return teacher;
	}

	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}

	public Class[] getAssignedClasses() {
		return assignedClasses;
	}

	public void setAssignedClasses(Class[] assignedClasses) {
		this.assignedClasses = assignedClasses;
	}

	public TeacherClassAssignmentElement(Teacher teacher,
			Class[] assignedClasses) {
		super();
		this.teacher = teacher;
		this.assignedClasses = assignedClasses;
	}

	public TeacherClassAssignmentElement() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
