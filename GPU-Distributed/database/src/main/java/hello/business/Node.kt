package hello.business

data class Node(
		val lock: Lock,
		val transWaitsLock: Transaction,
		val transHasLock: Transaction
)