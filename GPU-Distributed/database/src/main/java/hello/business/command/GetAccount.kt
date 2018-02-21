package hello.business.command

import hello.data.account.Account
import hello.data.account.AccountRepository

class GetAccount(
		val accountRepository: AccountRepository,
		val name: String,
		val callback: (Account?) -> Unit
) : Command {
	
	override val reverseCommand: Command?
		get() = EmptyCommand()
	
	override fun execute() {
		val account = accountRepository.findByName(name).firstOrNull()
		callback(account)
	}
}