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

package org.eclipse.sapphire.ui.swt.gef.presentation;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ui.diagram.ConnectionBendpointsEvent;
import org.eclipse.sapphire.ui.diagram.ConnectionEvent;
import org.eclipse.sapphire.ui.diagram.ConnectionLabelEvent;
import org.eclipse.sapphire.ui.diagram.DiagramConnectionPart;
import org.eclipse.sapphire.ui.swt.gef.DiagramConfigurationManager;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramConnectionModel;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class DiagramConnectionPresentation extends DiagramPresentation 
{
	private Listener connectionListener;
	
	public DiagramConnectionPresentation(final DiagramConnectionPart connPart, final DiagramPresentation parent, 
			final Shell shell, final DiagramConfigurationManager configManager, final DiagramResourceCache resourceCache)
	{
		super(connPart, parent, configManager, shell);
	}

	public void init(final DiagramConnectionModel diagramConnectionModel) 
	{
		connectionListener = new FilteredListener<ConnectionEvent>() 
		{
			@Override
			protected void handleTypedEvent(ConnectionEvent event) 
			{
				if (event instanceof ConnectionBendpointsEvent)
				{
					diagramConnectionModel.handleUpdateBendPoints();
				}
				else if (event instanceof ConnectionLabelEvent)
				{
					ConnectionLabelEvent labelEvent = (ConnectionLabelEvent)event;
					if (labelEvent.moveLabel())
					{
						diagramConnectionModel.handleUpdateConnectionMoveLabel();
					}
					else
					{
						diagramConnectionModel.handleUpdateConnection();
					}
				}
			}
		};
		part().attach(connectionListener);
	}

	@Override
	public void dispose()
	{
		part().detach(connectionListener);
		super.dispose();
	}

	@Override
	public DiagramConnectionPart part()
	{
		return (DiagramConnectionPart)super.part();
	}

}
