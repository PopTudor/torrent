package com.company.proto

import com.company.proto.torrent.Torrent


val ipPreffix: String = "127.0.0."
val ipSuffixes: List<Int> = listOf(1)

val basePort: Int = 5000
val portOffset = listOf(4, 5)

val nodeList = mutableListOf<Torrent.Node>()
