package hello.business

import java.util.concurrent.locks.ReentrantReadWriteLock

data class Lock(
		val type: LockType,
		val resource: Any,
		val table: String,
		val transaction: Transaction
) : ReentrantReadWriteLock() {
	val id: Int = randInt()
	
}

enum class LockType { READ, WRITE }