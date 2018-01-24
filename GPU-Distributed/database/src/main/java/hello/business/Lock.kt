package hello.business

data class Lock(val id: Int, val type: LockType, val record: Any, val transactionId: Int)

enum class LockType { READ, WRITE }