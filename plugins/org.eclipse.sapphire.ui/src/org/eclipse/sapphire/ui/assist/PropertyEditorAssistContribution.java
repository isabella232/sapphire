/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.assist;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.sapphire.ImageData;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PropertyEditorAssistContribution
{
    private final String text;
    private final Map<String,ImageData> images;
    private final Map<String,Runnable> links;
    
    public static Factory factory()
    {
        return new Factory();
    }
    
    private PropertyEditorAssistContribution( final String text,
                                              final Map<String,ImageData> images,
                                              final Map<String,Runnable> links )
    {
        this.text = text;
        this.images = Collections.unmodifiableMap( images );
        this.links = Collections.unmodifiableMap( links );
    }
    
    public final String text()
    {
        return this.text;
    }
    
    public final Map<String,ImageData> images()
    {
        return this.images;
    }
    
    public final ImageData image( final String id )
    {
        return this.images.get( id );
    }
    
    public final Map<String,Runnable> links()
    {
        return this.links;
    }
    
    public final Runnable link( final String id )
    {
        return this.links.get( id );
    }
    
    public static final class Factory
    {
        private String text;
        private final Map<String,ImageData> images = new HashMap<String,ImageData>();
        private final Map<String,Runnable> links = new HashMap<String,Runnable>();
        
        private Factory()
        {
            // No direct public instantiation. Use factory() method instead.
        }
        
        public Factory text( final String text )
        {
            if( text == null )
            {
                throw new IllegalArgumentException(); 
            }
            
            this.text = text;
            
            return this;
        }
        
        public Factory image( final String id,
                              final ImageData image )
        {
            if( id == null )
            {
                throw new IllegalArgumentException();
            }
            
            if( image == null )
            {
                throw new IllegalArgumentException();
            }
            
            this.images.put( id, image );
            
            return this;
        }
        
        public Factory link( final String id,
                             final Runnable operation )
        {
            if( id == null )
            {
                throw new IllegalArgumentException();
            }
            
            if( operation == null )
            {
                throw new IllegalArgumentException();
            }
            
            this.links.put( id, operation );
            
            return this;
        }
        
        public PropertyEditorAssistContribution create()
        {
            if( this.text == null )
            {
                throw new IllegalStateException();
            }
            
            return new PropertyEditorAssistContribution( this.text, this.images, this.links );
        }
    }

}
