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

package org.eclipse.sapphire.ui.internal;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.ui.PartValidationEvent;
import org.eclipse.sapphire.ui.SapphirePart;

/**
 * Returns the validation result of a part.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PartValidationFunction extends Function
{
    @Override
    public String name()
    {
        return "Validation";
    }
    
    @Override
    public final FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            private SapphirePart part;
            private Listener listener;
            
            @Override
            protected Object evaluate()
            {
                SapphirePart p = null;
                
                try
                {
                    p = operand( 0, SapphirePart.class, false );
                }
                finally
                {
                    if( this.part != p )
                    {
                        if( this.part != null )
                        {
                            this.part.detach( this.listener );
                        }
                        
                        this.part = p;
                   
                        if( this.part != null )
                        {
                            if( this.listener == null )
                            {
                                this.listener = new FilteredListener<PartValidationEvent>()
                                {
                                    @Override
                                    protected void handleTypedEvent( final PartValidationEvent event )
                                    {
                                        refresh();
                                    }
                                };
                            }
                            
                            this.part.attach( this.listener );
                        }
                    }
                }
                
                if( p == null )
                {
                    throw new IllegalStateException();
                }
                
                return p.validation();
            }
            
            @Override
            public void dispose()
            {
                super.dispose();
                
                if( this.part != null )
                {
                    this.part.detach( this.listener );
                    this.part = null;
                }
                
                this.listener = null;
            }
        };
    }
    
}
