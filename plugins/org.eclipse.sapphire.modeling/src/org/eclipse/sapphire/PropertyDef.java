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

package org.eclipse.sapphire;

import static org.eclipse.sapphire.modeling.localization.LocalizationUtil.transformCamelCaseToLabel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.sapphire.modeling.EditFailedException;
import org.eclipse.sapphire.modeling.ModelMetadataItem;
import org.eclipse.sapphire.modeling.annotations.Derived;
import org.eclipse.sapphire.modeling.annotations.Listeners;
import org.eclipse.sapphire.modeling.annotations.ReadOnly;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.internal.PropertyMetaModelServiceContext;
import org.eclipse.sapphire.util.ListFactory;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class PropertyDef extends ModelMetadataItem
{
    public static final String PROPERTY_FIELD_PREFIX = "PROP_"; //$NON-NLS-1$
    
    private final ElementType modelElementType;
    private final String propertyName;
    private final PropertyDef baseProperty;

    private final Class<?> typeClass;
    private final ElementType type;
    
    private List<Listener> listeners;
    private ServiceContext serviceContext;
    
    public PropertyDef( final ElementType modelElementType,
                        final String propertyName,
                        final PropertyDef baseProperty )
    {
        this.modelElementType = modelElementType;
        this.propertyName = propertyName;
        this.baseProperty = baseProperty;
        
        try
        {
            final Type typeAnnotation = getAnnotation( Type.class );
            
            if( typeAnnotation == null )
            {
                if( this instanceof ValueProperty )
                {
                    this.typeClass = String.class;
                }
                else
                {
                    final String message
                        = "Property \"" + propertyName + "\" of " + this.modelElementType.getModelElementClass().getClass()
                          + " is missing the required Type annotation.";
                    
                    throw new IllegalStateException( message );
                }
            }
            else
            {
                this.typeClass = typeAnnotation.base();
            }
        }
        catch( RuntimeException e )
        {
            Sapphire.service( LoggingService.class ).log( e );
            throw e;
        }
        
        if( this instanceof ValueProperty || this instanceof TransientProperty )
        {
            this.type = null;
        }
        else
        {
            this.type = ElementType.read( this.typeClass );
        }
    }
    
    public ElementType getModelElementType()
    {
        return this.modelElementType;
    }
    
    public String name()
    {
        return this.propertyName;
    }
    
    public final Class<?> getTypeClass()
    {
        return this.typeClass;
    }
    
    public final ElementType getType()
    {
        return this.type;
    }
    
    public final boolean isOfType( final Class<?> type )
    {
        return type.isAssignableFrom( getTypeClass() );        
    }
    
    @Override
    public PropertyDef getBase()
    {
        return this.baseProperty;
    }
    
    @Override
    protected void initAnnotations( final ListFactory<Annotation> annotations )
    {
        Field propField = null;
        
        for( Field field : this.modelElementType.getModelElementClass().getFields() )
        {
            final String fieldName = field.getName();
            
            if( fieldName.startsWith( PROPERTY_FIELD_PREFIX ) )
            {
                final String propName = convertFieldNameToPropertyName( fieldName );
                
                if( this.propertyName.equalsIgnoreCase( propName ) )
                {
                    propField = field;
                    break;
                }
            }
        }
        
        if( propField != null )
        {
            annotations.add( propField.getDeclaredAnnotations() );
        }
    }

    @Override
    public <A extends Annotation> List<A> getAnnotations( final Class<A> type )
    {
        final ListFactory<A> annotationsListFactory = ListFactory.start();
        
        annotationsListFactory.add( super.getAnnotations( type ) );
        
        if( this.baseProperty != null )
        {
            annotationsListFactory.add( this.baseProperty.getAnnotations( type ) );
        }
        
        return annotationsListFactory.result();
    }
    
    @Override
    public <A extends Annotation> A getAnnotation( final Class<A> type )
    {
        A annotation = super.getAnnotation( type );
        
        if( annotation == null && this.baseProperty != null )
        {
            annotation = this.baseProperty.getAnnotation( type );
        }
        
        return annotation;
    }
    
    @Override
    protected final String getDefaultLabel()
    {
        return transformCamelCaseToLabel( this.propertyName );
    }
    
    @Override
    public final LocalizationService getLocalizationService()
    {
        return this.modelElementType.getLocalizationService();
    }

    public final boolean isReadOnly()
    {
        return hasAnnotation( ReadOnly.class ) || isDerived();
    }
    
    public final boolean isDerived()
    {
        return hasAnnotation( Derived.class );
    }
    
    protected RuntimeException convertReflectiveInvocationException( final Exception e )
    {
        final Throwable cause = e.getCause();
        
        if( cause instanceof EditFailedException )
        {
            return (EditFailedException) cause;
        }
        
        return new RuntimeException( e );
    }
    
    private static String convertFieldNameToPropertyName( final String fieldName )
    {
        if( fieldName.startsWith( PROPERTY_FIELD_PREFIX ) )
        {
            final StringBuilder buffer = new StringBuilder();
            
            for( int i = PROPERTY_FIELD_PREFIX.length(); i < fieldName.length(); i++ )
            {
                final char ch = fieldName.charAt( i );
                
                if( ch != '_' )
                {
                    buffer.append( ch );
                }
            }
            
            return buffer.toString();
        }
        else
        {
            return null;
        }
    }
    
    public synchronized List<Listener> listeners()
    {
        if( this.listeners == null )
        {
            final ListFactory<Listener> listenersListFactory = ListFactory.start();
            final Listeners listenersAnnotation = getAnnotation( Listeners.class );
            
            if( listenersAnnotation != null )
            {
                for( Class<? extends Listener> cl : listenersAnnotation.value() )
                {
                    try
                    {
                        listenersListFactory.add( cl.newInstance() );
                    }
                    catch( Exception e )
                    {
                        Sapphire.service( LoggingService.class ).log( e );
                    }
                }
            }
            
            this.listeners = listenersListFactory.result();
        }
        
        return this.listeners;
    }

    /**
     * Returns the service of the specified type from the property metamodel service context.
     * 
     * <p>Service Context: <b>Sapphire.Property.MetaModel</b></p>
     * 
     * @param <S> the type of the service
     * @param type the type of the service
     * @return the service or <code>null</code> if not available
     */
    
    public <S extends Service> S service( final Class<S> type )
    {
        return services().service( type );
    }

    /**
     * Returns services of the specified type from the property metamodel service context.
     * 
     * <p>Service Context: <b>Sapphire.Property.MetaModel</b></p>
     * 
     * @param <S> the type of the service
     * @param type the type of the service
     * @return the list of services or an empty list if none are available
     */
    
    public <S extends Service> List<S> services( final Class<S> type )
    {
        return services().services( type );
    }

    /**
     * Returns the property metamodel service context.
     * 
     * <p>Service Context: <b>Sapphire.Property.MetaModel</b></p>
     * 
     * @return the property metamodel service context
     */
    
    public synchronized ServiceContext services()
    {
        if( this.serviceContext == null )
        {
            this.serviceContext = new PropertyMetaModelServiceContext( this );
        }
        
        return this.serviceContext;
    }
    
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( this.modelElementType.getModelElementClass().getName() );
        buf.append( '#' );
        buf.append( this.propertyName );
        
        return buf.toString();
    }
    
}
