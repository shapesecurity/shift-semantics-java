package com.shapesecurity.shift.es2016.semantics;

import com.shapesecurity.functional.data.ImmutableList;
import com.shapesecurity.functional.data.Maybe;
import com.shapesecurity.shift.es2016.scope.Variable;
import com.shapesecurity.shift.es2016.semantics.asg.BreakTarget;
import com.shapesecurity.shift.es2016.semantics.asg.LocalReference;

public class PerFunctionState {
	private ImmutableList<Variable> additionalVariables;
	private ImmutableList<InlineFunctionState> inlineFunctionStates;

	public PerFunctionState() {
		additionalVariables = ImmutableList.empty();
		inlineFunctionStates = ImmutableList.empty();
	}

	public void addVariable(Variable variable) {
		additionalVariables = additionalVariables.cons(variable);
	}

	public ImmutableList<Variable> getAdditionalVariables() {
		return additionalVariables;
	}

	public InlineFunctionState enterInlineFunction(LocalReference returnVar, BreakTarget endOfFunction) {
		InlineFunctionState state = new InlineFunctionState(returnVar, endOfFunction);
		this.addVariable(state.getReturnVar().variable);
		inlineFunctionStates = inlineFunctionStates.cons(state);
		return state;
	}

	public void exitInlineFunction() {
		inlineFunctionStates = inlineFunctionStates.maybeTail().fromJust();
	}

	public Maybe<InlineFunctionState> getCurrentInlineFunction() {
		return inlineFunctionStates.maybeHead();
	}
}
