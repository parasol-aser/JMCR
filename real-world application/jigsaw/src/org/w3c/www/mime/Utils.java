// Utils.java
// $Id: Utils.java,v 1.1 2010/06/15 12:26:30 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.mime;

import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import org.w3c.tools.sorter.Sorter;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class Utils {

    private static Hashtable extension_map = new Hashtable();

    private static void setSuffix(String ext, String ct) {
	extension_map.put(ext, ct);
    }

    private static Hashtable charset_map = new Hashtable();

    private static void setCharset(String lang, String charset) {
	charset_map.put(lang, charset);
    }

    static {
	setSuffix("", "content/unknown");
	setSuffix("uu", "application/octet-stream");
	setSuffix("saveme", "application/octet-stream");
	setSuffix("dump", "application/octet-stream");
	setSuffix("hqx", "application/octet-stream");
	setSuffix("arc", "application/octet-stream");
	setSuffix("o", "application/octet-stream");
	setSuffix("a", "application/octet-stream");
	setSuffix("bin", "application/octet-stream");
	setSuffix("exe", "application/octet-stream");
	setSuffix("z", "application/octet-stream");
	setSuffix("gz", "application/octet-stream");
	setSuffix("jar", "application/octet-stream");
	setSuffix("oda", "application/oda");
	setSuffix("pdf", "application/pdf");
	setSuffix("eps", "application/postscript");
	setSuffix("ai", "application/postscript");
	setSuffix("ps", "application/postscript");
	setSuffix("rtf", "application/rtf");
	setSuffix("dvi", "application/x-dvi");
	setSuffix("hdf", "application/x-hdf");
	setSuffix("latex", "application/x-latex");
	setSuffix("cdf", "application/x-netcdf");
	setSuffix("nc", "application/x-netcdf");
	setSuffix("tex", "application/x-tex");
	setSuffix("texinfo", "application/x-texinfo");
	setSuffix("texi", "application/x-texinfo");
	setSuffix("t", "application/x-troff");
	setSuffix("tr", "application/x-troff");
	setSuffix("roff", "application/x-troff");
	setSuffix("man", "application/x-troff-man");
	setSuffix("me", "application/x-troff-me");
	setSuffix("ms", "application/x-troff-ms");
	setSuffix("src", "application/x-wais-source");
	setSuffix("wsrc", "application/x-wais-source");
	setSuffix("zip", "application/zip");
	setSuffix("bcpio", "application/x-bcpio");
	setSuffix("cpio", "application/x-cpio");
	setSuffix("gtar", "application/x-gtar");
	setSuffix("shar", "application/x-shar");
	setSuffix("sh", "application/x-shar");
	setSuffix("sv4cpio", "application/x-sv4cpio");
	setSuffix("sv4crc", "application/x-sv4crc");
	setSuffix("tar", "application/x-tar");
	setSuffix("ustar", "application/x-ustar");
	setSuffix("snd", "audio/basic");
	setSuffix("au", "audio/basic");
	setSuffix("aifc", "audio/x-aiff");
	setSuffix("aif", "audio/x-aiff");
	setSuffix("aiff", "audio/x-aiff");
	setSuffix("wav", "audio/x-wav");
	setSuffix("gif", "image/gif");
	setSuffix("ief", "image/ief");
	setSuffix("jfif", "image/jpeg");
	setSuffix("jfif-tbnl", "image/jpeg");
	setSuffix("jpe", "image/jpeg");
	setSuffix("jpg", "image/jpeg");
	setSuffix("jpeg", "image/jpeg");
	setSuffix("tif", "image/tiff");
	setSuffix("tiff", "image/tiff");
	setSuffix("ras", "image/x-cmu-rast");
	setSuffix("pnm", "image/x-portable-anymap");
	setSuffix("pbm", "image/x-portable-bitmap");
	setSuffix("pgm", "image/x-portable-graymap");
	setSuffix("ppm", "image/x-portable-pixmap");
	setSuffix("rgb", "image/x-rgb");
	setSuffix("png", "image/png");
	setSuffix("xbm", "image/x-xbitmap");
	setSuffix("xml", "text/xml");
	setSuffix("xsl", "text/xml");
	setSuffix("xsd", "text/xml");
	setSuffix("xpm", "image/x-xpixmap");
	setSuffix("xwd", "image/x-xwindowdump");
	setSuffix("htm", "text/html");
	setSuffix("html", "text/html");
	setSuffix("xhtm", "application/xhtml+xml");
	setSuffix("xhtml", "application/xhtml+xml");
	setSuffix("css", "text/css");
	setSuffix("text", "text/plain");
	setSuffix("c", "text/plain");
	setSuffix("cc", "text/plain");
	setSuffix("c++", "text/plain");
	setSuffix("h", "text/plain");
	setSuffix("pl", "text/plain");
	setSuffix("txt", "text/plain");
	setSuffix("java", "text/plain");
	setSuffix("rtx", "application/rtf");
	setSuffix("tsv", "texyt/tab-separated-values");
	setSuffix("etx", "text/x-setext");
	setSuffix("mpg", "video/mpeg");
	setSuffix("mpe", "video/mpeg");
	setSuffix("mpeg", "video/mpeg");
	setSuffix("mov", "video/quicktime");
	setSuffix("qt", "video/quicktime");
	setSuffix("avi", "application/x-troff-msvideo");
	setSuffix("movie", "video/x-sgi-movie");
	setSuffix("mv", "video/x-sgi-movie");
	setSuffix("mime", "message/rfc822");
	setSuffix("smi", "application/smil");
	setSuffix("smil", "application/smil");
	setSuffix("sgml", "text/sgml");
	setSuffix("sgm", "text/sgml");
	setSuffix("svg", "image/svg+xml");

	setCharset("ar", "ISO-8859-6");
	setCharset("be", "ISO-8859-5");
	setCharset("bg", "ISO-8859-5");
	setCharset("ca", "ISO-8859-1");
	setCharset("cs", "ISO-8859-2");
	setCharset("da", "ISO-8859-1");
	setCharset("de", "ISO-8859-1");
	setCharset("el", "ISO-8859-7");
	setCharset("en", "ISO-8859-1");
	setCharset("es", "ISO-8859-1");
	setCharset("et", "ISO-8859-1");
	setCharset("fi", "ISO-8859-1");
	setCharset("fr", "ISO-8859-1");
	setCharset("hr", "ISO-8859-2");
	setCharset("hu", "ISO-8859-2");
	setCharset("is", "ISO-8859-1");
	setCharset("it", "ISO-8859-1");
	setCharset("iw", "ISO-8859-8");
	setCharset("ja", "Shift_JIS");
	setCharset("ko", "EUC-KR");
	setCharset("lt", "ISO-8859-2");
	setCharset("lv", "ISO-8859-2");
	setCharset("mk", "ISO-8859-5");
	setCharset("nl", "ISO-8859-1");
	setCharset("no", "ISO-8859-1");
	setCharset("pl", "ISO-8859-2");
	setCharset("pt", "ISO-8859-1");
	setCharset("ro", "ISO-8859-2");
	setCharset("ru", "ISO-8859-5");
	setCharset("sh", "ISO-8859-5");
	setCharset("sk", "ISO-8859-2");
	setCharset("sl", "ISO-8859-2");
	setCharset("sq", "ISO-8859-2");
	setCharset("sr", "ISO-8859-5");
	setCharset("sv", "ISO-8859-1");
	setCharset("tr", "ISO-8859-9");
	setCharset("uk", "ISO-8859-5");
	setCharset("zh", "GB2312");
	setCharset("zh_TW", "Big5");

    }

    /**
     * A useful utility routine that tries to guess the content-type
     * of an object based upon its extension.
     */
    public static String guessContentTypeFromName(String fname) {
	String ext = "";
	int i = fname.lastIndexOf('#');

	if (i != -1)
	    fname = fname.substring(0, i - 1);
	i = fname.lastIndexOf('.');
	i = Math.max(i, fname.lastIndexOf('/'));
	i = Math.max(i, fname.lastIndexOf('?'));

	if (i != -1 && fname.charAt(i) == '.') {
	    if (++i < fname.length()) {
		ext = fname.substring(i).toLowerCase();
	    }
	}
	return (String) extension_map.get(ext);
    }

    public static MimeType getMimeType(String filename) {
	try {
	    return new MimeType(guessContentTypeFromName(filename));
	} catch (MimeTypeFormatException ex) {
	    return null;
	}
    }

    public static Vector getKnownMimeTypesinVector() {
        return Sorter.sortStringEnumeration(extension_map.elements());
    }

    public static String[] getKnownMimeTypes() {
        Vector sorted = Sorter.sortStringEnumeration(extension_map.elements());
        String array[] = new String[sorted.size()];
        sorted.copyInto(array);
        return array;
    }

    public static Vector getKnownExtensionsinVector() {
	return Sorter.sortStringEnumeration(extension_map.keys());
    }

    public static String[] getKnownExtensions() {
        Vector sorted = Sorter.sortStringEnumeration(extension_map.keys());
        String array[] = new String[sorted.size()];
        sorted.copyInto(array);
        return array;
    }

    public static Vector getKnownLanguagesinVector() {
	return Sorter.sortStringEnumeration(charset_map.keys());
    }

    public static String[] getKnownLanguages() {
        Vector sorted = Sorter.sortStringEnumeration(charset_map.keys());
        String array[] = new String[sorted.size()];
        sorted.copyInto(array);
        return array;
    }

    public static Vector getKnownCharsetsinVector() {
	return Sorter.sortStringEnumeration(charset_map.elements());
    }

    public static String[] getKnownCharsets() {
        Vector sorted = Sorter.sortStringEnumeration(charset_map.elements());
        String array[] = new String[sorted.size()];
        sorted.copyInto(array);
        return array;
    }

    public static String getCharset(Locale locale) {
	return getCharset(locale.getLanguage());
    }

    public static String getCharset(String lang) {
	return (String)charset_map.get(lang);
    }

}
