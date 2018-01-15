package com.company.proto.handlers;

import com.company.proto.torrent.Torrent;

public interface Handler {
	void handle(Torrent.Message message);
}
