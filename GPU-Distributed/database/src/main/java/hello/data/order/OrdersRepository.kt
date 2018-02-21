package hello.data.order

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface OrdersRepository : CrudRepository<Order, String> {
	fun findOrderByAccountId(accountId: String)
}