package me.okonecny.interactivetext

import java.util.concurrent.atomic.AtomicLong

typealias InteractiveId = Long

class LinearInteractiveIdGenerator {
    companion object {
        const val firstInteractiveId: InteractiveId = 0
        const val invalidInteractiveId: InteractiveId = -1
    }

    private val currentId: AtomicLong = AtomicLong(firstInteractiveId)

    fun generateId(): InteractiveId {
        var id: InteractiveId = currentId.getAndIncrement()
        while (id == invalidInteractiveId) {
            id = currentId.getAndIncrement()
        }
        return id
    }
}