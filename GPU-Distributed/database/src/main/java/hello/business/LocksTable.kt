package hello.business

import org.springframework.stereotype.Service
import java.util.*

@Service
class LocksTable(
) {
	private val locks = Collections.synchronizedMap(mutableMapOf<Any, Lock>())
	
	fun getLock(resource: Any) = locks.getValue(resource)
	
	fun isUnlocked(transaction: Transaction, table: String, resource: Any) = !isLocked(transaction, table, resource)
	
	fun isLocked(transaction: Transaction, table: String, resource: Any): Boolean {
		if (locks.containsKey(resource)) {
			val lock = locks[resource] ?: return false
			return lock.transaction == transaction && lock.table == table
		}
		return false
	}
	
	fun lock(lock: Lock, resource: Any) {
		locks[resource] = lock
	}
	
	fun hasReadLock(transaction: Transaction, table: String?, resource: Any): Boolean {
		if (locks.containsKey(resource)) {
			val lock = locks[resource] ?: return false
			return lock.transaction == transaction && lock.table == table && lock.type == LockType.READ
		}
		return false
	}
	
}