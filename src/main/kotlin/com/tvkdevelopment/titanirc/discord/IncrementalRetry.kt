package com.tvkdevelopment.titanirc.discord

import com.tvkdevelopment.titanirc.util.Log
import dev.kord.gateway.retry.Retry
import kotlinx.coroutines.delay
import kotlin.time.Duration

class IncrementalRetry(
    private val backoffStart: Duration,
    private val backoffStepSize: Duration,
    private val backoffMax: Duration,
) : Retry {

    init {
        require(backoffStart.isPositive()) { "backoffStart needs to be positive but was ${backoffStart.inWholeMilliseconds} ms" }
        require(backoffMax.isPositive()) { "backoffMax needs to be positive but was ${backoffMax.inWholeMilliseconds} ms" }
        require(
            backoffMax.minus(backoffStart).isPositive()
        ) { "backoffMax ${backoffMax.inWholeMilliseconds} ms needs to be bigger than backoffStart ${backoffStart.inWholeMilliseconds} ms" }
        require(backoffStepSize.isPositive()) { "backoffStepSize needs to be positive but was ${backoffMax.inWholeMilliseconds} ms" }
    }

    override val hasNext: Boolean = true

    private var tries: Int = 0

    override fun reset() {
        tries = 0
    }

    override suspend fun retry() {
        ++tries
        val backoff = (backoffStart + backoffStepSize * tries).coerceAtMost(backoffMax)

        Log.i("Discord retry attempt $tries, delaying for $backoff")
        delay(backoff)
    }
}
