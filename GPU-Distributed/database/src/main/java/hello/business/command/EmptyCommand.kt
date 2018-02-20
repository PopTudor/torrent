package hello.business.command

class EmptyCommand : Command {
	override val reverseCommand: Command?
		get() = EmptyCommand()
	
	override fun execute() {
		println("nothing to do")
	}
}