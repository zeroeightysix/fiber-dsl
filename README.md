# fiber-dsl

A small DSL for [fiber](https://github.com/DaemonicLabs/fiber)

## Usage

If you had a `ConfigNode` called `a`:
```kotlin
val a = ConfigNode("Foo", "Bar", false)
```

Transform your old, unreadable builders from:
```kotlin
val value = ConfigValueBuilder(Int::class.java)
    .withName("Baz")
    .withComment("Qux")
    .withListener(BiConsumer { old, new -> println("Baz just changed from $old to $new") })
    .withDefaultValue(2)
    .constraints()
    .composite(CompositeType.OR)
    .minNumerical(5)
    .maxNumerical(0)
    .finishComposite()
    .finish()
    .build()
```
To a beautiful type-safe DSL:
```kotlin
val value = a.value<Int> {
    name { "Baz" }
    comment { "Qux" }
    listener { old, new ->
        println("Baz just changed from $old to $new")
    }
    defaultValue { 2 }
    constrained {
        or {
            min { 5 }
            max { 0 }
        }
    }
}
```