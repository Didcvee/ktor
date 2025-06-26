package com.example

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class UserRegisterRequest(val name: String, val email: String, val password: String)

@Serializable
data class UserLoginRequest(val email: String, val password: String)

@Serializable
data class UserForgotPasswordRequest(val email: String)

@Serializable
data class UserResetPasswordRequest(val code: String, val newPassword: String)

@Serializable
data class BuySneakerRequest(val sneakerId: Int, val count: Int)

@Serializable
data class BuySneakersRequest(val sneakers: List<BuySneakerRequest>)


@Serializable
data class Sneaker(
    val id: Int,
    val name: String,
    val description: String,
    @Contextual val cost: BigDecimal,
    val count: Int,
    val discount: Int,
    val photo: String,
    val gender: Char,
    val bootSize: Int,
    val categoryId: Int
)

@Serializable
data class Category(
    val id: Int,
    val name: String,
    val description: String
)

@Serializable
data class SneakerCategoryResponse(
    val sneakers: List<Sneaker>,
    val categories: List<Category>
)

@Serializable
data class OrderItemDto(
    val id: Int,
    val sneakerId: Int,
    val quantity: Int,
    val priceAtOrder: String
)

@Serializable
data class OrderDto(
    val id: Int,
    val orderDate: String, // можно и как LocalDateTime
    val customerName: String?,
    val customerEmail: String?,
    val totalAmount: String,
    val items: List<OrderItemDto>
)