package com.urlShortener

import org.apache.commons.validator._

object UrlValidator {
  
  val schemes = Array("http", "https")
  val validator = new UrlValidator(schemes)
  
  def validate(url: String): Boolean = validator.isValid(url)
}