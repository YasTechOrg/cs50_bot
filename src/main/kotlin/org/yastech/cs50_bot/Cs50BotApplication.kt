package org.yastech.cs50_bot

import com.elbekd.bot.Bot
import com.elbekd.bot.feature.chain.chain
import com.elbekd.bot.feature.chain.jumpTo
import com.elbekd.bot.feature.chain.jumpToAndFire
import com.elbekd.bot.model.toChatId
import com.elbekd.bot.types.*
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
	var nameFa = ""
	var nameEn: String
	var msgID = 0L
	bot.chain("/generate")
	{
		bot.jumpToAndFire("run", it)
	}.then("run"){
		bot.sendMessage(it.chat.id.toChatId(), "لطفا نام و نام خانوادگی خود را به فارسی وارد کنید!")
	}.then{
		nameFa = "${it.text}"
		bot.sendMessage(it.chat.id.toChatId(), "ازت ممنونم ${it.text}.\n" +
				"لطفا نام و نام خانوادگی خود را به انگلیسی وارد کنید!")
	}.then {
		nameEn ="${it.text}"
		bot.sendMessage(it.chat.id.toChatId(), "نام فارسی : $nameFa\n" +
				"نام انگلیسی : $nameEn\n" +
				"آیا اطلاعات وارد شده درست است ؟", replyMarkup = ReplyKeyboardMarkup( keyboard = listOf(
			listOf(
				KeyboardButton("لغو"),
				KeyboardButton("ویرایش اطلاعات"),
			),
			listOf(
				KeyboardButton("ساختن تصویر نوشته جدید!")
			)
		),
		oneTimeKeyboard = true)).let { msg ->
			msgID = msg.messageId
		}
	}.then {
		when(it.text)
		{
			"لغو" ->
			{
				bot.sendMessage(it.chat.id.toChatId(), "عملیات با موفقیت لغو شد!")
				bot.deleteMessage(it.chat.id.toChatId(), msgID)
			}
			"ویرایش اطلاعات" -> bot.jumpToAndFire("run", it)
			"ساختن تصویر نوشته جدید!" -> bot.sendMessage(it.chat.id.toChatId(), "ساحته شد !")
		}
	}.build()

	// Start Bot
	bot.start()
}