# Turin programming language

Turin is a static language compiled for the JVM.

# Philosophy

Programming should be about describe data, phenomenons, processes and interactions. The language should not stay in the way to let us focus on this fun and challenging task.

# Goals

The initial goal is to complement Java for a series of tasks for which it is not strongly suited. The initial focus is on the definition of datatypes with proper defaults (no need to write or generated hashCode, equals, getters and setters in most cases).

We want to be pragmatic: to start with a small language and make it usable in practice as soon as possible. To do so we are working on an [IntelliJ plugin](https://github.com/ftomassetti/turin-intellij-plugin) and we plan to start working soon on a Maven plugin.

# Status

Types with properties and methods can be defined and can compiled to class files. It is possible to invoke Java from Turin and viceversa.

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

# License

Turin is released under the Apache License v2.0

# Contributing

All sorts of questions, advices and contributions are extremely welcome!
