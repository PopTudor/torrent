import hello.data.account.Account
import hello.data.account.AccountRepository

class AccountRepositoryMock : AccountRepository {
	private val table = mutableListOf<Account?>()
	
	override fun findByName(name: String?): MutableList<Account> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	
	override fun <S : Account?> save(entity: S): S {
		table += entity
		return entity
	}
	
	fun size() = table.size
	override fun <S : Account?> save(entities: MutableIterable<S>?): MutableIterable<S> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	override fun findOne(id: Long?): Account? {
		return table.firstOrNull { it?.id == id }
	}
	
	override fun findAll(): MutableIterable<Account> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	override fun findAll(ids: MutableIterable<Long>?): MutableIterable<Account> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	override fun count(): Long {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	override fun exists(id: Long?): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	override fun deleteAll() {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	override fun delete(id: Long?) {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	override fun delete(entity: Account?) {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	override fun delete(entities: MutableIterable<Account>?) {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
}