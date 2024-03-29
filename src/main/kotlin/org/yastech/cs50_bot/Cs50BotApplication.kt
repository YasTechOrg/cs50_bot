package org.yastech.cs50_bot

import com.elbekd.bot.Bot
import com.elbekd.bot.feature.chain.chain
import com.elbekd.bot.feature.chain.jumpToAndFire
import com.elbekd.bot.feature.chain.terminateChain
import com.elbekd.bot.model.toChatId
import com.elbekd.bot.types.KeyboardButton
import com.elbekd.bot.types.Message
import com.elbekd.bot.types.ReplyKeyboardMarkup
import com.elbekd.bot.util.SendingByteArray
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.io.ClassPathResource
import java.awt.*
import java.awt.font.TextAttribute
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.URL
import java.text.AttributedString
import java.util.*
import javax.imageio.ImageIO
import kotlin.concurrent.schedule


@SpringBootApplication
class Cs50BotApplication

fun main(args: Array<String>)
{
	// Run Spring Boot Application
	runApplication<Cs50BotApplication>(*args)

	// Set Bot Token
	val token = args[0]
	println("bot runs on token ${args[0]}")

	// Creat Bot
	val bot = Bot.createPolling(token)

	// Start Command
	bot.onCommand("/start") { (msg, _)  ->
		bot.sendMessage(msg.chat.id.toChatId(), "جهت ساخت استوری اختصاصی با نام و عکس خود، دستور /generate را وارد کنید.")
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

	val map = mutableMapOf<Long, MutableMap<String, Any>>()

	// Command Chain
	bot.chain("/generate")
	{ msg ->

		Timer("SettingUp", false).schedule(500) {
			// Global Variables
			map[msg.chat.id] = mutableMapOf(
				"nameFa" to "",
				"nameEn" to "",
				"msgID" to 0L,
				"profileState" to "unset",
				"profilePicture" to URL("https://cs50x.ir/summer")
			)

			// Run Process

		}.run().also {
			bot.jumpToAndFire("run", msg)
		}
	}.then("run"){

		// Get FullName In Persian
		bot.sendMessage(it.chat.id.toChatId(), "لطفا نام و نام خانوادگی خود را به فارسی وارد کنید!")
	}.then{

		// Set User Response And Get User FullName In English
		map[it.chat.id]!!["nameFa"] = "${it.text}"
		bot.sendMessage(it.chat.id.toChatId(), "حالا لطفا نام و نام خانوادگی خود را به انگلیسی وارد کنید!")
	}.then {

		// Set User Response
		map[it.chat.id]!!["nameEn"] ="${it.text}"

		// Check If Use Have Profile Picture
		if(bot.getUserProfilePhotos(it.chat.id).photos.isNotEmpty())
		{

			// Set User Image State to "SELECTIVE"
			map[it.chat.id]!!["profileState"] = "SELECTIVE"

			// Ask User For Profile Picture
			bot.sendMessage(it.chat.id.toChatId(), "عکس شما در تصویر نوشته چاپ می شود . کدام تصویر را انتخاب میکنید ؟",
				replyMarkup = ReplyKeyboardMarkup( keyboard = listOf(
					listOf(
						KeyboardButton("عکس پروفایل"),
						KeyboardButton("ارسال عکس"),
					),
					listOf(KeyboardButton("لغو"))

				), oneTimeKeyboard = true /* Close After Click */ )

			).let { msg -> map[it.chat.id]!!["msgID"] = msg.messageId /* Set Promise Message ID*/ }
		}
		else
		{

			// Set User Image State to "CUSTOM"
			map[it.chat.id]!!["profileState"] = "CUSTOM"

			// Send Get Picture Message
			bot.sendMessage(it.chat.id.toChatId(), "لطفا عکسی میخواهید در تصویر نویسه چاپ شود را وارد کنید!")
		}

	}.then {

		// If Profile State Is CUSTOM
		if (map[it.chat.id]!!["profileState"] == "CUSTOM")
		{

			if(it.photo.isEmpty()) bot.close()

			// Set Custom Picture Url
			if (it.photo.isNotEmpty())
			{
				map[it.chat.id]!!["profilePicture"] = URL("https://api.telegram.org/file/bot$token/${bot.getFile(it.photo[0].fileId).filePath}")
			}

			// Jump To Final
			bot.jumpToAndFire("final", it)
		}

		// If Profile State Is SELECTIVE
		else
		{
			when(it.text)
			{
				// On Cancel
				"لغو" ->
				{
					// Cancel Process
					removeAction(bot, it, map[it.chat.id]!!["msgID"] as Long)

					// Terminate /generate chain
					bot.terminateChain(it.chat.id)
				}

				// On Profile Picture
				"عکس پروفایل" ->
				{

					// Set Profile State
					map[it.chat.id]!!["profileState"] = "PROFILE_PIC"

					// Set Profile Picture Url
					map[it.chat.id]!!["profilePicture"] = URL(
						"https://api.telegram.org/file/bot$token/${bot.getFile(bot.getUserProfilePhotos(it.chat.id).photos[0][0].fileId).filePath!!}"
					)

					// Jump To Final
					bot.jumpToAndFire("final", it)
				}

				// On Custom Picture
				"ارسال عکس" ->
				{
					// Set Profile State
					map[it.chat.id]!!["profileState"] = "CUSTOM"

					// Send Get Picture Message
					bot.sendMessage(it.chat.id.toChatId(), "لطفا عکسی میخواهید در تصویر نویسه چاپ شود را وارد کنید!")
				}
				else ->
				{
					// Cancel Process
					removeAction(bot, it, map[it.chat.id]!!["msgID"] as Long)

					// Jump To Final
					bot.jumpToAndFire("run", it)
				}
			}
		}
	}.then {

		if (it.photo.isEmpty())
		{
			// Cancel Process
			removeAction(bot, it, map[it.chat.id]!!["msgID"] as Long)

			// Jump To Final
			bot.terminateChain(it.chat.id)
		}

		// Check If Custom Image
		if (map[it.chat.id]!!["profileState"] == "CUSTOM") map[it.chat.id]!!["profilePicture"] = URL(
			"https://api.telegram.org/file/bot$token/${bot.getFile(it.photo[0].fileId).filePath}"
		)

		// Jump To Final
		bot.jumpToAndFire("final", it)

	}.then("final") {

		// Remove Keyboard
		bot.deleteMessage(it.chat.id.toChatId(), map[it.chat.id]!!["msgID"] as Long)

		// Send Waiting Response
		bot.sendMessage(it.chat.id.toChatId(), "در حال ساختن ...")

		// Get Temp Image From Application Resources And Initialize It
		val file = ClassPathResource("temp.jpg").inputStream

		// Initialize Final Byte Array Output Variable
		val bos = ByteArrayOutputStream()

		// Design Process
		try {

			// Do Process In Blocking Context
			run {

				// Profile Picture Size
				val profilePicSize = 948

				// Read Fonts From Application Resources And Create Custom Fonts
				val nameFaFont = Font.createFont(Font.TRUETYPE_FONT, ClassPathResource("Dana-Black.ttf").inputStream).deriveFont(160.0f)
				val nameEnFont = Font.createFont(Font.TRUETYPE_FONT, ClassPathResource("Gotham-Book.otf").inputStream).deriveFont(80.0f)

				// Main Canvas
				val g = ImageIO.read(file)

				// Load User Profile Photo
				val profilePhoto = ImageIO.read(map[it.chat.id]!!["profilePicture"] as URL)

				// Create User Profile Photo Buffer Image
				val profilePhotoOutput = BufferedImage(profilePicSize, profilePicSize, BufferedImage.TYPE_INT_ARGB)

				// Create User Profile Photo Canvas
				val g2 = profilePhotoOutput.createGraphics()

				// Draw Base Circle
				g2.composite = AlphaComposite.Src
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
				g2.color = Color.WHITE
				g2.fill(RoundRectangle2D.Float(0f, 0f, profilePicSize.toFloat(), profilePicSize.toFloat(), 1600f, 1600f))
				g2.composite = AlphaComposite.SrcAtop

				// Draw Profile Photo In Canvas
				g2.drawImage(profilePhoto, 0, 0, profilePicSize, profilePicSize, null)

				// Render Profile Photo Result
				g2.dispose()

				// Draw Profile Photo In Canvas
				g.graphics.drawImage(profilePhotoOutput,678, 1055,profilePicSize, profilePicSize, null)

				// Create New Attributed String And Pass Persian Name String
				var attributedText = AttributedString(map[it.chat.id]!!["nameFa"].toString())

				// Set Persian Name Font Style
				attributedText.addAttribute(TextAttribute.FONT, nameFaFont)
				attributedText.addAttribute(TextAttribute.FOREGROUND, Color.WHITE)

				// Get Persian Name Font Metrics
				var metrics: FontMetrics = g.graphics.getFontMetrics(nameFaFont)

				// Get Persian Name Center Position From Font Metrics
				var positionX: Int = (g.width - metrics.stringWidth(map[it.chat.id]!!["nameFa"].toString())) / 2

				// Draw Persian Name With ( x : center , y : 490px )
				g.graphics.drawString(attributedText.iterator, positionX, 2220)


				// Reinitialize Attributed String And Pass English Name String
				attributedText = AttributedString(map[it.chat.id]!!["nameEn"].toString())

				// Set English Name Font Style
				attributedText.addAttribute(TextAttribute.FONT, nameEnFont)
				attributedText.addAttribute(TextAttribute.FOREGROUND, Color.WHITE)

				// Get English Name Font Metrics
				metrics = g.graphics.getFontMetrics(nameEnFont)

				// Get English Name Center Position From Font Metrics
				positionX = (g.width - metrics.stringWidth(map[it.chat.id]!!["nameEn"].toString())) / 2

				// Draw English Name With ( x : center , y : 520px )
				g.graphics.drawString(attributedText.iterator, positionX, 2360)

				// Render Main Canvas
				g.graphics.dispose()

				// Write Final Rendered Result On Byte Array Output Stream On JPEG Format
				ImageIO.write(g, "jpeg", bos)
			}
		}
		finally {

			// Finally Send Result Photo As ByteArray To User
			bot.sendPhoto(it.chat.id.toChatId(), SendingByteArray(bos.toByteArray(), "${map[it.chat.id]!!["nameEn"].toString()}.jpeg"))
			bot.sendDocument(it.chat.id.toChatId(), SendingByteArray(bos.toByteArray(), "${map[it.chat.id]!!["nameEn"].toString()}.jpeg"))
			map.remove(it.chat.id)
		}

	// Build Generate Chain
	}.build()

	// Start Bot
	bot.start()
}

suspend fun removeAction(bot: Bot, message: Message, msgID: Long)
{
	// Set Chat ID
	val chatId = message.chat.id.toChatId()

	// Remove Keyboard
	bot.deleteMessage(chatId, msgID)

	// Show Cancel Response
	bot.sendMessage(chatId, "عملیات با موفقیت لغو شد!")
	bot.sendMessage(chatId, "دستورات ربات :\n" +
			"\n" +
			"/start - شروع کار با ربات\n" +
			"/help - راهنمای ربات\n" +
			"/generate - ساخت تصویر نویسه جدید\n" +
			"\n" +
			"برای پشتیبانی بات با این ایمیل در تماس باشید : yastechorg@gmail.com")
}