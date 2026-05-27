package com.fairpay.util

import kotlin.random.Random

object IdGenerator {

    fun newId8(): String =
        Random.nextInt(0, 100_000_000)
            .toString()
            .padStart(8, '0')
}