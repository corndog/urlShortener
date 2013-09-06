package com.urlShortener

import akka.actor._
import spray.routing._
import spray.http._
import spray.http.MediaTypes._
import java.net.URLEncoder

object Main extends App with SimpleRoutingApp {
  implicit val system = ActorSystem("urlShortener")
  val NotFound = "not-found"
  val host = "http://127.0.0.1:8080/"
  
  startServer(interface = "localhost", port = 8080) {
    path(NotFound) {
      get {
        html(HtmlComponents.notFound)
      }
    } ~
    path("") {
      get {
        html(HtmlComponents.home)
      } ~
      post {
        formFields('url.as[String]) { url =>
	  
	  val htmlResponse = 
	    ShortenerService.findShortUrl(url).map{ u =>
	      HtmlComponents.result(Right(u))
	    }.getOrElse(HtmlComponents.result(ShortenerService.shorten(url)))
	  html(htmlResponse)		
	}
      }
    }  ~
    path(PathElement) { shortUrl =>
      val redirectTo = ShortenerService.lengthen(shortUrl).getOrElse(host + NotFound)
      val statusCode = if (redirectTo.contains(NotFound)) StatusCodes.Found else StatusCodes.MovedPermanently
      redirect(redirectTo, statusCode)
    }
  }
  
  def html(x: xml.NodeBuffer) = 
    respondWithMediaType(`text/html`) {
      complete { 
        HtmlComponents.layout(x) 
      }
    }
}