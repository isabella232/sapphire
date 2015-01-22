/******************************************************************************
 * Copyright (c) 2015 Oracle
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

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class Transient<T> extends Property
{
    private T content;

    public Transient( final Element element,
                      final TransientProperty property )
    {
        super( element, property );
    }
    
    /**
     * Returns a reference to Transient.class that is parameterized with the given type.
     * 
     * <p>Example:</p>
     * 
     * <p><code>Class&lt;Transient&lt;Integer>> cl = Transient.of( Integer.class );</code></p>
     *  
     * @param type the type
     * @return a reference to Transient.class that is parameterized with the given type
     */
    
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    
    public static <TX> Class<Transient<TX>> of( final Class<TX> type )
    {
        return (Class) Transient.class;
    }
    
    @Override
    public void refresh()
    {
        synchronized( root() )
        {
            init();
    
            refreshEnablement( false );
            refreshValidation( false );
        }
    }
    
    @Override
    public TransientProperty definition()
    {
        return (TransientProperty) super.definition();
    }
    
    public T content()
    {
        init();
        
        synchronized( this )
        {
            return this.content;
        }
    }
    
    @Override
    public boolean empty()
    {
        synchronized( root() )
        {
            init();
            
            return ( this.content == null );
        }
    }

    public void write( final T content )
    {
        init();
        
        PropertyEvent event = null;
        
        synchronized( this )
        {
            if( ! equal( this.content, content ) )
            {
                this.content = content;
                event = new PropertyContentEvent( this );
            }
        }
        
        if( event != null )
        {
            broadcast( event );
            
            refreshEnablement( false );
            refreshValidation( false );
        }
    }

    @Override
    public void clear()
    {
        write( null );
    }
    
    @Override
    @SuppressWarnings( "unchecked" )
    
    public void copy( final Element source )
    {
        if( source == null )
        {
            throw new IllegalArgumentException();
        }
        
        final Property p = source.property( (PropertyDef) definition() );
        
        if( p instanceof Transient<?> )
        {
            write( ( (Transient<T>) p ).content() );
        }
    }
    
    @Override
    @SuppressWarnings( "unchecked" )
    
    public void copy( final ElementData source )
    {
        if( source == null )
        {
            throw new IllegalArgumentException();
        }
        
        final Object content = source.read( name() );
        
        if( definition().getTypeClass().isInstance( content ) )
        {
            write( (T) content );
        }
        else
        {
            clear();
        }
    }
    
    @Override
    
    public boolean holds( final Element element )
    {
        if( element == null )
        {
            throw new IllegalArgumentException();
        }
        
        return false;
    }
    
    @Override
    
    public boolean holds( final Property property )
    {
        if( property == null )
        {
            throw new IllegalArgumentException();
        }
        
        return ( this == property );
    }
    
    @Override
    public String toString()
    {
        final T content = content();
        return ( content == null ? "<null>" : content.toString() );
    }
    
}
