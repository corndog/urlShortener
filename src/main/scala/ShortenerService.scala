package com.urlShortener

import scala.slick.driver.PostgresDriver.simple._ //{Session,Database}
import scala.slick.jdbc.StaticQuery.interpolation
import Database.threadLocalSession

object Services {
  import BackEnd.ShortenerService
  
  trait SqlShortenerService extends ShortenerService {
    import scala.util._

    private val password = "password"
    private val user = "postgres"
    
    private def _shorten(url:String): Try[String] = {
      Database.forURL("jdbc:postgresql:urlShortener",
                    driver = "org.postgresql.Driver",
                    user = user,
                    password = password) withSession {

	// base64 encoding logic in a stored proc
        Try {
	  sql"insert into urls(url, short_url) values($url, (select b64((select currval(pg_get_serial_sequence('urls', 'id')))))) returning short_url".as[String].list.head
        }
      }
    }
    
    
    def shorten(url:String): Try[String] =
      if ( ! (UrlValidator.validate(url)) )
	Failure(new java.net.MalformedURLException(s"Invalid Url: $url"))
      else {
        findShort(url).map(Success(_)).getOrElse(_shorten(url))
      }
      
    
    private def findShort(url:String): Option[String] =
      Database.forURL(
        "jdbc:postgresql:urlShortener",
        driver = "org.postgresql.Driver",
        user = user,
        password = password) withSession {
        
	sql"SELECT short_url FROM urls where url = $url".as[String].list.headOption
      }

  
    def lengthen(shortUrl:String): Option[String] =
      Database.forURL("jdbc:postgresql:urlShortener",
                    driver = "org.postgresql.Driver",
                    user = user,
                    password = password) withSession {
        sql"SELECT url FROM urls where short_url = $shortUrl".as[String].list.headOption
      }
 
  }

}
