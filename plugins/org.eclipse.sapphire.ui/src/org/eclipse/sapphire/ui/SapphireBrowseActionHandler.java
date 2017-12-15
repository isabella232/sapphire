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

package org.eclipse.sapphire.ui;

import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.EditFailedException;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class SapphireBrowseActionHandler

    extends SapphirePropertyEditorActionHandler
    
{
    @Override
    public final ValueProperty getProperty()
    {
        return (ValueProperty) super.getProperty();
    }
    
    @Override
    protected final Object run( final SapphireRenderingContext context )
    {
        final String text = browse( context );
        
        if( text != null )
        {
            try
            {
                getModelElement().write( getProperty(), text );
            }
            catch( Exception e )
            {
                // Log this exception unless the cause is EditFailedException. These exception
                // are the result of the user declining a particular action that is necessary
                // before the edit can happen (such as making a file writable).
                
                final EditFailedException editFailedException = EditFailedException.findAsCause( e );
                
                if( editFailedException == null )
                {
                    SapphireUiFrameworkPlugin.log( e );
                }
            }
        }
        
        return null;
    }
    
    protected abstract String browse( final SapphireRenderingContext context );

    protected final String createBrowseDialogMessage( final String entity )
    {
        return NLS.bind( Resources.browseDialogMessage, entity );
    }
    
    private static final class Resources extends NLS 
    {
        public static String browseDialogMessage;

        static 
        {
            initializeMessages( SapphireBrowseActionHandler.class.getName(), Resources.class );
        }
    }
    
}