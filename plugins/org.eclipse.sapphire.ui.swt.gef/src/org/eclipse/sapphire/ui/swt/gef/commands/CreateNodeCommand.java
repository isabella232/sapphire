/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.commands;

import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.diagram.actions.DiagramNodeAddActionHandler;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeTemplate;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.swt.gef.presentation.DiagramPresentation;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class CreateNodeCommand extends Command 
{
	private DiagramPresentation diagramPresentation;
	private DiagramNodeTemplate nodeTemplate;
	private Point location;

	public CreateNodeCommand(DiagramPresentation diagramPresentation, DiagramNodeTemplate nodeTemplate, Point location) {
		this.diagramPresentation = diagramPresentation;
		this.nodeTemplate = nodeTemplate;
		this.location = location;
	}

	@Override
	public void execute() 
	{		
		// Invoke "Sapphire.Add" action
		SapphireDiagramEditorPagePart editorPart = this.nodeTemplate.getDiagramEditorPart();
		SapphireAction addAction = editorPart.getAction(SapphireActionSystem.ACTION_ADD);
		if (addAction != null)
		{
			List<SapphireActionHandler> addHandlers = addAction.getActiveHandlers();
			for (SapphireActionHandler handler : addHandlers)
			{
				DiagramNodeAddActionHandler nodeAddHandler = (DiagramNodeAddActionHandler)handler;
				if (nodeAddHandler.getNodeTemplate().equals(this.nodeTemplate))
				{
					editorPart.setMouseLocation(this.location.x, this.location.y);
					nodeAddHandler.execute(this.diagramPresentation);
					break;
				}
			}
		}
	}	
}
