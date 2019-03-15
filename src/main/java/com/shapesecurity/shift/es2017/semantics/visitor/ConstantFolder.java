package com.shapesecurity.shift.es2017.semantics.visitor;

import com.shapesecurity.functional.data.ImmutableList;
import com.shapesecurity.functional.data.Maybe;
import com.shapesecurity.shift.es2017.semantics.Semantics;
import com.shapesecurity.shift.es2017.semantics.asg.*;
import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.FloatMath;
import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.IntMath;
import com.shapesecurity.shift.es2017.semantics.asg.BinaryOperation.Logic;
import com.shapesecurity.shift.es2017.semantics.asg.UnaryOperation.Not;

import javax.annotation.Nonnull;

import static com.shapesecurity.shift.es2017.semantics.visitor.ECMA262Operations.ToInt32;
import static com.shapesecurity.shift.es2017.semantics.visitor.ECMA262Operations.ToNumber;
import static com.shapesecurity.shift.es2017.semantics.visitor.ECMA262Operations.ToString;
import static com.shapesecurity.shift.es2017.semantics.visitor.ECMA262Operations.ToUint32;
import static com.shapesecurity.shift.es2017.semantics.visitor.ECMA262Operations.Type;

public class ConstantFolder extends ReconstructingReducer {

	public static Reducer<Node> create() {
		return new NodeAdaptingReducer(new ConstantFolder());
	}

	protected ConstantFolder() {
		super();
	}

	public static Semantics reduce(@Nonnull Semantics semantics) {
		return new Semantics(
			new Director<>(ConstantFolder.create()).reduceNode(semantics.node),
			semantics.locals,
			semantics.scriptVarDecls,
			semantics.scopeLookup,
			semantics.functionScopes
		);
	}

	@Nonnull
	@Override
	public NodeWithValue reduceNot(@Nonnull Not not, @Nonnull NodeWithValue expression) {
		if (expression instanceof LiteralNumber) {
			LiteralNumber n = (LiteralNumber) expression;
			return new LiteralBoolean(n.value == 0);
		} else if (expression instanceof LiteralBoolean) {
			return new LiteralBoolean(!((LiteralBoolean) expression).value);
		} else if (expression instanceof Not) {
			Not subNot = (Not) expression;
			if (subNot.expression instanceof Not) {
				Not subSubNot = (Not) subNot.expression;
				return reduceNot(subSubNot, subSubNot.expression);
			}
		}
		return super.reduceNot(not, expression);
	}

	@Nonnull
	@Override
	public Node reduceIfElse(@Nonnull IfElse ifElse, @Nonnull NodeWithValue test, @Nonnull Block consequent, @Nonnull Block alternate) {
		Maybe<Boolean> t = Truthiness.truthiness(test);
		return t.<Node>map(x -> x ? consequent : alternate).orJustLazy(() -> super.reduceIfElse(ifElse, test, consequent, alternate));
	}

	@Nonnull
	@Override
	public NodeWithValue reduceFloatMath(@Nonnull FloatMath floatMath, @Nonnull NodeWithValue left, @Nonnull NodeWithValue right) {
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
		return super.reduceFloatMath(floatMath, left, right);
	}

	@Nonnull
	@Override
	public NodeWithValue reduceIntMath(@Nonnull IntMath intMath, @Nonnull NodeWithValue left, @Nonnull NodeWithValue right) {
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
			default:
				return super.reduceIntMath(intMath, left, right);
		}
	}

	@Nonnull
	@Override
	public NodeWithValue reduceLogic(@Nonnull Logic expression, @Nonnull NodeWithValue left, @Nonnull NodeWithValue right) {
		switch (expression.operator) {
			case And:
				return Truthiness.truthiness(left)
							.map(leftIsTruthy -> leftIsTruthy ? new BlockWithValue(ImmutableList.of(left), right) : left)
							.orJustLazy(() -> super.reduceLogic(expression, left, right));
			case Or:
				return Truthiness.truthiness(left)
							.map(leftIsTruthy -> leftIsTruthy ? left : new BlockWithValue(ImmutableList.of(left), right))
							.orJustLazy(() -> super.reduceLogic(expression, left, right));
			default:
				throw new RuntimeException("Not reached");
		}
	}

}
