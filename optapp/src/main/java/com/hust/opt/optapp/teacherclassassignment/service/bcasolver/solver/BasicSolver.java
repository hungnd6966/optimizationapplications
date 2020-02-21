package com.hust.opt.optapp.teacherclassassignment.service.bcasolver.solver;

import com.hust.opt.optapp.teacherclassassignment.service.bcasolver.DataIO;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.cplex.IloCplex;

public abstract class BasicSolver{
	
	protected DataIO io;
	protected Solver solver;
	
	protected IloCplex model;
	protected IloIntVar[][] x;
	protected IloLinearIntExpr[] nbCredits;
	
	public void stateBasicModel() throws IloException {
		model = new IloCplex();
		x = new IloIntVar[io.getN()][io.getM()];
		nbCredits = new IloLinearIntExpr[io.getM()];

		int[][] p = new int[io.getN()][io.getM()];
		for(int i=0; i<io.getN(); i++) {
			for(int j=0; j<io.getD()[i].size(); j++) {
				p[i][io.getD()[i].get(j)] = io.getP()[i].get(j);
			}
		}
		for(int i=0; i<io.getN(); i++) {
			for (int j=0; j<io.getM(); j++) {
				if (p[i][j] == 0) {
					x[i][j] = model.intVar(0, 0, "x[" + i + "][" + j + "]");
				} else {
					x[i][j] = model.intVar(0, 1, "x[" + i + "][" + j + "]");
				}
			}
		}
		
		for (int i=0; i<io.getN(); i++) {
			model.addEq(model.sum(x[i]), 1);
		}
		
		for (int i=0; i<io.getConflict().length; i++) {
			int c1 = io.getConflict()[i][0];
			int c2 = io.getConflict()[i][1];
			for(int j=0; j<io.getM(); j++) {
				model.addLe(model.sum(x[c1][j], x[c2][j]), 1);
			}
		}
		
		for (int i=0; i<io.getM(); i++) {
			nbCredits[i] = model.linearIntExpr();
			for (int j=0; j<io.getN(); j++) {
				nbCredits[i].addTerm(io.getCredit()[j], x[j][i]);
			}
		}
	}
	
	public void checkSolution(IloCplex model, IloIntVar[][] x) throws Exception {
		int count=0;
		for (int i=0; i<io.getN(); i++) {
			for (int j=0; j<io.getM(); j++) {
				if (model.getValue(x[i][j])> 0.5 && !io.getD()[i].contains(j)) {
					System.out.println("subject " + i + " can not be assigned to teacher " + j);
					count ++;
				}
			}
		}
		for (int i=0; i<io.getConflict().length; i++) {
			int c1 = io.getConflict()[i][0];
			int c2 = io.getConflict()[i][1];
			for (int j=0; j<io.getM(); j++) {
				if (model.getValue(x[c1][j]) > 0.5 && model.getValue(x[c2][j]) >0.5) {
					System.out.println("subject " + c2 + " and subject " + c2 + " can not be assigned to one teacher");
					count ++;
				}
			}
		}
		if (count==0) {
			System.out.println("Check solution result: Free error");
		} else {
			System.exit(-1);
		}
	}

}
