<?xml version="1.0" encoding="UTF-8" ?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!--  
 | Composite DITA topics to FO

 *-->

<!DOCTYPE xsl:stylesheet [
<!-- entities for use in the generated output (Unicode typographic glyphs) -->
  <!ENTITY gt            "&gt;"> 
  <!ENTITY lt            "&lt;"> 
  <!ENTITY rbl           "&#160;">
  <!ENTITY nbsp          "&#160;">
  <!ENTITY quot          "&#34;">
  <!ENTITY quotedblleft  "&#x201C;">
  <!ENTITY quotedblright "&#x201D;">
  <!ENTITY sqbull        "[]">
  <!ENTITY middot        "&#x00A5;">

  <!ENTITY section       "&#xA7;">
  <!ENTITY endash        "&#x2013;">
  <!ENTITY emdash        "&#x2014;">

  <!ENTITY copyr         "&#xA9;">
  <!ENTITY trademark     "&#x2122;">
  <!ENTITY registered    "&#xAE;">
<!-- create some fixed values for now for customizing later -->
  <!ENTITY copyrowner    "Sample Company">
  <!ENTITY copyrdate     "2001">
  <!ENTITY headerstub    "(stub for header content)">

]>
<xsl:stylesheet version="1.0"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fox="http://xml.apache.org/fop/extensions"
			 xmlns:java="http://xml.apache.org/xslt/java"
                    exclude-result-prefixes="java">
  <!-- stylesheet imports -->
  <xsl:import href="xslfo/topic2foImpl.xsl"/>
  <xsl:import href="xslfo/domains2fo.xsl"/>
  <!-- XSL-FO output with XML syntax; no actual doctype for FO -->
  <xsl:output method="xml" version="1.0"/>
  <!-- CONTROL PARAMETERS: -->
  <!-- offset -->
  <xsl:param name="basic-start-indent">72pt</xsl:param>
  <xsl:param name="basic-end-indent">24pt</xsl:param>
  <xsl:param name="output-related-links"/>
  <!-- GLOBALS: -->
  <xsl:param name="dflt-ext">.jpg</xsl:param>
  <!-- For Antenna House, set to ".jpg" -->
  <!-- Set the prefix for error message numbers -->
  <xsl:variable name="msgprefix">IDXS</xsl:variable>
  <!-- =============== start of override tweaks ============== -->
  <!-- force draft mode on all the time -->
  <xsl:param name="DRAFT" select="'no'"/>
  <!-- ====================== template rules for merged content ==================== -->
  <xsl:template match="dita" mode="toplevel">
    <xsl:call-template name="dita-setup"/>
  </xsl:template>
  <xsl:template match="*[contains(@class,' map/map ')]" mode="toplevel">
    <xsl:call-template name="dita-setup"/>
  </xsl:template>
  <!-- note that bkinfo provides information for a cover in a bookmap application, 
     hence bkinfo does not need to be instanced.  In an application that processes
     only maps, bkinfo should process as a topic based on default processing. -->
  <xsl:template match="*[contains(@class,' bkinfo/bkinfo ')]" priority="2">
    <!-- no operation for bkinfo -->
  </xsl:template>
  <!-- =========================== overall output organization ========================= -->
  <!-- this template rule defines the overall output organization -->
  <xsl:template name="dita-setup">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
      <!-- get the overall master page defs here -->
      <xsl:call-template name="define-page-masters-dita"/>
      <!-- create FOP outline elements for PDF bookmarks -->
      <xsl:call-template name="create-bookmark-tree"/>
      <!-- place generated content -->
      <xsl:call-template name="front-covers"/>
      <!--xsl:call-template name="titlepage-ednotice"/-->
      <xsl:call-template name="generated-frontmatter"/>
      <!-- place main content (fall through occurs here) -->
      <xsl:call-template name="main-doc3"/>
      <!-- return to place closing generated content -->
      <!--xsl:call-template name="index-chapter"/-->
      <!--xsl:call-template name="back-covers"/-->
    </fo:root>
  </xsl:template>
  <!-- create FOP outline elements for PDF bookmarks -->
  <xsl:template name="create-bookmark-tree">
    <fo:bookmark-tree>
      <!-- hard code bookmarks for the table of contents and cover -->
      <fo:bookmark internal-destination="cover">
        <fo:bookmark-title>Cover</fo:bookmark-title>
      </fo:bookmark>
      <fo:bookmark internal-destination="contents">
        <fo:bookmark-title>Contents</fo:bookmark-title>
      </fo:bookmark>
      <xsl:apply-templates mode="outline"/>
    </fo:bookmark-tree>
  </xsl:template>
  <xsl:template match="*" mode="outline">
    <xsl:if test="contains(@class,' topic/topic ')">
      <fo:bookmark>
        <xsl:attribute name="internal-destination">
          <!-- use id attribute node to generate anchor for PDF bookmark fix bug#1304859 -->
          <xsl:value-of select="@id"/>
        </xsl:attribute>
        <fo:bookmark-title>
          <!-- if topic contains navtitle, use that as label for PDF bookmark -->
          <!-- otherwise, use title -->
          <xsl:choose>
            <xsl:when test="navtitle">
              <xsl:apply-templates select="navtitle" mode="text-only"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="title" mode="text-only"/>
            </xsl:otherwise>
          </xsl:choose>
        </fo:bookmark-title>
        <xsl:apply-templates mode="outline" />
      </fo:bookmark>
    </xsl:if>
  </xsl:template>
  <xsl:template match="*" mode="text-only">
    <xsl:apply-templates select="text()|*" mode="text-only"/>
  </xsl:template>
  <xsl:template name="define-page-masters-dita">
    <fo:layout-master-set>
      <!-- master set for chapter pages, first page is the title page -->
      <fo:page-sequence-master master-name="chapter-master">
        <fo:repeatable-page-master-alternatives>
          <fo:conditional-page-master-reference page-position="first"
            odd-or-even="odd" master-reference="common-page"/>
          <!-- chapter-first-odd -->
          <fo:conditional-page-master-reference page-position="first"
            odd-or-even="even" master-reference="common-page"/>
          <!--chapter-first-even"/-->
          <fo:conditional-page-master-reference page-position="rest"
            odd-or-even="odd" master-reference="common-page"/>
          <!--chapter-rest-odd"/-->
          <fo:conditional-page-master-reference page-position="rest"
            odd-or-even="even" master-reference="common-page"/>
          <!--chapter-rest-even"/-->
        </fo:repeatable-page-master-alternatives>
      </fo:page-sequence-master>
      <fo:simple-page-master master-name="cover" xsl:use-attribute-sets="common-grid">
        <fo:region-body margin-top="72pt"/>
      </fo:simple-page-master>
      <fo:simple-page-master master-name="common-page" xsl:use-attribute-sets="common-grid">
        <fo:region-body margin-bottom="36pt" margin-top="12pt"/>
        <fo:region-before extent="12pt"/>
        <fo:region-after extent="24pt"/>
      </fo:simple-page-master>
    </fo:layout-master-set>
  </xsl:template>
  <xsl:template name="front-covers">
    <!-- generate an "outside front cover" page (right side) (sheet 1) -->
    <fo:page-sequence master-reference="cover">
      <fo:title>
        <xsl:value-of select="//*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/prodname ')]"/>
      </fo:title>
     
<!--Jeff added this here -->
<!--fo:static-content flow-name="xsl-region-after"-->    

    <!--/fo:static-content-->
<!--End of Jeff's addition -->

<!--Jeff is adding this section to replace what was originally there below -->
<fo:flow flow-name="xsl-region-body" >
        <!-- Custom cover art/text goes here -->
      <xsl:call-template name="place-cover-art"/>
      <!-- End of custom art section -->
      <fo:block text-align="left" font-family="Helvetica" id="cover">
        <!-- set the brand and title -->
        <fo:block font-size="22pt" font-weight="bold" line-height="140%">
         <xsl:value-of select="//*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/prodname ')]"/>
        </fo:block>
        <!-- set the subtitle -->
        <fo:block font-size="8pt" line-height="140%" margin-bottom="1in" font-style="italic">
          Version <xsl:value-of select="//*[contains(@class,' topic/prodinfo ')]/vrmlist/vrm/@version"/>
        </fo:block>
    <!-- set the brief copyright notice -->
<!-- <fo:block margin-top="3pc" text-align="right" font-size="8pt" line-height="normal">
          &copyr;  Copyright&nbsp; 
        <xsl:value-of select="//*[contains(@class,' topic/copyright ')]/copyryear/@year"/>:&nbsp; 
        <xsl:value-of select="//*[contains(@class,' topic/copyright ')]/*[contains(@class,' topic/copyrholder ')]"/><xsl:text> </xsl:text>
</fo:block>
-->
      </fo:block>     


 <!-- set the brief timestamp -->
<fo:block margin-top="3pc" text-align="right" font-size="8pt" line-height="normal">
          Derby Document build: <xsl:call-template name="TodaysDate"/>
</fo:block> 


     
</fo:flow>


    </fo:page-sequence>
    <!-- generate an "inside front cover" page (left side) (sheet 2) -->
    <fo:page-sequence master-reference="cover">
      <fo:flow flow-name="xsl-region-body">
        <fo:block xsl:use-attribute-sets="p" color="purple" text-align="center"/>
      </fo:flow>
    </fo:page-sequence>
  </xsl:template>

<!-- Timestamp template -->
<xsl:template name="TodaysDate">
<fo:block>
    <xsl:value-of select="java:format(java:java.text.SimpleDateFormat.new
('MMMM d, yyyy, h:mm:ss a (zz)'), java:java.util.Date.new())"/>
 </fo:block>
 </xsl:template>


<!-- Jeff is adding this to replace the piece below -->
<xsl:template name="place-cover-art">
<!-- product specific art, etc. -->
<fo:block margin-top="2pc" font-family="Helvetica" border-color="black" border-width="thin" padding="6pt">
  <fo:block font-size="12pt" line-height="100%" margin-top="12pc" margin-bottom="12pc" text-align="right">
    <fo:external-graphic src="url(../images/logowithtext.jpg)"/>
  </fo:block>
</fo:block>
</xsl:template>



  <!-- internal title page -->
  <!-- edition notices -->
  <!-- document notice data -->
  <!-- grant of usage data -->
  <!-- copyright info -->
  <!-- disclaimers -->
  <!-- redirect to notices page -->
  <!-- definitions for placement of Front Matter content -->
  <xsl:template name="generated-frontmatter">
    <fo:page-sequence master-reference="common-page" format="i" initial-page-number="1">
      <!-- Static setup for the generated pages -->
      <!-- header -->
      <fo:static-content flow-name="xsl-region-before">
<!-- version number header table of contents-->
        <fo:block font-size="8pt" line-height="140%" margin-bottom="1in" font-style="italic">
          Version <xsl:value-of select="//*[contains(@class,' topic/prodinfo ')]/vrmlist/vrm/@version"/>
         &#160;
	<xsl:value-of select="//*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/prodname ')]"/>
	</fo:block>
	</fo:static-content>
<!-- end version number -->
      <!-- footer -->
      <fo:static-content flow-name="xsl-region-after">
        <fo:block text-align="center" font-size="10pt" font-weight="bold" font-family="Helvetica">
          <fo:page-number/>
        </fo:block>
      </fo:static-content>
      <!-- Flow setup for the Front Matter "body" (new "chapters" start on odd pages) -->
      <fo:flow flow-name="xsl-region-body">
        <!-- first, generate a compulsory Table of Contents -->
        <fo:block line-height="12pt" font-size="10pt" font-family="Helvetica" id="page1-1">
          <fo:block text-align="left" font-family="Helvetica">
            <fo:block>
              <fo:leader color="black" leader-pattern="rule"
                rule-thickness="3pt" leader-length="2in"/>
            </fo:block>
            <fo:block font-size="20pt" font-weight="bold" line-height="140%" id="contents">
              Contents </fo:block>
            <xsl:call-template name="gen-toc"/>
          </fo:block>
        </fo:block>
      </fo:flow>
    </fo:page-sequence>
  </xsl:template>
  <xsl:template name="unused-toc">
    <!-- generate the List of Figures -->
    <!-- To be done
        <fo:block text-align="left" font-family="Helvetica" break-before="page">
    <fo:block><fo:leader color="black" leader-pattern="rule" rule-thickness="3pt" leader-length="2in"/></fo:block>
          <fo:block font-size="20pt" font-weight="bold" line-height="140%">
            Figures
          </fo:block>
          <xsl:call-template name="gen-figlist"/>
        </fo:block>
-->
    <!-- generate the List of Tables -->
    <!-- To be done
        <fo:block text-align="left" font-family="Helvetica" break-before="page">
    <fo:block><fo:leader color="black" leader-pattern="rule" rule-thickness="3pt" leader-length="2in"/></fo:block>
          <fo:block font-size="20pt" font-weight="bold" line-height="140%">
            Tables
          </fo:block>
          <xsl:call-template name="gen-tlist"/>
        </fo:block>
-->
    <!-- To be done: while still in Roman numbering, all the bkfrontm content... -->
  </xsl:template>
  <!-- initiate main content processing within basic page "shell" -->
  <xsl:template name="main-doc3">
    <fo:page-sequence master-reference="chapter-master">
      <!-- header: single page -->
      <fo:static-content flow-name="xsl-region-before">
        <!-- book title here -->
        <fo:block font-size="8pt" line-height="8pt">
	<xsl:value-of select="//*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/prodname ')]"/>  
	</fo:block>
      </fo:static-content>
      <!-- footer static stuff -->
      <fo:static-content flow-name="xsl-region-after">
        <fo:block text-align="center" font-size="10pt" font-weight="bold" font-family="Helvetica">
          <fo:page-number/>
        </fo:block>
      </fo:static-content>
      <!-- special footers for first page of new chapter -->
      <!-- Flow setup for the main content (frontm, body, backm) (new "chapters" start on odd pages) -->
      <fo:flow flow-name="xsl-region-body">
        <!-- chapter body content here -->
        <fo:block text-align="left" font-size="10pt" font-family="Helvetica" break-before="page">
          <xsl:apply-templates/>
        </fo:block>
      </fo:flow>
    </fo:page-sequence>
  </xsl:template>
  <!-- set up common attributes for all page definitions -->
  <xsl:attribute-set name="common-grid">
    <xsl:attribute name="page-width">51pc</xsl:attribute>
    <!-- A4: 210mm -->
    <xsl:attribute name="page-height">66pc</xsl:attribute>
    <!-- A4: 297mm -->
    <xsl:attribute name="margin-top">3pc</xsl:attribute>
    <xsl:attribute name="margin-bottom">3pc</xsl:attribute>
    <xsl:attribute name="margin-left">6pc</xsl:attribute>
    <xsl:attribute name="margin-right">6pc</xsl:attribute>
  </xsl:attribute-set>
  <!-- set up common attributes for all page definitions -->
  <xsl:attribute-set name="maptitle">
    <xsl:attribute name="font-size">16pt</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
  </xsl:attribute-set>
  <!-- set up common attributes for all page definitions -->
  <xsl:attribute-set name="mapabstract">
    <xsl:attribute name="margin-top">3pc</xsl:attribute>
    <xsl:attribute name="margin-bottom">3pc</xsl:attribute>
    <xsl:attribute name="margin-left">6pc</xsl:attribute>
    <xsl:attribute name="margin-right">6pc</xsl:attribute>
  </xsl:attribute-set>
  <!-- main toc generator -->
  <xsl:template name="gen-toc">
    <!-- get by main part: body -->
    <xsl:for-each select="//map/*[contains(@class,' topic/topic ')]">
      <fo:block text-align-last="justify" margin-top="6pt" margin-left="4.9pc">
   <fo:basic-link internal-destination="{string(@id)}">
 <fo:inline font-weight="bold">
          <!--Chapter <xsl:number level="any" from="map"/>. -->
          <xsl:value-of select="*[contains(@class,' topic/title ')]"/>
       
  </fo:inline>
        <fo:leader leader-pattern="dots"/>
                <fo:page-number-citation ref-id="{string(@id)}"/>
    </fo:basic-link>  
</fo:block>
      <xsl:call-template name="get-tce2-section"/>
    </xsl:for-each>
  </xsl:template>
  <!-- 2nd level header -->
  <xsl:template name="get-tce2-section">
    <xsl:for-each select="*[contains(@class,' topic/topic ')]">
      <fo:block text-align-last="justify" margin-left="7.5pc">
<fo:basic-link internal-destination="{string(@id)}">
        <fo:inline font-weight="bold">
          <xsl:value-of select="*[contains(@class,' topic/title ')]"/>
        </fo:inline>
        <fo:leader leader-pattern="dots"/>
        <fo:page-number-citation ref-id="{string(@id)}"/>
        </fo:basic-link>  
 </fo:block>
      <xsl:call-template name="get-tce3-section"/>
    </xsl:for-each>
  </xsl:template>
  <!-- 3nd level header -->
  <xsl:template name="get-tce3-section">
    <xsl:for-each select="*[contains(@class,' topic/topic ')]">
      <fo:block text-align-last="justify" margin-left="9pc">
<fo:basic-link internal-destination="{string(@id)}">
        <xsl:value-of select="*[contains(@class,' topic/title ')]"/>
        <fo:leader leader-pattern="dots"/>
        <fo:page-number-citation ref-id="{string(@id)}"/>
           </fo:basic-link>  
   </fo:block>
      <xsl:call-template name="get-tce4-section"/>
    </xsl:for-each>
  </xsl:template>
  <!-- 4th level header -->
  <xsl:template name="get-tce4-section">
    <xsl:for-each select="bksubsect1">
      <fo:block text-align-last="justify" margin-left="+5.9pc">
<fo:basic-link internal-destination="{string(@id)}">
        <xsl:value-of select="*/title"/>
        <fo:leader leader-pattern="dots"/>
        <fo:page-number-citation ref-id="{string(@id)}"/>
	</fo:basic-link>
      </fo:block>
      <xsl:call-template name="get-tce5-section"/>
    </xsl:for-each>
  </xsl:template>
  <!-- 5th level header -->
  <xsl:template name="get-tce5-section">
    <xsl:for-each select="bksubsect2">
      <fo:block text-align-last="justify" margin-left="+5.9pc">
	<fo:basic-link internal-destination="{string(@id)}">
        <xsl:value-of select="*/title"/>
        <fo:leader leader-pattern="dots"/>
        <fo:page-number-citation ref-id="{string(@id)}"/>
	</fo:basic-link>
      </fo:block>
      <!--xsl:call-template name="get-tce6-section"/-->
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
