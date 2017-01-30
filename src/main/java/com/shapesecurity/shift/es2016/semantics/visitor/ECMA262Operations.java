package com.shapesecurity.shift.es2016.semantics.visitor;

import com.shapesecurity.functional.data.Maybe;
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
import com.shapesecurity.shift.es2016.semantics.asg.NodeWithValue;

import javax.annotation.Nonnull;

public final class ECMA262Operations {

    private static final long TWO_TO_32 = 4294967296L;
    private static final long TWO_TO_31 = 2147483648L;

    private ECMA262Operations() {
    }

    public static Maybe<Type> Type(@Nonnull NodeWithValue node) {
        if (node instanceof LiteralUndefined) {
            return Maybe.of(Type.Undefined);
        } else if (node instanceof LiteralNull) {
            return Maybe.of(Type.Null);
        } else if (node instanceof LiteralBoolean) {
            return Maybe.of(Type.Boolean);
        } else if (node instanceof LiteralString) {
            return Maybe.of(Type.String);
        } else if (node instanceof LiteralSymbol) {
            return Maybe.of(Type.Symbol);
        } else if (node instanceof LiteralNumber || node instanceof LiteralInfinity) {
            return Maybe.of(Type.Number);
        } else if (node instanceof LiteralEmptyObject || node instanceof LiteralEmptyArray || node instanceof LiteralFunction || node instanceof LiteralRegExp) {
            return Maybe.of(Type.Object);
        }
        return Maybe.empty();
    }

    public static Maybe<LiteralString> ToString(@Nonnull NodeWithValue node) {
        if (node instanceof LiteralUndefined) {
            return Maybe.of(new LiteralString("undefined"));
        } else if (node instanceof LiteralNull) {
            return Maybe.of(new LiteralString("null"));
        } else if (node instanceof LiteralBoolean) {
            return Maybe.of(new LiteralString(((LiteralBoolean) node).value ? "true" : "false"));
        } else if (node instanceof LiteralNumber) {
            return Maybe.of(new LiteralString(Double.toString(((LiteralNumber) node).value)));
        } else if (node instanceof LiteralString) {
            return Maybe.of((LiteralString) node);
        } else if (node instanceof LiteralSymbol) {
            // throw a TypeError
            return Maybe.empty();
        }
        return Maybe.empty();
    }

    public static Maybe<LiteralNumber> ToNumber(@Nonnull NodeWithValue node) {
        if (node instanceof LiteralUndefined) {
            return Maybe.of(new LiteralNumber(Double.NaN));
        } else if (node instanceof LiteralNull) {
            return Maybe.of(new LiteralNumber(0));
        } else if (node instanceof LiteralBoolean) {
            return Maybe.of(new LiteralNumber(((LiteralBoolean) node).value ? 1 : 0));
        } else if (node instanceof LiteralNumber) {
            return Maybe.of((LiteralNumber) node);
        } else if (node instanceof LiteralString) {
            return Maybe.of(new LiteralNumber(Double.parseDouble(((LiteralString) node).value)));
        } else if (node instanceof LiteralSymbol) {

            // throw a TypeError
            return Maybe.empty();
        }
        return Maybe.empty();
    }

    public static Maybe<Integer> ToInt32(@Nonnull NodeWithValue node) {
        return ToUint32(node).map(num-> {
            if (num >= TWO_TO_31){
                return ((Long) (num - TWO_TO_32)).intValue();
            } else {
                return num.intValue();
            }
        });
    }

    public static Maybe<Long> ToUint32(@Nonnull NodeWithValue node) {
        return ToNumber(node).map(numberLiteral -> {
            double number = numberLiteral.value;
            if (Double.isNaN(number) || number == 0 || Double.isInfinite(number)) {
                return 0L;
            } else {
                long truncated = (long)(Math.floor(Math.abs(number)) % TWO_TO_32);
                if (number < 0) truncated = TWO_TO_32 - truncated;
                return truncated;
            }
        });
    }

    public static Maybe<NodeWithValue> ToPrimitive(@Nonnull NodeWithValue node) {
        if (node instanceof LiteralUndefined || node instanceof LiteralNull || node instanceof LiteralBoolean || node instanceof LiteralNumber || node instanceof LiteralString || node instanceof LiteralSymbol) {
            return Maybe.of(node);
        }
        return Maybe.empty();
    }

    public enum Type {
        Undefined,
        Null,
        Boolean,
        String,
        Symbol,
        Number,
        Object;
    }
}
