/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.services.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementDisposedEvent;
import org.eclipse.sapphire.modeling.ModelElementListener;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.PossibleValuesService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PossibleValuesServiceFactory

    extends ServiceFactory
    
{
    @Override
    public boolean applicable( final ServiceContext context,
                               final Class<? extends Service> service )
    {
        final ValueProperty property = context.find( ValueProperty.class );
        return ( property != null && property.hasAnnotation( PossibleValues.class ) );
    }

    @Override
    public Service create( final ServiceContext context,
                           final Class<? extends Service> service )
    {
        PossibleValuesService svc = null;
        final PossibleValues annotation = context.find( ValueProperty.class ).getAnnotation( PossibleValues.class );
        
        if( annotation != null )
        {
            if( annotation.values().length > 0 )
            {
                svc = new PossibleValuesServiceStatic( annotation.values(), annotation.invalidValueMessage(),
                                                       annotation.invalidValueSeverity(), annotation.caseSensitive() );
            }
            
            if( svc == null && annotation.property().length() > 0 )
            {
                svc = new PossibleValuesServiceFromModel( new ModelPath( annotation.property() ),
                                                          annotation.invalidValueMessage(), annotation.invalidValueSeverity(), 
                                                          annotation.caseSensitive() );
            }
        }
        
        return svc;
    }
    
    private static abstract class PossibleValuesServiceExt extends PossibleValuesService
    {
        private final String invalidValueMessageTemplate;
        private final Status.Severity invalidValueSeverity;
        private final boolean caseSensitive;
        
        public PossibleValuesServiceExt( final String invalidValueMessageTemplate,
                                         final Status.Severity invalidValueSeverity,
                                         final boolean caseSensitive )
        {
            if( invalidValueMessageTemplate == null || invalidValueMessageTemplate.length() == 0 )
            {
                this.invalidValueMessageTemplate = null;
            }
            else
            {
                this.invalidValueMessageTemplate = invalidValueMessageTemplate;
            }

            this.invalidValueSeverity = invalidValueSeverity;
            this.caseSensitive = caseSensitive;
        }
    
        @Override
        public String getInvalidValueMessage( final String invalidValue )
        {
            if( this.invalidValueMessageTemplate == null )
            {
                return super.getInvalidValueMessage( invalidValue );
            }

            return NLS.bind( this.invalidValueMessageTemplate, invalidValue );
        }

        @Override
        public Status.Severity getInvalidValueSeverity( final String invalidValue )
        {
            return this.invalidValueSeverity;
        }

        @Override
        public boolean isCaseSensitive()
        {
            return this.caseSensitive;
        }
    }
    
    private static final class PossibleValuesServiceStatic extends PossibleValuesServiceExt
    {
        private final List<String> values;
        
        public PossibleValuesServiceStatic( final String[] values,
                                            final String invalidValueMessageTemplate,
                                            final Status.Severity invalidValueSeverity,
                                            final boolean caseSensitive )
        {
            super( invalidValueMessageTemplate, invalidValueSeverity, caseSensitive );
            
            final List<String> list = new ArrayList<String>();
            
            for( String item : values )
            {
                if( item != null )
                {
                    list.add( item );
                }
            }
            
            this.values = Collections.unmodifiableList( list );
        }
    
        @Override
        protected void fillPossibleValues( final SortedSet<String> values )
        {
            values.addAll( this.values );
        }
    }
    
    private static final class PossibleValuesServiceFromModel extends PossibleValuesServiceExt
    {
        private final ModelPath path;
        private Set<String> values;
        
        public PossibleValuesServiceFromModel( final ModelPath path,
                                               final String invalidValueMessageTemplate,
                                               final Status.Severity invalidValueSeverity,
                                               final boolean caseSensitive )
        {
            super( invalidValueMessageTemplate, invalidValueSeverity, caseSensitive );
            
            this.path = path;
            this.values = Collections.emptySet();
        }
        
        @Override
        protected void init()
        {
            super.init();
            
            final IModelElement element = context( IModelElement.class );
            
            final ModelPropertyListener listener = new ModelPropertyListener()
            {
                @Override
                public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
                {
                    refresh();
                }
            };
            
            element.addListener( listener, this.path );
            
            element.addListener
            (
                new ModelElementListener()
                {
                    @Override
                    public void handleElementDisposedEvent( final ModelElementDisposedEvent event )
                    {
                        element.removeListener( listener, PossibleValuesServiceFromModel.this.path );
                    }
                }
            );
        }
    
        @Override
        protected void fillPossibleValues( final SortedSet<String> values )
        {
            values.addAll( this.values );
        }
        
        private void refresh()
        {
            final Set<String> newValues = context( IModelElement.class ).read( this.path );
            
            if( ! this.values.equals( newValues ) )
            {
                this.values = Collections.unmodifiableSet( newValues );
                broadcast();
            }
        }
    }
    
}
