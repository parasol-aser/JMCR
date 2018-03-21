// HttpFactory.java
// $Id: HttpFactory.java,v 1.1 2010/06/15 12:19:45 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

import org.w3c.www.mime.MimeType;

/**
 * Use this class to create pre-defined header values of various kind.
 */

public class HttpFactory {

    /**
     * Create an <code>Accept</code> header clause.
     * @param type The MIME type that you will accept.
     * @param quality The quality you are willing to set to that MIME type.
     * @return An instance of <strong>HttpAccept</strong>.
     * @see HttpAccept
     */

    public static HttpAccept makeAccept(MimeType type, double quality) {
	return new HttpAccept(true, type, quality);
    }

    /**
     * Create an <code>Accept</code> header clause.
     * This will be assigned the default <strong>1.0</strong> quality.
     * @param type The MIME type that you will accept.
     * @return An instance of <strong>HttpAccept</strong>.
     * @see HttpAccept
     */

    public static HttpAccept makeAccept(MimeType type) {
	return new HttpAccept(true, type, (double) 1.0);
    }

    /**
     * Build an accept object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpAccept instance.
     */

    public static HttpAccept parseAccept(String strval) {
	HttpAccept a = new HttpAccept();
	a.setString(strval);
	return a;
    }

    /**
     * Build a list of accept clause, ready for the <code>Accept</code> header.
     * @param accepts The various accept clauses, as build through calls to
     * <code>makeAccept</code> and gathered in an array, or <strong>null
     * </strong> to create an empty list.
     * @return An instance of <strojng>HttpAcceptList</code>.
     * @see HttpAcceptList
     */

    public static HttpAcceptList makeAcceptList(HttpAccept accepts[]) {
	if ( accepts == null )
	    return new HttpAcceptList(null);
	else
	    return new HttpAcceptList(accepts);
    }

    /**
     * Build an accept list object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpAcceptList instance.
     */

    public static HttpAcceptList parseAcceptList(String strval) {
	HttpAcceptList a = new HttpAcceptList();
	a.setString(strval);
	return a;
    }

    /**
     * Build an <code>Accept-Charset</code> header clause.
     * @param charset The accepted charset.
     * @param quality The quality under which this charset is accepted.
     * @return An instance of HttpAcceptCharset.
     * @see HttpAcceptCharset
     */

    public static HttpAcceptCharset makeAcceptCharset(String charset
						      , double quality) {
	return new HttpAcceptCharset(true, charset, quality);
    }

    /**
     * Build an <code>Accept-Charset</code> header clause.
     * Uses the default <strong>1.0</strong> quality.
     * @param charset The accepted charset.
     * @return An instance of HttpAcceptCharset.
     * @see HttpAcceptCharset
     */

    public static HttpAcceptCharset makeAcceptCharset(String charset) {
	return new HttpAcceptCharset(true, charset, (double) 1.0);
    }

    /**
     * Build an accept charset object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpAcceptCharset instance.
     */

    public static HttpAcceptCharset parseAcceptCharset(String strval) {
	HttpAcceptCharset a = new HttpAcceptCharset();
	a.setString(strval);
	return a;
    }

    /**
     * Build a list of accepted charset for the <code>Accept-Charset</code>
     * header.
     * @param charsets A list of accepted charsets, encoded as an array
     * or <strong>null</strong> to create an empty list.
     * @return An instance of HttpAcceptCharsetList.
     * @see HttpAcceptCharsetList
     */

    public static
    HttpAcceptCharsetList makeAcceptCharsetList(HttpAcceptCharset charsets[]) {
	return new HttpAcceptCharsetList(charsets);
    }

    /**
     * Build an accept charset list object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpAcceptCharsetList instance.
     */

    public static HttpAcceptCharsetList parseAcceptCharsetList(String strval) {
	HttpAcceptCharsetList a = new HttpAcceptCharsetList();
	a.setString(strval);
	return a;
    }

    /**
     * Build an <code>Accept-Encoding</code> header clause.
     * @param enc The accepted encoding.
     * @param quality The quality at which this encoding is accepted.
     * @return An instance of HttpAcceptEncoding.
     * @see HttpAcceptEncoding
     */

    public static HttpAcceptEncoding makeAcceptEncoding(String enc
							, double quality) {
	return new HttpAcceptEncoding(true, enc, quality);
    }

    /**
     * Build an <code>Accept-Encoding</code> header clause.
     * Uses the default <strong>1.0</strong> quality.
     * @param enc The accepted encoding.
     * @return An instance of HttpAcceptEncoding.
     * @see HttpAcceptEncoding
     */

    public static HttpAcceptEncoding makeAcceptEncoding(String enc) {
	return new HttpAcceptEncoding(true, enc, (double) 1.0);
    }

    /**
     * Build an accept encoding object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpAcceptLanguage instance.
     */

    public static HttpAcceptEncoding parseAcceptEncoding(String strval) {
	HttpAcceptEncoding e = new HttpAcceptEncoding();
	e.setString(strval);
	return e;
    }

    /**
     * Build a list of accept encoding clauses for the <code>Accept-Encoding
     * </code> header.
     * @param langs A list of accepted encodings, encoded as an array, or
     * <strong>null</strong> to create an empty list.
     */

    public static 
    HttpAcceptEncodingList makeAcceptEncodingList(HttpAcceptEncoding encs[]) {
	return new HttpAcceptEncodingList(encs);
    }

    /**
     * Build an accept encoding list object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpAcceptENcodingList instance.
     */

    public static 
    HttpAcceptEncodingList parseAcceptEncodingList(String strval) {
	HttpAcceptEncodingList a = new HttpAcceptEncodingList();
	a.setString(strval);
	return a;
    }

    /**
     * Build an <code>Accept-Language</code> header clause.
     * @param lang The accepted language.
     * @param quality The quality at which this language is accepted.
     * @return An instance of HttpAcceptLanguage.
     * @see HttpAcceptLanguage
     */

    public static HttpAcceptLanguage makeAcceptLanguage(String lang
							, double quality) {
	return new HttpAcceptLanguage(true, lang, quality);
    }

    /**
     * Build an <code>Accept-Language</code> header clause.
     * Uses the default <strong>1.0</strong> quality.
     * @param lang The accepted language.
     * @return An instance of HttpAcceptLanguage.
     * @see HttpAcceptLanguage
     */

    public static HttpAcceptLanguage makeAcceptLanguage(String lang) {
	return new HttpAcceptLanguage(true, lang, (double) 1.0);
    }

    /**
     * Build an accept language object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpAcceptLanguage instance.
     */

    public static HttpAcceptLanguage parseAcceptLanguage(String strval) {
	HttpAcceptLanguage a = new HttpAcceptLanguage();
	a.setString(strval);
	return a;
    }

    /**
     * Build a list of accept language clauses for the <code>Accept-Language
     * </code> header.
     * @param langs A list of accepted languages, encoded as an array, or
     * <strong>null</strong> to create an empty list.
     */

    public static 
    HttpAcceptLanguageList makeAcceptLanguageList(HttpAcceptLanguage langs[]) {
	return new HttpAcceptLanguageList(langs);
    }

    /**
     * Build an accept language list object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpAcceptLanguageList instance.
     */

    public static 
    HttpAcceptLanguageList parseAcceptLanguageList(String strval) {
	HttpAcceptLanguageList a = new HttpAcceptLanguageList();
	a.setString(strval);
	return a;
    }

    /**
     * Build a empty bag instance.
     * Bags are used in PEP and PICS.
     * @param name The name of the bag to construct.
     * @return An empty bag instance.
     * @see HttpBag
     */

    public static HttpBag makeBag(String name) {
	return new HttpBag(true, name);
    }

    /**
     * Build a bag object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpBag instance.
     */

    public static HttpBag parseBag(String strval) {
	HttpBag a = new HttpBag();
	a.setString(strval);
	return a;
    }

    /**
     * Build an empty cache control directive.
     * @return An instance of HttpCacheControl, with default settings.
     * @see HttpCacheControl
     */

    public static HttpCacheControl makeCacheControl() {
	return new HttpCacheControl(true);
    }

    /**
     * Build a cache control object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpCacheControl instance.
     */

    public static HttpCacheControl parseCacheControl(String strval) {
	HttpCacheControl a = new HttpCacheControl();
	a.setString(strval);
	return a;
    }

    /**
     * Build a challenge requesting authorization from a client.
     * @param scheme The scheme used by that challenge.
     * @return An HttpChallenge instance.
     * @see HttpChallenge
     */

    public static HttpChallenge makeChallenge(String scheme) {
	return new HttpChallenge(true, scheme);
    }

    /**
     * Build a challenge object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpChallenge instance.
     */

    public static HttpChallenge parseChallenge(String strval) {
	HttpChallenge a = new HttpChallenge();
	a.setString(strval);
	return a;
    }

    /**
     * Build the description of a HTTP content range.
     * @param unit The unit of that range.
     * @param firstpos The first position of that range (can be <strong>-1
     * </strong> to indicate a postfix range.
     * @param lastpost The last position of that range (can be <strong>-1
     * </strong> to indicate a prefix range).
     * @param length The full length of the entity from which that range was
     * taken.
     * @return An instance of HttpContentRange.
     * @see HttpContentRange
     */

    public static HttpContentRange makeContentRange(String unit
						    , int firstpos
						    , int lastpos
						    , int length) {
	return new HttpContentRange(true, unit, firstpos, lastpos, length);
    }

    /**
     * Build a content range object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpContantRange instance.
     */

    public static HttpContentRange parseContentRange(String strval) {
	HttpContentRange a = new HttpContentRange();
	a.setString(strval);
	return a;
    }

    /**
     * Build a single cookie value.
     * @param name The name of that cookie.
     * @param value The value of that cookie.
     * @return An instance of HttpCookie.
     * @see HttpCookie
     */

    public static HttpCookie makeCookie(String name, String value) {
	return new HttpCookie(true, name, value);
    }

    /**
     * Build a list of cookies out of a set of cookies.
     * @param cookies The cookies to be added to the list, may be
     * <strong>null</strong> to create an empty list.
     * @return An instance of HttpCookieList.
     * @see HttpCookieList
     */

    public static HttpCookieList makeCookieList(HttpCookie cookies[]) {
	return new HttpCookieList(cookies);
    }

    /**
     * Build a cookie list object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpCookieList instance.
     */

    public static HttpCookieList parseCookieList(String strval) {
	HttpCookieList a = new HttpCookieList();
	a.setString(strval);
	return a;
    }

    /**
     * Build credential informations.
     * @param scheme The scheme for that credentials.
     * @return An instance of HttpCredential.
     * @see HttpCredential
     */

    public static HttpCredential makeCredential(String scheme) {
	return new HttpCredential(true, scheme);
    }

    /**
     * Build a credential object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpCredential instance.
     */

    public static HttpCredential parseCredential(String strval) {
	HttpCredential a = new HttpCredential();
	a.setString(strval);
	return a;
    }

    /**
     * Build an HTTP date object.
     * @param date The date, given in milliseconds since Java epoch.
     * @return An instance of HttpDate.
     * @see HttpDate
     */

    public static HttpDate makeDate(long date) {
	return new HttpDate(true, date);
    }

    /**
     * Build an HTTP date object representing the current time.
     * @return An instance of HttpDate.
     * @see HttpDate
     */

    public static HttpDate makeDate() {
	return new HttpDate(true, System.currentTimeMillis());
    }

    /**
     * Build a date object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpDate instance.
     */

    public static HttpDate parseDate(String strval) {
	HttpDate a = new HttpDate();
	a.setString(strval);
	return a;
    }

    /**
     * Build an entity tag object.
     * @param isWeak Is this a weak entity tag.
     * @param tag The tag encoded as a String.
     * @return An instance of HttpEntityTag.
     * @see HttpEntityTag
     */

    public static HttpEntityTag makeETag(boolean isWeak, String tag) {
	return new HttpEntityTag(true, isWeak, tag);
    }

    /**
     * Build an entity tag object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpEntityTag instance.
     */

    public static HttpEntityTag parseETag(String strval) {
	HttpEntityTag a = new HttpEntityTag();
	a.setString(strval);
	return a;
    }

    /**
     * Build an entity tag list.
     * @param tags A list of enetity tags, encoded as an array, or <strong>
     * null</strong> to create an empty list.
     * @return An instance of HttpEntityTagList.
     * @see HttpEntityTagList
     */

    public static HttpEntityTagList makeETagList(HttpEntityTag tags[]) {
	return new HttpEntityTagList(tags);
    }

    /**
     * Build an entity tag list object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpEntityTagList instance.
     */

    public static HttpEntityTagList parseEntityTagList(String strval) {
	HttpEntityTagList a = new HttpEntityTagList();
	a.setString(strval);
	return a;
    }

    /**
     * Build a wrapper for an HTTP integer.
     * @param i The integer to wrap for HTTP transportation.
     * @return An instance of HttpInteger.
     * @see HttpInteger
     */

    public static HttpInteger makeInteger(int i) {
	return new HttpInteger(true, i);
    }

    /**
     * Build an integer object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpInteger instance.
     */

    public static HttpInteger parseInteger(String strval) {
	HttpInteger a = new HttpInteger();
	a.setString(strval);
	return a;
    }

    /**
     * Build a wrapper for a MIME type suitable for HTTP transportation.
     * @param type The MIME type to wrap.
     * @return An instance of HttpMimeType.
     * @see HttpMimeType
     */

    public static HttpMimeType makeMimeType(MimeType type) {
	return new HttpMimeType(true, type);
    }

    /**
     * Build an MIME type object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpMimeType instance.
     */

    public static HttpMimeType parseMimeType(String strval) {
	HttpMimeType a = new HttpMimeType();
	a.setString(strval);
	return a;
    }

    /**
     * Build an object representing an HTTP range.
     * @param unit The units in which that byte range is measured.
     * @param firstpos The first position of requested byte range.
     * @param lastpos The last position of requested byte range.
     * @return An instance of HttpRange.
     * @see HttpRange
     */

    public static HttpRange makeRange(String unit, int firstpos, int lastpos) {
	return new HttpRange(true, unit, firstpos, lastpos);
    }

    /**
     * Build a range object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpRange instance.
     */

    public static HttpRange parseRange(String strval) {
	HttpRange a = new HttpRange();
	a.setString(strval);
	return a;
    }

    /**
     * Build a list of ranges.
     * @param ranges A list of ranges, encoded as an array, or <strong>
     * null</strong> to create an empty list.
     * @return An instance of HttprangeList.
     * @see HttpRangeList
     */

    public static HttpRangeList makeRangeList(HttpRange ranges[]) {
	return new HttpRangeList(ranges);
    }

    /**
     * Build a list of ranges object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpRangeList instance.
     */

    public static HttpRangeList parseRangeList(String strval) {
	HttpRangeList a = new HttpRangeList();
	a.setString(strval);
	return a;
    }

    /**
     * Build a set cookie clause for the <code>Set-Cookie</code> header.
     * @param name The name of the cookie we are requesting to be set.
     * @param value It's value.
     * @return An instance of HttpSetCookie.
     * @see HttpSetCookie
     */

    public static HttpSetCookie makeSetCookie(String name, String value) {
	return new HttpSetCookie(true, name, value);
    }

    /**
     * Build a list of set cookies commands.
     * @param setcookies A list of set cookie commands, encoded as an
     * array, or <strong>null</strong> to build an empty list.
     * @return An instance of HttpStCookieList.
     * @see HttpSetCookieList
     */

    public static 
    HttpSetCookieList makeSetCookieList(HttpSetCookie setcookies[]) {
	return new HttpSetCookieList(setcookies);
    }

    /**
     * Build a list of set cookies object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpSetCookieList instance.
     */

    public static HttpSetCookieList parseSetCookieList(String strval) {
	HttpSetCookieList a = new HttpSetCookieList();
	a.setString(strval);
	return a;
    }

    /**
     * Build a wrapper for a String, for HTTP transportation.
     * @param str The String (or <em>token</em>) to wrap.
     * @return An instance of HttpString.
     * @see HttpString
     */

    public static HttpString makeString(String str) {
	return new HttpString(true, str);
    }

    /**
     * Build a String object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpString instance.
     */

    public static HttpString parseString(String strval) {
	HttpString a = new HttpString();
	a.setString(strval);
	return a;
    }

    /**
     * Build a list of strings.
     * @param list The list of strings, encoded as an array, or <strong>null
     * </strong> to create an empty list.
     * @return An instance of HttpTokenList.
     * @see HttpTokenList
     */

    public static HttpTokenList makeStringList(String list[]) {
	return new HttpTokenList(list);
    }

    /**
     * Build a list of one string.
     * @param item The item to be added to the list.
     * @return An instance of HttpTokenList.
     * @see HttpTokenList
     */

    public static HttpTokenList makeStringList(String item) {
	String list[] = new String[1];
	list[0]       = item;
	return new HttpTokenList(list);
    }

    /**
     * Build a list of string object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpTokenList instance.
     */

    public static HttpTokenList parseTokenList(String strval) {
	HttpTokenList a = new HttpTokenList();
	a.setString(strval);
	return a;
    }

    /**
     * Build an HTTP Warning object.
     * @param status The warning status code.
     * @param agent The agent generating that warning.
     * @param text The text for the warning.
     * @return An instance of HttpWarning.
     * @see HttpWarning
     */

    public static HttpWarning makeWarning(int status
					  , String agent
					  , String text) {
	return new HttpWarning(true, status, agent, text);
    }

    /**
     * Build an HTTP Warning object.
     * @param status The warning status code.
     * @return An instance of HttpWarning.
     * @see HttpWarning
     */

    public static HttpWarning makeWarning(int status) {
	return new HttpWarning(true, status, null, null);
    }

    /**
     * Build a warning object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpWarning instance.
     */

    public static HttpWarning parseWarning(String strval) {
	HttpWarning a = new HttpWarning();
	a.setString(strval);
	return a;
    }

    /**
     * Build a list of warnings for the <code>Warning</code> header.
     * @param warnings A list of warning encoded as an array, or <strong>
     * null</strong> to get an empty list.
     * @return An instance of HttpWarningList.
     * @see HttpWarningList
     */

    public static HttpWarningList makeWarningList(HttpWarning warnings[]) {
	return new HttpWarningList(warnings);
    }

    /**
     * Build a list of warnings object by parsing the given string.
     * @param strval The String to parse.
     * @return An HttpWarningList instance.
     */

    public static HttpWarningList parseWarningList(String strval) {
	HttpWarningList a = new HttpWarningList();
	a.setString(strval);
	return a;
    }

}


