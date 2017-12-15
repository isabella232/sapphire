/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - [376266] Diagram delete all connection bend points action should be available in multi-select mode
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.actions;

import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.diagram.editor.DiagramConnectionPart;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class DeleteAllBendPointsForConnectionActionHandler extends SapphireActionHandler 
{
    
    @Override
    public boolean isEnabled()
    {
        return ! ( (DiagramConnectionPart) getPart() ).getConnectionBendpoints().isEmpty();
    }

    @Override
    protected Object run( final SapphireRenderingContext context) 
    {
        ( (DiagramConnectionPart) getPart() ).removeAllBendpoints();
        
        return null;
    }

}
