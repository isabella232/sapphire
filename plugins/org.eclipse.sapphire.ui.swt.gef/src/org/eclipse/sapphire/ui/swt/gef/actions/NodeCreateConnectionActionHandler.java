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

import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionDef;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.swt.gef.SapphireDiagramEditor;
import org.eclipse.sapphire.ui.swt.gef.internal.DiagramNodeConnectionCreationTool;
import org.eclipse.sapphire.ui.swt.gef.presentation.DiagramNodePresentation;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class NodeCreateConnectionActionHandler extends SapphireActionHandler 
{
	private IDiagramConnectionDef connectionDef;
	
	public NodeCreateConnectionActionHandler(IDiagramConnectionDef connectionDef)
	{
		this.connectionDef = connectionDef;
		
	}
	
	@Override
    public void init( final SapphireAction action, final ActionHandlerDef def )
    {
		super.init(action, def);
		String label = connectionDef.getToolPaletteLabel().content();
		String tooltip = connectionDef.getToolPaletteDescription().content();
		if (label != null)
		{
			setLabel(label);
		}
		if (tooltip != null)
		{
			setToolTip(tooltip);
		}
		
		SapphireDiagramEditorPagePart pagePart = action.getPart().nearest(SapphireDiagramEditorPagePart.class);
		List<SapphireDiagramEditorPagePart.ConnectionPalette> conns = pagePart.getConnectionPalettes();
		ImageData connImage = null;
		for (SapphireDiagramEditorPagePart.ConnectionPalette conn : conns)
		{
			if (conn.getConnectionDef() == this.connectionDef)
			{
				connImage = conn.getSmallIcon();
				break;
			}
		}
		if (connImage == null)
		{
			connImage = getAction().getImage(16);
		}
		if (connImage != null)
		{
			addImage(connImage);
		}
    }
	
	@Override
	protected Object run(Presentation context) 
	{
        DiagramNodePresentation nodePresentation = (DiagramNodePresentation)context;
        SapphireDiagramEditor diagramEditor = nodePresentation.getConfigurationManager().getDiagramEditor();

        if( diagramEditor != null )
        {
        	GraphicalEditPart sourceEditPart = diagramEditor.getGraphicalEditPart(nodePresentation.part());
    		DefaultEditDomain editDomain = diagramEditor.getEditDomain();
    		DiagramNodeConnectionCreationTool connectionTool = new DiagramNodeConnectionCreationTool();
    		connectionTool.setEditDomain(editDomain);
    		editDomain.setActiveTool(connectionTool);
    		
    		connectionTool.continueConnection(sourceEditPart, diagramEditor, connectionDef);

        }
		return null;
	}

}
