package hello.business

data class WaitFor(
		val lock: Lock,
		val transHasLock: Transaction,
		val transWaitsLock: Transaction,
		var isBeingVisited: Boolean = false,
		var visited: Boolean = false
) {
	fun getNeighbors(): List<Transaction> {
		return listOf(transHasLock, transWaitsLock)
	}
}