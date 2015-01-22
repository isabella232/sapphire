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

import static org.eclipse.sapphire.modeling.util.MiscUtil.equal;

import java.util.Set;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementReferenceService;
import org.eclipse.sapphire.ElementReferenceService.SourceEvent;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PossibleValuesService;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.ReferenceValue;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * {@link PossibleValuesService} implementation that derives its behavior from {@link ElementReferenceService}.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ElementReferencePossibleValuesService extends PossibleValuesService
{
    public ElementReferenceService elementReferenceService;
    public Listener elementReferenceServiceListener;
    public ElementList<?> list;
    public String key;
    public Listener listListener;
    
    @Override
    protected void initPossibleValuesService()
    {
        this.elementReferenceService = context( ReferenceValue.class ).service( ElementReferenceService.class );
        
        this.elementReferenceServiceListener = new FilteredListener<SourceEvent>()
        {
            @Override
            protected void handleTypedEvent( final SourceEvent event )
            {
                refresh();
            }
        };
        
        this.elementReferenceService.attach( this.elementReferenceServiceListener );
        
        this.listListener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( PropertyContentEvent event )
            {
                refresh();
            }
        };
    }
    
    @Override
    protected void compute( final Set<String> values )
    {
        final ElementList<?> list = this.elementReferenceService.list();
        final String key = this.elementReferenceService.key();
        
        if( this.list != list || ! equal( this.key, key ) )
        {
            if( this.list != null )
            {
                this.list.detach( this.listListener, this.key );
                this.list = null;
            }

            this.list = list;
            this.key = key;
            
            if( this.list != null )
            {
                this.list.attach( this.listListener, this.key );
            }
        }
        
        if( this.list != null )
        {
            for( final Element element : this.list )
            {
                final String text = ( (Value<?>) element.property( this.key ) ).text();
                
                if( text != null )
                {
                    values.add( text );
                }
            }
        }
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        this.elementReferenceService.detach( this.elementReferenceServiceListener );
        this.elementReferenceService = null;
        this.elementReferenceServiceListener = null;
        
        if( this.list != null )
        {
            if( ! this.list.disposed() )
            {
                this.list.detach( this.listListener, this.key );
            }
            
            this.list = null;
        }
        
        this.key = null;
        this.listListener = null;
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final ReferenceValue<?,?> ref = context.find( ReferenceValue.class );
            return ( ref != null ) && ( ref.service( ElementReferenceService.class ) != null );
        }
    }

}
