package com.shapesecurity.shift.es2016.semantics.visitor;

import com.shapesecurity.functional.data.Maybe;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.Equality;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.FloatMath;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.In;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.InstanceOf;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.IntMath;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.Logic;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.RelationalComparison;
import com.shapesecurity.shift.es2016.semantics.asg.BlockWithValue;
import com.shapesecurity.shift.es2016.semantics.asg.Call;
import com.shapesecurity.shift.es2016.semantics.asg.DeleteGlobalProperty;
import com.shapesecurity.shift.es2016.semantics.asg.DeleteProperty;
import com.shapesecurity.shift.es2016.semantics.asg.GlobalReference;
import com.shapesecurity.shift.es2016.semantics.asg.Halt;
import com.shapesecurity.shift.es2016.semantics.asg.Keys;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralBoolean;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralEmptyArray;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralEmptyObject;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralFunction;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralInfinity;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralNull;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralNumber;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralRegExp;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralString;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralSymbol;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralUndefined;
import com.shapesecurity.shift.es2016.semantics.asg.LocalReference;
import com.shapesecurity.shift.es2016.semantics.asg.MemberAccess;
import com.shapesecurity.shift.es2016.semantics.asg.MemberAssignment;
import com.shapesecurity.shift.es2016.semantics.asg.New;
import com.shapesecurity.shift.es2016.semantics.asg.NodeWithValue;
import com.shapesecurity.shift.es2016.semantics.asg.RequireObjectCoercible;
import com.shapesecurity.shift.es2016.semantics.asg.TemporaryReference;
import com.shapesecurity.shift.es2016.semantics.asg.This;
import com.shapesecurity.shift.es2016.semantics.asg.TypeCoercionNumber;
import com.shapesecurity.shift.es2016.semantics.asg.TypeCoercionString;
import com.shapesecurity.shift.es2016.semantics.asg.TypeofGlobal;
import com.shapesecurity.shift.es2016.semantics.asg.UnaryOperation.BitwiseNot;
import com.shapesecurity.shift.es2016.semantics.asg.UnaryOperation.Negation;
import com.shapesecurity.shift.es2016.semantics.asg.UnaryOperation.Not;
import com.shapesecurity.shift.es2016.semantics.asg.UnaryOperation.Typeof;
import com.shapesecurity.shift.es2016.semantics.asg.UnaryOperation.VoidOp;
import com.shapesecurity.shift.es2016.semantics.asg.VariableAssignment;

import javax.annotation.Nonnull;

import static com.shapesecurity.shift.es2016.semantics.visitor.ECMA262Operations.Type;


final class Truthiness implements FAlgebraNodeWithValue<Maybe<Boolean>> {
    private Truthiness() {}
    public static final Truthiness INSTANCE = new Truthiness();

    public static Maybe<Boolean> truthiness(@Nonnull NodeWithValue node) {
        return CataNodeWithValue.cata(Truthiness.INSTANCE, node);
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(BlockWithValue blockWithValue) {
        return truthiness(blockWithValue.result);
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(Call call) {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(DeleteGlobalProperty deleteGlobalProperty) {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(DeleteProperty deleteProperty) {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(GlobalReference globalReference) {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(Halt halt) {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(Keys keys) {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(LiteralBoolean literalBoolean) {
        return Maybe.of(literalBoolean.value);
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(LiteralEmptyArray literalEmptyArray) {
        return Maybe.of(true);
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(LiteralEmptyObject literalEmptyObject) {
        return Maybe.of(true);
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(LiteralFunction literalFunction) {
        return Maybe.of(true);
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(LiteralInfinity literalInfinity) {
        return Maybe.of(true);
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(LiteralNull literalNull) {
        return Maybe.of(false);
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(LiteralNumber literalNumber) {
        return Maybe.of(literalNumber.value != 0 && !Double.isNaN(literalNumber.value));
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(LiteralRegExp literalRegExp) {
        return Maybe.of(true);
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(LiteralString literalString) {
        return Maybe.of(!(literalString.value.isEmpty()));
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(LiteralSymbol literalSymbol) {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(LiteralUndefined literalUndefined) {
        return Maybe.of(false);
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(MemberAccess memberAccess) {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(MemberAssignment memberAssignment) {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(New new_) {
        return Maybe.of(true);
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(RequireObjectCoercible requireObjectCoercible) {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(LocalReference localReference) {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(TemporaryReference temporaryReference) {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(This this_) {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(TypeCoercionNumber typeCoercionNumber) {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(TypeCoercionString typeCoercionString) {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(TypeofGlobal typeofGlobal) {
        return Maybe.of(true);
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(VariableAssignment variableAssignment) {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(Equality equality) {
        NodeWithValue left = equality.left();
        NodeWithValue right = equality.right();
        if (!(left instanceof LiteralNumber && Double.isNaN(((LiteralNumber) left).value)) &&
                Type(left).map(t -> t != Type.Object).orJust(false) && left.equals(right)) {
            Equality.Operator operator = equality.operator;
            return Maybe.of(operator == Equality.Operator.StrictEq || operator == Equality.Operator.Eq);
        }
        // TODO: implement non-strict equality algorithm
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(FloatMath floatMath) {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(In in_) {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(InstanceOf instanceOf) {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(IntMath intMath) {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(Logic logic) {
        NodeWithValue left = logic.left();
        NodeWithValue right = logic.right();
        if (logic.operator == Logic.Operator.And) {
            return truthiness(left).flatMap(x -> truthiness(right).map(y -> x && y));
        } else {
            return truthiness(left).flatMap(x -> truthiness(right).map(y -> x || y));
        }
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(RelationalComparison relationalComparison) {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(BitwiseNot bitwiseNot) {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(Negation negation) {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(Not not) {
        return truthiness(not.expression()).map(x -> !x);
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(Typeof typeof) {
        return Maybe.of(true);
    }

    @Nonnull
    @Override
    public Maybe<Boolean> apply(VoidOp voidOp) {
        return Maybe.of(false);
    }
}
