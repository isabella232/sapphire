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

import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionDef;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class ContextButtonConnectionRequest extends CreateConnectionRequest 
{
	private IDiagramConnectionDef connDef;
	
	public void setConnectionDef(IDiagramConnectionDef connDef)
	{
		this.connDef = connDef;
	}
	
	@Override
	public Object getNewObjectType() 
	{
		return this.connDef;
	}
}
