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

import org.jetbrains.annotations.NotNull;

public class ReconstructingReducer {
    @NotNull
    protected Block visitBlock(Block block) {
        return new Block(block.children.map(this::visitNode));
    }

    @NotNull
    protected Break visitBreak(Break _break) {
        return new Break(_break.target, _break.finalliesBroken); // TODO is not in fact cloning targets
    }

    @NotNull
    protected BreakTarget visitBreakTarget(BreakTarget breakTarget) {
        return breakTarget; // TODO is not in fact cloning targets
    }

    @NotNull
    protected Call visitCall(Call call) {
        return new Call(
                call.context.map(this::visitLocalReference),
                visitExpression(call.callee),
                call.arguments.map(this::visitExpression)
        );
    }

    @NotNull
    protected NodeWithValue visitExpression(NodeWithValue expression) {
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

    @NotNull
    protected NodeWithValue visitNot(@NotNull Not not) {
        return new Not(visitExpression(not.expression));
    }

    @NotNull
    protected BlockWithValue visitExpressionBlock(@NotNull BlockWithValue blockWithValue) {
        return new BlockWithValue(visitBlock(blockWithValue.head), visitExpression(blockWithValue.result));
    }

    @NotNull
    protected FloatMath visitFloatMath(@NotNull FloatMath floatMath) {
        return new FloatMath(floatMath.operator, visitExpression(floatMath.left), visitExpression(floatMath.right));
    }

    @NotNull
    protected IntMath visitIntMath(@NotNull IntMath intMath) {
        return new IntMath(intMath.operator, visitExpression(intMath.left), visitExpression(intMath.right));
    }

    @NotNull
    protected InstanceOf visitInstanceOf(@NotNull InstanceOf instanceOf) {
        return new InstanceOf(instanceOf.left, instanceOf.right);
    }

    @NotNull
    protected Negation visitNegation(@NotNull Negation negation) {
        return new Negation(negation.expression);
    }

    @NotNull
    protected Equality visitEquality(@NotNull Equality equality) {
        return new Equality(equality.operator, equality.left, equality.right);
    }

    @NotNull
    protected New visitNew(@NotNull New expression) {
        return new New(expression.callee, expression.arguments);
    }

    @NotNull
    protected This visitThis(@NotNull This expression) {
        return new This(expression.strict);
    }

    @NotNull
    protected RequireObjectCoercible visitRequireObjectCoercible(@NotNull RequireObjectCoercible expression) {
        return new RequireObjectCoercible(expression);
    }

    @NotNull
    protected TypeCoercionString visitTypeCoercionString(@NotNull TypeCoercionString expression) {
        return new TypeCoercionString(expression);
    }

    @NotNull
    protected TypeCoercionNumber visitTypeCoercionNumber(@NotNull TypeCoercionNumber expression) {
        return new TypeCoercionNumber(expression);
    }

    @NotNull
    protected Keys visitKeys(@NotNull Keys keys) {
        return new Keys(keys);
    }

    @NotNull
    protected TypeofGlobal visitTypeofGlobal(@NotNull TypeofGlobal typeofGlobal) {
        return new TypeofGlobal(typeofGlobal.which);
    }

    @NotNull
    protected VoidOp visitVoidOp(@NotNull VoidOp voidOp) {
        return new VoidOp(voidOp.expression);
    }

    @NotNull
    protected DeleteGlobalProperty visitDeleteGlobalProperty(@NotNull DeleteGlobalProperty expression) {
        return new DeleteGlobalProperty(expression.which);
    }

    @NotNull
    protected Logic visitLogic(@NotNull Logic expression) {
        return new Logic(expression.operator, expression.left, expression.right);
    }

    @NotNull
    protected In visitIn(@NotNull In expression) {
        return new In(expression.left, expression.right);
    }

    @NotNull
    protected DeleteProperty visitDeleteProperty(@NotNull DeleteProperty expression) {
        return new DeleteProperty(expression.object, expression.fieldExpression, expression.strict);
    }

    @NotNull
    protected Typeof visitTypeof(@NotNull Typeof expression) {
        return new Typeof(expression.expression);
    }

    @NotNull
    protected BitwiseNot visitBitwiseNot(@NotNull BitwiseNot expression) {
        return new BitwiseNot(expression.expression);
    }

    @NotNull
    protected Literal visitLiteral(@NotNull Literal literal) {
        if (literal instanceof LiteralNumber) {
            return new LiteralNumber(((LiteralNumber) literal).value);
        } else if (literal instanceof LiteralBoolean) {
            return new LiteralBoolean(((LiteralBoolean) literal).value);
        } else if (literal instanceof LiteralString) {
            return new LiteralString(((LiteralString) literal).value);
        } else if (literal instanceof LiteralNull) {
            return new LiteralNull();
        } else if (literal instanceof LiteralFunction) {
            return new LiteralFunction(((LiteralFunction) literal).name,
                    ((LiteralFunction) literal).arguments,
                    ((LiteralFunction) literal).parameters,
                    ((LiteralFunction) literal).locals,
                    ((LiteralFunction) literal).captured,
                    ((LiteralFunction) literal).body,
                    ((LiteralFunction) literal).isStrict);
        } else if (literal instanceof LiteralEmptyObject) {
            return new LiteralEmptyObject();
        } else if (literal instanceof LiteralEmptyArray) {
            return new LiteralEmptyArray();
        } else if (literal instanceof LiteralRegExp) {
            return new LiteralRegExp(((LiteralRegExp) literal).pattern, ((LiteralRegExp) literal).flags);
        } else if (literal instanceof LiteralInfinity) {
            return new LiteralInfinity();
        }
        throw new RuntimeException("Literal not implemented: " + literal.getClass().getSimpleName());
    }

    @NotNull
    protected Either<GlobalReference, LocalReference> visitReference(Either<GlobalReference, LocalReference> e) {
        return e.map(this::visitGlobalReference, this::visitLocalReference);
    }

    @NotNull
    protected GlobalReference visitGlobalReference(GlobalReference ref) {
        return new GlobalReference(ref.name);
    }

    @NotNull
    protected LocalReference visitLocalReference(LocalReference ref) {
        return new LocalReference(ref.variable);
    }

    @NotNull
    protected MemberAccess visitMemberAccess(MemberAccess memberAccess) {
        return new MemberAccess(visitExpression(memberAccess.object), visitExpression(memberAccess.fieldExpression));
    }

    @NotNull
    protected MemberAssignment visitMemberAssignment(MemberAssignment memberAssignment) {
        if (memberAssignment.property instanceof MemberAssignmentProperty.StaticValue) {
            return new MemberAssignment(visitExpression(memberAssignment.object), visitExpression(memberAssignment.fieldExpression), visitExpression(((MemberAssignmentProperty.StaticValue) memberAssignment.property).value), memberAssignment.strict);
        }
        throw new RuntimeException("MemberAssignmentProperty not implemented: " + memberAssignment.property.getClass().getSimpleName());
    }

    @NotNull
    protected Node visitNode(Node node) {
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

    @NotNull
    protected Node visitThrow(@NotNull Throw node) {
        return new Throw(node.expression);
    }

    @NotNull
    protected Node visitMemberDefinition(@NotNull MemberDefinition node) {
        return new MemberDefinition(node.object, node.fieldExpression, node.property);
    }

    @NotNull
    protected Node visitTryCatchFinally(@NotNull TryCatchFinally node) {
        return new TryCatchFinally(node.tryBody, node.catchBody, node.finallyBody);
    }

    @NotNull
    protected Node visitSwitchStatement(@NotNull SwitchStatement node) {
        return new SwitchStatement(node.discriminant, node.preDefaultCases, node.defaultCase, node.postDefaultCases);
    }

    @NotNull
    protected Node visitIfElse(@NotNull IfElse ifElse) {
        return new IfElse(visitExpression(ifElse.test), visitBlock(ifElse.consequent), visitBlock(ifElse.alternate));
    }

    @NotNull
    protected RelationalComparison visitRelationalComparison(RelationalComparison relationalComparison) {
        return new RelationalComparison(relationalComparison.operator, visitExpression(relationalComparison.left), visitExpression(relationalComparison.right));
    }

    @NotNull
    protected VariableAssignment visitVariableAssignment(VariableAssignment variableAssignment) {
        return new VariableAssignment(visitReference(variableAssignment.ref), visitExpression(variableAssignment.value), variableAssignment.strict);
    }

    @NotNull
    protected Void visitVoid(Void _void) {
        return Void.INSTANCE;
    }

    @NotNull
    public Node visit(Node node) {
        return visitNode(node);
    }
}
