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

package org.eclipse.sapphire.internal;

import org.eclipse.sapphire.Counter;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Index;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.annotations.NoDuplicates;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class UniqueValueValidationService extends ValidationService
{
    @Text( "Unique {0} required. Another occurrence of \"{1}\" was found" )
    private static LocalizableText message; 
    
    static
    {
        LocalizableText.init( UniqueValueValidationService.class );
    }
    
    private Index<?> index;
    private Listener listener;
    
    @Override
    protected void initValidationService()
    {
        final Value<?> value = context( Value.class );
        final ElementList<?> list = (ElementList<?>) value.element().parent();
        
        this.index = list.index( value.definition() );
        
        this.listener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                refresh();
            }
        };
        
        this.index.attach( this.listener );
    }

    @Override
    protected Status compute()
    {
        Counter.increment( UniqueValueValidationService.class );

        final Value<?> value = context( Value.class );
        final String text = value.text();
        
        if( text != null && this.index.elements( text ).size() > 1 )
        {
            final String label = value.definition().getLabel( true, CapitalizationType.NO_CAPS, false );
            final String msg = message.format( label, text );
            return Status.createErrorStatus( msg );
        }
        
        return Status.createOkStatus();
    }

    @Override
    public void dispose()
    {
        this.index.detach( this.listener );
        
        this.index = null;
        this.listener = null;
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            final Element element = context.find( Element.class );
            return ( property != null && property.hasAnnotation( NoDuplicates.class ) && element.parent() instanceof ElementList );
        }
        
    }
    
}
