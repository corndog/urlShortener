package com.urlShortener

object HtmlComponents {

  def layout(x: xml.NodeBuffer, title:String = "URL Shortener") = {
    <html> 
      <head>
	<title>{title}</title>
      </head>
      <body>{x}</body>
    </html> 
  }

  val form = {
    <form action="/" method="POST">
      <input type="text" name="url" size="80" />
      <input type="submit" value="do it now" />
    </form>
  }
  
  val home = {
    <h1>Enter the URL you want to shorten</h1>
    <div>{ form }</div>
  }
  
  def result(shortUrl:Either[java.lang.Throwable, String]) = {
    <div>
      { shortUrl.fold(
          e => <div>Something went wrong, sorry, maybe try again</div>,
	  s => <div>Your short url is { "http://127.0.0.1:8080/" + s}</div>
        ) 
      }
    </div>
    <div>
      <a href="/">I want to shorten another URL</a>
    </div>
  }
    
}