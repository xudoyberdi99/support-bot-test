package on.insurance.supportbot


import on.insurance.supportbot.teligram.Admin
import on.insurance.supportbot.teligram.Group
import on.insurance.supportbot.teligram.MessageEntity
import on.insurance.supportbot.teligram.User
import org.springframework.util.FileCopyUtils
import org.springframework.http.HttpEntity
import org.springframework.http.ResponseEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.*
import java.io.FileInputStream
import java.io.IOException
import javax.servlet.http.HttpServletResponse
import javax.validation.constraints.NotNull


@RestController
@RequestMapping("/api/v1/operator")
class OperatorController(
    private val service: OperatorService,
    private val userService: UserService,
    private val groupService: GroupService,
    private val messageService: MessageService
) {
    @PostMapping
    fun create(@RequestBody dto:OperatorCreateDto) = service.create(dto)

    @GetMapping("{id}")
    fun get(@PathVariable id: Long): OperatorDto = service.get(id)

    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @RequestBody dto: OperatorUpdateDto) = service.update(id, dto)


    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)

    @GetMapping
    fun getAllList(pageable: Pageable): Page<User> = userService.operatorList(pageable)
}

@RestController
@RequestMapping("api/v1/chat")
class GroupController(
    private val groupService: GroupService,
    private val messageService: MessageService
    ) {
    @GetMapping("filter")
    fun getChatsByDateFilter(
        @RequestParam fromDate: Long,
        @RequestParam toDate: Long,
        pageable: Pageable
    ): Page<FilterByDate> =
        groupService.OperatorListByDate(fromDate, toDate, pageable)

    @GetMapping("ChatsListByOperatorId/{id}")
    fun ChatsListByOperatorId(
        @PathVariable id: Long,
        @RequestParam fromDate: Long,
        @RequestParam toDate: Long,
        pageable: Pageable
    ): Page<ChatListByOperatorId> =
        groupService.chatListOperatorId(id, fromDate, toDate, pageable)

    @GetMapping("{groupId}")
    fun getAllMessageByGroupId(@PathVariable groupId: Long): List<MessageEntity> {
        return messageService.getAllMessageByGroupId(groupId)
    }


    @GetMapping("groupList")
    fun getAllGroupList(@RequestBody dto: GroupsByOperatorIdDto): List<Group> =
        groupService.groupsByOperatorId(dto)

    @GetMapping("messageList/{groupId}")
    fun getAllMessageList(@PathVariable groupId: Long): List<MessageEntity> =
        messageService.getAllMessageByGroupId(groupId)
}



@RestController
@RequestMapping("api/v1/user")
class UserController(private val userService: UserService) {

    @GetMapping()
    fun page(@NotNull  pageable: Pageable): Page<ResponseUser> = userService.userListWithPagination(pageable)

    @GetMapping("{id}")
    fun getUser(@PathVariable("id") id: Long): ResponseUser = userService.getContact(id)

    @PutMapping("{id}")
    fun editUser(@PathVariable("id") id: Long, @RequestBody userRequest: UserRequest) =
        userService.editUser(id, userRequest)
}

@RestController
@RequestMapping("api/v1/queue")
class QueueController(private val userService: UserService) {
    @GetMapping()
    fun queue(pageable: Pageable): Page<ResponseUser> = userService.queueListWithPagination(pageable)
}
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
 private val jwtProvider: JwtProvider,
 private val authenticationManager: AuthenticationManager
){
    @PostMapping("/login")
    fun loginUser(@RequestBody loginDto:LoginDto): HttpEntity<*>? {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                loginDto.username,loginDto.password
            )
        )
        val admin= authentication.principal as Admin
        val token = jwtProvider.generateToken(admin.username)
        return ResponseEntity.ok(token)
    }
}

@RestController
@RequestMapping("/api/v1/document")
class DocumentController(
){
    val document: String ="documents"

    @GetMapping("getFromFileSystem/{name}")
    @Throws(IOException::class)
    fun getFromFileSystem(@PathVariable name: String, response: HttpServletResponse) {
            response.setHeader(
                "Content-Disposition",
                "attachment; file=\"" + name + "\""
            )
            val fileInputStream = FileInputStream(document + "/" + name)
            FileCopyUtils.copy(fileInputStream, response.outputStream)
        }

}




