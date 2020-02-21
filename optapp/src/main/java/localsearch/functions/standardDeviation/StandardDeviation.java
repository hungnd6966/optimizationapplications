package localsearch.functions.standardDeviation;

import java.util.HashSet;

import localsearch.functions.basic.FuncVarConst;
import localsearch.model.AbstractInvariant;
import localsearch.model.IFunction;
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;

public class StandardDeviation extends AbstractInvariant implements IFunction {
	
	private int _minValue;
	private int _maxValue;
	private int _value;
	private LocalSearchManager _ls;
	private IFunction[] _f;
	private HashSet<VarIntLS> _x;
	
	public StandardDeviation(IFunction[] f) {
		_f = f;
		_ls = f[0].getLocalSearchManager();
		post();
	}
	
	public StandardDeviation(VarIntLS[] x) {
		int n = x.length;
		_f = new IFunction[n];
		for (int i=0; i<n; i++) {
			_f[i] = new FuncVarConst(x[i]);
		}
		_ls = x[0].getLocalSearchManager();
		post();
	}
	
	private void post() {
		_x = new HashSet<VarIntLS>();
		for(int i=0; i<_f.length; i++) {
			VarIntLS[] vars = _f[i].getVariables();
			if (vars!=null) {
				for (VarIntLS v: vars) {
					_x.add(v);
				}
			}
		}
		_ls.post(this);
	}

	@Override
	public int getMinValue() {
		// TODO Auto-generated method stub
		return _minValue;
	}

	@Override
	public int getMaxValue() {
		// TODO Auto-generated method stub
		return _maxValue;
	}

	@Override
	public int getValue() {
		// TODO Auto-generated method stub
		return _value;
	}

	@Override
	public int getAssignDelta(VarIntLS x, int val) {
		if (!_x.contains(x)) return 0;
		
		int sumf = 0;
		for(int i=0; i<_f.length; i++) {
			sumf += _f[i].getAssignDelta(x, val) + _f[i].getValue();
		}
		int standard = sumf/_f.length;
		
		int sqDeviation = 0;
		for(int i=0; i<_f.length; i++) {
			sqDeviation += Math.pow(_f[i].getAssignDelta(x, val) + _f[i].getValue() - standard, 2);
		}
		return sqDeviation/_f.length - _value;
	}

	@Override
	public int getSwapDelta(VarIntLS x, VarIntLS y) {
		if (!_x.contains(x) && !_x.contains(y)) return 0;
		if (!_x.contains(x) && _x.contains(y)) return this.getAssignDelta(y, x.getValue());
		if (_x.contains(x) && !_x.contains(y)) return this.getAssignDelta(x, y.getValue());
		
		int sumf = 0;
		for(int i=0; i<_f.length; i++) {
			sumf += _f[i].getSwapDelta(x, y) + _f[i].getValue();
		}
		int standard = sumf/_f.length;
		
		int sqDeviation = 0;
		for(int i=0; i<_f.length; i++) {
			sqDeviation += Math.pow(_f[i].getSwapDelta(x, y) + _f[i].getValue() - standard, 2);
		}
		
		return sqDeviation/_f.length - _value;
	}
	
	@Override
	public void propagateInt(VarIntLS x, int val) {
		if (!_x.contains(x)) return;
		_value = _value + this.getAssignDelta(x, val);
	}

	public String name(){
		return "StandardDeviation";
	}
	
	@Override
	public void initPropagate() {
		int sumf = 0;
		for(int i=0; i<_f.length; i++) {
			sumf += _f[i].getValue();
		}
		int standard = sumf/_f.length;
		
		int sqDeviation = 0;
		for(int i=0; i<_f.length; i++) {
			sqDeviation += Math.pow(_f[i].getValue() - standard, 2);
		}
		
		_value = sqDeviation/_f.length;
	}

	@Override
	public VarIntLS[] getVariables() {
		VarIntLS[] x = new VarIntLS[_x.size()];
		int idx=0;
		for (VarIntLS v: _x) {
			x[idx++] = v;
		}
		return x;
	}

	@Override
	public LocalSearchManager getLocalSearchManager() {
		// TODO Auto-generated method stub
		return _ls;
	}
	
	@Override
	public boolean verify() {
		return true;
	}
	
}
