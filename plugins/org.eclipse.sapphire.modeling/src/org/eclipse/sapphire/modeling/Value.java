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

import java.util.Locale;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.sapphire.modeling.annotations.ModelPropertyValidator;
import org.eclipse.sapphire.modeling.serialization.ValueSerializationService;
import org.eclipse.sapphire.modeling.util.internal.MiscUtil;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class Value<T>

    extends ModelParticle
    
{
    private final ValueProperty property;
    private final String raw;
    private final T parsed;
    private IStatus valres;
    private boolean defaultValueInitialized;
    private String defaultText;
    private T defaultContent;

    public Value( final IModelElement parent,
                  final ValueProperty property,
                  final String value )
    {
        super( parent );
        
        this.property = property;
        this.raw = normalize( value );
        this.parsed = parse( property.decodeKeywords( this.raw ) );
        this.valres = null;
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
    
    @SuppressWarnings( "unchecked" )
    
    private void initValidation()
    {
        if( this.valres == null )
        {
            final ModelPropertyValidator<Value<?>> validator = (ModelPropertyValidator<Value<?>>) this.property.getValidator();
            
            if( validator == null )
            {
                this.valres = Status.OK_STATUS;
            }
            else
            {
                this.valres = validator.validate( this );
            }
        }
    }

    private void initDefaultValue()
    {
        if( ! this.defaultValueInitialized )
        {
            this.defaultText = normalize( getParent().service().getDefaultValue( this.property ) );
            this.defaultContent = parse( this.property.decodeKeywords( this.defaultText ) );
            this.defaultValueInitialized = true;
        }
    }

    @Override
    public IModelElement getParent()
    {
        return (IModelElement) super.getParent();
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
        return getLocalizedText( true, Locale.getDefault() );
    }
    
    public String getLocalizedText( final boolean useDefaultValue )
    {
        return getLocalizedText( useDefaultValue, Locale.getDefault() );
    }
    
    public String getLocalizedText( final boolean useDefaultValue,
                                    final Locale locale )
    {
        final String originalText = getText( useDefaultValue );
        
        if( originalText != null )
        {
            final ModelStore modelStore = getModel().getModelStore();
            return modelStore.getLocalizedText( originalText, locale );
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
    
    public IStatus validate()
    {
        initValidation();
        return this.valres;
    }
    
    @Override
    public boolean equals( final Object val )
    {
        if( this == val )
        {
            return true;
        }
        
        if( val == null )
        {
            return false;
        }
        
        init();
        
        final Value<?> value = (Value<?>) val;
        
        return ( getParent() == value.getParent() ) && ( this.property == value.property ) &&
               ( MiscUtil.equal( this.raw, value.raw ) ) && equal( this.valres, value.valres ) &&
               ( MiscUtil.equal( this.defaultText, value.defaultText ) ); 
    }
    
    @Override
    public int hashCode()
    {
        int hashCode = getParent().hashCode();
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
            return (T) getParent().service( ValueSerializationService.class ).decode( this.property, str );
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
    
    private static boolean equal( final IStatus x,
                                  final IStatus y )
    {
        return x.getMessage().equals( y.getMessage() ) && ( x.getSeverity() == y.getSeverity());
    }
    
}
