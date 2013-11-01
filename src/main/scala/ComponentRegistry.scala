package com.urlShortener

import BackEnd._

// put the cake together
object ComponentRegistry extends KVBasedShortenerService with KVStorageComponent {
  val storageComponent = new InMemoryStorage()
  val shortenerService = new KVBasedService()
}