import hello.business.Transaction

fun Transaction.printBlocked() {
	println("$this blocked")
}

fun Transaction.printFinish() {
	println("$this finish")
}

fun Transaction.printHolding(any: Any) {
	println("$this holding $any")
}

fun Transaction.printAcquired(any: Any) {
	println("$this acquired $any")
}