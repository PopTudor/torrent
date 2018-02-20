package hello.business.command

interface Command {
	val reverseCommand: Command?
	fun execute()
}

interface CommandCallback<in R> {
	fun onSuccess(result: R)
}