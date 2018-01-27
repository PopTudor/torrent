package com.company.proto.handlers

import com.company.proto.torrent.Torrent
import com.google.protobuf.ByteString

import java.util.ArrayList
import java.util.HashMap

object HandlerFactory {
	private val storage = HashMap<Torrent.FileInfo, ByteString>()
	private val duplicates = ArrayList<Duplicate>()
	
	fun create(message: Torrent.Message, node: Torrent.Node) = when {
		message.hasUploadRequest() -> UploadRequest(storage)
		message.hasReplicateRequest() -> ReplicateHandler(storage, duplicates, node)
		message.hasLocalSearchRequest() -> LocalSearchHandler(storage)
		message.hasSearchRequest() -> SearchHandler(storage, node)
		message.hasDownloadRequest() -> DownloadHandler(storage)
		message.hasChunkRequest() -> ChunkHandler(storage)
		else -> EmptyHandler()
	}
}
