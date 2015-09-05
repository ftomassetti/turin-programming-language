# turin-programming-language

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

It prints: _The protagonist is Ranma, 16"
