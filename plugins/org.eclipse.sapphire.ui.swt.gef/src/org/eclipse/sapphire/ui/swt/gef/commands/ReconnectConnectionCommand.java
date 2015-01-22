/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 *    Shenxue Zhou - Implement the reconnect using sapphire diagram part api
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.commands;

import org.eclipse.gef.commands.Command;
import org.eclipse.sapphire.ui.diagram.ConnectionService;
import org.eclipse.sapphire.ui.diagram.DiagramConnectionPart;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramConnectionModel;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramNodeModel;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class ReconnectConnectionCommand extends Command {

	private final DiagramConnectionModel connection; 

	private final DiagramNodeModel oldSource;
	private final DiagramNodeModel oldTarget;

	private DiagramNodeModel newSource;
	private DiagramNodeModel newTarget;

	public ReconnectConnectionCommand(DiagramConnectionModel connection) {
		setLabel("connection creation");
		this.connection = connection;
		this.oldSource = connection.getSourceNode();
		this.oldTarget = connection.getTargetNode();
	}

	@Override
	public boolean canExecute() {
		DiagramConnectionPart connectionPart = this.connection.getModelPart();
		if (!connectionPart.removable()) {
			return false;
		}
		if (newSource != null) {
			return checkSourceReconnection();
		} else if (newTarget != null) {
			return checkTargetReconnection();
		}
		return false;
	}

	private boolean checkSourceReconnection() {
		DiagramConnectionPart connectionPart = this.connection.getModelPart();
		ConnectionService connService = connectionPart.nearest(SapphireDiagramEditorPagePart.class).service(ConnectionService.class);
		if (!connService.valid(newSource.getModelPart(), oldTarget.getModelPart(), connectionPart.getConnectionTypeId()))
			return false;

		return true;
	}

	private boolean checkTargetReconnection() {
		DiagramConnectionPart connectionPart = this.connection.getModelPart();
		ConnectionService connService = connectionPart.nearest(SapphireDiagramEditorPagePart.class).service(ConnectionService.class);
		if (!connService.valid(oldSource.getModelPart(), newTarget.getModelPart(), connectionPart.getConnectionTypeId()))
			return false;
				
		return true;
	}

	@Override
	public void execute() 
	{
		if ((newSource != null && newSource == oldSource) || 
				(newTarget != null && newTarget == oldTarget))
			return;
		DiagramConnectionPart connectionPart = connection.getModelPart();
		connectionPart.reconnect(newSource != null ? newSource.getModelPart() : oldSource.getModelPart(), 
									newTarget != null ? newTarget.getModelPart() : oldTarget.getModelPart());
	}

	public void setNewTarget(DiagramNodeModel target) {
		if (target == null) {
			throw new IllegalArgumentException();
		}
		this.newTarget = target;
		this.newSource = null;
	}

	public void setNewSource(DiagramNodeModel source) {
		if (source == null) {
			throw new IllegalArgumentException();
		}
		this.newSource = source;
		this.newTarget = null;
	}

}
