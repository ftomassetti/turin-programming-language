# Turin programming language

[![Build Status](https://travis-ci.org/ftomassetti/turin-programming-language.svg?branch=master)](https://travis-ci.org/ftomassetti/turin-programming-language)
[![Documentation](https://readthedocs.org/projects/turin-programming-language/badge/?version=latest)](http://turin-programming-language.readthedocs.org/en/latest/?badge=latest)

Turin is a pragmatic static language for the JVM.

# Documentation

Available [here](http://turin-programming-language.readthedocs.org/en/latest/).

# Philosophy

Programming should be about describe data, phenomenons, processes and interactions. The language should not stay in the way to let us focus on this fun and challenging task.

# Goals

The initial goal is to complement Java for a series of tasks for which it is not strongly suited. The initial focus is on the definition of datatypes with proper defaults (no need to write or generated hashCode, equals, getters and setters in most cases).

We want to be pragmatic: to start with a small language and make it usable in practice as soon as possible. To do so we are working on an [IntelliJ plugin](https://github.com/ftomassetti/turin-intellij-plugin) and we plan to start working soon on a Maven plugin.

# Status

Types with properties and methods can be defined and can compiled to class files. It is possible to invoke Java from Turin and viceversa.

# Supporting tools

* [turin-maven-plugin](https://github.com/ftomassetti/turin-maven-plugin)
* [turin-intellij-plugin](https://github.com/ftomassetti/turin-intellij-plugin)

# Examples

This is an example of a program (compiling & running) written in Turin.

```
namespace manga

// now print is an alias for call to all the overload variants of println on System.out
import java.lang.System.out.println as print

// we define this property in generale: a name is a String
property String : name

// this is our new datatype
type MangaCharacter {
    // we refer to the property defined above
    has name
    // we define a new property, UInt is an unsigned int
    has UInt : age
    
    // we overload toString. For short methods it can make sense to use 
    // this shorthand. And we have string interpolation
    String toString() = "#{name}, #{age}"
}

// this define a class with a main method
program MangaExample(String[] args) {
    // type inference at work: we are instantiating the class defined above
    // note that the constructor is generated for us
    val ranma = MangaCharacter("Ranma", 16)
    // let's call a java method ( System.out.println(String) ) and use more
    // string interpolation
    print("The protagonist is #{ranma}")
}
```

It prints: _The protagonist is Ranma, 16_

# Applications in Turin

So far I started working on one application in Turin. It is a [java-formatter](https://github.com/ftomassetti/java-formatter).

# License

Turin is released under the Apache License v2.0

# Contributing

All sorts of questions, advices and contributions are extremely welcome!
