package hello

import hello.business.Transaction
import hello.business.command.Command
import org.springframework.stereotype.Service
import java.util.*

@Service
open class TransactionManagerCommands {
	// transaction -> list Operation
	private val commands = Collections.synchronizedMap(mutableMapOf<Transaction, MutableList<Command>>())
	// transaction -> list reverse Operation
	private var executedCommands = Collections.synchronizedMap(mutableMapOf<Transaction, MutableList<Command>>())
	
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
		executedCommands[transaction]?.reversed()?.forEach {
			println("rollback: $transaction")
			it.execute()
		}
	}
}