package hello.business

import org.springframework.stereotype.Service
import java.util.*

@Service
class LocksTable {
	private val locks = Collections.synchronizedMap(mutableMapOf<Transaction, Lock>())
	
	operator fun get(lock: Lock): Lock = locks[lock.transaction]!!
	
	@Synchronized
	fun isLocked(lock: Lock): Boolean {
		if (lock.transaction in locks) {
			val lockedTransaction = locks[lock.transaction] ?: return false
			return lockedTransaction == lock
		}
		return false
	}
	
	fun isUnlocked(lock: Lock) = !isLocked(lock)
	
	@Synchronized
	fun lock(lock: Lock): Lock {
		locks[lock.transaction] = lock
		lock.readLock().lock()
		return lock
	}
	
	@Synchronized
	fun hasReadLock(lock: Lock): Boolean {
		if (lock.transaction in locks) {
			val lockedTransaction = locks[lock.resource] ?: return false
			return lockedTransaction == lock
		}
		return false
	}
	
	operator fun minusAssign(lock: Lock) {
		if (lock.isWriteLocked) lock.writeLock().unlock()
		lock.readLock().unlock()
		locks.remove(lock.transaction)
	}
	
}