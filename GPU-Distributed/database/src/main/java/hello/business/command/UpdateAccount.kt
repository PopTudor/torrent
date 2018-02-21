package hello.business.command

import hello.data.account.Account
import hello.data.account.AccountRepository

class UpdateAccount(
		val accountRepository: AccountRepository,
		val account: Account
) : Command {
	override var reverseCommand: Command? = null
		get() = UndoUpdateAccount(accountRepository, account)
	
	override fun execute() {
		accountRepository.save(account)
	}
}