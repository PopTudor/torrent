package hello.business

data class WaitForData(
		val lockType: LockType,
		val lockTable: Any,
		val lockObject: Any,
		val transHasLock: Boolean,
		val transWaitsLock: Boolean
)