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


private val PORT_START = 5001
private val PORT_END = 5005

fun nodeList(): List<Torrent.Node> {
	return nodeList
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

