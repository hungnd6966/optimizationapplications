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
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;
import localsearch.search.TabuSearch;

public class Test {

	private int N;// number of classes
	private int M;// number of teachers;
	private int[] credit; // credit[i] is the number of credits of class i
	private ArrayList<Integer>[] D;// D[i] is the list of teachers for class i
	private ArrayList<Integer>[] P;// P[i] is the list of corresponding priority for teacher
	private int[][] conflict;// (conflict[k][0], conflict[k][1]) is a pair of conflicting classes (cannot
								// assigned to the same teacher)
	private final int MAX_ASSIGNMENT = 27;
	private final int EXPECTED = 13;

	// modelling
	private LocalSearchManager mgr;
	private ConstraintSystem S;
	private VarIntLS[] X;
	private VarIntLS[] sumCredits;
	private VarIntLS[] priority;
	private VarIntLS objMaxAssignment;
	private VarIntLS objDeltaAssignment;
	private VarIntLS objPriority;

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
					int p = in.nextInt();
					D[id].add(t);
					P[id].add(p);
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
		int totalCourses = 0;
		int totalCredits = 0;
		int maxCourses = 0;
		int minCourses = Integer.MAX_VALUE;
		int maxCredits = 0;
		int minCredits = Integer.MAX_VALUE;
		for (int t = 0; t < M; t++) {
			int nbCourses = 0;
			int nbCredits = 0;
			System.out.print("teacher " + t + ": ");
			for (int i = 0; i < N; i++)
				if (X[i].getValue() == t) {
					System.out.print(i + " ");
					nbCourses++;
					nbCredits += credit[i];
					totalCourses++;
					totalCredits += credit[i];
				}
			if (maxCourses < nbCourses)
				maxCourses = nbCourses;
			if (minCourses > nbCourses)
				minCourses = nbCourses;
			if (maxCredits < nbCredits)
				maxCredits = nbCredits;
			if (minCredits > nbCredits)
				minCredits = nbCredits;
			System.out.println(", nbCourses = " + nbCourses + ", nbCredits = " + nbCredits);

		}
		System.out.println("nbCourses = " + totalCourses + ", nbTeachers = " + M + ", totalCredits = " + totalCredits
				+ ", maxCourse = " + maxCourses + ", minCourses = " + minCourses + ", maxCredits = " + maxCredits
				+ ", minCredits = " + minCredits);
		for (int i = 0; i < N; i++) {
			System.out.println("X[" + i + "] = " + X[i].getValue());
		}
	}

	public void stateModel() {
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

		sumCredits = new VarIntLS[M];
		for (int i = 0; i < M; i++) {
			sumCredits[i] = new VarIntLS(mgr, 0, MAX_ASSIGNMENT);
			S.post(new IsEqual(new FuncPlus(sumCredits[i], 0), new ConditionalSum(X, credit, i)));
		}

		objMaxAssignment = new VarIntLS(mgr, 0, MAX_ASSIGNMENT);
		S.post(new IsEqual(new Max(sumCredits), objMaxAssignment));
/*
		VarIntLS[] sqDelta = new VarIntLS[M];
		int tmp = Math.max(EXPECTED, MAX_ASSIGNMENT-EXPECTED);
		for (int i = 0; i < M; i++) {
			sqDelta[i] = new VarIntLS(mgr, 0, tmp*tmp);
			S.post(new IsEqual(new FuncPlus(sqDelta[i], 0), 
					new FuncMult(new FuncMinus(sumCredits[i], EXPECTED), new FuncMinus(sumCredits[i], EXPECTED))));
		}
		objDeltaAssignment = new VarIntLS(mgr, 0, 50000);
		S.post(new IsEqual(new FuncPlus(objDeltaAssignment, 0), new Sum(sqDelta)));

		priority = new VarIntLS[N];
		for (int i = 0; i < N; i++) {
			priority[i] = new VarIntLS(mgr, 1, 2);
			for (int j = 0; j < D[i].size(); j++) {
				int t = D[i].get(j).intValue();
				S.post(new Implicate(new IsEqual(X[i], t), new IsEqual(priority[i], P[i].get(j).intValue())));
			}
		}
		objPriority = new VarIntLS(mgr, N, 1000);
		S.post(new IsEqual(new FuncPlus(objPriority, 0), new Sum(priority)));
*/		
		mgr.close();
	}

	public void search(int tabulen, int maxTime, int maxIter, int maxStable) {
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
		best = new BestSolution(X, sumCredits, S);
		int bestV = S.violations();
		int init = bestV;
		System.out.println("Initial S: " + init);
		init /= 20;

		while (count++ < maxIter && (System.currentTimeMillis() - startTime < maxTime) && S.violations() > 0) {
			move.clear();
			int preVio = S.violations();
			int minDelta = (int) 1e6;
			int c;
			if (bestV > init) { 
				c = 0;
			} else {
				c = r.nextInt(10) % 1;
			}
			
			switch (c) {
			case 0: {
				int n = S.getVariables().length;
				VarIntLS[] x = S.getVariables();
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
				for(int i1=0; i1<N; i1++) {
					for(int i2=i1+1; i2<N; i2++) {
						if (X[i1].getDomain().contains(X[i2].getValue()) && X[i2].getDomain().contains(X[i1].getValue()) && 
								D[i1].size()>1 && D[i2].size()>1) {
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
				}
				int size = move.size();
				if (size > 0) {
					Move m = move.get(r.nextInt(size));
					int x = m.x, y = m.y;
					int v1 = X[x].getValue();
					int v2 = X[y].getValue();
					tabu[y][v1] = count + tabulen;
					tabu[x][v2] = count + tabulen;
					X[x].setValuePropagate(v2);
					X[y].setValuePropagate(v1);
				}
				break;
			}
			}
			if (bestV > S.violations()) {
				bestV = S.violations();
				best.update(X, sumCredits, S);
				nic = 0;
			} else {
				if (bestV <= 10 && bestV == S.violations()) best.update(X, sumCredits, S);
				nic ++;
			}
			System.out.println("Searching for a feasible solution, Case " + c + ", Step " + count + ": best = " + best.getViolation() + ", S = " + preVio + ", delta = "
					+ minDelta + ", nic = " + nic);
			if (count%2000==0) {
				this.printSolution();
			}
			if (nic>=maxStable) {
				nic = 0;
				System.out.println("Perturbing solution...");
				//this.perturbingSolution(best, tabu);
			}
		}
	}
	
	public void perturbingSolution(BestSolution best, int[][] tabu) {
		int num = 5;
		for (int i=0; i<N; i++) {
			X[i].setValuePropagate(best.getX(i));
		}
		
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
	}
	
	public void optimizeSolution() {
		
	}

	public void tabuSearch() {
		TabuSearch ts = new TabuSearch();
		ts.search(S, 100, 2000, 10000, 50);
		ts.searchMaintainConstraintsMinimize(new FuncPlus(objMaxAssignment, 0), S, 30, 100, 10000, 50);
		ts.searchMaintainConstraintsMinimize(new FuncPlus(objDeltaAssignment, 0), S, 30, 100, 10000, 50);
		ts.searchMaintainConstraintsMaximize(new FuncPlus(objPriority, 0), S, 30, 100, 300, 50);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Test s = new Test();
		//s.loadData("D:\\Documents\\BCA-SOICT\\bca_input.txt");
		s.loadData("data\\teacherclassassignment\\input_20182_1905_ext_0_format1.txt");
		s.stateModel();
		// s.tabuSearch();
		s.search(100, 10000, 100000, 100);
		s.printSolution();
	}

}
