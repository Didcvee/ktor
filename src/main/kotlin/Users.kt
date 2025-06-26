package com.example

import com.example.Categories.autoIncrement
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.timestamp
import java.math.BigDecimal
import java.time.Instant

object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = text("password_hash")
    val resetToken = text("reset_token").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())

    override val primaryKey = PrimaryKey(id)
}

object Sneakers : Table("sneakers") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val description = varchar("description", 255)
    val cost = decimal("cost", 10, 2)
    val count = integer("count")
    val discount = integer("discount")
    val photo = varchar("photo", 255)
    val gender = char("gender")
    val bootSize = integer("bootsize")
    val categoryId = integer("categoryid")

    override val primaryKey = PrimaryKey(id)
}


object Categories : Table("categories") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val description = varchar("description", 255)

    override val primaryKey = PrimaryKey(id)
}

object Orders : IntIdTable("orders") {
    val orderDate = datetime("order_date").defaultExpression(CurrentDateTime)
    val customerName = varchar("customer_name", 255).nullable()
    val customerEmail = varchar("customer_email", 255).nullable()
    val totalAmount = decimal("total_amount", 10, 2)
}

object OrderItems : IntIdTable("order_items") {
    val orderId = integer("order_id").references(Orders.id, onDelete = ReferenceOption.CASCADE)
    val sneakerId = integer("sneaker_id")
    val quantity = integer("quantity")
    val priceAtOrder = decimal("price_at_order", 10, 2)
}