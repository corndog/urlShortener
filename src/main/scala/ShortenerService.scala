package com.urlShortener

import scala.slick.driver.PostgresDriver.simple._ //{Session,Database}
import scala.slick.jdbc.StaticQuery.interpolation
import Database.threadLocalSession

object Services {
  import BackEnd.ShortenerService

  trait InMemoryShortenerService extends ShortenerService {
    import scala.collection.mutable.{Map => MMap}
    
    private var counter = 0
    private var longToShort = MMap[String,String]()
    private var shortToLong = MMap[String,String]()
    
    private def _shorten(url: String): String = { this.synchronized {
        longToShort.get(url)
	  .getOrElse {
            val shortPath = Base64Encoder.encode(counter)
            counter = counter + 1
            longToShort += ( (url -> shortPath) )
            shortToLong += ( (shortPath -> url) )
            shortPath
        }
      }
    }
    
    def shorten(url: String): Either[java.lang.Throwable, String] = {
      if ( ! (UrlValidator.validate(url)) )
	Left(new java.net.MalformedURLException("Invalid Url"))
      else
        Right(_shorten(url))
    }
    
    def lengthen(url: String) = shortToLong.get(url)
  }

  trait SqlShortenerService extends ShortenerService {

    private val password = "0bunyip"
    private val user = "postgres"

    
    private def _shorten(url:String): Either[java.lang.Throwable, String] = {
      Database.forURL("jdbc:postgresql:urlShortener",
                    driver = "org.postgresql.Driver",
                    user = user,
                    password = password) withSession {

        try {
          val rm = sqlu"INSERT INTO urls (url) VALUES ($url)".first
	  val id = sql"SELECT id FROM urls where url = $url".as[Long].list.head
	  val encodedUrl = Base64Encoder.encode(id)
	  val x = sqlu"UPDATE urls SET short_url = $encodedUrl WHERE url = $url".first
	  Right(encodedUrl)
        } catch {
	 // if we have a constraint violation just look it up again
	 // haven't figured out how to inspect the error in more detail to confirm
	  case c:org.postgresql.util.PSQLException => {
	    val shortUrl = sql"SELECT short_url FROM urls where url = $url".as[String].list.head
	    Right(shortUrl)
	  }
          case e:java.lang.Throwable => { println(e); Left(e) }
        }
      }
    }
    
    
    def shorten(url:String): Either[java.lang.Throwable, String] = {
      if ( ! (UrlValidator.validate(url)) )
	Left(new java.net.MalformedURLException("Invalid Url"))
      else {
        try {
          findShort(url).map(Right(_)).getOrElse(_shorten(url))
	} catch {
	  case e:java.lang.Throwable => Left(e)
	}
      }
    }
    
    private def findShort(url:String): Option[String] = {
      Database.forURL(
        "jdbc:postgresql:urlShortener",
        driver = "org.postgresql.Driver",
        user = user,
        password = password) withSession {
        
	sql"SELECT short_url FROM urls where url = $url".as[String].list.headOption
      }
    }
  
    def lengthen(shortUrl:String): Option[String] = {
      Database.forURL("jdbc:postgresql:urlShortener",
                    driver = "org.postgresql.Driver",
                    user = user,
                    password = password) withSession {
        sql"SELECT url FROM urls where short_url = $shortUrl".as[String].list.headOption
      }
    }
  }
}