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

package org.eclipse.sapphire.modeling.xml.schema;

import javax.xml.namespace.QName;

import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class XmlContentModel
{
    protected final XmlDocumentSchema schema;
    protected final int minOccur;
    protected final int maxOccur;
    
    protected XmlContentModel( final XmlDocumentSchema schema,
                               final int minOccur,
                               final int maxOccur )
    {
        this.schema = schema;
        this.minOccur = minOccur;
        this.maxOccur = maxOccur;
    }
    
    public XmlDocumentSchema getSchema()
    {
        return this.schema;
    }
    
    public int getMinOccur()
    {
        return this.minOccur;
    }
    
    public int getMaxOccur()
    {
        return this.maxOccur;
    }
    
    public abstract XmlContentModel findChildElementContentModel( QName childElementName );
    
    public final int findInsertionPosition( final NodeList nodeList,
                                            final QName element )
    {
        final InsertionPosition point
            = findInsertionPosition( nodeList, nodeList.getLength(), element, new Position() );
        
        if( point.listIndex == -1 )
        {
            point.listIndex = nodeList.getLength();
        }
        
        return point.listIndex;
    }
    
    protected abstract InsertionPosition findInsertionPosition( NodeList nodeList,
                                                                int nodeListLength,
                                                                QName element,
                                                                Position position );
    
    protected abstract void toString( StringBuilder buf,
                                      String indent );

    @Override
    public final String toString()
    {
        final StringBuilder buf = new StringBuilder();
        toString( buf, "" ); //$NON-NLS-1$
        return buf.toString();
    }
    
    protected static final class InsertionPosition extends Position
    {
        public static final int G1_EXCEEDS_MAX_OCCUR = 1;
        public static final int G2_OK_TO_INSERT = 2;
        public static final int G3_OK_TO_INSERT = 3;
        public static final int G4_MEETS_OCCUR_REQUIREMENT = 4;
        
        public static final int MAX_GRADE = G4_MEETS_OCCUR_REQUIREMENT;

        public int grade;
        
        public InsertionPosition()
        {
            this.grade = 0;
            this.listIndex = -1;
        }
        
        public void merge( final InsertionPosition pt )
        {
            if( this.grade <= pt.grade && this.listIndex < pt.listIndex )
            {
                this.listIndex = pt.listIndex;
                this.grade = pt.grade;
            }
        }
        
        @Override
        public String toString()
        {
            final StringBuilder buf = new StringBuilder();
            buf.append( "(" ); //$NON-NLS-1$
            buf.append( "pos=" ); //$NON-NLS-1$
            buf.append( this.listIndex );
            buf.append( ",grade=" ); //$NON-NLS-1$
            buf.append( this.grade );
            buf.append( ")" ); //$NON-NLS-1$
            
            return buf.toString();
        }
    }

    protected static class Position
    {
        public int listIndex = 0;
    }
    
    public static abstract class Factory
    {
        protected int minOccur = 1;
        protected int maxOccur = 1;
        
        public final int getMinOccur()
        {
            return this.minOccur;
        }
        
        public final void setMinOccur( final int minOccur )
        {
            this.minOccur = minOccur;
        }
        
        public final int getMaxOccur()
        {
            return this.maxOccur;
        }
        
        public final void setMaxOccur( final int maxOccur )
        {
            this.maxOccur = maxOccur;
        }
        
        public abstract XmlContentModel create( final XmlDocumentSchema schema );
    }
    
}

