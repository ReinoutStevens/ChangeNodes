ChangeNodes
===========

A TreeDifferencing algorithm, which provided two AST nodes, outputs a TreeEditScript that when applied on the first AST transforms it into the second AST.
Currently the algorithm works for Eclipse's JDT nodes.

The algorithm is based on the paper "Change Detection in Hierarchically Structured Information" by Chawathe et. al.
It has been used in other tools as well, eg. ChangeDistiller ( https://bitbucket.org/sealuzh/tools-changedistiller ).

This is an early implementation of the algorithm, and parts still need to be improved.