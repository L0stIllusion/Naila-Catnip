package bot.naila.discordbot.utils

import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.entity.util.Permission

typealias onInsufficientPermissions = (messsage: Message) -> Unit

class PermissionHandler(val message: Message) {
    private val member = message.member()

    fun authorIsOwner(onInsufficientPermissions: onInsufficientPermissions = {}) =
        PermissionCheckResponse.create(member?.isOwner ?: false, onInsufficientPermissions)

    fun authorHasPerms(vararg perms: Permission, onInsufficientPermissions: onInsufficientPermissions = {}) =
        PermissionCheckResponse.create(member?.hasPermissions(*arrayOf(*perms).plus(Permission.ADMINISTRATOR)) ?: false, onInsufficientPermissions)

    fun customCheck(predicate: (message: Message) -> Boolean, onInsufficientPermissions: onInsufficientPermissions = {}) =
        PermissionCheckResponse.create(predicate(message), onInsufficientPermissions)

    fun handle(vararg responses: PermissionCheckResponse): Boolean {
        var hasPermissions = true
        loop@ for(response in responses) {
            when(response) {
                is PermissionCheckResponse.PassedPermissionCheck -> continue@loop
                is PermissionCheckResponse.FailedPermissionCheck -> {
                    response.onInsufficientPermissions(message)
                    hasPermissions = false
                    break@loop
                }
            }
        }
        return hasPermissions
    }
}

sealed class PermissionCheckResponse(val successful: Boolean) {
    class PassedPermissionCheck: PermissionCheckResponse(true)
    data class FailedPermissionCheck(val onInsufficientPermissions: onInsufficientPermissions): PermissionCheckResponse(false)

    companion object {
        fun create(passed: Boolean, onInsufficientPermissions: onInsufficientPermissions): PermissionCheckResponse =
            if(passed) PassedPermissionCheck() else FailedPermissionCheck(onInsufficientPermissions)
    }
}
