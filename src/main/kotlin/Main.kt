package org.example

import jakarta.activation.DataHandler
import jakarta.activation.DataSource
import jakarta.activation.FileDataSource
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import java.io.File
import java.util.*

fun main() {
//    sendEmail()
    fetchEmails()
//    fetch LastEmailWithAttachment()
}

val username = "bandukov_ilya@mail.ru"
val password = "GH1BcaH4dNLtu8uTN5em"

fun sendEmail() {
    val host = "smtp.mail.ru"
    val port = 465

    val toEmail = "bandukov_ilya@mail.ru"
    val subject = "Лабораторная работа №6. Почтовый сервер"
    val currentTime = java.time.LocalDateTime.now().toString()

    val messageText = """
        Бандуков Илья Юрьевич БСБО-12-22
        Бандуков Илья Юрьевич БСБО-12-22
        Время отправки: $currentTime
    """.trimIndent()

    val props = Properties().apply {
        put("mail.smtp.host", host)
        put("mail.smtp.port", port)
        put("mail.smtp.ssl.enable", "true")
        put("mail.smtp.auth", "true")
    }

    val imagePath = "D:/Study/РТУ МИРЭА/3 курс 6 сем/Разработка веб приложений/6/mail/src/main/kotlin/P1050187.JPG"  // Или используйте ресурсы в Android
    val imageFile = File(imagePath)

    if (!imageFile.exists()) {
        println("Файл изображения не найден!")
        return
    }

    val session = Session.getInstance(props, object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(username, password)
        }
    })

    try {
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(username))
            setRecipient(Message.RecipientType.TO, InternetAddress(toEmail))
            setSubject(subject, "UTF-8")
            setText(messageText, "UTF-8")
            setHeader("X-Priority", "1")  // Важное
        }

        val multipart = MimeMultipart()

        val textPart = MimeBodyPart().apply {
            setText(messageText, "UTF-8")
        }
        multipart.addBodyPart(textPart)

        val imagePart = MimeBodyPart().apply {
            val source: DataSource = FileDataSource(imageFile)
            dataHandler = DataHandler(source)
            fileName = "lab6_image.png"
            setHeader("Content-ID", "<image>")
        }
        multipart.addBodyPart(imagePart)

        message.setContent(multipart)

        Transport.send(message)
        println("Письмо успешно отправлено!")
    } catch (e: Exception) {
        println("Ошибка при отправке: ${e.message}")
    }
}

fun fetchEmails() {
    val host = "imap.mail.ru"
    val port = 993

    val props = Properties().apply {
        put("mail.imap.host", host)
        put("mail.imap.port", port)
        put("mail.imap.ssl.enable", "true")
        put("mail.imap.auth", "true")
    }

    try {
        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })

        val store = session.getStore("imap")
        store.connect()

        val inbox = store.getFolder("INBOX")
        inbox.open(Folder.READ_ONLY)

        val totalEmails = inbox.messageCount
        println("Всего писем в ящике: $totalEmails")

        println("\nЗаголовки 5 последних писем:")
        val messages = inbox.getMessages(totalEmails - 4, totalEmails)
        for (msg in messages) {
            println("""
                От: ${(msg.from[0] as InternetAddress).address}
                Тема: ${msg.subject}
                Дата: ${msg.sentDate}
            """.trimIndent() + "\n")
        }

        println("\nПоиск письма от одногрупника:")
        val groupmateEmail = username
        val searchTerm = "Бандуков Илья Юрьевич БСБО-12-22"
        var foundMessage: Message? = null

        for (i in totalEmails downTo 1) {
            val msg = inbox.getMessage(i)
            if (msg.from.any { (it as InternetAddress).address.equals(groupmateEmail, true) } ||
                msg.subject?.contains(searchTerm, true) == true) {
                foundMessage = msg
                break
            }
        }

        if (foundMessage != null) {
            println("Найдено письмо от одногрупника:")
            println("От: ${(foundMessage.from[0] as InternetAddress).address}")
            println("Тема: ${foundMessage.subject}")
            println("Дата: ${foundMessage.sentDate}")
            println("Текст письма:\n${getTextFromMessage(foundMessage)}")
        } else {
            println("Письмо от одногрупника не найдено.")
        }

        inbox.close(false)
        store.close()
    } catch (e: Exception) {
        println("Ошибка при получении писем: ${e.message}")
    }
}

private fun getTextFromMessage(msg: Message): String {
    val multipart = msg.content as Multipart
    val text = StringBuilder()
    for (i in 0 until multipart.count) {
        val part = multipart.getBodyPart(i)
        if (part.isMimeType("text/plain")) {
            text.append(part.content.toString())
        }
    }
    return text.toString()
}

fun fetchLastEmailWithAttachment() {
    val host = "imap.mail.ru"
    val port = 993
    val saveDir = "D:/Study/РТУ МИРЭА/3 курс 6 сем/Разработка веб приложений/6/mail/src/main/kotlin" // Папка для сохранения вложений

    val props = Properties().apply {
        put("mail.imap.host", host)
        put("mail.imap.port", port)
        put("mail.imap.ssl.enable", "true")
        put("mail.imap.auth", "true")
    }

    try {
        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })
        val store = session.getStore("imap")
        store.connect()

        val inbox = store.getFolder("INBOX")
        inbox.open(Folder.READ_ONLY)

        val lastMessage = inbox.getMessage(inbox.messageCount)



        val multipart = lastMessage.content as Multipart

        for (i in 0 until multipart.count) {
            val part = multipart.getBodyPart(i)
            if (Part.ATTACHMENT.equals(part.disposition, ignoreCase = true)) {
                val fileName = part.fileName ?: "attachment_${System.currentTimeMillis()}"
                val filePath = saveDir + fileName

                (part as MimeBodyPart).saveFile(filePath)
                println("Вложение сохранено: $filePath")
            }
        }

        inbox.close(false)
        store.close()
    } catch (e: Exception) {
        println("Ошибка: ${e.message}")
    }
}