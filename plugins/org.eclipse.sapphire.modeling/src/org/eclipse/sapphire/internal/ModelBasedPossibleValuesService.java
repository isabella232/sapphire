/******************************************************************************
 * Copyright (c) 2015 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [384683] PossibleValuesServiceFromModel does not load values initially
 ******************************************************************************/

package org.eclipse.sapphire.internal;

import java.util.Set;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PossibleValues;
import org.eclipse.sapphire.PossibleValuesService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.PropertyVisitor;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * {@link PossibleValuesService} implementation that derives its behavior from @{@link PossibleValues} annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public final class ModelBasedPossibleValuesService extends PossibleValuesService
{
    private ModelPath path;
    private Listener listener;
    
    @Override
    protected void initPossibleValuesService()
    {
        final Property property = context( Property.class );
        final Element element = property.element();
        
        final PossibleValues a = property.definition().getAnnotation( PossibleValues.class );
        
        this.path = new ModelPath( a.property() );

        final String invalidValueMessage = a.invalidValueMessage();
        
        if( invalidValueMessage.length() > 0 )
        {
            this.invalidValueMessage = invalidValueMessage;
        }
        
        this.invalidValueSeverity = a.invalidValueSeverity();
        this.ordered = a.ordered();
        
        this.listener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                if( ! property.disposed() )
                {
                    refresh();
                }
            }
        };
        
        try
        {
            element.attach( this.listener, this.path );
        }
        catch( IllegalArgumentException e )
        {
            // Ignore exceptions caused by an invalid model path. This can happen when the element is instantiated
            // outside its typical model context. This service is expected to gracefully degrade by returning an
            // empty set of possible values.
        }
    }

    @Override
    protected void compute( final Set<String> values )
    {
        final Element element = context( Element.class );
        
        if( ! element.disposed() )
        {
            element.visit
            (
                this.path,
                new PropertyVisitor()
                {
                    @Override
                    public boolean visit( final Value<?> property )
                    {
                        final String text = property.text();
                        
                        if( text != null )
                        {
                            values.add( text );
                        }
                        
                        return true;
                    }
                }
            );
        }
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        this.path = null;
        
        if( this.listener != null )
        {
            context( Element.class ).detach( this.listener, ModelBasedPossibleValuesService.this.path );
            this.listener = null;
        }
    }

    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final Property property = context.find( Property.class );
            
            if( property instanceof Value || property instanceof ElementList )
            {
                final PossibleValues possibleValuesAnnotation = property.definition().getAnnotation( PossibleValues.class );
                
                if( possibleValuesAnnotation != null && possibleValuesAnnotation.property().length() > 0 )
                {
                    return true;
                }
            }
            
            return false;
        }
    }
    
}
