Turin Programming Language's documentation
==========================================

Turin is intended to be a pragmatic language which leverages all the benefits of the JVM while trying to be more clear,
powerful and less redundant than Java.

Getting started
---------------

Turin is intended to be similar to Java in many respects. The philosophy is to introduce differences when an improvement
can be made. For other things we stick to the Java approach.

A few general things that are different are:

* newlines are meaningful: similarly to Python you cannot insert a newline everywhere in the code
* value and type identifiers are different: in Java classes tend to start with a capital letters while variable and
  parameter names tend to start with a lowercase letter. However this is not enforced by the language and the parser
  cannot take advantage from it. In Turin this is enforced instead. It is still possible to use Java types with a lower
  case letter or Java values with a capital letter by using escaping sequences.

Differences w.r.t. Java
=======================

In this section we examine more in details differences between Turin and Java.

.. toctree::
   :maxdepth: 2

   javadiff

Syntax
======

Here we describe step by step the Turin syntax.

.. toctree::
   :maxdepth: 2

   namespace
   imports
   functions
   properties
   types
   programs


