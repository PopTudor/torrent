package com.company.proto.handlers

import com.company.proto.torrent.Torrent

class EmptyHandler : Handler {
	override fun handle(message: Torrent.Message): Torrent.Message {
		return Torrent.Message.newBuilder().build()
	}
}
