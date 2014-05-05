package com.urlShortener

import scala.util._

object BackEnd {

  trait ShortenerService {
    def shorten(longUrl:String): Try[String]
    def lengthen(shortUrl: String): Option[String]
  }

  trait KVBasedStorer {
    def getShort(longUrl:String): Option[String]
    def getLong(shortUrl:String): Option[String]
    def put(kv: (String,String)): Unit
    def getNextInt: Int
  }

  trait KVStorageComponent {
    import scala.collection.mutable.{Map => MMap}
    
    val storageComponent:KVBasedStorer
    
    class InMemoryStorage extends KVBasedStorer {
   
      private var longToShort = MMap[String,String]()
      private var shortToLong = MMap[String,String]()
      private var counter = 0
    
      def getShort(longUrl: String): Option[String] = longToShort.get(longUrl)
    
      def getLong(shortUrl: String): Option[String] = shortToLong.get(shortUrl)

      def getNextInt: Int = {
        val result = counter
        counter = counter + 1
        result
      }
    
      def put(kv: (String,String)) = {
        val longUrl = kv._1
        val shortUrl = kv._2
        longToShort += ( (longUrl -> shortUrl) )
        shortToLong += ( (shortUrl -> longUrl) )
      }
    }

    // some other class implementing the same functions using redis say  ??
  }


  trait KVBasedShortenerService { this: KVStorageComponent =>

    val shortenerService:ShortenerService

    class KVBasedService extends ShortenerService {

      private def _shorten(url: String): String = {  
        val shortPath = Base64Encoder.encode(storageComponent.getNextInt)
        storageComponent.put( (url -> shortPath) )
        shortPath
      }
    
      def shorten(longUrl: String): Try[String] = {
        if ( ! (UrlValidator.validate(longUrl)) )
          Failure(new java.net.MalformedURLException("Invalid Url"))
        else
          storageComponent.getShort(longUrl).map(Success(_)).getOrElse( Success(_shorten(longUrl) ))
      }
    
      def lengthen(shortUrl: String): Option[String] = storageComponent.getLong(shortUrl)
    }
   }
 }