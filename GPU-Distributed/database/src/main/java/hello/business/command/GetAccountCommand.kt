package hello.business.command

import hello.data.account.Account
import hello.data.account.AccountRepository

class GetAccountCommand(
		val accountRepository: AccountRepository,
		val name: String
) : Command {
	var resultAccount: Account? = null
	override var reverseCommand: Command? = null
		get() = EmptyCommand()
	
	override fun execute() {
		resultAccount = accountRepository.findByName(name).firstOrNull()
	}
}