// TemplateContainer.java
// $Id: TemplateContainer.java,v 1.1 2010/06/15 12:28:03 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.indexer;

import java.io.File;

import org.w3c.tools.resources.ExternalContainer;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceContext;
import org.w3c.tools.resources.ServerInterface;

public class TemplateContainer extends ExternalContainer {

  public File getRepository(ResourceContext context) {
    return new File(context.getServer().getIndexerDirectory(),
		    getIdentifier());
  }

  public TemplateContainer(ResourceContext context, String id) {
    super(id, context, true);
  }

}
