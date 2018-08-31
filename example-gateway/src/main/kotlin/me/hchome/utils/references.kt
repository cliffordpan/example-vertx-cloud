package me.hchome.utils

import java.lang.ref.SoftReference
import kotlin.reflect.KProperty

class SoftReferenceDelegate<T>(private val creator: () -> T) {
    private var reference: SoftReference<T> = SoftReference(creator())

    operator fun getValue(thisRef: Any, property: KProperty<*>): T = reference.get() ?: creator().also {
        this.reference = SoftReference(it)
    }

    operator fun setValue(thisRef: Any, property: KProperty<*>, t: T) {
        reference = SoftReference(t)
    }
}

fun <T> soft(creator: () -> T) = SoftReferenceDelegate(creator)