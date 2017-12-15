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

package org.eclipse.sapphire.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyService;
import org.eclipse.sapphire.modeling.ModelService;

/**
 * Produces the list of file extensions that are allowed for a path value property. Most frequently specified
 * via @FileExtensions annotation.
 * 
 * @since 0.3.1
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class FileExtensionsService extends ModelPropertyService
{
    private List<String> extensions = Collections.emptyList();
    
    @Override
    public final void init( final IModelElement element,
                            final ModelProperty property,
                            final String[] params )
    {
        super.init( element, property, params );
        
        initFileExtensionsService( element, property, params );
        
        refresh( false );
    }

    protected void initFileExtensionsService( final IModelElement element,
                                              final ModelProperty property,
                                              final String[] params )
    {
    }
    
    public final List<String> extensions()
    {
        return this.extensions;
    }
    
    protected abstract void compute( List<String> extensions );
    
    protected final void refresh()
    {
        refresh( true );
    }
    
    protected final void refresh( final boolean notifyListeners )
    {
        List<String> extensions = new ArrayList<String>();
        
        try
        {
            compute( extensions );
        }
        catch( Exception e )
        {
            LoggingService.log( e );
        }
        
        extensions = scrub( extensions );
        
        if( ! this.extensions.equals( extensions ) )
        {
            this.extensions = extensions;
            
            if( notifyListeners )
            {
                notifyListeners( new FileExtensionsChangedEvent( this ) );
            }
        }
    }
    
    private static List<String> scrub( final List<String> extensions )
    {
        final List<String> clean = new ArrayList<String>();
        
        for( String extension : extensions )
        {
            if( extension != null )
            {
                extension = extension.trim();
                
                if( extension.length() > 0 )
                {
                    clean.add( extension );
                }
            }
        }
        
        return Collections.unmodifiableList( clean );
    }
    
    public static class FileExtensionsChangedEvent extends Event
    {
        public FileExtensionsChangedEvent( final ModelService service )
        {
            super( service );
        }
    }
    
}
