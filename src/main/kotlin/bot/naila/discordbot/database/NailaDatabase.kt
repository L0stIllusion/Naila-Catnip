package bot.naila.discordbot.database

import bot.naila.discordbot.DATABASE_PASSWORD
import org.apache.logging.log4j.LogManager
import java.sql.*

private val databaseLogger = LogManager.getLogger("Naila [DATABASE]")

object NailaDatabase {
    init {
        Class.forName("org.postgresql.Driver")
        //autocreate users
        makeRequest(request = {
            prepareStatement("create table if not exists users(id bigint primary key not null, langPref text)")
                .execute()
        })
        //autocreate servers
        makeRequest(request = {
            prepareStatement("create table if not exists servers(id bigint primary key not null, langPref text)")
                .execute()
        })
    }

    fun <T> makeRequest(request: Connection.() -> T, onSuccess: (response: T) -> Unit = { }, onError: (t: Throwable) -> Unit = { }): T? {
        return try {
            DriverManager.getConnection("jdbc:postgresql:naila", "naila", DATABASE_PASSWORD)
                .let(request)
                .also(onSuccess)
        } catch (e: SQLException) {
            databaseLogger.error("SQL request error: \n ${e.message}")
            onError(e)
            null
        }
    }
}

inline fun <T> PreparedStatement.executeAndPositionQuery(query: (set: ResultSet) -> T): T? {
    val result = executeQuery()
    return when {
        result.next() -> query(result)
        else -> null
    }
}
