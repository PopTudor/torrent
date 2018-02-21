package hello.business.command

import hello.data.account.Account
import hello.data.account.AccountRepository

class UndoUpdateAccount(
		val accountRepository: AccountRepository,
		val account: Account
) : Command {
	override var reverseCommand: Command? = UpdateAccount(accountRepository, account)
	
	override fun execute() {
		accountRepository.save(account)
	}
}