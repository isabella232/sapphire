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

package org.eclipse.sapphire.ui;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.modeling.EditFailedException;
import org.eclipse.sapphire.modeling.el.FunctionContext;
import org.eclipse.sapphire.modeling.el.ModelElementFunctionContext;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.def.SapphireActionType;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class SapphireActionHandler extends SapphireActionSystemPart
{
    private SapphireAction action;

    public void init( final SapphireAction action,
                      final ActionHandlerDef def )
    {
        this.action = action;
        
        super.init( def );
        
        if( getId() == null )
        {
            final StringBuilder buf = new StringBuilder();
            buf.append( getClass().getName() );
            buf.append( '#' );
            buf.append( System.identityHashCode( this ) );
            
            setId( buf.toString() );
        }
        
        if( getLabel() == null )
        {
            setLabel( this.action.getLabel() );
        }
    }
    
    protected final FunctionContext initFunctionContext()
    {
        final ISapphirePart part = getPart();
        return new ModelElementFunctionContext( part.getLocalModelElement(), part.definition().adapt( LocalizationService.class ) );
    }
    
    public final SapphireAction getAction()
    {
        return this.action;
    }
    
    public final ISapphirePart getPart()
    {
        return this.action.getPart();
    }
    
    public final String getContext()
    {
        return this.action.getContext();
    }
    
    public Element getModelElement()
    {
        return getPart().getModelElement();
    }
    
    public final void execute( final Presentation context )
    {
        if( getAction().getType() == SapphireActionType.TOGGLE )
        {
            setChecked( ! isChecked() );
        }
        
        broadcast( new PreExecuteEvent() );
        
        Object result = null;
        
        try
        {
            result = run( context );
        }
        catch( Exception e )
        {
            // Log this exception unless the cause is EditFailedException. These exception
            // are the result of the user declining a particular action that is necessary
            // before the edit can happen (such as making a file writable).
            
            final EditFailedException editFailedException = EditFailedException.findAsCause( e );
            
            if( editFailedException == null )
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
        }
        
        broadcast( new PostExecuteEvent( result ) );
    }
    
    protected abstract Object run( Presentation context );
    
    public static class PreExecuteEvent extends Event
    {
    }

    public static class PostExecuteEvent extends Event
    {
        private final Object result;
        
        public PostExecuteEvent( final Object result )
        {
            this.result = result;
        }
        
        public Object getResult()
        {
            return this.result;
        }
    }
    
}