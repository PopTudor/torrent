package com.company.proto.handlers

import com.company.proto.torrent.Torrent

interface Handler {
	fun handle(message: Torrent.Message): Torrent.Message
}
