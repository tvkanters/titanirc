package com.tvkdevelopment.titanirc

import io.mockk.MockKVerificationScope
import io.mockk.mockk
import kotlin.reflect.KClass

inline fun <reified T : Any> niceMockk(
    name: String? = null,
    relaxed: Boolean = true,
    vararg moreInterfaces: KClass<*>,
    relaxUnitFun: Boolean = true,
    block: T.() -> Unit = {}
): T = mockk(
    name,
    relaxed,
    *moreInterfaces,
    relaxUnitFun = relaxUnitFun,
    block = block
)

fun MockKVerificationScope.contains(subString: String) =
    match<String> { it.contains(subString) }