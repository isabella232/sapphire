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

package org.eclipse.sapphire.ui.editor.views.masterdetails.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.ui.SapphireCondition;
import org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsContentNode;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class ListPropertyNodeFactory
{
    private final IModelElement modelElement;
    private final ListProperty listProperty;
    private Map<Object,MasterDetailsContentNode> nodesCache;
    private final SapphireCondition visibleWhenCondition;
    
    public ListPropertyNodeFactory( final IModelElement modelElement,
                                    final ListProperty listProperty,
                                    final SapphireCondition visibleWhenCondition )
    {
        if( modelElement == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( listProperty == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.modelElement = modelElement;
        this.listProperty = listProperty;
        this.nodesCache = null;
        this.visibleWhenCondition = visibleWhenCondition;
    }
    
    public final boolean isVisible()
    {
        if( this.visibleWhenCondition != null )
        {
            return this.visibleWhenCondition.getConditionState();
        }
        
        return true;
    }
    
    public ListProperty getListProperty()
    {
        return this.listProperty;
    }
    
    public List<MasterDetailsContentNode> createNodes()
    {
        final Map<Object,MasterDetailsContentNode> newCache = new HashMap<Object,MasterDetailsContentNode>();
        final List<MasterDetailsContentNode> nodes = new ArrayList<MasterDetailsContentNode>();
        final ModelElementList<?> list = this.modelElement.read( this.listProperty );
        
        for( IModelElement listEntryModelElement : list )
        {
            MasterDetailsContentNode node = ( this.nodesCache != null ? this.nodesCache.get( listEntryModelElement ) : null );
            
            if( node == null )
            {
                node = createNode( listEntryModelElement );
            }
            
            nodes.add( node );
            newCache.put( listEntryModelElement, node );
        }
        
        this.nodesCache = newCache;
        
        return nodes;
    }
    
    protected abstract MasterDetailsContentNode createNode( final IModelElement listElement );
}
