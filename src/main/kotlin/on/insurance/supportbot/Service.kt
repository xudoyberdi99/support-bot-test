package on.insurance.supportbot

import on.insurance.supportbot.teligram.*
import on.insurance.supportbot.teligram.Contact
import on.insurance.supportbot.teligram.Group
import on.insurance.supportbot.teligram.MessageEntity
import on.insurance.supportbot.teligram.MessageType.*
import on.insurance.supportbot.teligram.User
import org.springframework.context.annotation.Lazy
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Message
import java.io.File
import java.util.*
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.*


interface UserService {
    fun getUser(chatId: Long): User
    fun update(user: User)
    fun get(userId: Long): Group
    fun backOperator(operator: User)
    fun operatorIsActive(operator: User)
    fun emptyOperator(user: User): User?
    fun checkOperator(contact: Contact, user: User): User
    fun operatorList(pageable: Pageable): Page<User>

    fun userListWithPagination(pageable: Pageable): Page<ResponseUser>
    fun queueListWithPagination(pageable: Pageable): Page<ResponseUser>
    fun getContact(id: Long): ResponseUser
    fun editUser(id: Long, userRequest: UserRequest)

}

interface GroupService {
    fun update(group: Group): Group
    fun getGroupByUserId(user: User): Group
    fun getNewGroupByOperator(operator: User): Group?
    fun getGroupByOperatorId(operator: User): Group?

    //operator_id buyicha groupList admin uchun
//    operator_id, first_day, last_day kirib keladi

    fun groupsByOperatorId(groupsByOperatorIdDto: GroupsByOperatorIdDto): List<Group>
    fun OperatorListByDate(fromDate:Long,toDate:Long,pageable: Pageable):Page<FilterByDate>
    fun chatListOperatorId(operatorId: Long,fromDate:Long,toDate:Long,pageable: Pageable):Page<ChatListByOperatorId>
}

interface MessageService{
    fun creat(message:Message,group: Group,user: User)
    fun getUserMessage(group: Group):List<MessageEntity>
    //messagelar Listini group id buyicha olish
    fun getAllMessageByGroupId(groupId: Long): List<MessageEntity>
}

interface ContactService {
    fun saveContact(phoneNumber: String, username: String, user: User): Contact
    fun checkContact(contact: Contact, user: User)
}

interface OperatorService {
    fun create(dto: OperatorCreateDto)
    fun update(id: Long, dto: OperatorUpdateDto)
    fun get(id: Long): OperatorDto
    fun delete(id: Long)
    fun listOfOperator(): List<OperatorDto>

}


@Service
class MessageServiceImpl(
    val messageRepository: MessageRepository,
    @Lazy
    val myBot: MyBot,
    val attachmentRepository: AttachmentRepository,
) : MessageService {
    override fun creat(message:Message, group: Group, user: User) {
        val sendDocument = SendDocument()
        message.caption?.run { }
        message.text?.run {
            messageRepository.save(MessageEntity(user.chatId, message.messageId, user, group, TEXT, null, this))
        }
        message.document?.run {
            val document = message.document
            val originalName = document.fileName
            val name = "${Date().time}-${originalName}"
            val size = document.fileSize
            val contentType = document.mimeType

            val attachment = Attachment(originalName, size, contentType, name)
            val messageEntity = MessageEntity(
                user.chatId,
                message.messageId,
                user,
                group,
                DOCUMENT,
                attachmentRepository.save(attachment)
            )
            message.caption?.run { messageEntity.caption = this }
            messageRepository.save(messageEntity)
            sendDocument.document = InputFile(document.fileId)
            File("./documents").mkdirs()
            val file = File("./documents/${name}")
            file.writeBytes(
                myBot.getFromTelegram(document.fileId, myBot.botToken)
            )
        }
        message.audio?.run {
            val document = message.audio
            val originalName = document.fileName
            val name = "${Date().time}-${originalName}"
            val size = document.fileSize
            val contentType = document.mimeType

            val attachment = Attachment(originalName, size, contentType, name)
            val messageEntity =
                MessageEntity(user.chatId, message.messageId, user, group, AUDIO, attachmentRepository.save(attachment))
            message.caption?.run { messageEntity.caption = this }
            messageRepository.save(messageEntity)
            sendDocument.document = InputFile(document.fileId)
            File("./documents").mkdirs()
            val file = File("./documents/${name}")
            file.writeBytes(
                myBot.getFromTelegram(document.fileId, myBot.botToken)
            )
        }
        message.voice?.run {
            val document = message.voice
            val originalName = "voice.mp3"
            val name = "${Date().time}-${originalName}"
            val size = document.fileSize
            val contentType = document.mimeType

            val attachment = Attachment(originalName, size, contentType, name)
            val messageEntity =
                MessageEntity(user.chatId, message.messageId, user, group, VOICE, attachmentRepository.save(attachment))
            message.caption?.run { messageEntity.caption = this }
            messageRepository.save(messageEntity)
            sendDocument.document = InputFile(document.fileId)
            File("./documents").mkdirs()
            val file = File("./documents/${name}")
            file.writeBytes(
                myBot.getFromTelegram(document.fileId, myBot.botToken)
            )
        }
        message.photo?.run {
            val document = message.photo[3]
            val originalName = "photo.png"
            val name = "${Date().time}-${originalName}"
            val size = document.fileSize.toLong()
            val contentType = "png"

            val attachment = Attachment(originalName, size, contentType, name)
            val messageEntity =
                MessageEntity(user.chatId, message.messageId, user, group,PHOTO, attachmentRepository.save(attachment))
            message.caption?.run { messageEntity.caption = this }
            messageRepository.save(messageEntity)
            sendDocument.document = InputFile(document.fileId)
            File("./documents").mkdirs()
            val file = File("./documents/${name}")
            file.writeBytes(
                myBot.getFromTelegram(document.fileId, myBot.botToken)
            )
        }
    }



    override fun getUserMessage(group: Group): List<MessageEntity> {
        messageRepository.getUserMessage(group.user!!.id!!, group.id!!)?.run {
            return this
        }
        return emptyList()
    }

    override fun getAllMessageByGroupId(groupId: Long): List<MessageEntity> {
        return messageRepository.getAllMessageByGroupId(groupId)
    }
}

@Service
class GroupServiceImpl(
    val groupRepository: GroupRepository,
    val userService: UserService,
) : GroupService {

    override fun update(group: Group): Group {
        return groupRepository.save(group)
    }

    override fun getGroupByUserId(user: User): Group {
        return groupRepository.getGroupByUserIdAndActive(user.id!!).run { this } ?: createGroup(user)
    }

    fun createGroup(user: User): Group {
        val emptyOperator = userService.emptyOperator(user)
        emptyOperator?.run { userService.backOperator(this) }
        return groupRepository.save(Group(user, emptyOperator, user.language))
    }


    override fun getGroupByOperatorId(operator: User): Group? {
        return groupRepository.getGroupByOperatorIdAndActive(operator.id!!)?.run { this }
    }


    override fun getNewGroupByOperator(operator: User): Group? {
        return groupRepository.getGroupByOperatorAndLanguageAndActive(operator.language.name)?.run { this }
    }

    override fun groupsByOperatorId(dto: GroupsByOperatorIdDto): List<Group> {
        val firstTime=Date(dto.first_day)
        val lastTime=Date(dto.last_day)
        return groupRepository.GroupsByOperatorId(dto.operator_id, firstTime,lastTime)
    }

    override fun OperatorListByDate(fromDate:Long, toDate: Long, pageable: Pageable): Page<FilterByDate> {
        val from=Date(fromDate)
        val to=Date(toDate)
        return groupRepository.filterDate(from,to,pageable)
    }

    override fun chatListOperatorId(
        operatorId: Long,
        fromDate: Long,
        toDate: Long,
        pageable: Pageable
    ): Page<ChatListByOperatorId> {
        val from=Date(fromDate)
        val to=Date(toDate)
        return groupRepository.chatListOperatorId(operatorId,from,to,pageable)
    }
}

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val operatorRepository: OperatorRepository,
    private val contactRepository: ContactRepository
) : UserService {
    override fun getUser(chatId: Long): User {
        return userRepository.findByChatIdd(chatId)?.run { this } ?: createUser(chatId)
    }

    override fun emptyOperator(user: User): User? {
        return userRepository.emptyOperator(user.language.name)
    }

    fun createUser(chatId: Long): User {
        return userRepository.save(User(chatId))
    }

    override fun update(user: User) {
        userRepository.save(user)
    }

    override fun get(userId: Long): Group {
        TODO("Not yet implemented")
    }

    override fun backOperator(operator: User) {
        operator.isActive = false
        userRepository.save(operator)
    }

    override fun operatorIsActive(operator: User) {
        operator.isActive = true
        userRepository.save(operator)
    }

    override fun checkOperator(contact: Contact, user: User): User {
        var phoneNumber = contact.phoneNumber
        if (phoneNumber.startsWith("+")){
            phoneNumber=phoneNumber.substring(4)
        }else{
            phoneNumber=phoneNumber.substring(3)
        }
        if (operatorRepository.existsByPhoneNumber(phoneNumber)) {
            user.run { this.role = Role.OPERATOR }
        } else {
            user.role = Role.USER
        }
        return user
    }

    override fun operatorList(pageable: Pageable): Page<User> {
        return userRepository.getAllOperatorListByRole(pageable)
    }


    //this is for admin  user list with pagination
    override fun userListWithPagination(pageable: Pageable): Page<ResponseUser> = contactRepository.userInfo(pageable)
    override fun queueListWithPagination(pageable: Pageable): Page<ResponseUser> = contactRepository.queueInfo(pageable)


    override fun getContact(id: Long): ResponseUser = contactRepository.getUser(id)
    override fun editUser(id: Long, userRequest: UserRequest) {
        val user = userRepository.findByIdNotDeleted(id) ?: throw NullPointerException("we have not this user")
        val contact = contactRepository.findByUsername(id) ?: throw NullPointerException("we have not this user")
        userRequest.run {

            fullName?.run {
                contact.userName = fullName!!
                contactRepository.save(contact)
            }
            state?.run {
                user.botStep = state!!
                userRepository.save(user)
            }

        }
    }


}

@Service
class ContactServiceImpl(private val contactRepository: ContactRepository) : ContactService {
    override fun saveContact(phoneNumber: String, username: String, user: User): Contact {
        val contact = Contact(phoneNumber, user, username)
        return contactRepository.save(contact)
    }

    override fun checkContact(contact: Contact, user: User) {

    }
}

@Service
class OperatorServiceImpl(
    private val repository: OperatorRepository
) : OperatorService {
    override fun create(dto: OperatorCreateDto) {
        repository.save(dto.toEntity())
    }

    override fun update(id: Long, dto: OperatorUpdateDto) {
        val entity = repository.findByIdNotDeleted(id) ?: throw NullPointerException("we have not this operator")
        dto.run {
            name?.run { entity.name = this }
            phoneNumber?.run { entity.phoneNumber = this }
            repository.save(entity)
        }
    }

    override fun get(id: Long): OperatorDto = repository.findByIdNotDeleted(id)?.run { OperatorDto.toDto(this) }
        ?: throw NullPointerException("Couldn't find by id")


    override fun delete(id: Long) {
        repository.trash(id)
    }

    override fun listOfOperator() = repository.getAllOperator().map(OperatorDto.Companion::toDto)
}

@Service
class AuthService(
    private val adminRepository: AdminRepository
):UserDetailsService{

    @Throws(NullPointerException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val admin = adminRepository.findByUsername(username)?:throw NullPointerException("Invalid username or password")
        return admin
    }

}






