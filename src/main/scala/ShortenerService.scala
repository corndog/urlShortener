package com.urlShortener

import scala.slick.driver.PostgresDriver.simple._ //{Session,Database}
import scala.slick.jdbc.StaticQuery.interpolation
import Database.threadLocalSession

object ShortenerService {
  import ErrorCodes._

  val password = "password"
  val user = "postgres"

  def shorten(url:String): Int = {
    Database.forURL("jdbc:postgresql:urlShortener",
                    driver = "org.postgresql.Driver",
                    user = user,
                    password = password) withSession {
     
      try {
        val rm = sqlu"INSERT INTO urls (url) VALUES ($url)".first
	val id = sql"SELECT id FROM urls where url = $url".as[Long].list.head
	val encodedUrl = Base64Encoder.encode(id)
	val x = sqlu"UPDATE urls SET short_url = $encodedUrl WHERE url = $url".first
      } catch {
        case e: java.lang.Throwable => println(e, "hopefully just a constraint violation")
      }
      noError
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
  
  def findShortUrl(url:String): Option[String] = {
    Database.forURL("jdbc:postgresql:urlShortener",
                    driver = "org.postgresql.Driver",
                    user = user,
                    password = password) withSession {
      sql"SELECT short_url FROM urls where url = $url".as[String].list.headOption
    }
  }
}