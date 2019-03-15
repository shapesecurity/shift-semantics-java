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
import com.shapesecurity.shift.es2017.semantics.asg.NodeWithValue;
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


// allows a ReconstructingReducer to appear as a Reducer for usage in the Director.
public final class NodeAdaptingReducer implements Reducer<Node> {

	@Nonnull
	public final ReconstructingReducer reducer;

	public NodeAdaptingReducer(@Nonnull ReconstructingReducer reducer) {
		this.reducer = reducer;
	}

	@Nonnull
	@Override
	public Node reduceAll(@Nonnull Node node, @Nonnull Node reduced) {
		return reducer.reduceAll(node, reduced);
	}

	@Nonnull
	@Override
	public Node reduceBlock(@Nonnull Block block, @Nonnull ImmutableList<Node> children) {
		return reducer.reduceBlock(block, children);
	}

	@Nonnull
	@Override
	public Node reduceLoop(@Nonnull Loop loop, @Nonnull Node block) {
		return reducer.reduceLoop(loop, (Block) block);
	}

	@Nonnull
	@Override
	public Node reduceBreak(@Nonnull Break _break, @Nonnull Node breakTarget) {
		return reducer.reduceBreak(_break, (BreakTarget) breakTarget);
	}

	@Nonnull
	@Override
	public Node reduceBreakTarget(@Nonnull BreakTarget breakTarget) {
		return reducer.reduceBreakTarget(breakTarget);
	}

	@Nonnull
	@Override
	public Node reduceCall(@Nonnull Call call, @Nonnull Node callee, @Nonnull ImmutableList<Node> arguments) {
		return reducer.reduceCall(call, (NodeWithValue) callee, arguments.map(node -> (NodeWithValue) node));
	}

	@Nonnull
	@Override
	public Node reduceLiteralUndefined(@Nonnull LiteralUndefined literalUndefined) {
		return reducer.reduceLiteralUndefined(literalUndefined);
	}

	@Nonnull
	@Override
	public Node reduceLiteralSymbol(@Nonnull LiteralSymbol literalSymbol) {
		return reducer.reduceLiteralSymbol(literalSymbol);
	}

	@Nonnull
	@Override
	public Node reduceLiteralInfinity(@Nonnull LiteralInfinity literalInfinity) {
		return reducer.reduceLiteralInfinity(literalInfinity);
	}

	@Nonnull
	@Override
	public Node reduceLiteralRegExp(@Nonnull LiteralRegExp literalRegExp) {
		return reducer.reduceLiteralRegExp(literalRegExp);
	}

	@Nonnull
	@Override
	public Node reduceLiteralEmptyArray(@Nonnull LiteralEmptyArray literalEmptyArray) {
		return reducer.reduceLiteralEmptyArray(literalEmptyArray);
	}

	@Nonnull
	@Override
	public Node reduceLiteralEmptyObject(@Nonnull LiteralEmptyObject literalEmptyObject) {
		return reducer.reduceLiteralEmptyObject(literalEmptyObject);
	}

	@Nonnull
	@Override
	public Node reduceLiteralFunction(@Nonnull LiteralFunction literalFunction, @Nonnull Node body) {
		return reducer.reduceLiteralFunction(literalFunction, (Block) body);
	}

	@Nonnull
	@Override
	public Node reduceReturn(@Nonnull Return _return, @Nonnull Maybe<Node> expression) {
		return reducer.reduceReturn(_return, expression.map(node -> (NodeWithValue) node));
	}

	@Nonnull
	@Override
	public Node reduceReturnAfterFinallies(@Nonnull ReturnAfterFinallies returnAfterFinallies, @Nonnull Maybe<Node> savedValue) {
		return reducer.reduceReturnAfterFinallies(returnAfterFinallies, savedValue.map(node -> (LocalReference) node));
	}

	@Nonnull
	@Override
	public Node reduceLiteralNull(@Nonnull LiteralNull literalNull) {
		return reducer.reduceLiteralNull(literalNull);
	}

	@Nonnull
	@Override
	public Node reduceLiteralString(@Nonnull LiteralString literalString) {
		return reducer.reduceLiteralString(literalString);
	}

	@Nonnull
	@Override
	public Node reduceLiteralNumber(@Nonnull LiteralNumber literalNumber) {
		return reducer.reduceLiteralNumber(literalNumber);
	}

	@Nonnull
	@Override
	public Node reduceLiteralBoolean(@Nonnull LiteralBoolean literalBoolean) {
		return reducer.reduceLiteralBoolean(literalBoolean);
	}

	@Nonnull
	@Override
	public Node reduceMemberCall(@Nonnull MemberCall memberCall, @Nonnull Node object, @Nonnull Node fieldExpression, @Nonnull ImmutableList<Node> arguments) {
		return reducer.reduceMemberCall(memberCall, (NodeWithValue) object, (NodeWithValue) fieldExpression, arguments.map(node -> (NodeWithValue) node));
	}

	@Nonnull
	@Override
	public Node reduceNot(@Nonnull Not not, @Nonnull Node expression) {
		return reducer.reduceNot(not, (NodeWithValue) expression);
	}

	@Nonnull
	@Override
	public Node reduceBlockWithValue(@Nonnull BlockWithValue blockWithValue, @Nonnull Node block, @Nonnull Node result) {
		return reducer.reduceBlockWithValue(blockWithValue, (Block) block, (NodeWithValue) result);
	}

	@Nonnull
	@Override
	public Node reduceFloatMath(@Nonnull FloatMath floatMath, @Nonnull Node left, @Nonnull Node right) {
		return reducer.reduceFloatMath(floatMath, (NodeWithValue) left, (NodeWithValue) right);
	}

	@Nonnull
	@Override
	public Node reduceIntMath(@Nonnull IntMath intMath, @Nonnull Node left, @Nonnull Node right) {
		return reducer.reduceIntMath(intMath, (NodeWithValue) left, (NodeWithValue) right);
	}

	@Nonnull
	@Override
	public Node reduceInstanceOf(@Nonnull InstanceOf instanceOf, @Nonnull Node left, @Nonnull Node right) {
		return reducer.reduceInstanceOf(instanceOf, (NodeWithValue) left, (NodeWithValue) right);
	}

	@Nonnull
	@Override
	public Node reduceNegation(@Nonnull Negation negation, @Nonnull Node expression) {
		return reducer.reduceNegation(negation, (NodeWithValue) expression);
	}

	@Nonnull
	@Override
	public Node reduceEquality(@Nonnull Equality equality, @Nonnull Node left, @Nonnull Node right) {
		return reducer.reduceEquality(equality, (NodeWithValue) left, (NodeWithValue) right);
	}

	@Nonnull
	@Override
	public Node reduceNew(@Nonnull New _new, @Nonnull Node callee, @Nonnull ImmutableList<Node> arguments) {
		return reducer.reduceNew(_new, (NodeWithValue) callee, arguments.map(node -> (NodeWithValue) node));
	}

	@Nonnull
	@Override
	public Node reduceThis(@Nonnull This expression) {
		return reducer.reduceThis(expression);
	}

	@Nonnull
	@Override
	public Node reduceRequireObjectCoercible(@Nonnull RequireObjectCoercible requireObjectCoercible, @Nonnull Node expression) {
		return reducer.reduceRequireObjectCoercible(requireObjectCoercible, (NodeWithValue) expression);
	}

	@Nonnull
	@Override
	public Node reduceTypeCoercionString(@Nonnull TypeCoercionString typeCoercionString, @Nonnull Node expression) {
		return reducer.reduceTypeCoercionString(typeCoercionString, (NodeWithValue) expression);
	}

	@Nonnull
	@Override
	public Node reduceTypeCoercionNumber(@Nonnull TypeCoercionNumber typeCoercionNumber, @Nonnull Node expression) {
		return reducer.reduceTypeCoercionNumber(typeCoercionNumber, (NodeWithValue) expression);
	}

	@Nonnull
	@Override
	public Node reduceTypeCoercionObject(@Nonnull TypeCoercionObject typeCoercionObject, @Nonnull Node expression) {
		return reducer.reduceTypeCoercionObject(typeCoercionObject, (NodeWithValue) expression);
	}

	@Nonnull
	@Override
	public Node reduceKeys(@Nonnull Keys keys, @Nonnull Node object) {
		return reducer.reduceKeys(keys, (NodeWithValue) object);
	}

	@Nonnull
	@Override
	public Node reduceTypeofGlobal(@Nonnull TypeofGlobal typeofGlobal) {
		return reducer.reduceTypeofGlobal(typeofGlobal);
	}

	@Nonnull
	@Override
	public Node reduceVoidOp(@Nonnull VoidOp voidOp, @Nonnull Node expression) {
		return reducer.reduceVoidOp(voidOp, (NodeWithValue) expression);
	}

	@Nonnull
	@Override
	public Node reduceDeleteGlobalProperty(@Nonnull DeleteGlobalProperty expression) {
		return reducer.reduceDeleteGlobalProperty(expression);
	}

	@Nonnull
	@Override
	public Node reduceLogic(@Nonnull Logic expression, @Nonnull Node left, @Nonnull Node right) {
		return reducer.reduceLogic(expression, (NodeWithValue) left, (NodeWithValue) right);
	}

	@Nonnull
	@Override
	public Node reduceHalt(@Nonnull Halt halt) {
		return reducer.reduceHalt(halt);
	}

	@Nonnull
	@Override
	public Node reduceIn(@Nonnull In expression, @Nonnull Node left, @Nonnull Node right) {
		return reducer.reduceIn(expression, (NodeWithValue) left, (NodeWithValue) right);
	}

	@Nonnull
	@Override
	public Node reduceDeleteProperty(@Nonnull DeleteProperty expression, @Nonnull Node object, @Nonnull Node fieldExpression) {
		return reducer.reduceDeleteProperty(expression, (NodeWithValue) object, (NodeWithValue) fieldExpression);
	}

	@Nonnull
	@Override
	public Node reduceTypeof(@Nonnull Typeof typeOf, @Nonnull Node expression) {
		return reducer.reduceTypeof(typeOf, (NodeWithValue) expression);
	}

	@Nonnull
	@Override
	public Node reduceBitwiseNot(@Nonnull BitwiseNot bitwiseNot, @Nonnull Node expression) {
		return reducer.reduceBitwiseNot(bitwiseNot, (NodeWithValue) expression);
	}

	@Nonnull
	@Override
	public Node reduceGlobalReference(@Nonnull GlobalReference ref) {
		return reducer.reduceGlobalReference(ref);
	}

	@Nonnull
	@Override
	public Node reduceTemporaryReference(@Nonnull TemporaryReference ref) {
		return reducer.reduceTemporaryReference(ref);
	}

	@Nonnull
	@Override
	public Node reduceLocalReference(@Nonnull LocalReference ref) {
		return reducer.reduceLocalReference(ref);
	}

	@Nonnull
	@Override
	public Node reduceMemberAccess(@Nonnull MemberAccess memberAccess, @Nonnull Node object, @Nonnull Node fieldExpression) {
		return reducer.reduceMemberAccess(memberAccess, (NodeWithValue) object, (NodeWithValue) fieldExpression);
	}

	@Nonnull
	@Override
	public Node reduceMemberAssignment(@Nonnull MemberAssignment memberAssignment, @Nonnull Node object, @Nonnull Node fieldExpression, @Nonnull Node value) {
		return reducer.reduceMemberAssignment(memberAssignment, (NodeWithValue) object, (NodeWithValue) fieldExpression, (NodeWithValue)value);
	}

	@Nonnull
	@Override
	public Node reduceThrow(@Nonnull Throw node, @Nonnull Node expression) {
		return reducer.reduceThrow(node, (NodeWithValue) expression);
	}

	@Nonnull
	@Override
	public Node reduceStaticValue(@Nonnull MemberAssignmentProperty.StaticValue node, @Nonnull Node value) {
		return reducer.reduceStaticValue(node, (NodeWithValue) value);
	}

	@Nonnull
	public Node reduceGetter(@Nonnull MemberAssignmentProperty.Getter node, @Nonnull Node value) {
		return reducer.reduceGetter(node, (LiteralFunction) value);
	}

	@Nonnull
	public Node reduceSetter(@Nonnull MemberAssignmentProperty.Setter node, @Nonnull Node value) {
		return reducer.reduceSetter(node, (LiteralFunction) value);
	}

	@Nonnull
	@Override
	public Node reduceMemberDefinition(@Nonnull MemberDefinition node, @Nonnull Node object, @Nonnull Node fieldExpression, @Nonnull Node property) {
		return reducer.reduceMemberDefinition(node, (NodeWithValue) object, (NodeWithValue) fieldExpression, (MemberAssignmentProperty) property);
	}

	@Nonnull
	@Override
	public Node reduceTryCatch(@Nonnull TryCatch node, @Nonnull Node tryBody, @Nonnull Node catchBody) {
		return reducer.reduceTryCatch(node, (Block) tryBody, Pair.of(node.catchBody.left, (Block) catchBody));
	}

	@Nonnull
	@Override
	public Node reduceTryFinally(@Nonnull TryFinally node, @Nonnull Node tryBody, @Nonnull Node finallyBody) {
		return reducer.reduceTryFinally(node, (Block) tryBody, (Block) finallyBody);
	}

	@Nonnull
	@Override
	public Node reduceSwitchStatement(@Nonnull SwitchStatement node, @Nonnull Node discriminant, @Nonnull ImmutableList<Pair<Node, Node>> preDefaultCases, @Nonnull Node defaultCase, @Nonnull ImmutableList<Pair<Node, Node>> postDefaultCases) {
		return reducer.reduceSwitchStatement(node, (LocalReference) discriminant, preDefaultCases.map(pair -> Pair.of((NodeWithValue) pair.left, (Block) pair.right)), (Block) defaultCase, postDefaultCases.map(pair -> Pair.of((NodeWithValue) pair.left, (Block) pair.right)));
	}

	@Nonnull
	@Override
	public Node reduceIfElse(@Nonnull IfElse ifElse, @Nonnull Node test, @Nonnull Node consequent, @Nonnull Node alternate) {
		return reducer.reduceIfElse(ifElse, (NodeWithValue) test, (Block) consequent, (Block) alternate);
	}

	@Nonnull
	@Override
	public Node reduceRelationalComparison(@Nonnull RelationalComparison relationalComparison, @Nonnull Node left, @Nonnull Node right) {
		return reducer.reduceRelationalComparison(relationalComparison, (NodeWithValue) left, (NodeWithValue) right);
	}

	@Nonnull
	@Override
	public Node reduceVariableAssignment(@Nonnull VariableAssignment variableAssignment, @Nonnull Node ref, @Nonnull Node value) {
		return reducer.reduceVariableAssignment(variableAssignment, ref instanceof LocalReference ? Either.right((LocalReference) ref) : Either.left((GlobalReference) ref), (NodeWithValue) value);
	}

	@Nonnull
	@Override
	public Node reduceVoid(@Nonnull Void _void) {
		return reducer.reduceVoid(_void);
	}

}
