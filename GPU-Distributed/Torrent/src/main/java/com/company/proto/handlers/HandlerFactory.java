package com.company.proto.handlers;

import com.company.proto.torrent.Torrent;
import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandlerFactory {
	private static Map<String, ByteString> storage = new HashMap<>();
	private static List<Duplicate> duplicates = new ArrayList<>();
	
	public static Handler create(Torrent.Message message, Torrent.Node node) {
		if (message.hasUploadRequest())
			return new UploadRequest(storage);
		else if (message.hasReplicateRequest())
			return new ReplicateHandler(storage, duplicates, node);
		else if (message.hasLocalSearchRequest())
			throw new RuntimeException("Invalid Message Type");
		else if (message.hasLocalSearchRequest())
			return new LocalSearchHandler();
		else if (message.hasSearchRequest())
			throw new RuntimeException("Invalid Message Type");
		else if (message.hasSearchRequest())
			throw new RuntimeException("Invalid Message Type");
		else if (message.hasDownloadRequest())
			throw new RuntimeException("Invalid Message Type");
		else if (message.hasChunkRequest()) {
			return new ChunkHandler(storage);
		}
		
		throw new RuntimeException("Invalid Message Type");
	}
}
