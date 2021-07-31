package com.github.cimsbioko.server.util

import java.util.*

object IdUtil {
    fun generateUuid(): String = UUID.randomUUID().toString().replace("-", "")
}