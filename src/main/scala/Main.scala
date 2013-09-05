package com.urlShortener

import akka.actor._
import spray.routing._
import spray.http._
import spray.http.MediaTypes._
import java.net.URLEncoder

object Main extends App with SimpleRoutingApp {
  implicit val system = ActorSystem("urlShortener")
  
  import ErrorCodes._
  
  startServer(interface = "localhost", port = 8080) {
    path("") {
      get {
        parameters('url ?, 'code ?) { (url, errorCode) =>
	  val shortUrl = url.flatMap(u => ShortenerService.findShortUrl(u))
	  println("URL ??? " + url.getOrElse("no url") + " : " + shortUrl.getOrElse("nope"))
	  html(HtmlComponents.home(shortUrl, errorCode))
	}
      }
    } ~
    path("shorten") {
      post {
        formFields('url.as[String]) { url =>
          println("POSTED url " + url)
	  // try and find it first I think, at least save the failed inserts
	  val errorCode = 
	    if (UrlValidator.validate(url)) ShortenerService.shorten(url) else invalidUrl
	  val redirectPath = 
	    if (errorCode == noError) "/?url=" + URLEncoder.encode(url, "UTF-8") else "/?code=" + errorCode
	  redirect(redirectPath, StatusCodes.MovedPermanently)			
	}
      }
    }
  }
  
  def html(x: xml.NodeBuffer) = 
    respondWithMediaType(`text/html`) {
      complete { 
        HtmlComponents.layout(x) 
      }
    }
}

// make an ENUM
object ErrorCodes {
  val noError = 0
  val invalidUrl = 1
  val otherError = 2
}