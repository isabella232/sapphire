/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Ling Hao - [383924] Extend Sapphire Diagram Framework to support SQL Schema diagram like editors
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.parts;

import java.beans.PropertyChangeEvent;

import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.sapphire.ui.diagram.editor.TextPart;
import org.eclipse.sapphire.ui.swt.gef.DiagramConfigurationManager;
import org.eclipse.sapphire.ui.swt.gef.figures.TextFigure;
import org.eclipse.sapphire.ui.swt.gef.internal.DirectEditorManagerFactory;
import org.eclipse.sapphire.ui.swt.gef.model.ShapeModel;
import org.eclipse.sapphire.ui.swt.gef.model.TextModel;
import org.eclipse.sapphire.ui.swt.gef.policies.ShapeLabelDirectEditPolicy;
import org.eclipse.sapphire.ui.swt.gef.tools.SapphireDragEditPartsTracker;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class TextEditPart extends ShapeEditPart 
{
	public TextEditPart(DiagramConfigurationManager configManager)
	{
		super(configManager);
	}
		
	@Override
	protected void createEditPolicies() 
	{
		TextModel textModel = (TextModel)getModel();
		TextPart textPart = (TextPart)textModel.getSapphirePart();

		if (textPart.isEditable())
		{
			installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new ShapeLabelDirectEditPolicy());
		}
	}
	
	@Override 
	protected void refreshVisuals() 
	{
		TextModel textModel = (TextModel)getModel();
		TextPart textPart = (TextPart)textModel.getSapphirePart();
		((TextFigure)getFigure()).setText(textPart.getContent());
	}
		
	@Override
	public void performRequest(Request request) 
	{
		if (request.getType() == RequestConstants.REQ_DIRECT_EDIT)
		{
			if (!(request instanceof DirectEditRequest))
			{
				// Direct edit invoked using key command
				performDirectEdit();
			}
		}
		else if (request.getType().equals(REQ_OPEN) && (request instanceof SelectionRequest))
		{
			performDirectEdit();
		}
		else
		{
			super.performRequest(request);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) 
	{
		String prop = evt.getPropertyName();
		if (prop.equals(ShapeModel.SHAPE_START_EDITING))
		{
			performDirectEdit();
		}
	}
	
	private void performDirectEdit()
	{
		TextModel textModel = (TextModel)getModel();
		TextPart textPart = (TextPart)textModel.getSapphirePart();		
		if (textPart.isEditable())
		{
			TextFigure textFigure = (TextFigure)getFigure();
			if (textFigure != null)
			{
				DirectEditManager manager = DirectEditorManagerFactory.createDirectEditorManager(this, textPart, 
						new NodeCellEditorLocator(getConfigurationManager(), textFigure), textFigure);
				manager.show();
			}
		}
	}
	
	@Override
	public DragTracker getDragTracker(Request request) {
		return new SapphireDragEditPartsTracker(this);
	}
	
}
