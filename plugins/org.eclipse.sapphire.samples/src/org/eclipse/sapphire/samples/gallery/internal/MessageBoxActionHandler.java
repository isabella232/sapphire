/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.samples.gallery.internal;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class MessageBoxActionHandler

    extends SapphireActionHandler
    
{
    private String message;
    
    @Override
    public void init( final SapphireAction action,
                      final ISapphireActionHandlerDef def )
    {
        super.init( action, def );
        
        this.message = def.getParam( "message" );
    }
    
    @Override
    protected Object run( final SapphireRenderingContext context )
    {
        MessageDialog.openInformation( context.getShell(), "Message", this.message );
        return null;
    }
    
}
