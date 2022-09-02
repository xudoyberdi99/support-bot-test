package on.insurance.supportbot.teligram

import on.insurance.supportbot.teligram.RoleService.RoleAdmin
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.awt.TrayIcon
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*
import javax.persistence.*

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
open class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP) var createdDate: Date? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP) var modifiedDate: Date? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false,
)

@Entity
class Contact(
    @Column(unique = true, nullable = false) var phoneNumber: String,
    @OneToOne var user: User,
    var userName: String
) : BaseEntity()

@Entity
@Table(name = "users")
class User(
    var chatId: Long,
    @Enumerated(EnumType.STRING) var botStep: BotStep = BotStep.START,
    @Enumerated(EnumType.STRING) var language: Language = Language.UZ,
    @Enumerated(EnumType.STRING) var role: Role? = null,
    var isActive: Boolean = true
) : BaseEntity()

@Entity(name = "message")
data class MessageEntity(
    val chatId:Long,
    val massageId:Int,
    @ManyToOne var user: User,
    @ManyToOne var group: Group,
    @Enumerated(EnumType.STRING) val messageType:MessageType,
    @OneToOne val attachment: Attachment?,
    val text: String? = null,
    var caption: String? = null,
    var isActive: Boolean? = true
) : BaseEntity()


@Entity(name = "groups")
class Group(
    @ManyToOne var user: User? = null,
    @ManyToOne var operator: User? = null,
    @Enumerated(EnumType.STRING) var language: Language?,
    var isRead:Boolean=false,
    var isActive: Boolean = true,
    var ball:Int=0,
) : BaseEntity()

 @Entity
class Operator(
     var languages: Int,
    var name: String,
    var phoneNumber: String,
) : BaseEntity()


@Entity
class Attachment(
    var fileOriginalName:String,
    var size:Long,
    var contentType:String,
    var name:String
) :BaseEntity()

@Entity
class Admin(
    @Column(unique = true, nullable = false) private var username: String,
    @Column private var password: String,
    @Enumerated(EnumType.STRING)  var role: Role=Role.ADMIN
) : BaseEntity(),UserDetails{
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        val grantedAuthorities= mutableListOf<GrantedAuthority>()
        grantedAuthorities.add(SimpleGrantedAuthority(Role.ADMIN.toString()))
        return grantedAuthorities
    }

    override fun getPassword(): String {
        return password
    }

    override fun getUsername(): String {
        return username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}