package io.jadu.nstream.di

import org.koin.core.module.Module
import org.koin.dsl.module

fun appModule(): Module = module {

}

val appModule = listOf(
    appModule(),
    platformModule()
)