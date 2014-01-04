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

package org.eclipse.sapphire.modeling.el;

import java.util.List;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.ImageService;
import org.eclipse.sapphire.Listener;

/**
 * A function that returns the image associated with the current model element. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ImageFunction extends Function
{
    public static ImageFunction create()
    {
        final ImageFunction function = new ImageFunction();
        function.init();
        return function;
    }
    
    @Override
    public String name()
    {
        return "Image";
    }

    @Override
    public FunctionResult evaluate( final FunctionContext context )
    {
        return new FunctionResult( this, context )
        {
            private ImageService imageService;
            private Listener imageServiceListener;
            
            @Override
            protected void init()
            {
                final List<FunctionResult> operands = operands();
                final Element element;
                
                if( operands.isEmpty() )
                {
                    element = ( (ModelElementFunctionContext) context ).element();
                }
                else
                {
                    element = cast( operand( 0 ), Element.class );
                }
                
                this.imageService = element.service( ImageService.class );
                
                if( this.imageService != null )
                {
                    this.imageServiceListener = new Listener()
                    {
                        @Override
                        public void handle( final Event event )
                        {
                            refresh();
                        }
                    };
                    
                    this.imageService.attach( this.imageServiceListener );
                }
            }

            @Override
            protected Object evaluate()
            {
                if( this.imageService == null )
                {
                    return null;
                }
                else
                {
                    return this.imageService.image();
                }
            }

            @Override
            public void dispose()
            {
                super.dispose();
                
                if( this.imageService != null )
                {
                    this.imageService.detach( this.imageServiceListener );
                    
                    this.imageService = null;
                    this.imageServiceListener = null;
                }
            }
        };
    }
    
}
