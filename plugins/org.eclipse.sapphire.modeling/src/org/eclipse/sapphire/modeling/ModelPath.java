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

package org.eclipse.sapphire.modeling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ModelPath
{
    private static final ModelRootSegment MODEL_ROOT_SEGMENT = new ModelRootSegment();
    private static final ParentElementSegment PARENT_ELEMENT_SEGMENT = new ParentElementSegment();
    private static final AllSiblingsSegment ALL_SIBLINGS_SEGMENT = new AllSiblingsSegment();
    private static final AllDescendentsSegment ALL_DESCENDENTS_SEGMENT = new AllDescendentsSegment();
    
    public static final ModelPath ALL_DESCENDENTS = new ModelPath( "*" );
    
    private final List<Segment> segments;
    private final int offset;

    public ModelPath( final String path ) throws MalformedPathException
    {
        this( parse( path ), 0 );
    }
    
    private ModelPath( final List<Segment> segments,
                       final int offset )
    
        throws MalformedPathException
        
    {
        this.segments = segments;
        this.offset = offset;
    }
    
    public int length()
    {
        return this.segments.size() - this.offset;
    }
    
    public Segment segment( final int index )
    {
        return this.segments.get( index );
    }
    
    public Segment head()
    {
        return this.segments.get( this.offset );
    }
    
    public ModelPath tail()
    
        throws MalformedPathException
        
    {
        return new ModelPath( this.segments, this.offset + 1 );
    }
    
    public ModelPath append( final ModelPath path )
    
        throws MalformedPathException
        
    {
        final List<Segment> segments = new ArrayList<Segment>();
        
        for( int i = this.offset, n = this.segments.size(); i < n; i++ )
        {
            segments.add( this.segments.get( i ) );
        }
        
        for( int i = path.offset, n = path.segments.size(); i < n; i++ )
        {
            segments.add( path.segments.get( i ) );
        }
        
        return new ModelPath( segments, 0 );
    }
    
    public boolean isPrefixOf( final ModelPath path )
    {
        final int length = length();
        
        if( length < path.length() )
        {
            for( int i = 0; i < length; i++ )
            {
                if( ! segment( 0 ).equals( path.segment( 0 ) ) )
                {
                    return false;
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    public ModelPath makeRelativeTo( final ModelPath path )
    {
        if( length() == 1 )
        {
            throw new IllegalArgumentException();
        }
        
        if( head().equals( path.head() ) )
        {
            if( path.length() == 1 )
            {
                return tail();
            }
            else
            {
                return tail().makeRelativeTo( path.tail() );
            }
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }
    
    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof ModelPath )
        {
            final ModelPath p = (ModelPath) obj;
            final int size = this.segments.size();
            
            if( size == p.segments.size() )
            {
                for( int i = 0; i < size; i++ )
                {
                    if( ! this.segments.get( i ).equals( p.segments.get( i ) ) )
                    {
                        return false;
                    }
                }
                
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public int hashCode()
    {
        int hashCode = 0;
        
        for( Segment segment : this.segments )
        {
            hashCode += segment.hashCode();
        }
        
        return hashCode;
    }
    
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        
        for( int i = this.offset, n = this.segments.size(); i < n; i++ )
        {
            final int buflen = buf.length();
            
            if( buflen > 0 && buf.charAt( buflen - 1 ) != '/' )
            {
                buf.append( '/' );
            }
            
            buf.append( this.segments.get( i ).toString() );
        }
        
        return buf.toString();
    }
    
    private static List<Segment> parse( String path )
    {
        final List<Segment> segments = new ArrayList<Segment>();
        
        path = path.trim();
        
        if( path.startsWith( "/" ) )
        {
            segments.add( MODEL_ROOT_SEGMENT );
            path = path.substring( 1 );
        }
        
        for( String part : path.split( "/" ) )
        {
            if( part.length() == 0 )
            {
                throw new IllegalArgumentException( path );
            }
            
            if( part.equals( ".." ) )
            {
                segments.add( PARENT_ELEMENT_SEGMENT );
            }
            else if( part.equals( "*" ) )
            {
                segments.add( ALL_DESCENDENTS_SEGMENT );
            }
            else if( part.equals( "#" ) )
            {
                segments.add( ALL_SIBLINGS_SEGMENT );
            }
            else
            {
                int openBracket = part.indexOf( '[' );
                
                if( openBracket != -1 )
                {
                    int closeBracket = part.indexOf( ']' );
                    
                    if( ( closeBracket - openBracket ) <= 1 )
                    {
                        throw new IllegalArgumentException( path );
                    }

                    if( openBracket == 0 || closeBracket != ( part.length() - 1 ) )
                    {
                        throw new IllegalArgumentException( path );
                    }
                    
                    segments.add( new PropertySegment( part.substring( 0, openBracket ) ) );
                    segments.add( new TypeFilterSegment( part.substring( openBracket + 1, closeBracket ) ) );
                }
                else
                {
                    segments.add( new PropertySegment( part ) );
                }
            }
        }
        
        if( segments.size() > 1 && segments.get( 0 ) instanceof AllDescendentsSegment )
        {
            segments.set( 0, ALL_SIBLINGS_SEGMENT );
        }
        
        return segments;
    }
    
    public static abstract class Segment
    {
    }
    
    public static final class ModelRootSegment extends Segment
    {
        private ModelRootSegment()
        {
        }
        
        @Override
        public String toString()
        {
            return "/";
        }
    }
    
    public static final class ParentElementSegment extends Segment
    {
        private ParentElementSegment()
        {
        }
        
        @Override
        public String toString()
        {
            return "..";
        }
    }
    
    public static final class AllSiblingsSegment extends Segment
    {
        private AllSiblingsSegment()
        {
        }
        
        @Override
        public String toString()
        {
            return "#";
        }
    }
    
    public static final class AllDescendentsSegment extends Segment
    {
        private AllDescendentsSegment()
        {
        }
        
        @Override
        public String toString()
        {
            return "*";
        }
    }
    
    public static final class PropertySegment extends Segment
    {
        private final String property;
        
        private PropertySegment( final String property )
        {
            this.property = property;
        }
        
        public String getPropertyName()
        {
            return this.property;
        }
        
        @Override
        public boolean equals( final Object obj )
        {
            if( obj instanceof PropertySegment )
            {
                return this.property.equals( ( (PropertySegment) obj  ).property );
            }
            
            return false;
        }
        
        @Override
        public int hashCode()
        {
            return this.property.hashCode();
        }
        
        @Override
        public String toString()
        {
            return this.property;
        }
    }
    
    public static final class TypeFilterSegment extends Segment
    {
        private final Set<String> types;
        
        private TypeFilterSegment( final String expr )
        {
            if( ! expr.startsWith( "#type=" ) || expr.length() < 7 )
            {
                throw new IllegalArgumentException( expr );
            }
            
            final String typesAsString = expr.substring( 6 ).trim();
            final Set<String> types = new HashSet<String>();
            
            for( String type : typesAsString.split( "," ) )
            {
                types.add( type );
            }
            
            this.types = Collections.unmodifiableSet( types );
        }
        
        public Set<String> getTypes()
        {
            return this.types;
        }
        
        @Override
        public boolean equals( final Object obj )
        {
            if( obj instanceof TypeFilterSegment )
            {
                return this.types.equals( ( (TypeFilterSegment) obj  ).types );
            }
            
            return false;
        }
        
        @Override
        public int hashCode()
        {
            return this.types.hashCode();
        }
        
        @Override
        public String toString()
        {
            final StringBuilder buf = new StringBuilder();
            
            buf.append( "[#type=" );
            boolean first = true;
            
            for( String type : this.types )
            {
                if( ! first )
                {
                    buf.append( ',' );
                }
                
                buf.append( type );
                first = false;
            }
            
            buf.append( ']' );
            
            return buf.toString();
        }
    }
    
    @SuppressWarnings( "serial" )
    public static final class MalformedPathException extends RuntimeException
    {
        public MalformedPathException( final String message )
        {
            super( message );
        }
    }

}
