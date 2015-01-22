/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - [376266] Diagram delete all connection bend points action should be available in multi-select mode
 *    Ling Hao - [383924] Flexible diagram node shapes
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.actions;

import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireEditorPagePart.SelectionChangedEvent;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.diagram.ConnectionBendpointsEvent;
import org.eclipse.sapphire.ui.diagram.DiagramConnectionPart;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class DeleteAllBendPointsForConnectionActionHandler extends SapphireActionHandler 
{
    
	@Override
	public void init(SapphireAction action, ActionHandlerDef def) {
		super.init(action, def);

    	DiagramConnectionPart part = (DiagramConnectionPart) getPart();
		part.attach(new Listener() {
			@Override
			public void handle(final Event e) {
                if( e instanceof SelectionChangedEvent ) {
                    broadcast( new EnablementChangedEvent() );
                } else if (e instanceof ConnectionBendpointsEvent) {
                	broadcast( new EnablementChangedEvent() );
				}

			}
		});
	}

	@Override
    public boolean isEnabled()
    {
        return ! ( (DiagramConnectionPart) getPart() ).getBendpoints().isEmpty();
    }

    @Override
    protected Object run( final Presentation context) 
    {
        ( (DiagramConnectionPart) getPart() ).removeAllBendpoints();
        
        return null;
    }

}
