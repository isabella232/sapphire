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

package org.eclipse.sapphire.ui.def;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SapphireKeySequence
{
    public enum Modifier
    {
        SHIFT,
        ALT,
        CONTROL
    }
    
    /**
     * ASCII character convenience constant for the delete character
     * (value is the <code>char</code> with value 127).
     */
    
    public static final char DEL = 0x7F;
    
    /**
     * Accelerator constant used to differentiate a key code from a
     * unicode character.
     * 
     * If this bit is set, then the key stroke
     * portion of an accelerator represents a key code.  If this bit
     * is not set, then the key stroke portion of an accelerator is
     * a unicode character.
     * 
     * The following expression is false:
     * 
     * <code>((SWT.MOD1 | SWT.MOD2 | 'T') & SWT.KEYCODE_BIT) != 0</code>.
     * 
     * The following expression is true:
     * 
     * <code>((SWT.MOD3 | SWT.F2) & SWT.KEYCODE_BIT) != 0</code>.
     * 
     * (value is (1&lt;&lt;24))
     */
    
    public static final int KEYCODE_BIT = (1 << 24);
    
    /**
     * Keyboard event constant representing the UP ARROW key
     * (value is (1&lt;&lt;24)+1).
     */
    
    public static final int ARROW_UP = KEYCODE_BIT + 1;

    /**
     * Keyboard event constant representing the DOWN ARROW key
     * (value is (1&lt;&lt;24)+2).
     */
    
    public static final int ARROW_DOWN = KEYCODE_BIT + 2;
    
	/**
	 * Keyboard event constant representing the F2 key
	 * (value is (1&lt;&lt;24)+11).
	 */
    
    public static final int F2 = KEYCODE_BIT + 11;
    
    private static final char SEPARATOR = '+';
    private static final String SYMBOL_SHIFT = "SHIFT";
    private static final String SYMBOL_ALT = "ALT";
    private static final String SYMBOL_CONTROL = "CONTROL";
    private static final String SYMBOL_ENTER = "ENTER";
    private static final String SYMBOL_DEL = "DEL";
    private static final String SYMBOL_ARROW_UP = "ARROW_UP";
    private static final String SYMBOL_ARROW_DOWN = "ARROW_DOWN";
    private static final String SYMBOL_F2 = "F2";
    
    private final Set<Modifier> modifiers;
    private final int keyCode;
    
    public SapphireKeySequence( final String definition )
    {
        final Set<Modifier> modifiers = new HashSet<Modifier>();
        
        int keyCode = -1;
        
        for( String segment : definition.split( "\\" + SEPARATOR ) )
        {
            if( segment.equalsIgnoreCase( SYMBOL_SHIFT ) )
            {
                if( modifiers.contains( Modifier.SHIFT ) )
                {
                    throw new IllegalArgumentException();
                }
                
                modifiers.add( Modifier.SHIFT );
            }
            else if( segment.equalsIgnoreCase( SYMBOL_ALT ) )
            {
                if( modifiers.contains( Modifier.ALT ) )
                {
                    throw new IllegalArgumentException();
                }
                
                modifiers.add( Modifier.ALT );
            }
            else if( segment.equalsIgnoreCase( SYMBOL_CONTROL ) )
            {
                if( modifiers.contains( Modifier.CONTROL ) )
                {
                    throw new IllegalArgumentException();
                }
                
                modifiers.add( Modifier.CONTROL );
            }
            else if( segment.equalsIgnoreCase( SYMBOL_ENTER ) )
            {
                if( keyCode != -1 )
                {
                    throw new IllegalArgumentException();
                }
                
                keyCode = '\r';
            }
            else if( segment.equalsIgnoreCase( SYMBOL_DEL ) )
            {
                if( keyCode != -1 )
                {
                    throw new IllegalArgumentException();
                }
                
                keyCode = DEL;
            }
            else if( segment.equalsIgnoreCase( SYMBOL_ARROW_UP ) )
            {
                if( keyCode != -1 )
                {
                    throw new IllegalArgumentException();
                }
                
                keyCode = ARROW_UP;
            }
            else if( segment.equalsIgnoreCase( SYMBOL_ARROW_DOWN ) )
            {
                if( keyCode != -1 )
                {
                    throw new IllegalArgumentException();
                }
                
                keyCode = ARROW_DOWN;
            }
            else if( segment.equalsIgnoreCase( SYMBOL_F2 ) )
            {
                if( keyCode != -1 )
                {
                    throw new IllegalArgumentException();
                }
                
                keyCode = F2;
            }
            else if( segment.length() == 1 )
            {
                if( keyCode != -1 )
                {
                    throw new IllegalArgumentException();
                }
                
                keyCode = segment.charAt( 0 );
            }
            else
            {
                throw new IllegalArgumentException();
            }
        }
        
        if( keyCode == -1 )
        {
            throw new IllegalArgumentException();
        }
        
        this.modifiers = Collections.unmodifiableSet( modifiers );
        this.keyCode = keyCode;
    }
    
    public Set<Modifier> getModifiers()
    {
        return this.modifiers;
    }
    
    public int getKeyCode()
    {
        return this.keyCode;
    }
    
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        
        for( Modifier modifier : Modifier.values() )
        {
            if( buf.length() > 0 )
            {
                buf.append( SEPARATOR );
            }
            
            buf.append( modifier.name() );
        }
        
        if( buf.length() > 0 )
        {
            buf.append( SEPARATOR );
        }
        
        buf.append( (char) this.keyCode );
        
        return buf.toString();
    }
    
}
