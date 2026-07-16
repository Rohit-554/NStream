package io.jadu.nstream

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform