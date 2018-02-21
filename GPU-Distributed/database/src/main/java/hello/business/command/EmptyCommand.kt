package hello.business.command

class EmptyCommand : Command {
	override var reverseCommand: Command? = null
		get() = EmptyCommand()
	
	override fun execute() {
		println("nothing to do")
	}
}