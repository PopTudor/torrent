package hello.data.history

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HistoryRepository : CrudRepository<History, String> {
	override fun findOne(id: String): History
}