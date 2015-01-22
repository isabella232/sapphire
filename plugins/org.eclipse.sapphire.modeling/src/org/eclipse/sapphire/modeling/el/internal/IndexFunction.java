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

package org.eclipse.sapphire.modeling.el.internal;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionException;
import org.eclipse.sapphire.modeling.el.FunctionResult;

/**
 * Determines the index of a model element within its parent list. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class IndexFunction extends Function
{
    @Text( "Cannot determine index if parent is not a list." )
    private static LocalizableText parentNotListMessage;
    
    static
    {
        LocalizableText.init( IndexFunction.class );
    }

    @Override
    public String name()
    {
        return "Index";
    }

    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            private ElementList<?> list;
            private Listener listener;
            
            @Override
            protected Object evaluate()
            {
                final Element element = operand( 0, Element.class, false );
                final Property parent = element.parent();
                
                if( parent instanceof ElementList )
                {
                    final ElementList<?> list = (ElementList<?>) parent;
                    
                    if( this.list != list )
                    {
                        if( this.listener == null )
                        {
                            this.listener = new FilteredListener<PropertyContentEvent>()
                            {
                                @Override
                                protected void handleTypedEvent( final PropertyContentEvent event )
                                {
                                    refresh();
                                }
                            };
                        }
                        else if( this.list != null )
                        {
                            this.list.detach( this.listener );
                        }
                        
                        this.list = list;
                        this.list.attach( this.listener );
                    }
                    
                    for( int i = 0, n = list.size(); i < n; i++ )
                    {
                        if( list.get( i ) == element )
                        {
                            return i;
                        }
                    }
                    
                    return -1;
                }
                else
                {
                    if( this.list != null )
                    {
                        this.list.detach( this.listener );
                        this.list = null;
                    }
                    
                    throw new FunctionException( parentNotListMessage.text() );
                }
            }

            @Override
            public void dispose()
            {
                super.dispose();
                
                if( this.list != null )
                {
                    this.list.detach( this.listener );
                    this.list = null;
                }
                
                this.listener = null;
            }
        };
    }
    
}
