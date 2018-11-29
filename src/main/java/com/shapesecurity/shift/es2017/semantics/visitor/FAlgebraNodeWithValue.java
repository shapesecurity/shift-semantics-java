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

public interface FAlgebraNodeWithValue<T> {
	@Nonnull
	T apply(BlockWithValue blockWithValue);

	@Nonnull
	T apply(Call call);

	@Nonnull
	T apply(DeleteGlobalProperty deleteGlobalProperty);

	@Nonnull
	T apply(DeleteProperty deleteProperty);

	@Nonnull
	T apply(GlobalReference globalReference);

	@Nonnull
	T apply(Halt halt);

	@Nonnull
	T apply(Keys keys);

	@Nonnull
	T apply(LiteralBoolean literalBoolean);

	@Nonnull
	T apply(LiteralEmptyArray literalEmptyArray);

	@Nonnull
	T apply(LiteralEmptyObject literalEmptyObject);

	@Nonnull
	T apply(LiteralFunction literalFunction);

	@Nonnull
	T apply(LiteralInfinity literalInfinity);

	@Nonnull
	T apply(LiteralNull literalNull);

	@Nonnull
	T apply(LiteralNumber literalNumber);

	@Nonnull
	T apply(LiteralRegExp literalRegExp);

	@Nonnull
	T apply(LiteralString literalString);

	@Nonnull
	T apply(LiteralSymbol literalSymbol);

	@Nonnull
	T apply(LiteralUndefined literalUndefined);

	@Nonnull
	T apply(MemberAccess memberAccess);

	@Nonnull
	T apply(MemberAssignment memberAssignment);

	@Nonnull
	T apply(New new_);

	@Nonnull
	T apply(RequireObjectCoercible requireObjectCoercible);

	@Nonnull
	T apply(LocalReference localReference);

	@Nonnull
	T apply(TemporaryReference temporaryReference);

	@Nonnull
	T apply(This this_);

	@Nonnull
	T apply(TypeCoercionNumber typeCoercionNumber);

	@Nonnull
	T apply(TypeCoercionString typeCoercionString);

	@Nonnull
	T apply(TypeofGlobal typeofGlobal);

	@Nonnull
	T apply(VariableAssignment variableAssignment);

	@Nonnull
	T apply(Equality equality);

	@Nonnull
	T apply(FloatMath floatMath);

	@Nonnull
	T apply(In in_);

	@Nonnull
	T apply(InstanceOf instanceOf);

	@Nonnull
	T apply(IntMath intMath);

	@Nonnull
	T apply(Logic logic);

	@Nonnull
	T apply(RelationalComparison relationalComparison);

	@Nonnull
	T apply(BitwiseNot bitwiseNot);

	@Nonnull
	T apply(Negation negation);

	@Nonnull
	T apply(Not not);

	@Nonnull
	T apply(Typeof typeof);

	@Nonnull
	T apply(VoidOp voidOp);
}
