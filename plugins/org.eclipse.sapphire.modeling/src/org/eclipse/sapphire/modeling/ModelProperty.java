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

package org.eclipse.sapphire.modeling;

import static org.eclipse.sapphire.modeling.localization.LocalizationUtil.transformCamelCaseToLabel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.sapphire.modeling.annotations.Derived;
import org.eclipse.sapphire.modeling.annotations.PropertyListeners;
import org.eclipse.sapphire.modeling.annotations.ReadOnly;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.internal.PropertyMetaModelServiceContext;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ModelProperty extends ModelMetadataItem
{
    public static final String PROPERTY_FIELD_PREFIX = "PROP_"; //$NON-NLS-1$
    
    private final ModelElementType modelElementType;
    private final String propertyName;
    private final ModelProperty baseProperty;

    private final Class<?> typeClass;
    private final ModelElementType type;
    
    private final Map<Class<? extends Annotation>,Annotation> annotations;
    private Set<ModelPropertyListener> listeners;
    private Set<ModelPropertyListener> listenersReadOnly;
    private ServiceContext serviceContext;
    
    public ModelProperty( final ModelElementType modelElementType,
                          final String propertyName,
                          final ModelProperty baseProperty )
    {
        try
        {
            this.modelElementType = modelElementType;
            this.propertyName = propertyName;
            this.baseProperty = baseProperty;
            this.annotations = new HashMap<Class<? extends Annotation>,Annotation>();
            
            gatherAnnotations();
            
            final PropertyListeners propertyListenersAnnotation = getAnnotation( PropertyListeners.class );
            
            if( propertyListenersAnnotation != null )
            {
                for( Class<? extends ModelPropertyListener> cl : propertyListenersAnnotation.value() )
                {
                    try
                    {
                        addListener( cl.newInstance() );
                    }
                    catch( Exception e )
                    {
                        LoggingService.log( e );
                    }
                }
            }
        }
        catch( RuntimeException e )
        {
            LoggingService.log( e );
            throw e;
        }
        
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
            LoggingService.log( e );
            throw e;
        }
        
        if( this instanceof ValueProperty || this instanceof TransientProperty )
        {
            this.type = null;
        }
        else
        {
            this.type = ModelElementType.getModelElementType( this.typeClass );
        }
        
        this.modelElementType.addProperty( this );
    }
    
    public ModelElementType getModelElementType()
    {
        return this.modelElementType;
    }
    
    public String getName()
    {
        return this.propertyName;
    }
    
    public final Class<?> getTypeClass()
    {
        return this.typeClass;
    }
    
    public final ModelElementType getType()
    {
        return this.type;
    }
    
    public final boolean isOfType( final Class<?> type )
    {
        return type.isAssignableFrom( getTypeClass() );        
    }
    
    @Override
    public ModelProperty getBase()
    {
        return this.baseProperty;
    }
    
    @Override
    @SuppressWarnings( "unchecked" )
    public <A extends Annotation> List<A> getAnnotations( final Class<A> type )
    {
        final List<A> annotations = new ArrayList<A>();
        final A annotation = (A) this.annotations.get( type );
        
        if( annotation != null )
        {
            annotations.add( annotation );
        }
        
        if( this.baseProperty != null )
        {
            annotations.addAll( this.baseProperty.getAnnotations( type ) );
        }
        
        return annotations;
    }
    
    @Override
    @SuppressWarnings( "unchecked" )
    public <A extends Annotation> A getAnnotation( final Class<A> type,
                                                   final boolean localOnly )
    {
        A annotation = (A) this.annotations.get( type );
        
        if( annotation == null && this.baseProperty != null && ! localOnly )
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

    public ModelProperty refine( final ModelElementType type )
    {
        return type.getProperty( this.propertyName );
    }

    public ModelProperty refine( final IModelElement modelElement )
    {
        return refine( ModelElementType.getModelElementType( modelElement.getClass() ) );
    }

    public final boolean isReadOnly()
    {
        return hasAnnotation( ReadOnly.class ) || isDerived();
    }
    
    public final boolean isDerived()
    {
        return hasAnnotation( Derived.class );
    }
    
    private void gatherAnnotations() 
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
            for( Annotation x : propField.getAnnotations() )
            {
                this.annotations.put( x.annotationType(), x );
            }
        }
    }
    
    public Set<ModelPropertyListener> getListeners()
    {
        synchronized( this )
        {
            if( this.listeners == null )
            {
                return Collections.emptySet();
            }
            else
            {
                return this.listenersReadOnly;
            }
        }
    }
    
    public void addListener( final ModelPropertyListener listener )
    {
        synchronized( this )
        {
            if( this.listeners == null )
            {
                this.listeners = new CopyOnWriteArraySet<ModelPropertyListener>();
                this.listenersReadOnly = Collections.unmodifiableSet( this.listeners );
            }
            
            this.listeners.add( listener );
        }
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
    
    
    public <S extends Service> S service( final Class<S> serviceType )
    {
        final List<S> services = services( serviceType );
        return ( services.isEmpty() ? null : services.get( 0 ) );
    }

    public <S extends Service> List<S> services( final Class<S> serviceType )
    {
        return services().services( serviceType );
    }

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
