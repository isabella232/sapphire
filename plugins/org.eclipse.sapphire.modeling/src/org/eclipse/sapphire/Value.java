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

import static org.eclipse.sapphire.modeling.util.MiscUtil.equal;

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.annotations.Derived;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.services.DefaultValueService;
import org.eclipse.sapphire.services.DerivedValueService;
import org.eclipse.sapphire.services.ValueNormalizationService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class Value<T> extends Property
{
    private static final int DEFAULT_CONTENT_INITIALIZED = 1 << 4;
    
    private String text;
    private T content;
    private String defaultText;
    private T defaultContent;
    
    public Value( final Element element,
                  final ValueProperty property )
    {
        super( element, property );
    }
    
    /**
     * Returns a reference to Value.class that is parameterized with the given type.
     * 
     * <p>Example:</p>
     * 
     * <p><code>Class&lt;Value&lt;Integer>> cl = Value.of( Integer.class );</code></p>
     *  
     * @param type the type
     * @return a reference to Value.class that is parameterized with the given type
     */
    
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    
    public static <TX> Class<Value<TX>> of( final Class<TX> type )
    {
        return (Class) Value.class;
    }
    
    @Override
    public final void refresh()
    {
        synchronized( root() )
        {
            init();
            
            refreshContent( false );
            refreshDefaultContent( false );
            refreshEnablement( false );
            refreshValidation( false );
        }
    }
    
    private void refreshContent( final boolean onlyIfNotInitialized )
    {
        boolean initialized;
        
        synchronized( this )
        {
            initialized = ( ( this.initialization & CONTENT_INITIALIZED ) != 0 ); 
        }
        
        if( ! initialized || ! onlyIfNotInitialized )
        {
            final ValueProperty p = definition();
            
            String afterText;
            
            if( p.hasAnnotation( Derived.class ) )
            {
                final DerivedValueService derivedValueService = service( DerivedValueService.class );
                
                if( ! initialized )
                {
                    final Listener listener = new Listener()
                    {
                        @Override
                        public void handle( final Event event )
                        {
                            refreshContent( false );
                        }
                    };
                    
                    derivedValueService.attach( listener );
                }
                
                afterText = derivedValueService.value();
            }
            else
            {
                afterText = binding().read();
            }
            
            afterText = normalize( service( ValueNormalizationService.class ).normalize( p.encodeKeywords( afterText ) ) );
            
            final boolean proceed;
            
            synchronized( this )
            {
                initialized = ( ( this.initialization & CONTENT_INITIALIZED ) != 0 );
                proceed = ( ! initialized || ! equal( this.text, afterText ) );
            }
            
            if( proceed )
            {
                final T afterContent = parse( p.decodeKeywords( afterText ) );
                
                PropertyContentEvent event = null; 
                
                synchronized( this )
                {
                    this.text = afterText;
                    this.content = afterContent;
                    
                    if( initialized )
                    {
                        event = new PropertyContentEvent( this );
                    }
                    else
                    {
                        this.initialization |= CONTENT_INITIALIZED;
                    }
                }
                
                broadcast( event );
            }
        }
    }
    
    private void refreshDefaultContent( final boolean onlyIfNotInitialized )
    {
        boolean initialized;
        
        synchronized( this )
        {
            initialized = ( ( this.initialization & DEFAULT_CONTENT_INITIALIZED ) != 0 );
        }
        
        if( ! initialized || ! onlyIfNotInitialized )
        {
            final ValueProperty p = definition();
            
            String afterText = null;
            
            final DefaultValueService defaultValueService = service( DefaultValueService.class );
            
            if( defaultValueService != null )
            {
                if( ! initialized )
                {
                    final Listener listener = new Listener()
                    {
                        @Override
                        public void handle( final Event event )
                        {
                            refreshDefaultContent( false );
                        }
                    };
                    
                    defaultValueService.attach( listener );
                }
                
                afterText = defaultValueService.value();
                
                if( afterText != null )
                {
                    afterText = normalize( service( ValueNormalizationService.class ).normalize( p.encodeKeywords( afterText ) ) );
                }
            }
            
            final boolean proceed;
            
            synchronized( this )
            {
                initialized = ( ( this.initialization & DEFAULT_CONTENT_INITIALIZED ) != 0 );
                proceed = ( ! initialized || ! equal( this.defaultText, afterText ) );
            }
            
            if( proceed )
            {
                final T afterContent = parse( p.decodeKeywords( afterText ) );
                
                PropertyDefaultEvent event = null; 
                
                synchronized( this )
                {
                    this.defaultText = afterText;
                    this.defaultContent = afterContent;
                    
                    if( initialized )
                    {
                        event = new PropertyDefaultEvent( this );
                    }
                    else
                    {
                        this.initialization |= DEFAULT_CONTENT_INITIALIZED;
                    }
                }
                
                broadcast( event );
            }
        }
    }
    
    @Override
    public final ValueProperty definition()
    {
        return (ValueProperty) super.definition();
    }
    
    @Override
    protected final ValuePropertyBinding binding()
    {
        return (ValuePropertyBinding) super.binding();
    }
    
    public final String text()
    {
        return text( true );
    }
    
    public final String text( final boolean useDefaultValue )
    {
        init();
        
        refreshContent( true );
        
        synchronized( this )
        {
            if( this.text != null )
            {
                return this.text;
            }
        }
        
        if( useDefaultValue )
        {
            refreshDefaultContent( true );
            
            synchronized( this )
            {
                return this.defaultText;
            }
        }

        return null;
    }
    
    public final String localized()
    {
        return localized( true );
    }
    
    public final String localized( final boolean useDefaultValue )
    {
        return localized( useDefaultValue, CapitalizationType.NO_CAPS, true );
    }
    
    public final String localized( final CapitalizationType capitalizationType,
                                   final boolean includeMnemonic )
    {
        return localized( true, capitalizationType, includeMnemonic );
    }
    
    public final String localized( final boolean useDefaultValue,
                                   final CapitalizationType capitalizationType,
                                   final boolean includeMnemonic )
    {
        final String sourceLangText = text( useDefaultValue );
        
        if( sourceLangText != null )
        {
            return element().adapt( LocalizationService.class ).text( sourceLangText, capitalizationType, includeMnemonic );
        }
        
        return null;
    }
    
    public final T content()
    {
        return content( true );
    }
    
    public final T content( final boolean useDefaultValue )
    {
        init();
        
        refreshContent( true );
        
        synchronized( this )
        {
            if( this.content != null )
            {
                return this.content;
            }
        }
        
        if( useDefaultValue )
        {
            refreshDefaultContent( true );
            
            synchronized( this )
            {
                return this.defaultContent;
            }
        }

        return null;
    }
    
    @Override
    public final boolean empty()
    {
        synchronized( root() )
        {
            init();
            refreshContent( true );
            
            return ( this.text == null );
        }
    }

    public final T getDefaultContent()
    {
        init();
        refreshDefaultContent( true );
        
        return this.defaultContent;
    }
    
    public final String getDefaultText()
    {
        init();
        
        refreshDefaultContent( true );
        
        return this.defaultText;
    }
    
    public final boolean malformed()
    {
        init();
        
        refreshContent( true );
        
        synchronized( this )
        {
            if( this.text != null )
            {
                return ( this.content == null );
            }
        }
        
        refreshDefaultContent( true );
        
        synchronized( this )
        {
            return ( this.defaultText != null && this.defaultContent == null );
        }
    }
    
    public final void write( final Object content )
    {
        init();
        
        // TODO: Read-only?

        final ValueProperty p = definition();
        
        String text = null;
        
        if( content != null )
        {
            if( content instanceof String )
            {
                text = (String) content;
            }
            else
            {
                text = service( MasterConversionService.class ).convert( content, String.class );
                
                if( text == null )
                {
                    throw new IllegalArgumentException();
                }
            }
        }
        
        text = normalize( service( ValueNormalizationService.class ).normalize( p.decodeKeywords( text ) ) );
        
        if( ! equal( text( false ), text ) )
        {
            binding().write( text );
            refresh();
        }
    }
    
    @Override
    public final void clear()
    {
        write( null );
    }
    
    @Override
    public final void copy( final Element source )
    {
        if( source == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( definition().isReadOnly() )
        {
            throw new UnsupportedOperationException();
        }
        
        final Property p = source.property( (PropertyDef) definition() );
        
        if( p instanceof Value<?> )
        {
            write( ( (Value<?>) p ).text( false ) );
        }
    }
    
    @Override
    public final String toString()
    {
        final String text = text( false );
        return ( text == null ? "<null>" : text );
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
            return (T) service( MasterConversionService.class ).convert( str, definition().getTypeClass() );
        }
    }
    
    private static String normalize( String str )
    {
        if( str != null && str.length() == 0 )
        {
            str = null;
        }
        
        return str;
    }
    
}
