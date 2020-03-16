package me.zeroeightsix.fiber

import me.zeroeightsix.fiber.tree.ConfigNode
import me.zeroeightsix.fiber.tree.ConfigValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class DslKtTest {

    @DisplayName("Test dsl-made node")
    @Test
    fun node() {
        val node = node(
            name = "Foo",
            comment = "Bar",
            serializeSeparately = false
        ) {
            value<Int> {
                name { "Baz" }
                constrained {
                    or {
                        biggerThan { 5 }
                        smallerThan { 0 }
                    }
                }
            }
        }

        assertEquals("Foo", node.name, "Node name is correct")
        assertEquals("Bar", node.comment, "Node comment is correct")
        assertEquals(false, node.isSerializedSeparately, "Node serialized separately property is correct")

        assertEquals(1, node.items.size, "Node has one child")
        node.items.firstOrNull()?.let {
            assertEquals("Baz", it.name, "Child name is correct")

            val value = it as ConfigValue<Int>

            assertEquals(false, it.setValue(2), "Child constraints were applied correctly")
            assertEquals(true, it.setValue(10), "Child constraints were applied correctly")
            assertEquals(true, it.setValue(-5), "Child constraints were applied correctly")
        }
    }

}