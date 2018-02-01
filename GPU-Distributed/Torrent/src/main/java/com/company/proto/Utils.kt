package com.company.proto

import com.company.proto.torrent.Torrent
import com.google.common.hash.Hashing
import com.google.protobuf.ByteString

import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.util.ArrayList
import java.util.Arrays
import java.util.function.Predicate
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import javax.xml.bind.DatatypeConverter


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
		if (isNullOrBlank()) throw PatternSyntaxException("Empty regex", this, 0)
		Pattern.compile(this)
		true
	} catch (exception: PatternSyntaxException) {
		false
	}
}

fun ByteArray.toMD5Hash(): ByteString {
	val md = MessageDigest.getInstance("MD5")
	val digested = md.digest(this)
	return ByteString.copyFrom(digested)
}

fun ByteString.toMD5Hash(): ByteString {
	return this.toByteArray().toMD5Hash()
}

fun ByteString.hashToReadableMD5():String{
	return DatatypeConverter.printHexBinary(this.toByteArray())
}

fun ByteString.toChunkedArray(chunkSize: Int): Iterable<Torrent.ChunkInfo> {
	val chunksInfo = mutableListOf<Torrent.ChunkInfo>()
	this.chunked(chunkSize).forEachIndexed { index, list ->
		val array = list.toByteArray()
		val chunkInfo = Torrent.ChunkInfo
				.newBuilder()
				.setIndex(index)
				.setSize(array.size)
				.setHash(array.toMD5Hash())
				.build()
		chunksInfo.add(chunkInfo)
	}
	return chunksInfo
}

fun ByteString.toChunkAt(chunkInfo: Torrent.ChunkInfo): ByteString? {
	return this.chunked(CHUNK_SIZE)
			.map { it.toByteArray() }
			.map { it.toByteString() }
			.firstOrNull { it.toMD5Hash() == chunkInfo.hash }
}

fun ByteArray.toByteString() = ByteString.copyFrom(this)