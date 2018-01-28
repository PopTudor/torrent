package com.company.proto


import com.company.proto.handlers.Handler
import com.company.proto.handlers.HandlerFactory
import com.company.proto.torrent.Torrent

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

import com.company.proto.UtilsIO.readMessageFrom
import com.company.proto.UtilsIO.writeMessageTo


class Server @Throws(IOException::class)
constructor(host: String, port: Int) {
	init {
		val node = Torrent.Node.newBuilder()
				.setHost(host)
				.setPort(port)
				.build()
		val inetAddress = InetAddress.getByName(node.host)
		ServerSocket(node.port, 10, inetAddress).use { listener ->
			while (true) {
				println("Listening on: ${listener.localSocketAddress}:${listener.localPort}")
				listener.accept().use { socket ->
					// ..... open .....
					val input = DataInputStream(socket.getInputStream())
					// ..... receive .....
					val message = readMessageFrom(input)
					// ..... process .....
					val handler = HandlerFactory.create(message, node)
					val response = handler.handle(message)
					// ..... respond ......
					val output = DataOutputStream(socket.getOutputStream())
					writeMessageTo(response, output)
					
					input.close()
					output.close()
				}
			}
		}
	}
}
