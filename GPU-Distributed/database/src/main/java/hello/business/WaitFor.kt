package hello.business

data class WaitFor(
		val lock: Lock,
		val transHasLock: Transaction,
		val transWaitsLock: Transaction
)