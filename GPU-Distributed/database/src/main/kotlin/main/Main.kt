package main

import com.google.gson.GsonBuilder
import hello.data.Deposit
import hello.data.DepositStatus
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import kotlin.concurrent.thread

interface API {
	@POST("deposit")
	fun deposit(@Body deposit: Deposit): Observable<DepositStatus>
}

fun main(args: Array<String>) {
	val gson = GsonBuilder()
			.setLenient()
			.create()
	val retrofit = Retrofit.Builder()
			.baseUrl("http://[::1]:8080/")
			.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
			.addConverterFactory(GsonConverterFactory.create(gson))
			.client(getClient())
			.build()
	val api = retrofit.create(API::class.java)
	val first = thread(start = false) {
		api.deposit(Deposit(10.0, "tudor"))
				.subscribe({ println(it) }, { println(it) })
	}
	val second = thread(start = false) {
		api.deposit(Deposit(13.0, "tudor"))
				.subscribe({ println(it) }, { println(it) })
	}

	first.start()
	second.start()

	first.join()
	second.join()
}

fun getClient(): OkHttpClient {
	val interceptor = HttpLoggingInterceptor()
	interceptor.level = HttpLoggingInterceptor.Level.BODY
	return OkHttpClient.Builder()
			.addNetworkInterceptor(interceptor)
			.build()
}
