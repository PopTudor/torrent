package hello.business

import hello.business.command.Command
import org.springframework.stereotype.Service
import java.util.*

@Service
class TransactionManager {
	// transaction -> list Operation
	val commands = Collections.synchronizedMap(mutableMapOf<Transaction, MutableList<Command>>())
	// transaction -> list reverse Operation
	var executedCommands = Collections.synchronizedMap(mutableMapOf<Transaction, MutableList<Command>>())
	
	fun commit(transaction: Transaction) {
		executedCommands = mutableMapOf<Transaction, MutableList<Command>>()
		val commandList = commands[transaction] ?: emptyList<Command>()
		
		for (command in commandList) {
			println("executing: ${command.javaClass}")
			command.execute()
			val reverseCommand = command.reverseCommand
			if (reverseCommand != null) {
				executedCommands[transaction]?.add(reverseCommand)
			}
		}
	}
	
	fun addCommands(transaction: Transaction, command: Command) {
		if (commands[transaction] == null)
			commands[transaction] = mutableListOf(command)
		else
			commands[transaction]?.add(command)
	}
	
	fun rollback(transaction: Transaction) {
		executedCommands[transaction]?.reversed()?.forEach { it.execute() }
	}
}