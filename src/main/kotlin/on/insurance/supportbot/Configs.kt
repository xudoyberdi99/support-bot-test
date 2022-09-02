package on.insurance.supportbot

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.util.StringUtils
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import org.springframework.web.servlet.support.RequestContextUtils
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Configuration
class WebMvcConfigure : WebMvcConfigurer {

    @Bean
    fun errorMessageSource() = ResourceBundleMessageSource().apply {
        setDefaultEncoding(Charsets.UTF_8.name())
        setBasename("error")
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(object : HandlerInterceptorAdapter() {
            override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
                request.getHeader("hl")?.let {
                    RequestContextUtils.getLocaleResolver(request)
                        ?.setLocale(request, response, StringUtils.parseLocale(it))
                }
                return true
            }
        })
    }
}

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val authService: AuthService,
    private val jwtFilter: JwtFilter
): WebSecurityConfigurerAdapter() {

    @Throws(Exception::class)
    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService<UserDetailsService>(authService).passwordEncoder(passwordEncoder())
    }

    @Bean
    @Throws(Exception::class)
    override fun authenticationManagerBean(): AuthenticationManager? {
        return super.authenticationManagerBean()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
            .csrf().disable()
            .authorizeRequests()
            .antMatchers("/api/v1/**")
            .permitAll()
            .anyRequest()
            .authenticated()
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }
}