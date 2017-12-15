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

package org.eclipse.sapphire.modeling;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class LayeredElementBindingImpl

    extends ElementBindingImpl
    
{
    private Object object;
    private Resource resource;
    
    @Override
    
    public final Resource read()
    {
        final Object newObject = readUnderlyingObject();
        
        if( this.object != newObject )
        {
            this.object = newObject;
            this.resource = ( this.object == null ? null : createResource( this.object ) );
        }
        
        return this.resource;
    }
    
    protected abstract Object readUnderlyingObject();
    
    @Override
    
    public Resource create( final ModelElementType type )
    {
        this.object = createUnderlyingObject( type );
        this.resource = createResource( this.object );
        
        return this.resource;
    }
    
    protected Object createUnderlyingObject( final ModelElementType type )
    {
        throw new UnsupportedOperationException();
    }
    
    protected abstract Resource createResource( Object obj );
    
}
