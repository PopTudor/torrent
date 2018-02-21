package hello.business.command

interface Command {
	var reverseCommand: Command?
	fun execute()
}
