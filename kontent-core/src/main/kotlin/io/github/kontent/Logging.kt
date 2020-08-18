package io.github.kontent

interface OperationalEvents {
    fun emit(event: OperationalEvent)

    companion object {
        val NoOp = object : OperationalEvents {
            override fun emit(event: OperationalEvent) = Unit
        }
    }
}

interface OperationalEvent

object PrintRawOperationalEvents : OperationalEvents {
    override fun emit(event: OperationalEvent) {
        println(event)
    }
}

data class BuildSucceeded(val pages: Int, val assets: Int): OperationalEvent