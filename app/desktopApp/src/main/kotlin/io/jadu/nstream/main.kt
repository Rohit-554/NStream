package io.jadu.nstream

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.jadu.nstream.di.appModule
import org.koin.core.context.startKoin

fun main() = application {
    startKoin {
        modules(appModule)
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "NStream",
    ) {
        App()
    }
}