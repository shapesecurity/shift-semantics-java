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
package com.shapesecurity.shift.semantics.visitor;

import com.shapesecurity.functional.data.Either;
import com.shapesecurity.shift.semantics.asg.BinaryOperation.Equality;
import com.shapesecurity.shift.semantics.asg.BinaryOperation.FloatMath;
import com.shapesecurity.shift.semantics.asg.BinaryOperation.In;
import com.shapesecurity.shift.semantics.asg.BinaryOperation.InstanceOf;
import com.shapesecurity.shift.semantics.asg.BinaryOperation.IntMath;
import com.shapesecurity.shift.semantics.asg.BinaryOperation.Logic;
import com.shapesecurity.shift.semantics.asg.BinaryOperation.RelationalComparison;
import com.shapesecurity.shift.semantics.asg.Block;
import com.shapesecurity.shift.semantics.asg.BlockWithValue;
import com.shapesecurity.shift.semantics.asg.Break;
import com.shapesecurity.shift.semantics.asg.BreakTarget;
import com.shapesecurity.shift.semantics.asg.Call;
import com.shapesecurity.shift.semantics.asg.DeleteGlobalProperty;
import com.shapesecurity.shift.semantics.asg.DeleteProperty;
import com.shapesecurity.shift.semantics.asg.GlobalReference;
import com.shapesecurity.shift.semantics.asg.IfElse;
import com.shapesecurity.shift.semantics.asg.Keys;
import com.shapesecurity.shift.semantics.asg.Literal;
import com.shapesecurity.shift.semantics.asg.LiteralBoolean;
import com.shapesecurity.shift.semantics.asg.LiteralEmptyArray;
import com.shapesecurity.shift.semantics.asg.LiteralEmptyObject;
import com.shapesecurity.shift.semantics.asg.LiteralFunction;
import com.shapesecurity.shift.semantics.asg.LiteralInfinity;
import com.shapesecurity.shift.semantics.asg.LiteralNull;
import com.shapesecurity.shift.semantics.asg.LiteralNumber;
import com.shapesecurity.shift.semantics.asg.LiteralRegExp;
import com.shapesecurity.shift.semantics.asg.LiteralString;
import com.shapesecurity.shift.semantics.asg.LocalReference;
import com.shapesecurity.shift.semantics.asg.Loop;
import com.shapesecurity.shift.semantics.asg.MemberAccess;
import com.shapesecurity.shift.semantics.asg.MemberAssignment;
import com.shapesecurity.shift.semantics.asg.MemberAssignmentProperty;
import com.shapesecurity.shift.semantics.asg.MemberDefinition;
import com.shapesecurity.shift.semantics.asg.New;
import com.shapesecurity.shift.semantics.asg.Node;
import com.shapesecurity.shift.semantics.asg.NodeWithValue;
import com.shapesecurity.shift.semantics.asg.RequireObjectCoercible;
import com.shapesecurity.shift.semantics.asg.SwitchStatement;
import com.shapesecurity.shift.semantics.asg.TemporaryReference;
import com.shapesecurity.shift.semantics.asg.This;
import com.shapesecurity.shift.semantics.asg.Throw;
import com.shapesecurity.shift.semantics.asg.TryCatchFinally;
import com.shapesecurity.shift.semantics.asg.TypeCoercionNumber;
import com.shapesecurity.shift.semantics.asg.TypeCoercionString;
import com.shapesecurity.shift.semantics.asg.TypeofGlobal;
import com.shapesecurity.shift.semantics.asg.UnaryOperation.BitwiseNot;
import com.shapesecurity.shift.semantics.asg.UnaryOperation.Negation;
import com.shapesecurity.shift.semantics.asg.UnaryOperation.Not;
import com.shapesecurity.shift.semantics.asg.UnaryOperation.Typeof;
import com.shapesecurity.shift.semantics.asg.UnaryOperation.VoidOp;
import com.shapesecurity.shift.semantics.asg.VariableAssignment;
import com.shapesecurity.shift.semantics.asg.Void;

import javax.annotation.Nonnull;

public class ReconstructingReducer {
    @Nonnull
    protected Block visitBlock(@Nonnull Block block) {
        return new Block(block.children.map(this::visitNode));
    }

    @Nonnull
    protected Node visitBreak(@Nonnull Break _break) {
        return new Break(_break.target, _break.finalliesBroken); // TODO is not in fact cloning targets
    }

    @Nonnull
    protected BreakTarget visitBreakTarget(@Nonnull BreakTarget breakTarget) {
        return breakTarget;
    }

    @Nonnull
    protected NodeWithValue visitCall(@Nonnull Call call) {
        return new Call(
                call.context.map(this::visitLocalReference),
                visitExpression(call.callee),
                call.arguments.map(this::visitExpression)
        );
    }

    @Nonnull
    protected NodeWithValue visitExpression(@Nonnull NodeWithValue expression) {
        if (expression instanceof Call) {
            return visitCall((Call) expression);
        } else if (expression instanceof BlockWithValue) {
            return visitExpressionBlock((BlockWithValue) expression);
        } else if (expression instanceof FloatMath) {
            return visitFloatMath((FloatMath) expression);
        } else if (expression instanceof IntMath) {
            return visitIntMath((IntMath) expression);
        } else if (expression instanceof GlobalReference) {
            return visitGlobalReference((GlobalReference) expression);
        } else if (expression instanceof Literal) {
            return visitLiteral((Literal) expression);
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
        } else if (expression instanceof RequireObjectCoercible) {
            return visitRequireObjectCoercible((RequireObjectCoercible) expression);
        } else if (expression instanceof TypeCoercionString) {
            return visitTypeCoercionString((TypeCoercionString) expression);
        } else if (expression instanceof TypeCoercionNumber) {
            return visitTypeCoercionNumber((TypeCoercionNumber) expression);
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

    @Nonnull
    protected NodeWithValue visitNot(@Nonnull Not not) {
        return new Not(visitExpression(not.expression));
    }

    @Nonnull
    protected BlockWithValue visitExpressionBlock(@Nonnull BlockWithValue blockWithValue) {
        return new BlockWithValue(visitBlock(blockWithValue.head), visitExpression(blockWithValue.result));
    }

    @Nonnull
    protected NodeWithValue visitFloatMath(@Nonnull FloatMath floatMath) {
        return new FloatMath(floatMath.operator, visitExpression(floatMath.left), visitExpression(floatMath.right));
    }

    @Nonnull
    protected NodeWithValue visitIntMath(@Nonnull IntMath intMath) {
        return new IntMath(intMath.operator, visitExpression(intMath.left), visitExpression(intMath.right));
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
    private NodeWithValue visitLiteral(@Nonnull Literal literal) {
        if (literal instanceof LiteralNumber) {
            return new LiteralNumber(((LiteralNumber) literal).value);
        } else if (literal instanceof LiteralBoolean) {
            return new LiteralBoolean(((LiteralBoolean) literal).value);
        } else if (literal instanceof LiteralString) {
            return new LiteralString(((LiteralString) literal).value);
        } else if (literal instanceof LiteralNull) {
            return LiteralNull.INSTANCE;
        } else if (literal instanceof LiteralFunction) {
            return new LiteralFunction(((LiteralFunction) literal).name,
                    ((LiteralFunction) literal).arguments,
                    ((LiteralFunction) literal).parameters,
                    ((LiteralFunction) literal).locals,
                    ((LiteralFunction) literal).captured,
                    ((LiteralFunction) literal).body,
                    ((LiteralFunction) literal).isStrict);
        } else if (literal instanceof LiteralEmptyObject) {
            return LiteralEmptyObject.INSTANCE;
        } else if (literal instanceof LiteralEmptyArray) {
            return LiteralEmptyArray.INSTANCE;
        } else if (literal instanceof LiteralRegExp) {
            return new LiteralRegExp(((LiteralRegExp) literal).pattern, ((LiteralRegExp) literal).flags);
        } else if (literal instanceof LiteralInfinity) {
            return LiteralInfinity.INSTANCE;
        }
        throw new RuntimeException("Literal not implemented: " + literal.getClass().getSimpleName());
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
        return new MemberAccess(visitExpression(memberAccess.object), visitExpression(memberAccess.fieldExpression));
    }

    @Nonnull
    protected NodeWithValue visitMemberAssignment(@Nonnull MemberAssignment memberAssignment) {
        if (memberAssignment.property instanceof MemberAssignmentProperty.StaticValue) {
            return new MemberAssignment(visitExpression(memberAssignment.object), visitExpression(memberAssignment.fieldExpression), visitExpression(((MemberAssignmentProperty.StaticValue) memberAssignment.property).value), memberAssignment.strict);
        }
        throw new RuntimeException("MemberAssignmentProperty not implemented: " + memberAssignment.property.getClass().getSimpleName());
    }

    @Nonnull
    protected Node visitNode(@Nonnull Node node) {
        if (node instanceof Block) {
            return visitBlock((Block) node);
        } else if (node instanceof BreakTarget) {
            return visitBreakTarget((BreakTarget) node);
        } else if (node instanceof Break) {
            return visitBreak((Break) node);
        } else if (node instanceof NodeWithValue) {
            return visitExpression((NodeWithValue) node);
        } else if (node instanceof IfElse) {
            return visitIfElse((IfElse) node);
        } else if (node instanceof Loop) {
            return new Loop(visitBlock(((Loop) node).block));
        } else if (node instanceof Void) {
            return visitVoid((Void) node);
        } else if (node instanceof Throw) {
            return visitThrow((Throw) node);
        } else if (node instanceof MemberDefinition) {
            return visitMemberDefinition((MemberDefinition) node);
        } else if (node instanceof TryCatchFinally) {
            return visitTryCatchFinally((TryCatchFinally) node);
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
    protected Node visitTryCatchFinally(@Nonnull TryCatchFinally node) {
        return new TryCatchFinally(node.tryBody, node.catchBody, node.finallyBody);
    }

    @Nonnull
    protected Node visitSwitchStatement(@Nonnull SwitchStatement node) {
        return new SwitchStatement(node.discriminant, node.preDefaultCases, node.defaultCase, node.postDefaultCases);
    }

    @Nonnull
    protected Node visitIfElse(@Nonnull IfElse ifElse) {
        return new IfElse(visitExpression(ifElse.test), visitBlock(ifElse.consequent), visitBlock(ifElse.alternate));
    }

    @Nonnull
    protected NodeWithValue visitRelationalComparison(@Nonnull RelationalComparison relationalComparison) {
        return new RelationalComparison(relationalComparison.operator, visitExpression(relationalComparison.left), visitExpression(relationalComparison.right));
    }

    @Nonnull
    protected NodeWithValue visitVariableAssignment(@Nonnull VariableAssignment variableAssignment) {
        return new VariableAssignment(visitReference(variableAssignment.ref), visitExpression(variableAssignment.value), variableAssignment.strict);
    }

    @Nonnull
    protected Node visitVoid(@Nonnull Void _void) {
        return Void.INSTANCE;
    }

    @Nonnull
    public Node visit(@Nonnull Node node) {
        return visitNode(node);
    }
}
