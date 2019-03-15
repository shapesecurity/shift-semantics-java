package com.shapesecurity.shift.es2017.semantics.visitor;

import com.shapesecurity.functional.Pair;
import com.shapesecurity.functional.data.ImmutableList;
import com.shapesecurity.functional.data.Maybe;
import com.shapesecurity.functional.data.Monoid;
import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.Equality;
import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.FloatMath;
import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.In;
import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.InstanceOf;
import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.IntMath;
import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.Logic;
import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.RelationalComparison;
import com.shapesecurity.shift.es2017.semantics.asg.Block;
import com.shapesecurity.shift.es2017.semantics.asg.BlockWithValue;
import com.shapesecurity.shift.es2017.semantics.asg.Break;
import com.shapesecurity.shift.es2017.semantics.asg.BreakTarget;
import com.shapesecurity.shift.es2017.semantics.asg.Call;
import com.shapesecurity.shift.es2017.semantics.asg.DeleteGlobalProperty;
import com.shapesecurity.shift.es2017.semantics.asg.DeleteProperty;
import com.shapesecurity.shift.es2017.semantics.asg.GlobalReference;
import com.shapesecurity.shift.es2017.semantics.asg.Halt;
import com.shapesecurity.shift.es2017.semantics.asg.IfElse;
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
import com.shapesecurity.shift.es2017.semantics.asg.Loop;
import com.shapesecurity.shift.es2017.semantics.asg.MemberAccess;
import com.shapesecurity.shift.es2017.semantics.asg.MemberAssignment;
import com.shapesecurity.shift.es2017.semantics.asg.MemberAssignmentProperty;
import com.shapesecurity.shift.es2017.semantics.asg.MemberCall;
import com.shapesecurity.shift.es2017.semantics.asg.MemberDefinition;
import com.shapesecurity.shift.es2017.semantics.asg.New;
import com.shapesecurity.shift.es2017.semantics.asg.Node;
import com.shapesecurity.shift.es2017.semantics.asg.RequireObjectCoercible;
import com.shapesecurity.shift.es2017.semantics.asg.Return;
import com.shapesecurity.shift.es2017.semantics.asg.ReturnAfterFinallies;
import com.shapesecurity.shift.es2017.semantics.asg.SwitchStatement;
import com.shapesecurity.shift.es2017.semantics.asg.TemporaryReference;
import com.shapesecurity.shift.es2017.semantics.asg.This;
import com.shapesecurity.shift.es2017.semantics.asg.Throw;
import com.shapesecurity.shift.es2017.semantics.asg.TryCatch;
import com.shapesecurity.shift.es2017.semantics.asg.TryFinally;
import com.shapesecurity.shift.es2017.semantics.asg.TypeCoercionNumber;
import com.shapesecurity.shift.es2017.semantics.asg.TypeCoercionObject;
import com.shapesecurity.shift.es2017.semantics.asg.TypeCoercionString;
import com.shapesecurity.shift.es2017.semantics.asg.TypeofGlobal;
import com.shapesecurity.shift.es2017.semantics.asg.UnaryOperation.BitwiseNot;
import com.shapesecurity.shift.es2017.semantics.asg.UnaryOperation.Negation;
import com.shapesecurity.shift.es2017.semantics.asg.UnaryOperation.Not;
import com.shapesecurity.shift.es2017.semantics.asg.UnaryOperation.Typeof;
import com.shapesecurity.shift.es2017.semantics.asg.UnaryOperation.VoidOp;
import com.shapesecurity.shift.es2017.semantics.asg.VariableAssignment;
import com.shapesecurity.shift.es2017.semantics.asg.Void;

import javax.annotation.Nonnull;

public abstract class MonoidalReducer<State> implements Reducer<State> {

	protected final Monoid<State> monoid;

	public MonoidalReducer(@Nonnull Monoid<State> monoid) {
		this.monoid = monoid;
	}

	protected State identity() {
		return this.monoid.identity();
	}

	protected State append(State a, State b) {
		return this.monoid.append(a, b);
	}

	protected State append(State a, State b, State c) {
		return append(append(a, b), c);
	}

	protected State append(State a, State b, State c, State d) {
		return append(append(a, b, c), d);
	}

	protected State fold(ImmutableList<State> as) {
		return as.foldLeft(this::append, this.identity());
	}

	protected State fold1(ImmutableList<State> as, State a) {
		return as.foldLeft(this::append, a);
	}

	@Nonnull
	protected State o(@Nonnull Maybe<State> s) {
		return s.orJust(this.identity());
	}

	@Nonnull
	public State reduceAll(@Nonnull Node node, @Nonnull State reduced) {
		return reduced;
	}

	@Nonnull
	public State reduceBlock(@Nonnull Block block, @Nonnull ImmutableList<State> children) {
		return fold(children);
	}

	@Nonnull
	public State reduceLoop(@Nonnull Loop loop, @Nonnull State block) {
		return block;
	}

	@Nonnull
	public State reduceBreak(@Nonnull Break _break, @Nonnull State breakTarget) {
		return breakTarget;
	}

	@Nonnull
	public State reduceBreakTarget(@Nonnull BreakTarget breakTarget) {
		return identity();
	}

	@Nonnull
	public State reduceCall(@Nonnull Call call, @Nonnull State callee, @Nonnull ImmutableList<State> arguments) {
		return fold1(arguments, callee);
	}

	@Nonnull
	public State reduceLiteralUndefined(@Nonnull LiteralUndefined literalUndefined) {
		return identity();
	}

	@Nonnull
	public State reduceLiteralSymbol(@Nonnull LiteralSymbol literalSymbol) {
		return identity();
	}

	@Nonnull
	public State reduceLiteralInfinity(@Nonnull LiteralInfinity literalInfinity) {
		return identity();
	}

	@Nonnull
	public State reduceLiteralRegExp(@Nonnull LiteralRegExp literalRegExp) {
		return identity();
	}

	@Nonnull
	public State reduceLiteralEmptyArray(@Nonnull LiteralEmptyArray literalEmptyArray) {
		return identity();
	}

	@Nonnull
	public State reduceLiteralEmptyObject(@Nonnull LiteralEmptyObject literalEmptyObject) {
		return identity();
	}

	@Nonnull
	public State reduceLiteralFunction(@Nonnull LiteralFunction literalFunction, @Nonnull State body) {
		return body;
	}

	@Nonnull
	public State reduceReturn(@Nonnull Return _return, @Nonnull Maybe<State> expression) {
		return o(expression);
	}

	@Nonnull
	public State reduceReturnAfterFinallies(@Nonnull ReturnAfterFinallies returnAfterFinallies, @Nonnull Maybe<State> savedValue) {
		return o(savedValue);
	}

	@Nonnull
	public State reduceLiteralNull(@Nonnull LiteralNull literalNull) {
		return identity();
	}

	@Nonnull
	public State reduceLiteralString(@Nonnull LiteralString literalString) {
		return identity();
	}

	@Nonnull
	public State reduceLiteralNumber(@Nonnull LiteralNumber literalNumber) {
		return identity();
	}

	@Nonnull
	public State reduceLiteralBoolean(@Nonnull LiteralBoolean literalBoolean) {
		return identity();
	}

	@Nonnull
	public State reduceMemberCall(@Nonnull MemberCall memberCall, @Nonnull State object, @Nonnull State fieldExpression, @Nonnull ImmutableList<State> arguments) {
		return fold1(arguments, append(object, fieldExpression));
	}

	@Nonnull
	public State reduceNot(@Nonnull Not not, @Nonnull State expression) {
		return expression;
	}

	@Nonnull
	public State reduceBlockWithValue(@Nonnull BlockWithValue blockWithValue, @Nonnull State block, @Nonnull State result) {
		return append(block, result);
	}

	@Nonnull
	public State reduceFloatMath(@Nonnull FloatMath floatMath, @Nonnull State left, @Nonnull State right) {
		return append(left, right);
	}

	@Nonnull
	public State reduceIntMath(@Nonnull IntMath intMath, @Nonnull State left, @Nonnull State right) {
		return append(left, right);
	}

	@Nonnull
	public State reduceInstanceOf(@Nonnull InstanceOf instanceOf, @Nonnull State left, @Nonnull State right) {
		return append(left, right);
	}

	@Nonnull
	public State reduceNegation(@Nonnull Negation negation, @Nonnull State expression) {
		return expression;
	}

	@Nonnull
	public State reduceEquality(@Nonnull Equality equality, @Nonnull State left, @Nonnull State right) {
		return append(left, right);
	}

	@Nonnull
	public State reduceNew(@Nonnull New expression, @Nonnull State callee, @Nonnull ImmutableList<State> arguments) {
		return fold1(arguments, callee);
	}

	@Nonnull
	public State reduceThis(@Nonnull This expression) {
		return identity();
	}

	@Nonnull
	public State reduceRequireObjectCoercible(@Nonnull RequireObjectCoercible requireObjectCoercible, @Nonnull State expression) {
		return expression;
	}

	@Nonnull
	public State reduceTypeCoercionString(@Nonnull TypeCoercionString typeCoercionString, @Nonnull State expression) {
		return expression;
	}

	@Nonnull
	public State reduceTypeCoercionNumber(@Nonnull TypeCoercionNumber typeCoercionNumber, @Nonnull State expression) {
		return expression;
	}

	@Nonnull
	public State reduceTypeCoercionObject(@Nonnull TypeCoercionObject typeCoercionObject, @Nonnull State expression) {
		return expression;
	}

	@Nonnull
	public State reduceKeys(@Nonnull Keys keys, @Nonnull State object) {
		return object;
	}

	@Nonnull
	public State reduceTypeofGlobal(@Nonnull TypeofGlobal typeofGlobal) {
		return identity();
	}

	@Nonnull
	public State reduceVoidOp(@Nonnull VoidOp voidOp, @Nonnull State expression) {
		return expression;
	}

	@Nonnull
	public State reduceDeleteGlobalProperty(@Nonnull DeleteGlobalProperty expression) {
		return identity();
	}

	@Nonnull
	public State reduceLogic(@Nonnull Logic expression, @Nonnull State left, @Nonnull State right) {
		return append(left, right);
	}

	@Nonnull
	public State reduceHalt(@Nonnull Halt halt) {
		return identity();
	}

	@Nonnull
	public State reduceIn(@Nonnull In expression, @Nonnull State left, @Nonnull State right) {
		return append(left, right);
	}

	@Nonnull
	public State reduceDeleteProperty(@Nonnull DeleteProperty expression, @Nonnull State object, @Nonnull State fieldExpression) {
		return append(object, fieldExpression);
	}

	@Nonnull
	public State reduceTypeof(@Nonnull Typeof typeOf, @Nonnull State expression) {
		return expression;
	}

	@Nonnull
	public State reduceBitwiseNot(@Nonnull BitwiseNot bitwiseNot, @Nonnull State expression) {
		return expression;
	}

	@Nonnull
	public State reduceGlobalReference(@Nonnull GlobalReference ref) {
		return identity();
	}

	@Nonnull
	public State reduceTemporaryReference(@Nonnull TemporaryReference ref) {
		return identity();
	}

	@Nonnull
	public State reduceLocalReference(@Nonnull LocalReference ref) {
		return identity();
	}

	@Nonnull
	public State reduceMemberAccess(@Nonnull MemberAccess memberAccess, @Nonnull State object, @Nonnull State fieldExpression) {
		return append(object, fieldExpression);
	}

	@Nonnull
	public State reduceMemberAssignment(@Nonnull MemberAssignment memberAssignment, @Nonnull State object, @Nonnull State fieldExpression, @Nonnull State value) {
		return append(object, fieldExpression, value);
	}

	@Nonnull
	public State reduceThrow(@Nonnull Throw node, @Nonnull State expression) {
		return expression;
	}

	@Nonnull
	public State reduceStaticValue(@Nonnull MemberAssignmentProperty.StaticValue node, @Nonnull State value) {
		return value;
	}

	@Nonnull
	public State reduceGetter(@Nonnull MemberAssignmentProperty.Getter node, @Nonnull State value) {
		return value;
	}

	@Nonnull
	public State reduceSetter(@Nonnull MemberAssignmentProperty.Setter node, @Nonnull State value) {
		return value;
	}

	@Nonnull
	public State reduceMemberDefinition(@Nonnull MemberDefinition node, @Nonnull State object, @Nonnull State fieldExpression, @Nonnull State property) {
		return append(object, fieldExpression, property);
	}

	@Nonnull
	public State reduceTryCatch(@Nonnull TryCatch node, @Nonnull State tryBody, State catchBlock) {
		return append(tryBody, catchBlock);
	}

	@Nonnull
	public State reduceTryFinally(@Nonnull TryFinally node, @Nonnull State tryBody, @Nonnull State finallyBody) {
		return append(tryBody, finallyBody);
	}

	@Nonnull
	public State reduceSwitchStatement(@Nonnull SwitchStatement node, @Nonnull State discriminant, @Nonnull ImmutableList<Pair<State, State>> preDefaultCases, @Nonnull State defaultCase, @Nonnull ImmutableList<Pair<State, State>> postDefaultCases) {
		return append(fold1(preDefaultCases.map(pair -> append(pair.left, pair.right)), discriminant), fold1(postDefaultCases.map(pair -> append(pair.left, pair.right)), defaultCase));
	}

	@Nonnull
	public State reduceIfElse(@Nonnull IfElse ifElse, @Nonnull State test, @Nonnull State consequent, @Nonnull State alternate) {
		return append(test, consequent, alternate);
	}

	@Nonnull
	public State reduceRelationalComparison(@Nonnull RelationalComparison relationalComparison, @Nonnull State left, @Nonnull State right) {
		return append(left, right);
	}

	@Nonnull
	public State reduceVariableAssignment(@Nonnull VariableAssignment variableAssignment, @Nonnull State ref, @Nonnull State value) {
		return append(ref, value);
	}

	@Nonnull
	public State reduceVoid(@Nonnull Void _void) {
		return identity();
	}


}
