package com.company.proto.handlers;

import com.company.proto.torrent.Torrent;

public class HandlerFactory {
	public static Handler create(Torrent.Message message) {
		if (message.hasLocalSearchRequest())
			return new LocalSearchHandler();
		throw new RuntimeException("Invalid Message Type");
	}
}
