package com.urlShortener

import scala.util._

trait Views {

  def layout(x: => Seq[xml.Node], title:String = "URL Shortener") =
    <html>
      <head>
	<title>{title}</title>
      </head>
      <body>{x}</body>
    </html>


  val form =
    <form action="/" method="POST">
      <input type="text" name="url" size="80" />
      <input type="submit" value="do it now" />
    </form>
 
  
  val home = {
    <h1>Enter the URL you want to shorten</h1>
    <div>{ form }</div>
  }
  
  def result(shortUrl: Try[String]) = {
    <div>
      { shortUrl match {
          case Failure(m) => <div>Error:</div><div>{m}</div> 
	  case Success(s) => <div>Your short url is { Main.host + s}</div>
        } 
      }
    </div>
    <div>
      <a href="/">I want to shorten another URL</a>
    </div>
  }
  
  val notFound = {
    <h1>Not Found</h1>
    <a href="/">I want to shorten another URL</a>
  }
    
}