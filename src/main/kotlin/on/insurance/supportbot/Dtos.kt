package on.insurance.supportbot


import on.insurance.supportbot.teligram.Admin
import on.insurance.supportbot.teligram.Group
import on.insurance.supportbot.teligram.Language
import on.insurance.supportbot.teligram.Operator
import on.insurance.supportbot.teligram.*
import java.util.Date
import javax.persistence.EnumType
import javax.persistence.Enumerated

interface GroupsByOperatorId {
    val kun: String
    val group: Group
}
    data class GroupsByOperatorIdDto(
        var operator_id: Long,
        var first_day: Long,
        var last_day: Long,
    )

data class OperatorCreateDto(
    var languages:List<Int>,
    var name: String,
    var phoneNumber: String

) {
    fun toEntity(): Operator = Operator(languages[0],name, phoneNumber)
}

data class OperatorUpdateDto(
    var name: String? = null,
    var phoneNumber: String? = null
)

data class OperatorDto(
    var id: Long,
    var name: String,
    var phoneNumber: String

) {
    companion object {
        fun toDto(entity: Operator) = OperatorDto(entity.id!!, entity.name, entity.phoneNumber)
    }
}
data class LoginDto(var username:String,var password:String)
data class BaseMessage(val code: Int, val message: String)
data class UserDto(
    var id: Long,
    var chatId: Long,
    var botStep: BotStep,
    var language: Language,
    var isActive: Boolean

)

interface ResponseUser {
    var id: Long
    var telephoneNumber: String
    var fullName: String
    var systemLanguage: String
    var state:String

}
data class UserRequest(
    var fullName: String?=null,
    var state:BotStep?=null
)
interface FilterByDate {
   var avg:Short
   var count:Int
   var id:Long
   var name:String
}
interface ChatListByOperatorId{
    var chatId:Long
    var createdDate:Date
    var messagesNumber:Int
    var operatorName:String
}