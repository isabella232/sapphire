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

package org.eclipse.sapphire.ui;

import java.util.List;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.el.AndFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.ui.def.LineSeparatorDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class SeparatorPart extends FormComponentPart
{
    @Override
    protected Function initVisibleWhenFunction()
    {
        final ISapphirePart parent = getParentPart();
        
        if( parent instanceof FormPart )
        {
            final FormPart form = (FormPart) parent;
            
            if( ! ( this instanceof LineSeparatorPart && ( (LineSeparatorDef) definition() ).getLabel().getText() != null ) )
            {
                /*
                 * A separator is considered not necessary if
                 * 
                 * 1. it is the first visible part of a form, or
                 * 2. it is the last visible part of a form, or
                 * 3. it is immediately preceded by another separator in the list of visible parts.
                 */
                
                return AndFunction.create
                (
                    super.initVisibleWhenFunction(),
                    new Function()
                    {
                        @Override
                        public String name()
                        {
                            return "NecessarySeparator";
                        }
            
                        @Override
                        public FunctionResult evaluate( final FunctionContext context )
                        {
                            return new FunctionResult( this, context )
                            {
                                @Override
                                protected void init()
                                {
                                    final Listener listener = new FilteredListener<PartVisibilityEvent>()
                                    {
                                        @Override
                                        protected void handleTypedEvent( final PartVisibilityEvent event )
                                        {
                                            refresh();
                                        }
                                    };
                                    
                                    final List<? extends SapphirePart> parts = form.getChildParts();
                                    
                                    if( parts == null )
                                    {
                                        form.attach
                                        (
                                            new FilteredListener<PartInitializationEvent>()
                                            {
                                                @Override
                                                protected void handleTypedEvent( final PartInitializationEvent event )
                                                {
                                                    for( SapphirePart part : form.getChildParts() )
                                                    {
                                                        part.attach( listener );
                                                    }
                                                    
                                                    form.detach( this );
                                                    
                                                    refresh();
                                                }
                                            }
                                        );
                                    }
                                    else
                                    {
                                        for( SapphirePart part : parts )
                                        {
                                            part.attach( listener );
                                        }
                                    }
                                }
            
                                @Override
                                protected Object evaluate()
                                {
                                    final List<? extends SapphirePart> parts = form.getChildParts();
                                    
                                    if( parts != null )
                                    {
                                        SapphirePart previous = null;
                                        
                                        for( SapphirePart part : parts )
                                        {
                                            if( part == SeparatorPart.this )
                                            {
                                                if( previous == null || previous instanceof SeparatorPart )
                                                {
                                                    return false;
                                                }
                                                
                                                previous = part;
                                            }
                                            else if( part.visible() )
                                            {
                                                previous = part;
                                            }
                                        }
                                        
                                        if( previous == SeparatorPart.this )
                                        {
                                            return false;
                                        }
                                    }
                                    
                                    return true;
                                }
                            };
                        }
                    }
                );
            }
        }
        
        return super.initVisibleWhenFunction();
    }
    
}
