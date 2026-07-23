package io.jadu.nstream.di

import io.jadu.nstream.EmailSender
import io.jadu.nstream.SmtpEmailSender
import io.jadu.nstream.auth.AuthService
import io.jadu.nstream.auth.PasswordHasher
import io.jadu.nstream.auth.TokenService
import io.jadu.nstream.config.ServerConfig
import io.jadu.nstream.repository.AccountRepository
import io.jadu.nstream.repository.AccountTokenRepository
import io.jadu.nstream.repository.ExposedAccountRepository
import io.jadu.nstream.repository.ExposedAccountTokenRepository
import io.jadu.nstream.repository.ExposedRefreshSessionRepository
import io.jadu.nstream.repository.RefreshSessionRepository
import io.jadu.nstream.security.RedisRateLimiter
import io.jadu.nstream.watchmode.ContentResponseCache
import io.jadu.nstream.watchmode.WatchModeClient
import io.jadu.nstream.watchmode.WatchModelResponseCache
import org.koin.core.module.Module
import org.koin.dsl.module

fun serverModule(config: ServerConfig): Module = module {
    single { config }
    single { PasswordHasher() }
    single { TokenService(get()) }
    single<AccountRepository> { ExposedAccountRepository() }
    single<RefreshSessionRepository> { ExposedRefreshSessionRepository() }
    single<AccountTokenRepository> { ExposedAccountTokenRepository() }
    single<EmailSender> { SmtpEmailSender(get()) }
    single { RedisRateLimiter(get()) }
    single <ContentResponseCache> { WatchModelResponseCache(get()) }
    single { WatchModeClient(get(), get()) }
    single { AuthService(get(), get(), get(), get(), get(), get()) }
}