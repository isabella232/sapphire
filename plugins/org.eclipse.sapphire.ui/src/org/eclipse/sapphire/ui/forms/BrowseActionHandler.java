/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.modeling.EditFailedException;
import org.eclipse.sapphire.ui.Presentation;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class BrowseActionHandler extends PropertyEditorActionHandler
{
    @Text( "Select {0}:" )
    private static LocalizableText browseDialogMessage;

    static 
    {
        LocalizableText.init( BrowseActionHandler.class );
    }

    @Override
    public Value<?> property()
    {
        return (Value<?>) super.property();
    }
    
    @Override
    protected final Object run( final Presentation context )
    {
        final String text = browse( context );
        
        if( text != null )
        {
            try
            {
                property().write( text );
            }
            catch( Exception e )
            {
                // Log this exception unless the cause is EditFailedException. These exception
                // are the result of the user declining a particular action that is necessary
                // before the edit can happen (such as making a file writable).
                
                final EditFailedException editFailedException = EditFailedException.findAsCause( e );
                
                if( editFailedException == null )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                }
            }
        }
        
        return null;
    }
    
    protected abstract String browse( final Presentation context );

    protected final String createBrowseDialogMessage( final String entity )
    {
        return browseDialogMessage.format( entity );
    }
    
}