package com.shapesecurity.shift.es2017.semantics.visitor;

import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.Equality;
import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.FloatMath;
import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.In;
import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.InstanceOf;
import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.IntMath;
import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.Logic;
import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.RelationalComparison;
import com.shapesecurity.shift.es2017.semantics.asg.BlockWithValue;
import com.shapesecurity.shift.es2017.semantics.asg.Call;
import com.shapesecurity.shift.es2017.semantics.asg.DeleteGlobalProperty;
import com.shapesecurity.shift.es2017.semantics.asg.DeleteProperty;
import com.shapesecurity.shift.es2017.semantics.asg.GlobalReference;
import com.shapesecurity.shift.es2017.semantics.asg.Halt;
import com.shapesecurity.shift.es2017.semantics.asg.Keys;
import com.shapesecurity.shift.es2017.semantics.asg.LiteralBoolean;
import com.shapesecurity.shift.es2017.semantics.asg.LiteralEmptyArray;
import com.shapesecurity.shift.es2017.semantics.asg.LiteralEmptyObject;
import com.shapesecurity.shift.es2017.semantics.asg.LiteralFunction;
import com.shapesecurity.shift.es2017.semantics.asg.LiteralInfinity;
import com.shapesecurity.shift.es2017.semantics.asg.LiteralNull;
import com.shapesecurity.shift.es2017.semantics.asg.LiteralNumber;
import com.shapesecurity.shift.es2017.semantics.asg.LiteralRegExp;
import com.shapesecurity.shift.es2017.semantics.asg.LiteralString;
import com.shapesecurity.shift.es2017.semantics.asg.LiteralSymbol;
import com.shapesecurity.shift.es2017.semantics.asg.LiteralUndefined;
import com.shapesecurity.shift.es2017.semantics.asg.LocalReference;
import com.shapesecurity.shift.es2017.semantics.asg.MemberAccess;
import com.shapesecurity.shift.es2017.semantics.asg.MemberAssignment;
import com.shapesecurity.shift.es2017.semantics.asg.New;
import com.shapesecurity.shift.es2017.semantics.asg.NodeWithValue;
import com.shapesecurity.shift.es2017.semantics.asg.RequireObjectCoercible;
import com.shapesecurity.shift.es2017.semantics.asg.TemporaryReference;
import com.shapesecurity.shift.es2017.semantics.asg.This;
import com.shapesecurity.shift.es2017.semantics.asg.TypeCoercionNumber;
import com.shapesecurity.shift.es2017.semantics.asg.TypeCoercionString;
import com.shapesecurity.shift.es2017.semantics.asg.TypeofGlobal;
import com.shapesecurity.shift.es2017.semantics.asg.UnaryOperation.BitwiseNot;
import com.shapesecurity.shift.es2017.semantics.asg.UnaryOperation.Negation;
import com.shapesecurity.shift.es2017.semantics.asg.UnaryOperation.Not;
import com.shapesecurity.shift.es2017.semantics.asg.UnaryOperation.Typeof;
import com.shapesecurity.shift.es2017.semantics.asg.UnaryOperation.VoidOp;
import com.shapesecurity.shift.es2017.semantics.asg.VariableAssignment;

import javax.annotation.Nonnull;

public class CataNodeWithValue {
	@Nonnull
	static <T> T cata(@Nonnull FAlgebraNodeWithValue<T> fAlgebra, @Nonnull NodeWithValue node) {
		if (node instanceof BlockWithValue) {
			return fAlgebra.apply(((BlockWithValue) node));
		} else if (node instanceof Call) {
			return fAlgebra.apply((Call) node);
		} else if (node instanceof DeleteGlobalProperty) {
			return fAlgebra.apply(((DeleteGlobalProperty) node));
		} else if (node instanceof DeleteProperty) {
			return fAlgebra.apply(((DeleteProperty) node));
		} else if (node instanceof GlobalReference) {
			return fAlgebra.apply(((GlobalReference) node));
		} else if (node instanceof Halt) {
			return fAlgebra.apply(((Halt) node));
		} else if (node instanceof Keys) {
			return fAlgebra.apply(((Keys) node));
		} else if (node instanceof LiteralBoolean) {
			return fAlgebra.apply(((LiteralBoolean) node));
		} else if (node instanceof LiteralEmptyArray) {
			return fAlgebra.apply(((LiteralEmptyArray) node));
		} else if (node instanceof LiteralEmptyObject) {
			return fAlgebra.apply(((LiteralEmptyObject) node));
		} else if (node instanceof LiteralFunction) {
			return fAlgebra.apply(((LiteralFunction) node));
		} else if (node instanceof LiteralInfinity) {
			return fAlgebra.apply(((LiteralInfinity) node));
		} else if (node instanceof LiteralNull) {
			return fAlgebra.apply(((LiteralNull) node));
		} else if (node instanceof LiteralNumber) {
			return fAlgebra.apply(((LiteralNumber) node));
		} else if (node instanceof LiteralRegExp) {
			return fAlgebra.apply(((LiteralRegExp) node));
		} else if (node instanceof LiteralString) {
			return fAlgebra.apply(((LiteralString) node));
		} else if (node instanceof LiteralSymbol) {
			return fAlgebra.apply(((LiteralSymbol) node));
		} else if (node instanceof LiteralUndefined) {
			return fAlgebra.apply(((LiteralUndefined) node));
		} else if (node instanceof TemporaryReference) {
			return fAlgebra.apply(((TemporaryReference) node));
		} else if (node instanceof LocalReference) {
			return fAlgebra.apply(((LocalReference) node));
		} else if (node instanceof MemberAccess) {
			return fAlgebra.apply(((MemberAccess) node));
		} else if (node instanceof MemberAssignment) {
			return fAlgebra.apply(((MemberAssignment) node));
		} else if (node instanceof New) {
			return fAlgebra.apply(((New) node));
		} else if (node instanceof RequireObjectCoercible) {
			return fAlgebra.apply(((RequireObjectCoercible) node));
		} else if (node instanceof This) {
			return fAlgebra.apply(((This) node));
		} else if (node instanceof TypeCoercionNumber) {
			return fAlgebra.apply(((TypeCoercionNumber) node));
		} else if (node instanceof TypeCoercionString) {
			return fAlgebra.apply(((TypeCoercionString) node));
		} else if (node instanceof TypeofGlobal) {
			return fAlgebra.apply(((TypeofGlobal) node));
		} else if (node instanceof VariableAssignment) {
			return fAlgebra.apply(((VariableAssignment) node));
		} else if (node instanceof Equality) {
			return fAlgebra.apply(((Equality) node));
		} else if (node instanceof FloatMath) {
			return fAlgebra.apply(((FloatMath) node));
		} else if (node instanceof In) {
			return fAlgebra.apply(((In) node));
		} else if (node instanceof InstanceOf) {
			return fAlgebra.apply(((InstanceOf) node));
		} else if (node instanceof IntMath) {
			return fAlgebra.apply(((IntMath) node));
		} else if (node instanceof Logic) {
			return fAlgebra.apply(((Logic) node));
		} else if (node instanceof RelationalComparison) {
			return fAlgebra.apply(((RelationalComparison) node));
		} else if (node instanceof BitwiseNot) {
			return fAlgebra.apply(((BitwiseNot) node));
		} else if (node instanceof Negation) {
			return fAlgebra.apply(((Negation) node));
		} else if (node instanceof Not) {
			return fAlgebra.apply(((Not) node));
		} else if (node instanceof Typeof) {
			return fAlgebra.apply(((Typeof) node));
		} else if (node instanceof VoidOp) {
			return fAlgebra.apply(((VoidOp) node));
		}
		throw new RuntimeException("Not reached.");
	}
}
