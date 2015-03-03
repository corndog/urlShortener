package com.urlShortener

import scala.util._
import scala.slick.driver.PostgresDriver.simple._ //{Session,Database}
import scala.slick.jdbc.StaticQuery.interpolation
import Database.threadLocalSession

object Services {
  import BackEnd.ShortenerService
  
  trait SqlShortenerService extends ShortenerService {
    
    val db =  Database.forURL("jdbc:postgresql:urlShortener",
                    driver = "org.postgresql.Driver",
                    user = "postgres",
                    password = "password")
    
    private def _shorten(url:String): Try[String] = {
      // base64 encoding logic in a stored proc
      val query = sql"insert into urls(url, short_url) values($url, (select b64((select currval(pg_get_serial_sequence('urls', 'id')))))) returning short_url"
      db.withSession {  
        Try {
          query.as[String].list.head
        }
      }
    }
    
    
    def shorten(url:String): Try[String] =
      if ( ! (UrlValidator.validate(url)) )
        Failure(new java.net.MalformedURLException(s"Invalid Url: $url"))
      else
        findShort(url).map(Success(_)).getOrElse(_shorten(url))
      
    
    private def findShort(url:String): Option[String] = {
      val query = sql"SELECT short_url FROM urls where url = $url"
      db.withSession {
        query.as[String].list.headOption
      }
    }

  
    def lengthen(shortUrl:String): Option[String] = {
      val query = sql"SELECT url FROM urls where short_url = $shortUrl"
      db.withSession {
        query.as[String].list.headOption
      }
    }
  }
}
