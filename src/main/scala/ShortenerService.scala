package com.urlShortener

import scala.slick.driver.PostgresDriver.simple._ //{Session,Database}
import scala.slick.jdbc.StaticQuery.interpolation
import Database.threadLocalSession

object Services {
  import BackEnd.ShortenerService
  
  trait SqlShortenerService extends ShortenerService {

    private val password = "password"
    private val user = "postgres"
    
    private def _shorten(url:String): Either[java.lang.Throwable, String] = {
      Database.forURL("jdbc:postgresql:urlShortener",
                    driver = "org.postgresql.Driver",
                    user = user,
                    password = password) withSession {

	// base64 encoding logic in a stored proc
        try {
	  val encodedUrl:String = sql"insert into urls(url, short_url) values($url, (select b64((select currval(pg_get_serial_sequence('urls', 'id')))))) returning short_url".as[String].list.head
	  Right(encodedUrl)
        } catch {
          case e:java.lang.Throwable => Left(e)
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
