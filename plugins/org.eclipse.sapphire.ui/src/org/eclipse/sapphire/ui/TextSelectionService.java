/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation review and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceEvent;
import org.eclipse.sapphire.util.EqualsFactory;
import org.eclipse.sapphire.util.HashCodeFactory;

/**
 * TextSelectionService is a conduit between the presentation layer and anything that needs 
 * to observe or change the selection in a text property editor.
 * 
 * <p>This service is not intended to be implemented by adopters.</p>
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TextSelectionService extends Service
{
    private Range selection = new Range( 0, 0 );

    /**
     * Returns the current text selection.
     */
    
    public Range selection()
    {
        return this.selection;
    }

    /**
     * Places the caret at the specified position. This is equivalent to setting the selection to a zero-length
     * range starting at this position. If selection changes, TextSelectionEvent will be fired.
     * 
     * @param position the desired caret position
     */
    
    public void select( final int position )
    {
        select( position, position );
    }

    /**
     * Selects a text range. If selection changes, TextSelectionEvent will be fired.
     * 
     * @param start the starting position of the text range
     * @param end the ending position of the text range (non-inclusive)
     */
    
    public void select( final int start, final int end )
    {
        select( new Range( start, end ) );
    }
    
    /**
     * Selects a text range. If selection changes, TextSelectionEvent will be fired.
     * 
     * @param start the starting position of the text range
     * @param end the ending position of the text range (non-inclusive)
     */
    
    public void select( final Range range )
    {
        if( range == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( ! this.selection.equals( range ) )
        {
            final Range before = this.selection;
            
            this.selection = range;
            
            broadcast( new TextSelectionEvent( this, before, this.selection ) );
        }
    }
    
    /**
     * Represents an immutable range of characters defined by a starting and an ending position.
     */
    
    public static final class Range
    {
        private final int start;
        private final int end;
        
        /**
         * Constructs a new Range object.
         * 
         * @param start the starting position of the text range
         * @param end the ending position of the text range (non-inclusive)
         */
        
        public Range( final int start, final int end )
        {
            if( start < 0 )
            {
                throw new IllegalArgumentException();
            }
            
            if( end < start )
            {
                throw new IllegalArgumentException();
            }
            
            this.start = start;
            this.end = end;
        }
        
        @Override
        
        public boolean equals( final Object obj )
        {
            if( obj instanceof Range )
            {
                final Range range = (Range) obj;
                return EqualsFactory.start().add( this.start, range.start ).add( this.end, range.end ).result();
            }
            
            return false;
        }

        @Override
        
        public int hashCode()
        {
            return HashCodeFactory.start().add( this.start ).add( this.end ).result();
        }
        
        /**
         * Returns the starting position of the text range.
         */

        public int start()
        {
            return this.start;
        }
        
        /**
         * Returns the ending position of the text range.
         */
        
        public int end()
        {
            return this.end;
        }
        
        /**
         * Returns the length of the text range.
         */
        
        public int length()
        {
            return this.end - this.start;
        }
        
        @Override
        
        public String toString()
        {
            return "[" + this.start + "," + this.end + ")";
        }
    }
    
    /**
     * The event that is fired when text selection changes.
     */

    public static final class TextSelectionEvent extends ServiceEvent
    {
        private Range before;
        private Range after;

        TextSelectionEvent( final TextSelectionService service, final Range before, final Range after )
        {
            super( service );
            
            this.before = before;
            this.after = after;
        }
        
        /**
         * Returns the text selection before the selection was changed.
         */

        public Range before()
        {
            return this.before;
        }
        
        /**
         * Returns the text selection after the selection was changed.
         */

        public Range after()
        {
            return this.after;
        }
    }

}
