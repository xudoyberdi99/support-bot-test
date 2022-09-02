package on.insurance.supportbot.teligram.RoleService

import on.insurance.supportbot.teligram.BotService
import on.insurance.supportbot.teligram.User
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.Update

@Service
class RoleAdmin(
    @Lazy
    val botService: BotService
) {
    fun adminFunc(update: Update,user: User){

    }
}