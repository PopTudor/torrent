package hello.business.command

import hello.data.order.Order
import hello.data.order.OrdersRepository

class DeleteOrder(
		val ordersRepository: OrdersRepository,
		val order: Order
) : Command {
	override var reverseCommand: Command? = null
		get() = CreateOrderCommand(ordersRepository, order)
	
	override fun execute() {
		ordersRepository.delete(order)
	}
}