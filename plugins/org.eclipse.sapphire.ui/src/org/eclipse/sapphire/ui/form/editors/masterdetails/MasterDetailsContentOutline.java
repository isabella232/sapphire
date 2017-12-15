/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.form.editors.masterdetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.IMasterDetailsContentNodeDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.IMasterDetailsEditorPageDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.state.IContentOutlineNodeState;
import org.eclipse.sapphire.ui.form.editors.masterdetails.state.IMasterDetailsEditorPageState;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class MasterDetailsContentOutline
{
    public static abstract class Listener
    {
        public void handleNodeUpdate( final MasterDetailsContentNode node )
        {
        }
        
        public void handleNodeStructureChange( final MasterDetailsContentNode node )
        {
        }
        
        public void handleNodeExpandedStateChange( final MasterDetailsContentNode node )
        {
        }
        
        public void handleSelectionChange( final List<MasterDetailsContentNode> newSelection )
        {
        }
        
        public void handleFilterChange( final String newFilterText )
        {
        }
    }
    
    private final MasterDetailsEditorPagePart editorPagePart;
    private final IMasterDetailsEditorPageDef editorPageDef;
    private final IModelElement rootModelElement;
    private MasterDetailsContentNode root;
    private List<MasterDetailsContentNode> selection;
    private final Set<Listener> listeners;
    private String filterText;
    
    public MasterDetailsContentOutline( final MasterDetailsEditorPagePart editorPagePart )
    {
        this.editorPagePart = editorPagePart;
        this.editorPageDef = editorPagePart.getDefinition();
        this.rootModelElement = editorPagePart.getModelElement();
        this.selection = Collections.emptyList();
        this.listeners = new CopyOnWriteArraySet<Listener>();
        this.filterText = "";
    }
    
    public MasterDetailsContentNode getRoot()
    {
        if( this.root == null )
        {
            final IMasterDetailsContentNodeDef rootNodeDef = this.editorPageDef.getRootNode();
            
            this.root = new MasterDetailsContentNode();
            this.root.init( this.editorPagePart, this.rootModelElement, rootNodeDef, Collections.<String,String>emptyMap() );
            
            loadTreeState();
            
            addListener
            (
                new Listener()
                {
                    @Override
                    public void handleNodeExpandedStateChange( final MasterDetailsContentNode node )
                    {
                        saveTreeState();
                    }

                    @Override
                    public void handleSelectionChange( final List<MasterDetailsContentNode> newSelection )
                    {
                        saveTreeState();
                    }

                    @Override
                    public void handleNodeStructureChange( final MasterDetailsContentNode node )
                    {
                        MasterDetailsContentOutline.this.handleNodeStructureChange();
                    }
                }
            );
        }
        
        return this.root;
    }
    
    public MasterDetailsContentNode getSelectedNode()
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
    
    public List<MasterDetailsContentNode> getSelectedNodes()
    {
        return this.selection;
    }
    
    public void setSelectedNode( final MasterDetailsContentNode selection )
    {
        if( selection == null )
        {
            setSelectedNodes( Collections.<MasterDetailsContentNode>emptyList() );
        }
        else
        {
            setSelectedNodes( Collections.singletonList( selection ) );
        }
    }
    
    public void setSelectedNodes( final List<MasterDetailsContentNode> selection )
    {
        if( ! this.selection.equals( selection ) )
        {
            for( MasterDetailsContentNode node : selection )
            {
                final MasterDetailsContentNode parent = node.getParentNode();
                
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
                this.selection = new ArrayList<MasterDetailsContentNode>( selection );
            }
            
            notifyOfSelectionChange( this.selection );
        }
    }

    public void setSelection( final String path )
    {
        MasterDetailsContentNode node = this.root;
        
        for( String segment : path.split( "/" ) )
        {
            boolean segmentMatched = false;
            
            for( MasterDetailsContentNode n : node.getChildNodes() )
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
    
    public List<MasterDetailsContentNode> getExpandedNodes()
    {
        final List<MasterDetailsContentNode> result = new ArrayList<MasterDetailsContentNode>();
        
        for( MasterDetailsContentNode node : this.root.getChildNodes() )
        {
            node.getExpandedNodes( result );
        }
        
        return result;
    }
    
    public void setExpandedNodes( final Set<MasterDetailsContentNode> expandedNodes )
    {
        for( MasterDetailsContentNode node : this.root.getChildNodes() )
        {
            setExpandedNodes( node, expandedNodes );
        }
    }
    
    private static void setExpandedNodes( final MasterDetailsContentNode node,
                                          final Set<MasterDetailsContentNode> expandedNodes )
    {
        for( MasterDetailsContentNode child : node.getChildNodes() )
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
            notifyOfFilterChange( filterText );
        }
    }
    
    public void addListener( final Listener listener )
    {
        this.listeners.add( listener );
    }
    
    public void removeListener( final Listener listener )
    {
        this.listeners.remove( listener );
    }
    
    public void notifyOfNodeUpdate( final MasterDetailsContentNode node )
    {
        for( Listener listener : this.listeners )
        {
            listener.handleNodeUpdate( node );
        }
        
        final MasterDetailsContentNode parent = node.getParentNode();
        
        if( parent != null )
        {
            notifyOfNodeUpdate( parent );
        }
    }
    
    public void notifyOfNodeStructureChange( final MasterDetailsContentNode node )
    {
        for( Listener listener : this.listeners )
        {
            listener.handleNodeStructureChange( node );
        }
    }
    
    public void notifyOfNodeExpandedStateChange( final MasterDetailsContentNode node )
    {
        for( Listener listener : this.listeners )
        {
            listener.handleNodeExpandedStateChange( node );
        }
    }
    
    public void notifyOfSelectionChange( final List<MasterDetailsContentNode> newSelection )
    {
        for( Listener listener : this.listeners )
        {
            listener.handleSelectionChange( newSelection );
        }
    }
    
    private void notifyOfFilterChange( final String newFilterText )
    {
        for( Listener listener : this.listeners )
        {
            listener.handleFilterChange( newFilterText );
        }
    }
    
    public void refresh()
    {
        notifyOfNodeStructureChange( null );
    }
    
    public void dispose()
    {
        if( this.root != null )
        {
            this.root.dispose();
        }
    }
    
    private void handleNodeStructureChange()
    {
        
        final List<MasterDetailsContentNode> newSelection = new ArrayList<MasterDetailsContentNode>();
        
        for( MasterDetailsContentNode node : this.selection )
        {
            final LinkedList<MasterDetailsContentNode> path = new LinkedList<MasterDetailsContentNode>();
            
            while( node != this.root )
            {
                path.addFirst( node );
                node = node.getParentNode();
            }
            
            node = this.root;
            
            for( MasterDetailsContentNode n : path )
            {
                if( node.getChildNodes().contains( n ) )
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
                final List<MasterDetailsContentNode> topLevelNodes = this.root.getChildNodes();
                
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
    
    private void loadTreeState()
    {
        final List<MasterDetailsContentNode> selection = new ArrayList<MasterDetailsContentNode>();
        
        final IMasterDetailsEditorPageState editorPageState = this.editorPagePart.getState();
        
        if( editorPageState != null )
        {
            final IContentOutlineNodeState rootNodeState = editorPageState.getContentOutlineState().getRoot();
            
            for( MasterDetailsContentNode node : this.root.getChildNodes() )
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
            MasterDetailsContentNode node = this.root;
            
            final String defaultInitialNodePath = this.editorPageDef.getInitialSelectionPath().getText();
            
            if( defaultInitialNodePath != null )
            {
                for( String segment : defaultInitialNodePath.split( "/" ) )
                {
                    node = node.getChildNodeByLabel( segment );
                    
                    if( node != null )
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
    
    private void loadTreeState( final IContentOutlineNodeState parentNodeState,
                                final MasterDetailsContentNode node,
                                final List<MasterDetailsContentNode> selection )
    {
        final String nodeLabel = node.getLabel();
        
        for( IContentOutlineNodeState childNodeState : parentNodeState.getChildren() )
        {
            if( nodeLabel.equals( childNodeState.getLabel().getText() ) )
            {
                node.setExpanded( childNodeState.getExpanded().getContent() );
                
                if( childNodeState.getSelected().getContent() )
                {
                    selection.add( node );
                }

                for( MasterDetailsContentNode child : node.getChildNodes() )
                {
                    loadTreeState( childNodeState, child, selection );
                }
                
                break;
            }
        }
    }
    
    private void saveTreeState()
    {
        final IMasterDetailsEditorPageState editorPageState = this.editorPagePart.getState();
        
        if( editorPageState != null )
        {
            final IContentOutlineNodeState rootNodeState = editorPageState.getContentOutlineState().getRoot();
            
            rootNodeState.getChildren().clear();
            
            final List<MasterDetailsContentNode> selection = getSelectedNodes();
            
            for( MasterDetailsContentNode node : this.root.getChildNodes() )
            {
                saveTreeState( rootNodeState, node, selection );
            }

            try
            {
                rootNodeState.resource().save();
            }
            catch( Exception e )
            {
                SapphireUiFrameworkPlugin.log( e );
            }
        }
    }

    private void saveTreeState( final IContentOutlineNodeState parentNodeState,
                                final MasterDetailsContentNode node,
                                final List<MasterDetailsContentNode> selection )
    {
        final boolean isExpanded = node.isExpanded();
        final boolean isSelected = selection.contains( node );
        
        if( isExpanded || isSelected )
        {
            final IContentOutlineNodeState childNodeState = parentNodeState.getChildren().addNewElement();
            
            childNodeState.setLabel( node.getLabel() );
            
            if( isExpanded )
            {
                childNodeState.setExpanded( isExpanded );
            }
            
            if( isSelected )
            {
                childNodeState.setSelected( isSelected );
            }
            
            for( MasterDetailsContentNode child : node.getChildNodes() )
            {
                saveTreeState( childNodeState, child, selection );
            }
        }
    }
    
}
