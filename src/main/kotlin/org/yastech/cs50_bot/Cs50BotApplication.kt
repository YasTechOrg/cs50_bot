package org.yastech.cs50_bot

import com.elbekd.bot.Bot
import com.elbekd.bot.model.toChatId
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Cs50BotApplication

fun main(args: Array<String>)
{
	runApplication<Cs50BotApplication>(*args)

	// Set Bot Token
	val token = "5412773834:AAFBi28R150rMjbOXbdLizi_JPmG6Z4X0TU"

	// Creat Bot
	val bot = Bot.createPolling(token)

	// Start Command
	bot.onCommand("/start") { (msg, _)  ->
		bot.sendMessage(msg.chat.id.toChatId(), "خوش آمدید!\n" +
				"چه کمکی از من ساخته است ؟\n" +
				"\n" +
				"/start - شروع کار با ربات\n" +
				"/help - راهنمای ربات\n" +
				"/generate - ساخت تصویر نویسه جدید\n" +
				"\n" +
				"برای پشتیبانی بات با این ایمیل در تماس باشید : yastechorg@gmail.com")
	}

	// Help Command
	bot.onCommand("/help") { (msg, _) ->
		bot.sendMessage(msg.chat.id.toChatId(), "دستورات ربات :\n" +
				"\n" +
				"/start - شروع کار با ربات\n" +
				"/help - راهنمای ربات\n" +
				"/generate - ساخت تصویر نویسه جدید\n" +
				"\n" +
				"برای پشتیبانی بات با این ایمیل در تماس باشید : yastechorg@gmail.com")
	}

	// Generate Command
	bot.onCommand("/generate") { (msg, _) ->
		bot.sendMessage(msg.chat.id.toChatId(), "On Development!")
	}

	// Start Bot
	bot.start()
}