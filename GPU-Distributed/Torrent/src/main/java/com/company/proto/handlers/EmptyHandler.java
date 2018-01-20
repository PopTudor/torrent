package com.company.proto.handlers;

import com.company.proto.torrent.Torrent;

public class EmptyHandler implements Handler {
	@Override
	public Torrent.Message handle(Torrent.Message message) {
		return Torrent.Message.newBuilder().build();
	}
}
