package on.insurance.supportbot.teligram

enum class BotStep {
    START,
    LANGUAGE,
    CONTACT,
    CHAT, QUEUE,
    CLOSE, BEGIN, BACK, BALL,BLOC,

}

enum class MessageType{
    DOCUMENT,
    TEXT,
    AUDIO,
    VIDEO,
    VOICE,
    PHOTO,
}

enum class Language(
    val value: String
    ) {
    UZ("\uD83C\uDDF8\uD83C\uDDF1 uzbek"),
    RU("\uD83C\uDDF7\uD83C\uDDFA Русский"),
    ENG("\uD83C\uDDFA\uD83C\uDDF8 English");
}

enum class Message(
    private val code: String,
    private val uz:String,
    private val ru:String,
    private val eng:String,
){
    LANGUAGE("langage","Iltimos, tilni tanlang \uD83C\uDF10","Пожалуйста, выберите язык! \uD83C\uDF10","Please, choose language! \uD83C\uDF10"),
    CONTACT("share contact","kontaktizni yuboring","отправить ваш контакт","share contact"),
    SHARE_CONTACT("share contact","kontakni yuborish \uD83D\uDCDE","отправить контакт \uD83D\uDCDE","share contact \uD83D\uDCDE"),
    YOU_HAVE_CONTACTED_THE_OPERATOR("You have contacted the Operator","operator bilan bog'landingiz","Вы связались с Оператором","You have contacted the Operator"),
    CLICK_THE_BEGIN_BUTTON_TO_START("click the begin button to start","boshlash uchun boshlash tugmasini bosing","нажмите кнопку начать, чтобы начать","click the begin button to start"),
    BEGIN("begin","boshlash \uD83D\uDCF2","начать \uD83D\uDCF2","begin \uD83D\uDCF2"),
    YOU_ARE_ENABLED("You are enabled","Siz activ holga O'tdingiz","Вы включены","You are enabled"),
    NEW_CHAT("new chat","Chat yangilandi","Чат обновлен","new chat"),
    CLOSE("close","yopish ❌","закрыть ❌","close ❌"),
    EXIT("exit","chiqish \uD83D\uDE80","выход \uD83D\uDE80","exit \uD83D\uDE80"),
    GIVE_THE_OPERATOR_MARK("give the operator a score","operatorga ball bering","дайте оператору очки","give the operator a score"),
    BLOCK("block","bloklash \uD83D\uDCF5","блок \uD83D\uDCF5","bloc \uD83D\uDCF5"),
    YOU_ARE_BLOCKED("you are blocked","siz bloklangansiz","вы заблокированы","you are block");
    open operator fun get(language:Language): String? {
        if (language.name == "UZ") return uz
        return if (language.name == "RU") ru else this.eng
    }

    open fun isInside(text: String): Boolean {
        return text == uz || text == ru || text == eng
    }
}

enum class Role {
    USER, ADMIN, OPERATOR

}

enum class ErrorCode(val code: Int) {
    GENERAL(100),
    OBJECT_NOT_FOUND(101)
}