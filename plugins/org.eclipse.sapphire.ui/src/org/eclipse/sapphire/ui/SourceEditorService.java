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

package org.eclipse.sapphire.ui;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.PropertyDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class SourceEditorService
{
    /**
     * Determines if locating the specified element and property in the source editor
     * is possible. 
     * 
     * <p>The model element parameter must always be specified, but the property parameter
     * is optional. If property is omitted, the entire text associated with model element
     * should be shown.</p>
     * 
     * @param element the model element
     * @param property the property or null
     * @return true if locating the specified element and property in the source editor is possible
     */
    
    public abstract boolean find( Element element,
                                  PropertyDef property );
    
    /**
     * Locates the specified element and property in the source editor. The appropriate
     * block of text should be selected and revealed in the editor. If an exact match
     * cannot be made, the closest relevant block of text should be shown.
     * 
     * <p>The model element parameter must always be specified, but the property parameter
     * is optional. If property is omitted, the entire text associated with model element
     * should be shown.</p>
     * 
     * @param element the model element
     * @param property the property or null
     */
    
    public abstract void show( Element element,
                               PropertyDef property );
    
    public static final class Range
    {
        private boolean initialized = false;
        private int start;
        private int end;
        
        public Range()
        {
            this.initialized = false;
        }
        
        public boolean initialized()
        {
            return this.initialized;
        }
        
        public int start()
        {
            return this.start;
        }
        
        public int end()
        {
            return this.end;
        }
        
        public void merge( final int start,
                           final int end )
        {
            if( this.initialized )
            {
                this.start = ( start < this.start ? start : this.start );
                this.end = ( end > this.end ? end : this.end );
            }
            else
            {
                this.start = start;
                this.end = end;
                this.initialized = true;
            }
        }
    }
    
}
