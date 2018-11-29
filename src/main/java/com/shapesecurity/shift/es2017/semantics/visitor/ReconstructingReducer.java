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

import com.shapesecurity.functional.data.Either;
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

public class ReconstructingReducer implements FAlgebraNodeWithValue<NodeWithValue> {
	@Nonnull
	protected Block visitBlock(@Nonnull Block block) {
		return new Block(block.children.map(this::visitNode));
	}

	@Nonnull
	protected Node visitBreak(@Nonnull Break _break) {
		return new Break(_break.target, _break.broken); // TODO is not in fact cloning targets
	}

	@Nonnull
	protected BreakTarget visitBreakTarget(@Nonnull BreakTarget breakTarget) {
		return breakTarget;
	}

	@Nonnull
	protected NodeWithValue visitCall(@Nonnull Call call) {
		return new Call(
				visitNodeWithValue(call.callee),
				call.arguments.map(this::visitNodeWithValue)
		);
	}

	@Nonnull
	protected NodeWithValue visitNodeWithValue(@Nonnull NodeWithValue expression) {
		if (expression instanceof Call) {
			return visitCall((Call) expression);
		} else if (expression instanceof BlockWithValue) {
			return visitBlockWithValue((BlockWithValue) expression);
		} else if (expression instanceof FloatMath) {
			return visitFloatMath((FloatMath) expression);
		} else if (expression instanceof IntMath) {
			return visitIntMath((IntMath) expression);
		} else if (expression instanceof GlobalReference) {
			return visitGlobalReference((GlobalReference) expression);
		} else if (expression instanceof LiteralNumber) {
			return visitLiteralNumber((LiteralNumber) expression);
		} else if (expression instanceof LiteralBoolean) {
			return visitLiteralBoolean((LiteralBoolean) expression);
		} else if (expression instanceof LiteralString) {
			return visitLiteralString((LiteralString) expression);
		} else if (expression instanceof LiteralNull) {
			return visitLiteralNull((LiteralNull) expression);
		} else if (expression instanceof LiteralFunction) {
			return visitLiteralFunction((LiteralFunction) expression);
		} else if (expression instanceof LiteralEmptyObject) {
			return visitLiteralEmptyObject((LiteralEmptyObject) expression);
		} else if (expression instanceof LiteralEmptyArray) {
			return visitLiteralEmptyArray((LiteralEmptyArray) expression);
		} else if (expression instanceof LiteralRegExp) {
			return visitLiteralRegExp((LiteralRegExp) expression);
		} else if (expression instanceof LiteralInfinity) {
			return visitLiteralInfinity((LiteralInfinity) expression);
		} else if (expression instanceof LiteralSymbol) {
			return visitLiteralSymbol((LiteralSymbol) expression);
		} else if (expression instanceof LiteralUndefined) {
			return visitLiteralUndefined((LiteralUndefined) expression);
		} else if (expression instanceof TemporaryReference) {
			return visitTemporaryReference((TemporaryReference) expression);
		} else if (expression instanceof LocalReference) {
			return visitLocalReference((LocalReference) expression);
		} else if (expression instanceof MemberAccess) {
			return visitMemberAccess((MemberAccess) expression);
		} else if (expression instanceof MemberAssignment) {
			return visitMemberAssignment((MemberAssignment) expression);
		} else if (expression instanceof RelationalComparison) {
			return visitRelationalComparison((RelationalComparison) expression);
		} else if (expression instanceof VariableAssignment) {
			return visitVariableAssignment((VariableAssignment) expression);
		} else if (expression instanceof MemberCall) {
			return visitMemberCall((MemberCall) expression);
		} else if (expression instanceof Not) {
			return visitNot((Not) expression);
		} else if (expression instanceof InstanceOf) {
			return visitInstanceOf((InstanceOf) expression);
		} else if (expression instanceof Negation) {
			return visitNegation((Negation) expression);
		} else if (expression instanceof Equality) {
			return visitEquality((Equality) expression);
		} else if (expression instanceof New) {
			return visitNew((New) expression);
		} else if (expression instanceof This) {
			return visitThis((This) expression);
		} else if (expression instanceof Halt) {
			return visitHalt((Halt) expression);
		} else if (expression instanceof RequireObjectCoercible) {
			return visitRequireObjectCoercible((RequireObjectCoercible) expression);
		} else if (expression instanceof TypeCoercionString) {
			return visitTypeCoercionString((TypeCoercionString) expression);
		} else if (expression instanceof TypeCoercionNumber) {
			return visitTypeCoercionNumber((TypeCoercionNumber) expression);
		} else if (expression instanceof TypeCoercionObject) {
			return visitTypeCoercionObject((TypeCoercionObject) expression);
		} else if (expression instanceof Keys) {
			return visitKeys((Keys) expression);
		} else if (expression instanceof TypeofGlobal) {
			return visitTypeofGlobal((TypeofGlobal) expression);
		} else if (expression instanceof VoidOp) {
			return visitVoidOp((VoidOp) expression);
		} else if (expression instanceof DeleteGlobalProperty) {
			return visitDeleteGlobalProperty((DeleteGlobalProperty) expression);
		} else if (expression instanceof Logic) {
			return visitLogic((Logic) expression);
		} else if (expression instanceof In) {
			return visitIn((In) expression);
		} else if (expression instanceof DeleteProperty) {
			return visitDeleteProperty((DeleteProperty) expression);
		} else if (expression instanceof Typeof) {
			return visitTypeof((Typeof) expression);
		} else if (expression instanceof BitwiseNot) {
			return visitBitwiseNot((BitwiseNot) expression);
		}
		throw new RuntimeException("Expression not implemented: " + expression.getClass().getSimpleName());
	}

	private NodeWithValue visitLiteralUndefined(LiteralUndefined literalUndefined) {
		return LiteralUndefined.INSTANCE;
	}

	private NodeWithValue visitLiteralSymbol(LiteralSymbol literalSymbol) {
		return literalSymbol;
	}

	private NodeWithValue visitLiteralInfinity(LiteralInfinity literalInfinity) {
		return LiteralInfinity.INSTANCE;
	}

	private NodeWithValue visitLiteralRegExp(LiteralRegExp literalRegExp) {
		return new LiteralRegExp(literalRegExp.pattern, literalRegExp.global, literalRegExp.ignoreCase, literalRegExp.multiLine, literalRegExp.sticky, literalRegExp.unicode);
	}

	private NodeWithValue visitLiteralEmptyArray(LiteralEmptyArray literalEmptyArray) {
		return LiteralEmptyArray.INSTANCE;
	}

	private NodeWithValue visitLiteralEmptyObject(LiteralEmptyObject literalEmptyObject) {
		return LiteralEmptyObject.INSTANCE;
	}

	private NodeWithValue visitLiteralFunction(LiteralFunction literalFunction) {
		return new LiteralFunction(
				literalFunction.name,
				literalFunction.arguments,
				literalFunction.parameters,
				literalFunction.locals,
				literalFunction.captured,
				literalFunction.body,
				literalFunction.isStrict
		);
	}

	private NodeWithValue visitLiteralNull(LiteralNull literalNull) {
		return LiteralNull.INSTANCE;
	}

	private NodeWithValue visitLiteralString(LiteralString literalString) {
		return literalString;
	}

	private NodeWithValue visitLiteralNumber(LiteralNumber literalNumber) {
		return literalNumber;
	}

	private NodeWithValue visitLiteralBoolean(LiteralBoolean literalBoolean) {
		return literalBoolean;
	}

	@Nonnull
	protected NodeWithValue visitMemberCall(@Nonnull MemberCall memberCall) {
		return new MemberCall(
				visitNodeWithValue(memberCall.object),
				visitNodeWithValue(memberCall.fieldExpression),
				memberCall.arguments.map(this::visitNodeWithValue)
		);
	}

	@Nonnull
	protected NodeWithValue visitNot(@Nonnull Not not) {
		return new Not(visitNodeWithValue(not.expression));
	}

	@Nonnull
	protected NodeWithValue visitBlockWithValue(@Nonnull BlockWithValue blockWithValue) {
		return new BlockWithValue(visitBlock(blockWithValue.head), visitNodeWithValue(blockWithValue.result));
	}

	@Nonnull
	protected NodeWithValue visitFloatMath(@Nonnull FloatMath floatMath) {
		return new FloatMath(floatMath.operator, visitNodeWithValue(floatMath.left), visitNodeWithValue(floatMath.right));
	}

	@Nonnull
	protected NodeWithValue visitIntMath(@Nonnull IntMath intMath) {
		return new IntMath(intMath.operator, visitNodeWithValue(intMath.left), visitNodeWithValue(intMath.right));
	}

	@Nonnull
	protected NodeWithValue visitInstanceOf(@Nonnull InstanceOf instanceOf) {
		return new InstanceOf(instanceOf.left, instanceOf.right);
	}

	@Nonnull
	protected NodeWithValue visitNegation(@Nonnull Negation negation) {
		return new Negation(negation.expression);
	}

	@Nonnull
	protected NodeWithValue visitEquality(@Nonnull Equality equality) {
		return new Equality(equality.operator, equality.left, equality.right);
	}

	@Nonnull
	protected NodeWithValue visitNew(@Nonnull New expression) {
		return new New(expression.callee, expression.arguments);
	}

	@Nonnull
	protected NodeWithValue visitThis(@Nonnull This expression) {
		return new This(expression.strict);
	}

	@Nonnull
	protected NodeWithValue visitRequireObjectCoercible(@Nonnull RequireObjectCoercible requireObjectCoercible) {
		return new RequireObjectCoercible(requireObjectCoercible.expression);
	}

	@Nonnull
	protected NodeWithValue visitTypeCoercionString(@Nonnull TypeCoercionString typeCoercionString) {
		return new TypeCoercionString(typeCoercionString.expression);
	}

	@Nonnull
	protected NodeWithValue visitTypeCoercionNumber(@Nonnull TypeCoercionNumber typeCoercionNumber) {
		return new TypeCoercionNumber(typeCoercionNumber.expression);
	}

	@Nonnull
	protected NodeWithValue visitTypeCoercionObject(@Nonnull TypeCoercionObject typeCoercionObject) {
		return new TypeCoercionObject(typeCoercionObject.expression);
	}

	@Nonnull
	protected NodeWithValue visitKeys(@Nonnull Keys keys) {
		return new Keys(keys._object);
	}

	@Nonnull
	protected NodeWithValue visitTypeofGlobal(@Nonnull TypeofGlobal typeofGlobal) {
		return new TypeofGlobal(typeofGlobal.which);
	}

	@Nonnull
	protected NodeWithValue visitVoidOp(@Nonnull VoidOp voidOp) {
		return new VoidOp(voidOp.expression);
	}

	@Nonnull
	protected NodeWithValue visitDeleteGlobalProperty(@Nonnull DeleteGlobalProperty expression) {
		return new DeleteGlobalProperty(expression.which);
	}

	@Nonnull
	protected NodeWithValue visitLogic(@Nonnull Logic expression) {
		return new Logic(expression.operator, expression.left, expression.right);
	}

	@Nonnull
	protected NodeWithValue visitHalt(@Nonnull Halt halt) {
		return Halt.INSTANCE;
	}

	@Nonnull
	protected NodeWithValue visitIn(@Nonnull In expression) {
		return new In(expression.left, expression.right);
	}

	@Nonnull
	protected NodeWithValue visitDeleteProperty(@Nonnull DeleteProperty expression) {
		return new DeleteProperty(expression.object, expression.fieldExpression, expression.strict);
	}

	@Nonnull
	protected NodeWithValue visitTypeof(@Nonnull Typeof expression) {
		return new Typeof(expression.expression);
	}

	@Nonnull
	protected NodeWithValue visitBitwiseNot(@Nonnull BitwiseNot expression) {
		return new BitwiseNot(expression.expression);
	}

	@Nonnull
	protected Either<GlobalReference, LocalReference> visitReference(@Nonnull Either<GlobalReference, LocalReference> e) {
		return e.map(this::visitGlobalReference, this::visitLocalReference);
	}

	@Nonnull
	protected GlobalReference visitGlobalReference(@Nonnull GlobalReference ref) {
		return new GlobalReference(ref.name);
	}

	@Nonnull
	protected LocalReference visitTemporaryReference(@Nonnull TemporaryReference ref) {
		return new TemporaryReference(ref.variable);
	}

	@Nonnull
	protected LocalReference visitLocalReference(@Nonnull LocalReference ref) {
		return new LocalReference(ref.variable);
	}

	@Nonnull
	protected NodeWithValue visitMemberAccess(@Nonnull MemberAccess memberAccess) {
		return new MemberAccess(visitNodeWithValue(memberAccess.object), visitNodeWithValue(memberAccess.fieldExpression));
	}

	@Nonnull
	protected NodeWithValue visitMemberAssignment(@Nonnull MemberAssignment memberAssignment) {
		if (memberAssignment.property instanceof MemberAssignmentProperty.StaticValue) {
			return new MemberAssignment(visitNodeWithValue(memberAssignment.object), visitNodeWithValue(memberAssignment.fieldExpression), visitNodeWithValue(((MemberAssignmentProperty.StaticValue) memberAssignment.property).value), memberAssignment.strict);
		}
		throw new RuntimeException("MemberAssignmentProperty not implemented: " + memberAssignment.property.getClass().getSimpleName());
	}

	@Nonnull
	protected Node visitNode(@Nonnull Node node) {
		if (node instanceof NodeWithValue) {
			return visitNodeWithValue((NodeWithValue) node);
		} else if (node instanceof Block) {
			return visitBlock((Block) node);
		} else if (node instanceof BreakTarget) {
			return visitBreakTarget((BreakTarget) node);
		} else if (node instanceof Break) {
			return visitBreak((Break) node);
		} else if (node instanceof IfElse) {
			return visitIfElse((IfElse) node);
		} else if (node instanceof Loop) {
			return new Loop(visitBlock(((Loop) node).block));
		} else if (node instanceof com.shapesecurity.shift.es2017.semantics.asg.Void) {
			return visitVoid((com.shapesecurity.shift.es2017.semantics.asg.Void) node);
		} else if (node instanceof Throw) {
			return visitThrow((Throw) node);
		} else if (node instanceof MemberDefinition) {
			return visitMemberDefinition((MemberDefinition) node);
		} else if (node instanceof TryCatch) {
			return visitTryCatch((TryCatch) node);
		} else if (node instanceof TryFinally) {
			return visitTryFinally((TryFinally) node);
		} else if (node instanceof SwitchStatement) {
			return visitSwitchStatement((SwitchStatement) node);
		}
		throw new RuntimeException("Node not implemented: " + node.getClass().getSimpleName());
	}

	@Nonnull
	protected Node visitThrow(@Nonnull Throw node) {
		return new Throw(node.expression);
	}

	@Nonnull
	protected Node visitMemberDefinition(@Nonnull MemberDefinition node) {
		return new MemberDefinition(node.object, node.fieldExpression, node.property);
	}

	@Nonnull
	protected Node visitTryCatch(@Nonnull TryCatch node) {
		return new TryCatch(node.tryBody, node.catchBody);
	}

	@Nonnull
	protected Node visitTryFinally(@Nonnull TryFinally node) {
		return new TryFinally(node.tryBody, node.finallyBody);
	}

	@Nonnull
	protected Node visitSwitchStatement(@Nonnull SwitchStatement node) {
		return new SwitchStatement(node.discriminant, node.preDefaultCases, node.defaultCase, node.postDefaultCases);
	}

	@Nonnull
	protected Node visitIfElse(@Nonnull IfElse ifElse) {
		return new IfElse(visitNodeWithValue(ifElse.test), visitBlock(ifElse.consequent), visitBlock(ifElse.alternate));
	}

	@Nonnull
	protected NodeWithValue visitRelationalComparison(@Nonnull RelationalComparison relationalComparison) {
		return new RelationalComparison(relationalComparison.operator, visitNodeWithValue(relationalComparison.left), visitNodeWithValue(relationalComparison.right));
	}

	@Nonnull
	protected NodeWithValue visitVariableAssignment(@Nonnull VariableAssignment variableAssignment) {
		return new VariableAssignment(visitReference(variableAssignment.ref), visitNodeWithValue(variableAssignment.value), variableAssignment.strict);
	}

	@Nonnull
	protected Node visitVoid(@Nonnull com.shapesecurity.shift.es2017.semantics.asg.Void _void) {
		return Void.INSTANCE;
	}

	@Nonnull
	public Node visit(@Nonnull Node node) {
		return visitNode(node);
	}


	@Nonnull
	@Override
	public NodeWithValue apply(BlockWithValue blockWithValue) {
		return visitBlockWithValue(blockWithValue);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(Call call) {
		return visitCall(call);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(DeleteGlobalProperty deleteGlobalProperty) {
		return visitDeleteGlobalProperty(deleteGlobalProperty);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(DeleteProperty deleteProperty) {
		return visitDeleteProperty(deleteProperty);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(GlobalReference globalReference) {
		return visitGlobalReference(globalReference);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(Halt halt) {
		return visitHalt(halt);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(Keys keys) {
		return visitKeys(keys);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(LiteralBoolean literalBoolean) {
		return visitLiteralBoolean(literalBoolean);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(LiteralEmptyArray literalEmptyArray) {
		return visitLiteralEmptyArray(literalEmptyArray);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(LiteralEmptyObject literalEmptyObject) {
		return visitLiteralEmptyObject(literalEmptyObject);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(LiteralFunction literalFunction) {
		return visitLiteralFunction(literalFunction);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(LiteralInfinity literalInfinity) {
		return visitLiteralInfinity(literalInfinity);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(LiteralNull literalNull) {
		return visitLiteralNull(literalNull);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(LiteralNumber literalNumber) {
		return visitLiteralNumber(literalNumber);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(LiteralRegExp literalRegExp) {
		return visitLiteralRegExp(literalRegExp);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(LiteralString literalString) {
		return visitLiteralString(literalString);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(LiteralSymbol literalSymbol) {
		return visitLiteralSymbol(literalSymbol);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(LiteralUndefined literalUndefined) {
		return visitLiteralUndefined(literalUndefined);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(MemberAccess memberAccess) {
		return visitMemberAccess(memberAccess);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(MemberAssignment memberAssignment) {
		return visitMemberAssignment(memberAssignment);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(New new_) {
		return visitNew(new_);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(RequireObjectCoercible requireObjectCoercible) {
		return visitRequireObjectCoercible(requireObjectCoercible);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(LocalReference localReference) {
		return visitLocalReference(localReference);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(TemporaryReference temporaryReference) {
		return visitTemporaryReference(temporaryReference);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(This this_) {
		return visitThis(this_);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(TypeCoercionNumber typeCoercionNumber) {
		return visitTypeCoercionNumber(typeCoercionNumber);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(TypeCoercionString typeCoercionString) {
		return visitTypeCoercionString(typeCoercionString);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(TypeofGlobal typeofGlobal) {
		return visitTypeofGlobal(typeofGlobal);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(VariableAssignment variableAssignment) {
		return visitVariableAssignment(variableAssignment);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(Equality equality) {
		return visitEquality(equality);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(FloatMath floatMath) {
		return visitFloatMath(floatMath);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(In in_) {
		return visitIn(in_);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(InstanceOf instanceOf) {
		return visitInstanceOf(instanceOf);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(IntMath intMath) {
		return visitIntMath(intMath);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(Logic logic) {
		return visitLogic(logic);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(RelationalComparison relationalComparison) {
		return visitRelationalComparison(relationalComparison);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(BitwiseNot bitwiseNot) {
		return visitBitwiseNot(bitwiseNot);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(Negation negation) {
		return visitNegation(negation);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(Not not) {
		return visitNot(not);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(Typeof typeof) {
		return visitTypeof(typeof);
	}

	@Nonnull
	@Override
	public NodeWithValue apply(VoidOp voidOp) {
		return visitVoidOp(voidOp);
	}
}
