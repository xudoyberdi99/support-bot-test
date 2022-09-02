package on.insurance.supportbot.teligram.RoleService

import on.insurance.supportbot.GroupService
import on.insurance.supportbot.MessageService
import on.insurance.supportbot.UserService
import on.insurance.supportbot.teligram.*
import on.insurance.supportbot.teligram.Message.*
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Service
class RoleOperator(
    @Lazy
    val botService: BotService,
    val groupService: GroupService,
    val messageService: MessageService,
    val userService: UserService,
    @Lazy
    val myBot: MyBot,
    @Lazy
    val roleUser: RoleUser,
) {
    lateinit var update: Update
    lateinit var operator: User
    var group: Group?=null

    fun operatorFunc(updateFunc: Update,userFunc: User){
        update = updateFunc
        operator = userFunc

        group = groupService.getGroupByOperatorId(operator)?.run {  this }
        update.message?.text?.run { scanButton(this) }
        when (operator.botStep) {
            BotStep.CHAT -> {
                var user:User?=group?.user
                if (user!=null){
                    saveChat()
                    sendText()
                }
            }
            BotStep.BACK->{
                botService.sendMassage(update.message.chatId, CLICK_THE_BEGIN_BUTTON_TO_START[operator.language]!!,beginButton(operator.language))
                userService.backOperator(operator)
                group?.run {
                    group!!.isActive=false
                    groupService.update(group!!)
                }
            }
            BotStep.BEGIN->{
                botService.sendMassage(update.message.chatId,YOU_ARE_ENABLED[operator.language]!!,menuButton(operator.language))
                userService.operatorIsActive(operator)
                operator.botStep=BotStep.CHAT
                begin()
            }
            BotStep.CLOSE->{
                botService.sendMassage(update.message.chatId,NEW_CHAT[operator.language]!!,menuButton(operator.language))
                userService.operatorIsActive(operator)
                operator.botStep=BotStep.CHAT
                begin()
            }
        }
        userService.update(operator)

    }

    fun saveChat() {
        messageService.creat(update.message, group!!, operator)
    }

    fun sendText() {
        var from_chat_id: Long = 0
        var message_id:Int=0
        update.message?.run {
            from_chat_id = getChatId()
            message_id=messageId
        }
        group!!.user?.run { myBot.copyMessage(this.chatId,from_chat_id, message_id) }
    }

    fun scanButton(text:String){

        when(text){
            CLOSE[operator.language]->{
                operator.botStep=BotStep.CLOSE
                group?.run {
                    botService.sendMassage(user!!.chatId,GIVE_THE_OPERATOR_MARK[user!!.language]!!,ballButtons())
                    user!!.botStep=BotStep.BALL
                    userService.update(user!!)
                    group!!.isActive=false
                    group!!.isRead=true
                    groupService.update(group!!)
                }
            }
            EXIT[operator.language]->{
                operator.botStep=BotStep.BACK
                group?.run {
                    botService.sendMassage(user!!.chatId,GIVE_THE_OPERATOR_MARK[user!!.language]!!,ballButtons())
                    user!!.botStep=BotStep.BALL
                    userService.update(user!!)
                    group!!.isActive=false
                    group!!.isRead=true
                    groupService.update(group!!)
                }
            }
            BEGIN[operator.language]->{
                operator.botStep=BotStep.BEGIN
            }
            BLOCK[operator.language]->{
                operator.botStep=BotStep.CLOSE
                group?.run {
                    botService.sendMassage(user!!.chatId,YOU_ARE_BLOCKED[user!!.language]!!)
                    user!!.botStep=BotStep.BLOC
                    userService.update(user!!)
                    group!!.isActive=false
                    group!!.isRead=true
                    groupService.update(group!!)
                }
            }
        }
    }

    fun menuButton(lang: Language): ReplyKeyboardMarkup = ReplyKeyboardMarkup().apply {
        oneTimeKeyboard = true
        resizeKeyboard = true
        selective = false
        keyboard = mutableListOf(KeyboardRow(listOf(
            KeyboardButton().apply {
                text = CLOSE[lang]!!
            },
            KeyboardButton().apply {
                text = EXIT[lang]!!
            } ,
            KeyboardButton().apply {
                text = BLOCK[lang]!!
            }
        )))

    }

    fun beginButton(lang: Language): ReplyKeyboardMarkup = ReplyKeyboardMarkup().apply {
        oneTimeKeyboard = true
        resizeKeyboard = true
        selective = false
        keyboard = mutableListOf(KeyboardRow(listOf(
            KeyboardButton().apply {
                text =BEGIN[lang]!!
            }
        )))

    }

    fun begin(){
        var opChatId=operator.chatId
        var operator1=operator
        groupService.getNewGroupByOperator(operator1)?.run {
            userService.backOperator(operator1)
            val group1=this
                messageService.getUserMessage(group1)?.run {
                    val userMessage =this
                    userMessage.forEach {
                        myBot.forwardMessage(opChatId,it.chatId,it.massageId)
                    }
                    group1.operator=operator1
                    group1.isActive=true
                    groupService.update(group1)
                }
        }

    }

    fun ballButtons(): InlineKeyboardMarkup {
        var inlineKeyboardMarkup = InlineKeyboardMarkup()
        var rowList: MutableList<List<InlineKeyboardButton>> = ArrayList()
        var keyboardButtons = mutableListOf<InlineKeyboardButton>()
        for(i in 1..5){
            val inlineKeyboardButton = InlineKeyboardButton()
            inlineKeyboardButton.text = i.toString()
            inlineKeyboardButton.callbackData = i.toString()
            keyboardButtons.add(inlineKeyboardButton);
        }
        rowList.add(keyboardButtons)
        var keyboardButtons2 = mutableListOf<InlineKeyboardButton>()
        val inlineKeyboardButton = InlineKeyboardButton()
        inlineKeyboardButton.text = "next"
        inlineKeyboardButton.callbackData = "0"
        keyboardButtons2.add(inlineKeyboardButton);
        rowList.add(keyboardButtons2)
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup
    }
}