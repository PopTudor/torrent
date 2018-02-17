package hello.business

import org.springframework.stereotype.Service

@Service
class TwoPhaseScheduler(
		private val locksTable: LocksTable,
		private val waitForGraphTable: WaitForGraphTable
) {
	
	@Synchronized
	fun readLock(transaction: Transaction, resource: Any): Lock {
		val table = resource.javaClass.simpleName
		val lock = Lock(LockType.READ, resource, table, transaction)
		return when {
			locksTable.isUnlocked(lock) -> {
				locksTable += lock
				lock
			}
			locksTable.hasReadLock(lock) -> {
				locksTable += lock
				lock
			}
			
			locksTable.hasWriteLock(lock) -> {
				if (locksTable.hasWriteLockForTransaction(transaction)) {
					return lock
				}
				
				while (locksTable.hasWriteLock(lock)) {
					if (isDeadlock()) {
						releaseLocks(transaction)
						transaction.status = TransactionStatus.ABORT
					}
					val transHasLock = locksTable[lock][0].transaction
					waitForGraphTable += WaitFor(lock, transHasLock, transaction)
					Thread.sleep(100)
				}
				lock
			}
			else -> throw IllegalArgumentException()
		}
	}
	
	@Synchronized
	fun writeLock(transaction: Transaction, resource: Any): Lock {
		val table = resource.javaClass.simpleName
		val lock = Lock(LockType.WRITE, resource, table, transaction)
		return when {
			locksTable.isUnlocked(lock) -> {
				locksTable += (lock)
				lock
			}
			locksTable.hasReadLock(lock) -> {
				locksTable += lock
				lock
			}
			locksTable.hasWriteLock(lock) -> {
				while (locksTable.hasWriteLock(lock)) {
					if (isDeadlock()) {
						releaseLocks(transaction)
						transaction.status = TransactionStatus.ABORT
					}
					val transHasLock = locksTable[lock][0].transaction
					waitForGraphTable += WaitFor(lock, transHasLock, transaction)
					Thread.sleep(100)
				}
				locksTable += lock
				lock
			}
			else -> throw IllegalArgumentException("Something is wrong")
		}
	}
	
	private fun isDeadlock(): Boolean {
		return false
	}
	
	fun releaseLocks(transaction: Transaction) {
		locksTable.forEach {
			if (it.transaction.id == transaction.id) {
				locksTable -= it
			}
		}
	}
	
	fun release(lock: Lock) {
		locksTable -= lock
	}
	
}

enum class LockStatus { SUCCESS, FAIL }