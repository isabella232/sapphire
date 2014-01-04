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

package org.eclipse.sapphire.ui.swt.gef.model;

import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.diagram.DiagramConnectionPart;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class DiagramConnectionLabelModel extends DiagramModelBase {
	
    public final static String CONNECTION_LABEL = "CONNECTION_LABEL";
    public final static String CONNECTION_LABEL_MOVED = "CONNECTION_LABEL_MOVED";
    public final static String CONNECTION_START_EDITING = "CONNECTION_START_EDITING";

    private DiagramConnectionModel connectionModel;

	public DiagramConnectionLabelModel(DiagramConnectionModel connectionModel) {
		this.connectionModel = connectionModel;
	}
	
	@Override
	public SapphirePart getSapphirePart() {
		return getModelPart();
	}
	
	public DiagramModel getDiagramModel() {
		return connectionModel.getDiagramModel();
	}

	public DiagramConnectionModel getConnectionModel() {
		return connectionModel;
	}

	public DiagramConnectionPart getModelPart() {
		return connectionModel.getModelPart();
	}

	public void handleUpdateLabel() {
		firePropertyChange(CONNECTION_LABEL, null, getModelPart().getLabel());
	}

	public void handleUpdateLabelMoved() {
		firePropertyChange(CONNECTION_LABEL_MOVED, null, getModelPart().getLabel());
	}

	public void handleStartEditing() {
		firePropertyChange(CONNECTION_START_EDITING, null, null);
	}

}
