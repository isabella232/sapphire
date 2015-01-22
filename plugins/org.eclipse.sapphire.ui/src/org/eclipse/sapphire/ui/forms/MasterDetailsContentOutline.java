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

package org.eclipse.sapphire.ui.forms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.sapphire.Disposable;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.ListenerContext;
import org.eclipse.sapphire.util.ListFactory;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class MasterDetailsContentOutline implements Disposable
{
    private final MasterDetailsEditorPagePart editorPagePart;
    private final MasterDetailsEditorPageDef editorPageDef;
    private final Element rootModelElement;
    private MasterDetailsContentNodePart root;
    private List<MasterDetailsContentNodePart> selection;
    private final ListenerContext listeners;
    private String filterText;
    
    public MasterDetailsContentOutline( final MasterDetailsEditorPagePart editorPagePart )
    {
        this.editorPagePart = editorPagePart;
        this.editorPageDef = editorPagePart.definition();
        this.rootModelElement = editorPagePart.getModelElement();
        this.selection = Collections.emptyList();
        this.listeners = new ListenerContext();
        this.filterText = "";
    }
    
    public MasterDetailsContentNodePart getRoot()
    {
        if( this.root == null )
        {
            final MasterDetailsContentNodeDef rootNodeDef = this.editorPageDef.getRootNode();
            
            this.root = new MasterDetailsContentNodePart();
            this.root.init( this.editorPagePart, this.rootModelElement, rootNodeDef, Collections.<String,String>emptyMap() );
            this.root.initialize();
            
            loadTreeState();
            
            attach
            (
                new Listener()
                {
                    @Override
                    public void handle( final Event event )
                    {
                        if( event instanceof NodeExpandedStateChangedEvent || event instanceof SelectionChangedEvent )
                        {
                            saveTreeState();
                        }
                    }
                }
            );
        }
        
        return this.root;
    }
    
    public MasterDetailsContentNodePart getSelectedNode()
    {
        if( this.selection.isEmpty() )
        {
            return null;
        }
        else
        {
            return this.selection.get( 0 );
        }
    }
    
    public List<MasterDetailsContentNodePart> getSelectedNodes()
    {
        return this.selection;
    }
    
    public void setSelectedNode( final MasterDetailsContentNodePart selection )
    {
        if( selection == null )
        {
            setSelectedNodes( Collections.<MasterDetailsContentNodePart>emptyList() );
        }
        else
        {
            setSelectedNodes( Collections.singletonList( selection ) );
        }
    }
    
    public void setSelectedNodes( final List<MasterDetailsContentNodePart> selection )
    {
        if( ! this.selection.equals( selection ) )
        {
            for( MasterDetailsContentNodePart node : selection )
            {
                final MasterDetailsContentNodePart parent = node.getParentNode();
                
                if( parent != null )
                {
                    parent.setExpanded( true );
                }
            }
            
            if( selection.isEmpty() )
            {
                this.selection = Collections.emptyList();
            }
            else
            {
                this.selection = new ArrayList<MasterDetailsContentNodePart>( selection );
            }
            
            this.listeners.broadcast( new SelectionChangedEvent( this.selection ) );
        }
    }

    public void setSelection( final String path )
    {
        MasterDetailsContentNodePart node = this.root;
        
        for( String segment : path.split( "/" ) )
        {
            boolean segmentMatched = false;
            
            for( MasterDetailsContentNodePart n : node.nodes().visible() )
            {
                if( n.getLabel().equals( segment ) )
                {
                    node = n;
                    segmentMatched = true;
                    break;
                }
            }
            
            if( ! segmentMatched )
            {
                break;
            }
        }
        
        if( node != this.root )
        {
            setSelectedNode( node );
        }
    }
    
    void refreshSelection()
    {
        final List<MasterDetailsContentNodePart> newSelection = new ArrayList<MasterDetailsContentNodePart>();
        
        for( MasterDetailsContentNodePart node : this.selection )
        {
            final LinkedList<MasterDetailsContentNodePart> path = new LinkedList<MasterDetailsContentNodePart>();
            
            while( node != this.root )
            {
                path.addFirst( node );
                node = node.getParentNode();
            }
            
            node = this.root;
            
            for( MasterDetailsContentNodePart n : path )
            {
                if( node.nodes().visible().contains( n ) )
                {
                    node = n;
                }
                else
                {
                    break;
                }
            }
            
            if( node == this.root )
            {
                final List<MasterDetailsContentNodePart> topLevelNodes = this.root.nodes().visible();
                
                if( topLevelNodes.size() > 0 )
                {
                    node = topLevelNodes.get( 0 );
                }
                else
                {
                    node = null;
                }
            }
            
            if( ! newSelection.contains( node ) )
            {
                newSelection.add( node );
            }
        }
        
        setSelectedNodes( newSelection );
    }

    public List<MasterDetailsContentNodePart> getExpandedNodes()
    {
        final List<MasterDetailsContentNodePart> result = new ArrayList<MasterDetailsContentNodePart>();
        
        for( MasterDetailsContentNodePart node : this.root.nodes().visible() )
        {
            node.getExpandedNodes( result );
        }
        
        return result;
    }
    
    public void setExpandedNodes( final Set<MasterDetailsContentNodePart> expandedNodes )
    {
        for( MasterDetailsContentNodePart node : this.root.nodes().visible() )
        {
            setExpandedNodes( node, expandedNodes );
        }
    }
    
    private static void setExpandedNodes( final MasterDetailsContentNodePart node,
                                          final Set<MasterDetailsContentNodePart> expandedNodes )
    {
        for( MasterDetailsContentNodePart child : node.nodes().visible() )
        {
            setExpandedNodes( child, expandedNodes );
        }
        
        final boolean shouldBeExpanded = expandedNodes.contains( node );
        
        if( node.isExpanded() != shouldBeExpanded )
        {
            node.setExpanded( shouldBeExpanded );
        }
    }
    
    public String getFilterText()
    {
        return this.filterText;
    }
    
    public void setFilterText( final String filterText )
    {
        if( ! this.filterText.equals( filterText ) )
        {
            this.filterText = filterText;
            this.listeners.broadcast( new FilterChangedEvent( filterText ) );
        }
    }
    
    public final ListenerContext listeners()
    {
        return this.listeners;
    }
    
    public final boolean attach( final Listener listener )
    {
        return this.listeners.attach( listener );
    }
    
    public final boolean detach( final Listener listener )
    {
        return this.listeners.detach( listener );
    }
    
    public void notifyOfNodeExpandedStateChange( final MasterDetailsContentNodePart node )
    {
        this.listeners.broadcast( new NodeExpandedStateChangedEvent( node ) );
    }
    
    @Override
    public void dispose()
    {
        if( this.root != null )
        {
            this.root.dispose();
        }
    }
    
    private void loadTreeState()
    {
        final List<MasterDetailsContentNodePart> selection = new ArrayList<MasterDetailsContentNodePart>();
        
        final MasterDetailsEditorPageState editorPageState = this.editorPagePart.state();
        
        if( editorPageState != null )
        {
            final MasterDetailsNodeState rootNodeState = editorPageState.getContentOutlineState().getRoot();
            
            for( MasterDetailsContentNodePart node : this.root.nodes().visible() )
            {
                loadTreeState( rootNodeState, node, selection );
            }
        }

        if( ! selection.isEmpty() )
        {
            setSelectedNodes( selection );
        }
        else
        {
            MasterDetailsContentNodePart node = this.root;
            
            final String defaultInitialNodePath = this.editorPageDef.getInitialSelectionPath().text();
            
            if( defaultInitialNodePath != null )
            {
                for( String segment : defaultInitialNodePath.split( "/" ) )
                {
                    node = node.findNode( segment );
                    
                    if( node != null && node.visible() )
                    {
                        node.setExpanded( true );
                    }
                    else
                    {
                        break;
                    }
                }
            }
            
            if( node != null )
            {
                setSelectedNode( node );
            }
        }
    }
    
    private void loadTreeState( final MasterDetailsNodeState parentNodeState,
                                final MasterDetailsContentNodePart node,
                                final List<MasterDetailsContentNodePart> selection )
    {
        final String nodeLabel = node.getLabel();
        
        for( MasterDetailsNodeState childNodeState : parentNodeState.getChildren() )
        {
            if( nodeLabel.equals( childNodeState.getLabel().text() ) )
            {
                node.setExpanded( childNodeState.getExpanded().content() );
                
                if( childNodeState.getSelected().content() )
                {
                    selection.add( node );
                }

                for( MasterDetailsContentNodePart child : node.nodes().visible() )
                {
                    loadTreeState( childNodeState, child, selection );
                }
                
                break;
            }
        }
    }
    
    private void saveTreeState()
    {
        final MasterDetailsEditorPageState editorPageState = this.editorPagePart.state();
        
        if( editorPageState != null )
        {
            final MasterDetailsNodeState rootNodeState = editorPageState.getContentOutlineState().getRoot();
            
            rootNodeState.getChildren().clear();
            
            final List<MasterDetailsContentNodePart> selection = getSelectedNodes();
            
            for( MasterDetailsContentNodePart node : this.root.nodes().visible() )
            {
                saveTreeState( rootNodeState, node, selection );
            }
        }
    }

    private void saveTreeState( final MasterDetailsNodeState parentNodeState,
                                final MasterDetailsContentNodePart node,
                                final List<MasterDetailsContentNodePart> selection )
    {
        final boolean isExpanded = node.isExpanded();
        final boolean isSelected = selection.contains( node );
        
        if( isExpanded || isSelected )
        {
            final MasterDetailsNodeState childNodeState = parentNodeState.getChildren().insert();
            
            childNodeState.setLabel( node.getLabel() );
            
            if( isExpanded )
            {
                childNodeState.setExpanded( isExpanded );
            }
            
            if( isSelected )
            {
                childNodeState.setSelected( isSelected );
            }
            
            for( MasterDetailsContentNodePart child : node.nodes().visible() )
            {
                saveTreeState( childNodeState, child, selection );
            }
        }
    }
    
    public static final class NodeExpandedStateChangedEvent extends Event
    {
        private final MasterDetailsContentNodePart node;
        
        public NodeExpandedStateChangedEvent( final MasterDetailsContentNodePart node )
        {
            this.node = node;
        }
        
        public MasterDetailsContentNodePart node()
        {
            return this.node;
        }
    }
    
    public static final class SelectionChangedEvent extends Event
    {
        private final List<MasterDetailsContentNodePart> selection;
        
        public SelectionChangedEvent( final List<MasterDetailsContentNodePart> selection )
        {
            this.selection = ListFactory.unmodifiable( selection );
        }
        
        public List<MasterDetailsContentNodePart> selection()
        {
            return this.selection;
        }

        @Override
        public boolean supersedes( final Event event )
        {
            return ( event instanceof SelectionChangedEvent );
        }
    }
    
    public static final class FilterChangedEvent extends Event
    {
        private final String filter;
        
        public FilterChangedEvent( final String filter )
        {
            this.filter = filter;
        }
        
        public String filter()
        {
            return this.filter;
        }
    }
    
}
