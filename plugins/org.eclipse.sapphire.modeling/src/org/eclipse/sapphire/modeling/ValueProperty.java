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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.annotations.AbsolutePath;
import org.eclipse.sapphire.modeling.annotations.BasePathsProvider;
import org.eclipse.sapphire.modeling.annotations.EclipseWorkspacePath;
import org.eclipse.sapphire.modeling.annotations.ModelPropertyValidator;
import org.eclipse.sapphire.modeling.annotations.NoDuplicates;
import org.eclipse.sapphire.modeling.annotations.NonNullValue;
import org.eclipse.sapphire.modeling.annotations.NumericRange;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.internal.SapphireModelingFrameworkPlugin;
import org.eclipse.sapphire.modeling.java.JavaPackageName;
import org.eclipse.sapphire.modeling.java.JavaTypeConstraints;
import org.eclipse.sapphire.modeling.java.JavaTypeName;
import org.eclipse.sapphire.modeling.java.internal.JavaTypeNameValidator;
import org.eclipse.sapphire.modeling.java.internal.QualifiedJavaIdentifierValueValidator;
import org.eclipse.sapphire.modeling.validators.AbsolutePathValueValidator;
import org.eclipse.sapphire.modeling.validators.BasicValueValidator;
import org.eclipse.sapphire.modeling.validators.EclipseWorkspacePathValueValidator;
import org.eclipse.sapphire.modeling.validators.NumericRangeValidator;
import org.eclipse.sapphire.modeling.validators.PossibleValuesValidator;
import org.eclipse.sapphire.modeling.validators.ReferenceValueValidator;
import org.eclipse.sapphire.modeling.validators.RelativePathValueValidator;
import org.eclipse.sapphire.modeling.validators.UniqueValueValidator;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ValueProperty 

    extends ModelProperty
    
{
    private static final Set<ValueKeyword> NO_KEYWORDS = Collections.emptySet();
    private static final Set<ValueKeyword> INTEGER_KEYWORDS;
    private static final Set<ValueKeyword> LONG_KEYWORDS;
    private static final Set<ValueKeyword> FLOAT_KEYWORDS;
    private static final Set<ValueKeyword> DOUBLE_KEYWORDS;
    
    static
    {
        Set<ValueKeyword> keywords = new HashSet<ValueKeyword>();
        keywords.add( new IntegerValueKeyword( "max-int", String.valueOf( Integer.MAX_VALUE ) ) );
        keywords.add( new IntegerValueKeyword( "min-int", String.valueOf( Integer.MIN_VALUE ) ) );
        
        INTEGER_KEYWORDS = Collections.unmodifiableSet( keywords );
        
        keywords = new HashSet<ValueKeyword>();
        keywords.add( new LongValueKeyword( "max-long", String.valueOf( Long.MAX_VALUE ) ) );
        keywords.add( new LongValueKeyword( "min-long", String.valueOf( Long.MIN_VALUE ) ) );
        
        LONG_KEYWORDS = Collections.unmodifiableSet( keywords );

        keywords = new HashSet<ValueKeyword>();
        keywords.add( new FloatValueKeyword( "max-float", String.valueOf( Float.MAX_VALUE ) ) );
        keywords.add( new FloatValueKeyword( "min-float", String.valueOf( Float.MIN_VALUE ) ) );
        
        FLOAT_KEYWORDS = Collections.unmodifiableSet( keywords );

        keywords = new HashSet<ValueKeyword>();
        keywords.add( new DoubleValueKeyword( "max-double", String.valueOf( Double.MAX_VALUE ) ) );
        keywords.add( new DoubleValueKeyword( "min-double", String.valueOf( Double.MIN_VALUE ) ) );
        
        DOUBLE_KEYWORDS = Collections.unmodifiableSet( keywords );
    }
    
    private final Set<ValueKeyword> keywords;
    
    public ValueProperty( final ModelElementType type,
                          final String propertyName )
    {
        this( type, propertyName, null );
    }

    public ValueProperty( final ModelElementType type,
                          final ValueProperty baseProperty )
    {
        this( type, baseProperty.getName(), baseProperty );
    }
    
    private ValueProperty( final ModelElementType type,
                           final String propertyName,
                           final ValueProperty baseProperty )
    {
        super( type, propertyName, baseProperty );
        
        // In the future, this could be generalized to make it possible for extenders that create their own
        // value types to define keywords. In fact, it should be possible to define keywords attached to a specific
        // property (via an annotation). Of course, if we go that route, we would need some way to let the user
        // see the list of available keywords for a given property.
        
        final Class<?> propType = getTypeClass();
        
        if( propType == Integer.class )
        {
            this.keywords = INTEGER_KEYWORDS;
        }
        else if( propType == Long.class )
        {
            this.keywords = LONG_KEYWORDS;
        }
        else if( propType == Float.class )
        {
            this.keywords = FLOAT_KEYWORDS;
        }
        else if( propType == Double.class )
        {
            this.keywords = DOUBLE_KEYWORDS;
        }
        else
        {
            this.keywords = NO_KEYWORDS;
        }
    }
    
    @Override
    protected List<ModelPropertyValidator<? extends Object>> createValidators()
    {
        final List<ModelPropertyValidator<? extends Object>> validators = super.createValidators();
            
        if( hasAnnotation( NonNullValue.class ) )
        {
            final String labelText = getLabel( true, CapitalizationType.FIRST_WORD_ONLY, false );

            final ModelPropertyValidator<Value<?>> nullValueValidator = new ModelPropertyValidator<Value<?>>()
            {
                @Override
                public IStatus validate( final Value<?> value )
                {
                    if( value.getText() == null )
                    {
                        final String message = Resources.bind( Resources.nullValueValidationMessage, labelText );
                        return new Status( Status.ERROR, SapphireModelingFrameworkPlugin.PLUGIN_ID, message );
                    }
                    else
                    {
                        return Status.OK_STATUS;
                    }
                }
            };

            validators.add( nullValueValidator );
        }
        
        if( hasAnnotation( NoDuplicates.class ) )
        {
            validators.add( new UniqueValueValidator() );
        }
        
        if( hasAnnotation( Reference.class ) )
        {
            validators.add( new ReferenceValueValidator() );
        }
        
        final Class<?> type = getTypeClass();
        
        if( JavaPackageName.class.isAssignableFrom( type ) )
        {
            validators.add( new QualifiedJavaIdentifierValueValidator() );
        }
        else if( JavaTypeName.class.isAssignableFrom( type ) || hasAnnotation( JavaTypeConstraints.class ) )
        {
            validators.add( new QualifiedJavaIdentifierValueValidator() );
            validators.add( new JavaTypeNameValidator( this ) );
        }
        else if( IPath.class.isAssignableFrom( type ) )
        {
            if( hasAnnotation( AbsolutePath.class ) )
            {
                validators.add( new AbsolutePathValueValidator( this ) );
            }
            else if( hasAnnotation( BasePathsProvider.class ) )
            {
                validators.add( new RelativePathValueValidator( this ) );
            }
            else if( hasAnnotation( EclipseWorkspacePath.class ) )
            {
                validators.add( new EclipseWorkspacePathValueValidator( this ) );
            }
        }
        else if( ! String.class.isAssignableFrom( type ) )
        {
            validators.add( new BasicValueValidator() );
        }
        
        if( PossibleValuesValidator.isNecessary( this ) )
        {
            validators.add( new PossibleValuesValidator() );
        }
        
        final NumericRange rangeConstraintAnnotation = getAnnotation( NumericRange.class );
        
        if( rangeConstraintAnnotation != null )
        {
            final String minStr = rangeConstraintAnnotation.min();
            final String maxStr = rangeConstraintAnnotation.max();
            
            if( minStr != null || maxStr != null )
            {
                try
                {
                    if( Integer.class.isAssignableFrom( type ) )
                    {
                        final Integer min = ( minStr.length() > 0 ? Integer.valueOf( minStr ) : null );
                        final Integer max = ( maxStr.length() > 0 ? Integer.valueOf( maxStr ) : null );
                        validators.add( new NumericRangeValidator<Integer>( min, max ) );
                    }
                    else if( Long.class.isAssignableFrom( type ) )
                    {
                        final Long min = ( minStr.length() > 0 ? Long.valueOf( minStr ) : null );
                        final Long max = ( maxStr.length() > 0 ? Long.valueOf( maxStr ) : null );
                        validators.add( new NumericRangeValidator<Long>( min, max ) );
                    }
                    else if( Float.class.isAssignableFrom( type ) )
                    {
                        final Float min = ( minStr.length() > 0 ? Float.valueOf( minStr ) : null );
                        final Float max = ( maxStr.length() > 0 ? Float.valueOf( maxStr ) : null );
                        validators.add( new NumericRangeValidator<Float>( min, max ) );
                    }
                    else if( Double.class.isAssignableFrom( type ) )
                    {
                        final Double min = ( minStr.length() > 0 ? Double.valueOf( minStr ) : null );
                        final Double max = ( maxStr.length() > 0 ? Double.valueOf( maxStr ) : null );
                        validators.add( new NumericRangeValidator<Double>( min, max ) );
                    }
                    else if( BigInteger.class.isAssignableFrom( type ) )
                    {
                        final BigInteger min = ( minStr.length() > 0 ? new BigInteger( minStr ) : null );
                        final BigInteger max = ( maxStr.length() > 0 ? new BigInteger( maxStr ) : null );
                        validators.add( new NumericRangeValidator<BigInteger>( min, max ) );
                    }
                    else if( BigDecimal.class.isAssignableFrom( type ) )
                    {
                        final BigDecimal min = ( minStr.length() > 0 ? new BigDecimal( minStr ) : null );
                        final BigDecimal max = ( maxStr.length() > 0 ? new BigDecimal( maxStr ) : null );
                        validators.add( new NumericRangeValidator<BigDecimal>( min, max ) );
                    }
                }
                catch( NumberFormatException e )
                {
                    SapphireModelingFrameworkPlugin.log( e );
                }
            }
        }
        
        return validators;
    }
    
    public Set<ValueKeyword> getKeywords()
    {
        return this.keywords;
    }
    
    public ValueKeyword getKeyword( final String keyword )
    {
        for( ValueKeyword kwd : this.keywords )
        {
            if( kwd.getKeyword().equals( keyword ) )
            {
                return kwd;
            }
        }
        
        return null;
    }
    
    public String decodeKeywords( final String value )
    {
        String result = value;
        
        if( value != null )
        {
            for( ValueKeyword keyword : this.keywords )
            {
                result = keyword.decode( value );
                
                if( result != value )
                {
                    break;
                }
            }
        }
        
        return result;
    }

    public String encodeKeywords( final String value )
    {
        String result = value;
        
        if( value != null )
        {
            for( ValueKeyword keyword : this.keywords )
            {
                result = keyword.encode( value );
                
                if( result != value )
                {
                    break;
                }
            }
        }
        
        return result;
    }
    
    @Override
    protected Set<ModelPath> initDependencies()
    {
        final Set<ModelPath> dependencies = super.initDependencies();
        
        if( hasAnnotation( NoDuplicates.class ) )
        {
            dependencies.add( new ModelPath( "*/" + getName() ) );
        }
        
        return dependencies;
    }
    
    private static final class Resources
        
        extends NLS

    {
        public static String nullValueValidationMessage;
        
        static
        {
            initializeMessages( ValueProperty.class.getName(), Resources.class );
        }
    }
    
}
