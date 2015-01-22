/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef;

import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionDef;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class ConnectionCreationFactory implements CreationFactory {
	
	private IDiagramConnectionDef connectionDef;
	
	public ConnectionCreationFactory(IDiagramConnectionDef connectionDef) {
		this.connectionDef = connectionDef;
	}

	public Object getNewObject() {
		return null;
	}

	public Object getObjectType() {
		return connectionDef;
	}

}
