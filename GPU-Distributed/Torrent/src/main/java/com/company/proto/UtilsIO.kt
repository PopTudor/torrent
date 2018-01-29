package com.company.proto

import com.company.proto.torrent.Torrent

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

@Throws(IOException::class)
fun DataInputStream.readMessage(): Torrent.Message {
	val len = this.readInt()
	val data = ByteArray(len)
	val readLen = this.read(data, 0, len)
	if (readLen == -1) throw IOException("error")
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
