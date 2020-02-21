package com.hust.opt.optapp.teacherclassassignment.service.bcasolver.solver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.hust.opt.optapp.teacherclassassignment.service.bcasolver.DataIO;
import com.hust.opt.optapp.teacherclassassignment.service.opencbls.Move;

import localsearch.constraints.basic.LessOrEqual;
import localsearch.constraints.basic.NotEqual;
import localsearch.functions.conditionalsum.ConditionalSum;
import localsearch.functions.standardDeviation.StandardFloatDeviation;
import localsearch.model.ConstraintSystem;
import localsearch.model.IConstraint;
import localsearch.model.IFloatFunction;
import localsearch.model.IFunction;
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;

public class OpenCBLSSolverPhaseThree implements Solver {

	private DataIO io;

	private LocalSearchManager mgr;
	private ConstraintSystem S;
	private VarIntLS[] X;
	private IFunction[] nbCredits;
	private IFloatFunction objFunc;

	private Random r;
	private ArrayList<Move> move;
	private long startTime;
	private double bestF;

	public OpenCBLSSolverPhaseThree(DataIO io, long t0) {
		super();
		this.startTime = t0;
		this.io = io;
		r = new Random();
	}

	private void stateModel() {
		mgr = new LocalSearchManager();
		X = new VarIntLS[io.getN()];
		for (int i = 0; i < io.getN(); i++) {
			Set<Integer> Di = new HashSet<Integer>();
			for (int v : io.getD()[i])
				Di.add(v);
			X[i] = new VarIntLS(mgr, Di);
		}

		S = new ConstraintSystem(mgr);
		
		for (int i=0; i<io.getN(); i++) {
			for (int j=0; j<io.getM(); j++) {
				if (io.getPriority()[i][j]==0) {
					S.post(new NotEqual(X[i], j));
				}
			}
		}

		for (int[] t : io.getConflict()) {
			S.post(new NotEqual(X[t[0]], X[t[1]]));
		}

		nbCredits = new IFunction[io.getM()];
		for (int i = 0; i < io.getM(); i++) {
			nbCredits[i] = new ConditionalSum(X, io.getCredit(), i);
			S.post(new LessOrEqual(nbCredits[i], io.getFmax()));
			S.post(new LessOrEqual(io.getFmin(), nbCredits[i]));
		}

		 objFunc = new StandardFloatDeviation(nbCredits);
		 for (int i=0; i<io.getN(); i++) {
			 X[i].setValuePropagate(io.getX(i));
		 }
		mgr.close();
	}
	
	private void oneMoveSearch(int tabulen, int maxTime, int maxIter, int maxStable, double perturbedRate) {
		maxTime *= 1000;
		int count = 0;
		int nic = 0;
		int[][] tabu = new int[io.getN()][io.getM()];
		move = new ArrayList<Move>();
		bestF = objFunc.getValue();
		
		while (count++ < maxIter && (System.currentTimeMillis() - startTime < maxTime)) {
			move.clear();
			int c = r.nextInt(10) % 2;
			double minDeltaF = 9999999;
			
			switch (c) {
			case 0: {
				int nei = 0;
				while (nei++ < 300) {
					int i = r.nextInt(io.getN());
					if (io.getD()[i].size() == 1)
						continue;
					for (int j : X[i].getDomain()) {
						nei ++;
						if (S.getAssignDelta(X[i], j)<=0) {
							double deltaF = objFunc.getAssignDelta(X[i], j);
							if (deltaF + objFunc.getValue() <= objFunc.getValue() || tabu[i][j] <= count) {
								if (deltaF == minDeltaF) {
									move.add(new Move(i, j));
								} else if (deltaF < minDeltaF) {
									minDeltaF = deltaF;
									move.clear();
									move.add(new Move(i, j));
								}
							}
						}
					}
				}
				int size = move.size();
				if (size > 0) {
					Move m = move.get(r.nextInt(size));
					int i = m.x, j = m.y;
					X[i].setValuePropagate(j);
					tabu[i][j] = count + tabulen;
				}
				break;
			}
			case 1: {
				int nei = 0;
				while (nei++ < 1000) {
					int i1 = r.nextInt(io.getN());
					int i2 = r.nextInt(io.getN());
					if (i1 != i2 && io.getD()[i1].size() > 1 && io.getD()[i2].size() > 1 && X[i1].getDomain().contains(X[i2].getValue())
							&& X[i2].getDomain().contains(X[i1].getValue())) {
						if (S.getSwapDelta(X[i1], X[i2]) <= 0) {
							double deltaF = objFunc.getSwapDelta(X[i1], X[i2]);
							if (deltaF + objFunc.getValue() < objFunc.getValue() || 
									(tabu[i1][X[i2].getValue()] <= count && tabu[i2][X[i1].getValue()] <= count)) {
								if (deltaF == minDeltaF) {
									move.add(new Move(i1, i2));
								} else if (deltaF < minDeltaF) {
									minDeltaF = deltaF;
									move.clear();
									move.add(new Move(i1, i2));
								}
							}
						}
					}
				}
				int size = move.size();
				if (size > 0) {
					Move m = move.get(r.nextInt(size));
					int i1 = m.x, i2 = m.y;
					int v1 = X[i1].getValue();
					int v2 = X[i2].getValue();
					tabu[i2][v1] = count + tabulen;
					tabu[i1][v2] = count + tabulen;
					X[i1].setValuePropagate(v2);
					X[i2].setValuePropagate(v1);
				}
				break;
			}
			}
			if (bestF > objFunc.getValue()) {
				nic = 0;
				bestF = objFunc.getValue();
				System.out.println("run time" + (System.currentTimeMillis() - startTime));
				System.out.println("Phase 04 - Minimize standard deviation, Step " + count + ", case " + c + ": bestF = "
						+ Math.sqrt(bestF) + ", f = " + objFunc.getValue() + ", delta = " + minDeltaF + ", nic = " + nic);
			} else {
				nic++;
			}

			if (nic >= maxStable) {
				nic = 0;
				this.restartMaintainConstraint(X, S, tabu);
			}
		}
	}
	
	
	private void restartMaintainConstraint(VarIntLS[] x, IConstraint S,
			int[][] tabu) {

		for (int i = 0; i < x.length; i++) {
			java.util.ArrayList<Integer> L = new java.util.ArrayList<Integer>();
			for (int v = x[i].getMinValue(); v <= x[i].getMaxValue(); v++) {
				if (S.getAssignDelta(x[i], v) <= 0)
					L.add(v);
			}
			
			int idx = r.nextInt(L.size());
			int v = L.get(idx);
			x[i].setValuePropagate(v);
			
		}
		for (int i = 0; i < tabu.length; i++) {
			for (int j = 0; j < tabu[i].length; j++)
				tabu[i][j] = -1;
		}

	}

	@Override
	public void solve() {
		this.stateModel();
		
		this.oneMoveSearch(100, 900, 100000000, 300, 0.1);
		int[][] conflict = new int[io.getN()][io.getN()];
			for (int i=0; i<io.getConflict().length; i++) {
				int c1 = io.getConflict()[i][0];
				int c2 = io.getConflict()[i][1];
				conflict[c1][c2] = 1;
				conflict[c2][c1] = 1;
			}
		int count = 0;
		for (int i=0; i<io.getN(); i++) {
			if (!io.getD()[i].contains(X[i].getValue())) {
				System.out.println("Vital wrong in soluiton: class " + i + " can not be assigned to teacher " + X[i].getValue());
				count ++;
			}
			
			
			for (int j=0; j<io.getN(); j++) {
				if (X[i].getValue() == X[j].getValue() && conflict[i][j] ==1) {
					System.out.println("Vital wrong in soluiton: class " + i + "and class j can not be assigned to one teacher");
					count ++;
				}
			}
		}	
		if (count==0) {
			System.out.println("Check solution result: Free error");
		} else {
			System.exit(-1);
		}
		System.out.println(Math.sqrt(bestF));
	}

	@Override
	public void releaseSolution() throws Exception {
		int solution[] = new int[io.getN()];
		for (int i=0; i<io.getN(); i++) {
			solution[i] = X[i].getValue();
		}
		io.setX(solution);
		
		int[] credit = new int[io.getM()];
		for(int i=0; i<io.getM(); i++) {
			credit[i] = nbCredits[i].getValue();
		}
		io.setNbCredits(credit);
	}
}