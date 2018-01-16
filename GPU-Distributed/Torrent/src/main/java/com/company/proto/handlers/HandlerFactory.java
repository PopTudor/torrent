package com.company.proto.handlers;

import com.company.proto.torrent.Torrent;
import com.google.protobuf.ByteString;

import java.util.HashMap;
import java.util.Map;

public class HandlerFactory {
	private static Map<Object, ByteString> storage = new HashMap<>();
	
	public static Handler create(Torrent.Message message) {
		if (message.hasUploadRequest())
			return new UploadRequest(storage);
		else if (message.hasReplicateRequest())
			return new ReplicateHandler();
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
		else if (message.hasChunkRequest())
			throw new RuntimeException("Invalid Message Type");
		
		throw new RuntimeException("Invalid Message Type");
	}
}
