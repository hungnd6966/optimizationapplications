package com.hust.opt.optapp.teacherclassassignment.service.bcasolver.solver;

import com.hust.opt.optapp.teacherclassassignment.service.bcasolver.DataIO;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;

public class CplexMipSolverPhaseOne extends BasicSolver implements Solver{
	
	private IloNumVar fmax;

	public CplexMipSolverPhaseOne(DataIO io) {
		super();
		this.io = io;
	}

	private void stateModel() throws IloException {
		super.stateBasicModel();
		
		int totalCredits = 0;
		for(int i=0; i<io.getN(); i++) {
			totalCredits += io.getCredit()[i];
		}
		fmax = model.intVar(0, totalCredits);
		for (int i = 0; i < io.getM(); i++) {
			model.addLe(nbCredits[i], fmax);
		}
		
		model.addMinimize(fmax);
	}

	@Override
	public void solve() throws Exception {
		this.stateModel();
		if (model.solve()) {
			this.checkSolution(model, x);
			System.out.println("Solution found by phase 1: " + model.getStatus());
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
		
		io.setFmax((int) Math.ceil(model.getValue(fmax)));
	}

}
