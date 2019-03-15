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

public interface Reducer<State> {

	@Nonnull
	State reduceAll(@Nonnull Node node, @Nonnull State reduced);

	@Nonnull
	State reduceBlock(@Nonnull Block block, @Nonnull ImmutableList<State> children);

	@Nonnull
	State reduceLoop(@Nonnull Loop loop, @Nonnull State block);

	@Nonnull
	State reduceBreak(@Nonnull Break _break, @Nonnull State breakTarget);

	@Nonnull
	State reduceBreakTarget(@Nonnull BreakTarget breakTarget);

	@Nonnull
	State reduceCall(@Nonnull Call call, @Nonnull State callee, @Nonnull ImmutableList<State> arguments);

	@Nonnull
	State reduceLiteralUndefined(@Nonnull LiteralUndefined literalUndefined);

	@Nonnull
	State reduceLiteralSymbol(@Nonnull LiteralSymbol literalSymbol);

	@Nonnull
	State reduceLiteralInfinity(@Nonnull LiteralInfinity literalInfinity);

	@Nonnull
	State reduceLiteralRegExp(@Nonnull LiteralRegExp literalRegExp);

	@Nonnull
	State reduceLiteralEmptyArray(@Nonnull LiteralEmptyArray literalEmptyArray);

	@Nonnull
	State reduceLiteralEmptyObject(@Nonnull LiteralEmptyObject literalEmptyObject);

	@Nonnull
	State reduceLiteralFunction(@Nonnull LiteralFunction literalFunction, @Nonnull State body);

	@Nonnull
	State reduceReturn(@Nonnull Return _return, @Nonnull Maybe<State> expression);

	@Nonnull
	State reduceReturnAfterFinallies(@Nonnull ReturnAfterFinallies returnAfterFinallies, @Nonnull Maybe<State> savedValue);

	@Nonnull
	State reduceLiteralNull(@Nonnull LiteralNull literalNull);

	@Nonnull
	State reduceLiteralString(@Nonnull LiteralString literalString);

	@Nonnull
	State reduceLiteralNumber(@Nonnull LiteralNumber literalNumber);

	@Nonnull
	State reduceLiteralBoolean(@Nonnull LiteralBoolean literalBoolean);

	@Nonnull
	State reduceMemberCall(@Nonnull MemberCall memberCall, @Nonnull State object, @Nonnull State fieldExpression, @Nonnull ImmutableList<State> arguments);

	@Nonnull
	State reduceNot(@Nonnull Not not, @Nonnull State expression);

	@Nonnull
	State reduceBlockWithValue(@Nonnull BlockWithValue blockWithValue, @Nonnull State block, @Nonnull State result);

	@Nonnull
	State reduceFloatMath(@Nonnull FloatMath floatMath, @Nonnull State left, @Nonnull State right);

	@Nonnull
	State reduceIntMath(@Nonnull IntMath intMath, @Nonnull State left, @Nonnull State right);

	@Nonnull
	State reduceInstanceOf(@Nonnull InstanceOf instanceOf, @Nonnull State left, @Nonnull State right);

	@Nonnull
	State reduceNegation(@Nonnull Negation negation, @Nonnull State expression);

	@Nonnull
	State reduceEquality(@Nonnull Equality equality, @Nonnull State left, @Nonnull State right);

	@Nonnull
	State reduceNew(@Nonnull New expression, @Nonnull State callee, @Nonnull ImmutableList<State> arguments);

	@Nonnull
	State reduceThis(@Nonnull This expression);

	@Nonnull
	State reduceRequireObjectCoercible(@Nonnull RequireObjectCoercible requireObjectCoercible, @Nonnull State expression);

	@Nonnull
	State reduceTypeCoercionString(@Nonnull TypeCoercionString typeCoercionString, @Nonnull State expression);

	@Nonnull
	State reduceTypeCoercionNumber(@Nonnull TypeCoercionNumber typeCoercionNumber, @Nonnull State expression);

	@Nonnull
	State reduceTypeCoercionObject(@Nonnull TypeCoercionObject typeCoercionObject, @Nonnull State expression);

	@Nonnull
	State reduceKeys(@Nonnull Keys keys, @Nonnull State object);

	@Nonnull
	State reduceTypeofGlobal(@Nonnull TypeofGlobal typeofGlobal);

	@Nonnull
	State reduceVoidOp(@Nonnull VoidOp voidOp, @Nonnull State expression);

	@Nonnull
	State reduceDeleteGlobalProperty(@Nonnull DeleteGlobalProperty expression);

	@Nonnull
	State reduceLogic(@Nonnull Logic expression, @Nonnull State left, @Nonnull State right);

	@Nonnull
	State reduceHalt(@Nonnull Halt halt);

	@Nonnull
	State reduceIn(@Nonnull In expression, @Nonnull State left, @Nonnull State right);

	@Nonnull
	State reduceDeleteProperty(@Nonnull DeleteProperty expression, @Nonnull State object, @Nonnull State fieldExpression);

	@Nonnull
	State reduceTypeof(@Nonnull Typeof typeOf, @Nonnull State expression);

	@Nonnull
	State reduceBitwiseNot(@Nonnull BitwiseNot bitwiseNot, @Nonnull State expression);

	@Nonnull
	State reduceGlobalReference(@Nonnull GlobalReference ref);

	@Nonnull
	State reduceTemporaryReference(@Nonnull TemporaryReference ref);

	@Nonnull
	State reduceLocalReference(@Nonnull LocalReference ref);

	@Nonnull
	State reduceMemberAccess(@Nonnull MemberAccess memberAccess, @Nonnull State object, @Nonnull State fieldExpression);

	@Nonnull
	State reduceMemberAssignment(@Nonnull MemberAssignment memberAssignment, @Nonnull State object, @Nonnull State fieldExpression, @Nonnull State value);

	@Nonnull
	State reduceThrow(@Nonnull Throw node, @Nonnull State expression);

	@Nonnull
	State reduceStaticValue(@Nonnull MemberAssignmentProperty.StaticValue node, @Nonnull State value);

	@Nonnull
	State reduceGetter(@Nonnull MemberAssignmentProperty.Getter node, @Nonnull State value);

	@Nonnull
	State reduceSetter(@Nonnull MemberAssignmentProperty.Setter node, @Nonnull State value);

	@Nonnull
	State reduceMemberDefinition(@Nonnull MemberDefinition node, @Nonnull State object, @Nonnull State fieldExpression, @Nonnull State property);

	@Nonnull
	State reduceTryCatch(@Nonnull TryCatch node, @Nonnull State tryBody, State catchBlock);

	@Nonnull
	State reduceTryFinally(@Nonnull TryFinally node, @Nonnull State tryBody, @Nonnull State finallyBody);

	@Nonnull
	State reduceSwitchStatement(@Nonnull SwitchStatement node, @Nonnull State discriminant, @Nonnull ImmutableList<Pair<State, State>> preDefaultCases, @Nonnull State defaultCase, @Nonnull ImmutableList<Pair<State, State>> postDefaultCases);

	@Nonnull
	State reduceIfElse(@Nonnull IfElse ifElse, @Nonnull State test, @Nonnull State consequent, @Nonnull State alternate);

	@Nonnull
	State reduceRelationalComparison(@Nonnull RelationalComparison relationalComparison, @Nonnull State left, @Nonnull State right);

	@Nonnull
	State reduceVariableAssignment(@Nonnull VariableAssignment variableAssignment, @Nonnull State ref, @Nonnull State value);

	@Nonnull
	State reduceVoid(@Nonnull Void _void);


}
