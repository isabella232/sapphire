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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.sapphire.modeling.annotations.DependsOn;
import org.eclipse.sapphire.modeling.annotations.EnabledByBooleanProperty;
import org.eclipse.sapphire.modeling.annotations.EnabledByEnumProperty;
import org.eclipse.sapphire.modeling.annotations.ModelPropertyValidator;
import org.eclipse.sapphire.modeling.annotations.PropertyListeners;
import org.eclipse.sapphire.modeling.annotations.ReadOnly;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.annotations.Validator;
import org.eclipse.sapphire.modeling.annotations.Validators;
import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ModelProperty 

    extends ModelMetadataItem
    
{
    public static final String PROPERTY_FIELD_PREFIX = "PROP_"; //$NON-NLS-1$
    
    private final ModelElementType modelElementType;
    private final String propertyName;
    private final ModelProperty baseProperty;

    private final Class<?> typeClass;
    private final ModelElementType type;
    private final List<Class<?>> allPossibleTypeClasses;
    private final List<Class<?>> allPossibleTypeClassesReadOnly;
    private final List<ModelElementType> allPossibleTypes;
    private final List<ModelElementType> allPossibleTypesReadOnly;
    
    private final Method getterMethod;
    private final Map<Class<? extends Annotation>,Annotation> annotations;
    private ModelPropertyValidator<?> validator;
    private boolean isValidatorInitialized;
    private Set<ModelPropertyListener> listeners;
    private Set<ModelPropertyListener> listenersReadOnly;
    private Set<ModelPath> dependencies;
    
    public ModelProperty( final ModelElementType modelElementType,
                          final String propertyName,
                          final ModelProperty baseProperty )
    {
        try
        {
            this.modelElementType = modelElementType;
            this.propertyName = propertyName;
            this.baseProperty = baseProperty;
            this.dependencies = null;
            
            final String propLowerCase = this.propertyName.toLowerCase();
            
            final String alt1 = "get" + propLowerCase; //$NON-NLS-1$
            final String alt2 = "is" + propLowerCase; //$NON-NLS-1$
            
            Method getter = null;
            
            for( Method method : this.modelElementType.getModelElementClass().getMethods() )
            {
                final String methodName = method.getName().toLowerCase();
                
                if( ( methodName.equals( alt1 ) || methodName.equals( alt2 ) ) 
                    && method.getParameterTypes().length == 0 )
                {
                    getter = method;
                    break;
                }
            }
            
            if( getter == null )
            {
                throw new IllegalStateException( propertyName );
            }
            
            this.getterMethod = getter;
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
                        SapphireModelingFrameworkPlugin.log( e );
                    }
                }
            }
        }
        catch( RuntimeException e )
        {
            SapphireModelingFrameworkPlugin.log( e );
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
                    this.allPossibleTypeClasses = Collections.<Class<?>>singletonList( this.typeClass );
                    this.allPossibleTypeClassesReadOnly = this.allPossibleTypeClasses;
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

                if( typeAnnotation.possible().length == 0 )
                {
                    this.allPossibleTypeClasses = Collections.<Class<?>>singletonList( this.typeClass );
                    this.allPossibleTypeClassesReadOnly = this.allPossibleTypeClasses;
                }
                else
                {
                    this.allPossibleTypeClasses = new ArrayList<Class<?>>();
                    
                    for( Class<?> cl : typeAnnotation.possible() )
                    {
                        this.allPossibleTypeClasses.add( cl );
                    }
                    
                    this.allPossibleTypeClassesReadOnly = Collections.unmodifiableList( this.allPossibleTypeClasses );
                }
            }
        }
        catch( RuntimeException e )
        {
            SapphireModelingFrameworkPlugin.log( e );
            throw e;
        }
        
        if( this instanceof ValueProperty )
        {
            this.type = null;
            this.allPossibleTypes = Collections.emptyList();
            this.allPossibleTypesReadOnly = Collections.emptyList();
        }
        else
        {
            this.type = ModelElementType.getModelElementType( this.typeClass );
            
            if( this.allPossibleTypeClasses.size() == 1 )
            {
                this.allPossibleTypes = Collections.singletonList( ModelElementType.getModelElementType( this.typeClass ) );
                this.allPossibleTypesReadOnly = this.allPossibleTypes;
            }
            else
            {
                this.allPossibleTypes = new ArrayList<ModelElementType>();
                
                for( Class<?> cl : this.allPossibleTypeClasses )
                {
                    this.allPossibleTypes.add( ModelElementType.getModelElementType( cl ) );
                }
                
                this.allPossibleTypesReadOnly = Collections.unmodifiableList( this.allPossibleTypes );
            }
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
    
    public final List<Class<?>> getAllPossibleTypeClasses()
    {
        return this.allPossibleTypeClassesReadOnly;
    }
    
    public final List<ModelElementType> getAllPossibleTypes()
    {
        return this.allPossibleTypesReadOnly;
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
    
    public Method getGetterMethod()
    {
        return this.getterMethod;
    }
    
    @SuppressWarnings( "unchecked" )
    
    public <T> T invokeGetterMethod( final Object model )
    {
        try
        {
            return (T) this.getterMethod.invoke( model );
        }
        catch( Exception e )
        {
            final Throwable cause = e.getCause();
            
            if( cause instanceof EditFailedException )
            {
                throw (EditFailedException) cause;
            }
            else
            {
                final Class<?> expectedClass = getModelElementType().getModelElementClass();
                final Class<?> actualClass = model.getClass();
                
                final StringBuilder buf = new StringBuilder();
                
                buf.append( "Expected: " ); //$NON-NLS-1$
                buf.append( expectedClass.getName() );
                buf.append( '\n' );
                buf.append( "Actual: " ); //$NON-NLS-1$
                buf.append( actualClass.getName() );
                
                throw new RuntimeException( buf.toString(), cause );
            }
        }
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
    public String getResource( final String key )
    {
        String resource = this.modelElementType.getResource( key );
        
        if( resource == null && this.baseProperty != null )
        {
            resource = this.baseProperty.getResource( key );
        }
        
        return resource;
    }

    @Override
    protected String getLabelResourceKeyBase()
    {
        return this.propertyName;
    }
    
    @Override
    protected String getDefaultLabel()
    {
        return transformCamelCaseToLabel( this.propertyName );
    }

    public ModelProperty refine( final ModelElementType type )
    {
        return type.getProperty( this.propertyName );
    }

    public ModelProperty refine( final IModelElement modelElement )
    {
        return refine( ModelElementType.getModelElementType( modelElement.getClass() ) );
    }

    public synchronized final ModelPropertyValidator<?> getValidator()
    {
        if( ! this.isValidatorInitialized )
        {
            final List<ModelPropertyValidator<? extends Object>> validators = createValidators();
        
            if( validators.isEmpty() )
            {
                this.validator = null;
            }
            else if( validators.size() == 1 )
            {
                this.validator = validators.get( 0 );
            }
            else
            {
                final ModelPropertyValidator<Object> unionValidator = new ModelPropertyValidator<Object>()
                {
                    @Override
                    @SuppressWarnings( "unchecked" )
                    
                    public IStatus validate( final Object value )
                    {
                        final SapphireMultiStatus multiStatus = new SapphireMultiStatus();
                        
                        for( ModelPropertyValidator<? extends Object> validator : validators )
                        {
                            multiStatus.add( ( (ModelPropertyValidator<Object>) validator).validate( value ) );
                        }
                        
                        return multiStatus;
                    }
                };
                
                this.validator = unionValidator;
            }
            
            this.isValidatorInitialized = true;
        }
        
        return this.validator;
    }
    
    protected List<ModelPropertyValidator<? extends Object>> createValidators()
    {
        final List<ModelPropertyValidator<? extends Object>> validators = new ArrayList<ModelPropertyValidator<? extends Object>>();
        createValidators( validators, this );
        return validators;
    }
    
    private static void createValidators( final List<ModelPropertyValidator<? extends Object>> validators,
                                          final ModelProperty property )
    {
        final Validator validatorAnnotation = property.getAnnotation( Validator.class, true );
        
        if( validatorAnnotation != null )
        {
            createValidator( validators, validatorAnnotation );
        }
        
        final Validators validatorsAnnotation = property.getAnnotation( Validators.class, true );
        
        if( validatorsAnnotation != null )
        {
            for( Validator x : validatorsAnnotation.value() )
            {
                createValidator( validators, x );
            }
        }

        final ModelProperty baseProperty = property.getBase();
        
        if( baseProperty != null )
        {
            createValidators( validators, baseProperty );
        }
    }
    
    private static void createValidator( final List<ModelPropertyValidator<? extends Object>> validators,
                                         final Validator validatorAnnotation )
    {
        final Class<? extends ModelPropertyValidator<?>> validatorClass = validatorAnnotation.impl();
        final ModelPropertyValidator<?> validator;
        
        try
        {
            validator = validatorClass.newInstance();
            validator.init( validatorAnnotation.params() );
            validators.add( validator );
        }
        catch( Exception e )
        {
            SapphireModelingFrameworkPlugin.log( e );
        }
    }
    
    public boolean isReadOnly()
    {
        return hasAnnotation( ReadOnly.class );
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
    
    public final Set<ModelPath> getDependencies()
    {
        if( this.dependencies == null )
        {
            final Set<String> dependenciesAsStrings = new HashSet<String>();
            
            final DependsOn dependsOnAnnotation = getAnnotation( DependsOn.class );
            
            if( dependsOnAnnotation != null )
            {
                for( String dependsOnPropertyRef : dependsOnAnnotation.value() )
                {
                    dependenciesAsStrings.add( dependsOnPropertyRef );
                }
            }
            
            final EnabledByBooleanProperty enabledByBooleanProperty = getAnnotation( EnabledByBooleanProperty.class );
            
            if( enabledByBooleanProperty != null )
            {
                dependenciesAsStrings.add( enabledByBooleanProperty.value() );
            }
            
            final EnabledByEnumProperty enabledByEnumProperty = getAnnotation( EnabledByEnumProperty.class );
            
            if( enabledByEnumProperty != null )
            {
                dependenciesAsStrings.add( enabledByEnumProperty.property() );
            }
            
            final Set<ModelPath> dependencies = new HashSet<ModelPath>();
            
            for( String str : dependenciesAsStrings )
            {
                ModelPath path = null;
                
                try
                {
                    path = new ModelPath( str );
                }
                catch( ModelPath.MalformedPathException e )
                {
                    SapphireModelingFrameworkPlugin.log( e );
                }
                
                dependencies.add( path );
            }
            
            this.dependencies = Collections.unmodifiableSet( dependencies );
        }
        
        return this.dependencies;
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
