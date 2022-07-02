package org.yastech.cs50_bot

import com.elbekd.bot.Bot
import com.elbekd.bot.feature.chain.chain
import com.elbekd.bot.feature.chain.jumpToAndFire
import com.elbekd.bot.model.toChatId
import com.elbekd.bot.types.KeyboardButton
import com.elbekd.bot.types.ReplyKeyboardMarkup
import com.elbekd.bot.util.SendingByteArray
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.util.ResourceUtils
import java.awt.*
import java.awt.font.TextAttribute
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.URL
import java.text.AttributedString
import javax.imageio.ImageIO


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
	var nameEn = ""
	var msgID = 0L
	bot.chain("/generate")
	{
		bot.jumpToAndFire("run", it)
	}.then("run"){
		bot.sendMessage(it.chat.id.toChatId(), "لطفا نام و نام خانوادگی خود را به فارسی وارد کنید!")
	}.then{
		nameFa = "${it.text}"
		bot.sendMessage(it.chat.id.toChatId(), "لطفا نام و نام خانوادگی خود را به انگلیسی وارد کنید!")
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
		oneTimeKeyboard = true)
		).let { msg ->
			msgID = msg.messageId
		}
	}.then {
		when(it.text)
		{
			"لغو" ->
			{
				bot.deleteMessage(it.chat.id.toChatId(), msgID)
				bot.sendMessage(it.chat.id.toChatId(), "عملیات با موفقیت لغو شد!")
				bot.sendMessage(it.chat.id.toChatId(), "دستورات ربات :\n" +
						"\n" +
						"/start - شروع کار با ربات\n" +
						"/help - راهنمای ربات\n" +
						"/generate - ساخت تصویر نویسه جدید\n" +
						"\n" +
						"برای پشتیبانی بات با این ایمیل در تماس باشید : yastechorg@gmail.com")
			}
			"ویرایش اطلاعات" -> {
				bot.deleteMessage(it.chat.id.toChatId(), msgID)
				bot.jumpToAndFire("run", it)
			}
			"ساختن تصویر نوشته جدید!" ->
			{
				bot.deleteMessage(it.chat.id.toChatId(), msgID)
				bot.sendMessage(it.chat.id.toChatId(), "در حال ساختن ...")

				val file = ResourceUtils.getFile("classpath:temp.jpeg")
				val bos = ByteArrayOutputStream()
				try {
					run {
						val nameFaFont = Font.createFont(Font.TRUETYPE_FONT, ResourceUtils.getFile("classpath:Dana-Black.ttf")).deriveFont(30.0f)
						val nameEnFont = Font.createFont(Font.TRUETYPE_FONT, ResourceUtils.getFile("classpath:Gotham-Book.otf")).deriveFont(12.0f)
						val g = ImageIO.read(file)

						val url = URL("https://api.telegram.org/file/bot$token/${bot.getFile(bot.getUserProfilePhotos(it.chat.id).photos[0][0].fileId).filePath!!}")

						val profilePhoto = ImageIO.read(url)

						val output = BufferedImage(182, 182, BufferedImage.TYPE_INT_ARGB)
						val g2 = output.createGraphics()
						g2.composite = AlphaComposite.Src
						g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
						g2.color = Color.WHITE
						g2.fill(RoundRectangle2D.Float(0f, 0f, 182f, 182f, 360f, 360f))

						g2.composite = AlphaComposite.SrcAtop
						g2.drawImage(profilePhoto, 0, 0, 182, 182, null)
						g2.dispose()

						g.graphics.drawImage(output,136, 263,182, 182, null)


						var attributedText = AttributedString(nameFa)
						attributedText.addAttribute(TextAttribute.FONT, nameFaFont)
						attributedText.addAttribute(TextAttribute.FOREGROUND, Color.WHITE)
						var metrics: FontMetrics = g.graphics.getFontMetrics(nameFaFont)
						var positionX: Int = (g.width - metrics.stringWidth(nameFa)) / 2
						g.graphics.drawString(attributedText.iterator, positionX, 490)

						attributedText = AttributedString(nameEn)
						attributedText.addAttribute(TextAttribute.FONT, nameEnFont)
						attributedText.addAttribute(TextAttribute.FOREGROUND, Color.WHITE)
						metrics = g.graphics.getFontMetrics(nameEnFont)
						positionX = (g.width - metrics.stringWidth(nameEn)) / 2
						g.graphics.drawString(attributedText.iterator, positionX, 520)


						g.graphics.dispose()
						ImageIO.write(g, "jpeg", bos)
					}
				}
				finally {
					bot.sendPhoto(it.chat.id.toChatId(), SendingByteArray(bos.toByteArray(), "${nameEn}.jpeg"))
				}
			}
		}
	}.build()

	// Start Bot
	bot.start()
}