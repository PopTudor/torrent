package hello.business.command

import hello.business.Transaction
import hello.data.history.History
import hello.data.history.HistoryRepository

class CreateHistoryCommand(val historyRepository: HistoryRepository,
						   val transaction: Transaction,
						   val changedRes: Any
) : Command {
	override var reverseCommand: Command? = null
	
	override fun execute() {
		historyRepository.save(History(transaction))
	}
}