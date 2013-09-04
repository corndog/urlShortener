package com.urlShortener

import scala.slick.driver.PostgresDriver.simple._ //{Session,Database}
import scala.slick.jdbc.StaticQuery.interpolation
import Database.threadLocalSession

object ShortenerService {

  def shorten(url:String): Int = {
    1
  }
  
  def lengthen(url:String): String = {
    "cats"
  }
}