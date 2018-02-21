package hello.business.command

class EmptyCommand : Command {
	override var reverseCommand: Command? = EmptyCommand()
	
	override fun execute() {
		println("nothing to do")
	}
}