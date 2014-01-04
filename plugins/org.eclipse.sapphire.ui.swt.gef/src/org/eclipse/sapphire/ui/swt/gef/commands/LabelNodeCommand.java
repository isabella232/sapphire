/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 *    Shenxue Zhou - Refreshing connections attached to the node
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ui.diagram.DiagramConnectionPart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.FunctionUtil;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.diagram.editor.TextPart;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class LabelNodeCommand extends Command {
	
	private TextPart textPart;
	private String labelText;

	public LabelNodeCommand(TextPart textPart, String labelText) {
		this.textPart = textPart;
		this.labelText = labelText;
	}

	@Override
	public void execute() 
	{
		DiagramNodePart nodePart = textPart.nearest(DiagramNodePart.class);
		SapphireDiagramEditorPagePart diagramPart = nodePart.nearest(SapphireDiagramEditorPagePart.class);
		List<DiagramConnectionPart> attachedConns = new ArrayList<DiagramConnectionPart>();
		List<DiagramConnectionPart> attachedConns2 = new ArrayList<DiagramConnectionPart>();
		
		for (DiagramConnectionPart connPart : diagramPart.getConnections())
		{
			if (!connPart.removable())
				continue;
			if (connPart.getEndpoint1() == nodePart.getLocalModelElement())
			{
				attachedConns.add(connPart);
			}
			if (connPart.getEndpoint2() == nodePart.getLocalModelElement())
			{
				attachedConns2.add(connPart);
			}
		}
			
		Value<?> prop = FunctionUtil.getFunctionProperty(this.textPart.getLocalModelElement(), 
				this.textPart.getContentFunction());
		prop.write( this.labelText );
		
		for (DiagramConnectionPart connPart : attachedConns)
		{
			connPart.reconnect(nodePart, diagramPart.getDiagramNodePart(connPart.getEndpoint2()));
		}
		for (DiagramConnectionPart connPart : attachedConns2)
		{
			connPart.reconnect(diagramPart.getDiagramNodePart(connPart.getEndpoint1()), nodePart);
		}				
	}
	
}
