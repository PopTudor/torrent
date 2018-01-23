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
			return new LocalSearchHandler(storage);
		else if (message.hasSearchRequest())
			return new SearchHandler(storage,node);
		else if (message.hasDownloadRequest())
			return new DownloadHandler();
		else if (message.hasChunkRequest()) {
			return new ChunkHandler(storage);
		}
		return new EmptyHandler();
	}
}
