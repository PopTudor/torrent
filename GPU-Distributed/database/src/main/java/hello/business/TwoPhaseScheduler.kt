package hello.business

import org.springframework.stereotype.Service

@Service
class TwoPhaseScheduler(
		private val locksTable: LocksTable,
		private val waitForGraphTable: WaitForGraphTable
) {
	
	@Synchronized
	fun readLock(transaction: Transaction, resource: Any): Lock {
		val lock = Lock(LockType.READ, resource, resource.toString(), transaction)
		return when {
			locksTable.isUnlocked(lock) -> {
				locksTable += lock
				lock
			}
			locksTable.hasWriteLock(lock) -> {
				while (locksTable.hasWriteLock(lock)) {
					if (isDeadlock()) {
						releaseLocks(transaction)
						transaction.status = TransactionStatus.ABORT
					}
//					val transHasLock = locksTable[lock].transaction
//					waitForGraphTable += WaitFor(lock, transHasLock, transaction)
					Thread.sleep(100)
				}
				lock
			}
			locksTable.hasReadLock(lock) -> {
				locksTable += lock
				lock
			}
			else -> throw IllegalArgumentException()
		}
	}
	
	private fun isDeadlock(): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	private fun releaseLocks(transaction: Transaction) {
		locksTable.forEach {
			if (it.transaction.id == transaction.id) {
				locksTable -= it
			}
		}
	}
	
//	fun writeLock(transaction: Transaction, resource: Any): WaitFor {
//		val table = resource.javaClass.simpleName
//		val lock = Lock(LockType.READ, resource, table, transaction)
//		when {
//			locksTable.isUnlocked(lock) -> locksTable.lock(lock)
//			else -> TODO("wait for unlock")
//		}
//	}
	
	fun release(lock: Lock) {
		locksTable -= lock
	}
	
}

enum class LockStatus { SUCCESS, FAIL }