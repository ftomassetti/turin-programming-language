# Turin programming language

Turin is a static language compiled for the JVM.

This is an example of a program (compiling & running) written in Turin.

```
namespace manga

import java.lang.System.out.println as print

property String : name

type MangaCharacter {
    has name
    has UInt : age

    String toString() = "#{name}, #{age}"
}

program MangaExample(String[] args) {
    val ranma = MangaCharacter("Ranma", 16)
    print("The protagonist is #{ranma}")
}
```

It prints: _The protagonist is Ranma, 16_

# Goals

The initial goal is to complement Java for a series of tasks for which it is not strongly suited. The initial focus is on the definition of datatypes with proper defaults (no need to write or generated hashCode, equals, getters and setters in most cases).
