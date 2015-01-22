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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionException;
import org.eclipse.sapphire.modeling.el.FunctionResult;

/**
 * Determines the size of a collection, a map, an array or a string.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SizeFunction extends Function
{
    @Text( "Function Size cannot be applied to a {0} object." )
    private static LocalizableText unsupportedTypeMessage;
    
    static
    {
        LocalizableText.init( SizeFunction.class );
    }

    @Override
    public String name()
    {
        return "Size";
    }
    
    @Override
    public final FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            private Object operand;
            private Listener listener;
            
            @Override
            protected Object evaluate()
            {
                Object operand = null;
                
                try
                {
                    operand = operand( 0, Object.class, false );
                }
                finally
                {
                    if( this.operand != operand )
                    {
                        if( this.operand instanceof Property && this.listener != null )
                        {
                            ( (Property) this.operand ).detach( this.listener );
                        }
                        
                        this.operand = operand;
                        
                        if( this.operand instanceof Property )
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
                            
                            ( (Property) this.operand ).attach( this.listener );
                        }
                    }
                }
                
                if( this.operand instanceof Collection )
                {
                    return ( (Collection<?>) this.operand ).size();
                }
                else if( this.operand instanceof Map<?,?> )
                {
                    return ( (Map<?,?>) this.operand ).size();
                }
                else if( this.operand.getClass().isArray() )
                {
                    return Array.getLength( this.operand );
                }
                else if( this.operand instanceof Value )
                {
                    final String text = ( (Value<?>) this.operand ).text();
                    return ( text == null ? 0 : text.length() );
                }
                else if( this.operand instanceof String )
                {
                    return ( (String) this.operand ).length();
                }

                final String msg = unsupportedTypeMessage.format( this.operand.getClass().getName() );
                throw new FunctionException( msg );
            }
            
            @Override
            public void dispose()
            {
                super.dispose();
                
                if( this.operand instanceof ElementList )
                {
                    ( (ElementList<?>) this.operand ).detach( this.listener );
                }
                
                this.operand = null;
                this.listener = null;
            }
        };
    }

}
