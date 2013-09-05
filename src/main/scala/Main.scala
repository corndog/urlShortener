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
        html(HtmlComponents.home)
      } ~
      post {
        formFields('url.as[String]) { url =>
          println("POSTED url " + url)
	  
	  val htmlResponse = 
	    ShortenerService.findShortUrl(url).map{ u =>
	      HtmlComponents.result(Right(u))
	    }.getOrElse(HtmlComponents.result(ShortenerService.shorten(url)))
	  html(htmlResponse)		
	}
      }
    }  ~
    // extract URI path element as Int
    // NOT RIGHT YET
      path("" / Rest) { shortUrl =>
        val redirectTo = ShortenerService.lengthen(shortUrl).getOrElse("/") // add message somewhere if not found
	redirect(redirectTo, StatusCodes.MovedPermanently)
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