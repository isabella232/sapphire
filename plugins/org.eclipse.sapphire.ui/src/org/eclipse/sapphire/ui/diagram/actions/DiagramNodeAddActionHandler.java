/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 *    Shenxue Zhou - [374433] DiagramNodeAddActionHandlerFactory issues
 *    Konstantin Komissarchik - [381794] Cleanup needed in presentation code for diagram context menu
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.actions;

import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.ui.Point;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeTemplate;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class DiagramNodeAddActionHandler extends SapphireActionHandler 
{
	public static final String ID_BASE = "Sapphire.Add.";
	private DiagramNodeTemplate nodeTemplate;
	
	public DiagramNodeAddActionHandler(DiagramNodeTemplate nodeTemplate)
	{
		this.nodeTemplate = nodeTemplate;
	}
	
    @Override
    public void init( final SapphireAction action,
                      final ActionHandlerDef def )
    {
    	super.init(action, def);
    	setId( ID_BASE + this.nodeTemplate.getNodeTypeId());
		if (this.nodeTemplate.getToolPaletteLabel() != null)
		{
			setLabel(this.nodeTemplate.getToolPaletteLabel());
		}
		final ImageData typeSpecificAddImage = this.nodeTemplate.getNodeType().image();
		if (typeSpecificAddImage != null)
		{
			addImage(typeSpecificAddImage);
		}
    }
    
	@Override
	protected Object run(Presentation context) 
	{
    	SapphireDiagramEditorPagePart diagramPart = 
    			(SapphireDiagramEditorPagePart)this.nodeTemplate.parent();

		DiagramNodePart nodePart = this.nodeTemplate.createNewDiagramNode();
		Point pt = diagramPart.getMouseLocation();
		nodePart.setNodeBounds(pt.getX(), pt.getY());
		
		// Select the new node and put it in direct-edit mode
		diagramPart.selectAndDirectEdit(nodePart);
		return nodePart;
	}	

	public DiagramNodeTemplate getNodeTemplate()
	{
		return this.nodeTemplate;
	}
}
