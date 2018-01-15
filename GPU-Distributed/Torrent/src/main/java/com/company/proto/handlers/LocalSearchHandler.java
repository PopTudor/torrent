package com.company.proto.handlers;

import com.company.proto.torrent.Torrent;

public class LocalSearchHandler implements Handler {
	@Override
	public Torrent.Message handle(Torrent.Message message) {
		
		return message;
	}
}
