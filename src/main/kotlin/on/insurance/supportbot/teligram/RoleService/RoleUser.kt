package on.insurance.supportbot.teligram.RoleService

import on.insurance.supportbot.GroupService
import on.insurance.supportbot.MessageService
import on.insurance.supportbot.UserService
import on.insurance.supportbot.teligram.*
import on.insurance.supportbot.teligram.BotStep.*
import on.insurance.supportbot.teligram.Message.YOU_HAVE_CONTACTED_THE_OPERATOR
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import java.awt.SystemColor.text

@Service
class RoleUser(
    @Lazy
    val botService: BotService,
    val groupService: GroupService,
    val messageService: MessageService,
    val userService: UserService,
    @Lazy
    val myBot: MyBot,
) {
    lateinit var update: Update
    lateinit var user: User
    lateinit var group: Group

    fun userFunc(updateFunc: Update, userFunc: User) {
        update = updateFunc
        user = userFunc
        group = groupService.getGroupByUserId(user)

        update.message?.text?.run { scanButton(this) }

        when (user.botStep) {
            CHAT -> {
                saveChat()
                sendText()
            }
            BACK -> {
                val remove = ReplyKeyboardRemove(true)
                botService.sendMassage(update.message.chatId, YOU_HAVE_CONTACTED_THE_OPERATOR[user.language]!!, remove)
                user.botStep = CHAT
            }
            BLOC->{
                botService.sendMassage(user.chatId, Message.YOU_ARE_BLOCKED[user.language]!!)
            }
            else -> botService.deleteMessage(update)

        }
        userService.update(user)
    }

    fun inlineFunk(updateFunc: Update, userFunc: User){
        update=updateFunc
        user=userFunc
        group = groupService.getGroupByUserId(user)
        val data = update.callbackQuery.data
        when(user.botStep){
            BALL ->{
                group.ball=data.toInt()
                group.deleted=true
                groupService.update(group)
                user.botStep=CHAT
                botService.deleteMessage(update)
            }

        }
        userService.update(user)
    }


    fun saveChat() {
        messageService.creat(update.message, group, user)
    }

    fun sendText() {
        group.operator?.run { myBot.forwardMessage(update,this.chatId) }
    }

    fun queueButton(lang: String): ReplyKeyboardMarkup = ReplyKeyboardMarkup().apply {
        oneTimeKeyboard = true
        resizeKeyboard = true
        selective = false
        keyboard = mutableListOf(KeyboardRow(listOf(
            KeyboardButton().apply {
                text = "navbatingizni bilish"
                requestContact = false
            }
        )))

    }

    fun scanButton(text: String) {
        when (text) {
            "navbatingizni bilish" -> {
                user.botStep = QUEUE
            }
        }
    }


}