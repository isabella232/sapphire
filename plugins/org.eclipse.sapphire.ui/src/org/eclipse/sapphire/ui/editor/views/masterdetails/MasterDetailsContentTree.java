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

package org.eclipse.sapphire.ui.editor.views.masterdetails;

import static org.eclipse.sapphire.ui.editor.views.masterdetails.MasterDetailsPage.PREFS_CONTENT_TREE_STATE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.ui.def.IEditorPageDef;
import org.eclipse.sapphire.ui.def.IMasterDetailsTreeNodeDef;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class MasterDetailsContentTree
{
    private static final String PREFS_EXPANDED = "Expanded"; //$NON-NLS-1$
    private static final String PREFS_SELECTED = "Selected"; //$NON-NLS-1$
    
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
    
    private final MasterDetailsPage page;
    private final IEditorPageDef pageDef;
    private final IModelElement rootModelElement;
    private MasterDetailsContentNode root;
    private List<MasterDetailsContentNode> selection;
    private final Set<Listener> listeners;
    private String filterText;
    
    public MasterDetailsContentTree( final MasterDetailsPage page,
                                     final IEditorPageDef pageDef,
                                     final IModelElement rootModelElement )
    {
        this.page = page;
        this.pageDef = pageDef;
        this.rootModelElement = rootModelElement;
        this.selection = Collections.emptyList();
        this.listeners = new CopyOnWriteArraySet<Listener>();
        this.filterText = "";
    }
    
    public MasterDetailsContentNode getRoot()
    {
        if( this.root == null )
        {
            final IMasterDetailsTreeNodeDef rootNodeDef = this.pageDef.getRootNode();
            
            this.root = new MasterDetailsContentNode();
            this.root.init( this.page, this.rootModelElement, rootNodeDef, Collections.<String,String>emptyMap() );
            
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
                        MasterDetailsContentTree.this.handleNodeStructureChange();
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
        try
        {
            final List<MasterDetailsContentNode> selection = new ArrayList<MasterDetailsContentNode>();
            
            Preferences prefs = this.page.getInstancePreferences( false );
            
            if( prefs != null && prefs.nodeExists( PREFS_CONTENT_TREE_STATE ) )
            {
                prefs = prefs.node( PREFS_CONTENT_TREE_STATE );
                
                for( MasterDetailsContentNode node : this.root.getChildNodes() )
                {
                    loadTreeState( prefs, node, selection );
                }
            }

            if( ! selection.isEmpty() )
            {
                setSelectedNodes( selection );
            }
            else
            {
                MasterDetailsContentNode node = this.root;
                
                final String defaultInitialNodePath = this.pageDef.getInitialSelectionPath().getText();
                
                if( defaultInitialNodePath != null )
                {
                    for( String segment : defaultInitialNodePath.split( "/" ) ) //$NON-NLS-1$
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
        catch( BackingStoreException e )
        {
            SapphireUiFrameworkPlugin.log( e );
        }
    }
    
    private void loadTreeState( final Preferences parentPrefs,
                                final MasterDetailsContentNode node,
                                final List<MasterDetailsContentNode> selection )
    
        throws BackingStoreException
        
    {
        final String nodeLabel = node.getLabel();
        
        if( parentPrefs.nodeExists( nodeLabel ) )
        {
            final Preferences prefs = parentPrefs.node( nodeLabel );

            if( prefs.getBoolean( PREFS_EXPANDED, false ) == true )
            {
                node.setExpanded( true );
            }
            
            if( prefs.getBoolean( PREFS_SELECTED, false ) == true )
            {
                selection.add( node );
            }
            
            for( MasterDetailsContentNode child : node.getChildNodes() )
            {
                loadTreeState( prefs, child, selection );
            }
        }
    }
    
    private void saveTreeState()
    {
        try
        {
            final Preferences prefs = this.page.getInstancePreferences( true ).node( PREFS_CONTENT_TREE_STATE );
            
            for( String nodeLabel : prefs.childrenNames() )
            {
                prefs.node( nodeLabel ).removeNode();
            }
            
            final List<MasterDetailsContentNode> selection = getSelectedNodes();
            
            for( MasterDetailsContentNode node : this.root.getChildNodes() )
            {
                saveTreeState( prefs, node, selection );
            }
            
            prefs.flush();
        }
        catch( BackingStoreException e )
        {
            SapphireUiFrameworkPlugin.log( e );
        }
    }

    private void saveTreeState( final Preferences parentPrefs,
                                final MasterDetailsContentNode node,
                                final List<MasterDetailsContentNode> selection )
    
        throws BackingStoreException
        
    {
        final boolean isExpanded = node.isExpanded();
        final boolean isSelected = selection.contains( node );
        
        if( isExpanded || isSelected )
        {
            final Preferences prefs = parentPrefs.node( node.getLabel() );
            
            if( isExpanded )
            {
                prefs.putBoolean( PREFS_EXPANDED, true );
            }
            
            if( isSelected )
            {
                prefs.putBoolean( PREFS_SELECTED, true );
            }
            
            for( MasterDetailsContentNode child : node.getChildNodes() )
            {
                saveTreeState( prefs, child, selection );
            }
        }
    }
    
}
