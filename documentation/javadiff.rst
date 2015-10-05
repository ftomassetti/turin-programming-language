Why should someone use Turin instead of Java? In our opinion Turin is in general cleaner and benefit from the experience
of using Java for 20 years to find improvements. In this section we list a few specific points.

No overloaded methods
~~~~~~~~~~~~~~~~~~~~~

We think that overloaded methods can be confusing and they make method resolution much more obscure that it should be:
primitive conversion, auto-boxing/unboxing and type hierarchies make possible for a method call to match several methods
with the same name. For example Assert.assertEquals have several variants including one which takes two Objects and one
which takes two longs. Which one is invoked when passing to ints? Which one when passing two Integers?

We think a language should not introduce unnecessary complications so in Turin you cannot introduce overloaded methods,
however you can seamlessly invoke overloaded methods from Java classes.

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

Having named parameters we can also support easily default parameters: parameters which can be optionally specified,
otherwise they assume a sensible default value. They are a good alternative to overloaded methods (which are not supported
in Turin).

Parameters explosion
~~~~~~~~~~~~~~~~~~~~

::

    myMethod(*=anObject)

Functions
~~~~~~~~~

When Java was introduced it was promoting Object-Oriented Programming in an extremist way: no functions, just methods.
In practice people started immediately to create Utils classes with no constructor and a bunch of static methods.
We think that Utils classes are cluttered and useless: it makes more sense to just support function, which in fact can
be defined in Turin:

TBW

Type inference
~~~~~~~~~~~~~~

TBW
