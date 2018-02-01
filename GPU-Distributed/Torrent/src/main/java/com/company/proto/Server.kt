package com.company.proto


import com.company.proto.handlers.HandlerFactory
import com.company.proto.torrent.Torrent

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket


class Server @Throws(IOException::class)
constructor(host: String, port: Int) {
	init {
		val node = Torrent.Node.newBuilder()
				.setHost(host)
				.setPort(port)
				.build()
		nodeList.add(node)
		val inetAddress = InetAddress.getByName(node.host)
		ServerSocket(node.port, 10, inetAddress).use { listener ->
			loop {
				println("Listening on: ${listener.localSocketAddress}:${listener.localPort}")
				listener.accept().use { socket ->
					// ..... open .....
					val input = socket.getDataInputStream()
					// ..... receive .....
					val message = input.readMessage()
					// ..... process .....
					val handler = HandlerFactory.create(message, node)
					val response = handler.handle(message)
					// ..... respond ......
					val output = socket.getDataOutputStream()
					output.writeMessage(response)
					
					input.close()
					output.close()
				}
			}
		}
	}
}
