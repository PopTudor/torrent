package hello.business

import org.springframework.stereotype.Service
import java.util.*

@Service
class LocksTable(val transactionsTable: TransactionsTable) {
	private val locks = Collections.synchronizedMap(mutableMapOf<Any, Lock>())
	
	@Synchronized
	fun hasReadLock(lock: Lock) = when {
		lock.resource !in locks.keys -> false
		else -> locks[lock.resource]!!.type == LockType.READ
	}
	
	@Synchronized
	fun hasWriteLock(lock: Lock) = when {
		lock.resource !in locks.keys -> false
		else -> locks[lock.resource]?.type == LockType.WRITE
	}
	
	operator fun get(lock: Lock) = locks[lock.resource]
	
	fun forEach(body: (Lock) -> Unit) {
		locks.values.forEach(body)
	}
	
	fun isUnlocked(lock: Lock): Boolean {
		return lock.resource !in locks.keys
	}
	
	operator fun plusAssign(lock: Lock) {
		locks[lock.resource] = lock
		transactionsTable += lock.transaction
	}
	
	operator fun minusAssign(lock: Lock) {
		locks -= lock.resource
		transactionsTable += lock.transaction
	}
}