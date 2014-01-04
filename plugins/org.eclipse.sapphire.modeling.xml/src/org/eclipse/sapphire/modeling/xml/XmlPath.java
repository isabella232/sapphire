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

package org.eclipse.sapphire.modeling.xml;

import static org.eclipse.sapphire.modeling.xml.XmlUtil.createQualifiedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class XmlPath
{
    private final List<Segment> segments = new ArrayList<Segment>();
    private final List<Segment> segmentsReadOnly = Collections.unmodifiableList( this.segments );

    public XmlPath( final List<Segment> segments )
    {
        this.segments.addAll( segments );
    }
    
    public XmlPath( final String path )
    {
        this( path, null );
    }
    
    public XmlPath( final String path,
                    final XmlNamespaceResolver xmlNamespaceResolver )
    {
        for( String part : path.split( "/" ) )
        {
            if( part.length() == 0 )
            {
                continue;
            }
            
            boolean isAttribute = false;
            boolean isComment = false;
            
            if( part.startsWith( "@" ) )
            {
                part = part.substring( 1 );
                isAttribute = true;
            }
            else if( part.startsWith( "%" ) )
            {
                part = part.substring( 1 );
                isComment = true;
            }
            
            this.segments.add( new Segment( createQualifiedName( part, xmlNamespaceResolver ), isAttribute, isComment ) );
        }
    }
    
    public List<Segment> getSegments()
    {
        return this.segmentsReadOnly;
    }
    
    public Segment getSegment( final int index )
    {
        return this.segments.get( index );
    }
    
    public int getSegmentCount()
    {
        return this.segments.size();
    }
    
    public static final class Segment
    {
        private final QName qname;
        private final boolean isAttribute;
        private final boolean isComment;
        
        public Segment( final QName qname,
                        final boolean isAttribute,
                        final boolean isComment )
        {
            this.qname = qname;
            this.isAttribute = isAttribute;
            this.isComment = isComment;
        }
        
        public QName getQualifiedName()
        {
            return this.qname;
        }
        
        public boolean isAttribute()
        {
            return this.isAttribute;
        }
        
        public boolean isComment()
        {
            return this.isComment;
        }
    }
    
}
