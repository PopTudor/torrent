package hello.business.command

import hello.data.account.Account
import hello.data.account.AccountRepository

class GetAccount(
		val accountRepository: AccountRepository,
		val name: String,
		val callback: (Account?) -> Unit
) : Command {
	
	override var reverseCommand: Command? = EmptyCommand()
	
	override fun execute() {
		val account = accountRepository.findByName(name).firstOrNull()
		callback(account)
	}
}