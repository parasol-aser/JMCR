// StatisticsFrame.java
// $Id: StatisticsFrame.java,v 1.2 2010/06/15 17:53:08 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.status ;

import java.util.Date;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.Resource;

import org.w3c.tools.resources.store.ResourceStoreManager;

import org.w3c.www.http.HTTP;
import org.w3c.www.http.HttpMessage;

import org.w3c.jigsaw.frames.HTTPFrame;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.httpd;
import org.w3c.jigsaw.http.httpdStatistics;

import org.w3c.jigsaw.html.HtmlGenerator;

/**
 * This class exports the server statistics.
 * It makes available a bunch of various parameters about the current
 * server, and uses the Refresh meta-tag (as the ThreadStat) to 
 * make them redisplay.
 * <p>This would benefit from being an applet.
 */

public class StatisticsFrame extends HTTPFrame {
    private static int REFRESH_DEFAULT = 5;

    /**
     * Attribute index - Our refresh interval.
     */
    protected static int ATTR_REFRESH = -1 ;

    static {
	Attribute a   = null ;
	Class     cls = null ;
	try {
	    cls = Class.forName("org.w3c.jigsaw.status.StatisticsFrame");
	    //Added by Jeff Huang
	    //TODO: FIXIT
	} catch (Exception ex) {
	    ex.printStackTrace() ;
	    System.exit(1) ;
	}
	// The refresh interval attribute:
	a = new IntegerAttribute("refresh"
				 , new Integer(5)
				 , Attribute.EDITABLE) ;
	ATTR_REFRESH = AttributeRegistry.registerAttribute(cls, a) ;
    }

    static String time_tbl = ("<table border=\"1\" class=\"time\">"
			      + "<caption>Request processing times"
			      + "</caption><tr>"
			      + "<th>min"
			      + "<th>avg"
			      + "<th>max"
			      + "</tr><tr>") ;

    static String dyn_time_tbl = ("<table border=\"1\" class=\"time\">"
				  + "<caption>Dynamic request processing times"
				  + "</caption><tr>"
				  + "<th>min"
				  + "<th>avg"
				  + "<th>max"
				  + "</tr><tr>") ;

    static String sta_time_tbl = ("<table border=\"1\" class=\"time\">"
				  + "<caption>Static request processing times"
				  + "</caption><tr>"
				  + "<th>min"
				  + "<th>avg"
				  + "<th>max"
				  + "</tr><tr>") ;

    public void registerResource(FramedResource resource) {
	super.registerOtherResource(resource);
    }
				
    /**
     * Get the current set of statistics.
     * Display the collected statistics in an HTML table.
     * @param request TYhe request to process.
     */

    public Reply get (Request request) {
	HtmlGenerator g = new HtmlGenerator("Statistics") ;
	int refresh = getInt(ATTR_REFRESH, REFRESH_DEFAULT);
	if (refresh > 0) {
	    g.addMeta("Refresh", Integer.toString(refresh));
	}
	addStyleSheet(g);
	// Dump the statistics:
	httpdStatistics stats = ((httpd)getServer()).getStatistics() ;
	// Uptime:
	g.append("<h1>Server statistics</h1>");
	long start_time = stats.getStartTime();
	long uptime     = (System.currentTimeMillis() - start_time) / 1000;
	g.append("<p>Your server was started on <span class=\"date\">");
	g.append(new Date(start_time).toString());
	long duptime = uptime / (3600L*24L);
	long htemp   = uptime % (3600L*24L);
	long huptime = htemp / 3600L;
	long mtemp   = htemp % 3600L;
	long muptime = mtemp / 60L;
	long suptime = mtemp % 60L;
	g.append("</span>\n<p>It has now been running for <span "+
		 "class=\"uptime\">");
	g.append(Long.toString(duptime));
	g.append(" days, ");
	g.append(Long.toString(huptime));
	g.append(" hours, ");
	g.append(Long.toString(muptime));
	g.append(" minutes and ");
	g.append(Long.toString(suptime));
	g.append(" seconds.</span>\n");
	// Hits and bytes:
	long  nb_hits      = stats.getHitCount();
	long  dyn_hits     = stats.getDynamicHitCount();
	long  static_hits  = stats.getStaticHitCount();
	float static_pcent = 0;
	float dyn_pcent    = 0;
	if (nb_hits > 0) {
	    static_pcent = ((float) static_hits / (float) nb_hits) * 100;
	    dyn_pcent = ((float) dyn_hits / (float) nb_hits) * 100;
	}
	g.append("<ul><li>hits: ", Long.toString(nb_hits));
	g.append("  <ul>\n    <li>static: ",Long.toString(static_hits));
	g.append(" (", Float.toString(static_pcent));
	g.append("%)</li>\n    <li>dynamic: ", Long.toString(dyn_hits));
	g.append(" (", Float.toString(dyn_pcent));
	g.append("%)</li>\n  </ul>\n");
	long bytes = stats.getEmittedBytes();
	long kbytes = bytes / 1024;
	long mbytes = kbytes / 1024;
	long gbytes = mbytes / 1024;
        long tbytes = gbytes / 1024;
	if (tbytes != 0) {
	    g.append("</li>\n<li>bytes: ", Long.toString( tbytes),"TB, ");
	    g.append(Long.toString(gbytes % 1024), "GB, ");
	    g.append(Long.toString(mbytes % 1024), "MB, ");
	    g.append(Long.toString(kbytes % 1024), "KB, ");
	    g.append(Long.toString(bytes % 1024));
	} else if (gbytes != 0) {
	    g.append("</li>\n<li>bytes: ", Long.toString(gbytes), "GB, ");
	    g.append(Long.toString(mbytes % 1024), "MB, ");
	    g.append(Long.toString(kbytes % 1024), "KB, ");
	    g.append(Long.toString(bytes % 1024));
	} else if (mbytes != 0) {
	    g.append("</li>\n<li>bytes: ", Long.toString(mbytes), "MB, ");
	    g.append(Long.toString(kbytes % 1024), "KB, ");
	    g.append(Long.toString(bytes % 1024));
	} else if (kbytes != 0) {
	    g.append("</li>\n<li>bytes: ", Long.toString(kbytes), "KB, ");
	    g.append(Long.toString(bytes % 1024));
	} else {
	    g.append("</li>\n<li>bytes: ", Long.toString(bytes));
	}
	// avg hit/sec
	float avghits = 0;
	float avghitsday = 0;
	if (uptime > 0) {
	    avghits = ((float) nb_hits) / ((float) uptime);
	    avghitsday = ((float) nb_hits * 86400) / ((float) uptime); 
	}
	g.append("</li>\n<li>Average hits per second: ");
	g.append(Float.toString(avghits));
	g.append("</li>\n<li>Average hits per day: ");
	g.append(Integer.toString((int) avghitsday));
	// avg bytes/hit
	long avgbph;

	if (nb_hits > 0) {
	    avgbph = bytes / nb_hits;
	} else {
	    avgbph = 0;
	}
	kbytes = avgbph / 1024;
	mbytes = kbytes / 1024;
	gbytes = mbytes / 1024;
	if (gbytes != 0) {
	    g.append("</li>\n<li>Average bytes per hit: ");
	    g.append(Long.toString(gbytes), "GB, ");
	    g.append(Long.toString(mbytes % 1024), "MB, ");
	    g.append(Long.toString(kbytes % 1024), "KB, ");
	    g.append(Long.toString(avgbph % 1024));
	} else if (mbytes != 0) {
	    g.append("</li>\n<li>Average bytes per hit: ");
	    g.append(Long.toString(mbytes), "MB, ");
	    g.append(Long.toString(kbytes % 1024), "KB, ");
	    g.append(Long.toString(avgbph % 1024));	    
	} else if (kbytes != 0) {
	    g.append("</li>\n<li>Average bytes per hit: ");
	    g.append(Long.toString(kbytes), "KB, ");
	    g.append(Long.toString(avgbph % 1024));
	} else {
	    g.append("</li>\n<li>Average bytes per hit: ",
		     Long.toString(avgbph));
	}
	// avg throughput
	long avgbps = 0;
	if (uptime > 0) {
	    avgbps = bytes / uptime;
	}
	kbytes = avgbps / 1024;
	mbytes = kbytes / 1024;
	gbytes = mbytes / 1024;
	if (gbytes != 0) {
	    g.append("</li>\n<li>Average bytes per second: ");
	    g.append(Long.toString(gbytes),  "GB, ");
	    g.append(Long.toString(mbytes % 1024), "MB, ");
	    g.append(Long.toString(kbytes % 1024), "KB, ");
	    g.append(Long.toString(avgbps % 1024));
	} else if (mbytes != 0) {
	    g.append("</li>\n<li>Average bytes per second: ");
	    g.append(Long.toString(mbytes), "MB, ");
	    g.append(Long.toString(kbytes % 1024), "KB, ");
	    g.append(Long.toString(avgbps % 1024));
	} else if (kbytes != 0) {
	    g.append("</li>\n<li>Average bytes per second: ");
	    g.append(Long.toString(kbytes), "KB, ");
	    g.append(Long.toString(avgbps % 1024));
	} else {
	    g.append("</li>\n<li>Average bytes per second: ", 
		     Long.toString(avgbps));
	}    
	g.append("</li>\n</ul>");
	// Request times:
	g.append(time_tbl) ;
	g.append("<td>"
		 , Long.toString(stats.getMinRequestTime())
		 , " <span class=\"unit\">ms</span>") ;
	g.append("</td>\n<td>"
		 , Long.toString(stats.getMeanRequestTime())
		 , " <span class=\"unit\">ms</span>") ;
	g.append("</td>\n<td>"
		 , Long.toString(stats.getMaxRequestTime())
		 , " <span class=\"unit\">ms</span>") ;
	g.append("</td>\n</table>\n") ;

	// static
	if (static_hits>0) {
	    g.append(sta_time_tbl) ;
	    g.append("<td>"
		     , Long.toString(stats.getMinStaticRequestTime())
		     , " <span class=\"unit\">ms</span>") ;
	    g.append("</td>\n<td>"
		     , Long.toString(stats.getMeanStaticRequestTime())
		     , " <span class=\"unit\">ms</span>") ;
	    g.append("</td>\n<td>"
		     , Long.toString(stats.getMaxStaticRequestTime())
		     , " <span class=\"unit\">ms</span>") ;
	    g.append("</td>\n</table>\n") ;
	}

	// dynamic 
	if (dyn_hits>0) {
	    g.append(dyn_time_tbl) ;
	    g.append("<td>"
		     , Long.toString(stats.getMinDynamicRequestTime())
		     , " <span class=\"unit\">ms</span>") ;
	    g.append("</td>\n<td>"
		     , Long.toString(stats.getMeanDynamicRequestTime())
		     , " <span class=\"unit\">ms</span>") ;
	    g.append("</td>\n<td>"
		     , Long.toString(stats.getMaxDynamicRequestTime())
		     , " <span class=\"unit\">ms</span>") ;
	    g.append("</td>\n</table>\n") ;
	}

	// Get Server internal Stats
	try {
	    g.append(((httpd)getServer()).getHTMLStatus());
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	Reply reply = request.makeReply(HTTP.OK) ;
	reply.setNoCache();
	reply.setStream (g) ;
	reply.setDynamic(true);
	return reply ;
    }
}


