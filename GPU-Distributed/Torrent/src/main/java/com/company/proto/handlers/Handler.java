package com.company.proto.handlers;

import com.company.proto.torrent.Torrent;

public interface Handler {
	Torrent.Message handle(Torrent.Message message);
}
