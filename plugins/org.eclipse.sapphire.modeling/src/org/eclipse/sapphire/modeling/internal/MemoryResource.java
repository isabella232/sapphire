/******************************************************************************
 * Copyright (c) 2013 Oracle
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

import org.eclipse.sapphire.modeling.BindingImpl;
import org.eclipse.sapphire.modeling.ElementBindingImpl;
import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListBindingImpl;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Resource;
import org.eclipse.sapphire.modeling.ValueBindingImpl;
import org.eclipse.sapphire.modeling.ValueProperty;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class MemoryResource

    extends Resource
    
{
    private static final String[] EMPTY_PARAMS = new String[ 0 ];
    
    private final ModelElementType type;
    
    public MemoryResource( final ModelElementType type )
    {
        this( type, null );
    }
    
    public MemoryResource( final ModelElementType type,
                           final MemoryResource parent )
    {
        super( parent );
        this.type = type;
    }
    
    public ModelElementType type()
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
    protected BindingImpl createBinding( final ModelProperty property )
    {
        BindingImpl binding = null;
        
        if( property instanceof ValueProperty )
        {
            binding = new ValueBindingImpl()
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
        else if( property instanceof ImpliedElementProperty )
        {
            binding = new ElementBindingImpl()
            {
                private final MemoryResource element = new MemoryResource( property.getType(), MemoryResource.this );
                
                @Override
                public ModelElementType type( final Resource resource )
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
        else if( property instanceof ElementProperty )
        {
            binding = new ElementBindingImpl()
            {
                private MemoryResource element;
                
                @Override
                public ModelElementType type( final Resource resource )
                {
                    return ( (MemoryResource) resource ).type();
                }
                
                @Override
                public Resource read()
                {
                    return this.element;
                }

                @Override
                public Resource create( ModelElementType type )
                {
                    this.element = new MemoryResource( type, MemoryResource.this );
                    return this.element;
                }

                @Override
                public void remove()
                {
                    this.element = null;
                }

                @Override
                public boolean removable()
                {
                    return true;
                }
            };
        }
        else if( property instanceof ListProperty )
        {
            binding = new ListBindingImpl()
            {
                private final List<Resource> list = new ArrayList<Resource>();
                
                @Override
                public ModelElementType type( final Resource resource )
                {
                    return ( (MemoryResource) resource ).type();
                }
                
                @Override
                public List<Resource> read()
                {
                    return this.list;
                }

                @Override
                public Resource insert( final ModelElementType type,
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
        
        if( binding != null )
        {
            binding.init( element(), property, EMPTY_PARAMS );
        }
        
        return binding;
    }
    
}
