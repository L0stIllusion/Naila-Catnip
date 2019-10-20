package bot.naila.discordbot.commands

import bot.naila.discordbot.OWNERS
import bot.naila.discordbot.api
import com.mewna.catnip.entity.message.Message
import java.awt.Color
import javax.script.ScriptEngineManager
import javax.script.ScriptException

class EvalCommand: Command() {
    override val keys: List<String> = listOf("eval")
    override val descriptionKey: String = "wip"
    override val commandCategory: CommandCategory = CommandCategory.MISC

    private val codePattern = """eval(\s((\n)?```\w*\n)?(?<code>.+)(\n```)?)?""".toPattern()
    fun findCode(content: String) = codePattern.matcher(content).also { it.find() }.group("code") ?: ""

    override fun execute(message: Message) {
        if(OWNERS.contains(message.author().idAsLong())) {
            val engine = ScriptEngineManager()
                .getEngineByExtension("kts")
            engine.put("message", message)
            engine.put("api", api)
            val code = findCode(message.content())
            if(code.isEmpty()) {
                respond("no code found", message.channel())
                return
            }
            try {
                val response = engine.eval(code)
                respond(message.channel()) {
                    baseEmbed()
                        .updateEmbed {
                            title("Successful Eval")
                            color(Color.GREEN)
                            val fields = mapOf(
                                "Response" to "```kotlin\n${response ?: "null"}```",
                                "Type" to "```kotlin\n${response?.javaClass?.simpleName ?: "null"}```"
                            )
                            addFields(fields, false)
                        }
                }
            } catch(e: ScriptException) {
                respond(message.channel()) {
                    baseEmbed().updateEmbed {
                        color(Color.RED)
                        title("Exception has been thrown!")
                        val fields = mapOf(
                            "Eval" to "```kotlin\n${code}```",
                            "Exception Type" to "```kotlin\n${e.javaClass.simpleName}```".trimMargin(),
                            "Stacktrace" to "```kotlin\n${e.message}```"
                        )
                        addFields(fields, false)
                    }
                }
            } catch(e: Throwable) {
                e.printStackTrace()
                respond("what the fuck just happened\n```\n${e}\n```", message.channel())
            }
        }
    }
}
