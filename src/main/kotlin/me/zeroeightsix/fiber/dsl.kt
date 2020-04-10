package me.zeroeightsix.fiber

import me.zeroeightsix.fiber.builder.ConfigNodeBuilder
import me.zeroeightsix.fiber.builder.ConfigValueBuilder
import me.zeroeightsix.fiber.builder.constraint.AbstractConstraintsBuilder
import me.zeroeightsix.fiber.builder.constraint.CompositeConstraintsBuilder
import me.zeroeightsix.fiber.builder.constraint.ConstraintsBuilder
import me.zeroeightsix.fiber.constraint.CompositeType
import me.zeroeightsix.fiber.tree.ConfigNode
import me.zeroeightsix.fiber.tree.ConfigValue

fun rootNode(builder: (@FiberDslMarker ConfigNodeBuilder).() -> Unit): ConfigNode {
    return ConfigNodeBuilder()
        .apply(builder)
        .build()
}

fun ConfigNodeBuilder.node(name: String, builder: (@FiberDslMarker ConfigNodeBuilder).() -> Unit): ConfigNode {
    return fork(name)
        .apply(builder)
        .build()
}

fun ConfigNodeBuilder.comment(lambda: () -> String): ConfigNodeBuilder {
    return withComment(lambda())
}

fun ConfigNodeBuilder.serializeSeparately(lambda: () -> Boolean): ConfigNodeBuilder {
    return withSeparateSerialization(lambda())
}

fun ConfigNodeBuilder.serializeSeparately(): ConfigNodeBuilder {
    return serializeSeparately { true }
}

// Values

inline fun <reified E, reified T: Collection<E>> ConfigNodeBuilder.aggregate(name: String, defaultValue: T, builder: (@FiberDslMarker ConfigValueBuilder<T>).() -> Unit): ConfigValue<T> {
    return beginAggregateValue(name, defaultValue, E::class.java)
        .apply(builder)
        .build()
}

/**
 * @see ConfigValueBuilder.scalar
 */
inline fun <reified T> ConfigNodeBuilder.scalar(name: String, builder: (@FiberDslMarker ConfigValueBuilder<T>).() -> Unit): ConfigValue<T> {
    return beginValue(name, T::class.java)
        .apply(builder)
        .build()
}

/**
 * @see ConfigValueBuilder.withName
 */
fun <T> ConfigValueBuilder<T>.name(lambda: () -> String) {
    withName(lambda())
}

/**
 * @see ConfigValueBuilder.withComment
 */
fun <T> ConfigValueBuilder<T>.comment(lambda: () -> String) {
    withComment(lambda())
}

/**
 * @see ConfigValueBuilder.withListener
 */
fun <T> ConfigValueBuilder<T>.listener(lambda: (T, T) -> Unit) {
    withListener(lambda)
}

/**
 * @see ConfigValueBuilder.withListener
 */
fun <T> ConfigValueBuilder<T>.listener(lambda: (T) -> Unit) {
    withListener { _, value ->
        lambda(value)
    }
}

/**
 * @see ConfigValueBuilder.withDefaultValue
 */
fun <T> ConfigValueBuilder<T>.defaultValue(lambda: () -> T) {
    withDefaultValue(lambda())
}

/**
 * @see ConfigValueBuilder.final
 */
fun <T> ConfigValueBuilder<T>.final() {
    withFinality(true)
}

/**
 * @see ConfigValueBuilder.setFinal
 */
fun <T> ConfigValueBuilder<T>.final(final: Boolean) {
    withFinality(final)
}

//// Generic constraints

/**
 * @see ConfigValueBuilder.constraints
 */
fun <T> ConfigValueBuilder<T>.constrained(builder: (@FiberDslMarker ConstraintsBuilder<T>).() -> Unit) {
    beginConstraints()
        .apply(builder)
        .finishConstraints()
}

// Numerical constraints

/**
 * @see ConstraintsBuilder.atLeast
 */
fun <T: Number> AbstractConstraintsBuilder<*, T, T>.atLeast(lambda: () -> T) {
    atLeast(lambda())
}

/**
 * @see ConstraintsBuilder.atMost
 */
fun <T: Number> AbstractConstraintsBuilder<*, T, T>.atMost(lambda: () -> T) {
    atMost(lambda())
}

// Char sequence constraints

fun <T: CharSequence> AbstractConstraintsBuilder<*, T, T>.minLength(lambda: () -> Int) {
    minLength(lambda())
}

fun <T: CharSequence> AbstractConstraintsBuilder<*, T, T>.maxLength(lambda: () -> Int) {
    maxLength(lambda())
}

fun <T: CharSequence> AbstractConstraintsBuilder<*, T, T>.matchesRegex(lambda: () -> String) {
    regex(lambda())
}

// Aggregate constraints

fun <T: Collection<*>> AbstractConstraintsBuilder<*, T, T>.minCollectionSize(lambda: () -> Int) {
    minLength(lambda())
}

fun <T: Collection<*>> AbstractConstraintsBuilder<*, T, T>.maxCollectionSize(lambda: () -> Int) {
    maxLength(lambda())
}

fun AbstractConstraintsBuilder<*, Array<*>, Array<*>>.minArrayLength(lambda: () -> Int) {
    minLength(lambda())
}

fun AbstractConstraintsBuilder<*, Array<*>, Array<*>>.maxArrayLength(lambda: () -> Int) {
    maxLength(lambda())
}

//// Top-level constraints (because we don't allow composites in composites)

/**
 * @see ConstraintsBuilder.composite
 */
fun <T> ConstraintsBuilder<T>.composite(type: CompositeType, lambda: (@FiberDslMarker CompositeConstraintsBuilder<*, T>).() -> Unit) {
    composite(type)
        .apply(lambda)
        .finishComposite()
}

fun <T> ConstraintsBuilder<T>.and(lambda: (@FiberDslMarker CompositeConstraintsBuilder<*, T>).() -> Unit) =
    composite(CompositeType.AND, lambda)

fun <T> ConstraintsBuilder<T>.or(lambda: (@FiberDslMarker CompositeConstraintsBuilder<*, T>).() -> Unit) =
    composite(CompositeType.OR, lambda)

fun <T> ConstraintsBuilder<T>.invert(lambda: (@FiberDslMarker CompositeConstraintsBuilder<*, T>).() -> Unit) =
    composite(CompositeType.INVERT, lambda)

// The all-mighty dsl marker

@DslMarker
@Target(AnnotationTarget.TYPE)
annotation class FiberDslMarker