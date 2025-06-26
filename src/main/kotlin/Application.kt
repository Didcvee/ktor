package com.example

import com.example.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.math.BigDecimal
import java.util.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.AuthService.getAllOrders
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureSerialization()
        configureRouting()
        DatabaseFactory.init()
    }.start(wait = true)
}

object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toPlainString())
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        return BigDecimal(decoder.decodeString())
    }
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            serializersModule = SerializersModule {
                contextual(BigDecimalSerializer) // Добавляем поддержку BigDecimal
            }
        })
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT.require(Algorithm.HMAC256("secret"))
                    .withIssuer("http://localhost:8080")
                    .withAudience("http://localhost:8080/mainWindow") // Должно совпадать
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("email").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}

fun Application.configureRouting() {
    routing {
        post("/register") {
            val request = call.receive<UserRegisterRequest>()
            if (AuthService.registerUser(request.name, request.email, request.password)) {
                call.respond(mapOf("Email" to "Успешно"))
            } else {
                call.respond(mapOf("error" to "Email уже зарегистрирован"))
            }
        }

        post("/login") {
            val request = call.receive<UserLoginRequest>()
            try {
            if (AuthService.loginUser(request.email, request.password)) {
//                val jwtSecret = environment.config.propertyOrNull("jwt.secret")?.getString()
                val jwtSecret = "secret"
                    ?: throw ApplicationConfigurationException("jwt.secret not found")
                log.info("JWT Secret: $jwtSecret")

//                val jwtIssue = environment.config.propertyOrNull("jwt.issue")?.getString()
                val jwtIssue = "http://localhost:8080"
                    ?: throw ApplicationConfigurationException("jwt.issue not found")
                log.info("JWT Issue: $jwtIssue")

//                val jwtAudience = environment.config.propertyOrNull("jwt.audience")?.getString()
                val jwtAudience = "http://localhost:8080/mainWindow"
                    ?: throw ApplicationConfigurationException("jwt.audience not found")
                log.info("JWT Audience: $jwtAudience")
                val token = JWT.create()
                    .withIssuer(jwtIssue)
                    .withAudience(jwtAudience)
                    .withClaim("email", request.email)
                    .withExpiresAt(Date(System.currentTimeMillis() + 10 * 60000))
                    .sign(Algorithm.HMAC256(jwtSecret))

                call.respond(mapOf("Token" to token))
                }
            } catch (e: Exception) {
                log.error("Error fetching sneakers: ${e.message}", e)
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }

        post("/forgot-password") {
            val request = call.receive<UserForgotPasswordRequest>()
            val code = AuthService.generateResetCode(request.email)

            if (code != null) {
                EmailService.sendResetEmail(request.email, code)
                call.respond(mapOf("message" to "Код на восстановление пароля отправлен"))
            } else {
                call.respond(mapOf("error" to "Email не найден"))
            }
        }

        post("/reset-password") {
            val request = call.receive<UserResetPasswordRequest>()
            if (AuthService.resetPassword(request.code, request.newPassword)) {
                call.respond(mapOf("message" to "Пароль успешно изменён"))
            } else {
                call.respond(mapOf("error" to "Неверный код"))
            }
        }

        authenticate("auth-jwt"){
            get("/mainWindow") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userEmail = principal?.payload?.getClaim("email")?.asString()

                    if (userEmail == null) {
                        call.respond(HttpStatusCode.Unauthorized, "Invalid token")
                        return@get
                    }

                    log.info("User $userEmail is accessing /mainWindow")

                    val sneakers = AuthService.selectSneakers()
                    val categories = AuthService.selectCategories()

                    if (sneakers.isNotEmpty() && categories.isNotEmpty()) {
                        call.respond(SneakerCategoryResponse(sneakers, categories))
                    } else {
                        call.respond(mapOf("error" to "No sneakers or categories found"))
                    }
                } catch (e: Exception) {
                    log.error("Error fetching sneakers: ${e.message}", e)
                    call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
                }
            }
        }

        post("/buySneakers") {
            val request = call.receive<BuySneakersRequest>()
            if (AuthService.buySneakers(request)){
                call.respond(mapOf("message" to "Куплено"))
            } else{
                call.respond(mapOf("error" to "Ну вот не куплено"))
            }
        }

        get("/allOrders") {
            try {
                val orders = getAllOrders() // та функция, что мы ранее реализовали
                call.respond(orders)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Ошибка при получении заказов: ${e.message}"))
            }
        }
    }
}
