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

package org.eclipse.sapphire.tests.binding.list;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.Counter;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.LayeredListPropertyBinding;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyBinding;
import org.eclipse.sapphire.Resource;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TestResource extends Resource
{
    private final List<Object> list = new ArrayList<Object>( 2 );
    private final Counter readUnderlyingListCounter = new Counter();
    
    public TestResource()
    {
        super( null );
    }
    
    public List<Object> list()
    {
        return this.list;
    }
    
    public Counter getReadUnderlyingListCounter()
    {
        return this.readUnderlyingListCounter;
    }

    @Override
    protected PropertyBinding createBinding( final Property property )
    {
        if( property.definition() == TestElement.PROP_LIST )
        {
            return new LayeredListPropertyBinding()
            {
                @Override
                public ElementType type( final Resource resource )
                {
                    return Element.TYPE;
                }
                
                @Override
                protected Resource resource( final Object obj )
                {
                    return new ListEntryResource( TestResource.this, obj );
                }
                
                @Override
                protected List<?> readUnderlyingList()
                {
                    TestResource.this.readUnderlyingListCounter.increment();
                    return TestResource.this.list;
                }

                @Override
                protected Object insertUnderlyingObject( final ElementType type, final int position )
                {
                    final Object object = new Object();
                    TestResource.this.list.add( position, object );
                    return object;
                }

                @Override
                public void move( final Resource resource, final int position )
                {
                    final Object object = ( (ListEntryResource) resource ).object();
                    
                    final int oldPosition = TestResource.this.list.indexOf( object );
                    
                    if( position < oldPosition )
                    {
                        TestResource.this.list.remove( oldPosition );
                        TestResource.this.list.add( position, object );
                    }
                    else
                    {
                        TestResource.this.list.add( position, object );
                        TestResource.this.list.remove( oldPosition );
                    }
                }

                @Override
                public void remove( final Resource resource )
                {
                    final Object object = ( (ListEntryResource) resource ).object();
                    TestResource.this.list.remove( object );
                }
            };
        }
        
        throw new IllegalStateException();
    }
    
    private static final class ListEntryResource extends Resource
    {
        private final Object object;
        
        public ListEntryResource( final Resource parent, final Object object )
        {
            super( parent );
            
            if( object == null )
            {
                throw new IllegalArgumentException();
            }
            
            this.object = object;
        }
        
        public Object object()
        {
            return this.object;
        }

        @Override
        protected PropertyBinding createBinding( final Property property )
        {
            throw new IllegalStateException();
        }
    }

}
