package me.zeroeightsix.fiber

import me.zeroeightsix.fiber.builder.ConfigValueBuilder
import me.zeroeightsix.fiber.builder.constraint.AbstractConstraintsBuilder
import me.zeroeightsix.fiber.builder.constraint.CompositeConstraintBuilder
import me.zeroeightsix.fiber.builder.constraint.ConstraintsBuilder
import me.zeroeightsix.fiber.constraint.CompositeType
import me.zeroeightsix.fiber.tree.ConfigNode
import me.zeroeightsix.fiber.tree.ConfigValue
import me.zeroeightsix.fiber.tree.Node

// The reason we don't use the same builder/dsl pattern for name, comment, serialisation is because we want to set the parent of ConfigValues created inside the dsl.
// If we were to use a builder pattern, we wouldn't have an instance of ConfigNode to use in `withParent` in ConfigNode#value{}
fun node(name: String? = null, comment: String? = null, serializeSeparately: Boolean = false, builder: (@FiberDslMarker ConfigNode).() -> Unit): ConfigNode {
    return ConfigNode(name, comment, serializeSeparately)
        .apply(builder)
}

// Values

inline fun <reified T> ConfigNode.value(builder: (@FiberDslMarker ConfigValueBuilder<T>).() -> Unit): ConfigValue<T> {
    return ConfigValueBuilder(T::class.java)
        .withParent(this)
        .apply(builder)
        .build()
}

fun <T> ConfigValueBuilder<T>.name(lambda: () -> String) {
    withName(lambda())
}

fun <T> ConfigValueBuilder<T>.comment(lambda: () -> String) {
    withComment(lambda())
}

fun <T> ConfigValueBuilder<T>.listener(lambda: (T, T) -> Unit) {
    withListener(lambda)
}

fun <T> ConfigValueBuilder<T>.listener(lambda: (T) -> Unit) {
    withListener { _, value ->
        lambda(value)
    }
}

fun <T> ConfigValueBuilder<T>.defaultValue(lambda: () -> T) {
    withDefaultValue(lambda())
}

fun <T> ConfigValueBuilder<T>.parent(lambda: () -> Node) {
    withParent(lambda())
}

fun <T> ConfigValueBuilder<T>.final() {
    final(true)
}

fun <T> ConfigValueBuilder<T>.final(final: Boolean) {
    setFinal(final)
}

//// Generic constraints

fun <T> ConfigValueBuilder<T>.constrained(builder: (@FiberDslMarker ConstraintsBuilder<T>).() -> Unit) {
    constraints()
        .apply(builder)
        .finish()
}

// Numerical constraints

fun <T: Number> AbstractConstraintsBuilder<T>.min(lambda: () -> T) {
    addNumericalLowerBound(lambda())
}

fun <T: Number> AbstractConstraintsBuilder<T>.max(lambda: () -> T) {
    addNumericalUpperBound(lambda())
}

//// Top-level constraints (because we don't allow composites in composites)

fun <T> ConstraintsBuilder<T>.composite(type: CompositeType, lambda: CompositeConstraintBuilder<T>.() -> Unit) {
    composite(type)
        .apply(lambda)
        .finishComposite()
}

fun <T> ConstraintsBuilder<T>.and(lambda: CompositeConstraintBuilder<T>.() -> Unit) =
    composite(CompositeType.AND, lambda)

fun <T> ConstraintsBuilder<T>.or(lambda: CompositeConstraintBuilder<T>.() -> Unit) =
    composite(CompositeType.OR, lambda)

fun <T> ConstraintsBuilder<T>.invert(lambda: CompositeConstraintBuilder<T>.() -> Unit) =
    composite(CompositeType.INVERT, lambda)

// The all-mighty dsl marker

@DslMarker
@Target(AnnotationTarget.TYPE)
annotation class FiberDslMarker