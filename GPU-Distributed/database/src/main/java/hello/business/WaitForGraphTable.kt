package hello.business

import org.springframework.stereotype.Service
import java.util.*


@Service
class WaitForGraphTable(
		val transactionsTable: TransactionsTable
) {
	private val graph = Collections.synchronizedList(mutableListOf<WaitFor>())
	
	operator fun plusAssign(waitFor: WaitFor) {
		graph += waitFor
	}
	
	@Synchronized
	fun isDeadlock(): Boolean {
		return hasCycle()
	}
	
	@Synchronized
	fun release(transaction: Transaction) {
		graph.removeIf { it.transWaitsLock.id == transaction.id }
		transactionsTable -= transaction
	}
	
	
	private fun hasCycle(): Boolean {
		val visited = mutableListOf<WaitFor>()
		graph.forEach {
			if (hasCycle(it, visited)) return true
		}
		return false
	}
	
	private fun hasCycle(node: WaitFor, visited: MutableList<WaitFor>): Boolean {
		if (node in visited) return true
		visited += node
		for (nextNode in graph) {
			if (hasCycle(nextNode!!, visited)) return true
		}
		visited.removeAt(visited.size - 1)
		return false
	}
}