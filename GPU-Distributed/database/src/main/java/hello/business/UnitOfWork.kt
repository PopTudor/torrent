package hello.business

interface UnitOfWork<T> {
	fun commit()
	fun rollback()
	
}