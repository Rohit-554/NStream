package io.jadu.nstream

import io.jadu.nstream.config.ServerConfig
import io.ktor.utils.io.charsets.name
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.util.Properties

interface EmailSender {
    fun send(to: String, subject: String, body: String)
}

class SmtpEmailSender(config: ServerConfig) : EmailSender {


    private val session = Session.getInstance(Properties().apply {
        put("mail.smtp.host", config.smtpHost)
        put("mail.smtp.port", config.smtpPort)
        put("mail.smtp.auth", "false")
    })

    private val from = config.smtpFrom
    override fun send(to: String, subject: String, body: String) {
        val message = MimeMessage(session).apply {
            setFrom(this@SmtpEmailSender.from)
            setRecipient(Message.RecipientType.TO, InternetAddress(to))
            setSubject(subject, Charsets.UTF_8.name())
            setText(body, Charsets.UTF_8.name())
        }
        Transport.send(message)
    }
}