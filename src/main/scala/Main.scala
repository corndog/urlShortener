package com.urlShortener

import akka.actor._
import spray.routing._
import spray.http._
import spray.http.MediaTypes._
import com.urlShortener.Services._


object Main extends App with SimpleRoutingApp {
  implicit val system = ActorSystem("urlShortener")
  val host = "http://127.0.0.1:8080/"
  
  val service = ComponentRegistry.shortenerService
  
  startServer(interface = "localhost", port = 8080) {
    path("") {
      get {
        html(HtmlComponents.home)
      } ~
      post {
        formFields('url.as[String]) { url =>
	  val shortened = service.shorten(url)
	  html(HtmlComponents.result(shortened))		
	}
      }
    }  ~
    path(PathElement) { shortUrl =>
      service.lengthen(shortUrl)
        .map( u => redirect(u, StatusCodes.MovedPermanently))
        .getOrElse(html(HtmlComponents.notFound))
    }
  }
  
  def html(x: xml.NodeBuffer) = 
    respondWithMediaType(`text/html`) {
      complete { 
        HtmlComponents.layout(x) 
      }
    }
}