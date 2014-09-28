ChangeNodes
===========
ChangeNodes implements a Tree Differencing algorithmn, based on the paper "Change Detection in Hierarchically Structured Information" by Chawathe et. al. It takes as input two AST nodes and outputs a minimal edit script that, when applied, transforms the first AST into the second one. The edit script will contain the following operations:

* Insert: a node is inserted in the AST
* Delete: a node is removed from the AST
* Move: a node is moved to a different location in the AST
* Update: a node is updated/replaced with a different node

ChangeNodes directly works on [JDT nodes](http://help.eclipse.org/juno/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2FASTNode.html) provided by Eclipse. The code is an adaptation from ChangeDistiller, which can be found on [bitbucket](https://bitbucket.org/sealuzh/tools-changedistiller). The main differences are that ChangeDistiller first transforms the AST to their own AST representation, which is language agnostic. ChangeNodes directly uses the JDT nodes, which is aware of the represented language (namely Java).

We make use of the same heuristics as ChangeDistiller in the matching strategy. Due to using JDT nodes we do differ in some aspects. For example, when two nodes match we automatically match all ChildProperties of that node, as their location is fixed in the AST. For example, if a MethodDeclaration matches then its Name will also match.

The current implementation has been used on large-scale projects in an automated way. Manual inspection of some of the results indicates it is working properly, although the edit script is not always minimal (mainly due to incorrect matching of some nodes).



Usage
-----
We mainly use ChangeNodes in Clojure. You somehow need to get two AST nodes (typically CompilationUnits) using the Eclipse API. Once you have those you can feed them to ChangeNodes using the following code:

    (def diff (new changenodes.Differencer left-compilation-unit right-compilation-unit))
    (.difference diff)
    (.getOperations diff)
    
ChangeNodes is meant to be used with QwalKeko, a history querying tool that reasons over projects stored in git. You can find it [here](https://github.com/ReinoutStevens/damp.qwalkeko). Examples can be found [here](https://github.com/ReinoutStevens/damp.qwalkeko/blob/master/damp.qwalkeko.plugin/src/qwalkeko/clj/changenodes.clj) and [here](https://github.com/ReinoutStevens/damp.qwalkeko/blob/master/damp.qwalkeko.plugin/src/qwalkeko/experiments/selenium.clj).


Installation
------------
ChangeNodes is configured to be an Eclipse plugin. Installation is done by cloning this repository and importing it as a new Eclipse project. It includes all of its dependencies.

The Competition
---------------
Recently I stumbled upon [gumtree](https://github.com/jrfaller/gumtree) which looks like a nice tool as well that also features an implementation of ChangeDistiller. If ChangeNodes does not do what you want it to do you may want to check it out.
