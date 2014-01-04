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

package org.eclipse.sapphire.modeling.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.ListPropertyBinding;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyBinding;
import org.eclipse.sapphire.Resource;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValuePropertyBinding;
import org.eclipse.sapphire.modeling.ElementPropertyBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class MemoryResource extends Resource
{
    private final ElementType type;
    
    public MemoryResource( final ElementType type )
    {
        this( type, null );
    }
    
    public MemoryResource( final ElementType type,
                           final MemoryResource parent )
    {
        super( parent );
        this.type = type;
    }
    
    public ElementType type()
    {
        return this.type;
    }
    
    @Override
    public MemoryResource parent()
    {
        return (MemoryResource) super.parent();
    }

    @Override
    public MemoryResource root()
    {
        return (MemoryResource) super.root();
    }
    
    @Override
    protected PropertyBinding createBinding( final Property property )
    {
        PropertyBinding binding = null;
        
        if( property instanceof Value )
        {
            binding = new ValuePropertyBinding()
            {
                private String value;
                
                @Override
                public String read()
                {
                    return this.value;
                }
                
                @Override
                public void write( final String value )
                {
                    this.value = value;
                }
            };
        }
        else if( property instanceof ElementHandle )
        {
            if( property.definition() instanceof ImpliedElementProperty )
            {
                binding = new ElementPropertyBinding()
                {
                    private final MemoryResource element = new MemoryResource( property.definition().getType(), MemoryResource.this );
                    
                    @Override
                    public ElementType type( final Resource resource )
                    {
                        return ( (MemoryResource) resource ).type();
                    }
                    
                    @Override
                    public Resource read()
                    {
                        return this.element;
                    }
                };
            }
            else
            {
                binding = new ElementPropertyBinding()
                {
                    private MemoryResource element;
                    
                    @Override
                    public ElementType type( final Resource resource )
                    {
                        return ( (MemoryResource) resource ).type();
                    }
                    
                    @Override
                    public Resource read()
                    {
                        return this.element;
                    }
    
                    @Override
                    public Resource create( ElementType type )
                    {
                        this.element = new MemoryResource( type, MemoryResource.this );
                        return this.element;
                    }
    
                    @Override
                    public void remove()
                    {
                        this.element = null;
                    }
                };
            }
        }
        else if( property instanceof ElementList )
        {
            binding = new ListPropertyBinding()
            {
                private final List<Resource> list = new ArrayList<Resource>();
                
                @Override
                public ElementType type( final Resource resource )
                {
                    return ( (MemoryResource) resource ).type();
                }
                
                @Override
                public List<Resource> read()
                {
                    return this.list;
                }

                @Override
                public Resource insert( final ElementType type,
                                        final int position )
                {
                    final MemoryResource resource = new MemoryResource( type, MemoryResource.this );
                    this.list.add( position, resource );
                    return resource;
                }
                
                @Override
                public void move( final Resource resource, 
                                  final int position )
                {
                    final int oldPosition = this.list.indexOf( resource );
                    
                    if( position < oldPosition )
                    {
                        this.list.remove( oldPosition );
                        this.list.add( position, resource );
                    }
                    else
                    {
                        this.list.add( position, resource );
                        this.list.remove( oldPosition );
                    }
                }

                @Override
                public void remove( final Resource resource )
                {
                    this.list.remove( resource );
                }
            };
        }
        
        return binding;
    }
    
}
