package org.yastech.cs50_bot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Cs50BotApplication

fun main(args: Array<String>) {
	runApplication<Cs50BotApplication>(*args)
	println("hI")
}
