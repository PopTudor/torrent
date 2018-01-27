package com.company.proto

import com.company.proto.torrent.Torrent
import com.google.common.collect.Streams
import com.google.protobuf.ByteString
import io.reactivex.Observable

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.ConnectException
import java.net.Socket
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.IntStream
import java.util.stream.Stream

import com.company.proto.UtilsIO.readMessageFrom
import com.company.proto.UtilsIO.writeMessageTo

fun nodeList(): List<Torrent.Node> {
	return IntStream.rangeClosed(5001, 5005)
			.mapToObj { index ->
				Torrent.Node.newBuilder()
						.setHost("127.0.0.1")
						.setPort(index)
						.build()
			}.collect(Collectors.toList())
}

fun askOtherNodes(currentNode: Torrent.Node, action: Consumer<Torrent.Node>) {
	nodeList().stream()
			.filter { node -> node != currentNode }
			.forEach(action)
}

fun nodeObservable(currentNode: Torrent.Node): Observable<Torrent.Node> {
	return Observable.fromIterable(nodeList())
			.filter { it != currentNode }
}

