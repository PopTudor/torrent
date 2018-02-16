package hello.business

import hello.data.account.Account

object TransactionHistory {
	private val history = mutableListOf<Account>()
	
	@Synchronized
	fun add(account: Account) {
		history += account
	}
	
	@Synchronized
	fun remove(account: Account) {
		history -= account
	}
	
	operator fun plusAssign(account: Account) = add(account)
	operator fun minusAssign(account: Account) = remove(account)
	
}