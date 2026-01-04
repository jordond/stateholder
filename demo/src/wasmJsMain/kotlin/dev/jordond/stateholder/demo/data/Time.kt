package dev.jordond.stateholder.demo.data

import kotlin.js.Date

actual fun currentTimeMillis(): Long = Date.now().toLong()
