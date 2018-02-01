package com.company.proto

import com.company.proto.torrent.Torrent
import io.reactivex.Observable
import io.reactivex.internal.schedulers.NewThreadScheduler
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import kotlin.concurrent.thread
import java.util.Arrays


fun main(args: Array<String>) {
//	ipSuffixes.flatMap { ip -> portOffset.map { port -> Pair(ipPreffix + ip, basePort + port) } }
//			.map { (host, port) ->
//				Torrent.Node.newBuilder()
//						.setHost(host)
//						.setPort(port)
//						.build()
//			}
//			.toObservable()
//			.subscribe({
//				nodeList.add(it)
//				Server(it)
//			}, {})
	val node = Torrent.Node.newBuilder()
			.setHost("127.0.0.1")
			.setPort(5004).build()

	val server = Server(node)
}
