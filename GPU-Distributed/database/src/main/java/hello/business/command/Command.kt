package hello.business.command

interface Command {
	val reverseCommand: Command?
	fun execute()
	
}