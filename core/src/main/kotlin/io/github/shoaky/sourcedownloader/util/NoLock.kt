package io.github.shoaky.sourcedownloader.util

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock

object NoLock : Lock {

    override fun lock() {
    }

    override fun lockInterruptibly() {
    }

    override fun tryLock(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun tryLock(time: Long, unit: TimeUnit): Boolean {
        throw UnsupportedOperationException()
    }

    override fun unlock() {
    }

    override fun newCondition(): Condition {
        throw UnsupportedOperationException()
    }
}

fun Lock.lock(block: () -> Unit) {
    try {
        lock()
        block()
    } finally {
        unlock()
    }
}