package on.insurance.supportbot.teligram


import on.insurance.supportbot.ContactService
import on.insurance.supportbot.UserService
import on.insurance.supportbot.teligram.Message.*
import on.insurance.supportbot.teligram.RoleService.RoleAdmin
import on.insurance.supportbot.teligram.RoleService.RoleOperator
import on.insurance.supportbot.teligram.RoleService.RoleUser
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.meta.exceptions.TelegramApiException


@Service
class BotService(
    val myBot: MyBot,
    val userService: UserService,
    val roleUser: RoleUser,
    val roleOperator: RoleOperator,
    val roleAdmin: RoleAdmin,
    val contactService: ContactService,
) {

    fun massage(update: Update) {
        var chatId: Long = 0
        update.run {
            chatId = message.chatId
        }

        var user = userService.getUser(chatId)

        when (user.botStep) {
            BotStep.START -> {
                when (update.message.from.languageCode) {
                    "uz" -> user.language=Language.UZ
                    "ru" -> user.language=Language.RU
                    else -> user.language=Language.ENG
                }
                sendMassage(chatId, LANGUAGE[user.language]!!, languageButtons())
                user.botStep = BotStep.LANGUAGE
                userService.update(user)
            }
            BotStep.CONTACT -> {
                val contact = update.message.contact
                val saveContact = contactService.saveContact(contact.phoneNumber, contact.firstName, user)
                user = userService.checkOperator(saveContact, user)
                user.botStep = BotStep.BACK
                userService.update(user)
            }
        }
        when (user.role) {
            Role.USER -> roleUser.userFunc(update, user)
            Role.OPERATOR ->roleOperator.operatorFunc(update, user)
            Role.ADMIN -> roleAdmin.adminFunc(update, user)
        }
    }

    fun inline(update: Update) {
        var chatId: Long = 0
        var data = ""
        update.run {
            chatId = callbackQuery.message.chatId
            data = callbackQuery.data
        }
        val user = userService.getUser(chatId)
        val botStep = user.botStep

        when (botStep) {

            BotStep.LANGUAGE -> {
                deleteMessage(update)
                user.language = Language.valueOf(data)
                sendMassage(chatId,CONTACT[user.language]!!, getContact(SHARE_CONTACT[user.language]!!))
                user.botStep = BotStep.CONTACT
                userService.update(user)
            }
        }

        when (user.role){
            Role.USER -> roleUser.inlineFunk(update, user)
        }

    }


    fun sendMassage(chatId: Long, text: String, inlineKeyboardMarkup: InlineKeyboardMarkup?) {
        val sendMessage = SendMessage(chatId.toString(), text)
        inlineKeyboardMarkup?.run { sendMessage.replyMarkup = this }
        sendMessage.enableMarkdown(true)
        myBot.execute(sendMessage) ?: throw TelegramApiException("xatolik")
    }

    fun sendMassage(chatId: Long, text: String) {
        val sendMessage = SendMessage(chatId.toString(), text)
        sendMessage.enableMarkdown(true)
        myBot.execute(sendMessage) ?: throw TelegramApiException("xatolik")
    }

    fun sendMassage(chatId: Long, text: String, replyKeyboardMarkup: ReplyKeyboardMarkup) {
        val sendMessage = SendMessage(chatId.toString(), text)
        sendMessage.replyMarkup = replyKeyboardMarkup
        sendMessage.enableMarkdown(true)
        myBot.execute(sendMessage) ?: throw TelegramApiException("xatolik")
    }

    fun sendMassage(chatId: Long, text: String, remove: ReplyKeyboardRemove) {
        val sendMessage = SendMessage(chatId.toString(), text)
        sendMessage.replyMarkup = remove
        sendMessage.enableMarkdown(true)
        myBot.execute(sendMessage) ?: throw TelegramApiException("xatolik")
    }

    fun languageButtons(): InlineKeyboardMarkup {
        val inlineKeyboardMarkup = InlineKeyboardMarkup()
        val keyboardButtons = mutableListOf<InlineKeyboardButton>()
        val buttons = listOf<Language>(Language.UZ, Language.RU, Language.ENG)
        buttons.forEach {
            val inlineKeyboardButton = InlineKeyboardButton()
            inlineKeyboardButton.text = it.value
            inlineKeyboardButton.callbackData = it.name
            keyboardButtons.add(inlineKeyboardButton);
        }
        val rowList: MutableList<List<InlineKeyboardButton>> = ArrayList()
        rowList.add(keyboardButtons)
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup
    }

    fun getContact(lang: String): ReplyKeyboardMarkup = ReplyKeyboardMarkup().apply {
        oneTimeKeyboard = true
        resizeKeyboard = true
        selective = false
        keyboard = mutableListOf(KeyboardRow(listOf(
            KeyboardButton().apply {
                text = lang
                requestContact = true
            }
        )))

    }

    fun deleteMessage(update: Update) {
        var mesId: Int = 0
        var chatId: Long = 0
        update.callbackQuery?.run {
             chatId = message.chatId
            mesId = message.messageId
        }
        update.message?.run {
            chatId=getChatId()
            mesId=messageId
        }
        var deleteMessage = DeleteMessage(chatId.toString(), mesId)
        myBot.execute(deleteMessage) ?: throw TelegramApiException("xatolik")
    }
}