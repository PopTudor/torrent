package hello.business

data class WaitFor(
		val lock: Lock,
		val transWaitsLock: Transaction,
		val transHasLock: Transaction
) {
	fun getNeighbors(): List<Transaction> {
		return listOf(transHasLock, transWaitsLock)
	}
}