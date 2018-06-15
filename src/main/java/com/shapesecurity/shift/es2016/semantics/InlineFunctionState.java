package com.shapesecurity.shift.es2016.semantics;

import com.shapesecurity.shift.es2016.semantics.asg.BreakTarget;
import com.shapesecurity.shift.es2016.semantics.asg.LocalReference;

public class InlineFunctionState {
	private final LocalReference returnVar;
	private final BreakTarget endOfFunction;

	public InlineFunctionState(LocalReference returnVar, BreakTarget endOfFunction) {
		this.returnVar = returnVar;
		this.endOfFunction = endOfFunction;
	}

	public LocalReference getReturnVar() {
		return returnVar;
	}

	public BreakTarget getEndOfFunction() {
		return endOfFunction;
	}
}
