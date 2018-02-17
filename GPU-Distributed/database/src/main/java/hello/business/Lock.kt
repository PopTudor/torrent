package hello.business

import java.util.concurrent.locks.ReentrantReadWriteLock

data class Lock(
		val type: LockType,
		val resource: Any,
		val table: String,
		var transaction: Transaction
) : ReentrantReadWriteLock() {
	val id: Int = randInt()
	
	fun isReadLock() = type == LockType.READ
	fun isWriteLock() = type == LockType.WRITE
}

enum class LockType { READ, WRITE }

enum class TransactionState { AQUIRED, BLOCKED, ABORT }