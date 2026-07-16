package io.jadu.nstream

import androidx.compose.ui.window.ComposeUIViewController
import io.jadu.nstream.di.appModule
import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(appModule)
    }
}

fun MainViewController() = ComposeUIViewController(
    configure = { initKoin() }
) { App() }