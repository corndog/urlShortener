package com.urlShortener

object Base64Encoder {
  val base = 64L
  val charSet = ('0' to '9') ++ ('a' to 'z') ++ ('A' to 'Z') ++ Seq('+', '_')

  def encode(input:Long): String = {
    process(input, Seq[Char]())
  }

  def process(input:Long, base64Chars:Seq[Char]): String = {
    val quotient = input / base
    val remainder = input % base
    val acc = charSet(remainder.toInt) +: base64Chars

    if (quotient == 0) acc.mkString else process(quotient, acc) 
  }

  def decode(input:String): Long = {
    input.reverse.zipWithIndex.foldLeft(0.toDouble){ (acc, x) =>
      acc + ( charSet.indexOf(x._1) * math.pow(base, x._2) )
    }.toLong
  }
}