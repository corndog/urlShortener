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

    private val password = "password"
    private val user = "postgres"

    
    // need to put this whole thing in a transaction.
    def _shorten(url:String): Either[java.lang.Throwable, String] = {
      Database.forURL("jdbc:postgresql:urlShortener",
                    driver = "org.postgresql.Driver",
                    user = user,
                    password = password) withSession {

        val shortened = sql"SELECT short_url FROM urls where url = $url".as[String].list.headOption
        shortened.map( u=> Right(u)).getOrElse( try {
          val rm = sqlu"INSERT INTO urls (url) VALUES ($url)".first
	  val id = sql"SELECT id FROM urls where url = $url".as[Long].list.head
	  val encodedUrl = Base64Encoder.encode(id)
	  val x = sqlu"UPDATE urls SET short_url = $encodedUrl WHERE url = $url".first
	  Right(encodedUrl)
        } catch {
          case e: java.lang.Throwable => { /*println(e, "hopefully just a constraint violation");*/ Left(e)}
        })
      }
    }
    
    def shorten(url:String): Either[java.lang.Throwable, String] = {
      if ( ! (UrlValidator.validate(url)) )
	Left(new java.net.MalformedURLException("Invalid Url"))
      else
        _shorten(url)
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