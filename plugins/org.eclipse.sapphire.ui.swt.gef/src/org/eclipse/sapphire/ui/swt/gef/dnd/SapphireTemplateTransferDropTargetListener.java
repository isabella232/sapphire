/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.dnd;

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeTemplate;
import org.eclipse.sapphire.ui.swt.gef.SapphireDiagramEditor;
import org.eclipse.swt.dnd.DND;


/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class SapphireTemplateTransferDropTargetListener extends
		TemplateTransferDropTargetListener 
{
	private SapphireDiagramEditor diagramEditor;
	
	public SapphireTemplateTransferDropTargetListener(SapphireDiagramEditor diagramEditor)
	{
		super(diagramEditor.getGraphicalViewer());
		setEnablementDeterminedByCommand(true);
		this.diagramEditor = diagramEditor;
	}

	@Override
	protected void handleDrop() 
	{
		updateTargetRequest();
		updateTargetEditPart();

		if (getTargetEditPart() != null) 
		{
			Command command = getCommand();
			if (command != null && command.canExecute())
				getViewer().getEditDomain().getCommandStack().execute(command);
			else
				getCurrentEvent().detail = DND.DROP_NONE;
		} 
		else
		{
			getCurrentEvent().detail = DND.DROP_NONE;
		}
		selectAddedObject();
	}

	private void selectAddedObject() 
	{
		Object model = getCreateRequest().getNewObject();
		if (model instanceof DiagramNodeTemplate)
		{
			DiagramNodeTemplate nodeTemplate = (DiagramNodeTemplate)model;
			List<DiagramNodePart> nodeParts = nodeTemplate.getDiagramNodes();
			if (nodeParts.size() > 0)
			{
				DiagramNodePart nodePart = nodeParts.get(nodeParts.size() - 1);
				this.diagramEditor.selectAndDirectEditPart(nodePart);
			}
		}
	}

}
