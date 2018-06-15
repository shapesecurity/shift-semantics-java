package com.shapesecurity.shift.es2016.semantics.asgvisitor;

import com.shapesecurity.functional.Pair;
import com.shapesecurity.functional.Unit;
import com.shapesecurity.functional.data.ImmutableList;
import com.shapesecurity.functional.data.Maybe;
import com.shapesecurity.shift.es2016.scope.Variable;
import com.shapesecurity.shift.es2016.semantics.Semantics;
import com.shapesecurity.shift.es2016.semantics.asg.*;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.BinaryOperation;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.Equality;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.FloatMath;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.IntMath;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.RelationalComparison;
import com.shapesecurity.shift.es2016.semantics.asg.UnaryOperation.UnaryOperation;
import com.shapesecurity.shift.es2016.semantics.asg.BinaryOperation.Logic;
import com.shapesecurity.shift.es2016.semantics.asg.Void;

import javax.annotation.Nonnull;

import java.util.IdentityHashMap;
import java.util.Map;

public class Dottifier {
	@Nonnull
	private Map<Node, String> nodeNames = new IdentityHashMap<>();
	@Nonnull
	private Map<ImmutableList<? extends Node>, String> listNames = new IdentityHashMap<>();
	@Nonnull
	private Map<Maybe<? extends Node>, String> maybeNames = new IdentityHashMap<>();
	@Nonnull
	private Map<Variable, Integer> variableIds = new IdentityHashMap<>();
	@Nonnull
	private Map<Variable, String> variableNames = new IdentityHashMap<>();
	@Nonnull
	private Map<Node, Unit> visited = new IdentityHashMap<>(); // Java lacks IdentitySet, for some reason
	@Nonnull
	private Map<Node, Unit> declared = new IdentityHashMap<>(); // Java lacks IdentitySet, for some reason
	private int id = 0;
	private int varId = 0;

	private int indentationLevel = 0;

	private Dottifier() {
	}

	@Nonnull
	public static String render(@Nonnull Semantics semantics) {
		return (new Dottifier()).reduce(semantics);
	}

	@Nonnull
	private static String sanitize(@Nonnull String string) {
		return string.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}

	@Nonnull
	private String indent() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < indentationLevel; ++i) {
			stringBuilder.append("  ");
		}
		return stringBuilder.toString();
	}

	@Nonnull
	private String name(@Nonnull Node node) {
		if (!nodeNames.containsKey(node)) {
			nodeNames.put(node, node.getClass().getSimpleName() + "_" + (id++));
		}
		return nodeNames.get(node);
	}

	@Nonnull
	private String name(@Nonnull ImmutableList<? extends Node> struct) {
		if (!listNames.containsKey(struct)) {
			listNames.put(struct, "struct_" + (id++));
		}
		return listNames.get(struct);
	}

	@Nonnull
	private String name(@Nonnull Maybe<? extends Node> maybeNode) {
		if (!maybeNames.containsKey(maybeNode)) {
			maybeNames.put(maybeNode, maybeNode.map(this::name).orJust("nothing_" + (id++)));
		}
		return maybeNames.get(maybeNode);
	}

	private int id(@Nonnull Variable variable) {
		if (!variableIds.containsKey(variable)) {
			variableIds.put(variable, varId++);
		}
		return variableIds.get(variable);
	}

	@Nonnull
	private String label(@Nonnull Node node) {
		if (node instanceof Equality) {
			return "Equality\\nOperator: " + sanitize(((Equality) node).operator.name);
		} else if (node instanceof FloatMath) {
			return "FloatMath\\nOperator: " + sanitize(((FloatMath) node).operator.name);
		} else if (node instanceof IntMath) {
			return "IntMath\\nOperator: " + sanitize(((IntMath) node).operator.name);
		} else if (node instanceof Logic) {
			return "Logic\\nOperator: " + sanitize(((Logic) node).operator.name);
		} else if (node instanceof RelationalComparison) {
			return "RelationalComparison\\nOperator: " + sanitize(((RelationalComparison) node).operator.name);
		} else if (node instanceof DeleteGlobalProperty) {
			return "DeleteGlobalProperty\\nValue: " + ((DeleteGlobalProperty) node).which;
		} else if (node instanceof DeleteProperty) {
			return "DeleteProperty\\nStrict: " + ((DeleteProperty) node).strict;
		} else if (node instanceof GlobalReference) {
			return "GlobalReference\\nName: " + ((GlobalReference) node).name;
		} else if (node instanceof LiteralBoolean) {
			return "LiteralBoolean\\nValue: " + ((LiteralBoolean) node).value;
		} else if (node instanceof LiteralFunction) {
			return "LiteralFunction\\nStrict: " + ((LiteralFunction) node).isStrict;
		} else if (node instanceof LiteralNumber) {
			return "LiteralNumber\\nValue: " + ((LiteralNumber) node).value;
		} else if (node instanceof LiteralString) {
			return "LiteralString\\nValue: " + sanitize(((LiteralString) node).value);
		} else if (node instanceof LiteralRegExp) {
			LiteralRegExp literalRegExp = (LiteralRegExp) node;
			return "LiteralRegExp\\nPattern: " + sanitize(literalRegExp.pattern) + "\\nFlags: " + (literalRegExp.global ? "g" : "") + (literalRegExp.ignoreCase ? "i" : "") + (literalRegExp.multiLine ? "m" : "") + (literalRegExp.unicode ? "u" : "") + (literalRegExp.sticky ? "y" : "");
		} else if (node instanceof MemberAssignment) {
			return "MemberAssignment\\nStrict: " + ((MemberAssignment) node).strict;
		} else if (node instanceof This) {
			return "This\\nStrict: " + ((This) node).strict;
		} else if (node instanceof TypeofGlobal) {
			return "TypeofGlobal\\nName: " + ((TypeofGlobal) node).which;
		} else if (node instanceof VariableAssignment) {
			return "VariableAssignment\\nStrict: " + ((VariableAssignment) node).strict;
		}
		return node.getClass().getSimpleName();
	}

	@Nonnull
	private String ensureDeclared(@Nonnull Node node) {
		if (declared.containsKey(node)) {
			return indent() + "// " + name(node) + "\n";
		}
		declared.put(node, Unit.unit);
		return indent() + name(node) + " [label=\"" + label(node) + "\"];\n";
	}

	@Nonnull
	private Pair<String, String> reduceVariables(@Nonnull ImmutableList<Variable> variables) {
		// returns struct name, dot source.
		// TODO instead just return source and store the name somewhere, like we do elsewhere.
		// TODO currently requires that these be the first declarations. This isn't valid for locals or for captured.
		StringBuilder out = new StringBuilder();
		String structName = "struct_" + id++;
		out.append(indent())
				.append(structName)
				.append(" [\n");
		++indentationLevel;
		out.append(indent())
				.append("shape=\"record\" ")
				.append(indent())
				.append("label=\"");
		boolean first = true;
		for (Variable variable : variables) {
			if (!first) {
				out.append(" | ");
			} else {
				first = false;
			}

			assert !variableNames.containsKey(variable);
			int varId = id(variable);
			String name = "variable_" + varId;
			out.append('<')
					.append("variable_")
					.append(varId)
					.append("> ")
					.append("Variable\\nID: ")
					.append(varId);
			variableNames.put(variable, structName + ":" + name);
		}
		--indentationLevel;
		out.append("\"\n")
				.append(indent())
				.append("];\n");
		return Pair.of(structName, out.toString());
	}

	@Nonnull
	private String reduce(@Nonnull Semantics semantics) {
		StringBuilder out = new StringBuilder();
		out.append("digraph G {\n");
		++indentationLevel;
		Pair<String, String> locals = reduceVariables(semantics.locals);
		out.append(indent())
				.append("graph [compound=true];\n")
				.append(indent())
				.append("node [shape=Mrecord fontsize=10 fontname=\"Verdana\"];\n")
				.append(locals.right())
				.append(indent())
				.append("{ rank = source;\n"); // so the source node appears at the top
		++indentationLevel;
		out.append(indent())
				.append("semantics [label=\"Semantics\"];\n");
		--indentationLevel;
		out.append(indent())
				.append("}\n")
				.append(reduce(semantics.node))
				.append(indent())
				.append("semantics -> ")
				.append(locals.left())
				.append(" [label=\"scriptLocals\"];\n")
				.append(indent())
				.append("semantics -> ")
				.append(name(semantics.node))
				.append(" [label=\"node\"];\n");
		--indentationLevel;
		out.append("}\n");
		return out.toString();
	}

	@Nonnull
	private String reduce(@Nonnull ImmutableList<? extends Node> nodes) {
		// TODO handle empty list better

		StringBuilder builder = new StringBuilder();
		StringBuilder after = new StringBuilder();
		String structName = name(nodes);

		builder.append(indent())
				.append(structName)
				.append(" [\n");
		++indentationLevel;
		builder.append(indent())
				.append("shape=\"record\" ")
				.append(indent())
				.append("label=\"");
		boolean first = true;
		for (Node node : nodes) {
			if (!first) {
				builder.append(" | ");
			} else {
				first = false;
			}

			String nodeName = name(node);
			String nodeLabel;
			if (node instanceof BreakTarget) {
				// Another layer of indirection for BreakTargets
				String indirectionName = "indirection_" + (id++);
				after.append(indent())
						.append(nodeName)
						.append(" -> ")
						.append(structName)
						.append(':')
						.append(indirectionName)
						.append(";\n");
				nodeName = indirectionName;
				nodeLabel = "";
			} else {
				nodeNames.put(node, structName + ':' + nodeName);
				nodeLabel = label(node);
				declared.put(node, Unit.unit); // TODO consider combining declared and nodeNames
			}

			builder.append('<')
					.append(nodeName)
					.append("> ")
					.append(nodeLabel);
		}
		--indentationLevel;
		builder.append("\"\n")
				.append(indent())
				.append("];\n")
				.append(after);

		for (Node node : nodes) {
			builder.append(reduce(node));
		}
		return builder.toString();
	}

	@Nonnull
	private String reduce(@Nonnull Maybe<? extends Node> maybeNode) {
		return maybeNode.map(this::reduce).orJust(indent() + name(maybeNode) + " [label=\"\"];\n");
	}

	@Nonnull
	private String reduce(Node node) {
		if (visited.containsKey(node)) {
			return "";
		}
		visited.put(node, Unit.unit);

		if (node instanceof BinaryOperation) {
			return reduceBinaryOperation((BinaryOperation) node);
		} else if (node instanceof UnaryOperation) {
			return reduceUnaryOperation((UnaryOperation) node);
		} else if (node instanceof Block) {
			return reduceBlock((Block) node);
		} else if (node instanceof BlockWithValue) {
			return reduceBlockWithValue((BlockWithValue) node);
		} else if (node instanceof Break) {
			return reduceBreak((Break) node);
		} else if (node instanceof BreakTarget) {
			return reduceBreakTarget((BreakTarget) node);
		} else if (node instanceof Call) {
			return reduceCall((Call) node);
		} else if (node instanceof DeleteGlobalProperty) {
			return reduceDeleteGlobalProperty((DeleteGlobalProperty) node);
		} else if (node instanceof DeleteProperty) {
			return reduceDeleteProperty((DeleteProperty) node);
		} else if (node instanceof GlobalReference) {
			return reduceGlobalReference((GlobalReference) node);
		} else if (node instanceof Halt) {
			return reduceHalt((Halt) node);
		} else if (node instanceof IfElse) {
			return reduceIfElse((IfElse) node);
		} else if (node instanceof Keys) {
			return reduceKeys((Keys) node);
		} else if (node instanceof Literal) {
			if (node instanceof LiteralBoolean) {
				return reduceLiteralBoolean((LiteralBoolean) node);
			} else if (node instanceof LiteralEmptyArray) {
				return reduceLiteralEmptyArray((LiteralEmptyArray) node);
			} else if (node instanceof LiteralEmptyObject) {
				return reduceLiteralEmptyObject((LiteralEmptyObject) node);
			} else if (node instanceof LiteralFunction) {
				return reduceLiteralFunction((LiteralFunction) node);
			} else if (node instanceof LiteralInfinity) {
				return reduceLiteralInfinity((LiteralInfinity) node);
			} else if (node instanceof LiteralNull) {
				return reduceLiteralNull((LiteralNull) node);
			} else if (node instanceof LiteralNumber) {
				return reduceLiteralNumber((LiteralNumber) node);
			} else if (node instanceof LiteralRegExp) {
				return reduceLiteralRegExp((LiteralRegExp) node);
			} else if (node instanceof LiteralString) {
				return reduceLiteralString((LiteralString) node);
			} else if (node instanceof LiteralUndefined) {
				return reduceLiteralUndefined((LiteralUndefined) node);
			}
		} else if (node instanceof LocalReference) {
			return reduceLocalReference((LocalReference) node);
		} else if (node instanceof Loop) {
			return reduceLoop((Loop) node);
		} else if (node instanceof MemberAccess) {
			return reduceMemberAccess((MemberAccess) node);
		} else if (node instanceof MemberAssignment) {
			return reduceMemberAssignment((MemberAssignment) node);
		} else if (node instanceof MemberCall) {
			return reduceMemberCall((MemberCall) node);
		} else if (node instanceof MemberDefinition) {
			return reduceMemberDefinition((MemberDefinition) node);
		} else if (node instanceof New) {
			return reduceNew((New) node);
		} else if (node instanceof RequireObjectCoercible) {
			return reduceRequireObjectCoercible((RequireObjectCoercible) node);
		} else if (node instanceof Return) {
			return reduceReturn((Return) node);
		} else if (node instanceof SwitchStatement) {
			return reduceSwitchStatement((SwitchStatement) node);
		} else if (node instanceof This) {
			return reduceThis((This) node);
		} else if (node instanceof Throw) {
			return reduceThrow((Throw) node);
		} else if (node instanceof TryCatch) {
			return reduceTryCatch((TryCatch) node);
		} else if (node instanceof TryFinally) {
			return reduceTryFinally((TryFinally) node);
		} else if (node instanceof TypeCoercionNumber) {
			return reduceTypeCoercionNumber((TypeCoercionNumber) node);
		} else if (node instanceof TypeCoercionObject) {
			return reduceTypeCoercionObject((TypeCoercionObject) node);
		} else if (node instanceof TypeCoercionString) {
			return reduceTypeCoercionString((TypeCoercionString) node);
		} else if (node instanceof TypeofGlobal) {
			return reduceTypeofGlobal((TypeofGlobal) node);
		} else if (node instanceof VariableAssignment) {
			return reduceVariableAssignment((VariableAssignment) node);
		} else if (node instanceof Void) {
			return reduceVoid((Void) node);
		}
		throw new UnsupportedOperationException("Unimplemented: " + node.getClass().getSimpleName());
	}

	@Nonnull
	private String reduceBinaryOperation(@Nonnull BinaryOperation node) {
		String out = ensureDeclared(node);
		++indentationLevel;
		out += reduce(node.left());
		out += reduce(node.right());
		String lhs = indent() + name(node) + " -> ";
		out += lhs + name(node.left()) + " [label=\"left\"];\n";
		out += lhs + name(node.right()) + " [label=\"right\"];\n";
		--indentationLevel;
		return out;
	}

	@Nonnull
	private String reduceUnaryOperation(@Nonnull UnaryOperation node) {
		String out = ensureDeclared(node);
		++indentationLevel;
		out += reduce(node.expression());
		String lhs = indent() + name(node) + " -> ";
		out += lhs + name(node.expression()) + " [label=\"expression\"];\n";
		--indentationLevel;
		return out;
	}

	@Nonnull
	private String reduceBlock(@Nonnull Block node) {
		String out = ensureDeclared(node);
		++indentationLevel;
		out += reduce(node.children);
		String lhs = indent() + name(node) + " -> ";
		out += lhs + name(node.children) + " [label=\"children\"];\n";
		--indentationLevel;
		return out;
	}

	@Nonnull
	private String reduceBlockWithValue(@Nonnull BlockWithValue node) {
		String out = ensureDeclared(node);
		++indentationLevel;
		out += reduce(node.head);
		out += reduce(node.result);
		String lhs = indent() + name(node) + " -> ";
		out += lhs + name(node.head) + " [label=\"head\"];\n";
		out += lhs + name(node.result) + " [label=\"result\"];\n";
		--indentationLevel;
		return out;
	}

	@Nonnull
	private String reduceBreak(@Nonnull Break node) {
		String out = ensureDeclared(node);
		++indentationLevel;
		out += reduce(node.target);
		String lhs = indent() + name(node) + " -> ";
		out += lhs + name(node.target) + " [label=\"target\"];\n";
		--indentationLevel;
		return out;
	}

	@Nonnull
	private String reduceBreakTarget(@Nonnull BreakTarget node) {
		return ensureDeclared(node);
	}

	@Nonnull
	private String reduceCall(@Nonnull Call node) {
		String out = ensureDeclared(node);
		++indentationLevel;
		out += reduce(node.callee);
		out += reduce(node.arguments);
		String lhs = indent() + name(node) + " -> ";
		out += lhs + name(node.callee) + " [label=\"callee\"];\n";
		out += lhs + name(node.arguments) + " [label=\"arguments\"];\n";
		--indentationLevel;
		return out;
	}

	@Nonnull
	private String reduceMemberCall(@Nonnull MemberCall node) {
		String out = ensureDeclared(node);
		++indentationLevel;
		out += reduce(node.object);
		out += reduce(node.fieldExpression);
		out += reduce(node.arguments);
		String lhs = indent() + name(node) + " -> ";
		out += lhs + name(node.object) + " [label=\"object\"];\n";
		out += lhs + name(node.fieldExpression) + " [label=\"fieldExpression\"];\n";
		out += lhs + name(node.arguments) + " [label=\"arguments\"];\n";
		--indentationLevel;
		return out;
	}

	@Nonnull
	private String reduceDeleteGlobalProperty(@Nonnull DeleteGlobalProperty node) {
		return ensureDeclared(node);
	}

	@Nonnull
	private String reduceDeleteProperty(@Nonnull DeleteProperty node) {
		String out = ensureDeclared(node);
		++indentationLevel;
		out += reduce(node.object);
		out += reduce(node.fieldExpression);
		String lhs = indent() + name(node) + " -> ";
		out += lhs + name(node.object) + " [label=\"object\"];\n";
		out += lhs + name(node.fieldExpression) + " [label=\"fieldExpression\"];\n";
		--indentationLevel;
		return out;
	}

	@Nonnull
	private String reduceGlobalReference(@Nonnull GlobalReference node) {
		return ensureDeclared(node);
	}

	@Nonnull
	private String reduceHalt(@Nonnull Halt node) {
		return ensureDeclared(node);
	}

	@Nonnull
	private String reduceIfElse(@Nonnull IfElse node) {
		String out = ensureDeclared(node);
		++indentationLevel;
		out += reduce(node.test);
		out += reduce(node.consequent);
		out += reduce(node.alternate);
		String lhs = indent() + name(node) + " -> ";
		out += lhs + name(node.test) + " [label=\"test\"];\n";
		out += lhs + name(node.consequent) + " [label=\"consequent\"];\n";
		out += lhs + name(node.alternate) + " [label=\"alternate\"];\n";
		--indentationLevel;
		return out;
	}

	@Nonnull
	private String reduceKeys(@Nonnull Keys node) {
		String out = ensureDeclared(node);
		++indentationLevel;
		out += reduce(node._object);
		String lhs = indent() + name(node) + " -> ";
		out += lhs + name(node._object) + " [label=\"object\"];\n";
		--indentationLevel;
		return out;
	}

	@Nonnull
	private String reduceLiteralBoolean(@Nonnull LiteralBoolean node) {
		return ensureDeclared(node);
	}

	@Nonnull
	private String reduceLiteralEmptyArray(@Nonnull LiteralEmptyArray node) {
		return ensureDeclared(node);
	}

	@Nonnull
	private String reduceLiteralEmptyObject(@Nonnull LiteralEmptyObject node) {
		return ensureDeclared(node);
	}

	@Nonnull
	private String reduceLiteralFunction(@Nonnull LiteralFunction node) {
		StringBuilder out = new StringBuilder();
		StringBuilder after = new StringBuilder();
		out.append(ensureDeclared(node));
		out.append(indent())
				.append("{ rank=same;\n");
		++indentationLevel;
		String lhs = indent() + name(node) + " -> ";

		// name
		if (node.name.isJust()) {
			Variable variable = node.name.fromJust();
			assert !variableNames.containsKey(variable);
			int varId = id(variable);
			String name = "variable_" + varId;
			out.append(indent())
					.append(name)
					.append(" [label=\"Variable\\nID: ")
					.append(varId)
					.append("\"];\n");
			after.append(lhs)
					.append("variable_")
					.append(varId)
					.append(" [label=\"name\"];\n");
			variableNames.put(variable, name);
		} else {
			String nothing = "nothing_" + id++;
			out.append(indent())
					.append(nothing)
					.append(" [label=\"\"];\n");
			after.append(lhs)
					.append(nothing)
					.append(" [label=\"name\"];\n");
		}

		// arguments
		if (node.arguments.isJust()) {
			Variable variable = node.arguments.fromJust();
			assert !variableNames.containsKey(variable);
			int varId = id(variable);
			String name = "variable_" + varId;
			out.append(indent())
					.append(name)
					.append(" [label=\"Variable\\nID: ")
					.append(varId)
					.append("\"];\n");
			after.append(lhs)
					.append("variable_")
					.append(varId)
					.append(" [label=\"arguments\"];\n");
			variableNames.put(variable, name);
		} else {
			String nothing = "nothing_" + id++;
			out.append(indent())
					.append(nothing)
					.append(" [label=\"\"];\n");
			after.append(lhs)
					.append(nothing)
					.append(" [label=\"arguments\"];\n");
		}

		// parameters
		String paramsName = "struct_" + id++;
		out.append(indent())
				.append(paramsName)
				.append(" [\n");
		++indentationLevel;
		out.append(indent())
				.append("shape=\"record\" ")
				.append(indent())
				.append("label=\"");
		boolean first = true;
		for (Variable variable : node.parameters) {
			if (!first) {
				out.append(" | ");
			} else {
				first = false;
			}

			assert !variableNames.containsKey(variable);
			int varId = id(variable);
			String name = "variable_" + varId;
			out.append('<')
					.append("variable_")
					.append(varId)
					.append("> ")
					.append("Variable\\nID: ")
					.append(varId);
			variableNames.put(variable, paramsName + ":" + name);
		}
		--indentationLevel;
		out.append("\"\n")
				.append(indent())
				.append("];\n");
		after.append(lhs)
				.append(paramsName)
				.append(" [label=\"parameters\"];\n");

		// locals
		String localsName = "struct_" + id++;
		out.append(indent())
				.append(localsName)
				.append(" [\n");
		++indentationLevel;
		out.append(indent())
				.append("shape=\"record\" ")
				.append(indent())
				.append("label=\"");
		first = true;
		for (Variable variable : node.locals) {
			if (!first) {
				out.append(" | ");
			} else {
				first = false;
			}

			if (variableNames.containsKey(variable)) {
				int placeholderId = id++;
				out.append('<')
						.append("placeholder_")
						.append(placeholderId)
						.append(">");
				after.append(indent())
						.append(localsName)
						.append(":placeholder_")
						.append(placeholderId)
						.append(" -> ")
						.append(variableNames.get(variable))
						.append(";\n");
			} else {
				int varId = id(variable);
				String name = "variable_" + varId;
				out.append('<')
						.append(name)
						.append("> ")
						.append("Variable\\nID: ")
						.append(varId);
				variableNames.put(variable, localsName + ":" + name);
			}
		}
		--indentationLevel;
		out.append("\"\n")
				.append(indent())
				.append("];\n");
		after.append(lhs)
				.append(localsName)
				.append(" [label=\"locals\"];\n");


		// body
		assert !declared.containsKey(node.body);
		out.append(ensureDeclared(node.body));

		--indentationLevel;
		out.append(indent())
				.append("}\n"); // end rank=same
		++indentationLevel;
		out.append(reduce(node.body));
		after.append(lhs)
				.append(name(node.body))
				.append(" [label=\"body\"];\n");
		--indentationLevel;

		return out.append(after).toString();
	}

	@Nonnull
	private String reduceLiteralInfinity(@Nonnull LiteralInfinity node) {
		return ensureDeclared(node);
	}

	@Nonnull
	private String reduceLiteralNull(@Nonnull LiteralNull node) {
		return ensureDeclared(node);
	}

	@Nonnull
	private String reduceLiteralNumber(@Nonnull LiteralNumber node) {
		return ensureDeclared(node);
	}

	@Nonnull
	private String reduceLiteralRegExp(@Nonnull LiteralRegExp node) {
		return ensureDeclared(node);
	}

	@Nonnull
	private String reduceLiteralString(@Nonnull LiteralString node) {
		return ensureDeclared(node);
	}

	@Nonnull
	private String reduceLiteralUndefined(@Nonnull LiteralUndefined node) {
		return ensureDeclared(node);
	}

	@Nonnull
	private String reduceLocalReference(@Nonnull LocalReference node) {
		String out = ensureDeclared(node);
		++indentationLevel;
		if (variableNames.containsKey(node.variable)) {
			String lhs = indent() + name(node) + " -> ";
			out += lhs + variableNames.get(node.variable) + " [label=\"variable\"];\n";
		} else {
			throw new RuntimeException("Undeclared reference!");
		}
		--indentationLevel;
		return out;
	}

	@Nonnull
	private String reduceLoop(@Nonnull Loop node) {
		String out = ensureDeclared(node);
		++indentationLevel;
		out += reduce(node.block);
		String lhs = indent() + name(node) + " -> ";
		out += lhs + name(node.block) + " [label=\"block\"];\n";
		--indentationLevel;
		return out;
	}

	@Nonnull
	private String reduceMemberAccess(@Nonnull MemberAccess node) {
		String out = ensureDeclared(node);
		++indentationLevel;
		out += reduce(node.object);
		out += reduce(node.fieldExpression);
		String lhs = indent() + name(node) + " -> ";
		out += lhs + name(node.object) + " [label=\"object\"];\n";
		out += lhs + name(node.fieldExpression) + " [label=\"fieldExpression\"];\n";
		--indentationLevel;
		return out;
	}

	@Nonnull
	private String reduceMemberAssignment(@Nonnull MemberAssignment node) {
		String out = ensureDeclared(node);
		++indentationLevel;
		out += reduce(node.object);
		out += reduce(node.fieldExpression);
		out += reduce(node.property.value);
		String lhs = indent() + name(node) + " -> ";
		out += lhs + name(node.object) + " [label=\"object\"];\n";
		out += lhs + name(node.fieldExpression) + " [label=\"fieldExpression\"];\n";
		out += lhs + name(node.property.value) + " [label=\"value\"];\n";
		--indentationLevel;
		return out;
	}

	@Nonnull
	private String reduceMemberDefinition(@Nonnull MemberDefinition node) {
		StringBuilder out = new StringBuilder();
		StringBuilder after = new StringBuilder();
		out.append(ensureDeclared(node));
		String lhs = indent() + name(node) + " -> ";

		++indentationLevel;
		out.append(reduce(node.object));
		after.append(lhs)
				.append(name(node.object))
				.append(" [label=\"object\"];\n");
		out.append(reduce(node.fieldExpression));
		after.append(lhs)
				.append(name(node.fieldExpression))
				.append(" [label=\"fieldExpression\"];\n");
		Node prop;
		String propLabel;
		if (node.property instanceof MemberAssignmentProperty.Getter) {
			prop = ((MemberAssignmentProperty.Getter) node.property).value;
			propLabel = "getter";
		} else if (node.property instanceof MemberAssignmentProperty.Setter) {
			prop = ((MemberAssignmentProperty.Setter) node.property).value;
			propLabel = "setter";
		} else if (node.property instanceof MemberAssignmentProperty.StaticValue) {
			prop = ((MemberAssignmentProperty.StaticValue) node.property).value;
			propLabel = "value";
		} else {
			throw new UnsupportedOperationException("Unknown MemberAssignmentProperty class: " + node.property.getClass().getSimpleName());
		}
		out.append(reduce(prop));
		after.append(lhs)
				.append(name(prop))
				.append(" [label=\"")
				.append(propLabel)
				.append("\"];\n");
		--indentationLevel;
		return out.append(after).toString();
	}

	@Nonnull
	private String reduceNew(@Nonnull New node) {
		String out = ensureDeclared(node);
		++indentationLevel;
		out += reduce(node.callee);
		out += reduce(node.arguments);
		String lhs = indent() + name(node) + " -> ";
		out += lhs + name(node.callee) + " [label=\"callee\"];\n";
		out += lhs + name(node.arguments) + " [label=\"arguments\"];\n";
		--indentationLevel;
		return out;
	}

	@Nonnull
	private String reduceRequireObjectCoercible(@Nonnull RequireObjectCoercible node) {
		String out = ensureDeclared(node);
		++indentationLevel;
		out += reduce(node.expression);
		String lhs = indent() + name(node) + " -> ";
		out += lhs + name(node.expression) + " [label=\"expression\"];\n";
		--indentationLevel;
		return out;
	}

	@Nonnull
	private String reduceReturn(@Nonnull Return node) {
		String out = ensureDeclared(node);
		++indentationLevel;
		out += reduce(node.expression);
		String lhs = indent() + name(node) + " -> ";
		out += lhs + name(node.expression) + " [label=\"expression\"];\n";
		--indentationLevel;
		return out;
	}

	@Nonnull
	private String reduceSwitchStatement(@Nonnull SwitchStatement node) {
		StringBuilder out = new StringBuilder();
		StringBuilder after = new StringBuilder();
		out.append(ensureDeclared(node));
		String lhs = indent() + name(node) + " -> ";

		++indentationLevel;

		// discriminant
		out.append(reduce(node.discriminant));
		after.append(lhs)
				.append(name(node.discriminant))
				.append(" [label=\"discriminant\"];\n");

		// preDefaultCases
		String preDefaultCasesName = "struct_" + id++;
		out.append(indent())
				.append(preDefaultCasesName)
				.append(" [\n");
		++indentationLevel;
		out.append(indent())
				.append("shape=\"record\" ")
				.append(indent())
				.append("label=\"");
		boolean first = true;
		for (Pair<NodeWithValue, Block> _case : node.preDefaultCases) {
			if (!first) {
				out.append(" | ");
			} else {
				first = false;
			}

			StringBuilder afterCase = new StringBuilder();

			String caseName = "case_" + id++;
			out.append('<')
					.append(caseName)
					.append("> case");

			++indentationLevel;

			Node left = _case.left();
			afterCase.append(reduce(left));

			Node right = _case.right();
			afterCase.append(reduce(right));

			--indentationLevel;
			after.append(indent())
					.append(preDefaultCasesName)
					.append(":")
					.append(caseName)
					.append(" -> ")
					.append(name(left))
					.append(" [label=\"test\"];\n")
					.append(indent())
					.append(preDefaultCasesName)
					.append(":")
					.append(caseName)
					.append(" -> ")
					.append(name(right))
					.append(" [label=\"consequent\"];\n")
					.append(afterCase);
		}
		out.append("\"\n");
		--indentationLevel;
		out.append(indent())
				.append("];\n");
		after.append(lhs)
				.append(preDefaultCasesName)
				.append(" [label=\"preDefaultCases\"];\n");

		// default
		out.append(reduce(node.defaultCase));
		after.append(lhs)
				.append(name(node.defaultCase))
				.append(" [label=\"default\"];\n");


		// postDefaultCases
		String postDefaultCasesName = "struct_" + id++;
		out.append(indent())
				.append(postDefaultCasesName)
				.append(" [\n");
		++indentationLevel;
		out.append(indent())
				.append("shape=\"record\" ")
				.append(indent())
				.append("label=\"");
		first = true;
		for (Pair<NodeWithValue, Block> _case : node.postDefaultCases) {
			if (!first) {
				out.append(" | ");
			} else {
				first = false;
			}

			StringBuilder afterCase = new StringBuilder();

			String caseName = "case_" + id++;
			out.append('<')
					.append(caseName)
					.append("> case");

			++indentationLevel;

			Node left = _case.left();
			afterCase.append(reduce(left));

			Node right = _case.right();
			afterCase.append(reduce(right));

			--indentationLevel;
			after.append(indent())
					.append(postDefaultCasesName)
					.append(":")
					.append(caseName)
					.append(" -> ")
					.append(name(left))
					.append(" [label=\"test\"];\n")
					.append(indent())
					.append(postDefaultCasesName)
					.append(":")
					.append(caseName)
					.append(" -> ")
					.append(name(right))
					.append(" [label=\"consequent\"];\n")
					.append(afterCase);
		}
		out.append("\"\n");
		--indentationLevel;
		out.append(indent())
				.append("];\n");
		after.append(lhs)
				.append(postDefaultCasesName)
				.append(" [label=\"postDefaultCases\"];\n");


		--indentationLevel;
		return out.append(after).toString();
	}

	@Nonnull
	private String reduceThis(@Nonnull This node) {
		return ensureDeclared(node);
	}

	@Nonnull
	private String reduceThrow(@Nonnull Throw node) {
		String out = ensureDeclared(node);
		++indentationLevel;
		out += reduce(node.expression);
		String lhs = indent() + name(node) + " -> ";
		out += lhs + name(node.expression) + " [label=\"expression\"];\n";
		--indentationLevel;
		return out;
	}

	@Nonnull
	private String reduceTryCatch(@Nonnull TryCatch node) {
		StringBuilder out = new StringBuilder();
		StringBuilder after = new StringBuilder();
		out.append(ensureDeclared(node));
		++indentationLevel;
		String lhs = indent() + name(node) + " -> ";

		out.append(reduce(node.tryBody));
		after.append(lhs)
				.append(name(node.tryBody))
				.append(" [label=\"tryBody\"];\n");

		Pair<Variable, Block> pair = node.catchBody;
		if (variableNames.containsKey(pair.left())) {
			out.append(reduce(pair.right()));
			after.append(lhs)
					.append(variableNames.get(pair.left()))
					.append(" [label=\"catchVariable\"];\n")
					.append(lhs)
					.append(name(pair.right()))
					.append(" [label=\"catchBody\"];\n");
		} else {
			throw new RuntimeException("Undeclared reference!");
		}

		return out.append(after).toString();
	}

	@Nonnull
	private String reduceTryFinally(@Nonnull TryFinally node) {
		StringBuilder out = new StringBuilder();
		StringBuilder after = new StringBuilder();
		out.append(ensureDeclared(node));
		++indentationLevel;
		String lhs = indent() + name(node) + " -> ";

		out.append(reduce(node.tryBody));
		after.append(lhs)
				.append(name(node.tryBody))
				.append(" [label=\"tryBody\"];\n");

		out.append(reduce(node.finallyBody));
		after.append(lhs)
				.append(name(node.finallyBody))
				.append(" [label=\"finallyBody\"];\n");
		--indentationLevel;
		return out.append(after).toString();
	}

	@Nonnull
	private String reduceTypeCoercionNumber(@Nonnull TypeCoercionNumber node) {
		String out = ensureDeclared(node);
		++indentationLevel;
		out += reduce(node.expression);
		String lhs = indent() + name(node) + " -> ";
		out += lhs + name(node.expression) + " [label=\"expression\"];\n";
		--indentationLevel;
		return out;
	}

	@Nonnull
	private String reduceTypeCoercionObject(@Nonnull TypeCoercionObject node) {
		String out = ensureDeclared(node);
		++indentationLevel;
		out += reduce(node.expression);
		String lhs = indent() + name(node) + " -> ";
		out += lhs + name(node.expression) + " [label=\"expression\"];\n";
		--indentationLevel;
		return out;
	}

	@Nonnull
	private String reduceTypeCoercionString(@Nonnull TypeCoercionString node) {
		String out = ensureDeclared(node);
		++indentationLevel;
		out += reduce(node.expression);
		String lhs = indent() + name(node) + " -> ";
		out += lhs + name(node.expression) + " [label=\"expression\"];\n";
		--indentationLevel;
		return out;
	}

	@Nonnull
	private String reduceTypeofGlobal(@Nonnull TypeofGlobal node) {
		return ensureDeclared(node);
	}

	@Nonnull
	private String reduceVariableAssignment(@Nonnull VariableAssignment node) {
		String out = ensureDeclared(node);
		++indentationLevel;
		out += node.ref.either(this::reduce, this::reduce);
		out += reduce(node.value);
		String lhs = indent() + name(node) + " -> ";
		out += lhs + node.ref.either(this::name, this::name) + " [label=\"ref\"];\n";
		out += lhs + name(node.value) + " [label=\"value\"];\n";
		--indentationLevel;
		return out;
	}

	@Nonnull
	private String reduceVoid(@Nonnull Void node) {
		return ensureDeclared(node);
	}
}
