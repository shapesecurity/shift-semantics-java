/*
 * Copyright 2017 Shape Security, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shapesecurity.shift.es2017.semantics.visitor;

import com.shapesecurity.functional.Pair;
import com.shapesecurity.functional.data.Either;
import com.shapesecurity.functional.data.ImmutableList;
import com.shapesecurity.functional.data.Maybe;
import com.shapesecurity.shift.es2017.scope.Variable;
import com.shapesecurity.shift.es2017.semantics.asg.*;
import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.Equality;
import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.FloatMath;
import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.In;
import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.InstanceOf;
import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.IntMath;
import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.Logic;
import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.RelationalComparison;
import com.shapesecurity.shift.es2017.semantics.asg.UnaryOperation.BitwiseNot;
import com.shapesecurity.shift.es2017.semantics.asg.UnaryOperation.Negation;
import com.shapesecurity.shift.es2017.semantics.asg.UnaryOperation.Not;
import com.shapesecurity.shift.es2017.semantics.asg.UnaryOperation.Typeof;
import com.shapesecurity.shift.es2017.semantics.asg.UnaryOperation.VoidOp;
import com.shapesecurity.shift.es2017.semantics.asg.Void;

import javax.annotation.Nonnull;

public class ReconstructingReducer {

	public static Reducer<Node> create() {
		return new NodeAdaptingReducer(new ReconstructingReducer());
	}

	protected ReconstructingReducer() {

	}

	@Nonnull
	public Node reduceAll(@Nonnull Node node, @Nonnull Node reduced) {
		return reduced;
	}

	@Nonnull
	public Block reduceBlock(@Nonnull Block block, @Nonnull ImmutableList<Node> children) {
		return new Block(children);
	}

	@Nonnull
	public Node reduceLoop(@Nonnull Loop loop, @Nonnull Block block) {
		return new Loop(block);
	}

	@Nonnull
	public Node reduceBreak(@Nonnull Break _break, @Nonnull BreakTarget breakTarget) {
		return new Break(breakTarget, _break.broken);
	}

	@Nonnull
	public BreakTarget reduceBreakTarget(@Nonnull BreakTarget breakTarget) {
		return breakTarget;
	}

	@Nonnull
	public NodeWithValue reduceCall(@Nonnull Call call, @Nonnull NodeWithValue callee, @Nonnull ImmutableList<NodeWithValue> arguments) {
		return new Call(callee, arguments);
	}

	@Nonnull
	public NodeWithValue reduceLiteralUndefined(@Nonnull LiteralUndefined literalUndefined) {
		return LiteralUndefined.INSTANCE;
	}

	@Nonnull
	public NodeWithValue reduceLiteralSymbol(@Nonnull LiteralSymbol literalSymbol) {
		return new LiteralSymbol(literalSymbol.description);
	}

	@Nonnull
	public NodeWithValue reduceLiteralInfinity(@Nonnull LiteralInfinity literalInfinity) {
		return LiteralInfinity.INSTANCE;
	}

	@Nonnull
	public NodeWithValue reduceLiteralRegExp(@Nonnull LiteralRegExp literalRegExp) {
		return new LiteralRegExp(literalRegExp.pattern, literalRegExp.global, literalRegExp.ignoreCase, literalRegExp.multiLine, literalRegExp.sticky, literalRegExp.unicode);
	}

	@Nonnull
	public NodeWithValue reduceLiteralEmptyArray(@Nonnull LiteralEmptyArray literalEmptyArray) {
		return LiteralEmptyArray.INSTANCE;
	}

	@Nonnull
	public NodeWithValue reduceLiteralEmptyObject(@Nonnull LiteralEmptyObject literalEmptyObject) {
		return LiteralEmptyObject.INSTANCE;
	}

	@Nonnull
	public NodeWithValue reduceLiteralFunction(@Nonnull LiteralFunction literalFunction, @Nonnull Block body) {
		return new LiteralFunction(
			literalFunction.name,
			literalFunction.arguments,
			literalFunction.parameters,
			literalFunction.locals,
			literalFunction.captured,
			body,
			literalFunction.isStrict
		);
	}

	@Nonnull
	public Node reduceReturn(@Nonnull Return _return, @Nonnull Maybe<NodeWithValue> expression) {
		return new Return(expression);
	}

	@Nonnull
	public Node reduceReturnAfterFinallies(@Nonnull ReturnAfterFinallies returnAfterFinallies, @Nonnull Maybe<LocalReference> savedValue) {
		return new ReturnAfterFinallies(savedValue, returnAfterFinallies.broken);
	}

	@Nonnull
	public NodeWithValue reduceLiteralNull(@Nonnull LiteralNull literalNull) {
		return LiteralNull.INSTANCE;
	}

	@Nonnull
	public NodeWithValue reduceLiteralString(@Nonnull LiteralString literalString) {
		return new LiteralString(literalString.value);
	}

	@Nonnull
	public NodeWithValue reduceLiteralNumber(@Nonnull LiteralNumber literalNumber) {
		return new LiteralNumber(literalNumber.value);
	}

	@Nonnull
	public NodeWithValue reduceLiteralBoolean(@Nonnull LiteralBoolean literalBoolean) {
		return new LiteralBoolean(literalBoolean.value);
	}

	@Nonnull
	public NodeWithValue reduceMemberCall(@Nonnull MemberCall memberCall, @Nonnull NodeWithValue object, @Nonnull NodeWithValue fieldExpression, @Nonnull ImmutableList<NodeWithValue> arguments) {
		return new MemberCall(object, fieldExpression, arguments);
	}

	@Nonnull
	public NodeWithValue reduceNot(@Nonnull Not not, @Nonnull NodeWithValue expression) {
		return new Not(expression);
	}

	@Nonnull
	public NodeWithValue reduceBlockWithValue(@Nonnull BlockWithValue blockWithValue, @Nonnull Block block, @Nonnull NodeWithValue result) {
		return new BlockWithValue(block, result);
	}

	@Nonnull
	public NodeWithValue reduceFloatMath(@Nonnull FloatMath floatMath, @Nonnull NodeWithValue left, @Nonnull NodeWithValue right) {
		return new FloatMath(floatMath.operator, left, right);
	}

	@Nonnull
	public NodeWithValue reduceIntMath(@Nonnull IntMath intMath, @Nonnull NodeWithValue left, @Nonnull NodeWithValue right) {
		return new IntMath(intMath.operator, left, right);
	}

	@Nonnull
	public NodeWithValue reduceInstanceOf(@Nonnull InstanceOf instanceOf, @Nonnull NodeWithValue left, @Nonnull NodeWithValue right) {
		return new InstanceOf(left, right);
	}

	@Nonnull
	public NodeWithValue reduceNegation(@Nonnull Negation negation, @Nonnull NodeWithValue expression) {
		return new Negation(expression);
	}

	@Nonnull
	public NodeWithValue reduceEquality(@Nonnull Equality equality, @Nonnull NodeWithValue left, @Nonnull NodeWithValue right) {
		return new Equality(equality.operator, left, right);
	}

	@Nonnull
	public NodeWithValue reduceNew(@Nonnull New expression, @Nonnull NodeWithValue callee, @Nonnull ImmutableList<NodeWithValue> arguments) {
		return new New(callee, arguments);
	}

	@Nonnull
	public NodeWithValue reduceThis(@Nonnull This expression) {
		return new This(expression.strict);
	}

	@Nonnull
	public NodeWithValue reduceRequireObjectCoercible(@Nonnull RequireObjectCoercible requireObjectCoercible, @Nonnull NodeWithValue expression) {
		return new RequireObjectCoercible(expression);
	}

	@Nonnull
	public NodeWithValue reduceTypeCoercionString(@Nonnull TypeCoercionString typeCoercionString, @Nonnull NodeWithValue expression) {
		return new TypeCoercionString(expression);
	}

	@Nonnull
	public NodeWithValue reduceTypeCoercionNumber(@Nonnull TypeCoercionNumber typeCoercionNumber, @Nonnull NodeWithValue expression) {
		return new TypeCoercionNumber(expression);
	}

	@Nonnull
	public NodeWithValue reduceTypeCoercionObject(@Nonnull TypeCoercionObject typeCoercionObject, @Nonnull NodeWithValue expression) {
		return new TypeCoercionObject(expression);
	}

	@Nonnull
	public NodeWithValue reduceKeys(@Nonnull Keys keys, @Nonnull NodeWithValue object) {
		return new Keys(object);
	}

	@Nonnull
	public NodeWithValue reduceTypeofGlobal(@Nonnull TypeofGlobal typeofGlobal) {
		return new TypeofGlobal(typeofGlobal.which);
	}

	@Nonnull
	public NodeWithValue reduceVoidOp(@Nonnull VoidOp voidOp, @Nonnull NodeWithValue expression) {
		return new VoidOp(expression);
	}

	@Nonnull
	public NodeWithValue reduceDeleteGlobalProperty(@Nonnull DeleteGlobalProperty expression) {
		return new DeleteGlobalProperty(expression.which);
	}

	@Nonnull
	public NodeWithValue reduceLogic(@Nonnull Logic expression, @Nonnull NodeWithValue left, @Nonnull NodeWithValue right) {
		return new Logic(expression.operator, left, right);
	}

	@Nonnull
	public NodeWithValue reduceHalt(@Nonnull Halt halt) {
		return Halt.INSTANCE;
	}

	@Nonnull
	public NodeWithValue reduceIn(@Nonnull In expression, @Nonnull NodeWithValue left, @Nonnull NodeWithValue right) {
		return new In(left, right);
	}

	@Nonnull
	public NodeWithValue reduceDeleteProperty(@Nonnull DeleteProperty expression, @Nonnull NodeWithValue object, @Nonnull NodeWithValue fieldExpression) {
		return new DeleteProperty(object, fieldExpression, expression.strict);
	}

	@Nonnull
	public NodeWithValue reduceTypeof(@Nonnull Typeof typeOf, @Nonnull NodeWithValue expression) {
		return new Typeof(expression);
	}

	@Nonnull
	public NodeWithValue reduceBitwiseNot(@Nonnull BitwiseNot bitwiseNot, @Nonnull NodeWithValue expression) {
		return new BitwiseNot(expression);
	}

	@Nonnull
	public GlobalReference reduceGlobalReference(@Nonnull GlobalReference ref) {
		return new GlobalReference(ref.name);
	}

	@Nonnull
	public LocalReference reduceTemporaryReference(@Nonnull TemporaryReference ref) {
		return new TemporaryReference(ref.variable);
	}

	@Nonnull
	public LocalReference reduceLocalReference(@Nonnull LocalReference ref) {
		return new LocalReference(ref.variable);
	}

	@Nonnull
	public NodeWithValue reduceMemberAccess(@Nonnull MemberAccess memberAccess, @Nonnull NodeWithValue object, @Nonnull NodeWithValue fieldExpression) {
		return new MemberAccess(object, fieldExpression);
	}

	@Nonnull
	public NodeWithValue reduceMemberAssignment(@Nonnull MemberAssignment memberAssignment, @Nonnull NodeWithValue object, @Nonnull NodeWithValue fieldExpression, @Nonnull NodeWithValue value) {
		return new MemberAssignment(object, fieldExpression, value, memberAssignment.strict);
	}

	@Nonnull
	public Node reduceThrow(@Nonnull Throw node, @Nonnull NodeWithValue expression) {
		return new Throw(expression);
	}

	@Nonnull
	public MemberAssignmentProperty reduceStaticValue(@Nonnull MemberAssignmentProperty.StaticValue node, @Nonnull NodeWithValue value) {
		return new MemberAssignmentProperty.StaticValue(value);
	}

	@Nonnull
	public MemberAssignmentProperty reduceGetter(@Nonnull MemberAssignmentProperty.Getter node, @Nonnull LiteralFunction value) {
		return new MemberAssignmentProperty.Getter(value);

	}

	@Nonnull
	public MemberAssignmentProperty reduceSetter(@Nonnull MemberAssignmentProperty.Setter node, @Nonnull LiteralFunction value) {
		return new MemberAssignmentProperty.Setter(value);
	}

	@Nonnull
	public Node reduceMemberDefinition(@Nonnull MemberDefinition node, @Nonnull NodeWithValue object, @Nonnull NodeWithValue fieldExpression, @Nonnull MemberAssignmentProperty property) {
		return new MemberDefinition(object, fieldExpression, property);
	}

	@Nonnull
	public Node reduceTryCatch(@Nonnull TryCatch node, @Nonnull Block tryBody, @Nonnull Pair<Variable, Block> catchBody) {
		return new TryCatch(tryBody, catchBody);
	}

	@Nonnull
	public Node reduceTryFinally(@Nonnull TryFinally node, @Nonnull Block tryBody, @Nonnull Block finallyBody) {
		return new TryFinally(tryBody, finallyBody);
	}

	@Nonnull
	public Node reduceSwitchStatement(@Nonnull SwitchStatement node, @Nonnull LocalReference discriminant, @Nonnull ImmutableList<Pair<NodeWithValue, Block>> preDefaultCases, @Nonnull Block defaultCase, @Nonnull ImmutableList<Pair<NodeWithValue, Block>> postDefaultCases) {
		return new SwitchStatement(discriminant, preDefaultCases, defaultCase, postDefaultCases);
	}

	@Nonnull
	public Node reduceIfElse(@Nonnull IfElse ifElse, @Nonnull NodeWithValue test, @Nonnull Block consequent, @Nonnull Block alternate) {
		return new IfElse(test, consequent, alternate);
	}

	@Nonnull
	public NodeWithValue reduceRelationalComparison(@Nonnull RelationalComparison relationalComparison, @Nonnull NodeWithValue left, @Nonnull NodeWithValue right) {
		return new RelationalComparison(relationalComparison.operator, left, right);
	}

	@Nonnull
	public NodeWithValue reduceVariableAssignment(@Nonnull VariableAssignment variableAssignment, @Nonnull Either<GlobalReference, LocalReference> ref, @Nonnull NodeWithValue value) {
		return new VariableAssignment(ref, value, variableAssignment.strict);
	}

	@Nonnull
	public Node reduceVoid(@Nonnull com.shapesecurity.shift.es2017.semantics.asg.Void _void) {
		return Void.INSTANCE;
	}

}
