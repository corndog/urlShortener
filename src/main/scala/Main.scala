package com.urlShortener

import akka.actor._
import spray.routing._
import spray.http._
import spray.http.MediaTypes._
import com.urlShortener.Services._

// can either just grab one of the services directly by
// using with WhateverService from ShortenerServices in Main
// and just call shorten/lengthen directly
// OR
// use the service from the component registry, which follows
// the cake pattern example from Jonas Boner's blog post.

object Main extends App with SimpleRoutingApp with SqlShortenerService with Views {
  implicit val system = ActorSystem("urlShortener")
  val host = "http://127.0.0.1:8080/"
  
  // use this instead of say SqlShortenerService
  // val service = ComponentRegistry.shortenerService 
  
  startServer(interface = "localhost", port = 8080) {
    path("") {
      get {
        html(home)
      } ~
      post {
        formFields('url.as[String]) { url =>
	  val shortened = /*service.*/ shorten(url)
	  html(result(shortened))		
	}
      }
    }  ~
    (path(Segment) & get) { shortUrl =>
      /*service.*/ lengthen(shortUrl)
        .map( u => redirect(u, StatusCodes.MovedPermanently))
        .getOrElse(html(notFound))
    }
  }
  
  def html(x: => Seq[xml.Node]) = 
    respondWithMediaType(`text/html`) {
      complete { 
        "<!DOCTYPE html>" ++ layout(x).mkString 
      }
    }
}