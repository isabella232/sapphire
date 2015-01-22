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

import org.eclipse.sapphire.services.ReferenceService;
import org.eclipse.sapphire.services.ServiceEvent;

/**
 * A common implementation base for {@link ReferenceService} implementations that resolve to an {@link Element}.
 * 
 * <p>In many situations, the reference semantics can be specified using the @{@link ElementReference} annotation rather than through
 * a custom {@link ElementReferenceService} implementation.</p>
 * 
 * <pre><code><font color="#888888"> {@literal @}Reference( target = Table.class )</font>
 * {@literal @}ElementReference( list = "/Tables", key = "Name" )
 * 
 * <font color="#888888">ValueProperty PROP_TABLE = new ValueProperty( TYPE, "Table" );
 * 
 * ReferenceValue&lt;String,Table> getTable();
 * void setTable( String value );</font></code></pre>
 * 
 * <p>When more control is necessary, a custom implementation of {@link ElementReferenceService} can be provided. This is
 * necessary, for example, when the referenced elements are located in a different model or when the list and key are variable.</p>
 * 
 * <pre><code> <font color="#888888">{@literal @}Reference( target = Table.class )</font>
 * {@literal @}Service( impl = ExampleElementReferenceService.class )
 * 
 * <font color="#888888">ValueProperty PROP_TABLE = new ValueProperty( TYPE, "Table" );
 * 
 * ReferenceValue&lt;String,Table> getTable();
 * void setTable( String value );</font></code></pre>
 * 
 * <p>A PossibleValuesService implementation is automatically provided when this service is implemented.</p>
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class ElementReferenceService extends ReferenceService<Element>
{
    private ElementList<?> list;
    private String key;
    private Listener listener;
    
    /**
     * Returns the list containing elements being referenced. If the list returned by this method changes to another list,
     * a ListEvent must be broadcasted.
     */
    
    public abstract ElementList<?> list();
    
    /**
     * Returns the path through the model from an element in the list to the value property used by the reference. If the
     * path returned by this method changes, a KeyEvent must be broadcasted.
     */
    
    public abstract String key();
    
    @Override
    protected void initReferenceService()
    {
        this.listener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                if( event instanceof ValuePropertyContentEvent )
                {
                    final ValuePropertyContentEvent evt = (ValuePropertyContentEvent) event;
                    final Element element = evt.property().element();
                    
                    if( element == target() && evt.refactor() )
                    {
                        context( Value.class ).write( evt.after() );
                    }
                }
                
                refresh();
            }
        };

        attach
        (
            new FilteredListener<SourceEvent>()
            {
                @Override
                protected void handleTypedEvent( final SourceEvent event )
                {
                    refresh();
                }
            }
        );
    }

    @Override
    protected final Element compute()
    {
        final Value<?> reference = context( Value.class );
        final String text = reference.text();
        final ElementList<?> list = list();
        final String key = key();
        
        if( this.list != list || ! equal( this.key, key ) )
        {
            if( this.list != null )
            {
                if( ! this.list.disposed() )
                {
                    this.list.detach( this.listener, this.key );
                }
                
                this.list = null;
            }

            this.list = list;
            this.key = key;
            
            if( list != null )
            {
                this.list.attach( this.listener, this.key );
            }
        }
        
        if( list != null && text != null )
        {
            for( final Element element : list )
            {
                final String n = ( (Value<?>) element.property( this.key ) ).text();
                
                if( n != null && n.equals( text ) )
                {
                    return element;
                }
            }
        }
        
        return null;
    }

    @Override
    public void dispose()
    {
        if( this.list != null )
        {
            if( ! this.list.disposed() )
            {
                this.list.detach( this.listener, this.key );
            }
            
            this.list = null;
        }
        
        this.key = null;
        this.listener = null;
        
        super.dispose();
    }
    
    /**
     * An event that's broadcast when the list changes.
     */
    
    public final class ListEvent extends SourceEvent
    {
    }

    /**
     * An event that's broadcast when the key changes.
     */
    
    public final class KeyEvent extends SourceEvent
    {
    }

    /**
     * The common base class for {@link ListEvent} and {@link KeyEvent}.
     */
    
    public abstract class SourceEvent extends ServiceEvent
    {
        public SourceEvent()
        {
            super( ElementReferenceService.this );
        }
    }

}
