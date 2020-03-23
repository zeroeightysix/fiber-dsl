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

inline fun <reified E, reified T: Collection<E>> ConfigNode.aggregate(builder: (@FiberDslMarker ConfigValueBuilder<T, *>).() -> Unit): ConfigValue<T> {
    return ConfigValueBuilder.aggregate(T::class.java, E::class.java)
        .withParent(this)
        .apply(builder)
        .build()
}

/**
 * @see ConfigValueBuilder.scalar
 */
inline fun <reified T> ConfigNode.scalar(builder: (@FiberDslMarker ConfigValueBuilder<T, *>).() -> Unit): ConfigValue<T> {
    return ConfigValueBuilder.scalar(T::class.java)
        .withParent(this)
        .apply(builder)
        .build()
}

/**
 * @see ConfigValueBuilder.withName
 */
fun <T> ConfigValueBuilder<T, *>.name(lambda: () -> String) {
    withName(lambda())
}

/**
 * @see ConfigValueBuilder.withComment
 */
fun <T> ConfigValueBuilder<T, *>.comment(lambda: () -> String) {
    withComment(lambda())
}

/**
 * @see ConfigValueBuilder.withListener
 */
fun <T> ConfigValueBuilder<T, *>.listener(lambda: (T, T) -> Unit) {
    withListener(lambda)
}

/**
 * @see ConfigValueBuilder.withListener
 */
fun <T> ConfigValueBuilder<T, *>.listener(lambda: (T) -> Unit) {
    withListener { _, value ->
        lambda(value)
    }
}

/**
 * @see ConfigValueBuilder.withDefaultValue
 */
fun <T> ConfigValueBuilder<T, *>.defaultValue(lambda: () -> T) {
    withDefaultValue(lambda())
}

/**
 * @see ConfigValueBuilder.withParent
 */
fun <T> ConfigValueBuilder<T, *>.parent(lambda: () -> Node) {
    withParent(lambda())
}

/**
 * @see ConfigValueBuilder.final
 */
fun <T> ConfigValueBuilder<T, *>.final() {
    final(true)
}

/**
 * @see ConfigValueBuilder.setFinal
 */
fun <T> ConfigValueBuilder<T, *>.final(final: Boolean) {
    setFinal(final)
}

//// Generic constraints

/**
 * @see ConfigValueBuilder.constraints
 */
fun <T> ConfigValueBuilder<T, *>.constrained(builder: (@FiberDslMarker ConstraintsBuilder<*, T, *>).() -> Unit) {
    constraints()
        .apply(builder)
        .finish()
}

// Numerical constraints

/**
 * @see ConstraintsBuilder.atLeast
 */
fun <T: Number> AbstractConstraintsBuilder<*, T, T, *>.atLeast(lambda: () -> T) {
    atLeast(lambda())
}

/**
 * @see ConstraintsBuilder.atMost
 */
fun <T: Number> AbstractConstraintsBuilder<*, T, T, *>.atMost(lambda: () -> T) {
    atMost(lambda())
}

// Char sequence constraints

fun <T: CharSequence> AbstractConstraintsBuilder<*, T, T, *>.minLength(lambda: () -> Int) {
    minLength(lambda())
}

fun <T: CharSequence> AbstractConstraintsBuilder<*, T, T, *>.maxLength(lambda: () -> Int) {
    maxLength(lambda())
}

fun <T: CharSequence> AbstractConstraintsBuilder<*, T, T, *>.matchesRegex(lambda: () -> String) {
    regex(lambda())
}

// Aggregate constraints

fun <T: Collection<*>> AbstractConstraintsBuilder<*, T, T, *>.minCollectionSize(lambda: () -> Int) {
    minLength(lambda())
}

fun <T: Collection<*>> AbstractConstraintsBuilder<*, T, T, *>.maxCollectionSize(lambda: () -> Int) {
    maxLength(lambda())
}

fun AbstractConstraintsBuilder<*, Array<*>, Array<*>, *>.minArrayLength(lambda: () -> Int) {
    minLength(lambda())
}

fun AbstractConstraintsBuilder<*, Array<*>, Array<*>, *>.maxArrayLength(lambda: () -> Int) {
    maxLength(lambda())
}

//// Top-level constraints (because we don't allow composites in composites)

/**
 * @see ConstraintsBuilder.composite
 */
fun <T> ConstraintsBuilder<*, T, *>.composite(type: CompositeType, lambda: (@FiberDslMarker CompositeConstraintBuilder<*, T>).() -> Unit) {
    composite(type)
        .apply(lambda)
        .finishComposite()
}

fun <T> ConstraintsBuilder<*, T, *>.and(lambda: (@FiberDslMarker CompositeConstraintBuilder<*, T>).() -> Unit) =
    composite(CompositeType.AND, lambda)

fun <T> ConstraintsBuilder<*, T, *>.or(lambda: (@FiberDslMarker CompositeConstraintBuilder<*, T>).() -> Unit) =
    composite(CompositeType.OR, lambda)

fun <T> ConstraintsBuilder<*, T, *>.invert(lambda: (@FiberDslMarker CompositeConstraintBuilder<*, T>).() -> Unit) =
    composite(CompositeType.INVERT, lambda)

// The all-mighty dsl marker

@DslMarker
@Target(AnnotationTarget.TYPE)
annotation class FiberDslMarker