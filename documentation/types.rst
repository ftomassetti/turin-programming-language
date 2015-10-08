Primitive types
---------------

All the primitive Java types can be used in Turin.

In addition to them Turin introduces unsigned types:

* ubyte
* ushort
* uint
* ulong
* ufloat
* udouble

Those types are stored using the corresponding primitive types but the language ensures their values is always
equal or greater to zero.

Refer to Java Types
-------------------

All Java types can be referred in Turin.

Define new Types
----------------

Constructors
~~~~~~~~~~~~

Exactly as we do not like overloaded methods we do not like neither overloaded constructors, so each Turin type has exactly one
constructor. In simple cases the constructor can be just generated. Suppose you have a type like this one: ::

    type Point {
        int x
        int y
        int z default 0
        double distanceFromOrigin = Math.sqrt(x^2 + y^2 + z^2)
    }

In this case a constructor is defined which:

* takes tow values to assign to x and y
* does NOT take a value to assign to distanceFromOrigin (it always calculated from other values)
* can potentially take a value to assign to z, but this is not mandatory

That auto-generated constructor can be used like this: ::

    Point(1, 2)           // x=1, y=2, z=0, distanceFromOrigin =...
    Point(1, 2, 3)        // x=1, y=2, z=3, distanceFromOrigin =...
    Point(x=1, y=2, z=3)  // x=1, y=2, z=3, distanceFromOrigin =...
    Point(z=3, y=2, x=1)  // x=1, y=2, z=3, distanceFromOrigin =...
    Point(y=2, x=1)       // x=1, y=2, z=0, distanceFromOrigin =...

While passing four parameters or try to pass by name a value to distanceFromOrigin would cause an error.

This approach covers most cases and it is very simple and flexible. However there are two situations to consider:

* when a type extends another Turin type or a Java class we need to invoke also the super constructor
* when we desire to perform additional stuff in the constructor

Suppose for example that we create a type LabelledPoint which extends Point:

    type LabelledPoint extends Point {
        String label        
    }

In this case the auto-generated constructor will takes all the parameters from the base type and the newly defined
properties. It will just put all parameters without default values before all the others, so that LabelledPoint could
be initialized like this: ::

    LabelledPoint(1, 2, "hi")     // x=1, y=2, z=0, label="hi"
    LabelledPoint(1, 2, "hi", 3)  // x=1, y=2, z=3, label="hi"

And of course you can pass values by name.

However sometimes you want to introduce restrictions when extending a type or the extended type has several constructor
and Turin does not know which one to call. In this case you can define explicitly the constructor. Suppose for example
that all LabelledPoint should have a z value of 0, with no possibility to change it ::

    type LabelledPoint extends Point {
        String label

        init(int a, int b, String label) super(a, b) {
            this.label = label
        }
    }

The same constructor can be rewritten more concisely like this:

    init(int a, int b, String label) super(*) {
        this.label = label
    }

In this case the asterisk means "take all the parameters of this constructor named as parameter of the super type and just pass them".
However this shortcut cannot be used when the super type has multiple constructors.    

Generated methods
~~~~~~~~~~~~~~~~~

* constructor
* getters
* setters
* hashCode
* equals
* toString


