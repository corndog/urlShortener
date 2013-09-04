package com.urlShortener

import akka.actor._
import spray.routing._
import spray.http._
import spray.http.MediaTypes._

object Main extends App with SimpleRoutingApp {
  implicit val system = ActorSystem("urlShortener")
  
  val form = {
    <form action="shorten" method="POST">
      <input type="text" name="url" size="80" />
      <input type="submit" value="do it now" />
    </form>
  }

  startServer(interface = "localhost", port = 8080) {
    path("") {
      get {
        html(
          <h1>Enter the URL you want to shorten</h1>
          <div>{ form }</div>
        )
      }
    } ~
    path("shorten") {
      post {
        formFields('url.as[String]) { url =>
          println("POSTED url " + url)
	  redirect("/", StatusCodes.MovedPermanently)			
	}
      }
    }
  }
  
  def html(x: xml.NodeBuffer) = 
    respondWithMediaType(`text/html`) {
      complete { 
        <html> 
          <head>
          </head>
          <body>{x}</body>
        </html> 
      }
    }
  
}