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

import static org.eclipse.sapphire.ui.internal.TableWrapLayoutUtil.twlayout;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdfill;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdhfill;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdhhint;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdwhint;
import static org.eclipse.sapphire.ui.util.SwtUtil.glayout;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.sapphire.modeling.IModel;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.ui.SapphireCommands;
import org.eclipse.sapphire.ui.SapphireEditor;
import org.eclipse.sapphire.ui.SapphireEditorFormPage;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.SapphireSection;
import org.eclipse.sapphire.ui.actions.Action;
import org.eclipse.sapphire.ui.actions.ActionGroup;
import org.eclipse.sapphire.ui.actions.ActionsCommandBridge;
import org.eclipse.sapphire.ui.actions.ActionsRenderer;
import org.eclipse.sapphire.ui.actions.ShowHelpAction;
import org.eclipse.sapphire.ui.def.IEditorPageDef;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;
import org.eclipse.sapphire.ui.def.SapphireUiDefFactory;
import org.eclipse.sapphire.ui.editor.views.masterdetails.actions.CollapseAllAction;
import org.eclipse.sapphire.ui.editor.views.masterdetails.actions.ExpandAllAction;
import org.eclipse.sapphire.ui.editor.views.masterdetails.actions.HideOutlineAction;
import org.eclipse.sapphire.ui.editor.views.masterdetails.actions.MergedNodeAction;
import org.eclipse.sapphire.ui.editor.views.masterdetails.actions.NodeAction;
import org.eclipse.sapphire.ui.internal.ActionsHostUtil;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.sapphire.ui.util.internal.MutableReference;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class MasterDetailsPage 

    extends SapphireEditorFormPage
    
{
	public static final String ACTION_CONTEXT_HEADER = "header"; //$NON-NLS-1$
	public static final String ACTION_CONTEXT_OUTLINE_TOOLBAR = "outline-toolbar"; //$NON-NLS-1$
	public static final String ACTION_CONTEXT_OUTLINE_MENU = "outline-menu"; //$NON-NLS-1$
	
    static final String PREFS_CONTENT_TREE_STATE = "ContentTreeState"; //$NON-NLS-1$
    private static final String PREFS_VISIBLE = "Visible"; //$NON-NLS-1$
	
	private IEditorPageDef definition;
	private final MasterDetailsContentTree contentTree;
    private RootSection mainSection;
    private Map<String,List<ActionGroup>> actionsByContext = null;
    private ContentOutline contentOutlinePage;
    
    public MasterDetailsPage( final SapphireEditor editor,
                              final IModelElement rootModelElement,
                              final IPath pageDefinitionLocation ) 
    {
        this( editor, rootModelElement, pageDefinitionLocation, null );
    }

    public MasterDetailsPage( final SapphireEditor editor,
                              final IModelElement rootModelElement,
                              final IPath pageDefinitionLocation,
                              final String pageName ) 
    {
        super( editor, rootModelElement );

        final String bundleId = pageDefinitionLocation.segment( 0 );
        final String pageId = pageDefinitionLocation.lastSegment();
        final String relPath = pageDefinitionLocation.removeFirstSegments( 1 ).removeLastSegments( 1 ).toPortableString();
        
        final ISapphireUiDef def = SapphireUiDefFactory.load( bundleId, relPath );
        
        for( IEditorPageDef pg : def.getEditorPageDefs() )
        {
            if( pageId.equals( pg.getId().getText() ) )
            {
                this.definition = pg;
                break;
            }
        }
        
        setPartName( pageName == null ? this.definition.getPageName().getText() : pageName );
        
        // Content Outline
        
        this.contentTree = new MasterDetailsContentTree( this, this.definition, rootModelElement );
    }

    @Override
    public String getId()
    {
        return getPartName();
    }
    
    @Override
    public String getHelpContextId()
    {
        return this.definition.getHelpContextId().getText();
    }
    
    public MasterDetailsContentTree getContentTree()
    {
        return this.contentTree;
    }
    
    public void expandAllNodes()
    {
        for( MasterDetailsContentNode node : getContentTree().getRoot().getChildNodes() )
        {
            node.setExpanded( true, true );
        }
    }

    public void collapseAllNodes()
    {
        for( MasterDetailsContentNode node : getContentTree().getRoot().getChildNodes() )
        {
            node.setExpanded( false, true );
        }
    }

    public IDetailsPage getCurrentDetailsPage()
    {
        return this.mainSection.getCurrentDetailsSection();
    }
    
    protected void createFormContent( final IManagedForm managedForm ) 
    {
        ScrolledForm form = managedForm.getForm();
        FormToolkit toolkit = managedForm.getToolkit();
        toolkit.decorateFormHeading(managedForm.getForm().getForm());
        
        form.setText( this.definition.getPageHeaderText().getLocalizedText() );
        
        this.mainSection = new RootSection( getModel() );
        this.mainSection.createContent( managedForm );
        
        final String helpContextId = this.definition.getHelpContextId().getText();
        
        if( helpContextId != null )
        {
            PlatformUI.getWorkbench().getHelpSystem().setHelp( managedForm.getForm().getBody(), helpContextId );
        }

        final IToolBarManager toolbarManager = form.getToolBarManager();
        ActionsRenderer.fillToolBarManager( toolbarManager, form.getShell(), getActions( ACTION_CONTEXT_HEADER ) );
        ( (ToolBarManager) toolbarManager ).getControl().setCursor( null );
    }
    
    public IContentOutlinePage getContentOutlinePage()
    {
        if( this.contentOutlinePage == null )
        {
            this.contentOutlinePage = new ContentOutline();
        }
        
        return this.contentOutlinePage;
    }
    
    public boolean isDetailsMaximized()
    {
        try
        {
            Preferences prefs = getInstancePreferences( false );
            
            if( prefs != null && prefs.nodeExists( PREFS_CONTENT_TREE_STATE ) )
            {
                prefs = prefs.node( PREFS_CONTENT_TREE_STATE );
                final boolean contentTreeVisible = prefs.getBoolean( PREFS_VISIBLE, true );
                
                if( ! contentTreeVisible )
                {
                	return true;
                }
            }
        }
        catch( BackingStoreException e )
        {
            SapphireUiFrameworkPlugin.log( e );
        }
        
        return false;
    }
    
    public void setDetailsMaximized( final boolean maximized )
    {
        this.mainSection.setDetailsMaximized( maximized );
        
        try
        {
            final Preferences prefs = getInstancePreferences( true ).node( PREFS_CONTENT_TREE_STATE );
            prefs.putBoolean( PREFS_VISIBLE, ! maximized );
            prefs.flush();
        }
        catch( BackingStoreException e )
        {
            SapphireUiFrameworkPlugin.log( e );
        }
    }
    
    public List<ActionGroup> getActions( final String context )
    {
        initActions();
        
        List<ActionGroup> actions = this.actionsByContext.get( context );
        
        if( actions != null )
        {
            return actions;
        }

        return Collections.emptyList();
    }
    
    @Override
    public Action getAction( final String id )
    {
        initActions();
        
        for( List<ActionGroup> actions : this.actionsByContext.values() )
        {
            for( ActionGroup group : actions )
            {
                final Action action = group.getAction( id );
                
                if( action != null )
                {
                    return action;
                }
            }
        }
        
        return null;
    }
    
    private List<ActionGroup> getNodeActions( final List<MasterDetailsContentNode> nodes,
                                              final String context )
    {
        final List<ActionGroup> result = new ArrayList<ActionGroup>();
        
        if( ! nodes.isEmpty() && context != null && context.equals( ACTION_CONTEXT_OUTLINE_MENU ) )
        {
            if( nodes.size() == 1 )
            {
                result.addAll( nodes.get( 0 ).getMenuActions() );
            }
            else
            {
                final List<Map<String,NodeAction>> index = new ArrayList<Map<String,NodeAction>>();
                
                for( MasterDetailsContentNode node : nodes )
                {
                    final Map<String,NodeAction> nodeIndex = new HashMap<String,NodeAction>();
                    index.add( nodeIndex );
                    
                    for( ActionGroup group : node.getMenuActions() )
                    {
                        for( Action action : group.getActions() )
                        {
                            nodeIndex.put( action.getId(), (NodeAction) action ); 
                        }
                    }
                }
                
                for( ActionGroup group : nodes.get( 0 ).getMenuActions() )
                {
                    final List<Action> actions = new ArrayList<Action>();
                    
                    for( Action action : group.getActions() )
                    {
                        final String id = action.getId();
                        
                        if( ( (NodeAction) action ).isMergingAllowed() == true )
                        {
                            List<NodeAction> actionsToMerge = new ArrayList<NodeAction>();
                            
                            for( Map<String,NodeAction> subIndex : index )
                            {
                                final Action a = subIndex.get( id );
                                
                                if( a != null )
                                {
                                    actionsToMerge.add( (NodeAction) a );
                                }
                                else
                                {
                                    actionsToMerge = null;
                                    break;
                                }
                            }
                            
                            if( actionsToMerge != null )
                            {
                                final MergedNodeAction mergedAction = new MergedNodeAction();
                                
                                mergedAction.setId( id );
                                mergedAction.setCommandId( action.getCommandId() );
                                mergedAction.setType( action.getType() );
                                mergedAction.setLabel( action.getLabel() );
                                mergedAction.setPart( this );
    
                                mergedAction.setImageDescriptor( action.getImageDescriptor() );
                                
                                if( mergedAction.getImageDescriptor() == null )
                                {
                                    mergedAction.setImageDescriptor( action.getImageDescriptor() );
                                }
                                
                                for( NodeAction a : actionsToMerge )
                                {
                                    mergedAction.addAction( a );
                                }
                                
                                actions.add( mergedAction );
                            }
                        }
                    }
                    
                    if( ! actions.isEmpty() )
                    {
                        result.add( new ActionGroup( actions ) );
                    }
                }
            }
        }
        
        result.addAll( getActions( context ) );
        
        return result;
    }

    private void initActions()
    {
        if( this.actionsByContext == null )
        {
            this.actionsByContext = new HashMap<String,List<ActionGroup>>();
            
            final List<ActionGroup> headerActionSet = new ArrayList<ActionGroup>();
            
            final ActionGroup defaultHeaderActions = new ActionGroup();
            defaultHeaderActions.addAction( new HideOutlineAction() );
            //defaultHeaderActions.addAction( new LinkWithSourceViewAction() );
            defaultHeaderActions.addAction( new ShowHelpAction() );
            headerActionSet.add( defaultHeaderActions );
            
            ActionsHostUtil.initActions( headerActionSet, this.definition.getHeaderActionSetDef() );
            this.actionsByContext.put( ACTION_CONTEXT_HEADER, headerActionSet );
            
            final List<ActionGroup> outlineToolbarActionSet = new ArrayList<ActionGroup>();
            
            final ActionGroup expandAllCollapseAllActions = new ActionGroup();
            expandAllCollapseAllActions.addAction( new ExpandAllAction() );
            expandAllCollapseAllActions.addAction( new CollapseAllAction() );
            outlineToolbarActionSet.add( expandAllCollapseAllActions );
            
            ActionsHostUtil.initActions( outlineToolbarActionSet, this.definition.getOutlineToolbarActionSetDef() );
            this.actionsByContext.put( ACTION_CONTEXT_OUTLINE_TOOLBAR, outlineToolbarActionSet );
            
            final List<ActionGroup> outlineMenuActionSet = new ArrayList<ActionGroup>();
            
            ActionsHostUtil.initActions( outlineMenuActionSet, this.definition.getOutlineMenuActionSetDef() );
            this.actionsByContext.put( ACTION_CONTEXT_OUTLINE_MENU, outlineMenuActionSet );
            
            for( List<ActionGroup> actions : this.actionsByContext.values() )
            {
                for( ActionGroup group : actions )
                {
                    for( Action action : group.getActions() )
                    {
                        action.setPart( this );
                    }
                }
            }
        }
    }

    @Override
    public void setFocus()
    {
        if( isDetailsMaximized() )
        {
            setFocusOnDetails();
        }
        else
        {
            setFocusOnContentOutline();
        }
    }
    
    public void setFocusOnContentOutline()
    {
        if( isDetailsMaximized() )
        {
            setDetailsMaximized( false );
        }
        
        this.mainSection.masterSection.tree.setFocus();
    }
    
    public void setFocusOnDetails()
    {
        final Control control = findFirstFocusableControl( this.mainSection.detailsSectionControl );
        
        if( control != null )
        {
            control.setFocus();
        }
    }
    
    private Control findFirstFocusableControl( final Control control )
    {
        if( control instanceof Text || control instanceof Combo || control instanceof Link ||
            control instanceof List<?> || control instanceof Table || control instanceof Tree )
        {
            return control;
        }
        else if( control instanceof Text )
        {
            if( ( ( (Text) control ).getStyle() & SWT.READ_ONLY ) == 0 )
            {
                return control;
            }
        }
        else if( control instanceof Button )
        {
            int style = control.getStyle();
            
            if( ( style & SWT.CHECK ) != 0 || (style & SWT.RADIO ) != 0 )
            {
                return control;
            }
        }
        else if( control instanceof Composite )
        {
            for( Control child : ( (Composite) control ).getChildren() )
            {
                final Control res = findFirstFocusableControl( child );
                
                if( res != null )
                {
                    return res;
                }
            }
        }
        
        return null;
    }

    private FilteredTree createContentOutline( final Composite parent,
                                               final MasterDetailsContentTree contentTree,
                                               final boolean addBorders )
    {
        final int treeStyle = ( addBorders ? SWT.BORDER : SWT.NONE ) | SWT.MULTI;
        
        final ContentOutlineFilteredTree filteredTree = new ContentOutlineFilteredTree( parent, treeStyle, contentTree );
        final TreeViewer treeViewer = filteredTree.getViewer();
        
        SapphireCommands.configureContentOutlineContext( treeViewer.getTree() );
        
        final ITreeContentProvider contentProvider = new ITreeContentProvider()
        {
            public Object[] getElements( final Object inputElement )
            {
                return contentTree.getRoot().getChildNodes().toArray();
            }
        
            public Object[] getChildren( final Object parentElement )
            {
                return ( (MasterDetailsContentNode) parentElement ).getChildNodes().toArray();
            }
        
            public Object getParent( final Object element )
            {
                return ( (MasterDetailsContentNode) element ).getParentNode();
            }
        
            public boolean hasChildren( final Object element )
            {
                return ( ( (MasterDetailsContentNode) element ).getChildNodes() ).size() > 0;
            }
        
            public void inputChanged( final Viewer viewer,
                                      final Object oldInput,
                                      final Object newInput )
            {
            }

            public void dispose()
            {
            }
        };
        
        final LabelProvider labelProvider = new LabelProvider()
        {
            private final Map<ImageDescriptor,Image> images = new HashMap<ImageDescriptor,Image>();
            
            @Override
            public String getText( final Object element ) 
            {
                return ( (MasterDetailsContentNode) element ).getLabel();
            }
        
            @Override
            public Image getImage( final Object element ) 
            {
                return getImage( (MasterDetailsContentNode) element );
            }
            
            private Image getImage( final MasterDetailsContentNode node )
            {
                final ImageDescriptor imageDescriptor = node.getImageDescriptor();
                Image image = this.images.get( imageDescriptor );
                
                if( image == null )
                {
                    image = imageDescriptor.createImage();
                    this.images.put( imageDescriptor, image );
                }
                
                return image;
            }
            
            @Override
            public void dispose()
            {
                for( Image image : this.images.values() )
                {
                    image.dispose();
                }
            }
        };
        
        treeViewer.setContentProvider( contentProvider );
        treeViewer.setLabelProvider( labelProvider );
        treeViewer.setInput( new Object() );
        
        final MutableReference<Boolean> ignoreSelectionChange = new MutableReference<Boolean>( false );
        final MutableReference<Boolean> ignoreExpandedStateChange = new MutableReference<Boolean>( false );
        
        final MasterDetailsContentTree.Listener contentTreeListener = new MasterDetailsContentTree.Listener()
        {
            @Override
            public void handleNodeUpdate( final MasterDetailsContentNode node )
            {
                treeViewer.update( node, null );
            }

            @Override
            public void handleNodeStructureChange( final MasterDetailsContentNode node )
            {
                treeViewer.refresh( node );
            }

            @Override
            public void handleSelectionChange( final List<MasterDetailsContentNode> selection )
            {
                if( ignoreSelectionChange.get() == true )
                {
                    return;
                }
                
                final IStructuredSelection sel;
                
                if( selection.isEmpty() )
                {
                    sel = StructuredSelection.EMPTY;
                }
                else
                {
                    sel = new StructuredSelection( selection );
                }
                
                if( ! treeViewer.getSelection().equals( sel ) )
                {
                    for( MasterDetailsContentNode node : selection )
                    {
                        treeViewer.reveal( node );
                    }
                    
                    treeViewer.setSelection( sel );
                }
            }
            
            @Override
            public void handleNodeExpandedStateChange( final MasterDetailsContentNode node )
            {
                if( ignoreExpandedStateChange.get() == true )
                {
                    return;
                }
                
                final boolean expandedState = node.isExpanded();
                
                if( treeViewer.getExpandedState( node ) != expandedState )
                {
                    treeViewer.setExpandedState( node, expandedState );
                }
            }

            @Override
            public void handleFilterChange( final String newFilterText )
            {
                filteredTree.changeFilterText( newFilterText );
            }
        };

        contentTree.addListener( contentTreeListener );
        
        treeViewer.getTree().addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    contentTree.removeListener( contentTreeListener );
                }
            }
        );

        treeViewer.addSelectionChangedListener
        (
            new ISelectionChangedListener()
            {
                public void selectionChanged( final SelectionChangedEvent event )
                {
                    ignoreSelectionChange.set( true );
                    
                    try
                    {
                        final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                        final List<MasterDetailsContentNode> nodes = new ArrayList<MasterDetailsContentNode>();
                        
                        for( Iterator<?> itr = selection.iterator(); itr.hasNext(); )
                        {
                            nodes.add( (MasterDetailsContentNode) itr.next() );
                        }
                        
                        contentTree.setSelectedNodes( nodes );
                    }
                    finally
                    {
                        ignoreSelectionChange.set( false );
                    }
                }
            }
        );
        
        treeViewer.addTreeListener
        (
            new ITreeViewerListener()
            {
                public void treeExpanded( final TreeExpansionEvent event )
                {
                    ignoreExpandedStateChange.set( true );
                    
                    try
                    {
                        final MasterDetailsContentNode node = (MasterDetailsContentNode) event.getElement();
                        node.setExpanded( true );
                    }
                    finally
                    {
                        ignoreExpandedStateChange.set( false );
                    }
                }

                public void treeCollapsed( final TreeExpansionEvent event )
                {
                    ignoreExpandedStateChange.set( true );
                    
                    try
                    {
                        final MasterDetailsContentNode node = (MasterDetailsContentNode) event.getElement();
                        node.setExpanded( false );
                    }
                    finally
                    {
                        ignoreExpandedStateChange.set( false );
                    }
                }
            }
        );
        
        final Tree tree = treeViewer.getTree();
        final Menu menu = new Menu( tree );
        tree.setMenu( menu );
        
        menu.addMenuListener
        (
            new MenuAdapter()
            {
                public void menuShown( final MenuEvent event )
                {
                    for( MenuItem item : menu.getItems() )
                    {
                        item.dispose();
                    }
                    
                    final List<MasterDetailsContentNode> selection = contentTree.getSelectedNodes();
                    
                    if( ! selection.isEmpty() )
                    {
                        final List<ActionGroup> actions = getNodeActions( selection, ACTION_CONTEXT_OUTLINE_MENU );
                        ActionsRenderer.fillMenu( menu, actions );
                    }
                }
            }
        );
        
        treeViewer.setExpandedElements( contentTree.getExpandedNodes().toArray() );
        contentTreeListener.handleSelectionChange( contentTree.getSelectedNodes() );
        
        filteredTree.changeFilterText( contentTree.getFilterText() );
        
        return filteredTree;
    }
    
    private static void updateExpandedState( final MasterDetailsContentTree contentTree,
                                             final Tree tree )
    {
        final Set<MasterDetailsContentNode> expandedNodes = new HashSet<MasterDetailsContentNode>();
        gatherExpandedNodes( tree.getItems(), expandedNodes );
        contentTree.setExpandedNodes( expandedNodes );
    }
    
    private static void gatherExpandedNodes( final TreeItem[] items,
                                             final Set<MasterDetailsContentNode> result )
    {
        for( TreeItem item : items )
        {
            if( item.getExpanded() == true )
            {
                result.add( (MasterDetailsContentNode) item.getData() );
                gatherExpandedNodes( item.getItems(), result );
            }
        }
    }
    
    public void dispose() 
    {
        super.dispose();
        
        getContentTree().dispose();
        
        if( this.mainSection != null ) 
        {
            this.mainSection.dispose();
        }
    }
    
    private static final class ContentOutlineFilteredTree
    
        extends FilteredTree
        
    {
        private final MasterDetailsContentTree contentTree;
        private WorkbenchJob refreshJob;

        public ContentOutlineFilteredTree( final Composite parent,
                                           final int treeStyle,
                                           final MasterDetailsContentTree contentTree )
        {
            super( parent, treeStyle, new PatternFilter(), true );
            
            this.contentTree = contentTree;
            
            setBackground( Display.getCurrent().getSystemColor( SWT.COLOR_WHITE ) );
        }

        @Override
        protected WorkbenchJob doCreateRefreshJob()
        {
            final WorkbenchJob base = super.doCreateRefreshJob();
            
            this.refreshJob = new WorkbenchJob( base.getName() ) 
            {
                public IStatus runInUIThread( final IProgressMonitor monitor ) 
                {
                    IStatus st = base.runInUIThread( new NullProgressMonitor() );
                    
                    if( st.getSeverity() == IStatus.CANCEL )
                    {
                        return st;
                    }
                    
                    ContentOutlineFilteredTree.this.contentTree.setFilterText( getFilterString() );
                    
                    updateExpandedState( ContentOutlineFilteredTree.this.contentTree, getViewer().getTree() );
                    
                    return Status.OK_STATUS;
                }
            };
            
            return this.refreshJob;
        }
        
        public void changeFilterText( final String filterText )
        {
            final String currentFilterText = getFilterString();
            
            if( currentFilterText != null && ! currentFilterText.equals( filterText ) )
            {
                setFilterText( filterText );
                textChanged();
                
                //this.refreshJob.cancel();
                //this.refreshJob.runInUIThread( null );
            }
        }
    }
    
    private final class ContentOutline

        extends Page
        implements IContentOutlinePage
        
    {
        private Composite outerComposite = null;
        private FilteredTree filteredTree = null;
        private TreeViewer treeViewer = null;
        private IToolBarManager toolBarManager = null;
        private ActionsCommandBridge actionsCommandBridge = null;
        
        public void init( final IPageSite pageSite ) 
        {
            super.init( pageSite );
            pageSite.setSelectionProvider( this );
        }
        
        @Override
        public void createControl( final Composite parent )
        {
            this.outerComposite = new Composite( parent, SWT.NONE );
            this.outerComposite.setLayout( glayout( 1, 5, 5 ) );
            this.outerComposite.setBackground( Display.getCurrent().getSystemColor( SWT.COLOR_WHITE ) );
            
            this.filteredTree = createContentOutline( this.outerComposite, getContentTree(), false );
            this.filteredTree.setLayoutData( gdfill() );
            
            this.treeViewer = this.filteredTree.getViewer();
            this.toolBarManager = getSite().getActionBars().getToolBarManager();
            this.actionsCommandBridge = new ActionsCommandBridge( this.treeViewer.getTree() );
            
            this.treeViewer.addSelectionChangedListener
            (
                new ISelectionChangedListener()
                {
                    public void selectionChanged( final SelectionChangedEvent event )
                    {
                        refreshActions();
                    }
                }
            );
            
            refreshActions();
        }
        
        private void refreshActions()
        {
            for( IContributionItem item : this.toolBarManager.getItems() )
            {
                this.toolBarManager.remove( item );
            }
            
            final List<MasterDetailsContentNode> selection = getContentTree().getSelectedNodes();
            final List<ActionGroup> actions = getNodeActions( selection, ACTION_CONTEXT_OUTLINE_TOOLBAR );
            ActionsRenderer.fillToolBarManager( this.toolBarManager, getEditor().getSite().getShell(), actions );
            this.actionsCommandBridge.setActions( getNodeActions( selection, ACTION_CONTEXT_OUTLINE_MENU ) );
        }

        @Override
        public Control getControl()
        {
            return this.outerComposite;
        }

        @Override
        public void setFocus()
        {
            this.treeViewer.getControl().setFocus();
        }

        public ISelection getSelection()
        {
            if( this.treeViewer == null ) 
            {
                return StructuredSelection.EMPTY;
            }
            
            return this.treeViewer.getSelection();
        }

        public void setSelection( final ISelection selection )
        {
            if( this.treeViewer != null ) 
            {
                this.treeViewer.setSelection( selection );
            }
        }

        public void addSelectionChangedListener( final ISelectionChangedListener listener )
        {
        }

        public void removeSelectionChangedListener( final ISelectionChangedListener listener )
        {
        }
    }
    
    private final class RootSection 
    
        extends MasterDetailsBlock
        
    {
        private MasterSection masterSection;
        private List<IDetailsPage> detailsSections;
        private Control detailsSectionControl;
        
        public RootSection( final IModel descriptor ) 
        {
            this.detailsSections = new ArrayList<IDetailsPage>();
            this.detailsSectionControl = null;
        }

        @Override
        public void createContent(IManagedForm managedForm) 
        {
            super.createContent( managedForm );
            this.sashForm.setWeights( new int[] { 3, 7 } );
            
            try
            {
                final Field field = this.detailsPart.getClass().getDeclaredField( "pageBook" ); //$NON-NLS-1$
                field.setAccessible( true );
                this.detailsSectionControl = (Control) field.get( this.detailsPart );
            }
            catch( Exception e )
            {
                SapphireUiFrameworkPlugin.log( e );
            }
            
            this.masterSection.handleSelectionChangedEvent( getContentTree().getSelectedNodes() );
            
            setDetailsMaximized( isDetailsMaximized() );
        }
        
        @Override
        protected void createMasterPart( final IManagedForm managedForm, 
                                         final Composite parent ) 
        {
            this.masterSection = new MasterSection( managedForm, parent );
            final SectionPart spart = new SectionPart(this.masterSection);
            managedForm.addPart(spart);
        }

        @Override
        protected void registerPages( final DetailsPart detailsPart ) 
        {
            final IDetailsPage detailsPage = new DetailsSection( MasterDetailsPage.this );
            detailsPart.registerPage( MasterDetailsContentNode.class, detailsPage );
            this.detailsSections.add( detailsPage );
        }
        
        @Override
        protected void applyLayoutData( final SashForm sashForm )
        {
            sashForm.setLayoutData( gdwhint( gdhhint( gdfill(), 200 ), 400 ) );
        }

        public IDetailsPage getCurrentDetailsSection()
        {
            return this.detailsPart.getCurrentPage();
        }
        
        public void setDetailsMaximized( final boolean maximized )
        {
            this.sashForm.setMaximizedControl( maximized ? this.detailsSectionControl : null );
        }
        
        public void dispose()
        {
            if( this.masterSection != null )
            {
                this.masterSection.dispose();
            }
            
            for( IDetailsPage section : this.detailsSections )
            {
                section.dispose();
            }
        }
        
        @Override
        protected void createToolBarActions( IManagedForm managedForm )
        {
        }
    }
    
    private final class MasterSection 

        extends Section
        
    {
        private IManagedForm managedForm;
        private SectionPart sectionPart;
        private TreeViewer treeViewer;
        private Tree tree;
        private ToolBar toolbar;
        private boolean editorLevelNavToolBarActionsPresent = false;
        private List<ActionGroup> actions;
        private final ActionsCommandBridge actionsCommandBridge;
        
        public MasterSection( final IManagedForm managedForm,
                              final Composite parent) 
        {
            super( parent, Section.TITLE_BAR );
            
            final FormToolkit toolkit = managedForm.getToolkit();
            
            FormColors colors = toolkit.getColors();
            this.setMenu(parent.getMenu());
            toolkit.adapt(this, true, true);
            if (this.toggle != null) {
                this.toggle.setHoverDecorationColor(colors
                        .getColor(IFormColors.TB_TOGGLE_HOVER));
                this.toggle.setDecorationColor(colors
                        .getColor(IFormColors.TB_TOGGLE));
            }
            this.setFont(createBoldFont(colors.getDisplay(), this.getFont()));
            colors.initializeSectionToolBarColors();
            this.setTitleBarBackground(colors.getColor(IFormColors.TB_BG));
            this.setTitleBarBorderColor(colors
                    .getColor(IFormColors.TB_BORDER));
            this.setTitleBarForeground(colors
                    .getColor(IFormColors.TB_TOGGLE));
            
            this.marginWidth = 10;
            this.marginHeight = 10;
            setLayoutData( gdfill() );
            setLayout( glayout( 1, 0, 0 ) );
            setText( MasterDetailsPage.this.definition.getOutlineHeaderText().getLocalizedText() );
            
            final Composite client = toolkit.createComposite( this );
            client.setLayout( glayout( 1, 0, 0 ) );
            
            this.managedForm = managedForm;
            
            final MasterDetailsContentTree contentTree = getContentTree();
            
            final FilteredTree filteredTree = createContentOutline( client, contentTree, true );
            this.treeViewer = filteredTree.getViewer();
            this.tree = this.treeViewer.getTree();
            
            this.sectionPart = new SectionPart( this );
            this.managedForm.addPart( this.sectionPart );
    
            contentTree.addListener
            (
                new MasterDetailsContentTree.Listener()
                {
                    @Override
                    public void handleSelectionChange( final List<MasterDetailsContentNode> selection )
                    {
                        handleSelectionChangedEvent( selection );
                    }
                }
            );
            
            final GridLayout toolbarsCompositeLayout = glayout( 2, 0, 0 );
            toolbarsCompositeLayout.horizontalSpacing = 0;
            toolbarsCompositeLayout.verticalSpacing = 0;
            
            final List<ActionGroup> editorLevelNavigationToolBarActions = getActions( ACTION_CONTEXT_OUTLINE_TOOLBAR );
            
            if( editorLevelNavigationToolBarActions.isEmpty() )
            {
                this.editorLevelNavToolBarActionsPresent = false;
                this.toolbar = new ToolBar( this, SWT.FLAT | SWT.HORIZONTAL );
                setTextClient( this.toolbar );
            }
            else
            {
                this.editorLevelNavToolBarActionsPresent = true;

                final Composite toolbarsComposite = new Composite( this, SWT.NONE );
                toolbarsComposite.setLayout( toolbarsCompositeLayout );
                
                this.toolbar = new ToolBar( toolbarsComposite, SWT.FLAT | SWT.HORIZONTAL );
                this.toolbar.setLayoutData( gdhfill() );
                
                final ToolBar editorLevelActionsToolbar = new ToolBar( toolbarsComposite, SWT.FLAT | SWT.HORIZONTAL );
                editorLevelActionsToolbar.setLayoutData( gdhfill() );
                ActionsRenderer.fillToolBar( editorLevelActionsToolbar, getActions( ACTION_CONTEXT_OUTLINE_TOOLBAR ) );
                
                setTextClient( toolbarsComposite );
            }
            
            this.actionsCommandBridge = new ActionsCommandBridge( this.tree );
            
            toolkit.paintBordersFor( this );
            setClient( client );
        }
        
        private Font createBoldFont(Display display, Font regularFont) {
            FontData[] fontDatas = regularFont.getFontData();
            for (int i = 0; i < fontDatas.length; i++) {
                fontDatas[i].setStyle(fontDatas[i].getStyle() | SWT.BOLD);
            }
            return new Font(display, fontDatas);
        }
        
        public void refreshActions()
        {
            for( ToolItem item : this.toolbar.getItems() )
            {
                item.dispose();
            }
            
            final List<MasterDetailsContentNode> selection = getContentTree().getSelectedNodes();

            this.actions = getNodeActions( selection, null );
            ActionsRenderer.fillToolBar( this.toolbar, this.actions, this.editorLevelNavToolBarActionsPresent );
            
            this.actionsCommandBridge.setActions( getNodeActions( selection, ACTION_CONTEXT_OUTLINE_MENU ) );
            
            this.toolbar.getParent().getParent().layout( true, true );
        }
        
        private void handleSelectionChangedEvent( final List<MasterDetailsContentNode> selection )
        {
            final IStructuredSelection sel
                = ( selection.isEmpty() ? StructuredSelection.EMPTY : new StructuredSelection( selection.get( 0 ) ) );
            
            this.managedForm.fireSelectionChanged( this.sectionPart, sel );
            refreshActions();
        }
    }
    
    private static class DetailsSection 

        extends SapphireRenderingContext
        implements IDetailsPage
        
    {
        protected final MasterDetailsPage mainPage;
        private MasterDetailsContentNode node;
        protected IManagedForm mform;
        protected FormToolkit toolkit;
        
        public DetailsSection( final MasterDetailsPage mainPage )
        {
            super( null, null );
            
            this.mainPage = mainPage;
            this.node = null;
        }
        
        public SapphirePart getPart()
        {
            return this.node;
        }
        
        public void initialize( final IManagedForm form ) 
        {
            this.mform = form;
            this.toolkit = this.mform.getToolkit();
        }
    
        public final void createContents( final Composite parent ) 
        {
            this.composite = parent;
            
            final TableWrapLayout twl = twlayout( 1, 10, 10, 10, 10 );
            twl.verticalSpacing = 20;

            parent.setLayout( twl );
            
            createSections();
        }
        
        public void commit(boolean onSave) {
        }
    
        public void dispose() {
        }
    
        public boolean isDirty() {
            return false;
        }
    
        public boolean isStale() {
            return false;
        }
    
        public void refresh()
        {
        }

        public void setFocus() {
        }
        
        public boolean setFormInput(Object input) {
            return false;
        }
    
        public void adapt( final Control control )
        {
            super.adapt( control );
            
            if( control instanceof Composite )
            {
                this.toolkit.adapt( (Composite) control );
            }
            else if( control instanceof Label )
            {
                this.toolkit.adapt( control, false, false );
            }
            else
            {
                this.toolkit.adapt( control, true, true );
            }
        }
        
        @Override
        protected String getHelpContextIdPrefix() 
        {
            return this.mainPage.getEditor().getHelpContextIdPrefix();
        }
        
        public void selectionChanged( final IFormPart part, 
                                      final ISelection selection ) 
        {
            final IStructuredSelection ssel = (IStructuredSelection) selection;
            
            if( ssel.size() == 1 ) 
            {
                this.node = (MasterDetailsContentNode) ssel.getFirstElement();
            }
            else
            {
                this.node = null;
            }
            
            createSections();
        }
        
        protected void createSections()
        {
            final Composite rootComposite = getComposite();
            
            for( Control control : rootComposite.getChildren() )
            {
                control.setVisible( false );
                control.dispose();
            }
            
            if( this.node != null )
            {
                for( SapphireSection section : this.node.getSections() )
                {
                    if( section.checkVisibleWhenCondition() == false )
                    {
                        continue;
                    }
                    
                    section.render( this );
                }
            }
            
            rootComposite.getParent().layout( true, true );
        }
    }
    
}
