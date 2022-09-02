package on.insurance.supportbot

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtProvider {

    val now = Date()
    val accessKey = "auzNN7V0aB30poSilNi15HCiE"

    fun generateToken(username:String):String{
        val token:String=Jwts
            .builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + 1000 * 60 * 60 * 24))
            .signWith(SignatureAlgorithm.HS256,accessKey)
            .compact()

        return token
    }

    fun getUsernameFromToken(token:String):String?{
        try {
            return Jwts.parser()
                .setSigningKey(accessKey)
                .parseClaimsJws(token)
                .body
                .subject
        }catch (e:Exception ){
            return null
        }
    }
}

@Component
class JwtFilter(
    private val jwtProvider: JwtProvider,
    private val authService: AuthService
    ) : OncePerRequestFilter() {
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        var authorization = request.getHeader("Authorization")
        if (authorization!=null && authorization.startsWith("Bearer")){
            authorization=authorization.substring(7)
            val username = jwtProvider.getUsernameFromToken(authorization)
            if (username!=null){
                val userDetails = authService.loadUserByUsername(username)
                val usernamePasswordAuthenticationToken =
                    UsernamePasswordAuthenticationToken(userDetails, null,null)
                SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken
            }
        }
        filterChain.doFilter(request, response)
    }
}

