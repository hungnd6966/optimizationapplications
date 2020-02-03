package com.hust.opt.optapp.teacherclassassignment.service.opencbls;

import localsearch.model.ConstraintSystem;
import localsearch.model.VarIntLS;

public class BestSolution {

	private int[] X;
	private int[] nbCredits;
	private int violation;

	public BestSolution(VarIntLS[] x, VarIntLS[] sumCredits, ConstraintSystem s) {
		this.violation = s.violations();
		int n = x.length;
		X = new int[n];
		for (int i = 0; i < n; i++) {
			X[i] = x[i].getValue();
		}
		int M = sumCredits.length;
		this.nbCredits = new int[M];
		for (int i=0; i<M; i++) {
			this.nbCredits[i] = sumCredits[i].getValue();
		}
	}

	public int getViolation() {
		return violation;
	}

	public int getX(int index) {
		return X[index];
	}
	
	public int getNumberCredits(int index) {
		return nbCredits[index];
	}
	
	public void update(VarIntLS[] x, VarIntLS[] sumCredits, ConstraintSystem s) {
		if (s.violations() < this.violation) {
			violation = s.violations();
			
			int n = x.length;
			for (int i = 0; i < n; i++) {
				X[i] = x[i].getValue();
			}
			
			int m = sumCredits.length;
			for (int i=0; i<m; i++) {
				this.nbCredits[i] = sumCredits[i].getValue();
			}
		}
	}
}
