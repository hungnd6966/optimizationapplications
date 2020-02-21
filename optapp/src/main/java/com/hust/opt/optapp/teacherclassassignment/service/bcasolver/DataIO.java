package com.hust.opt.optapp.teacherclassassignment.service.bcasolver;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class DataIO {
	
	// input
	private int N;
	private int M; 
	private int[] credit; 
	private ArrayList<Integer>[] D; 
	private ArrayList<Integer>[] P; 
	private int[][] priority;
	private int[][] conflict;
	
	// output
	private int[] X;
	private int[] nbCredits;
	private int fmax;
	private int fmin;
	private int standardDeviation;
	private int fmaxTeacher;
	private int fminTeacher;
	
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
			priority = new int[N][M];
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < D[i].size(); j++) {
					priority[i][D[i].get(j)] = P[i].get(j);
				}
			}
			int K = in.nextInt();
			conflict = new int[K][2];
			for (int k = 0; k < K; k++) {
				conflict[k][0] = in.nextInt();
				conflict[k][1] = in.nextInt();
			}
			in.close();
			
			X = new int[N];
			nbCredits = new int[M];
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		try {
			PrintWriter out = new PrintWriter("data/output/domainOfTeachers.txt");
			for (int i=0; i<M; i++) {
				out.print("Teacher " + i + ": ");
				for (int j=0; j<N; j++) {
					if (priority[j][i] > 0) {
						out.print(j + ", ");
					}
				}
				out.println();
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void releaseOutput(String fileoutput) {
		try {
			PrintWriter out = new PrintWriter(fileoutput);
			for (int i = 0; i < M; i++) {
				out.print("Teacher " + i + ", nbCredits = " + nbCredits[i] + ":\t");
				int nCourses = 0;
				for (int j = 0; j < N; j++) {
					if (X[j] == i) {
						out.print(j + ", ");
						nCourses++;
					}
				}
				out.println(); // "nCourses = " + nCourses);
			}

			for (int i = 0; i < N; i++) {
				System.out.println("X[" + i + "] = " + X[i] + ", priority = " + priority[i][X[i]]);
			}
			out.println();
			for(int i=0; i<M; i++) {
				out.print("Teacher " + i + ": S = " + nbCredits[i] + "\t|");
				for (int j=0; j<nbCredits[i]; j++) {
					out.print("*");
				}
				out.println();
			}
			out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public int getObj() {
		int avg = 0;
		for (int i=0; i<M; i++) {
			avg += nbCredits[i];
		}
		avg /= M;
		
		int res = 0;
		for (int i=0; i<M; i++) {
			res += Math.pow(nbCredits[i] - avg, 2);
		}
		return res/M;
	}

	public int getN() {
		return N;
	}

	public void setN(int n) {
		N = n;
	}

	public int getM() {
		return M;
	}

	public void setM(int m) {
		M = m;
	}

	public int[] getCredit() {
		return credit;
	}

	public void setCredit(int[] credit) {
		this.credit = credit;
	}

	public ArrayList<Integer>[] getD() {
		return D;
	}

	public void setD(ArrayList<Integer>[] d) {
		D = d;
	}

	public ArrayList<Integer>[] getP() {
		return P;
	}

	public void setP(ArrayList<Integer>[] p) {
		P = p;
	}

	public int[][] getPriority() {
		return priority;
	}

	public void setP(int[][] p) {
		this.priority = p;
	}

	public int[][] getConflict() {
		return conflict;
	}

	public void setConflict(int[][] conflict) {
		this.conflict = conflict;
	}

	public int[] getX() {
		return X;
	}
	
	public int getX(int index) {
		return X[index];
	}

	public void setX(int[] x) {
		X = x;
	}

	public int[] getNbCredits() {
		return nbCredits;
	}
	
	public int  getNbCredits(int index) {
		return nbCredits[index];
	}

	public void setNbCredits(int[] nbCredits) {
		this.nbCredits = nbCredits;
		int eqMax = 0;
		int eqMin = 0;
		for (int i=0; i<M; i++) {
			if (this.nbCredits[i] == fmin) {
				eqMin ++;
			}
			if (this.nbCredits[i] == fmax) {
				eqMax ++;
			}
		}
		this.setFmaxTeacher(eqMax);
		this.setFminTeacher(eqMin);
	}

	public int getFmax() {
		return fmax;
	}

	public void setFmax(int fmax) {
		this.fmax = fmax;
	}

	public int getFmin() {
		return fmin;
	}

	public void setFmin(int fmin) {
		this.fmin = fmin;
	}

	public int getStandardDeviation() {
		return standardDeviation;
	}

	public void setStandardDeviation(int standardDeviation) {
		this.standardDeviation = standardDeviation;
	}

	public int getFmaxTeacher() {
		return fmaxTeacher;
	}

	public void setFmaxTeacher(int fmaxTeacher) {
		this.fmaxTeacher = fmaxTeacher;
	}

	public int getFminTeacher() {
		return fminTeacher;
	}

	public void setFminTeacher(int fminTeacher) {
		this.fminTeacher = fminTeacher;
	}

}
