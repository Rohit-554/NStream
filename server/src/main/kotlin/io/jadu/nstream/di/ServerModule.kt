package io.jadu.nstream.di

import io.jadu.nstream.config.ServerConfig
import org.koin.core.module.Module
import org.koin.dsl.module

fun serverModule(config: ServerConfig) : Module = module {
    single { config }
}