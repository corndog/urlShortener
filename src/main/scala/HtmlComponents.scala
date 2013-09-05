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
    <form action="shorten" method="POST">
      <input type="text" name="url" size="80" />
      <input type="submit" value="do it now" />
    </form>
  }
  
  // should probably use Either[Int,String]
  def home(shortUrl:Option[String], errorCode:Option[String]) = {
    <h1>Enter the URL you want to shorten</h1>
    <div>{ form }</div>
    <div>
      { shortUrl.map(u => <div>{"http://127.0.0.1/" + u}</div>).getOrElse(Nil) }
      { errorCode.map(e => <div>Something went wrong sorry</div>).getOrElse(Nil) }
    </div>
  }
}