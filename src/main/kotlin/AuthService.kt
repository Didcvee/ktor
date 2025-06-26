package com.example

import at.favre.lib.crypto.bcrypt.BCrypt
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.InetAddress
import java.util.Random

object AuthService {

    //USERS
    fun registerUser(name: String, email: String, password: String): Boolean {
        if (!isEmailValid(email)) {
            return false
        }

        val hashedPassword = hashPassword(password)
        return transaction {
            if (Users.select { Users.email eq email }.empty()) {
                Users.insert {
                    it[Users.name] = name
                    it[Users.email] = email
                    it[Users.passwordHash] = hashedPassword
                }
                true
            } else {
                false
            }
        }
    }

    fun loginUser(email: String, password: String): Boolean {
        val user = transaction {
            Users.select { Users.email eq email }
                .mapNotNull { it[Users.passwordHash] }
                .firstOrNull()
        }
        return user != null && checkPassword(password, user)
    }

    fun generateResetCode(email: String): String? {
        val code = generate4DigitCode()
        return transaction {
            val updatedRows = Users.update({ Users.email eq email }) {
                it[resetToken] = code
            }
            if (updatedRows > 0) code else null
        }
    }

    fun resetPassword(code: String, newPassword: String): Boolean {
        return transaction {
            val email = Users.select { Users.resetToken eq code }
                .mapNotNull { it[Users.email] }
                .firstOrNull()

            if (email != null) {
                val hashedPassword = hashPassword(newPassword)
                Users.update({ Users.email eq email }) {
                    it[passwordHash] = hashedPassword
                    it[resetToken] = null
                }
                true
            } else {
                false
            }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return try {
            val domain = email.substringAfter("@")
            val addresses = InetAddress.getAllByName(domain)
            addresses.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    private fun generate4DigitCode(): String {
        return (1000..9999).random().toString()
    }

    private fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    private fun checkPassword(password: String, hash: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hash).verified
    }


    //SNEAKERS
    fun selectSneakers(): List<Sneaker> {
        try {
            return transaction {
                Sneakers.selectAll().map {
                    Sneaker(
                        id = it[Sneakers.id],
                        name = it[Sneakers.name],
                        description = it[Sneakers.description],
                        cost = it[Sneakers.cost],
                        count = it[Sneakers.count],
                        discount = it[Sneakers.discount],
                        photo = it[Sneakers.photo],
                        gender = it[Sneakers.gender],
                        bootSize = it[Sneakers.bootSize],
                        categoryId = it[Sneakers.categoryId]
                    )
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Error selecting sneakers: ${e.message}", e)
        }
    }


    fun selectCategories(): List<Category> {
        try {
            return transaction {
                Categories.selectAll().map {
                    Category(
                        id = it[Categories.id],
                        name = it[Categories.name],
                        description = it[Categories.description],
                    )
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Error selecting categories: ${e.message}", e)
        }
    }

    fun buySneakers(req: BuySneakersRequest): Boolean {
        return try {
            transaction {
                // Получаем все нужные кроссовки по ID одним запросом
                val sneakersMap = Sneakers
                    .select { Sneakers.id inList req.sneakers.map { it.sneakerId } }
                    .associateBy({ it[Sneakers.id] }, { it[Sneakers.cost] })

                // Проверка: все ли кроссовки найдены
                if (sneakersMap.size != req.sneakers.size) {
                    throw IllegalArgumentException("Некоторые кроссовки не найдены в базе")
                }

                // Вычисляем итоговую сумму
                val total = req.sneakers.sumOf { item ->
                    val price = sneakersMap[item.sneakerId] ?: error("Нет цены")
                    price * item.count.toBigDecimal()
                }

                // Вставляем заказ
                val orderId = Orders.insertAndGetId {
                    it[customerName] = null
                    it[customerEmail] = null
                    it[totalAmount] = total
                }

                // Вставляем записи в order_items
                req.sneakers.forEach { item ->
                    val price = sneakersMap[item.sneakerId]!!
                    OrderItems.insert {
                        it[OrderItems.orderId] = orderId.value
                        it[sneakerId] = item.sneakerId
                        it[quantity] = item.count
                        it[priceAtOrder] = price
                    }
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getAllOrders(): List<OrderDto> {
        try {
            return transaction {
                val ordersMap = mutableMapOf<Int, OrderDto>()

                (Orders innerJoin OrderItems)
                    .selectAll()
                    .orderBy(Orders.orderDate, SortOrder.DESC)
                    .forEach { row ->
                        val orderId = row[Orders.id].value
                        val order = ordersMap.getOrPut(orderId) {
                            OrderDto(
                                id = orderId,
                                orderDate = row[Orders.orderDate].toString(), // Можно отформатировать
                                customerName = row[Orders.customerName],
                                customerEmail = row[Orders.customerEmail],
                                totalAmount = row[Orders.totalAmount].toString(),
                                items = mutableListOf()
                            )
                        }

                        val item = OrderItemDto(
                            id = row[OrderItems.id].value,
                            sneakerId = row[OrderItems.sneakerId],
                            quantity = row[OrderItems.quantity],
                            priceAtOrder = row[OrderItems.priceAtOrder].toString()
                        )

                        (order.items as MutableList).add(item)
                    }

                ordersMap.values.toList()
            }
        } catch (e: Exception) {
            throw RuntimeException("Error fetching orders: ${e.message}", e)
        }
    }
}


