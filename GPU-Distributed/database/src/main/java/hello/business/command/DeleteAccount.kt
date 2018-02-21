package hello.business.command

import hello.data.account.Account
import hello.data.account.AccountRepository

class DeleteAccount(
		val accountRepository: AccountRepository,
		val account: Account
) : Command {
	override var reverseCommand: Command? = null
		get() = CreateAccount(accountRepository, account)
	
	override fun execute() {
		accountRepository.delete(account)
	}
}