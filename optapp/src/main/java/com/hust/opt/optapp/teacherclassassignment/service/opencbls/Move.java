package com.hust.opt.optapp.teacherclassassignment.service.opencbls;

import java.util.Comparator;

public class Move {
	public int x;
	public int y;

	public Move(int x, int y) {
		this.x = x;
		this.y = y;
	}

}

class MoveComparator implements Comparator<Move>{
	@Override
	public int compare(Move m1, Move m2) {
		// TODO Auto-generated method stub
		return (new Integer(m1.y)).compareTo(m2.y);
	}
}