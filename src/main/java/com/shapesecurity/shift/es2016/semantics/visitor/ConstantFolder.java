package com.shapesecurity.shift.es2016.semantics.visitor;

import com.shapesecurity.functional.data.ImmutableList;
import com.shapesecurity.functional.data.Maybe;
import com.shapesecurity.shift.es2016.semantics.Semantics;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.FloatMath;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.IntMath;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.Logic;
import com.shapesecurity.shift.es2016.semantics.asg.BlockWithValue;
import com.shapesecurity.shift.es2016.semantics.asg.IfElse;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralBoolean;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralNumber;
import com.shapesecurity.shift.es2016.semantics.asg.LiteralString;
import com.shapesecurity.shift.es2016.semantics.asg.Node;
import com.shapesecurity.shift.es2016.semantics.asg.NodeWithValue;
import com.shapesecurity.shift.es2016.semantics.asg.UnaryOperation.Not;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static com.shapesecurity.shift.es2016.semantics.visitor.ECMA262Operations.ToInt32;
import static com.shapesecurity.shift.es2016.semantics.visitor.ECMA262Operations.ToNumber;
import static com.shapesecurity.shift.es2016.semantics.visitor.ECMA262Operations.ToString;
import static com.shapesecurity.shift.es2016.semantics.visitor.ECMA262Operations.ToUint32;
import static com.shapesecurity.shift.es2016.semantics.visitor.ECMA262Operations.Type;
import static com.shapesecurity.shift.es2016.semantics.visitor.Truthiness.truthiness;

public class ConstantFolder extends ReconstructingReducer {
    private final Semantics semantics;

    public ConstantFolder(Semantics semantics) {
        this.semantics = semantics;
    }

    @Nonnull
    public static Semantics reduce(Semantics input) {
        return new Semantics(new ConstantFolder(input).go(), input.locals, input.scriptVarDecls, input.scopeLookup, input.functionScopes);
    }

    @Nonnull
    private Node go() {
        return this.visitNode(this.semantics.node);
    }

    @Nonnull
    @Override
    protected NodeWithValue visitNot(@Nonnull Not not) {
        not = (Not) super.visitNot(not);
        if (not.expression instanceof LiteralNumber) {
            LiteralNumber n = (LiteralNumber) not.expression;
            return new LiteralBoolean(n.value == 0);
        } else if (not.expression instanceof LiteralBoolean) {
            return new LiteralBoolean(!((LiteralBoolean) not.expression).value);
        } else if (not.expression instanceof Not) {
            if (((Not) not.expression).expression instanceof Not) {
                return visitNot((Not) ((Not) not.expression).expression);
            }
        }
        return not;
    }

    @Nonnull
    @Override
    protected Node visitIfElse(@Nonnull IfElse ifElse) {
        final IfElse ifElsef = (IfElse) super.visitIfElse(ifElse);
        Maybe<Boolean> t = Truthiness.truthiness(ifElsef.test);
        return t.<Node>map(x -> x ? ifElsef.consequent : ifElsef.alternate).orJust(ifElsef);
    }

    @Nonnull
    @Override
    public NodeWithValue visitFloatMath(@NotNull FloatMath floatMath) {
        NodeWithValue left = visitNodeWithValue(floatMath.left());
        NodeWithValue right = visitNodeWithValue(floatMath.right());
        if (floatMath.operator == FloatMath.Operator.Plus) {
            if (Type(left).maybe(false, x -> x == Type.String) || Type(right).maybe(false, x -> x == Type.String)) {
                Maybe<LiteralString> lstr = ToString(left);
                Maybe<LiteralString> rstr = ToString(right);
                return lstr.<NodeWithValue>flatMap(l -> rstr.map(r -> new LiteralString(l.value + r.value))).orJustLazy(() -> new FloatMath(floatMath.operator, left, right));
            } else {
                Maybe<LiteralNumber> lnum = ToNumber(left);
                Maybe<LiteralNumber> rnum = ToNumber(right);
                return lnum.<NodeWithValue>flatMap(l -> rnum.map(r -> new LiteralNumber(l.value + r.value))).orJustLazy(() -> new FloatMath(floatMath.operator, left, right));
            }
        }
        return new FloatMath(floatMath.operator, left, right);
    }

    @Nonnull
    @Override
    public NodeWithValue visitIntMath(@NotNull IntMath intMath) {
        NodeWithValue left = visitNodeWithValue(intMath.left());
        NodeWithValue right = visitNodeWithValue(intMath.right());
        switch (intMath.operator) {
            case LeftShift: {
                Maybe<Integer> lnum = ToInt32(left);
                Maybe<Long> rnum = ToUint32(right);
                Maybe<Integer> shiftCount = rnum.map(numberLiteral -> (int) (numberLiteral & 0x1F));
                return lnum
                        .<NodeWithValue>flatMap(a -> shiftCount.map(b -> new LiteralNumber(a << b)))
                        .orJustLazy(() -> new IntMath(intMath.operator, left, right));
            }
            case RightShift: {
                Maybe<Integer> lnum = ToInt32(left);
                Maybe<Long> rnum = ToUint32(right);
                Maybe<Integer> shiftCount = rnum.map(numberLiteral -> (int) (numberLiteral & 0x1F));
                return lnum
                        .<NodeWithValue>flatMap(a -> shiftCount.map(b -> new LiteralNumber(a >> b)))
                        .orJustLazy(() -> new IntMath(intMath.operator, left, right));
            }
            case UnsignedRightShift: {
                Maybe<Long> lnum = ToUint32(left);
                Maybe<Long> rnum = ToUint32(right);
                Maybe<Integer> shiftCount = rnum.map(numberLiteral -> (int) (numberLiteral & 0x1F));
                return lnum
                        .<NodeWithValue>flatMap(a -> shiftCount.map(b -> new LiteralNumber(a >>> b)))
                        .orJustLazy(() -> new IntMath(intMath.operator, left, right));
             // TODO BitwiseAnd, BitwiseOr, BitwiseXor
            }
        }
        return new IntMath(intMath.operator, left, right);
    }

    @Override
    @Nonnull
    public NodeWithValue visitLogic(@Nonnull Logic logic) {
        NodeWithValue left = visitNodeWithValue(logic.left());
        switch (logic.operator) {
            case And:
                return truthiness(left)
                    .map(leftIsTruthy -> leftIsTruthy ? new BlockWithValue(ImmutableList.of(left), visitNodeWithValue(logic.right())) : left)
                    .orJustLazy(() -> new Logic(logic.operator, left, visitNodeWithValue(logic.right())));
            case Or:
                return truthiness(left)
                    .map(leftIsTruthy -> leftIsTruthy ? left : new BlockWithValue(ImmutableList.of(left), visitNodeWithValue(logic.right())))
                    .orJustLazy(() -> new Logic(logic.operator, left, visitNodeWithValue(logic.right())));
        }
        // Not reached.
        return new Logic(logic.operator, left, visitNodeWithValue(logic.right()));
    }
}
