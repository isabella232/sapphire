/******************************************************************************
 * Copyright (c) 2011 Oracle
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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.internal.MemoryResource;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.localization.LocalizationSystem;
import org.eclipse.sapphire.modeling.util.NLS;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ModelElementType 
    
    extends ModelMetadataItem
    
{
    private final Class<?> modelElementClass;
    private Class<?> implClass = null;
    private Constructor<?> implClassConstructor = null;
    private boolean implClassLoaded = false;
    private final List<ModelProperty> properties;
    private final LocalizationService localizationService;
    private ImageData image;
    private boolean imageInitialized;
    
    public ModelElementType( final Class<?> modelElementClass )
    {
        this.modelElementClass = modelElementClass;
        this.properties = new ArrayList<ModelProperty>();
        this.localizationService = LocalizationSystem.service( this.modelElementClass );
    }
    
    public static ModelElementType getModelElementType( final Class<?> modelElementClass )
    {
        return getModelElementType( modelElementClass, true );
    }
    
    public static ModelElementType getModelElementType( final Class<?> modelElementClass,
                                                        final boolean throwExceptionIfNotFound )
    {
        for( Field field : modelElementClass.getFields() )
        {
            if( field.getName().equals( "TYPE" ) ) //$NON-NLS-1$
            {
                try
                {
                    final Object fieldValue = field.get( null );
                    
                    if( fieldValue instanceof ModelElementType )
                    {
                        return (ModelElementType) fieldValue;
                    }
                    else
                    {
                        break;
                    }
                }
                catch( IllegalAccessException e )
                {
                    throw new RuntimeException( e );
                }
            }
        }
        
        if( throwExceptionIfNotFound )
        {
            throw new IllegalArgumentException( "Did not find TYPE field on " + modelElementClass.getName() ); //$NON-NLS-1$
        }
        else
        {
            return null;
        }
    }
    
    public Class<?> getModelElementClass()
    {
        return this.modelElementClass;
    }
    
    public String getSimpleName()
    {
        return this.modelElementClass.getSimpleName();
    }
    
    public String getQualifiedName()
    {
        return this.modelElementClass.getName();
    }
    
    public Class<?> getImplClass()
    {
        synchronized( this )
        {
            if( ! this.implClassLoaded )
            {
                this.implClassLoaded = true;
                
                final String implClassQualifiedName = getImplClassName( this.modelElementClass );
                
                if( implClassQualifiedName != null )
                {
                    try
                    {
                        this.implClass = this.modelElementClass.getClassLoader().loadClass( implClassQualifiedName );
                    }
                    catch( ClassNotFoundException e )
                    {
                        // No need to report this. The null return value signifies that the impl class was not found.
                    }
                    
                    if( this.implClass != null )
                    {
                        try
                        {
                            this.implClassConstructor = this.implClass.getConstructor( IModelParticle.class, ModelProperty.class, Resource.class );
                        }
                        catch( NoSuchMethodException e )
                        {
                            // todo: log a better message here
                            
                            LoggingService.log( e );
                            
                            this.implClass = null;
                        }
                    }
                }
            }
            
            return this.implClass;
        }
    }

    public static String getImplClassName( final Class<?> elementTypeClass )
    {
        if( elementTypeClass == null )
        {
            throw new IllegalArgumentException();
        }
        
        return getImplClassName( elementTypeClass.getName(), elementTypeClass.getAnnotation( GenerateImpl.class ) );
    }
    
    public static String getImplClassName( final String elementTypeClassName,
                                           final GenerateImpl generateImplAnnotation )
    {
        if( elementTypeClassName == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( generateImplAnnotation != null )
        {
            return getImplClassName( elementTypeClassName, generateImplAnnotation.packageName(), generateImplAnnotation.className() );
        }
        
        return null;
    }
    
    public static String getImplClassName( final String elementTypeClassName,
                                           final String preferredImplPackageName,
                                           final String preferredImplClassName )
    {
        if( elementTypeClassName == null )
        {
            throw new IllegalArgumentException();
        }
        
        final StringBuilder implClassQualifiedName = new StringBuilder();
        
        String elementTypePackageName = null;
        final int lastDotSeparator = elementTypeClassName.lastIndexOf( '.' );
        
        if( lastDotSeparator != -1 )
        {
            elementTypePackageName = elementTypeClassName.substring( 0, lastDotSeparator );
        }
        
        if( preferredImplPackageName == null || preferredImplPackageName.length() == 0 )
        {
            if( elementTypePackageName != null )
            {
                implClassQualifiedName.append( elementTypePackageName );
                implClassQualifiedName.append( '.' );
            }
            
            implClassQualifiedName.append( "internal" );
        }
        else
        {
            implClassQualifiedName.append( preferredImplPackageName );
        }
        
        implClassQualifiedName.append( '.' );
        
        if( preferredImplClassName == null || preferredImplClassName.length() == 0 )
        {
            final String fullTypeClassName;
            
            if( elementTypePackageName == null )
            {
                fullTypeClassName = elementTypeClassName;
            }
            else
            {
                fullTypeClassName = elementTypeClassName.substring( elementTypePackageName.length() + 1 );
            }
            
            final int lastSeparator = fullTypeClassName.lastIndexOf( '$' );
            final String simpleTypeClassName;
            
            if( lastSeparator == -1 )
            {
                simpleTypeClassName = fullTypeClassName;
            }
            else
            {
                final int simpleTypeClassNameStart = lastSeparator + 1;
                implClassQualifiedName.append( fullTypeClassName.substring( 0, simpleTypeClassNameStart ) );
                simpleTypeClassName = fullTypeClassName.substring( simpleTypeClassNameStart );
            }
            
            if( simpleTypeClassName.charAt( 0 ) == 'I' && simpleTypeClassName.length() > 1 && Character.isUpperCase( simpleTypeClassName.charAt( 1 ) ) )
            {
                implClassQualifiedName.append( simpleTypeClassName.substring( 1 ) );
            }
            else
            {
                implClassQualifiedName.append( simpleTypeClassName );
                implClassQualifiedName.append( "Impl" );
            }
        }
        else
        {
            implClassQualifiedName.append( preferredImplClassName );
        }
        
        return implClassQualifiedName.toString();
    }
    
    @SuppressWarnings( "unchecked" )
    
    public <T extends IModelElement> T instantiate( final IModelParticle parent,
                                                    final ModelProperty parentProperty,
                                                    final Resource resource )
    {
        getImplClass();
        
        if( this.implClassConstructor != null )
        {
            try
            {
                return (T) this.implClassConstructor.newInstance( parent, parentProperty, resource );
            }
            catch( Exception e )
            {
                final String msg = NLS.bind( Resources.cannotInstantiate, getSimpleName() );
                throw new RuntimeException( msg, e );
            }
        }
        
        final String msg = NLS.bind( Resources.cannotInstantiate, getSimpleName() );
        throw new RuntimeException( msg );
    }

    @SuppressWarnings( "unchecked" )
    
    public <T extends IModelElement> T instantiate( final Resource resource )
    {
        return (T) instantiate( null, null, resource );
    }
    
    @SuppressWarnings( "unchecked" )
    
    public <T extends IModelElement> T instantiate()
    {
        return (T) instantiate( null, null, new MemoryResource( this ) );
    }

    public List<ModelProperty> getProperties()
    {
        final TreeMap<String,ModelProperty> properties = new TreeMap<String,ModelProperty>();
        
        for( Class<?> cl : this.modelElementClass.getInterfaces() )
        {
            final ModelElementType t = getModelElementType( cl, false );
            
            if( t != null )
            {
                for( ModelProperty property : t.getProperties() )
                {
                    properties.put( property.getName(), property );
                }
            }
        }
        
        for( ModelProperty property : this.properties )
        {
            properties.put( property.getName(), property );
        }
        
        return new ArrayList<ModelProperty>( properties.values() );
    }
    
    public ModelProperty getProperty( final String propertyName )
    {
        for( ModelProperty property : getProperties() )
        {
            if( property.getName().equalsIgnoreCase( propertyName ) )
            {
                return property;
            }
        }
        
        return null;
    }
    
    void addProperty( final ModelProperty property )
    {
        this.properties.add( property );
    }
    
    @Override
    public <A extends Annotation> A getAnnotation( final Class<A> type,
                                                   final boolean localOnly )
    {
        return this.modelElementClass.getAnnotation( type );
    }
    
    public Class<?> getAnnotationHostClass( final Annotation annotation )
    {
        // TODO: Improve to take into account type hierarchies.
        
        return this.modelElementClass;
    }

    @Override
    protected String getDefaultLabel()
    {
        String className = this.modelElementClass.getName();
        int start = className.lastIndexOf( '.' ) + 1;
        final int startPlusOne = start + 1;
        
        if( className.charAt( start ) == 'I' && startPlusOne < className.length() && Character.isUpperCase( className.charAt( startPlusOne ) ) )
        {
            start = startPlusOne;
        }
        
        if( start > 0 )
        {
            className = className.substring( start );
        }
        
        return transformCamelCaseToLabel( className );
    }
    
    @Override
    public LocalizationService getLocalizationService()
    {
        return this.localizationService;
    }
    
    public ImageData image()
    {
        if( ! this.imageInitialized )
        {
            final Image imageAnnotation = getAnnotation( Image.class );
            
            if( imageAnnotation != null )
            {
                try
                {
                    this.image = ImageData.createFromClassLoader( getAnnotationHostClass( imageAnnotation ), imageAnnotation.path() );
                }
                catch( Exception e )
                {
                    LoggingService.log( e );
                }
            }
            
            this.imageInitialized = true;
        }
        
        return this.image;
    }

    protected static abstract class ModelPropertyInitListener
    {
        public abstract void propertyInitialized( final ModelProperty property );
    }

    private static final class Resources extends NLS
    {
        public static String cannotInstantiate; 
        
        static
        {
            initializeMessages( ModelElementType.class.getName(), Resources.class );
        }
    }
    
}
