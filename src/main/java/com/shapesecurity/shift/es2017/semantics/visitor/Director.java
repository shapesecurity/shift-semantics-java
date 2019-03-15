package com.shapesecurity.shift.es2017.semantics.visitor;

import com.shapesecurity.functional.Pair;
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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class Director<State> {

	@Nonnull
	private final Reducer<State> reducer;
	@Nonnull
	private final Map<Node, State> newNodes = new HashMap<>();

	public Director(@Nonnull Reducer<State> reducer) {
		this.reducer = reducer;
	}

	@Nonnull
	private State visitNode(@Nonnull Node old, @Nonnull Supplier<State> creator) {
		if (newNodes.containsKey(old)) {
			return newNodes.get(old);
		}
		State state = reducer.reduceAll(old, creator.get());
		newNodes.put(old, state);
		return state;
	}

	@Nonnull
	public State reduceBlock(@Nonnull Block block) {
		return visitNode(block, () -> reducer.reduceBlock(block, block.children.map(this::reduceNode)));
	}

	@Nonnull
	public State reduceBreak(@Nonnull Break _break) {
		return visitNode(_break, () -> reducer.reduceBreak(_break, this.reduceBreakTarget(_break.target)));
	}

	@Nonnull
	public State reduceBreakTarget(@Nonnull BreakTarget breakTarget) {
		return visitNode(breakTarget, () -> reducer.reduceBreakTarget(breakTarget));
	}

	@Nonnull
	public State reduceCall(@Nonnull Call call) {
		return visitNode(call, () -> reducer.reduceCall(
			call,
			reduceNodeWithValue(call.callee),
			call.arguments.map(this::reduceNodeWithValue)
		));
	}

	@Nonnull
	public State reduceNodeWithValue(@Nonnull NodeWithValue expression) {
		if (expression instanceof Call) {
			return reduceCall((Call) expression);
		} else if (expression instanceof BlockWithValue) {
			return reduceBlockWithValue((BlockWithValue) expression);
		} else if (expression instanceof FloatMath) {
			return reduceFloatMath((FloatMath) expression);
		} else if (expression instanceof IntMath) {
			return reduceIntMath((IntMath) expression);
		} else if (expression instanceof GlobalReference) {
			return reduceGlobalReference((GlobalReference) expression);
		} else if (expression instanceof LiteralNumber) {
			return reduceLiteralNumber((LiteralNumber) expression);
		} else if (expression instanceof LiteralBoolean) {
			return reduceLiteralBoolean((LiteralBoolean) expression);
		} else if (expression instanceof LiteralString) {
			return reduceLiteralString((LiteralString) expression);
		} else if (expression instanceof LiteralNull) {
			return reduceLiteralNull((LiteralNull) expression);
		} else if (expression instanceof LiteralFunction) {
			return reduceLiteralFunction((LiteralFunction) expression);
		} else if (expression instanceof LiteralEmptyObject) {
			return reduceLiteralEmptyObject((LiteralEmptyObject) expression);
		} else if (expression instanceof LiteralEmptyArray) {
			return reduceLiteralEmptyArray((LiteralEmptyArray) expression);
		} else if (expression instanceof LiteralRegExp) {
			return reduceLiteralRegExp((LiteralRegExp) expression);
		} else if (expression instanceof LiteralInfinity) {
			return reduceLiteralInfinity((LiteralInfinity) expression);
		} else if (expression instanceof LiteralSymbol) {
			return reduceLiteralSymbol((LiteralSymbol) expression);
		} else if (expression instanceof LiteralUndefined) {
			return reduceLiteralUndefined((LiteralUndefined) expression);
		} else if (expression instanceof TemporaryReference) {
			return reduceTemporaryReference((TemporaryReference) expression);
		} else if (expression instanceof LocalReference) {
			return reduceLocalReference((LocalReference) expression);
		} else if (expression instanceof MemberAccess) {
			return reduceMemberAccess((MemberAccess) expression);
		} else if (expression instanceof MemberAssignment) {
			return reduceMemberAssignment((MemberAssignment) expression);
		} else if (expression instanceof RelationalComparison) {
			return reduceRelationalComparison((RelationalComparison) expression);
		} else if (expression instanceof VariableAssignment) {
			return reduceVariableAssignment((VariableAssignment) expression);
		} else if (expression instanceof MemberCall) {
			return reduceMemberCall((MemberCall) expression);
		} else if (expression instanceof Not) {
			return reduceNot((Not) expression);
		} else if (expression instanceof InstanceOf) {
			return reduceInstanceOf((InstanceOf) expression);
		} else if (expression instanceof Negation) {
			return reduceNegation((Negation) expression);
		} else if (expression instanceof Equality) {
			return reduceEquality((Equality) expression);
		} else if (expression instanceof New) {
			return reduceNew((New) expression);
		} else if (expression instanceof This) {
			return reduceThis((This) expression);
		} else if (expression instanceof Halt) {
			return reduceHalt((Halt) expression);
		} else if (expression instanceof RequireObjectCoercible) {
			return reduceRequireObjectCoercible((RequireObjectCoercible) expression);
		} else if (expression instanceof TypeCoercionString) {
			return reduceTypeCoercionString((TypeCoercionString) expression);
		} else if (expression instanceof TypeCoercionNumber) {
			return reduceTypeCoercionNumber((TypeCoercionNumber) expression);
		} else if (expression instanceof TypeCoercionObject) {
			return reduceTypeCoercionObject((TypeCoercionObject) expression);
		} else if (expression instanceof Keys) {
			return reduceKeys((Keys) expression);
		} else if (expression instanceof TypeofGlobal) {
			return reduceTypeofGlobal((TypeofGlobal) expression);
		} else if (expression instanceof VoidOp) {
			return reduceVoidOp((VoidOp) expression);
		} else if (expression instanceof DeleteGlobalProperty) {
			return reduceDeleteGlobalProperty((DeleteGlobalProperty) expression);
		} else if (expression instanceof Logic) {
			return reduceLogic((Logic) expression);
		} else if (expression instanceof In) {
			return reduceIn((In) expression);
		} else if (expression instanceof DeleteProperty) {
			return reduceDeleteProperty((DeleteProperty) expression);
		} else if (expression instanceof Typeof) {
			return reduceTypeof((Typeof) expression);
		} else if (expression instanceof BitwiseNot) {
			return reduceBitwiseNot((BitwiseNot) expression);
		}
		throw new RuntimeException("Expression not implemented: " + expression.getClass().getSimpleName());
	}

	@Nonnull
	public State reduceLiteralUndefined(@Nonnull LiteralUndefined literalUndefined) {
		return visitNode(literalUndefined, () -> reducer.reduceLiteralUndefined(literalUndefined));
	}

	@Nonnull
	public State reduceLiteralSymbol(@Nonnull LiteralSymbol literalSymbol) {
		return visitNode(literalSymbol, () -> reducer.reduceLiteralSymbol(literalSymbol));
	}

	@Nonnull
	public State reduceLiteralInfinity(@Nonnull LiteralInfinity literalInfinity) {
		return visitNode(literalInfinity, () -> reducer.reduceLiteralInfinity(literalInfinity));
	}

	@Nonnull
	public State reduceLiteralRegExp(@Nonnull LiteralRegExp literalRegExp) {
		return visitNode(literalRegExp, () -> reducer.reduceLiteralRegExp(literalRegExp));
	}

	@Nonnull
	public State reduceLiteralEmptyArray(@Nonnull LiteralEmptyArray literalEmptyArray) {
		return visitNode(literalEmptyArray, () -> reducer.reduceLiteralEmptyArray(literalEmptyArray));
	}

	@Nonnull
	public State reduceLiteralEmptyObject(@Nonnull LiteralEmptyObject literalEmptyObject) {
		return visitNode(literalEmptyObject, () -> reducer.reduceLiteralEmptyObject(literalEmptyObject));
	}

	@Nonnull
	public State reduceLiteralFunction(@Nonnull LiteralFunction literalFunction) {
		return visitNode(literalFunction, () -> reducer.reduceLiteralFunction(literalFunction, this.reduceBlock(literalFunction.body)));
	}

	@Nonnull
	public State reduceReturn(@Nonnull Return _return) {
		return visitNode(_return, () -> reducer.reduceReturn(_return, _return.expression.map(this::reduceNodeWithValue)));
	}

	@Nonnull
	public State reduceReturnAfterFinallies(@Nonnull ReturnAfterFinallies returnAfterFinallies) {
		return visitNode(returnAfterFinallies, () -> reducer.reduceReturnAfterFinallies(returnAfterFinallies, returnAfterFinallies.savedValue.map(this::reduceLocalReference)));
	}

	@Nonnull
	public State reduceLiteralNull(@Nonnull LiteralNull literalNull) {
		return visitNode(literalNull, () -> reducer.reduceLiteralNull(literalNull));
	}

	@Nonnull
	public State reduceLiteralString(@Nonnull LiteralString literalString) {
		return visitNode(literalString, () -> reducer.reduceLiteralString(literalString));
	}

	@Nonnull
	public State reduceLiteralNumber(@Nonnull LiteralNumber literalNumber) {
		return visitNode(literalNumber, () -> reducer.reduceLiteralNumber(literalNumber));
	}

	@Nonnull
	public State reduceLiteralBoolean(@Nonnull LiteralBoolean literalBoolean) {
		return visitNode(literalBoolean, () -> reducer.reduceLiteralBoolean(literalBoolean));
	}

	@Nonnull
	public State reduceMemberCall(@Nonnull MemberCall memberCall) {
		return visitNode(memberCall, () -> reducer.reduceMemberCall(memberCall, this.reduceNodeWithValue(memberCall.object), this.reduceNodeWithValue(memberCall.fieldExpression), memberCall.arguments.map(this::reduceNodeWithValue)));
	}

	@Nonnull
	public State reduceNot(@Nonnull Not not) {
		return visitNode(not, () -> reducer.reduceNot(not, this.reduceNodeWithValue(not.expression)));
	}

	@Nonnull
	public State reduceBlockWithValue(@Nonnull BlockWithValue blockWithValue) {
		return visitNode(blockWithValue, () -> reducer.reduceBlockWithValue(blockWithValue, this.reduceBlock(blockWithValue.head), this.reduceNodeWithValue(blockWithValue.result)));
	}

	@Nonnull
	public State reduceFloatMath(@Nonnull FloatMath floatMath) {
		return visitNode(floatMath, () -> reducer.reduceFloatMath(floatMath, this.reduceNodeWithValue(floatMath.left), this.reduceNodeWithValue(floatMath.right)));
	}

	@Nonnull
	public State reduceIntMath(@Nonnull IntMath intMath) {
		return visitNode(intMath, () -> reducer.reduceIntMath(intMath, this.reduceNodeWithValue(intMath.left), this.reduceNodeWithValue(intMath.right)));
	}

	@Nonnull
	public State reduceInstanceOf(@Nonnull InstanceOf instanceOf) {
		return visitNode(instanceOf, () -> reducer.reduceInstanceOf(instanceOf, this.reduceNodeWithValue(instanceOf.left), this.reduceNodeWithValue(instanceOf.right)));
	}

	@Nonnull
	public State reduceNegation(@Nonnull Negation negation) {
		return visitNode(negation, () -> reducer.reduceNegation(negation, this.reduceNodeWithValue(negation.expression)));
	}

	@Nonnull
	public State reduceEquality(@Nonnull Equality equality) {
		return visitNode(equality, () -> reducer.reduceEquality(equality, this.reduceNodeWithValue(equality.left), this.reduceNodeWithValue(equality.right)));
	}

	@Nonnull
	public State reduceNew(@Nonnull New _new) {
		return visitNode(_new, () -> reducer.reduceNew(_new, this.reduceNodeWithValue(_new.callee), _new.arguments.map(this::reduceNodeWithValue)));
	}

	@Nonnull
	public State reduceThis(@Nonnull This expression) {
		return visitNode(expression, () -> reducer.reduceThis(expression));
	}

	@Nonnull
	public State reduceRequireObjectCoercible(@Nonnull RequireObjectCoercible requireObjectCoercible) {
		return visitNode(requireObjectCoercible, () -> reducer.reduceRequireObjectCoercible(requireObjectCoercible, this.reduceNodeWithValue(requireObjectCoercible.expression)));
	}

	@Nonnull
	public State reduceTypeCoercionString(@Nonnull TypeCoercionString typeCoercionString) {
		return visitNode(typeCoercionString, () -> reducer.reduceTypeCoercionString(typeCoercionString, this.reduceNodeWithValue(typeCoercionString.expression)));
	}

	@Nonnull
	public State reduceTypeCoercionNumber(@Nonnull TypeCoercionNumber typeCoercionNumber) {
		return visitNode(typeCoercionNumber, () -> reducer.reduceTypeCoercionNumber(typeCoercionNumber, this.reduceNodeWithValue(typeCoercionNumber.expression)));
	}

	@Nonnull
	public State reduceTypeCoercionObject(@Nonnull TypeCoercionObject typeCoercionObject) {
		return visitNode(typeCoercionObject, () -> reducer.reduceTypeCoercionObject(typeCoercionObject, this.reduceNodeWithValue(typeCoercionObject.expression)));
	}

	@Nonnull
	public State reduceKeys(@Nonnull Keys keys) {
		return visitNode(keys, () -> reducer.reduceKeys(keys, this.reduceNodeWithValue(keys._object)));
	}

	@Nonnull
	public State reduceTypeofGlobal(@Nonnull TypeofGlobal typeofGlobal) {
		return visitNode(typeofGlobal, () -> reducer.reduceTypeofGlobal(typeofGlobal));
	}

	@Nonnull
	public State reduceVoidOp(@Nonnull VoidOp voidOp) {
		return visitNode(voidOp, () -> reducer.reduceVoidOp(voidOp, this.reduceNodeWithValue(voidOp.expression)));
	}

	@Nonnull
	public State reduceDeleteGlobalProperty(@Nonnull DeleteGlobalProperty expression) {
		return visitNode(expression, () -> reducer.reduceDeleteGlobalProperty(expression));
	}

	@Nonnull
	public State reduceLogic(@Nonnull Logic expression) {
		return visitNode(expression, () -> reducer.reduceLogic(expression, this.reduceNodeWithValue(expression.left), this.reduceNodeWithValue(expression.right)));
	}

	@Nonnull
	public State reduceHalt(@Nonnull Halt halt) {
		return visitNode(halt, () -> reducer.reduceHalt(halt));
	}

	@Nonnull
	public State reduceIn(@Nonnull In expression) {
		return visitNode(expression, () -> reducer.reduceIn(expression, this.reduceNodeWithValue(expression.left), this.reduceNodeWithValue(expression.right)));
	}

	@Nonnull
	public State reduceDeleteProperty(@Nonnull DeleteProperty expression) {
		return visitNode(expression, () -> reducer.reduceDeleteProperty(expression, this.reduceNodeWithValue(expression.object), this.reduceNodeWithValue(expression.fieldExpression)));
	}

	@Nonnull
	public State reduceTypeof(@Nonnull Typeof typeOf) {
		return visitNode(typeOf, () -> reducer.reduceTypeof(typeOf, this.reduceNodeWithValue(typeOf.expression)));
	}

	@Nonnull
	public State reduceBitwiseNot(@Nonnull BitwiseNot bitwiseNot) {
		return visitNode(bitwiseNot, () -> reducer.reduceBitwiseNot(bitwiseNot, this.reduceNodeWithValue(bitwiseNot.expression)));
	}

	@Nonnull
	public State reduceGlobalReference(@Nonnull GlobalReference ref) {
		return visitNode(ref, () -> reducer.reduceGlobalReference(ref));
	}

	@Nonnull
	public State reduceTemporaryReference(@Nonnull TemporaryReference ref) {
		return visitNode(ref, () -> reducer.reduceTemporaryReference(ref));
	}

	@Nonnull
	public State reduceLocalReference(@Nonnull LocalReference ref) {
		return visitNode(ref, () -> reducer.reduceLocalReference(ref));
	}

	@Nonnull
	public State reduceMemberAccess(@Nonnull MemberAccess memberAccess) {
		return visitNode(memberAccess, () -> reducer.reduceMemberAccess(memberAccess, this.reduceNodeWithValue(memberAccess.object), this.reduceNodeWithValue(memberAccess.fieldExpression)));
	}

	@Nonnull
	public State reduceMemberAssignment(@Nonnull MemberAssignment memberAssignment) {
		return visitNode(memberAssignment, () -> reducer.reduceMemberAssignment(memberAssignment, this.reduceNodeWithValue(memberAssignment.object), this.reduceNodeWithValue(memberAssignment.fieldExpression), this.reduceNodeWithValue(memberAssignment.property.value)));
	}

	@Nonnull
	public State reduceNode(@Nonnull Node node) {
		if (node instanceof NodeWithValue) {
			return reduceNodeWithValue((NodeWithValue) node);
		} else if (node instanceof Block) {
			return reduceBlock((Block) node);
		} else if (node instanceof BreakTarget) {
			return reduceBreakTarget((BreakTarget) node);
		} else if (node instanceof Break) {
			return reduceBreak((Break) node);
		} else if (node instanceof IfElse) {
			return reduceIfElse((IfElse) node);
		} else if (node instanceof Loop) {
			return reduceLoop((Loop) node);
		} else if (node instanceof Void) {
			return reduceVoid((Void) node);
		} else if (node instanceof Throw) {
			return reduceThrow((Throw) node);
		} else if (node instanceof MemberDefinition) {
			return reduceMemberDefinition((MemberDefinition) node);
		} else if (node instanceof TryCatch) {
			return reduceTryCatch((TryCatch) node);
		} else if (node instanceof TryFinally) {
			return reduceTryFinally((TryFinally) node);
		} else if (node instanceof SwitchStatement) {
			return reduceSwitchStatement((SwitchStatement) node);
		} else if (node instanceof Return) {
			return reduceReturn((Return) node);
		} else if (node instanceof ReturnAfterFinallies) {
			return reduceReturnAfterFinallies((ReturnAfterFinallies) node);
		}
		throw new RuntimeException("Node not implemented: " + node.getClass().getSimpleName());
	}

	@Nonnull
	public State reduceThrow(@Nonnull Throw node) {
		return visitNode(node, () -> reducer.reduceThrow(node, this.reduceNodeWithValue(node.expression)));
	}

	@Nonnull
	public State reduceStaticValue(@Nonnull MemberAssignmentProperty.StaticValue node) {
		return visitNode(node, () -> reducer.reduceStaticValue(node, this.reduceNodeWithValue(node.value)));
	}

	@Nonnull
	public State reduceGetter(@Nonnull MemberAssignmentProperty.Getter node) {
		return visitNode(node, () -> reducer.reduceGetter(node, this.reduceNodeWithValue(node.value)));
	}

	@Nonnull
	public State reduceSetter(@Nonnull MemberAssignmentProperty.Setter node) {
		return visitNode(node, () -> reducer.reduceSetter(node, this.reduceNodeWithValue(node.value)));
	}

	@Nonnull
	public State reduceMemberDefinition(@Nonnull MemberDefinition node) {
		return visitNode(node, () -> reducer.reduceMemberDefinition(node, this.reduceNodeWithValue(node.object), this.reduceNodeWithValue(node.fieldExpression), this.reduceMemberAssignmentProperty(node.property)));
	}

	@Nonnull
	public State reduceMemberAssignmentProperty(@Nonnull MemberAssignmentProperty property) {
		if (property instanceof MemberAssignmentProperty.StaticValue) {
			return reduceStaticValue((MemberAssignmentProperty.StaticValue) property);
		} else if (property instanceof MemberAssignmentProperty.Getter) {
			return reduceGetter((MemberAssignmentProperty.Getter) property);
		} else if (property instanceof MemberAssignmentProperty.Setter) {
			return reduceSetter((MemberAssignmentProperty.Setter) property);
		}
		throw new RuntimeException("MemberAssignmentProperty not implemented: " + property.getClass().getSimpleName());
	}

	@Nonnull
	public State reduceTryCatch(@Nonnull TryCatch node) {
		return visitNode(node, () -> reducer.reduceTryCatch(node, this.reduceBlock(node.tryBody), this.reduceBlock(node.catchBody.right)));
	}

	@Nonnull
	public State reduceTryFinally(@Nonnull TryFinally node) {
		return visitNode(node, () -> reducer.reduceTryFinally(node, this.reduceBlock(node.tryBody), this.reduceBlock(node.finallyBody)));
	}

	@Nonnull
	public State reduceSwitchStatement(@Nonnull SwitchStatement node) {
		return visitNode(node, () -> reducer.reduceSwitchStatement(
			node,
			this.reduceLocalReference(node.discriminant),
			node.preDefaultCases.map(pair -> Pair.of(this.reduceNodeWithValue(pair.left), this.reduceBlock(pair.right))),
			this.reduceBlock(node.defaultCase),
			node.postDefaultCases.map(pair -> Pair.of(this.reduceNodeWithValue(pair.left), this.reduceBlock(pair.right)))
		));
	}

	@Nonnull
	public State reduceIfElse(@Nonnull IfElse ifElse) {
		return visitNode(ifElse, () -> reducer.reduceIfElse(ifElse, this.reduceNodeWithValue(ifElse.test), this.reduceBlock(ifElse.consequent), this.reduceBlock(ifElse.alternate)));
	}

	@Nonnull
	public State reduceLoop(@Nonnull Loop loop) {
		return visitNode(loop, () -> reducer.reduceLoop(loop, this.reduceBlock(loop.block)));
	}

	@Nonnull
	public State reduceRelationalComparison(@Nonnull RelationalComparison relationalComparison) {
		return visitNode(relationalComparison, () -> reducer.reduceRelationalComparison(relationalComparison, this.reduceNodeWithValue(relationalComparison.left), this.reduceNodeWithValue(relationalComparison.right)));
	}

	@Nonnull
	public State reduceVariableAssignment(@Nonnull VariableAssignment variableAssignment) {
		return visitNode(variableAssignment, () -> reducer.reduceVariableAssignment(variableAssignment, variableAssignment.ref.either(this::reduceGlobalReference, this::reduceLocalReference), this.reduceNodeWithValue(variableAssignment.value)));
	}

	@Nonnull
	public State reduceVoid(@Nonnull Void _void) {
		return visitNode(_void, () -> reducer.reduceVoid(_void));
	}

}
