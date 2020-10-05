package com.handtruth.kommon.concurrent.test

import kotlinx.atomicfu.atomic

class Resource<T>(initial: T) {

    private val shared = atomic(initial)
    private val state = atomic(States.Free)

    fun begin(newState: States) {
        val current = state.getAndSet(newState)
        assert(current == States.Free || current == States.Read && newState == States.Read)
    }

    fun free(expect: States) {
        val actual = state.getAndSet(States.Free)
        assert(expect == actual || expect == States.Read && actual == States.Free) {
            "e: $expect, a: $actual"
        }
    }

    inline fun <R> perform(state: States, block: () -> R): R {
        begin(state)
        try {
            return block()
        } finally {
            free(state)
        }
    }

    inline fun <R> read(block: () -> R): R {
        return perform(States.Read, block)
    }

    inline fun <R> write(block: () -> R): R {
        return perform(States.Write, block)
    }

    enum class States {
        Free, Read, Write
    }
}
