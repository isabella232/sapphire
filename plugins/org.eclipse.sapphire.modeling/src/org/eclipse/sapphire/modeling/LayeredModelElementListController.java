/******************************************************************************
 * Copyright (c) 2011 Oracle
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
import java.util.HashMap;
import java.util.List;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class LayeredModelElementListController<T extends IModelElement,B>

    extends ModelElementListController<T>

{
    protected List<T> refresh( final List<T> content,
                               final List<B> newBaseContent )
    {
        final int newContentSize = newBaseContent.size();
        
        if( content.size() == newContentSize )
        {
            boolean equal = true;
            
            for( int i = 0; i < newContentSize; i++ )
            {
                if( ! unwrap( content.get( i ) ).equals( newBaseContent.get( i ) ) )
                {
                    equal = false;
                    break;
                }
            }
            
            if( equal )
            {
                return content;
            }
        }
        
        final List<T> newContent = new ArrayList<T>( newContentSize );
        
        if( content.size() > 5 && newContentSize > 5 )
        {
            final HashMap<B,T> map = new HashMap<B,T>();
            
            for( T x : content )
            {
                map.put( unwrap( x ), x );
            }
            
            for( B baseElement : newBaseContent )
            {
                T modelElement = map.get( baseElement );
                
                if( modelElement == null )
                {
                    modelElement = wrap( baseElement );
                }
                
                newContent.add( modelElement );
            }
        }
        else
        {
            for( B baseElement : newBaseContent )
            {
                T modelElement = null;
                
                for( T x : content )
                {
                    final B y = unwrap( x );
                    
                    if( baseElement.equals( y ) )
                    {
                        modelElement = x;
                        break;
                    }
                }
                
                if( modelElement == null )
                {
                    modelElement = wrap( baseElement );
                }
                
                newContent.add( modelElement );
            }
        }
        
        return newContent;
    }

    protected abstract T wrap( B obj );
    
    protected abstract B unwrap( T obj );

}
