/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.samples.gallery.internal;

import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.samples.gallery.Circle;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;
import org.eclipse.sapphire.services.ValueSerializationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class CircleSerializationService extends ValueSerializationService
{
    @Override
    protected Object decodeFromString( final String value )
    {
        final String[] segments = value.split( "," );
        
        if( segments.length == 3 )
        {
            try
            {
                final int x = Integer.parseInt( segments[ 0 ].trim() );
                final int y = Integer.parseInt( segments[ 1 ].trim() );
                final int radius = Integer.parseInt( segments[ 2 ].trim() );
                
                return new Circle( x, y, radius );
            }
            catch( NumberFormatException e )
            {
                // No need to propagate the exception. A null return from this method
                // will cause the user to be notified of malformed value.
            }
        }
        
        return null;
    }
    
    @Override
    public String encode( final Object value )
    {
        // The default implementation delegates to the object's toString() method.
        // You do not need to override this method if your object's toString()
        // implementation matches the serialization format that you want to use.
        
        if( value != null )
        {
            final Circle circle = (Circle) value;
            final StringBuilder buf = new StringBuilder();
    
            buf.append( circle.x() );
            buf.append( ", " );
            buf.append( circle.y() );
            buf.append( ", " );
            buf.append( circle.radius() );
            
            return buf.toString();
        }
        
        return null;
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ValueProperty property = context.find( ValueProperty.class ); 
            return ( property != null && property.isOfType( Circle.class ) );
        }
    
        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new CircleSerializationService();
        }
    }

}
