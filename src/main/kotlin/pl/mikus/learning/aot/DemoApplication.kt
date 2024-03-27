package pl.mikus.learning.aot

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ImportRuntimeHints
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@ImportRuntimeHints(ExposedHints::class)
@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
	runApplication<DemoApplication>(*args)
}

@Component
@Transactional
class Demo : ApplicationRunner {
	override fun run(args: ApplicationArguments?) {
		SchemaUtils.create(Customers, Orders)

		Orders.deleteAll();
		Customers.deleteAll();

		"John,Teresa,Cleo,Sylvia,Romuald".split(",")
				.map { customerName ->
					Customers.insertAndGetId {
						it[name] = customerName
					}
				}

		val customerId = Customers.insertAndGetId {
			it[name] = "Marcin"
		}

		setOf(UUID.randomUUID(), UUID.randomUUID())
				.map { it.toString() }
				.forEach { productName ->
					Orders.insert {
						it[sku] = productName
						it[Orders.customerId] = customerId
					}
				}

		Customers.selectAll().forEach {
			println(it[Customers.id].toString() + ", " + it[Customers.name])
		}

		println("-".repeat(25))

		val customer = Customers.selectAll().where {
			Customers.name eq "Marcin"
		}.map {
			Customer ( it[Customers.id].value, it[Customers.name])
		}.first();


		println("Customer id same as first result: ${customerId} == ${customer.id}")

		(Customers innerJoin Orders)
				.selectAll()
				.where {
					Customers.id eq customerId
				}.forEach {println(it) }

	}

}

data class Customer(val id: Int?, val name: String, val orders:List<Order> = arrayListOf())
data class Order(val id: Int?, val sku: String)

object Customers : IntIdTable ("customers") {
	val name = text("name")
}

object Orders : IntIdTable (name = "orders") {
	val sku = text("sku")
	val customerId = reference("customers", Customers)
}

class ExposedHints : RuntimeHintsRegistrar {

	override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {

		arrayOf(
				org.jetbrains.exposed.spring.DatabaseInitializer::class,
				org.jetbrains.exposed.spring.SpringTransactionManager::class,
				java.util.Collections::class,
				Column::class,
				Database::class,
				Op::class ,
				Op.Companion::class ,
				DdlAware::class,
				Expression::class,
				ExpressionWithColumnType::class,
				ColumnType::class,
				DatabaseConfig::class,
				IColumnType::class,
				IntegerColumnType::class,
				PreparedStatementApi::class,
				ForeignKeyConstraint::class,
				IColumnType::class,
				QueryBuilder::class,
				Table::class,
				Transaction::class,
				TransactionManager::class,
				Column::class,
				Database::class,
				kotlin.jvm.functions.Function0::class,
				kotlin.jvm.functions.Function1::class,
				kotlin.jvm.functions.Function2::class,
				kotlin.jvm.functions.Function3::class,
				kotlin.jvm.functions.Function4::class,
				kotlin.jvm.functions.Function5::class,
				kotlin.jvm.functions.Function6::class,
				kotlin.jvm.functions.Function7::class,
				kotlin.jvm.functions.Function8::class,
				kotlin.jvm.functions.Function9::class,
				kotlin.jvm.functions.Function10::class,
				kotlin.jvm.functions.Function11::class,
				kotlin.jvm.functions.Function12::class,
				kotlin.jvm.functions.Function13::class,
				kotlin.jvm.functions.Function14::class,
				kotlin.jvm.functions.Function15::class,
				kotlin.jvm.functions.Function16::class,
				kotlin.jvm.functions.Function17::class,
				kotlin.jvm.functions.Function18::class,
				kotlin.jvm.functions.Function19::class,
				kotlin.jvm.functions.Function20::class,
				kotlin.jvm.functions.Function21::class,
				kotlin.jvm.functions.Function22::class,
				kotlin.jvm.functions.FunctionN::class
		)
				.map {  it.java }
				.forEach {
					hints.reflection().registerType(it, *MemberCategory.entries.toTypedArray())
				}

		arrayOf("META-INF/services/org.jetbrains.exposed.dao.id.EntityIDFactory",
				"META-INF/services/org.jetbrains.exposed.sql.DatabaseConnectionAutoRegistration",
				"META-INF/services/org.jetbrains.exposed.sql.statements.GlobalStatementInterceptor")
				.map { ClassPathResource(it) }
				.forEach { hints.resources().registerResource(it) }
	}

}