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

package org.eclipse.sapphire.ui.swt.gef.internal;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionDef;
import org.eclipse.sapphire.ui.swt.gef.SapphireDiagramEditor;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class DiagramNodeConnectionCreationTool extends SapphireConnectionCreationTool 
{
	private SapphireDiagramEditor diagramEditor;
	private IDiagramConnectionDef connectionDef;
	
	public void continueConnection(EditPart targetEditPart, SapphireDiagramEditor diagramEditor, IDiagramConnectionDef connectionDef)
	{
		this.diagramEditor = diagramEditor;
		this.connectionDef = connectionDef;
		
		activate();
		setTargetEditPart(targetEditPart);
		
		setConnectionSource(targetEditPart);

		Command command = targetEditPart.getCommand(getTargetRequest());
		((CreateConnectionRequest) getTargetRequest()).setSourceEditPart(targetEditPart);
		if (command != null) 
		{
			setState(STATE_CONNECTION_STARTED);
			setCurrentCommand(command);
			setViewer(diagramEditor.getGraphicalViewer());
		}
		if (isInState(STATE_CONNECTION_STARTED))
		{
			updateTargetRequest();
			updateTargetUnderMouse();
			showSourceFeedback();
			showTargetFeedback();
			setCurrentCommand(command);
		}
	}
	
	@Override
	protected boolean handleMove() 
	{
		if (isInState(STATE_CONNECTION_STARTED)) 
		{
			updateTargetRequest();
			updateTargetUnderMouse();
			showSourceFeedback();
			showTargetFeedback();
			setCurrentCommand(getCommand());
		}
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.tools.AbstractConnectionCreationTool#createTargetRequest
	 * ()
	 */
	@Override
	protected Request createTargetRequest() 
	{
		ContextButtonConnectionRequest request = new ContextButtonConnectionRequest();
		request.setType(getCommandName());
		request.setConnectionDef(connectionDef);
		return request;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.tools.AbstractConnectionCreationTool#updateTargetRequest
	 * ()
	 */
	@Override
	protected void updateTargetRequest() 
	{
		updateTargetUnderMouse();

		CreateConnectionRequest request = (CreateConnectionRequest) getTargetRequest();
		request.setType(getCommandName());		

		org.eclipse.draw2d.geometry.Point absoluteMousePosition = diagramEditor.getMouseLocation();
		request.setLocation(absoluteMousePosition);

	}
	
	@Override
	protected boolean handleButtonDown(int button) 
	{
		if (button == 3 && stateTransition(STATE_CONNECTION_STARTED, STATE_TERMINAL))
		{
			getDomain().loadDefaultTool();
			return true;
		}
		return super.handleButtonDown(button);		
	}
	
}
