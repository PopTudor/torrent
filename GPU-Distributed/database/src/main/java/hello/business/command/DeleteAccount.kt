package hello.business.command

import hello.data.account.Account
import hello.data.account.AccountRepository

class DeleteAccount(
		val accountRepository: AccountRepository,
		val account: Account
) : Command {
	override val reverseCommand: Command?
		get() = CreateAccount(accountRepository, account)
	
	override fun execute() {
		accountRepository.delete(account)
	}
}