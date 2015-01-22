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

package org.eclipse.sapphire.internal;

import org.eclipse.sapphire.CollationService;
import org.eclipse.sapphire.Counter;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Index;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Unique;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class UniqueValueValidationService extends ValidationService
{
    @Text( "Multiple occurrence of \"{0}\" were found" )
    private static LocalizableText message; 
    
    @Text( "Multiple occurrence of a missing value were found" )
    private static LocalizableText messageForNull; 
    
    static
    {
        LocalizableText.init( UniqueValueValidationService.class );
    }
    
    private CollationService collationService;
    private Listener collationServiceListener;
    private boolean checkNullValues;
    private Index<?> index;
    private Listener indexListener;
    
    @Override
    protected void initValidationService()
    {
        final Value<?> value = context( Value.class );
        
        this.checkNullValues = ! value.definition().getAnnotation( Unique.class ).ignoreNullValues();
        
        this.collationService = value.service( CollationService.class );
        
        this.collationServiceListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                synchronized( UniqueValueValidationService.this )
                {
                    if( UniqueValueValidationService.this.index != null )
                    {
                        UniqueValueValidationService.this.index.detach( UniqueValueValidationService.this.indexListener );
                        UniqueValueValidationService.this.index = null;
                    }
                }
                
                refresh();
            }
        };
        
        this.collationService.attach( this.collationServiceListener );

        this.indexListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                refresh();
            }
        };
    }

    @Override
    protected Status compute()
    {
        Counter.increment( UniqueValueValidationService.class );

        final Value<?> value = context( Value.class );
        final Index<?> index;
        
        synchronized( this )
        {
            if( this.index == null )
            {
                final ElementList<?> list = (ElementList<?>) value.element().parent();
                
                this.index = list.index( value.definition(), this.collationService.comparator() );
                this.index.attach( this.indexListener );
            }
            
            index = this.index;
        }
        
        final String text = value.text();
        
        if( ( text != null || this.checkNullValues ) && index.elements( text ).size() > 1 )
        {
            final String msg = ( text == null ? messageForNull.text() : message.format( text ) );
            return Status.createErrorStatus( msg );
        }
        
        return Status.createOkStatus();
    }

    @Override
    public void dispose()
    {
        if( this.collationService != null )
        {
            this.collationService.detach( this.collationServiceListener );
            this.collationService = null;
            this.collationServiceListener = null;
        }
        
        if( this.index != null )
        {
            this.index.detach( this.indexListener );
            this.index = null;
        }
        
        this.indexListener = null;
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            final Element element = context.find( Element.class );
            return ( property != null && property.hasAnnotation( Unique.class ) && element.parent() instanceof ElementList );
        }
        
    }
    
}
