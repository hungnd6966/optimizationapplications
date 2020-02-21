package com.hust.opt.optapp.teacherclassassignment.service.bcasolver.solver;

import com.hust.opt.optapp.teacherclassassignment.service.bcasolver.DataIO;

import ilog.concert.IloNumVar;

public class CplexMipSolverPhaseTwo extends BasicSolver implements Solver{
	
	private IloNumVar fmin;

	public CplexMipSolverPhaseTwo(DataIO io) {
		super();
		this.io = io;
	}

	public void stateModel() throws Exception {
		super.stateBasicModel();
		
		int totalCredits = 0;
		for(int i=0; i<io.getN(); i++) {
			totalCredits += io.getCredit()[i];
		}
		fmin = model.intVar(0, totalCredits);
		for (int i = 0; i < io.getM(); i++) {
			model.addGe(nbCredits[i], fmin);
			model.addLe(nbCredits[i], io.getFmax());
		}
		model.addMaximize(fmin);
	}
	
	public void solve() throws Exception {
		this.stateModel();
		if (model.solve()) {
			this.checkSolution(model, x);
			System.out.println("Solution found by phase 2: " + model.getStatus());
			this.releaseSolution();
		} else {
			System.out.println("Non solution");
		}
	}

	@Override
	public void releaseSolution() throws Exception {
		int solution[] = new int[io.getN()];
		for (int i=0; i<io.getN(); i++) {
			for(int j=0; j<io.getM(); j++) {
				if (model.getValue(x[i][j])>0) {
					solution[i] = j;
				}
			}
		}
		io.setX(solution);
		
		int[] credit = new int[io.getM()];
		for(int i=0; i<io.getM(); i++) {
			credit[i] = (int) Math.ceil(model.getValue(nbCredits[i]));
		}
		io.setNbCredits(credit);
		
		io.setFmin((int) Math.ceil(model.getValue(fmin)));
	}

}
