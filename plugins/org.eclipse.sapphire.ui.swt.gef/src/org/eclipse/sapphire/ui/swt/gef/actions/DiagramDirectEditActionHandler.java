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

package org.eclipse.sapphire.ui.swt.gef.actions;

import java.util.List;

import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.diagram.DiagramConnectionPart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.ShapePart;
import org.eclipse.sapphire.ui.swt.gef.SapphireDiagramEditor;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramModel;
import org.eclipse.sapphire.ui.swt.gef.presentation.DiagramPresentation;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class DiagramDirectEditActionHandler extends SapphireActionHandler
{

	@Override
	protected Object run( final Presentation context ) 
	{
		DiagramPresentation presentation = (DiagramPresentation)context;
		SapphireDiagramEditor diagramEditor = presentation.getConfigurationManager().getDiagramEditor();
		if (diagramEditor != null)
		{
			List<ISapphirePart> parts = diagramEditor.getSelectedParts();
			DiagramModel model = diagramEditor.getDiagramModel();
			if (parts.size() == 1)
			{
				ISapphirePart part = parts.get(0);
				if (part instanceof DiagramNodePart)
				{
					model.handleDirectEditing((DiagramNodePart)part);
				}
				else if (part instanceof ShapePart)
				{
					model.handleDirectEditing((ShapePart)part);
//					DiagramNodePart nodePart = part.nearest(DiagramNodePart.class);
//					
//					DiagramNodeModel nodeModel = model.getDiagramNodeModel(nodePart);
//					ShapeModel shapeModel = ShapeModelUtil.getChildShapeModel(nodeModel.getShapeModel(), (ShapePart)part);
//					shapeModel.handleDirectEditing((ShapePart)part);
				}
				else if (part instanceof DiagramConnectionPart && (((DiagramConnectionPart)part).removable()))
				{
					model.handleDirectEditing((DiagramConnectionPart)part);
				}
			}
		}
		return null;
	}

}
