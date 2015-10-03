Differences with Java
---------------------

Why should someone use Turin instead of Java? In our opinion Turin is in general cleaner and benefit from the experience
of using Java for 20 years to find improvements. In this section we list a few specific points.

Named parameters
~~~~~~~~~~~~~~~~

Turin supports named parameters. Named parameters can be used together with positional parameters and are particularly
useful to make code clearer and in conjunction with default parameters.

Suppose you have a method for parsing code which takes several parameters: ::

    Node parse(String code, boolean parseStatements,
               boolean parseComments, boolean stopOnError)

When invoking this method in Java we could write code like this: ::

    parse("my code", false, true, false)

Is not so easy to get which parameter mean what, right? Isn't possible to get confused and to do the wrong thing in
such case? In Turin you could call this method like this, if you want or: ::

    parse("my code", parseStatements=false,
          parseComments=true, stopOnError=false)

Isn't that clear? Consider that from Turin you can use named parameters also when invoking Java code. The goal is to make
nicer to use Java code from Turin than from Java.

While parameters names are not used by Java, they are
usually present in compiled code. If they are available while compiling code we can use them.

Default parameters
~~~~~~~~~~~~~~~~~~

TBW

Functions
~~~~~~~~~

TBW

Type inference
~~~~~~~~~~~~~~

TBW

No overloaded methods
~~~~~~~~~~~~~~~~~~~~~

TBW