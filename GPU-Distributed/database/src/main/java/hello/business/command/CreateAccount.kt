package hello.business.command

import hello.data.account.Account
import hello.data.account.AccountRepository

class CreateAccount(
		val accountRepository: AccountRepository,
		val account: Account
) : Command {
	
	override val reverseCommand: Command?
		get() = DeleteAccount(accountRepository, account)
	
	override fun execute() {
		accountRepository.save(account)
	}
}