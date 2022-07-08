package org.yastech.cs50_bot

import com.elbekd.bot.Bot
import com.elbekd.bot.feature.chain.chain
import com.elbekd.bot.feature.chain.jumpToAndFire
import com.elbekd.bot.feature.chain.terminateChain
import com.elbekd.bot.model.ChatId
import com.elbekd.bot.model.toChatId
import com.elbekd.bot.types.KeyboardButton
import com.elbekd.bot.types.Message
import com.elbekd.bot.types.ReplyKeyboardMarkup
import com.elbekd.bot.util.SendingByteArray
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.io.ClassPathResource
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
	// Run Spring Boot Application
	runApplication<Cs50BotApplication>(*args)

	// Set Bot Token
	//val token = "5412773834:AAFBi28R150rMjbOXbdLizi_JPmG6Z4X0TU"
//	val token = "5303949448:AAEFnKO2SkXD4J_0VjztntURBd2ojCYeYS8"
	val token = "5496536134:AAG5PmNrbSJHzv7H7WrxRObojgvXFhVWRtM"

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

	// Global Variables
	var nameFa = ""
	var nameEn = ""
	var msgID = 0L

	var profileState = "unset"
	var profilePicture = URL("https://cs50x.ir/summer")

	// Command Chain
	bot.chain("/generate")
	{

		// Run Process
		bot.jumpToAndFire("run", it)
	}.then("run"){

		// Get FullName In Persian
		bot.sendMessage(it.chat.id.toChatId(), "لطفا نام و نام خانوادگی خود را به فارسی وارد کنید!")
	}.then{

		// Set User Response And Get User FullName In English
		nameFa = "${it.text}"
		bot.sendMessage(it.chat.id.toChatId(), "حالا لطفا نام و نام خانوادگی خود را به انگلیسی وارد کنید!")
	}.then {

		// Set User Response
		nameEn ="${it.text}"

		// Check If Use Have Profile Picture
		if(bot.getUserProfilePhotos(it.chat.id).photos.isNotEmpty())
		{

			// Set User Image State to "SELECTIVE"
			profileState = "SELECTIVE"

			// Ask User For Profile Picture
			bot.sendMessage(it.chat.id.toChatId(), "عکس شما در تصویر نوشته چاپ می شود . کدام تصویر را انتخاب میکنید ؟",
				replyMarkup = ReplyKeyboardMarkup( keyboard = listOf(
					listOf(
						KeyboardButton("عکس پروفایل"),
						KeyboardButton("ارسال عکس"),
					),
					listOf(KeyboardButton("لغو"))

				), oneTimeKeyboard = true /* Close After Click */ )

			).let { msg -> msgID = msg.messageId /* Set Promise Message ID*/ }
		}
		else
		{

			// Set User Image State to "CUSTOM"
			profileState = "CUSTOM"

			// Send Get Picture Message
			bot.sendMessage(it.chat.id.toChatId(), "لطفا عکسی میخواهید در تصویر نویسه چاپ شود را وارد کنید!")
		}

	}.then {

		// If Profile State Is CUSTOM
		if (profileState == "CUSTOM")
		{

			// Set Custom Picture Url
			profilePicture = URL("https://api.telegram.org/file/bot$token/${bot.getFile(it.photo[0].fileId).filePath}")

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
					removeAction(bot, it, msgID)

					// Terminate /generate chain
					bot.terminateChain(it.chat.id)
				}

				// On Profile Picture
				"عکس پروفایل" ->
				{

					// Set Profile State
					profileState = "PROFILE_PIC"

					// Set Profile Picture Url
					profilePicture = URL(
						"https://api.telegram.org/file/bot$token/${bot.getFile(bot.getUserProfilePhotos(it.chat.id).photos[0][0].fileId).filePath!!}"
					)

					// Jump To Final
					bot.jumpToAndFire("final", it)
				}

				// On Custom Picture
				"ارسال عکس" ->
				{
					// Set Profile State
					profileState = "CUSTOM"

					// Send Get Picture Message
					bot.sendMessage(it.chat.id.toChatId(), "لطفا عکسی میخواهید در تصویر نویسه چاپ شود را وارد کنید!")
				}
			}
		}
	}.then {

		// Check If Custom Image
		if (profileState == "CUSTOM") profilePicture = URL(
			"https://api.telegram.org/file/bot$token/${bot.getFile(it.photo[0].fileId).filePath}"
		)

		// Jump To Final
		bot.jumpToAndFire("final", it)

	}.then("final"){

		// Get Data Promise From User
		bot.sendMessage(it.chat.id.toChatId(), "نام فارسی : $nameFa\n" +
				"نام انگلیسی : $nameEn\n" +
				"آیا اطلاعات وارد شده درست است ؟", replyMarkup = ReplyKeyboardMarkup( keyboard = listOf(
			listOf(
				KeyboardButton("لغو"),
				KeyboardButton("ویرایش اطلاعات"),
			),
			listOf(KeyboardButton("تایید"))

		), oneTimeKeyboard = true /* Close After Click */ )

		).let { msg -> msgID = msg.messageId /* Set Promise Message ID*/ }

	}.then {

		// Check User Response
		when(it.text)
		{
			// On Cancel
			"لغو" -> removeAction(bot, it, msgID)

			// On Edit Data
			"ویرایش اطلاعات" -> {

				// Remove Keyboard
				bot.deleteMessage(it.chat.id.toChatId(), msgID)

				// Restart Process
				bot.jumpToAndFire("run", it)
			}

			// On Create New Image
			"تایید" ->
			{

				// Remove Keyboard
				bot.deleteMessage(it.chat.id.toChatId(), msgID)

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
						val profilePhoto = ImageIO.read(profilePicture)

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
						var attributedText = AttributedString(nameFa)

						// Set Persian Name Font Style
						attributedText.addAttribute(TextAttribute.FONT, nameFaFont)
						attributedText.addAttribute(TextAttribute.FOREGROUND, Color.WHITE)

						// Get Persian Name Font Metrics
						var metrics: FontMetrics = g.graphics.getFontMetrics(nameFaFont)

						// Get Persian Name Center Position From Font Metrics
						var positionX: Int = (g.width - metrics.stringWidth(nameFa)) / 2

						// Draw Persian Name With ( x : center , y : 490px )
						g.graphics.drawString(attributedText.iterator, positionX, 2220)


						// Reinitialize Attributed String And Pass English Name String
						attributedText = AttributedString(nameEn)

						// Set English Name Font Style
						attributedText.addAttribute(TextAttribute.FONT, nameEnFont)
						attributedText.addAttribute(TextAttribute.FOREGROUND, Color.WHITE)

						// Get English Name Font Metrics
						metrics = g.graphics.getFontMetrics(nameEnFont)

						// Get English Name Center Position From Font Metrics
						positionX = (g.width - metrics.stringWidth(nameEn)) / 2

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
					bot.sendPhoto(it.chat.id.toChatId(), SendingByteArray(bos.toByteArray(), "${nameEn}.jpeg"))
					bot.sendDocument(it.chat.id.toChatId(), SendingByteArray(bos.toByteArray(), "${nameEn}.jpeg"))
				}
			}
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