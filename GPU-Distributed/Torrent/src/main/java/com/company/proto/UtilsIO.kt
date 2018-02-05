package com.company.proto

import com.company.proto.torrent.Torrent

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ByteOrder.BIG_ENDIAN

@Throws(IOException::class)
fun DataInputStream.readMessage(): Torrent.Message {
	val lenBuf = ByteBuffer.allocate(4)
			.order(ByteOrder.BIG_ENDIAN)
			.array()
	read(lenBuf)
	val len = ByteBuffer.wrap(lenBuf).int
	val data = ByteArray(len)
	var read = 0
	while (read < len) {
		val received = read(data, read, len - read)
		read += received
	}
	return Torrent.Message.parseFrom(data)
}

@Throws(IOException::class)
fun DataOutputStream.writeMessage(response: Torrent.Message) {
	val len = ByteBuffer.allocate(4)
			.order(ByteOrder.BIG_ENDIAN)
			.putInt(response.serializedSize)
			.array()
	val data = response.toByteArray()
	
	write(len, 0, len.size)
	write(data, 0, ByteBuffer.wrap(len).int)
	flush()
}

fun loop(function: () -> Unit) {
	while (true)
		function()
}

fun Socket.getDataOutputStream() = DataOutputStream(getOutputStream())
fun Socket.getDataInputStream() = DataInputStream(getInputStream())


