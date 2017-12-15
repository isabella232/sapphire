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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.ModelPath.AllDescendentsSegment;
import org.eclipse.sapphire.modeling.ModelPath.AllSiblingsSegment;
import org.eclipse.sapphire.modeling.ModelPath.ModelRootSegment;
import org.eclipse.sapphire.modeling.ModelPath.ParentElementSegment;
import org.eclipse.sapphire.modeling.ModelPath.TypeFilterSegment;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.DefaultValueProvider;
import org.eclipse.sapphire.modeling.annotations.DefaultValueProviderImpl;
import org.eclipse.sapphire.modeling.annotations.EnabledByBooleanProperty;
import org.eclipse.sapphire.modeling.annotations.EnabledByEnumProperty;
import org.eclipse.sapphire.modeling.annotations.EnabledWhen;
import org.eclipse.sapphire.modeling.annotations.Enabler;
import org.eclipse.sapphire.modeling.annotations.EnablerImpl;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.modeling.annotations.PossibleValuesFromModel;
import org.eclipse.sapphire.modeling.annotations.PossibleValuesProvider;
import org.eclipse.sapphire.modeling.annotations.PossibleValuesProviderImpl;
import org.eclipse.sapphire.modeling.internal.BooleanPropertyEnabler;
import org.eclipse.sapphire.modeling.internal.EnumPropertyEnabler;
import org.eclipse.sapphire.modeling.internal.PossibleValuesFromModelProvider;
import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;
import org.eclipse.sapphire.modeling.internal.ScriptBasedEnabler;
import org.eclipse.sapphire.modeling.internal.StaticValuesProvider;

/**
 * <p><code>
 * IModelElement el = ...<br/>
 * StandardModelElementService svc = el.service( StandardModelElementService.class );
 * </code></p>
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class StandardModelElementService

    extends ModelElementService
    
{
    private final Map<ModelProperty,List<EnablerImpl>> enablers;
    private final Map<ValueProperty,DefaultValueProviderImpl> defaultValueProviders;
    private final Map<ValueProperty,String> defaultValues;
    private final Map<ValueProperty,PossibleValuesProviderImpl> valuesProviders;
    
    public StandardModelElementService()
    {
        this.enablers = Collections.synchronizedMap( new HashMap<ModelProperty,List<EnablerImpl>>() );
        this.defaultValueProviders = Collections.synchronizedMap( new HashMap<ValueProperty,DefaultValueProviderImpl>() );
        this.defaultValues = Collections.synchronizedMap( new HashMap<ValueProperty,String>() );
        this.valuesProviders = Collections.synchronizedMap( new HashMap<ValueProperty,PossibleValuesProviderImpl>() );
    }
    
    public Object read( final ModelProperty property )
    {
        return property.invokeGetterMethod( getModelElement() );
    }
    
    public Value<?> read( final ValueProperty property )
    {
        return (Value<?>) property.invokeGetterMethod( getModelElement() );
    }
    
    public IModelElement read( final ElementProperty property )
    {
        return (IModelElement) property.invokeGetterMethod( getModelElement() );
    }

    public ModelElementList<?> read( final ListProperty property )
    {
        return (ModelElementList<?>) property.invokeGetterMethod( getModelElement() );
    }

    public SortedSet<String> read( final ModelPath path )
    {
        final SortedSet<String> result = new TreeSet<String>();
        read( path, result );
        return result;
    }

    public void read( final ModelPath path,
                      final Collection<String> result )
    {
        synchronized( getModelElement().getModel() )
        {
            final ModelPath.Segment head = path.head();
            final IModelElement element = getModelElement();
            
            if( head instanceof ModelRootSegment )
            {
                element.getModel().service().read( path.tail(), result );
            }
            else if( head instanceof ParentElementSegment )
            {
                IModelParticle parent = element.getParent();
                
                if( parent == null )
                {
                    logInvalidModelPathMessage( path );
                    return;
                }
                else
                {
                    if( parent instanceof ModelElementList<?> )
                    {
                        parent = parent.getParent();
                    }
                }
                
                ( (IModelElement) parent ).service().read( path.tail(), result );
            }
            else if( head instanceof AllSiblingsSegment )
            {
                IModelParticle parent = element.getParent();
                
                if( parent == null || ! ( parent instanceof ModelElementList<?> ) )
                {
                    logInvalidModelPathMessage( path );
                    return;
                }
                
                parent = parent.getParent();
                
                final ModelPath p = ( new ModelPath( element.getParentProperty().getName() ) ).append( path.tail() );
                ( (IModelElement) parent ).service().read( p, result );
            }
            else if( head instanceof AllDescendentsSegment )
            {
                for( ModelProperty property : element.getModelElementType().getProperties() )
                {
                    final Object obj = read( property );
                    
                    if( obj instanceof Value<?> )
                    {
                        final String val = ( (Value<?>) obj ).getText();
                        
                        if( val != null )
                        {
                            result.add( val );
                        }
                    }
                    else if( obj instanceof IModelElement )
                    {
                        ( (IModelElement) obj ).service().read( path, result );
                    }
                    else if( obj instanceof ModelElementList<?> )
                    {
                        for( IModelElement entry : (ModelElementList<?>) obj )
                        {
                            entry.service().read( path, result );
                        }
                    }
                }
            }
            else if( head instanceof TypeFilterSegment )
            {
                final String t = element.getModelElementType().getSimpleName();
                boolean match = false;
                
                for( String type : ( (TypeFilterSegment) head ).getTypes() )
                {
                    if( type.equalsIgnoreCase( t ) )
                    {
                        match = true;
                        break;
                    }
                }
                
                if( match )
                {
                    read( path.tail(), result );
                }
            }
            else
            {
                final String propertyName = ( (ModelPath.PropertySegment) head ).getPropertyName();
                final ModelProperty property = element.getModelElementType().getProperty( propertyName );

                if( property == null )
                {
                    logInvalidModelPathMessage( path );
                    return;
                }
                
                final Object obj = read( property );
                
                if( obj instanceof Value<?> )
                {
                    final String val = ( (Value<?>) obj ).getText();
                    
                    if( val != null )
                    {
                        result.add( val );
                    }
                    
                    if( path.length() != 1 )
                    {
                        logInvalidModelPathMessage( path );
                        return;
                    }
                }
                else if( obj instanceof IModelElement )
                {
                    ( (IModelElement) obj ).service().read( path.tail(), result );
                }
                else if( obj instanceof ModelElementList<?> )
                {
                    for( IModelElement entry : (ModelElementList<?>) obj )
                    {
                        entry.service().read( path.tail(), result );
                    }
                }
            }
        }
    }
    
    public void write( final ValueProperty property,
                       final Object value )
    {
        property.invokeSetterMethod( getModelElement(), value );
    }
    
    public boolean isEnabled( final ModelProperty property )
    {
        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        List<EnablerImpl> enablers = null;
        
        if( this.enablers.containsKey( property ) )
        {
            enablers = this.enablers.get( property );
        }
        else
        {
            enablers = new ArrayList<EnablerImpl>(); 
            
            for( EnabledByBooleanProperty enabledByBooleanAnnotation : property.getAnnotations( EnabledByBooleanProperty.class ) )
            {
                final EnablerImpl enabler = new BooleanPropertyEnabler();
                enabler.init( getModelElement(), property, new String[] { enabledByBooleanAnnotation.value() } );
                enablers.add( enabler );
            }
            
            for( EnabledByEnumProperty enabledByEnumAnnotation : property.getAnnotations( EnabledByEnumProperty.class ) )
            {
                final StringBuilder buf = new StringBuilder();
                
                for( String value : enabledByEnumAnnotation.values() )
                {
                    if( buf.length() > 0 )
                    {
                        buf.append( ',' );
                    }
                    
                    buf.append( value );
                }
                
                final EnablerImpl enabler = new EnumPropertyEnabler();
                enabler.init( getModelElement(), property, new String[] { enabledByEnumAnnotation.property(), buf.toString() } );
                enablers.add( enabler );
            }

            for( Enabler enablerAnnotation : property.getAnnotations( Enabler.class ) )
            {
                final Class<? extends EnablerImpl> enablerClass = enablerAnnotation.impl();
                
                try
                {
                    final EnablerImpl enabler = enablerClass.newInstance();
                    enabler.init( getModelElement(), property, enablerAnnotation.params() );
                    enablers.add( enabler );
                }
                catch( Exception e )
                {
                    SapphireModelingFrameworkPlugin.log( e );
                }
            }
            
            for( EnabledWhen enabledWhenAnnotation : property.getAnnotations( EnabledWhen.class ) )
            {
                final EnablerImpl enabler = new ScriptBasedEnabler();
                enabler.init( getModelElement(), property, new String[] { enabledWhenAnnotation.value() } );
                enablers.add( enabler );
            }
            
            final int count = enablers.size();
            
            if( count == 0 )
            {
                enablers = null;
            }
            else if( count == 1 )
            {
                enablers = Collections.singletonList( enablers.get( 0 ) );
            }
            
            this.enablers.put( property, enablers );
        }
        
        if( enablers != null )
        {
            for( EnablerImpl enabler : enablers )
            {
                try
                {
                    if( ! enabler.isEnabled() )
                    {
                        return false;
                    }
                }
                catch( Exception e )
                {
                    SapphireModelingFrameworkPlugin.log( e );
                }
            }
        }
        
        return true;
    }
    
    public String getDefaultValue( final ValueProperty property )
    {
        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( ! this.defaultValues.containsKey( property ) )
        {
            String defaultValue = null;
            DefaultValueProviderImpl defaultValueProvider = null;
            
            final DefaultValue defaultValueAnnotation = property.getAnnotation( DefaultValue.class );
            
            if( defaultValueAnnotation != null )
            {
                defaultValue = defaultValueAnnotation.value();
            }
            else
            {
                final DefaultValueProvider defaultValueProviderAnnotation = property.getAnnotation( DefaultValueProvider.class );
                
                if( defaultValueProviderAnnotation != null )
                {
                    final Class<? extends DefaultValueProviderImpl> defaultValueProviderClass = defaultValueProviderAnnotation.impl();
                    
                    try
                    {
                        defaultValueProvider = defaultValueProviderClass.newInstance();
                        defaultValueProvider.init( getModelElement(), property, defaultValueProviderAnnotation.params() );
                    }
                    catch( Exception e )
                    {
                        SapphireModelingFrameworkPlugin.log( e );
                        defaultValueProvider = null;
                    }
                }
            }
            
            this.defaultValues.put( property, defaultValue );
            this.defaultValueProviders.put( property, defaultValueProvider );
        }
        
        String defaultValue = this.defaultValues.get( property );
        
        if( defaultValue == null )
        {
            final DefaultValueProviderImpl provider = this.defaultValueProviders.get( property );
            
            if( provider != null )
            {
                try
                {
                    defaultValue = provider.getDefaultValue();
                }
                catch( Exception e )
                {
                    SapphireModelingFrameworkPlugin.log( e );
                }
            }
        }
        
        return defaultValue;
    }
    
    public PossibleValuesProviderImpl getPossibleValuesProvider( final ValueProperty property )
    {
        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        PossibleValuesProviderImpl valuesProvider = null;
        
        if( this.valuesProviders.containsKey( property ) )
        {
            valuesProvider = this.valuesProviders.get( property );
        }
        else
        {
            final PossibleValuesProvider valuesProviderAnnotation = property.getAnnotation( PossibleValuesProvider.class );
            
            if( valuesProviderAnnotation != null )
            {
                final Class<? extends PossibleValuesProviderImpl> valuesProviderClass = valuesProviderAnnotation.impl();
                
                try
                {
                    valuesProvider = valuesProviderClass.newInstance();
                    
                    valuesProvider.init( getModelElement(), property, valuesProviderAnnotation.invalidValueMessage(),
                                         valuesProviderAnnotation.invalidValueSeverity(), valuesProviderAnnotation.caseSensitive(),
                                         valuesProviderAnnotation.params() );
                }
                catch( Exception e )
                {
                    SapphireModelingFrameworkPlugin.log( e );
                    valuesProvider = null;
                }
            }
            
            if( valuesProvider == null )
            {
                final PossibleValues possibleValuesAnnotation = property.getAnnotation( PossibleValues.class );
                
                if( possibleValuesAnnotation != null )
                {
                    valuesProvider = new StaticValuesProvider( possibleValuesAnnotation.values() );
                    
                    valuesProvider.init( getModelElement(), property, possibleValuesAnnotation.invalidValueMessage(),
                                         possibleValuesAnnotation.invalidValueSeverity(), possibleValuesAnnotation.caseSensitive(),
                                         new String[ 0 ] );
                }
            }
            
            if( valuesProvider == null )
            {
                final PossibleValuesFromModel possibleValuesFromModelAnnotation = property.getAnnotation( PossibleValuesFromModel.class );
                
                if( possibleValuesFromModelAnnotation != null )
                {
                    valuesProvider = new PossibleValuesFromModelProvider( new ModelPath( possibleValuesFromModelAnnotation.path() ) );
                    
                    valuesProvider.init( getModelElement(), property, possibleValuesFromModelAnnotation.invalidValueMessage(),
                                         possibleValuesFromModelAnnotation.invalidValueSeverity(), possibleValuesFromModelAnnotation.caseSensitive(),
                                         new String[ 0 ] );
                }
            }
            
            this.valuesProviders.put( property, valuesProvider );
        }
        
        return valuesProvider;
    }

    private void logInvalidModelPathMessage( final ModelPath path )
    {
        final String message 
            = NLS.bind( Resources.invalidModelPath, getModelElement().getModelElementType().getModelElementClass().getName(), path.toString() );
        
        SapphireModelingFrameworkPlugin.logError( message, null );
    }
    
    private static final class Resources
    
        extends NLS
    
    {
        public static String invalidModelPath;
        
        static
        {
            initializeMessages( StandardModelElementService.class.getName(), Resources.class );
        }
    }

}
