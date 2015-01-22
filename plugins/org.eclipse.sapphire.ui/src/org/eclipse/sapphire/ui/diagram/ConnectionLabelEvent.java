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

package org.eclipse.sapphire.ui.diagram;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class ConnectionLabelEvent extends ConnectionEvent 
{
	private boolean moveLabel;
	
	public ConnectionLabelEvent(DiagramConnectionPart connectionPart)
	{
		this(connectionPart, false);
	}
	
	public ConnectionLabelEvent(DiagramConnectionPart connectionPart, boolean moveLabel)
	{
		super(connectionPart);
		this.moveLabel = moveLabel;		
	}
	
	public boolean moveLabel()
	{
		return this.moveLabel;
	}
}
