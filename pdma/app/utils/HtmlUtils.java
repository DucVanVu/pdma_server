package org.pepfar.pdma.app.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.safety.Whitelist;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

@Service
public class HtmlUtils implements InitializingBean
{

	@Override
	public void afterPropertiesSet() throws Exception {
	}

	public String removeXSSThreatsAlt(String input) {

		if (CommonUtils.isEmpty(input)) {
			return StringUtils.EMPTY;
		}

		return Jsoup.clean(input, Whitelist.basic());
	}

	/**
	 * Convert cell values 2 HTML entities
	 * 
	 * CR -> BR
	 * 
	 * @param value
	 * @return
	 */
	public static String cellValue2Html(String value) {

		if (CommonUtils.isEmpty(value)) {
			return StringUtils.EMPTY;
		}

		// value = value.replaceAll("\r\n", "<br/>");
		// value = value.replaceAll("\n", "<br/>");
		// value = value.replaceAll("\r", "<br/>");

		List<String> processedParas = new ArrayList<>();
		String[] paragraphs = value.split("\n");
		boolean lastParaWasBlank = false;
		boolean inListTag = false;

		for (String p : paragraphs) {
			p = p.trim();

			if (p.isEmpty()) {
				if (!lastParaWasBlank) {

					if (inListTag) {
						inListTag = false;
						processedParas.add("</ul>");
					}

					processedParas.add("<br/>");

					lastParaWasBlank = true;
				}
			} else {
				lastParaWasBlank = false;

				if (p.startsWith("-")) {
					if (!inListTag) {
						inListTag = true;
						processedParas.add("<ul>");
					}

					processedParas.add("<li>" + p.substring(1, p.length()) + "</li>");
				} else {
					if (inListTag) {
						inListTag = false;
						processedParas.add("</ul>");
					}

					processedParas.add("<p>" + p + "</p>");
				}
			}
		}

		if (inListTag) {
			processedParas.add("</ul>");
		}

		String ret = "";

		for (String s : processedParas) {
			ret = ret + s;
		}

		return ret;
	}

	/**
	 * Convert HTML entities 2 cell value
	 * 
	 * /p, /div, br/, /dd, /dt, /li -> CR
	 * 
	 * @param value
	 * @return
	 */
	public static String html2CellValue(String html) {

		if (CommonUtils.isEmpty(html)) {
			return StringUtils.EMPTY;
		}

		// create Jsoup document from HTML
		Document jsoupDoc = Jsoup.parse(html);

		// set pretty print to false, so \n is not removed
		jsoupDoc.outputSettings(new OutputSettings().indentAmount(0).outline(true).prettyPrint(false));

		// select all <br> tags and append \n after that
		jsoupDoc.select("br").after("\\n");

		// select all <p> tags and prepend \n before that
		jsoupDoc.select("p").before("\\n");

		// get the HTML from the document, and retaining original new lines
		String str = jsoupDoc.html().replaceAll("\\\\n", "\n");

		return Jsoup.clean(str, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
	}

	/**
	 * Start a HTML tag for a tag name
	 * 
	 * @param tagName
	 * @return
	 */
	public static String openTag(String tagName) {
		if (CommonUtils.isEmpty(tagName)) {
			return "";
		}

		return "<" + tagName + ">";
	}

	/**
	 * End a HTML tag for tag name
	 * 
	 * @param tagName
	 * @return
	 */
	public static String closeTag(String tagName) {
		if (CommonUtils.isEmpty(tagName)) {
			return "";
		}

		return "</" + tagName + ">";
	}
}
