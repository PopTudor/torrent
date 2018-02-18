package hello.business

object TransactionHistory {
	private val history = mutableListOf<Transaction>()
	
	@Synchronized
	fun add(transaction: Transaction) {
		history += transaction
	}
	
	@Synchronized
	fun remove(transaction: Transaction) {
		history -= transaction
	}
	
	operator fun plusAssign(transaction: Transaction) = add(transaction)
	operator fun minusAssign(transaction: Transaction) = remove(transaction)
	
}