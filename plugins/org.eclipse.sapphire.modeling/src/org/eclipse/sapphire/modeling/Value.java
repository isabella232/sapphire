/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling;

import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.util.MiscUtil;
import org.eclipse.sapphire.services.DefaultValueService;
import org.eclipse.sapphire.services.ValidationAggregationService;
import org.eclipse.sapphire.services.ValueSerializationMasterService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class Value<T> extends ModelParticle
{
    private final ValueProperty property;
    private final String raw;
    private final T parsed;
    private Status validation;
    private boolean defaultValueInitialized;
    private String defaultText;
    private T defaultContent;

    public Value( final IModelElement parent,
                  final ValueProperty property,
                  final String value )
    {
        super( parent, parent.resource() );
        
        this.property = property;
        this.raw = normalize( value );
        this.parsed = parse( property.decodeKeywords( this.raw ) );
        this.validation = null;
        this.defaultValueInitialized = false;
        this.defaultText = null;
        this.defaultContent = null;
    }
    
    public void init()
    {
        // The value objects are effectively immutable, however there is a problem with infinite
        // recursion when a validator or a default value provider accesses model properties. This 
        // method allows the value to be assigned to the model element's field before starting 
        // validation or default value computation. It should be called immediately after creating 
        // the value object. Note that it still possible to get infinite recursion if a say a 
        // validator calls value's validate method, but that has not come up so far as an 
        // interesting scenario.
        
        initValidation();
        initDefaultValue();
    }
    
    private void initValidation()
    {
        if( this.validation == null )
        {
            this.validation = parent().service( this.property, ValidationAggregationService.class ).validation();
        }
    }

    private void initDefaultValue()
    {
        if( ! this.defaultValueInitialized )
        {
            final DefaultValueService defaultValueService = parent().service( this.property, DefaultValueService.class );
            
            this.defaultText = ( defaultValueService == null ? null : normalize( defaultValueService.value() ) );
            this.defaultContent = parse( this.property.decodeKeywords( this.defaultText ) );
            this.defaultValueInitialized = true;
        }
    }

    @Override
    public IModelElement parent()
    {
        return (IModelElement) super.parent();
    }
    
    public ValueProperty getProperty()
    {
        return this.property;
    }
    
    public String getText()
    {
        return getText( true );
    }
    
    public String getText( final boolean useDefaultValue )
    {
        if( this.raw != null )
        {
            return this.raw;
        }
        else if( useDefaultValue )
        {
            return getDefaultText();
        }

        return null;
    }
    
    public String getLocalizedText()
    {
        return getLocalizedText( true );
    }
    
    public String getLocalizedText( final boolean useDefaultValue )
    {
        return getLocalizedText( useDefaultValue, CapitalizationType.NO_CAPS, true );
    }
    
    public String getLocalizedText( final CapitalizationType capitalizationType,
                                    final boolean includeMnemonic )
    {
        return getLocalizedText( true, capitalizationType, includeMnemonic );
    }
    
    public String getLocalizedText( final boolean useDefaultValue,
                                    final CapitalizationType capitalizationType,
                                    final boolean includeMnemonic )
    {
        final String sourceLangText = getText( useDefaultValue );
        
        if( sourceLangText != null )
        {
            return adapt( LocalizationService.class ).text( sourceLangText, capitalizationType, includeMnemonic );
        }
        
        return null;
    }
    
    public T getContent()
    {
        return getContent( true );
    }
    
    public T getContent( final boolean useDefaultValue )
    {
        if( this.parsed != null )
        {
            return this.parsed;
        }
        else if( useDefaultValue )
        {
            return getDefaultContent();
        }

        return null;
    }
    
    public boolean isDefault()
    {
        return ( this.raw == null );
    }
    
    public T getDefaultContent()
    {
        initDefaultValue();
        return this.defaultContent;
    }
    
    public String getDefaultText()
    {
        initDefaultValue();
        return this.defaultText;
    }
    
    public boolean isMalformed()
    {
        if( isDefault() )
        {
            initDefaultValue();
            return ( this.defaultText != null && this.defaultContent == null );
        }
        else
        {
            return ( this.raw != null && this.parsed == null );
        }
    }
    
    public Status validation()
    {
        initValidation();
        return this.validation;
    }
    
    @Override
    public boolean equals( final Object val )
    {
        if( this == val )
        {
            return true;
        }
        
        if( val == null || ! ( val instanceof Value ) )
        {
            return false;
        }
        
        init();
        
        final Value<?> value = (Value<?>) val;
        
        return ( parent() == value.parent() ) && ( this.property == value.property ) &&
               ( MiscUtil.equal( this.raw, value.raw ) ) && this.validation.equals( value.validation ) &&
               ( MiscUtil.equal( this.defaultText, value.defaultText ) ); 
    }
    
    @Override
    public int hashCode()
    {
        int hashCode = parent().hashCode();
        hashCode = hashCode ^ this.property.hashCode();
        hashCode = hashCode ^ ( this.raw == null ? 1 : this.raw.hashCode() );
        
        return hashCode;
    }
    
    @Override
    public String toString()
    {
        return ( this.raw == null ? "<null>" : this.raw );
    }
    
    @SuppressWarnings( "unchecked" )
    
    private T parse( final String str )
    {
        if( str == null )
        {
            return null;
        }
        else
        {
            return (T) parent().service( this.property, ValueSerializationMasterService.class ).decode( str );
        }
    }
    
    private String normalize( String str )
    {
        if( str != null && str.length() == 0 )
        {
            str = null;
        }
        
        return str;
    }
    
}
