package hello.business.command

import hello.business.Transaction
import hello.data.history.HistoryRepository

class History(val historyRepository: HistoryRepository,
			  val transaction: Transaction
) : Command {
	override var reverseCommand: Command? = EmptyCommand()
	
	override fun execute() {
		historyRepository.save(transaction)
	}
}