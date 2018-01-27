package com.company.proto

import com.company.proto.torrent.Torrent
import com.google.common.hash.Hashing
import com.google.protobuf.ByteString

import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.ArrayList
import java.util.Arrays
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException


fun writeLittleEndian(stream: OutputStreamWriter, bytes: ByteArray) {
	val bb = ByteBuffer.wrap(bytes)
	bb.order(ByteOrder.LITTLE_ENDIAN)
	while (bb.hasRemaining()) {
		val v = bb.short
		try {
			stream.write(v.toInt())
		} catch (e: IOException) {
			e.printStackTrace()
		}
		
	}
}

fun Torrent.Message.messageLen(): Byte {
	return serializedSize.toByte()
}

fun String.isValidRegex(): Boolean {
	return try {
		if (isBlank()) throw PatternSyntaxException("Empty regex", this, 0)
		Pattern.compile(this)
		true
	} catch (exception: PatternSyntaxException) {
		false
	}
}

fun ByteArray.toMD5Hash(): ByteString {
	val hashCode = Hashing.md5().hashBytes(this)
	return ByteString.copyFrom(hashCode.asBytes())
}

fun ByteString.toMD5Hash() = this.toByteArray().toMD5Hash()


fun ByteString.toList(): Iterable<Torrent.ChunkInfo> {
	return this.toByteArray().toList()
}

fun ByteArray.toList(): Iterable<Torrent.ChunkInfo> {
	val chunkInfos = ArrayList<Torrent.ChunkInfo>()
	var i = 0
	var index = 0
	while (i < this.size) {
		val chunk = Arrays.copyOfRange(this, i, Math.min(this.size, i + Constants.CHUNK_SIZE))
		
		val chunkInfo = Torrent.ChunkInfo
				.newBuilder()
				.setIndex(index)
				.setSize(chunk.size)
				.setHash(chunk.toMD5Hash())
				.build()
		chunkInfos.add(chunkInfo)
		i += Constants.CHUNK_SIZE
		index++
	}
	return chunkInfos
}
