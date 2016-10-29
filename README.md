Shift Semantics
===============

This project derives a representation of an ECMAScript program's semantics from a [Shift AST](https://github.com/shapesecurity/shift-spec).

## Usage example

```java
import com.shapesecurity.shift.parser.Parser;
import com.shapesecurity.shift.semantics.Explicator;

Script program = Parser.parseScript(programText);
Semantics semantics = Explicator.deriveSemantics(program);
```

## Installation

In `pom.xml`, under `project.dependencies`, add this dependency.

```xml
<dependency>
  <groupId>com.shapesecurity</groupId>
  <artifactId>shift-semantics</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Purpose

There is a great deal of information about a given ECMAScript program which is specified by the ECMAScript spec but not explicitly represented in the Shift AST: everything from identifier resolution (which identifiers refer to which variables) to evaluation order. The explicator exposes as much of that information as is practical while discarding irrelevant details, such as the names of local variables. In short, it attempts to capture all of and only the information needed to actually execute a program, so that tools such as compilers need not concern themselves with details about the original source text.

## Design

The `Explicator` class exposes a single static method, `deriveSemantics`, which accepts either a `Script` or a `Module` and produces a `Semantics` instance, suitable for further transformation.

The `Semantics` class represents programs as Abstract Semantic Graphs (ASGs) together with information about which variables are declared at the top level of the program. The ASGs are nearly trees, but can contain back-edges representing jumps between an ASG `Break` node and an ASG `BreakTarget` node and generally have many references to each individual `Variable` (an object representing an ECMAScript variable). The nodes in the graph sometimes contain further information, such as the string in question attached to a `LiteralString` node.
 
The ASG has the property that `Break`s can only point to `BreakTarget` nodes. Thus, while control may exit a `Block` in the middle (e.g., to perform a function call or throw an exception), it will not enter at any point other than the beginning or at a `BreakTarget`. This makes transformations easier to perform safely.
 
The body of the `Explicator` class consists of a collection of `explicateSomething` methods, where `Something` is typically an AST node and which generally take an AST node and produce an ASG node by calling each other recursively. A boolean flag is passed to indicate if the code in question is strict-mode, and a further flag may be passed when explicating expressions to indicate if the result of the expression will be used. Along the way the explicator creates a list of any temporary variables introduced, which is then saved in the node representing the innermost function or script containing those variables.  

The explicator also relies on an AST visitor, [`FinallyJumpReducer.java`](src/main/java/com/shapesecurity/shift/semantics/visitor/FinallyJumpReducer.java). This reducer gives a map from AST break/continue nodes to the statement which they break, along with a count of the number of `finally` statements which are broken by the jump.

Note that the explicator is specifically for ECMAScript. As such, operations like `+` have ECMAScript semantics: `a+b` may be either string concatenation or mathematical addition, depending on the values of `a` and `b`, and may invoke as many as six unrelated functions (getter for `a` on the global object, getter for `b` on the global object, `a.valueOf`, `a.toString`, `b.valueOf`, `b.toString`). Anyone writing a compiler or implementation is cautioned to keep this in mind.

## Limitations

By design, the explicator cannot represent programs which make use of `with` or direct calls to `eval`, since these introduce dynamic scoping. Since it is not always possible to statically determine if a call is a direct call to `eval`, we forbid all calls which are precisely of the form `eval(...)`, which is sufficient to prevent all direct `eval` calls. All other ES5 features are supported at this stage.

Currently the explicator does not support any ES2015 features except for block-scoped variable declarations. Even for those, it does not enforce Temporal Dead Zone semantics, nor does it create new per-iteration bindings for `let` declarations in the initializers of loops.

The explicator deliberately discards some of the information contained in the AST, including:
* the names of local variables and labels
* the precise locations of function declaration statements
* the distinctions between most "syntactic sugar" constructs, including those between:
  * a `for` loop and a `while(true)` loop preceded by an initializer and containing conditional break
  * `++x` and `x = Number(x) + 1`
  * a conditional expression and an `if-else` statement
  * an unnamed function expression assigned to a variable and a function declaration

Furthermore, not all ASGs cleanly represent ECMAScript programs: for example, they can have jumps to the middle of loops, which is not possible in ECMAScript.

For these reasons, it is *not possible* to reconstruct the original AST from the output of the explicator. While a semantically equivalent AST may be derivable in some cases, this is neither currently implemented nor a design goal.

## Contributing

* Open a Github issue with a description of your desired change. If one exists already, leave a message stating that you are working on it with the date you expect it to be complete.
* Fork this repo, and clone the forked repo.
* Install dependencies with `mvn`.
* Build and test in your environment with `mvn compile test`.
* Create a feature branch. Make your changes. Add tests.
* Build and test in your environment with `mvn compile test`.
* Make a commit that includes the text "fixes #*XX*" where *XX* is the Github issue.
* Open a Pull Request on Github.

## License

    Copyright 2016 Shape Security, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
