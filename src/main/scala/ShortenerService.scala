package com.urlShortener

import scala.slick.driver.PostgresDriver.simple._ //{Session,Database}
import scala.slick.jdbc.StaticQuery.interpolation
import Database.threadLocalSession

object ShortenerService {

  val password = "password"
  val user = "postgres"

  def shorten(url:String): Either[java.lang.Throwable, String] = {
    Database.forURL("jdbc:postgresql:urlShortener",
                    driver = "org.postgresql.Driver",
                    user = user,
                    password = password) withSession {

      if ( ! (UrlValidator.validate(url)) )
	Left(new java.net.MalformedURLException("Invalid Url"))
      else {
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