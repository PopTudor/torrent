package com.company.proto

import com.company.proto.torrent.Torrent

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.nio.ByteBuffer

@Throws(IOException::class)
fun DataInputStream.readMessage(): Torrent.Message {
	val len = this.readInt()
	val data = ByteArray(len)
	var read = 0
	while (read < len) {
		read += read(data, read, len - read)
	}
	return Torrent.Message.parseFrom(data)
}

@Throws(IOException::class)
fun DataOutputStream.writeMessage(response: Torrent.Message) {
	val len = response.serializedSize
	val data = response.toByteArray()
	this.writeInt(len)
	this.write(data, 0, len)
	this.flush()
}

fun loop(function: () -> Unit) {
	while (true)
		function()
}

fun Socket.getDataOutputStream() = DataOutputStream(getOutputStream())
fun Socket.getDataInputStream() = DataInputStream(getInputStream())


