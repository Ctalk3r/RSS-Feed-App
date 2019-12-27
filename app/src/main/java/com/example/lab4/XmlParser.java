package com.example.lab4;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class XmlParser {
    
    public ArrayList<RssFeedModel> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return getRSSModels(parser);
        } finally {
            in.close();
        }
    }

    private ArrayList<RssFeedModel> getRSSModels(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<RssFeedModel> RSSModelList = new ArrayList<RssFeedModel>();
        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, null, "channel");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("item")) {
                RSSModelList.add(getItem(parser));
            } else {
                step(parser);
            }
        }
        return RSSModelList;
    }

    private void step(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;


        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private RssFeedModel getItem(XmlPullParser parser) throws XmlPullParserException, IOException {
        String title = null;
        String link = null;
        String description = null;
        String image = null;

        parser.require(XmlPullParser.START_TAG, null, "item");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tag = parser.getName();
            if (tag.equals("title")) {
                title = readTag(parser, "title");
            } else if (tag.equals("media:description")) {
                String tagText = readTag(parser, "media:description");
                if (tagText.contains("img")) {
                    image = getAttr(tagText, "src");
                }
                description = parseText(tagText);
            } else if (tag.equals("description")) {
                String tagText = readTag(parser, "description");
                if (tagText.contains("img")) {
                    image = getAttr(tagText, "src");
                }
                description = parseText(tagText);
            } else if (tag.equals("link")) {
                link = readTag(parser, "link");
            } else if (tag.equals("enclosure")) {
                image = readImage(parser, tag);
            } else if (tag.equals("media:thumbnail")) {
                image = readImage(parser, tag);
            } else {
                step(parser);
            }
        }
        return new RssFeedModel(title, link, description, image);
    }

    private String readTag(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, tag);
        String content = getText(parser);
        parser.require(XmlPullParser.END_TAG, null, tag);
        return content;
    }

    private String readImage(XmlPullParser parser, String tagName) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, tagName);
        String image = parser.getAttributeValue(null, "url");
        parser.next();
        return image;
    }

    private String getText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String res = "";

        if (parser.next() == XmlPullParser.TEXT) {
            res = parser.getText();
            parser.nextTag();
        }
        return res;
    }

    private String parseText(String dirtyText) {
        dirtyText = dirtyText.replace("&quot;", "");
        dirtyText = dirtyText.replace("&#39;", "");
        dirtyText = dirtyText.replace("&amp;", "");
        dirtyText = dirtyText.replace("<br", "");
        dirtyText = dirtyText.replace("<img", "");
        dirtyText = dirtyText.replace("<p>", "");
        dirtyText = dirtyText.replace("<p >", "");
        dirtyText = dirtyText.replace("</p>", "");
        dirtyText = dirtyText.replace("<a>", "");
        dirtyText = dirtyText.replace("<a", "");
        dirtyText = dirtyText.replace("</a>", "");
        dirtyText = dirtyText.replace("<tr>", "");
        dirtyText = dirtyText.replace("</tr>", "");
        dirtyText = dirtyText.replace("<td>", "");
        dirtyText = dirtyText.replace("</td>", "");
        dirtyText = removeAttribute(dirtyText, "style");
        dirtyText = removeAttribute(dirtyText, "href");
        dirtyText = removeAttribute(dirtyText, "border");
        dirtyText = removeAttribute(dirtyText, "width");
        dirtyText = removeAttribute(dirtyText, "height");
        dirtyText = removeAttribute(dirtyText, "align");
        dirtyText = removeAttribute(dirtyText, "hspace");
        dirtyText = removeAttribute(dirtyText, "clear");
        dirtyText = removeAttribute(dirtyText, "src");
        dirtyText = getAttributeContent(dirtyText, "title");
        dirtyText = getAttributeContent(dirtyText, "alt");
        dirtyText = dirtyText.replace("/>", "");
        dirtyText = dirtyText.replace(">", "");
        dirtyText = dirtyText.replace("<", "");
        dirtyText = dirtyText.replace(" . ", "");
        dirtyText = dirtyText.replace("\r.", ".");
        return dirtyText;
    }

    private String removeAttribute(String dirtyText, String attribute) {
        int pos = dirtyText.indexOf(attribute);
        while (pos != -1 && + pos + attribute.length() < dirtyText.length() && dirtyText.charAt(pos + attribute.length() + 1) == '"') {
            int endPos = dirtyText.indexOf('"', pos + attribute.length() + 2);
            if (endPos == -1) break;
            dirtyText = dirtyText.substring(0, pos) + dirtyText.substring(endPos + 1);
            pos = dirtyText.indexOf(attribute);
        }
        return dirtyText;
    }

    private String getAttributeContent(String dirtyText, String attribute) {
        int pos = dirtyText.indexOf(attribute);
        while (pos != -1 && + pos + attribute.length() < dirtyText.length() && dirtyText.charAt(pos + attribute.length() + 1) == '"') {
            int endPos = dirtyText.indexOf('"', pos + attribute.length() + 2);
            if (endPos == -1) break;
            String res = dirtyText.substring(0, pos) +
                        dirtyText.substring(pos + attribute.length() + 2, endPos) + ". " +
                        dirtyText.substring(endPos + 1);
            if (attribute.equals("alt") && res.contains("Фото:")) {
                return removeAttribute(dirtyText, attribute);
            }
            dirtyText = res;
        }
        return dirtyText;
    }

    private String getAttr(String dirtyText, String attribute) {
        int pos = dirtyText.indexOf(attribute);
        while (pos != -1 && + pos + attribute.length() < dirtyText.length() && dirtyText.charAt(pos + attribute.length() + 1) == '"') {
            int endPos = dirtyText.indexOf('"', pos + attribute.length() + 2);
            if (endPos == -1) break;
            return dirtyText.substring(pos + attribute.length() + 2, endPos);
        }
        return "";
    }
}