// RuleParserException.java
// $Id: RuleNode.java,v 1.1 2010/06/15 12:28:31 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.proxy ;

import java.util.Hashtable;

/**
 * A RuleNode instance keeps track of one token within the lfh of a rule.
 * This data structure is usually known as a <em>hash-trie</em>, check
 * one of the Knuth books for more infos.
 */
public class RuleNode {
    /**
     * The token this node applies to.
     */
    String    token    = null;
    /**
     * It's optionally associated rule.
     */
    Rule      rule     = null;
    /**
     * A hashtable to keep track of children rules.
     */
    Hashtable children = null;

    void setRule(Rule rule) {
	this.rule = rule;
    }

    public Rule getRule() {
	return rule;
    }

    public Hashtable getChildren(){ 
	return children;
    }

    /**
     * Add a children rule node to this rule node.
     * @param token The child token.
     * @param rule The rule to map to this token.
     * @return The newly created RuleNode instance.
     */

    synchronized RuleNode addChild(String tok, Rule rule) {
	RuleNode node = new RuleNode(tok, rule);
	if ( children == null ) 
	    children = new Hashtable(5);
	children.put(tok, node);
	return node;
    }

    /**
     * Add a children rule node to this node.
     * This method does the same as above, except that it doesn't map
     * the created node to a rule.
     * @param tok The token this node applies to.
     * @return The newly createed RuleNode instance.
     */

    synchronized RuleNode addChild(String tok) {
	RuleNode node = new RuleNode(tok);
	if ( children == null ) 
	    children = new Hashtable(5);
	children.put(tok, node);
	return node;
    }

    /**
     * Lookup a rule.
     * Given a fully qualified host name, parse it into its components, and
     * starting from this rule node, lookup for a matching rule.
     * <p>The most precise rule is always returned.
     * @return The best matching rule, as a Rule instance, or <strong>
     * null</strong> if no matching rule was found.
     */

    public Rule lookupRule(String host) {
	// Parse the host into it's components:
	String  parts[] = new String[32];
	int     hostlen = host.length();
	int     phost   = 0;
	int     npart   = 0;   
	boolean isip    = true;
	for (int i = 0 ; i < hostlen; i++) {
	    if ( host.charAt(i) == '.' ) {
		if ( npart+1 >= parts.length ) {
		    // This is unlikely to happen, but anyway:
		    String newparts[] = new String[parts.length << 1];
		    System.arraycopy(parts, 0, newparts, 0, parts.length);
		    parts = newparts;
		}
		parts[npart++] = host.substring(phost, i);
		phost = ++i;
	    } else {
		if(isip)
		    isip = (host.charAt(i)>='0') && (host.charAt(i)<='9');
	    }
	}
	parts[npart++] = host.substring(phost);

	RuleNode node = this;
	Rule     ret  = rule;
	if(isip)
	    for (int i = 0; i <= npart ; i++ ) {
		node = node.lookup(parts[i]);
		if (node != null) {
		    ret = (node.rule != null) ? node.rule : rule;
		} else {
		    return ret;
		}
	    }
	else
	    for (int i = npart; --i >= 0 ; ) {
		node = node.lookup(parts[i]);
		if (node != null) {
		    ret = (node.rule != null) ? node.rule : ret;
		} else {
		    return ret;
		}
	    }
	return ret;
    }

    /**
     * Lookup a children rule node.
     * @param tok The token for the child that is to be looked up.
     * @return A RuleNode instance, if found, <strong>null</strong>
     * otherwise.
     */

    public RuleNode lookup(String tok) {
	return ((children != null) ? (RuleNode) children.get(tok) : null);
    }

    RuleNode(String token, Rule rule) {
	this.token = token.toLowerCase();
	this.rule  = rule;
    }

    RuleNode(String token) {
	this(token, null);
    }

    RuleNode() {
	this("**ROOT**", null);
    }
}
