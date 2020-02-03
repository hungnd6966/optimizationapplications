package com.hust.opt.optapp.teacherclassassignment.service.opencbls;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import localsearch.constraints.basic.Implicate;
import localsearch.constraints.basic.IsEqual;
import localsearch.constraints.basic.NotEqual;
import localsearch.functions.basic.FuncMinus;
import localsearch.functions.basic.FuncMult;
import localsearch.functions.basic.FuncPlus;
import localsearch.functions.conditionalsum.ConditionalSum;
import localsearch.functions.max_min.Max;
import localsearch.functions.sum.Sum;
import localsearch.model.ConstraintSystem;
import localsearch.model.IConstraint;
import localsearch.model.IFunction;
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;
import localsearch.search.TabuSearch;

public class OpenCBLSSolver {

	private int N;// number of classes
	private int M;// number of teachers;
	private int[] credit; // credit[i] is the number of credits of class i
	private ArrayList<Integer>[] D;// D[i] is the list of teachers for class i
	private ArrayList<Integer>[] P;// P[i] is the list of corresponding priority for teacher
	private int[][] p;
	private int[][] conflict;// (conflict[k][0], conflict[k][1]) is a pair of conflicting classes (cannot
								// assigned to the same teacher)
	private final int MAX_ASSIGNMENT = 25;
	private final int EXPECTED = 13;

	// modelling
	private LocalSearchManager mgr;
	private ConstraintSystem S;
	private VarIntLS[] X;
	private VarIntLS[] nbCredits;
	private VarIntLS[] priority;
	private VarIntLS[] sqDelta;
	private VarIntLS objMaxAssignment;
	private IFunction objDeltaAssignment;
	private IFunction objPriority;

	private BestSolution best;
	private ArrayList<Move> move;
	private Random r;

	public void loadData(String filename) {
		try {
			Scanner in = new Scanner(new File(filename));
			N = in.nextInt();
			M = in.nextInt();
			D = new ArrayList[N];
			P = new ArrayList[N];
			credit = new int[N];
			for (int i = 0; i < N; i++) {
				D[i] = new ArrayList<Integer>();
				P[i] = new ArrayList<Integer>();
			}
			for (int i = 0; i < N; i++) {
				int id = in.nextInt();
				credit[id] = in.nextInt();
				int k = in.nextInt();
				for (int j = 0; j < k; j++) {
					int t = in.nextInt();
					int p = 1;//in.nextInt();
					D[id].add(t);
					P[id].add(p);
				}
			}
			p = new int[N][M];
			for (int i=0; i<N; i++) {
				for(int j=0; j<D[i].size(); j++) {
					p[i][D[i].get(j)] = P[i].get(j);
				}
			}
			int K = in.nextInt();
			conflict = new int[K][2];
			for (int k = 0; k < K; k++) {
				conflict[k][0] = in.nextInt();
				conflict[k][1] = in.nextInt();
			}
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void printSolution() {
		for (int i=0; i< M; i++) {
			System.out.print("Teacher " + i + ", nbCredits = " + best.getNumberCredits(i) + ":\t");
			int nCourses = 0;
			for(int j=0; j<N; j++) {
				if (best.getX(j) == i) {
					System.out.print(j + ", ");
					nCourses ++;
				}
			}
			System.out.println(); //"nCourses = " + nCourses);
		}
		
		for(int i=0; i<N; i++) {
			int j = best.getX(i);
			System.out.println("X[" + i + "] = " + j + ", priority = " + p[i][j]);
		}
		System.out.println("S = " + best.getViolation());
	}

	public void stateFeasibleModel() {
		mgr = new LocalSearchManager();
		X = new VarIntLS[N];
		for (int i = 0; i < N; i++) {
			Set<Integer> Di = new HashSet<Integer>();
			for (int v : D[i])
				Di.add(v);
			X[i] = new VarIntLS(mgr, Di);
		}

		S = new ConstraintSystem(mgr);
		
		for (int[] t : conflict) {
			S.post(new NotEqual(X[t[0]], X[t[1]]));
		}

		nbCredits = new VarIntLS[M];
		for (int i = 0; i < M; i++) {
			nbCredits[i] = new VarIntLS(mgr, 0, MAX_ASSIGNMENT);
			S.post(new IsEqual(new FuncPlus(nbCredits[i], 0), new ConditionalSum(X, credit, i)));
		}

		objMaxAssignment = new VarIntLS(mgr, 0, MAX_ASSIGNMENT);
		S.post(new IsEqual(new Max(nbCredits), objMaxAssignment));
		
		mgr.close();
	}

	public void searchAFeasibleSolution(int tabulen, int maxTime, int maxIter, int maxStable) {
		maxTime *= 1000;
		int count = 0;
		int nic = 0;
		int[][] tabu = new int[N][M];
		long startTime = System.currentTimeMillis();
		move = new ArrayList<Move>();
		r = new Random();
		for (int i=0; i<N; i++) {
			if (D[i].size()==1) {
				X[i].setValuePropagate(D[i].get(0).intValue());
			}
		}
		VarIntLS[] x = S.getVariables();
		best = new BestSolution(X, nbCredits, S);
		int bestV = S.violations();
		int init = bestV;
		System.out.println("Initial S: " + init);
		init /= 10;

		while (count++ < maxIter && (System.currentTimeMillis() - startTime < maxTime) && S.violations() > 0) {
			move.clear();
			int preVio = S.violations();
			int minDelta = (int) 1e6;
			int c;
			if (bestV > init) { 
				c = 0;
			} else {
				c = r.nextInt(10) % 2;
			}
			
			switch (c) {
			case 0: {
				int n = S.getVariables().length;
				for (int i = 0; i < n; i++) {
					int idx = x[i].getID();
					if (idx<N && D[idx].size()==1) continue;
					for (int j : x[i].getDomain()) {
						int delta = S.getAssignDelta(x[i], j);
						if (bestV > delta + S.violations() || idx >= N || 
								(idx<N && tabu[idx][j]<=count)) {
							if (delta == minDelta) {
								move.add(new Move(i, j));
							} else if (delta < minDelta) {
								minDelta = delta;
								move.clear();
								move.add(new Move(i, j));
							}
						}
					}
				}
				int size = move.size();
				if (size > 0) {
					Move m = move.get(r.nextInt(size));
					int i = m.x, j = m.y;
					x[i].setValuePropagate(j);
					if (x[i].getID()<N) {
						tabu[x[i].getID()][j] = count + tabulen;
					} 
				}
				break;
			}
			case 1: {
				int nei = 0;
				while (nei++ < 20000) {
					int i1 = r.nextInt(N);
					int i2 = r.nextInt(N);
					if (i1 != i2 && D[i1].size()>1 && D[i2].size()>1 && 
							X[i1].getDomain().contains(X[i2].getValue()) && X[i2].getDomain().contains(X[i1].getValue())) {
						int delta = S.getSwapDelta(X[i1], X[i2]);
						if (best.getViolation() > delta + S.violations() ||
								(tabu[i1][X[i2].getValue()]<=count && tabu[i2][X[i1].getValue()]<=count)) {
							if (delta == minDelta) {
								move.add(new Move(i1, i2));
							} else if (delta < minDelta) {
								minDelta = delta;
								move.clear();
								move.add(new Move(i1, i2));
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
			if (bestV > S.violations()) {
				nic = 0;
				bestV = S.violations();
				best.update(X, nbCredits, S);
			} else {
				if (bestV <= 10 && bestV == S.violations()) best.update(X, nbCredits, S);
				nic ++;
			}
			System.out.println("Searching for a feasible solution, Case " + c + ", Step " + count + ": best = " + best.getViolation() + ", S = " + preVio + ", delta = "
					+ minDelta + ", nic = " + nic);
			/*
			if (count%2000==0) {
				this.printSolution();
			}
			*/
			if (nic>=maxStable) {
				nic = 0;
				System.out.println("\n__________________________________________________________________________________________\n"
						+ "\tRun time: " + (System.currentTimeMillis() - startTime)/1000 + "(s), Perturbing solution...\n"
							    + "__________________________________________________________________________________________");
				this.perturbingSolution(best, tabu);
				//this.restartMaintainConstraint(x, S, tabu);		
			}
		}
	}
	
	public void stateOptimizationModel() {
		mgr = new LocalSearchManager();
		VarIntLS[] tmpX = X;
		X = new VarIntLS[N];
		for (int i=0; i<N; i++) {
			X[i] = new VarIntLS(mgr, tmpX[i].getDomain());
		}
		S = new ConstraintSystem(mgr);
		
		for (int[] t : conflict) {
			S.post(new NotEqual(X[t[0]], X[t[1]]));
		}

		nbCredits = new VarIntLS[M];
		for (int i = 0; i < M; i++) {
			nbCredits[i] = new VarIntLS(mgr, 0, MAX_ASSIGNMENT);
			S.post(new IsEqual(new FuncPlus(nbCredits[i], 0), new ConditionalSum(X, credit, i)));
		}

		objMaxAssignment = new VarIntLS(mgr, 0, MAX_ASSIGNMENT);
		S.post(new IsEqual(new Max(nbCredits), objMaxAssignment));
		
		sqDelta = new VarIntLS[M];
		int tmp = Math.max(EXPECTED, MAX_ASSIGNMENT-EXPECTED);
		for (int i = 0; i < M; i++) {
			sqDelta[i] = new VarIntLS(mgr, 0, tmp*tmp);
			S.post(new IsEqual(new FuncPlus(sqDelta[i], 0), 
					new FuncMult(new FuncMinus(nbCredits[i], EXPECTED), new FuncMinus(nbCredits[i], EXPECTED))));
		}
		objDeltaAssignment = new Sum(sqDelta);
		
		priority = new VarIntLS[N];
		for (int i = 0; i < N; i++) {
			priority[i] = new VarIntLS(mgr, 1, 2);
			for (int j = 0; j < D[i].size(); j++) {
				int t = D[i].get(j).intValue();
				S.post(new Implicate(new IsEqual(X[i], t), new IsEqual(priority[i], P[i].get(j).intValue())));
			}
		}
		objPriority = new Sum(priority);
		
		mgr.close();
		
		this.mergeTwoModels();
	}
	
	public void mergeTwoModels() {			
		for (int i=0; i<N; i++) {
			int j = best.getX(i);
			X[i].setValuePropagate(j);
			priority[i].setValuePropagate(p[i][j]);
		}
		for (int i=0; i<M; i++) {
			int t = best.getNumberCredits(i);
			nbCredits[i].setValuePropagate(t);
			sqDelta[i].setValuePropagate((t-EXPECTED)*(t-EXPECTED));
		}
		
		int bestV = S.violations();
		System.out.println("S: " + bestV);
		int count = 0;
		while (S.violations() > best.getViolation()) {
			move.clear();
			int minDelta = 10000000;
			int n = S.getVariables().length;
			VarIntLS[] x = S.getVariables();
			for (int i = 0; i < n; i++) {
				if (x[i].getID()<N) continue;
				for (int j: x[i].getDomain()) {
					int delta = S.getAssignDelta(x[i], j);
					if (bestV > delta + S.violations()) {
						if (delta == minDelta) {
							move.add(new Move(i, j));
						} else if (delta < minDelta) {
							minDelta = delta;
							move.clear();
							move.add(new Move(i, j));
						}
					}
				}
			}
			int size = move.size();
			if (size > 0) {
				Move m = move.get(r.nextInt(size));
				int i = m.x, j = m.y;
				x[i].setValuePropagate(j);
			}
			System.out.println("Merging two models, Step " + count++ + ": S = " + S.violations() + ", delta = " + minDelta);
		}
	}
	
	public void optimizeSolution(int tabulen, int maxTime, int maxIter, int maxStable) {
		
	}

	public void tabuSearch(IFunction[] obj, int[] weight) {
		TabuSearch ts = new TabuSearch();
		//ts.search(S, 200, 300, 10000, 50);
		ts.searchMaintainConstraintsMinimize(new FuncPlus(objMaxAssignment, 0), S, 100, 100, 10000, 50);
		ts.searchMaintainConstraintsMinimize(new FuncPlus(objDeltaAssignment, 0), S, 100, 100, 10000, 50);
		ts.searchMaintainConstraintsMaximize(new FuncPlus(objPriority, 0), S, 100, 100, 300, 50);
	}
	
	public void perturbingSolution(BestSolution best, int[][] tabu) {
		int num = 25;
		for (int i=0; i<N; i++) {
			X[i].setValuePropagate(best.getX(i));
		}
		
		for (int k=0; k<num; k++) {
			int i = r.nextInt(N);
			int j = r.nextInt(D[i].size());
			X[i].setValuePropagate(D[i].get(j).intValue());
		}
		
		/*
		while (num>0) {
			int i1 = r.nextInt(N);
			int i2 = r.nextInt(N);
			if (i1 != i2 && D[i1].size()>1 && D[i2].size()>1) {
				num--;
				int v1 = X[i1].getValue();
				int v2 = X[i2].getValue();
				X[i1].setValuePropagate(v2);
				X[i2].setValuePropagate(v1);
			}
		}
		
		for (int i=0; i<N; i++) {
			for (int j=0; j<M; j++) {
				tabu[i][j] = -1;
			}
		}
		*/
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		OpenCBLSSolver s = new OpenCBLSSolver();
		s.loadData("D:\\Documents\\BCA-SOICT\\bca_input.txt");
		//s.loadData("data\\teacherclassassignment\\input_20182_1905_ext_0_format1.txt");
		s.stateFeasibleModel();
		long t0 = System.currentTimeMillis();
		s.searchAFeasibleSolution(200, 1000, 100000, 50);
		s.printSolution();
		//s.stateOptimizationModel();
		//s.optimizeSolution(100, 300, 100000, 50);
		//s.printSolution();
		System.out.println("Run time: " + (System.currentTimeMillis()-t0)/1000 + "(s)");
	}

}
