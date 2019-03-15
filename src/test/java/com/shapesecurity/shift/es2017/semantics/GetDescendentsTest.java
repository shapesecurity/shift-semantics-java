package com.shapesecurity.shift.es2017.semantics;

import com.shapesecurity.functional.data.ImmutableList;
import com.shapesecurity.shift.es2017.parser.Parser;
import com.shapesecurity.shift.es2017.semantics.asg.Node;
import com.shapesecurity.shift.es2017.semantics.visitor.GetDescendents;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.IdentityHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class GetDescendentsTest {

	// copied from JDK 10 java.lang.StringLatin1 to ensure consistency across JVM implementations
	private static int hashCode(byte[] value) {
		int h = 0;
		for (byte v : value) {
			h = 31 * h + (v & 0xff);
		}
		return h;
	}

	@Test
	public void testVisitor() throws Exception {
		String programText = "var z; typeof a; eval; ({ set a(b) { a = 0; }, get a(){} }).a = 0; " +
			"(function() { 'use strict'; try { undefined = 0; } catch (e) {} })(); " +
			"({a: 1}).a+(-1)+1-1*1/1%1|1&1^1<<1>>>1>>1; b = 1<1>1<=1>=1==1===1!=1!==!1;" +
			"b++; b in {}; f = (function (a) { return typeof 1;});" +
			"f(); f(1); f(1, []); f(1, [], null); f(1, [], null, this); f(1, [], null, this, /a/g); new f;" +
			"delete b; if (true) delete { a: 1}.a; if (false) ; else a = ~1; try { throw 'err'; } catch (e) {};" +
			"a instanceof Number; a = 2e308; a = 1.1; o = {}; (function() { 'use strict'; f(this); o.a = 1; delete o.a; })();" +
			"a = {'a':1}; for (b in a) ;" +
			"i = 1, j = 1; switch(i) { case --j: break; }" +
			"(function(a){arguments = {0:'a', 1:'b'}; arguments[0]=1; c = arguments[0]})(2);" +
			"(function A() { \n" +
			"  try { a = 1; return a;} catch(e) { a = 5; } finally { a = 2;}\n" +
			"})();" +
			"(function A() { \n" +
			"  label: try { } finally { break label; }; return 0;\n" +
			"})();";

		Semantics asg = Explicator.deriveSemantics(Parser.parseScript(programText));
		ImmutableList<Node> nodes = GetDescendents.getDescendants(asg.node);
		assertEquals(398, nodes.length);
		String allNodes = nodes.map(node -> node.getClass().getSimpleName()).foldLeft((acc, str) -> acc + "\n" + str, "");

		// hashing class names to avoid massive, unnecessary expectation
		// this hash will change if the ASG format/generation change, otherwise it should never change.
		assertEquals(139328831, hashCode(allNodes.getBytes(StandardCharsets.UTF_8)));
	}

	@Nonnull
	private static ImmutableList<Integer> getNodeLocations(@Nonnull ImmutableList<Node> nodes) {
		IdentityHashMap<Node, Integer> nodeOriginalLocation = new IdentityHashMap<>();

		return nodes.mapWithIndex((nodeIndex, node) -> {
			Integer originalLocation = nodeOriginalLocation.get(node);
			if (originalLocation == null) {
				nodeOriginalLocation.put(node, nodeIndex);
				return nodeIndex;
			} else {
				return originalLocation;
			}
		});
	}

	@Test
	public void testCyclicGraph() throws Exception {
		String programText = "x: {\n" +
			"  break x;\n" +
			"  break x;\n" +
			"  break x;\n" +
			"  break x;\n" +
			"  break x;\n" +
			"  break x;\n" +
			"  break x;\n" +
			"  break x;\n" +
			"}\n" +
			"{\n" +
			"  many(); things();\n" +
			"}";
		Semantics asg = Explicator.deriveSemantics(Parser.parseScript(programText));
		ImmutableList<Node> asgNodes = GetDescendents.getDescendants(asg.node);

		Semantics asgRebuilt = Util.rebuildSemantics(asg);
		ImmutableList<Node> asgRebuiltNodes = GetDescendents.getDescendants(asgRebuilt.node);

		assertEquals(asg, asgRebuilt);
		assertNotEquals(asgNodes, asgRebuiltNodes); // identity equals as Node doesn't implement equals

		ImmutableList<Integer> asgNodeLocations = getNodeLocations(asgNodes);
		ImmutableList<Integer> asgRebuiltNodeLocations = getNodeLocations(asgRebuiltNodes);
		assertEquals(asgNodeLocations, asgRebuiltNodeLocations);

		ImmutableList<Integer> monotonicExample = ImmutableList.empty();
		for (int i = asgNodeLocations.length - 1; i >= 0; --i) {
			monotonicExample = monotonicExample.cons(asgNodeLocations.length - 1 - i);
		}

		assertNotEquals(asgRebuilt, monotonicExample);
	}
}
