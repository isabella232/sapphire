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

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.modeling.el.AndFunction;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.SapphirePart;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class SeparatorPart extends FormComponentPart
{
    @Override
    protected Function initVisibleWhenFunction()
    {
        final ISapphirePart parent = parent();
        
        if( parent instanceof FormPart )
        {
            final FormPart form = (FormPart) parent;
            
            if( ! ( this instanceof LineSeparatorPart && ( (LineSeparatorDef) definition() ).getLabel().text() != null ) )
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
                                    form.attach
                                    (
                                        new FilteredListener<FormPart.VisibleChildrenEvent>()
                                        {
                                            @Override
                                            protected void handleTypedEvent( final FormPart.VisibleChildrenEvent event )
                                            {
                                                refresh();
                                            }
                                        }
                                    );
                                }
            
                                @Override
                                protected Object evaluate()
                                {
                                    SapphirePart previous = null;
                                    
                                    for( final SapphirePart part : form.children().all() )
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
