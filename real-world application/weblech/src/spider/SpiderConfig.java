/*
 * This is the MIT license, see also http://www.opensource.org/licenses/mit-license.html
 *
 * Copyright (c) 2001 Brian Pitcher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

// $Header: /project/jiss/smhuang/leap/weblech/src/spider/SpiderConfig.java,v 1.1 2010/06/30 15:45:27 smhuang Exp $

package spider;

import weblech.util.Logger;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;

public class SpiderConfig extends Logger implements Serializable
{
    private File saveRootDirectory;
    private File mailtoLogFile;

    private boolean refreshHTMLs;
    private boolean refreshImages;
    private boolean refreshOthers;

    private Set htmlExtensions;
    private Set imageExtensions;

    private URL startLocation;
    private String urlMatch;

    private List interestingURLSubstrings;
    private List boringURLSubstrings;

    private boolean depthFirst;
    private int maxDepth;

    private String userAgent;

    private String basicAuthUser;
    private String basicAuthPassword;

    private int spiderThreads;

    private long checkpointInterval;

    /**
     * Create a default config.
     */
    public SpiderConfig()
    {
        _logClass.debug("SpiderConfig()");

        saveRootDirectory = new File(".");
        mailtoLogFile = new File("mailto.txt");

        refreshHTMLs = true;
        refreshImages = false;
        refreshOthers = false;

        htmlExtensions = new HashSet();
        htmlExtensions.add("htm");
        htmlExtensions.add("html");
        htmlExtensions.add("shtml");

        imageExtensions = new HashSet();
        imageExtensions.add("jpg");
        imageExtensions.add("gif");
        imageExtensions.add("png");

        urlMatch = null;
        interestingURLSubstrings = new ArrayList();
        boringURLSubstrings = new ArrayList();
        depthFirst = false;
        maxDepth = 0;

        userAgent = "WebLech Spider 0.01alpha";
        basicAuthUser = "";
        basicAuthPassword = "";

        spiderThreads = 1;

        checkpointInterval = 0;
    }

    /**
     * Create a config from a java.util.Properties object.
     */
    public SpiderConfig(Properties props)
    {
        _logClass.debug("SpiderConfig(props)");

        saveRootDirectory = new File(props.getProperty("saveRootDirectory", "."));
        if(!saveRootDirectory.exists())
        {
            if(!saveRootDirectory.mkdirs())
            {
                _logClass.error("Couldn't create root directory: " + saveRootDirectory);
                _logClass.info("Defaulting to . instead");
                saveRootDirectory = new File(".");
            }
        }
        else if(!saveRootDirectory.isDirectory())
        {
            _logClass.error("Save root is not a directory: " + saveRootDirectory);
            _logClass.info("Defaulting to . instead");
            saveRootDirectory = new File(".");
        }

        String mailtoFileStr = props.getProperty("mailtoLogFile", "mailto.txt");
        // Check if absolute or relative name given
        if(mailtoFileStr.indexOf(":") != -1 || mailtoFileStr.startsWith("/") || mailtoFileStr.startsWith("\\"))
        {
            _logClass.debug("Using absolute file name " + mailtoFileStr);
            mailtoLogFile = new File(mailtoFileStr);
        }
        else
        {
            _logClass.debug("Constructing relative file name " + saveRootDirectory.getPath() + "/" + mailtoFileStr);
            mailtoLogFile = new File(saveRootDirectory.getPath() + "/" + mailtoFileStr);
        }

        refreshHTMLs = Boolean.valueOf(props.getProperty("refreshHTMLs", "true")).booleanValue();
        refreshImages = Boolean.valueOf(props.getProperty("refreshImages", "false")).booleanValue();
        refreshOthers = Boolean.valueOf(props.getProperty("refreshOthers", "false")).booleanValue();

        htmlExtensions = parseSet(props.getProperty("htmlExtensions", "htm,html,shtml"));
        imageExtensions = parseSet(props.getProperty("imageExtensions", "jpg,gif,png"));

        String startLocStr = props.getProperty("startLocation");
        if(startLocStr != null)
        {
            try
            {
                startLocation = new URL(startLocStr);
            }
            catch(MalformedURLException murle)
            {
                _logClass.error("Caught MalformedURLException parsing start URL '" + startLocStr + "' : " + murle.getMessage(), murle);
            }
        }
        else
        {
            _logClass.warn("startLocation not found in properties");
        }

        urlMatch = props.getProperty("urlMatch");

        interestingURLSubstrings = parsePropCommaSeparated(props.getProperty("interestingURLs"));
        boringURLSubstrings = parsePropCommaSeparated(props.getProperty("boringURLs"));

        depthFirst = Boolean.valueOf(props.getProperty("depthFirst", "false")).booleanValue();
        try
        {
            String maxDepthStr = props.getProperty("maxDepth", "0");
            maxDepth = Integer.parseInt(maxDepthStr);
        }
        catch(NumberFormatException nfe)
        {
            _logClass.error("Caught number format exception parsing max depth, defaulting to 1", nfe);
            maxDepth = 1;
        }

        userAgent = props.getProperty("userAgent", "WebLech Spider 0.01alpha");
        basicAuthUser = props.getProperty("basicAuthUser", "");
        basicAuthPassword = props.getProperty("basicAuthPassword", "");

        try
        {
            String threadsStr = props.getProperty("spiderThreads", "1");
            spiderThreads = Integer.parseInt(threadsStr);
        }
        catch(NumberFormatException nfe)
        {
            _logClass.error("Caught number format exception parsing number of threads, defaulting to 1", nfe);
            spiderThreads = 1;
        }

        try
        {
            String intervalStr = props.getProperty("checkpointInterval", "0");
            checkpointInterval = Long.parseLong(intervalStr);
        }
        catch(NumberFormatException nfe)
        {
            _logClass.error("Caught number format exception parsing checkpoint interval, defaulting to 0", nfe);
            spiderThreads = 1;
        }
    }

    private List parsePropCommaSeparated(String str)
    {
        ArrayList result = new ArrayList();
        if(str != null && str.length() > 0)
        {
            StringTokenizer tok = new StringTokenizer(str, ",");
            while(tok.hasMoreTokens())
            {
                result.add(tok.nextToken());
            }
        }
        return result;
    }


    public void setRefreshHTMLs(boolean refreshHTMLs)
    {
        this.refreshHTMLs = refreshHTMLs;
    }

    public boolean refreshHTMLs()
    {
        return refreshHTMLs;
    }

    public void setRefreshImages(boolean refreshImages)
    {
        this.refreshImages = refreshImages;
    }

    public boolean refreshImages()
    {
        return refreshImages;
    }

    public void setRefreshOthers(boolean refreshOthers)
    {
        this.refreshOthers = refreshOthers;
    }

    public boolean refreshOthers()
    {
        return refreshOthers;
    }

    public void setSaveRootDirectory(File saveRootDirectory)
    {
        this.saveRootDirectory = saveRootDirectory;
    }

    public File getSaveRootDirectory()
    {
        return saveRootDirectory;
    }

    public void setMailtoLogFile(File mailtoLogFile)
    {
        this.mailtoLogFile = mailtoLogFile;
    }

    public File getMailtoLogFile()
    {
        return mailtoLogFile;
    }

    public void setStartLocation(URL startLocation)
    {
        this.startLocation = startLocation;
    }

    public URL getStartLocation()
    {
        return startLocation;
    }

    public void setURLMatch(String urlMatch)
    {
        this.urlMatch = urlMatch;
    }

    public String getURLMatch()
    {
        return urlMatch;
    }

    public List getInterestingURLSubstrings()
    {
        return interestingURLSubstrings;
    }

    public void setInterestingURLSubstrings(List interestingURLSubstrings)
    {
        this.interestingURLSubstrings = interestingURLSubstrings;
    }

    public List getBoringURLSubstrings()
    {
        return boringURLSubstrings;
    }

    public void setBoringURLSubstrings(List boringURLSubstrings)
    {
        this.boringURLSubstrings = boringURLSubstrings;
    }

    public boolean isInteresting(URL u)
    {
        return matchURL(u, interestingURLSubstrings);
    }

    public boolean isBoring(URL u)
    {
        return matchURL(u, boringURLSubstrings);
    }

    private boolean matchURL(URL u, List substrings)
    {
        String str = u.toExternalForm();
        for(Iterator i = substrings.iterator(); i.hasNext(); )
        {
            String substr = (String) i.next();
            if(str.indexOf(substr) != -1)
            {
                return true;
            }
        }
        return false;
    }

    public void setDepthFirstSearch(boolean depthFirst)
    {
        this.depthFirst = depthFirst;
    }

    public boolean isDepthFirstSearch()
    {
        return depthFirst;
    }

    public void setMaxDepth(int maxDepth)
    {
        this.maxDepth = maxDepth;
    }

    public int getMaxDepth()
    {
        return maxDepth;
    }

    public void setUserAgent(String userAgent)
    {
        this.userAgent = userAgent;
    }

    public String getUserAgent()
    {
        return userAgent;
    }

    public void setBasicAuthUser(String basicAuthUser)
    {
        this.basicAuthUser = basicAuthUser;
    }

    public String getBasicAuthUser()
    {
        return basicAuthUser;
    }

    public void setBasicAuthPassword(String basicAuthPassword)
    {
        this.basicAuthPassword = basicAuthPassword;
    }

    public String getBasicAuthPassword()
    {
        return basicAuthPassword;
    }

    public void setSpiderThreads(int spiderThreads)
    {
        this.spiderThreads = spiderThreads;
    }

    public int getSpiderThreads()
    {
        return spiderThreads;
    }

    public void setCheckpointInterval(long interval)
    {
        this.checkpointInterval = interval;
    }

    public long getCheckpointInterval()
    {
        return checkpointInterval;
    }

    public String toString()
    {
        return "depthFirst:\t" + depthFirst
           + "\nmaxDepth:\t" + maxDepth
           + "\nhtmlExtensions:\t" + fromSet(htmlExtensions)
           + "\nimageExtensions:\t" + fromSet(imageExtensions)
           + "\nrefreshHTMLs:\t" + refreshHTMLs
           + "\nrefreshImages:\t" + refreshImages
           + "\nrefreshOthers:\t" + refreshOthers
           + "\nsaveRootDirectory:\t" + saveRootDirectory
           + "\nstartLocation:\t" + startLocation
           + "\nurlMatch:\t" + urlMatch
           + "\nuserAgent:\t" + userAgent
           + "\nbasicAuthUser:\t" + basicAuthUser
           + "\nbasicAuthPassword:\t" + "***"
           + "\nspiderThreads:\t" + spiderThreads
           + "\ncheckpointInterval:\t" + checkpointInterval;
    }

    private Set parseSet(String str)
    {
        _logClass.debug("parseSet(" + str + ")");
        HashSet result = new HashSet();
        StringTokenizer sTok = new StringTokenizer(str, ",");
        while(sTok.hasMoreTokens())
        {
            String tok = sTok.nextToken().trim();
            result.add(tok);
        }
        return result;
    }

    private String fromSet(Set s)
    {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for(Iterator i = s.iterator(); i.hasNext(); )
        {
            String str = (String) i.next();
            if(first)
            {
                first = false;
            }
            else
            {
                sb.append(",");
            }
            sb.append(str);
        }
        return sb.toString();
    }

} // End class SpiderConfig
