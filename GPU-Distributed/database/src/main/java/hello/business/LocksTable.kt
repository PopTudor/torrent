package hello.business

import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import java.util.*

@Service
class LocksTable(val transactionsTable: TransactionsTable) {
	private val locks = Collections.synchronizedMap(LinkedMultiValueMap<Any, Lock>())
	
	@Synchronized
	fun hasReadLock(lock: Lock) = when {
		lock.resource !in locks.keys -> false
		else -> {
			locks[lock.resource]!!.any { it.type == LockType.READ }
		}
	}
	
	@Synchronized
	fun hasWriteLock(lock: Lock) = when {
		lock.resource !in locks.keys -> false
		else -> {
			locks[lock.resource]!!.any { it.type == LockType.WRITE }
		}
	}
	
	operator fun get(lock: Lock): List<Lock> {
		return locks[lock.resource]!!.filter { it.resource == lock.resource }
	}
	
	fun forEach(body: (Lock) -> Unit) {
		locks.values.flatten().forEach(body)
	}
	
	fun isUnlocked(lock: Lock): Boolean {
		return lock.resource !in locks.keys
	}
	
	operator fun plusAssign(lock: Lock) {
		val key = locks[lock.resource]
		if (key == null)
			locks[lock.resource] = mutableListOf(lock)
		else
			locks[lock.resource]?.add(lock)
		transactionsTable += lock.transaction
	}
	
	operator fun minusAssign(lock: Lock) {
		locks -= lock.resource
		transactionsTable += lock.transaction
	}
}