package com.example

import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object EmailService {
    private const val SMTP_HOST = "smtp.yandex.ru"
    private const val SMTP_PORT = "465"
    private const val SMTP_USER = "perfiljevkiver@yandex.ru"
    private const val SMTP_PASSWORD = "nuuxizcgsssknurl"

    fun sendResetEmail(to: String, code: String) {
        val userName = transaction {
            Users.select { Users.email eq to }
                .mapNotNull { it[Users.name] }
                .firstOrNull() ?: "Пользователь"
        }

        val props = Properties().apply {
            put("mail.smtp.host", SMTP_HOST)
            put("mail.smtp.port", SMTP_PORT)
            put("mail.smtp.auth", "true")
            put("mail.smtp.ssl.enable", "true")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(SMTP_USER, SMTP_PASSWORD)
            }
        })

        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(SMTP_USER, "Support Team"))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                subject = "🔐 Восстановление пароля"

                val htmlContent = """
                    <html>
                        <body style="font-family: Arial, sans-serif; line-height: 1.5; color: #333;">
                            <h2 style="color: #007bff;">Восстановление пароля</h2>
                            <p>Здравствуйте, <b>$userName</b>,</p>
                            <p>Вы запросили восстановление пароля. Введите следующий код:</p>
                            <p style="font-size: 24px; font-weight: bold; color: #d9534f; background: #f8d7da; padding: 10px; border-radius: 5px; text-align: center;">
                                $code
                            </p>
                            <p>Если вы не запрашивали восстановление пароля, просто проигнорируйте это сообщение.</p>
                            <hr>
                            <p style="font-size: 12px; color: #888;">Это автоматическое сообщение, пожалуйста, не отвечайте на него.</p>
                        </body>
                    </html>
                """.trimIndent()

                setContent(htmlContent, "text/html; charset=UTF-8")
            }

            Transport.send(message)
            println("✅ Email успешно отправлен на $to через Яндекс SMTP")
        } catch (e: MessagingException) {
            e.printStackTrace()
            println("❌ Ошибка при отправке email через Яндекс: ${e.message}")
        }
    }
}




