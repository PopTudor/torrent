package com.company.proto.handlers;

import com.company.proto.torrent.Torrent;
import com.google.protobuf.ByteString;

import java.util.HashMap;
import java.util.Map;

public class HandlerFactory {
	private static Map<Object, ByteString> storage = new HashMap<>();
	public static Handler create(Torrent.Message message) {
		if (message.hasLocalSearchRequest())
			return new LocalSearchHandler();
		else if (message.hasUploadRequest())
			return new UploadRequest(storage);
		throw new RuntimeException("Invalid Message Type");
	}
}
