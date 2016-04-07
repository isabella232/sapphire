/******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.actions;

import java.util.List;

import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionHandlerFactory;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionDef;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.util.ListFactory;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class ConnectActionHandlerFactory extends SapphireActionHandlerFactory 
{

	@Override
	public List<SapphireActionHandler> create() 
	{
        final ListFactory<SapphireActionHandler> handlers = ListFactory.start();
        if (getPart() instanceof DiagramNodePart)
        {
	        DiagramNodePart nodePart = (DiagramNodePart)getPart();
	        SapphireDiagramEditorPagePart pagePart = nodePart.nearest(SapphireDiagramEditorPagePart.class);
	        List<IDiagramConnectionDef> connectionDefs = pagePart.possibleConnectionDefs(nodePart);
	        for (IDiagramConnectionDef connDef : connectionDefs)
	        {
	        	NodeCreateConnectionActionHandler handler = new NodeCreateConnectionActionHandler(connDef);
	        	handlers.add(handler);
	        }
        }
        return handlers.result();
	}

}
