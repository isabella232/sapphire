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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.sapphire.modeling.DoubleValueKeyword;
import org.eclipse.sapphire.modeling.FloatValueKeyword;
import org.eclipse.sapphire.modeling.IntegerValueKeyword;
import org.eclipse.sapphire.modeling.LongValueKeyword;
import org.eclipse.sapphire.modeling.ValueKeyword;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ValueProperty extends PropertyDef
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
    
    public ValueProperty( final ElementType type,
                          final String propertyName )
    {
        this( type, propertyName, null );
    }

    public ValueProperty( final ElementType type,
                          final ValueProperty baseProperty )
    {
        this( type, baseProperty.name(), baseProperty );
    }
    
    private ValueProperty( final ElementType type,
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
                
                if( ! result.equals( value ) )
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
                
                if( ! result.equals( value ) )
                {
                    break;
                }
            }
        }
        
        return result;
    }
    
}
