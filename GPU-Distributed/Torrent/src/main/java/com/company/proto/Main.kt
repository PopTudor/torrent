package com.company.proto

import io.reactivex.Observable
import java.io.IOException
import kotlin.concurrent.thread
import java.util.Arrays


@Throws(IOException::class)
fun main(args: Array<String>) {
	//		Server server1 = new Server("127.0.0.1",5002);
	//		Server server2 = new Server("127.0.0.1", 5003);
	//		Server server4 = new Server("127.0.0.1", 5004);
//	val server5 = Server("127.0.0.1", 5005)
	ipSuffixes.flatMap { ip -> portOffset.map { port -> Pair(ipPreffix + ip, basePort + port) } }
			.forEach { (host, port) ->
				println("$host  -----  $port")
				thread {
					Server(host, port)
				}
			}
	
}

fun tmp() {
	//		Server server1 = new Server("127.0.0.1",5002);
	//		Server server2 = new Server("127.0.0.1", 5003);
	//		Server server4 = new Server("127.0.0.1", 5004);
//	val server5 = Server("127.0.0.1", 5005)
	val len = ipSuffixes.size * portOffset.size
//	for (i in 0..len) {
	//			ips.flatMap { ip -> ports.map { port -> Pair(ip, port) } }
	ipSuffixes.flatMap { ip -> portOffset.map { port -> Pair(ipPreffix + ip, basePort + port) } }
			.forEach { (host, port) ->
				println("$host  -----  $port")
				thread {
					
					Server(host, port)
				}

//			val host = ipPreffix + ipSuffixes[i % ipSuffixes.size]
//			val port = basePort + portOffset[i % portOffset.size]
			
			}
//	}

}