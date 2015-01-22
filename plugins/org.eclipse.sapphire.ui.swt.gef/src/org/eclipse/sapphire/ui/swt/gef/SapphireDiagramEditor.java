/******************************************************************************
 * Copyright (c) 2015 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 *    Shenxue Zhou - Customized grid layer, grid state save and restore; DND fixes.
 *    Shenxue Zhou - [365019] SapphireDiagramEditor does not work on non-workspace files 
 *    Gregory Amerson - [374022] SapphireGraphicalEditor init with SapphireEditor
 *    Konstantin Komissarchik - [376245] Revert action in StructuredTextEditor does not revert diagram nodes and connections in SapphireDiagramEditor
 *    Gregory Amerson - [346172] Support zoom, print and save as image actions in the diagram editor
 *    Konstantin Komissarchik - [346172] Support zoom, print and save as image actions in the diagram editor
 *    Konstantin Komissarchik - [381794] Cleanup needed in presentation code for diagram context menu
 *    Konstantin Komissarchik - [382449] Support EL in EditorPageDef.PageHeaderText
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef;

import static org.eclipse.sapphire.ui.forms.swt.SwtUtil.toImageDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.editparts.GridLayer;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.localization.LabelTransformer;
import org.eclipse.sapphire.ui.Bounds;
import org.eclipse.sapphire.ui.ISapphireEditorActionContributor;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphireEditor;
import org.eclipse.sapphire.ui.SapphireEditorPagePart.PageHeaderImageEvent;
import org.eclipse.sapphire.ui.SapphireEditorPagePart.PageHeaderTextEvent;
import org.eclipse.sapphire.ui.SapphireEditorPagePart.SelectionChangedEvent;
import org.eclipse.sapphire.ui.SapphireHelpContext;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.def.EditorPageDef;
import org.eclipse.sapphire.ui.diagram.ConnectionService;
import org.eclipse.sapphire.ui.diagram.DiagramConnectionPart;
import org.eclipse.sapphire.ui.diagram.def.DiagramEditorPageDef;
import org.eclipse.sapphire.ui.diagram.editor.DiagramDirectEditPartEvent;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramPageEvent;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart.AutoLayoutEvent;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart.ZoomLevelEvent;
import org.eclipse.sapphire.ui.diagram.editor.ShapePart;
import org.eclipse.sapphire.ui.diagram.layout.DiagramLayoutPersistenceService;
import org.eclipse.sapphire.ui.forms.swt.ActionBridge;
import org.eclipse.sapphire.ui.forms.swt.ActionSystemPartBridge;
import org.eclipse.sapphire.ui.forms.swt.EditorPagePresentation;
import org.eclipse.sapphire.ui.forms.swt.HelpSystem;
import org.eclipse.sapphire.ui.forms.swt.SapphireActionPresentationManager;
import org.eclipse.sapphire.ui.forms.swt.SapphireToolBarManagerActionPresentation;
import org.eclipse.sapphire.ui.swt.gef.contextbuttons.ContextButtonManager;
import org.eclipse.sapphire.ui.swt.gef.dnd.ObjectsTransferDropTargetListener;
import org.eclipse.sapphire.ui.swt.gef.dnd.SapphireTemplateTransferDropTargetListener;
import org.eclipse.sapphire.ui.swt.gef.internal.DiagramEditorContextMenuProvider;
import org.eclipse.sapphire.ui.swt.gef.layout.HorizontalGraphLayout;
import org.eclipse.sapphire.ui.swt.gef.layout.VerticalGraphLayout;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramConnectionModel;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramModel;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramModelBase;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramNodeModel;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;
import org.eclipse.sapphire.ui.swt.gef.model.ShapeModel;
import org.eclipse.sapphire.ui.swt.gef.model.ShapeModelUtil;
import org.eclipse.sapphire.ui.swt.gef.palette.DefaultFlyoutPalettePreferences;
import org.eclipse.sapphire.ui.swt.gef.parts.DiagramConnectionEditPart;
import org.eclipse.sapphire.ui.swt.gef.parts.DiagramNodeEditPart;
import org.eclipse.sapphire.ui.swt.gef.parts.SapphireDiagramEditorEditPartFactory;
import org.eclipse.sapphire.ui.swt.gef.presentation.DiagramPagePresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.ILayoutExtension;
import org.eclipse.ui.forms.widgets.SizeCache;
import org.eclipse.ui.internal.forms.widgets.FormHeading;
import org.eclipse.ui.internal.forms.widgets.FormUtil;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class SapphireDiagramEditor extends GraphicalEditorWithFlyoutPalette implements ISapphireEditorActionContributor, EditorPagePresentation
{
    private final SapphireEditor editor;
    private Element element;
    private DefinitionLoader.Reference<EditorPageDef> definition;
    private SapphireDiagramEditorPagePart part;
    
    private DiagramLayoutPersistenceService layoutPersistenceService;
    private ConnectionService connectionService;
	private PaletteRoot root;
	private DiagramPagePresentation diagramPresentation;
    private DiagramModel diagramModel;
    private Listener layoutPersistenceServiceListener;
    private List<ISapphirePart> selectedParts = new ArrayList<ISapphirePart>();
    private List<GraphicalEditPart> selectedEditParts = null;
    private boolean editorIsDirty = false;

	private Point mouseLocation;
	private DiagramConfigurationManager configManager;
	private GraphicalViewerKeyHandler graphicalViewerKeyHandler;
	private SapphireDiagramKeyHandler diagramKeyHandler;
	private ContextButtonManager contextButtonManager = null;
	private boolean directEditingActive = false;
	
	// Diagram header, borrowed from org.eclipse.ui.forms.widgets.Form class
	private FormHeading header;
	private Composite body;
	private SizeCache bodyCache = new SizeCache();
	private SizeCache headCache = new SizeCache();	
	private FormColors formColors;
	private Listener diagramEditorPagePartListener;

	private Map<String,ActionBridge> globalActions;

	private SapphireDiagramOutline diagramOutline;
	
	private boolean isSelectionFromPagePart = false;
	
	
	public SapphireDiagramEditor( final SapphireEditor editor,
                                  final Element element,
                                  final DefinitionLoader.Reference<EditorPageDef> definition )
	{
        if( editor == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( element == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( definition == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.editor = editor;
        this.element = element;
        this.definition = definition;

        this.part = new SapphireDiagramEditorPagePart();
		this.part.init( editor, this.element, this.definition.resolve(), Collections.<String, String> emptyMap() );
		this.part.initialize();
        
        final String partName = this.definition.resolve().getPageName().localized( CapitalizationType.TITLE_STYLE, false );
        setPartName( partName );

        // Initialize connection service
        this.connectionService = SapphireDiagramEditorFactory.getConnectionService(this.part);
		// Initialize layout persistence service
		this.layoutPersistenceService = SapphireDiagramEditorFactory.getLayoutPersistenceService(this.part);
		
        this.configManager = new DiagramConfigurationManager(this);
        
		setEditDomain(new DefaultEditDomain(this));
		
		this.part.attach
        (
            new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    if( event instanceof SelectionChangedEvent )
                    {
                    	isSelectionFromPagePart = true;
                        selectParts(part.getSelections());
                        isSelectionFromPagePart = false;
                    } 
                    else if ( event instanceof DiagramPageEvent )
	                {
	                	handleDiagramPageEvent((DiagramPageEvent)event);
	                } 
                    else if ( event instanceof DiagramDirectEditPartEvent )
	                {
	    		    	selectAndDirectEditPart(((DiagramDirectEditPartEvent)event).part());
	                }
                }
            }
        );
        		
		this.layoutPersistenceServiceListener = new Listener() 
		{
            @Override
            public void handle( final Event event )
            {
                if( event instanceof DiagramLayoutPersistenceService.DirtyStateEvent )
                {
                    final DiagramLayoutPersistenceService.DirtyStateEvent evt = (DiagramLayoutPersistenceService.DirtyStateEvent) event;
                    
                    if( evt.after() == true )
                    {
                        markEditorDirty();
                    }
                    else
                    {
                        markEditorClean();
                    }
                }
            }			
		};
		
		this.layoutPersistenceService.attach(this.layoutPersistenceServiceListener);
		
        this.diagramEditorPagePartListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                if( event instanceof PageHeaderTextEvent )
                {
                    refreshPageHeaderText();
                }
                else if( event instanceof PageHeaderImageEvent )
                {
                    refreshPageHeaderImage();
                }
                else if( event instanceof ZoomLevelEvent )
                {
                    refreshZoomLevel();
                }
                else if (event instanceof AutoLayoutEvent)
                {
                	autoLayout(((AutoLayoutEvent)event).horizontal());
                }
            }
        };
        
        this.part.attach( this.diagramEditorPagePartListener );
    }
    
    private void handleDiagramPageEvent(DiagramPageEvent event) {
    	switch(event.getDiagramPageEventType()) {
	    	case GridStateChange:
				getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE, part.isGridVisible());
				getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_ENABLED, part.isGridVisible());
				getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_SPACING, 
						new Dimension(part.getGridUnit(), part.getVerticalGridUnit()));
				markEditorDirty();
	    		break;
	    	case GuideStateChange:
				getGraphicalViewer().setProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED, Boolean.valueOf(part.isShowGuides()));
				markEditorDirty();
	    		break;
	    	case DiagramChange:
				refreshPalette();
	    		break;   
	    	case DiagramSave:
	    		// Noop
	    		break;
	    	case SelectAll:
		    	selectAll();
    		break;
	    	case SelectAllNodes:
		    	selectAllNodes();
	    		break;
	    	default:
	    		break;
    	}
	}

    public DiagramEditorPageDef getDiagramEditorPageDef()
    {
    	return (DiagramEditorPageDef) this.definition.resolve();
    }
    
    public final SapphireEditor getEditor()
    {
        return this.editor;
    }
    
    public SapphireDiagramEditorPagePart getPart()
    {
        return this.part;
    }
    
    public final Element getModelElement()
    {
        return this.part.getModelElement();
    }
     
    public DiagramLayoutPersistenceService getLayoutPersistenceService()
    {
    	return this.layoutPersistenceService;
    }
    
    public ConnectionService getConnectionService()
    {
    	return this.connectionService;
    }
    
	@Override
	public boolean isDirty()
	{
		return this.editorIsDirty;
	}
	
	protected void markEditorDirty() {
		this.editorIsDirty = true;
		firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY);
	}
	
	protected void markEditorClean() {
		this.editorIsDirty = false;
		firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY);
	}

	@Override
	public DefaultEditDomain getEditDomain() {
		return super.getEditDomain();
	}
	
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// If not the active editor, ignore selection changed.
		boolean editorIsActive = getSite().getPage().isPartVisible(this);
		if (!editorIsActive) {
			// Check if we are a page of the active multipage editor
			IEditorPart activeEditor = getSite().getPage().getActiveEditor();
			if (activeEditor != null) {
				// Check if the top level editor if it is active
				editorIsActive = getSite().getPage().isPartVisible(activeEditor);
				if ( activeEditor instanceof FormEditor ) {
                    if ( !( this.equals( ( (FormEditor) activeEditor).getActiveEditor() ) ) ) {
                        editorIsActive = false;
                    }
                }
            }
        }
        if (editorIsActive) 
        {			
			// Bug 339360 - MultiPage Editor's selectionProvider does not notify PropertySheet (edit) 
			// bypass the selection provider
			if (selection instanceof StructuredSelection) 
			{
				StructuredSelection structuredSelection = (StructuredSelection) selection;
				List<ISapphirePart> partList = new ArrayList<ISapphirePart>();
				List<GraphicalEditPart> editPartList = new ArrayList<GraphicalEditPart>();
				for (Iterator<?> iterator = structuredSelection.iterator(); iterator.hasNext();) 
				{
					Object object = iterator.next();
					EditPart editPart = null;
					if (object instanceof EditPart) 
					{
						editPart = (EditPart) object;
					}
					else if (object instanceof IAdaptable) 
					{
						editPart = (EditPart) ((IAdaptable) object).getAdapter(EditPart.class);						
					}
					if (editPart != null && editPart.getModel() instanceof DiagramModelBase) 
					{
						SapphirePart sp = ((DiagramModelBase)editPart.getModel()).getSapphirePart();
						partList.add(sp);
						editPartList.add((GraphicalEditPart)editPart);
					}
				}
				// If the properties sheet has multiple tabs, switching tabs would trigger selection changed
				// event. But the selection would be empty. So we don't want to update the properties sheet or
				// the key handler in this case.
				if (!partList.isEmpty())
				{	
					this.selectedParts = partList;
					this.selectedEditParts = editPartList;
					if (!this.isSelectionFromPagePart)
					{
						getPart().setSelections(partList, false);
					}
					updateKeyHandler();
					
//					// [Bug 380728] Floating toolbar appears on a node when multiple nodes are selected
//					if (partList.size() > 1 || !(partList.get(0) instanceof DiagramNodePart))
//					{
//						this.contextButtonManager.hideContextButtonsInstantly();
//					}
					this.contextButtonManager.refresh();
				}				
			}
		}
	}    
	
	private void initActions()
	{
	    this.globalActions = new HashMap<String,ActionBridge>();
		
		final ActionBridge selectAllBridge = new ActionBridge( this.diagramPresentation, this.part.getAction( "Sapphire.Diagram.SelectAll" ) );
		this.globalActions.put( ActionFactory.SELECT_ALL.getId(), selectAllBridge );
		
        final ActionBridge deleteBridge = new ActionBridge( this.diagramPresentation, this.part.getAction( "Sapphire.Delete" ) );
        this.globalActions.put( ActionFactory.DELETE.getId(), deleteBridge );

        final ActionBridge printBridge = new ActionBridge( this.diagramPresentation, this.part.getAction( "Sapphire.Diagram.Print" ) );
        this.globalActions.put( ActionFactory.PRINT.getId(), printBridge );
	}
	
	private void updateKeyHandler()
	{
        if (this.diagramKeyHandler != null)
        {
        	this.diagramKeyHandler.dispose();
        	this.diagramKeyHandler = null;
        }
		List<ISapphirePart> selectedParts = this.getSelectedParts();
		this.diagramKeyHandler = new SapphireDiagramKeyHandler(this, selectedParts);
        if (this.graphicalViewerKeyHandler == null)
        {
        	graphicalViewerKeyHandler = new GraphicalViewerKeyHandler(getGraphicalViewer());
        }
		KeyHandler parentKeyHandler = graphicalViewerKeyHandler.setParent(this.diagramKeyHandler);
		getGraphicalViewer().setKeyHandler(parentKeyHandler);
	}
	
	private void refreshPalette() {
		PaletteRoot pr = getPaletteRoot();
		if (pr instanceof SapphirePaletteRoot) {
			SapphirePaletteRoot spr = (SapphirePaletteRoot) pr;
			spr.updatePaletteEntries();
		}
	}

	@Override
	protected PaletteRoot getPaletteRoot() {
		if (root == null)
			root = new SapphirePaletteRoot(part);
		return root;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			this.part.saveDiagram();
			markEditorClean();
		} catch (Exception e) {
		    Sapphire.service( LoggingService.class ).log( e );
		}
	}

	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);

		// Is this the right place?
		setEditDomain(new DefaultEditDomain(this));
				
	}

	@Override
	protected void initializeGraphicalViewer() {
		super.initializeGraphicalViewer();

		GraphicalViewer viewer = getGraphicalViewer();
		
		viewer.getControl().addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				setMouseLocation(e.x, e.y);
			}
		});
		
		// set the contents of this editor
		viewer.setContents(diagramModel);
		
		// listen for dropped parts
		viewer.addDropTargetListener(new SapphireTemplateTransferDropTargetListener(this));
		viewer.addDropTargetListener((TransferDropTargetListener) new ObjectsTransferDropTargetListener(viewer));
		postInit();
	}
	
	private void postInit()
	{
		// refresh node bounds
		for (DiagramNodeModel node : this.diagramModel.getNodes())
		{
			node.handleMoveNode();
		}

		//initRenderingContext();
		initActions();
		configureDiagramHeading();

		// If the layout file doesn't exist or no layout is written to the layout file, apply auto layout
		if (hasNoExistingLayout()) 
		{
			getGraphicalViewer().flush();
			getPart().autoLayout(true);
			markEditorClean();
		}
		
	}

	private void autoLayout(boolean horizontal)
	{
		if (horizontal)
		{
			new HorizontalGraphLayout().layout(this, true);
		}
		else
		{
			new VerticalGraphLayout().layout(this, true);
		}
	}
	

	private boolean hasNoExistingLayout()
	{
		if (this.layoutPersistenceService == null)
		{
			return true;
		}
		List<DiagramNodeModel> nodes = this.diagramModel.getNodes();
		for (DiagramNodeModel node : nodes)
		{
			DiagramNodePart nodePart = node.getModelPart();
			Bounds bounds = nodePart.getNodeBounds();
			if (bounds.getX() == -1 || bounds.getY() == -1)
			{
				return true;
			}
		}
				
		return false;
	}
	
	public DiagramPagePresentation getDiagramPresentation() {
		return this.diagramPresentation;
	}
	
	public DiagramModel getDiagramModel() {
		return this.diagramModel;
	}
	
	public DiagramResourceCache getResourceCache()
	{
		return this.diagramPresentation.getResourceCache();
	}
	
	public DiagramConfigurationManager getConfigurationManager()
	{
		return this.configManager;
	}
	
	public ContextButtonManager getContextButtonManager() 
	{
		return contextButtonManager;
	}
	
	@Override
	public void createPartControl(Composite parent) 
	{
		final Composite main = new Composite( parent, SWT.NONE );
		main.setLayout(new FormLayout());
		this.header = new FormHeading(main, SWT.NULL);
		this.formColors = new FormColors(parent.getDisplay());
		        
		super.createPartControl(main);
		this.body = getGraphicalControl().getParent();
	}
		
	@Override
	protected void configureGraphicalViewer() 
	{
		super.configureGraphicalViewer();
		GraphicalViewer viewer = getGraphicalViewer();		
        this.diagramPresentation = new DiagramPagePresentation(this.part, this.configManager, viewer.getControl().getShell());
        this.diagramModel = new DiagramModel(this.diagramPresentation);		
						
		viewer.setEditPartFactory(new SapphireDiagramEditorEditPartFactory(getConfigurationManager()));
		
		viewer.setRootEditPart(new ScalableFreeformRootEditPart()
		{
			@Override
			protected GridLayer createGridLayer() 
			{
				return new SapphireDiagramGridLayer(diagramModel);
			}			
		});
		
		// configure the context menu provider
		ContextMenuProvider cmProvider = new DiagramEditorContextMenuProvider(this);
		viewer.setContextMenu(cmProvider);
		
		// Configure grid and guide properties
		boolean isGridVisibleInViewer = false;
		if (viewer.getProperty(SnapToGrid.PROPERTY_GRID_VISIBLE) != null)
		{
			isGridVisibleInViewer = (Boolean) viewer.getProperty(SnapToGrid.PROPERTY_GRID_VISIBLE);
		}
		if (this.part.isGridVisible() != isGridVisibleInViewer)
		{
			viewer.setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE, this.part.isGridVisible());
			viewer.setProperty(SnapToGrid.PROPERTY_GRID_ENABLED, this.part.isGridVisible());
			viewer.setProperty(SnapToGrid.PROPERTY_GRID_SPACING, 
					new Dimension(this.part.getGridUnit(), this.part.getVerticalGridUnit()));
		}
		
		boolean isShowGuidesInViewer = false;
		if (viewer.getProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED) != null)
		{
			isShowGuidesInViewer = (Boolean)viewer.getProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED);
		}
		if (this.part.isShowGuides() != isShowGuidesInViewer)
		{
			viewer.setProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED, this.part.isShowGuides());
		}
		
		// Support context help
		
		this.getGraphicalControl().addHelpListener(new HelpListener() 
        {
            public void helpRequested(HelpEvent event) 
            {            	
            	if (getSelectedParts() != null && getSelectedParts().size() == 1)
            	{
            		ISapphirePart part = getSelectedParts().get(0);
	            	final SapphireHelpContext context = new SapphireHelpContext(part.getLocalModelElement(), null);
	            	if (context.getText() != null || (context.getRelatedTopics() != null && context.getRelatedTopics().length > 0))
	            	{
		                // determine a location in the upper right corner of the widget
		                org.eclipse.swt.graphics.Point point = HelpSystem.computePopUpLocation(event.widget.getDisplay());
		                // display the help
		                PlatformUI.getWorkbench().getHelpSystem().displayContext(context, point.x, point.y);
	            	}
            	}
            }
        });
		
		// context button manager
		contextButtonManager = new ContextButtonManager(this);
		
		final int zoomLevel = getPart().state().getZoomLevel().content();
		final double zoom = (double) zoomLevel / 100;
        
        getZoomManager().setZoom( zoom );
	}
			
	@Override
	protected PaletteViewerProvider createPaletteViewerProvider() 
	{
		return new PaletteViewerProvider(getEditDomain()) 
		{
			@Override
			protected void configurePaletteViewer(PaletteViewer viewer)
			{
				super.configurePaletteViewer(viewer);
				viewer.addDragSourceListener(new TemplateTransferDragSourceListener(viewer));
			}
		};
	}
	
	@Override
	protected FlyoutPreferences getPalettePreferences() 
	{
		return new DefaultFlyoutPalettePreferences(getPart().state());
	}
	
	
	public IAction getAction(String actionId)
	{
		if (actionId.equals(ActionFactory.SELECT_ALL.getId()))
		{
			return this.globalActions.get(ActionFactory.SELECT_ALL.getId());
		}
		else if (actionId.equals(ActionFactory.DELETE.getId()))
		{
			return this.globalActions.get(ActionFactory.DELETE.getId());
		}
		else if (actionId.equals(ActionFactory.PRINT.getId()))
		{
			return this.globalActions.get(ActionFactory.PRINT.getId());
		}
		
		return getActionRegistry().getAction(actionId);
	}
	
	@Override 
	@SuppressWarnings( "rawtypes" )
	
    public Object getAdapter(Class type)
	{
		if (type == IContentOutlinePage.class)
		{
			return getDiagramOutline();
		}		
		return super.getAdapter(type);
	}
	
	@Override
	public GraphicalViewer getGraphicalViewer() {
		return super.getGraphicalViewer();
	}

	public List<ISapphirePart> getSelectedParts()
	{
		return this.selectedParts;
	}
	
	public List<GraphicalEditPart> getSelectedEditParts()
	{
		return this.selectedEditParts;
	}
	
	public GraphicalEditPart getGraphicalEditPart(ISapphirePart sapphirePart)
	{
		if (sapphirePart instanceof DiagramNodePart || sapphirePart instanceof ShapePart || sapphirePart instanceof DiagramConnectionPart)
		{
			GraphicalViewer viewer = this.getGraphicalViewer();
			
			Object editpartObj = null;
			DiagramNodePart nodePart = null;
			DiagramConnectionPart connPart = null;
			if (sapphirePart instanceof DiagramNodePart)
			{
				nodePart = (DiagramNodePart)sapphirePart;
				DiagramNodeModel nodeModel = this.getDiagramModel().getDiagramNodeModel(nodePart);
				editpartObj = viewer.getEditPartRegistry().get(nodeModel);
			}
			else if (sapphirePart instanceof ShapePart)
			{
				nodePart = sapphirePart.nearest(DiagramNodePart.class);
				DiagramNodeModel nodeModel = this.getDiagramModel().getDiagramNodeModel(nodePart);
				ShapeModel shapeModel = ShapeModelUtil.getChildShapeModel(nodeModel.getShapeModel(), (ShapePart)sapphirePart);
				editpartObj = viewer.getEditPartRegistry().get(shapeModel);
			}
			else if (sapphirePart instanceof DiagramConnectionPart)
			{
				connPart = (DiagramConnectionPart)sapphirePart;
				DiagramConnectionModel connModel = this.getDiagramModel().getDiagramConnectionModel(connPart);
				editpartObj = viewer.getEditPartRegistry().get(connModel);				
			}
			return (GraphicalEditPart)editpartObj;
		}
		return null;
	}
	
	public void selectAndDirectEditPart(final ISapphirePart part)
	{
		if (part instanceof DiagramNodePart || part instanceof ShapePart || part instanceof DiagramConnectionPart)
		{
			GraphicalViewer viewer = getGraphicalViewer();
			// Bug 370869 - DND from the tool palette would show an invalid cursor before placing the new node 
			// in direct edit mode. TODO why?
			//viewer.getControl().forceFocus();
			
			GraphicalEditPart editpart = getGraphicalEditPart(part);
			ISapphirePart parentPart = part.parent();
			while ((editpart == null || !editpart.isSelectable()) && parentPart != null)
			{
				editpart = getGraphicalEditPart(parentPart);
				parentPart = parentPart.parent();
			}
			if (editpart != null) 
			{
				// Force a layout first.
				viewer.flush();
				viewer.select(editpart);
				viewer.reveal(editpart);
				if (part instanceof DiagramNodePart)
				{					
					getDiagramModel().handleDirectEditing((DiagramNodePart)part);
				}
				else if (part instanceof ShapePart)
				{
					getDiagramModel().handleDirectEditing((ShapePart)part);
				}
				else if (part instanceof DiagramConnectionPart)
				{
					getDiagramModel().handleDirectEditing((DiagramConnectionPart)part);
				}
				
			}
		}
    }
	
	public void selectAll()
	{
		GraphicalViewer viewer = this.getGraphicalViewer();
		for (Object obj : viewer.getEditPartRegistry().values())
		{
			if (obj instanceof DiagramConnectionEditPart ||
					obj instanceof DiagramNodeEditPart)
			{
				viewer.appendSelection((EditPart)obj);
			}
		}
	}
	
	public void selectAllNodes()
	{
		GraphicalViewer viewer = this.getGraphicalViewer();
		viewer.deselectAll();
		for (Object obj : viewer.getEditPartRegistry().values())
		{
			if (obj instanceof DiagramNodeEditPart)
			{
				viewer.appendSelection((DiagramNodeEditPart)obj);
			}
		}
	}

	public void selectParts(final List<ISapphirePart> selections)
	{
		boolean selectionChanged = false;
    	if (this.selectedParts.size() != selections.size())
    	{
    		selectionChanged = true;
    	}
    	else if (!this.selectedParts.containsAll(selections) || !selections.containsAll(this.selectedParts))
    	{
    		selectionChanged = true;
    	}
    	if (selectionChanged)
    	{
    		GraphicalViewer viewer = this.getGraphicalViewer();
    		
    		if (selections.isEmpty())
    		{
    			viewer.flush();
    			viewer.deselectAll();
    			return;
    		}
    		boolean first = true;
    		for (ISapphirePart sapphirePart : selections)
    		{
    			Object editpartObj = getGraphicalEditPart(sapphirePart);
    			if (editpartObj != null)
    			{
    				if (first)
    				{
    					viewer.flush();
    					viewer.select((EditPart)editpartObj);    					
    					first = false;
    				}
    				else
    				{
    					viewer.appendSelection((EditPart)editpartObj);
    				}
    				viewer.reveal((EditPart)editpartObj);
    			}
    		}    		
    	}
	}
	
	public Point getMouseLocation() {
		if (mouseLocation == null) {
			mouseLocation = new Point();
		}
		return mouseLocation;
	}

	private void setMouseLocation(int x, int y) 
	{
		getMouseLocation().setLocation(x, y);
		Point realLocation = calculateRealMouseLocation(getMouseLocation());
		this.part.setMouseLocation(realLocation.x, realLocation.y);
	}
	
	public FigureCanvas getFigureCanvas() 
	{
		GraphicalViewer viewer = getGraphicalViewer();
		return (FigureCanvas) viewer.getControl();
	}
	
	public double getZoomLevel() {
		ZoomManager zoomManager = getZoomManager();
		if (zoomManager == null)
			return 1;

		/*
		 * avoid long running calculations for large diagrams and zoom factors
		 * below 5%
		 */
		return Math.max(0.05D, zoomManager.getZoom());
	}

	/**
	 * Calculates the location in dependence from scrollbars and zoom factor.
	 * 
	 * @param nativeLocation
	 *            the native location
	 * @return the point
	 */
	public Point calculateRealMouseLocation(Point nativeLocation) 
	{
		Point ret = new Point(nativeLocation);
		Point viewLocation;
		// view location depends on the current scroll bar position
		viewLocation = getFigureCanvas().getViewport().getViewLocation();

		ret.x += viewLocation.x;
		ret.y += viewLocation.y;
		
		final ZoomManager zoomManager = getZoomManager();

		if( zoomManager != null )
		{
			ret = ret.getScaled( 1 / zoomManager.getZoom() );
		}

		return ret;
	}
    
	@Override
	public void dispose() {
		super.dispose();
		
		diagramPresentation.dispose();
		
		if (layoutPersistenceService != null)
		{
		    // FIXME: KOSTA: This is very questionable. Only service context should be disposing services.
		    
		    layoutPersistenceService.detach(this.layoutPersistenceServiceListener);
			layoutPersistenceService.dispose();
		}
		
        this.part.detach( this.diagramEditorPagePartListener );        
        final Image image = this.header.getImage();        
        if( image != null )
        {
            image.dispose();
        }
        
        for( ActionSystemPartBridge bridge : this.globalActions.values() )
        {
            bridge.dispose();
        }
        
        this.element = null;
        
        this.part.dispose();
        this.part = null;
        
        this.definition.dispose();
        this.definition = null;
	}
	
	public boolean isDirectEditingActive() 
	{
		return directEditingActive;
	}

	public void setDirectEditingActive(boolean directEditingActive) 
	{
		this.directEditingActive = directEditingActive;
		getContextButtonManager().hideContextButtonsInstantly();
	}
	
	private void configureDiagramHeading()
	{
		decorateHeading();
		
        final SapphireActionGroup actions = this.part.getActions( SapphireActionSystem.CONTEXT_DIAGRAM_HEADER );
        if (actions != null && !actions.isEmpty())
        {
	        final SapphireActionPresentationManager actionPresentationManager = new SapphireActionPresentationManager(this.diagramPresentation, actions);
	        final SapphireToolBarManagerActionPresentation actionPresentation = new SapphireToolBarManagerActionPresentation( actionPresentationManager );
	        actionPresentation.setToolBarManager( this.header.getToolBarManager() );
	        actionPresentation.render();
        }
        
        refreshPageHeaderText();
        refreshPageHeaderImage();
	}
	
	/**
	 * Takes advantage of the gradients and other capabilities to decorate the
	 * form heading using colors computed based on the current skin and
	 * operating system.
	 * 
	 * @since 3.3
	 */

	private void decorateHeading() 
	{
		Color top = this.formColors.getColor(IFormColors.H_GRADIENT_END);
		Color bot = this.formColors.getColor(IFormColors.H_GRADIENT_START);
		this.header.setTextBackground(new Color[] { top, bot }, new int[] { 100 },
				true);
		this.header.putColor(IFormColors.H_BOTTOM_KEYLINE1, this.formColors
				.getColor(IFormColors.H_BOTTOM_KEYLINE1));
		this.header.putColor(IFormColors.H_BOTTOM_KEYLINE2, this.formColors
				.getColor(IFormColors.H_BOTTOM_KEYLINE2));
		this.header.putColor(IFormColors.H_HOVER_LIGHT, this.formColors
				.getColor(IFormColors.H_HOVER_LIGHT));
		this.header.putColor(IFormColors.H_HOVER_FULL, this.formColors
				.getColor(IFormColors.H_HOVER_FULL));
		this.header.putColor(IFormColors.TB_TOGGLE, this.formColors
				.getColor(IFormColors.TB_TOGGLE));
		this.header.putColor(IFormColors.TB_TOGGLE_HOVER, this.formColors
				.getColor(IFormColors.TB_TOGGLE_HOVER));
		this.header.setSeparatorVisible(true);
		this.header.setFont(JFaceResources.getHeaderFont());
		this.header.setForeground(this.formColors.getColor(IFormColors.TITLE));
	}
	
    private void refreshPageHeaderText()
    {
        this.header.setText( LabelTransformer.transform( this.part.getPageHeaderText(), CapitalizationType.TITLE_STYLE, false ) );
        this.header.layout();
    }
	
	private void refreshPageHeaderImage()
	{
        final Image oldImage = this.header.getImage();
        
        if( oldImage != null )
        {
            oldImage.dispose();
        }
        
        final ImageData newImageData = this.part.getPageHeaderImage();
        
        if( newImageData == null )
        {
            this.header.setImage( null );
        }
        else
        {
            this.header.setImage( toImageDescriptor( newImageData ).createImage() );
        }
	}
    
	private void refreshZoomLevel()
	{
	    final int zoomLevel = this.part.getZoomLevel();
	    final double zoom = (double) zoomLevel / 100;
	    
	    getZoomManager().setZoom( zoom );
	}
	
	private ZoomManager getZoomManager()
    {
        return (ZoomManager) getGraphicalViewer().getProperty( ZoomManager.class.toString() );
    }

    private SapphireDiagramOutline getDiagramOutline()
	{
		if (this.diagramOutline == null && getGraphicalViewer() != null)
		{
			RootEditPart rootEditPart = getGraphicalViewer().getRootEditPart();
			if (rootEditPart instanceof ScalableFreeformRootEditPart)
			{
				this.diagramOutline = new SapphireDiagramOutline((ScalableFreeformRootEditPart)rootEditPart);
			}
		}
		return this.diagramOutline;			
	}
	
	// -----------------------------------------------------------------------------------------------
	// Inner classes
	//------------------------------------------------------------------------------------------------
	
	private class FormLayout extends Layout implements ILayoutExtension {
		public int computeMinimumWidth(Composite composite, boolean flushCache) {
			return computeSize(composite, 5, SWT.DEFAULT, flushCache).x;
		}

		public int computeMaximumWidth(Composite composite, boolean flushCache) {
			return computeSize(composite, SWT.DEFAULT, SWT.DEFAULT, flushCache).x;
		}

		public org.eclipse.swt.graphics.Point computeSize(Composite composite, int wHint, int hHint,
				boolean flushCache) {
			if (flushCache) {
				bodyCache.flush();
				headCache.flush();
			}
			bodyCache.setControl(body);
			headCache.setControl(header);

			int width = 0;
			int height = 0;

			org.eclipse.swt.graphics.Point hsize = headCache.computeSize(FormUtil.getWidthHint(wHint,
					header), SWT.DEFAULT);
			width = Math.max(hsize.x, width);
			height = hsize.y;
			
			boolean ignoreBody = false;
			
			org.eclipse.swt.graphics.Point bsize;
			if (ignoreBody)
				bsize = new org.eclipse.swt.graphics.Point(0,0);
			else
				bsize = bodyCache.computeSize(FormUtil.getWidthHint(wHint,
					body), SWT.DEFAULT);
			width = Math.max(bsize.x, width);
			height += bsize.y;
			return new org.eclipse.swt.graphics.Point(width, height);
		}

		protected void layout(Composite composite, boolean flushCache) {
			if (flushCache) {
				bodyCache.flush();
				headCache.flush();
			}
			bodyCache.setControl(body);
			headCache.setControl(header);
			Rectangle carea = composite.getClientArea();

			org.eclipse.swt.graphics.Point hsize = headCache.computeSize(carea.width, SWT.DEFAULT);
			headCache.setBounds(0, 0, carea.width, hsize.y);
			bodyCache
					.setBounds(0, hsize.y, carea.width, carea.height - hsize.y);
		}
	}

}
