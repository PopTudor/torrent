package main

import hello.data.DepositStatus
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import kotlin.concurrent.thread

interface API {
	@GET("deposit")
	fun deposit(@Query("deposit") amount: Int, @Query("user") user: String): Observable<DepositStatus>
}

fun main(args: Array<String>) {
	val retrofit = Retrofit.Builder()
			.baseUrl("http://[::1]:8080/")
			.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
			.addConverterFactory(GsonConverterFactory.create())
			.client(getClient())
			.build()
	val api = retrofit.create(API::class.java)
	val first = thread(start = false) {
		api.deposit(10, "tudor")
				.subscribe({ print(it) }, { print(it) })
	}
	val second = thread(start = false) {
		api.deposit(13, "tudor")
				.subscribe({ print(it) }, { print(it) })
	}

//	first.start()
	second.start()
	second.join()
}

fun getClient(): OkHttpClient? {
	val interceptor = HttpLoggingInterceptor()
	interceptor.level = HttpLoggingInterceptor.Level.BODY
	return OkHttpClient.Builder()
			.addNetworkInterceptor(interceptor)
			.build()
}
