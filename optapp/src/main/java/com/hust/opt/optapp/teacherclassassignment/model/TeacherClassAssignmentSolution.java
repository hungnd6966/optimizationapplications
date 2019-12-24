package com.hust.opt.optapp.teacherclassassignment.model;

public class TeacherClassAssignmentSolution {
	private TeacherClassAssignmentElement[] assignments;

	public TeacherClassAssignmentElement[] getAssignments() {
		return assignments;
	}

	public void setAssignments(TeacherClassAssignmentElement[] assignments) {
		this.assignments = assignments;
	}

	public TeacherClassAssignmentSolution(
			TeacherClassAssignmentElement[] assignments) {
		super();
		this.assignments = assignments;
	}

	public TeacherClassAssignmentSolution() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
