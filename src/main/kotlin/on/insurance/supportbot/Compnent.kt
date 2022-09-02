package on.insurance.supportbot

import on.insurance.supportbot.teligram.Admin
import on.insurance.supportbot.teligram.Role
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class DataLoader(
    private val adminRepository: AdminRepository,
    private val passwordEncoder: PasswordEncoder,

    @Value("\${spring.datasource.initialization-mode}")
    private val initialModeType: String
):CommandLineRunner{

    override fun run(vararg args: String?) {
        if (initialModeType.equals("always")){

            adminRepository.save(
                Admin(
                    "Admin",
                    passwordEncoder.encode("12345"),
                    Role.ADMIN
                )
            )
        }

    }
}