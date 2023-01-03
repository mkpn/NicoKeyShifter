package com.nicokeyshifter

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class NicoNicoRssXmlParser {

    // We don't use namespaces
    val ns: String? = null

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): List<*> {
        inputStream.use {
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(it, null)
            parser.nextTag()
            return readRss(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readRss(parser: XmlPullParser): List<Item> {
        parser.require(XmlPullParser.START_TAG, ns, "rss")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            // Starts by looking for the entry tag
            if (parser.name == "channel") {
                return readChannel(parser)
            } else {
                skip(parser)
            }
        }
        return emptyList()
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readChannel(parser: XmlPullParser): List<Item> {
        val entries = mutableListOf<Item>()

        parser.require(XmlPullParser.START_TAG, ns, "channel")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            // Starts by looking for the entry tag
            if (parser.name == "item") {
                entries.add(readItem(parser))
            } else {
                skip(parser)
            }
        }
        return entries
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
    @Throws(XmlPullParserException::class, IOException::class)
    fun readItem(parser: XmlPullParser): Item {
        parser.require(XmlPullParser.START_TAG, ns, "item")
        var title = ""
        var descriptionHtmlString = ""
        var link = ""
        var pubDate = ""
        var contentLength = ""
        var myListCount = ""
        var likeCount = ""
        var thumbnailImageUrl = ""
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "title" -> title = readTitle(parser)
                "description" -> {
                    descriptionHtmlString = readDescription(parser)
                    val html = (descriptionHtmlString)
                    val doc = Jsoup.parse(html)
                    thumbnailImageUrl = getThumbnailImageUrl(doc)
                    likeCount = getLikeCount(doc)
                    myListCount = getMyListCount(doc)
                    contentLength = getContentLength(doc)
                    pubDate = getPubDate(doc)
                }
                "link" -> link = readLink(parser)
                else -> skip(parser)
            }
        }
        val item =
            Item(title, link, pubDate, contentLength, myListCount, likeCount, thumbnailImageUrl)
        return item
    }

    // Processes title tags in the feed.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTitle(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "title")
        val title = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "title")
        return title
    }

    // Processes link tags in the feed.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readLink(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "link")
        val link = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "link")
        return link
    }

    // Processes summary tags in the feed.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readDescription(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "description")
        val description = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "description")
        return description
    }

    // For the tags title and summary, extracts their text values.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    private fun getThumbnailImageUrl(doc: Document) = doc.select("img").attr("src")
    private fun getLikeCount(doc: Document) = doc.getElementsByClass("nico-info-total-like").text()
    private fun getMyListCount(doc: Document) = doc.getElementsByClass("nico-info-total-mylist").text()
    private fun getContentLength(doc: Document) = doc.getElementsByClass("nico-info-length").text()
    private fun getPubDate(doc: Document) = doc.getElementsByClass("nico-info-date").text()

    @Throws(XmlPullParserException::class, IOException::class)
    fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}