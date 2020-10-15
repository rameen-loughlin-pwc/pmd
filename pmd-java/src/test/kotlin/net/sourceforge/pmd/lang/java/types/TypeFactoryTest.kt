/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.types

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.apache.commons.lang3.reflect.TypeLiteral

/**
 * @author Clément Fournier
 */
class TypeFactoryTest : FunSpec({

    val ts = testTypeSystem

    test("Test primitive types are reused") {

        listOf(true, false).forEach { isErased ->
            ts.typeOf(ts.getClassSymbol(Integer.TYPE), isErased).shouldBeSameInstanceAs(ts.INT)
            ts.typeOf(ts.getClassSymbol(Character.TYPE), isErased).shouldBeSameInstanceAs(ts.CHAR)
            ts.typeOf(ts.getClassSymbol(Void.TYPE), isErased).shouldBeSameInstanceAs(ts.NO_TYPE)
            ts.typeOf(ts.getClassSymbol(Void::class.java), isErased).shouldBeSameInstanceAs(ts.BOXED_VOID)
            ts.typeOf(ts.getClassSymbol(Object::class.java), isErased).shouldBeSameInstanceAs(ts.OBJECT)
        }
    }

    test("Test toString") {

        ts.arrayType(ts.INT).apply {
            toString() shouldBe "int[]"
        }

        ts.wildcard(true, ts.OBJECT).apply {
            toString() shouldBe "?"
        }

        ts.wildcard(true, ts.CLONEABLE).apply {
            toString() shouldBe "? extends java.lang.Cloneable"
        }

    }

    test("Test init") {

        ts.OBJECT shouldNotBe null
        ts.NULL_TYPE shouldNotBe null
        ts.UNBOUNDED_WILD shouldNotBe null

        ts.ERROR shouldNotBe null
        ts.UNKNOWN shouldNotBe null
        ts.NO_TYPE shouldNotBe null

    }

    test("Test reflect parsing") {

        // on the left this uses fromReflect, on the right the manual construction methods

        with(TypeDslOf(testTypeSystem)) {
            mirrorOf<String>()                          shouldBe String::class.raw
            mirrorOf<Foo<String>>()                     shouldBe Foo::class[String::class]
            mirrorOf<Array<Foo<String>>>()              shouldBe Foo::class[String::class].toArray(1)
            mirrorOf<Array<Array<Foo<in String>>>>()    shouldBe Foo::class[`?` `super` String::class].toArray(2)
        }
    }
})


private class Foo<T>

/**
 * Note: [T] must not contain type variables.
 */
private inline fun <reified T> mirrorOf(): JTypeMirror =
        TypesFromReflection.fromReflect(testTypeSystem, object : TypeLiteral<T>() {}.type, LexicalScope.EMPTY, Substitution.EMPTY)!!

