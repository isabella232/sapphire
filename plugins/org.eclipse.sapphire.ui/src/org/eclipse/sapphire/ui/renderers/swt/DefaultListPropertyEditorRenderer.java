/******************************************************************************
 * Copyright (c) 2013 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [358295] Need access to selection in list property editor
 ******************************************************************************/

package org.eclipse.sapphire.ui.renderers.swt;

import static org.eclipse.sapphire.ui.PropertyEditorPart.RELATED_CONTROLS;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_ADD;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_ASSIST;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_DELETE;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_JUMP;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_MOVE_DOWN;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_MOVE_UP;
import static org.eclipse.sapphire.ui.SapphireActionSystem.createFilterByActionId;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdvfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glspacing;
import static org.eclipse.sapphire.ui.swt.renderer.SwtUtil.suppressDashedTableEntryBorder;
import static org.eclipse.sapphire.ui.util.MiscUtil.findSelectionPostDelete;
import static org.eclipse.sapphire.util.CollectionsUtil.equalsBasedOnEntryIdentity;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.EditFailedException;
import org.eclipse.sapphire.modeling.ElementEvent;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImageData;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.PropertyContentEvent;
import org.eclipse.sapphire.modeling.PropertyEvent;
import org.eclipse.sapphire.modeling.Status.Severity;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.FixedOrderList;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.localization.LabelTransformer;
import org.eclipse.sapphire.modeling.util.MiscUtil;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.ImageService;
import org.eclipse.sapphire.services.PossibleTypesService;
import org.eclipse.sapphire.ui.ListSelectionService;
import org.eclipse.sapphire.ui.ListSelectionService.ListSelectionChangedEvent;
import org.eclipse.sapphire.ui.PropertyEditorPart;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionHandler.PostExecuteEvent;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.sapphire.ui.SapphirePropertyEditorActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.def.PropertyEditorDef;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.sapphire.ui.swt.ModelElementsTransfer;
import org.eclipse.sapphire.ui.swt.internal.PopUpListFieldCellEditorPresentation;
import org.eclipse.sapphire.ui.swt.internal.PopUpListFieldStyle;
import org.eclipse.sapphire.ui.swt.renderer.HyperlinkTable;
import org.eclipse.sapphire.ui.swt.renderer.SapphireActionPresentationManager;
import org.eclipse.sapphire.ui.swt.renderer.SapphireMenuActionPresentation;
import org.eclipse.sapphire.ui.swt.renderer.SapphireTextCellEditor;
import org.eclipse.sapphire.ui.swt.renderer.SapphireToolBarActionPresentation;
import org.eclipse.sapphire.util.MutableReference;
import org.eclipse.sapphire.util.ReadOnlyListFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public class DefaultListPropertyEditorRenderer extends ListPropertyEditorRenderer
{
    private boolean exposeAddAction;
    private boolean exposeDeleteAction;
    private Map<IModelElement,TableRow> rows;
    private Table table;
    private TableViewer tableViewer;
    private SelectionProvider selectionProvider;
    private List<ColumnHandler> columnHandlers;
    private Runnable refreshOperation;
    
    public DefaultListPropertyEditorRenderer( final SapphireRenderingContext context,
                                              final PropertyEditorPart part )
    {
        super( context, part );
        
        this.exposeAddAction = true;
        this.exposeDeleteAction = true;
    }

    @Override
    protected void createContents( final Composite parent )
    {
        createContents( parent, false, false );
    }
    
    protected Control createContents( final Composite parent,
                                      final boolean suppressLabel,
                                      final boolean ignoreLeftMarginHint )
    {
        final PropertyEditorPart part = getPart();
        final IModelElement element = part.getLocalModelElement();
        final ListProperty property = (ListProperty) part.getProperty();
        final boolean isReadOnly = part.isReadOnly();
        final boolean showHeader = part.getRenderingHint( PropertyEditorDef.HINT_SHOW_HEADER, true );
        
        final SapphireActionGroup actions = getActions();
        final SapphireActionPresentationManager actionPresentationManager = getActionPresentationManager();
        
        final SapphireToolBarActionPresentation toolBarActionsPresentation = new SapphireToolBarActionPresentation( actionPresentationManager );
        toolBarActionsPresentation.addFilter( createFilterByActionId( ACTION_ASSIST ) );
        toolBarActionsPresentation.addFilter( createFilterByActionId( ACTION_JUMP ) );
        
        final SapphireMenuActionPresentation menuActionsPresentation = new SapphireMenuActionPresentation( actionPresentationManager );
        menuActionsPresentation.addFilter( createFilterByActionId( ACTION_ASSIST ) );
        menuActionsPresentation.addFilter( createFilterByActionId( ACTION_JUMP ) );
        
        final Composite mainComposite = createMainComposite
        (
            parent,
            new CreateMainCompositeDelegate( part )
            {
                @Override
                public boolean getShowLabel()
                {
                    return ( suppressLabel ? false : super.getShowLabel() );
                }

                @Override
                public int getLeftMargin()
                {
                    return ( ignoreLeftMarginHint ? 0 : super.getLeftMargin() );
                }
            }
        );
        
        mainComposite.setLayout( glayout( ( isReadOnly ? 1 : 2 ), 0, 0, 0, 0 ) );
        
        final Composite tableComposite;
        
        if( this.decorator == null )
        {
            tableComposite = new Composite( mainComposite, SWT.NULL );
            tableComposite.setLayoutData( gdfill() );
            tableComposite.setLayout( glspacing( glayout( 2, 0, 0 ), 2 ) );
            this.context.adapt( tableComposite );
            
            this.decorator = createDecorator( tableComposite );
            this.decorator.control().setLayoutData( gdvalign( gd(), SWT.TOP ) );

            this.decorator.addEditorControl( tableComposite );
        }
        else
        {
            tableComposite = mainComposite;
        }
        
        this.decorator.addEditorControl( mainComposite );
        
        final List<ValueProperty> columnProperties = new ArrayList<ValueProperty>();
        
        for( ModelProperty childProperty : part.getChildProperties() )
        {
            if( childProperty instanceof ValueProperty )
            {
                columnProperties.add( (ValueProperty) childProperty );
            }
        }
        
        // Setting the whint in the following code is a hacky workaround for the problem
        // tracked by the following JFace bug:
        //
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=215997
        //
        
        final Composite tableParentComposite = new Composite( tableComposite, SWT.NULL );
        tableParentComposite.setLayoutData( gdwhint( gdfill(), 1 ) );
        final TableColumnLayout tableColumnLayout = new TableColumnLayout();
        tableParentComposite.setLayout( tableColumnLayout );
        
        this.tableViewer = new TableViewer( tableParentComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI );
        this.table = this.tableViewer.getTable();
        this.context.adapt( this.table );
        this.decorator.addEditorControl( this.table );
        
        final List<Control> relatedControls = new ArrayList<Control>();
        this.table.setData( RELATED_CONTROLS, relatedControls );
        
        this.table.addListener
        (
            SWT.MeasureItem, 
            new org.eclipse.swt.widgets.Listener() 
            {
                public void handleEvent( final org.eclipse.swt.widgets.Event event ) 
                {
                    // The rows should be 18 pixels at minimum to allow sufficient 
                    // room for the cell editors.
                    
                    event.height = Math.max( event.height, 18 );
                }
            }
        );
        
        this.columnHandlers = new ArrayList<ColumnHandler>();
        
        final ColumnViewerEditorActivationStrategy activationStrategy = new ColumnViewerEditorActivationStrategy( this.tableViewer ) 
        {
            protected boolean isEditorActivationEvent( final ColumnViewerEditorActivationEvent event ) 
            {
                final int columnIndex = ( (ViewerCell) event.getSource() ).getColumnIndex();
                final ColumnHandler columnHandler = getColumnHandler( columnIndex );
                return columnHandler.isEditorActivationEvent( event );
            }
        };
        
        TableViewerEditor.create( this.tableViewer, null, activationStrategy, ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_CYCLE_IN_ROW );

        this.table.setHeaderVisible( showHeader );
        
        this.selectionProvider = new SelectionProvider( this.tableViewer );
        
        this.table.addFocusListener
        (
            new FocusAdapter()
            {
                @Override
                public void focusGained( final FocusEvent event )
                {
                    handleTableFocusGainedEvent();
                }
            }
        );
        
        this.refreshOperation = new Runnable()
        {
            boolean running = false;
            
            public void run()
            {
                if( DefaultListPropertyEditorRenderer.this.table.isDisposed() )
                {
                    return;
                }
                
                if( this.running == true )
                {
                    return;
                }
                
                this.running = true;
                
                try
                {
                    DefaultListPropertyEditorRenderer.this.tableViewer.refresh();
                    DefaultListPropertyEditorRenderer.this.table.notifyListeners( SWT.Selection, null );
                    tableParentComposite.layout();
                }
                finally
                {
                    this.running = false;
                }
            }
        };
        
        final Listener listener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                DefaultListPropertyEditorRenderer.this.refreshOperation.run();
            }
        };
        
        element.attach( listener, property.getName() );
        
        addOnDisposeOperation
        (
            new Runnable()
            {
                public void run()
                {
                    element.detach( listener, property.getName() );
                }
            }
        );
        
        boolean showImages = true;
        
        final String columnWidthsHint = part.getRenderingHint( PropertyEditorDef.HINT_COLUMN_WIDTHS, "" );
        final StringTokenizer columnWidthsHintTokenizer = new StringTokenizer( columnWidthsHint, "," );
        
        for( final ValueProperty memberProperty : columnProperties )
        {
            final TableViewerColumn col2 = new TableViewerColumn( this.tableViewer, SWT.NONE );
            final PropertyEditorDef childPropertyEditorDef = part.definition().getChildPropertyEditor( memberProperty );
            
            if( childPropertyEditorDef == null )
            {
                final String label = memberProperty.getLabel( false, CapitalizationType.TITLE_STYLE, false );
                col2.getColumn().setText( label );
            }
            else
            {
                final MutableReference<FunctionResult> labelFunctionResultRef = new MutableReference<FunctionResult>();
                
                final Runnable updateLabelOp = new Runnable()
                {
                    public void run()
                    {
                        String label = (String) labelFunctionResultRef.get().value();
                        label = LabelTransformer.transform( label, CapitalizationType.TITLE_STYLE, false );
                        col2.getColumn().setText( label );
                    }
                };
                
                final FunctionResult labelFunctionResult = part.initExpression
                (
                    element,
                    childPropertyEditorDef.getLabel().getContent(), 
                    String.class,
                    Literal.create( memberProperty.getLabel( false, CapitalizationType.NO_CAPS, true ) ),
                    updateLabelOp
                );
                
                labelFunctionResultRef.set( labelFunctionResult );
                
                updateLabelOp.run();
                
                addOnDisposeOperation
                (
                    new Runnable()
                    {
                        public void run()
                        {
                            labelFunctionResult.dispose();
                        }
                    }
                );
            }
            
            ColumnWeightData columnWeightData = null;
            
            if( columnWidthsHintTokenizer.hasMoreTokens() )
            {
                final String columnWidthHint = columnWidthsHintTokenizer.nextToken();
                final String[] columnWidthHintSplit = columnWidthHint.split( ":" );
                
                if( columnWidthHintSplit.length == 1 || columnWidthHintSplit.length == 2 )
                {
                    try
                    {
                        final int minColumnWidth = Integer.parseInt( columnWidthHintSplit[ 0 ].trim() );
                        final int columnWeight;
                        
                        if( columnWidthHintSplit.length == 2 )
                        {
                            columnWeight = Integer.parseInt( columnWidthHintSplit[ 1 ].trim() );
                        }
                        else
                        {
                            columnWeight = 0;
                        }
                        
                        columnWeightData = new ColumnWeightData( columnWeight, minColumnWidth, true );
                    }
                    catch( NumberFormatException e ) {}
                }
            }
            
            if( columnWeightData == null )
            {
                columnWeightData = new ColumnWeightData( 1, 100, true );
            }
            
            tableColumnLayout.setColumnData( col2.getColumn(), columnWeightData );
            
            final ColumnHandler columnHandler = createColumnHandler( this.columnHandlers, memberProperty, showImages, childPropertyEditorDef );
            
            showImages = false; // Only the first column should ever show the image.
            
            col2.setLabelProvider( columnHandler.getLabelProvider() );
            col2.setEditingSupport( columnHandler.getEditingSupport() );
            
            final TableColumn tableColumn = col2.getColumn();
            
            tableColumn.addSelectionListener
            (
                new SelectionAdapter()
                {
                    @Override
                    public void widgetSelected( final SelectionEvent event )
                    {
                        final TableColumn currentSortColumn = DefaultListPropertyEditorRenderer.this.table.getSortColumn();
                        
                        if( currentSortColumn != tableColumn )
                        {
                            DefaultListPropertyEditorRenderer.this.table.setSortColumn( tableColumn );
                            DefaultListPropertyEditorRenderer.this.table.setSortDirection( SWT.DOWN );
                            DefaultListPropertyEditorRenderer.this.tableViewer.setComparator( new TableSorter( columnHandler, SWT.DOWN ) );
                        }
                        else
                        {
                            final int currentSortDirection = DefaultListPropertyEditorRenderer.this.table.getSortDirection();
                            
                            if( currentSortDirection == SWT.DOWN )
                            {
                                DefaultListPropertyEditorRenderer.this.table.setSortDirection( SWT.UP );
                                DefaultListPropertyEditorRenderer.this.tableViewer.setComparator( new TableSorter( columnHandler, SWT.UP ) );
                            }
                            else
                            {
                                DefaultListPropertyEditorRenderer.this.table.setSortColumn( null );
                                DefaultListPropertyEditorRenderer.this.tableViewer.setComparator( null );
                            }
                        }
                        
                        for( SapphireAction action : actions.getActions() )
                        {
                            for( SapphireActionHandler handler : action.getActiveHandlers() )
                            {
                                if( handler instanceof SapphirePropertyEditorActionHandler )
                                {
                                    ( (SapphirePropertyEditorActionHandler) handler ).refreshEnablementState();
                                }
                            }
                        }
                    }
                }
            );
        }
        
        final IStructuredContentProvider contentProvider = new IStructuredContentProvider()
        {
            public Object[] getElements( final Object inputElement )
            {
                final ModelElementList<IModelElement> list = getList();
                final Map<IModelElement,TableRow> rows = new LinkedHashMap<IModelElement,TableRow>();
                
                for( IModelElement element : list )
                {
                    TableRow row = null;
                    
                    if( DefaultListPropertyEditorRenderer.this.rows != null )
                    {
                        row = DefaultListPropertyEditorRenderer.this.rows.remove( element );
                    }
                    
                    if( row == null )
                    {
                        row = new TableRow( element );
                    }
                    
                    rows.put( element, row );
                }
                
                if( DefaultListPropertyEditorRenderer.this.rows != null )
                {
                    for( TableRow row : DefaultListPropertyEditorRenderer.this.rows.values() )
                    {
                        row.dispose();
                    }
                }
                
                DefaultListPropertyEditorRenderer.this.rows = rows;

                return rows.values().toArray();
            }

            public void inputChanged( final Viewer viewer,
                                      final Object oldInput,
                                      final Object newInput )
            {
            }

            public void dispose()
            {
                for( TableRow row : DefaultListPropertyEditorRenderer.this.rows.values() )
                {
                    row.dispose();
                }
            }
        };
        
        this.tableViewer.setContentProvider( contentProvider );
        this.tableViewer.setInput( contentProvider );
        
        this.table.addTraverseListener
        (
            new TraverseListener()
            {
                public void keyTraversed( final TraverseEvent event )
                {
                    handleTableTraverseEvent( event );
                }
            }
        );
        
        if( ! isReadOnly )
        {
            if( this.exposeAddAction )
            {
                final SapphireAction addAction = actions.getAction( ACTION_ADD );
                final List<SapphireActionHandler> addActionHandlers = new ArrayList<SapphireActionHandler>();
                
                final org.eclipse.sapphire.Listener addActionHandlerListener = new org.eclipse.sapphire.Listener()
                {
                    @Override
                    public void handle( final org.eclipse.sapphire.Event event )
                    {
                        if( event instanceof PostExecuteEvent )
                        {
                            if( DefaultListPropertyEditorRenderer.this.table.isDisposed() )
                            {
                                return;
                            }
                            
                            final IModelElement newListElement = (IModelElement) ( (PostExecuteEvent) event ).getResult();
        
                            if( newListElement != null )
                            {
                                DefaultListPropertyEditorRenderer.this.refreshOperation.run();
                                
                                final TableRow row = DefaultListPropertyEditorRenderer.this.rows.get( newListElement );
                                
                                DefaultListPropertyEditorRenderer.this.tableViewer.setSelection( new StructuredSelection( row ), true );
                                DefaultListPropertyEditorRenderer.this.tableViewer.editElement( row, 0 );
                                DefaultListPropertyEditorRenderer.this.table.notifyListeners( SWT.Selection, null );
                            }
                        }
                    }
                };
                
                final PossibleTypesService possibleTypesService = element.service( property, PossibleTypesService.class );

                final Runnable refreshAddActionHandlersOp = new Runnable()
                {
                    public void run()
                    {
                        addAction.removeHandlers( addActionHandlers );
                        
                        for( SapphireActionHandler addActionHandler : addActionHandlers )
                        {
                            addActionHandler.dispose();
                        }
                        
                        for( ModelElementType memberType : possibleTypesService.types() )
                        {
                            final SapphireActionHandler addActionHandler = new AddActionHandler( memberType );
                            addActionHandler.init( addAction, null );
                            addActionHandler.attach( addActionHandlerListener );
                            addActionHandlers.add( addActionHandler );
                            addAction.addHandler( addActionHandler );
                        }
                    }
                };
                
                refreshAddActionHandlersOp.run();
                
                final org.eclipse.sapphire.Listener possibleTypesServiceListener = new org.eclipse.sapphire.Listener()
                {
                    @Override
                    public void handle( final org.eclipse.sapphire.Event event )
                    {
                        refreshAddActionHandlersOp.run();
                    }
                };
                
                possibleTypesService.attach( possibleTypesServiceListener );
                
                addOnDisposeOperation
                (
                    new Runnable()
                    {
                        public void run()
                        {
                            addAction.removeHandlers( addActionHandlers );
                            
                            for( SapphireActionHandler addActionHandler : addActionHandlers )
                            {
                                addActionHandler.dispose();
                            }
                            
                            possibleTypesService.detach( possibleTypesServiceListener );
                        }
                    }
                );
            }
            
            if( this.exposeDeleteAction )
            {
                final SapphireAction deleteAction = actions.getAction( ACTION_DELETE );
                final SapphireActionHandler deleteActionHandler = new DeleteActionHandler();
                deleteActionHandler.init( deleteAction, null );
                deleteAction.addHandler( deleteActionHandler );
                
                addOnDisposeOperation
                (
                    new Runnable()
                    {
                        public void run()
                        {
                            deleteAction.removeHandler( deleteActionHandler );
                        }
                    }
                );
            }

            if( ! property.hasAnnotation( FixedOrderList.class ) )
            {
                final SapphireAction moveUpAction = actions.getAction( ACTION_MOVE_UP );
                final SapphireActionHandler moveUpActionHandler = new MoveUpActionHandler();
                moveUpActionHandler.init( moveUpAction, null );
                moveUpAction.addHandler( moveUpActionHandler );
                
                addOnDisposeOperation
                (
                    new Runnable()
                    {
                        public void run()
                        {
                            moveUpAction.removeHandler( moveUpActionHandler );
                        }
                    }
                );
                
                final SapphireAction moveDownAction = actions.getAction( ACTION_MOVE_DOWN );
                final SapphireActionHandler moveDownActionHandler = new MoveDownActionHandler();
                moveDownActionHandler.init( moveDownAction, null );
                moveDownAction.addHandler( moveDownActionHandler );
                
                addOnDisposeOperation
                (
                    new Runnable()
                    {
                        public void run()
                        {
                            moveDownAction.removeHandler( moveDownActionHandler );
                        }
                    }
                );

                final org.eclipse.sapphire.Listener moveActionHandlerListener = new org.eclipse.sapphire.Listener()
                {
                    @Override
                    public void handle( final org.eclipse.sapphire.Event event )
                    {
                        if( event instanceof PostExecuteEvent )
                        {
                            DefaultListPropertyEditorRenderer.this.refreshOperation.run();
                            
                            // This is a workaround for a weird problem on SWT on Windows. If modifier keys are pressed
                            // when the list is re-ordered (as in when issuing move up or move down command from the
                            // keyboard), the focused row can detached from selected row.
                            
                            final IModelElement element = getSelectedElement();
                            final TableItem[] items = DefaultListPropertyEditorRenderer.this.table.getItems();
                            
                            for( int i = 0; i < items.length; i++ )
                            {
                                if( items[ i ].getData() == element )
                                {
                                    DefaultListPropertyEditorRenderer.this.table.setSelection( i );
                                    break;
                                }
                            }
                        }
                    }
                };
                
                moveUpAction.attach( moveActionHandlerListener );
                moveDownAction.attach( moveActionHandlerListener );
                
                final ModelElementsTransfer transfer = new ModelElementsTransfer( getModelElement().type().getModelElementClass().getClassLoader() );
                final Transfer[] transfers = new Transfer[] { transfer };
                
                final DragSource dragSource = new DragSource( this.table, DND.DROP_MOVE );
                dragSource.setTransfer( transfers );

                final List<IModelElement> dragElements = new ArrayList<IModelElement>();
                
                dragSource.addDragListener
                (
                    new DragSourceListener()
                    {
                        public void dragStart( final DragSourceEvent event )
                        {
                            if( DefaultListPropertyEditorRenderer.this.tableViewer.getComparator() == null )
                            {
                                dragElements.addAll( getSelectedElements() );
                                event.doit = true;
                            }
                            else
                            {
                                event.doit = false;
                            }
                        }
                        
                        public void dragSetData( final DragSourceEvent event )
                        {
                            event.data = dragElements;
                        }
                        
                        @SuppressWarnings( "unchecked" )
                        
                        public void dragFinished( final DragSourceEvent event )
                        {
                            if( event.detail == DND.DROP_MOVE )
                            {
                                // When drop target is the same editor as drag source, the drop handler takes care of removing
                                // elements from their original location. The following block of code accounts for the case when 
                                // dropping into another editor.
                                
                                boolean droppedIntoAnotherEditor = false;
                                
                                for( IModelElement dragElement : dragElements )
                                {
                                    if( ! dragElement.disposed() )
                                    {
                                        droppedIntoAnotherEditor = true;
                                        break;
                                    }
                                }
                                
                                if( droppedIntoAnotherEditor )
                                {
                                    try
                                    {
                                        final IModelElement selectionPostDelete = findSelectionPostDelete( getList(), dragElements );
                                        
                                        for( IModelElement dragElement : dragElements )
                                        {
                                            final ModelElementList<IModelElement> dragElementContainer = (ModelElementList<IModelElement>) dragElement.parent();
                                            dragElementContainer.remove( dragElement );
                                        }
                                        
                                        setSelectedElement( selectionPostDelete );
                                    }
                                    catch( Exception e )
                                    {
                                        // Log this exception unless the cause is EditFailedException. These exception
                                        // are the result of the user declining a particular action that is necessary
                                        // before the edit can happen (such as making a file writable).
                                        
                                        final EditFailedException editFailedException = EditFailedException.findAsCause( e );
                                        
                                        if( editFailedException == null )
                                        {
                                            SapphireUiFrameworkPlugin.log( e );
                                        }
                                    }
                                }
                            }
                            
                            dragElements.clear();
                        }
                    }
                );
                
                final DropTarget target = new DropTarget( this.table, DND.DROP_MOVE );
                target.setTransfer( transfers );
                
                target.addDropListener
                (
                    new DropTargetAdapter()
                    {
                        public void dragOver( final DropTargetEvent event )
                        {
                            if( event.item != null )
                            {
                                final TableItem dragOverItem = (TableItem) event.item;

                                final Point pt = dragOverItem.getDisplay().map( null, DefaultListPropertyEditorRenderer.this.table, event.x, event.y );
                                final Rectangle bounds = dragOverItem.getBounds();
                                
                                if( pt.y < bounds.y + bounds.height / 2 )
                                {
                                    event.feedback = DND.FEEDBACK_INSERT_BEFORE;
                                }
                                else
                                {
                                    event.feedback = DND.FEEDBACK_INSERT_AFTER;
                                }
                            }
                            
                            event.feedback |= DND.FEEDBACK_SCROLL;
                        }

                        @SuppressWarnings( "unchecked" )
                        
                        public void drop( final DropTargetEvent event ) 
                        {
                            if( event.data == null )
                            {
                                event.detail = DND.DROP_NONE;
                                return;
                            }
                            
                            final List<IModelElement> droppedElements = (List<IModelElement>) event.data;
                            final Set<ModelElementType> possibleTypesService = property.service( PossibleTypesService.class ).types();
                            
                            for( IModelElement droppedElement : droppedElements )
                            {
                                if( ! possibleTypesService.contains( droppedElement.type() ) )
                                {
                                    event.detail = DND.DROP_NONE;
                                    return;
                                }
                            }
                            
                            final ModelElementList<IModelElement> list = getList();
                            
                            int position;
                            
                            if( event.item == null )
                            {
                                position = list.size();
                            }
                            else
                            {
                                final TableItem dropTargetItem = (TableItem) event.item;
                                final TableRow dropTargetRow = (TableRow) dropTargetItem.getData();
                                final IModelElement dropTargetElement = dropTargetRow.element();
                                
                                final Point pt = DefaultListPropertyEditorRenderer.this.table.getDisplay().map( null, DefaultListPropertyEditorRenderer.this.table, event.x, event.y );
                                final Rectangle bounds = dropTargetItem.getBounds();
                                
                                position = list.indexOf( dropTargetElement );
                                
                                if( pt.y >= bounds.y + bounds.height / 2 ) 
                                {
                                    position++;
                                }
                            }
                            
                            try
                            {
                                for( IModelElement dragElement : dragElements )
                                {
                                    final ModelElementList<IModelElement> dragElementContainer = (ModelElementList<IModelElement>) dragElement.parent();
                                    
                                    if( dragElementContainer == list && dragElementContainer.indexOf( dragElement ) < position )
                                    {
                                        position--;
                                    }
                                    
                                    dragElementContainer.remove( dragElement );
                                }
            
                                final List<IModelElement> newSelection = new ArrayList<IModelElement>();
                                
                                for( IModelElement droppedElement : droppedElements )
                                {
                                    final IModelElement insertedElement = list.insert( droppedElement.type(), position );
                                    insertedElement.copy( droppedElement );
                                    
                                    newSelection.add( insertedElement );
                                    
                                    position++;
                                }
                                
                                DefaultListPropertyEditorRenderer.this.tableViewer.refresh();
                                setSelectedElements( newSelection );
                            }
                            catch( Exception e )
                            {
                                // Log this exception unless the cause is EditFailedException. These exception
                                // are the result of the user declining a particular action that is necessary
                                // before the edit can happen (such as making a file writable).
                                
                                final EditFailedException editFailedException = EditFailedException.findAsCause( e );
                                
                                if( editFailedException == null )
                                {
                                    SapphireUiFrameworkPlugin.log( e );
                                }
                                
                                event.detail = DND.DROP_NONE;
                            }
                        }
                    }
                );
            }
            
            final ToolBar toolbar = new ToolBar( mainComposite, SWT.FLAT | SWT.VERTICAL );
            toolbar.setLayoutData( gdvfill() );
            toolBarActionsPresentation.setToolBar( toolbar );
            toolBarActionsPresentation.render();
            addControl( toolbar );
            this.context.adapt( toolbar );
            this.decorator.addEditorControl( toolbar );
            
            final Menu menu = new Menu( this.table );
            this.table.setMenu( menu );
            menuActionsPresentation.setMenu( menu );
            menuActionsPresentation.render();
        }
        
        final HyperlinkTable hyperlinkTable = new HyperlinkTable( this.table, actions );
        
        hyperlinkTable.setController
        (
            new HyperlinkTable.Controller()
            {
                @Override
                public boolean isHyperlinkEnabled( final TableItem item,
                                                   final int column )
                {
                    final SapphireActionHandler jumpHandler = getJumpHandler( item, column );
                    
                    if( jumpHandler != null )
                    {
                        return jumpHandler.isEnabled();
                    }

                    return false;
                }

                @Override
                public void handleHyperlinkEvent( final TableItem item,
                                                  final int column )
                {
                    final SapphireActionHandler jumpHandler = getJumpHandler( item, column );
                    
                    if( jumpHandler != null )
                    {
                        jumpHandler.execute( DefaultListPropertyEditorRenderer.this.context );
                    }
                }
                
                private SapphireActionHandler getJumpHandler( final TableItem item,
                                                              final int column )
                {
                    final IModelElement element = ( (TableRow) item.getData() ).element();
                    final ValueProperty property = columnProperties.get( column );
                    final PropertyEditorPart propertyEditor = getPart().getChildPropertyEditor( element, property );
                    final SapphireActionGroup actions = propertyEditor.getActions();
                    return actions.getAction( ACTION_JUMP ).getFirstActiveHandler();
                }
            }
        );
        
        suppressDashedTableEntryBorder( this.table );
        
        addControl( this.table );
        
        final ListSelectionService selectionService = part.service( ListSelectionService.class );
        
        this.selectionProvider.addSelectionChangedListener
        (
            new ISelectionChangedListener()
            {
                public void selectionChanged( SelectionChangedEvent event )
                {
                    selectionService.select( getSelectedElements() );
                }
            }
        );
        
        setSelectedElements( selectionService.selection() );
        
        final org.eclipse.sapphire.Listener selectionServiceListener = new org.eclipse.sapphire.Listener()
        {
            @Override
            public void handle( final org.eclipse.sapphire.Event event )
            {
                setSelectedElements( ( (ListSelectionChangedEvent) event ).after() );
            }
        };

        selectionService.attach( selectionServiceListener );

        addOnDisposeOperation
        ( 
            new Runnable()
            {
                public void run()
                {
                    selectionService.detach( selectionServiceListener );
                }
            }
        );
        
        return this.table;
    }
    
    @Override
    protected boolean canScaleVertically()
    {
        return true;
    }
    
    public final PropertyEditorAssistDecorator getDecorator()
    {
        return this.decorator;
    }
    
    public final void setDecorator( final PropertyEditorAssistDecorator decorator )
    {
        this.decorator = decorator;
    }
    
    public final boolean isAddActionDesired()
    {
        return this.exposeAddAction;
    }
    
    public final void setAddActionDesired( final boolean exposeAddAction )
    {
        this.exposeAddAction = exposeAddAction;
    }
    
    public final boolean isDeleteActionDesired()
    {
        return this.exposeDeleteAction;
    }
    
    public final void setDeleteActionDesired( final boolean exposeDeleteAction )
    {
        this.exposeDeleteAction = exposeDeleteAction;
    }
    
    public final IModelElement getSelectedElement()
    {
        final IStructuredSelection sel = (IStructuredSelection) this.selectionProvider.getSelection();
        return (IModelElement) sel.getFirstElement();
    }
    
    public final List<IModelElement> getSelectedElements()
    {
        final IStructuredSelection sel = (IStructuredSelection) this.selectionProvider.getSelection();
        final ReadOnlyListFactory<IModelElement> elements = ReadOnlyListFactory.create();
        
        for( Iterator<?> itr = sel.iterator(); itr.hasNext(); )
        {
            elements.add( (IModelElement) itr.next() );
        }
        
        return elements.export();
    }
    
    public final void setSelectedElement( final IModelElement element )
    {
        setSelectedElements( element == null ? Collections.<IModelElement>emptyList() : Collections.singletonList( element ) );
    }
    
    public final void setSelectedElements( final List<IModelElement> elements )
    {
        if( ! equalsBasedOnEntryIdentity( getSelectedElements(), elements ) )
        {
            final ReadOnlyListFactory<TableRow> rows = ReadOnlyListFactory.create();
            
            for( IModelElement element : elements )
            {
                final TableRow row = this.rows.get( element );
                
                if( row != null )
                {
                    rows.add( row );
                }
            }
            
            this.tableViewer.setSelection( new StructuredSelection( rows.export() ) );
        }
    }
    
    private final List<TableRow> getSelectedRows()
    {
        final List<TableRow> rows = new ArrayList<TableRow>();
        
        for( Iterator<?> itr = this.selectionProvider.getSelectedRows().iterator(); itr.hasNext(); )
        {
            rows.add( (TableRow) itr.next() );
        }
        
        return rows;
    }
    
    public final void setFocusOnTable()
    {
        this.table.setFocus();
    }
    
    @Override
    protected void handleListElementChangedEvent( final Event event )
    {
        super.handleListElementChangedEvent( event );
        
        if( ! this.table.isDisposed() )
        {
            this.table.getDisplay().asyncExec
            (
                new Runnable()
                {
                    public void run()
                    {
                        update( event instanceof ElementEvent ? ( (ElementEvent) event ).element() : ( (PropertyEvent) event ).element() );
                    }
                }
            );
        }
    }
    
    protected void handleTableFocusGainedEvent()
    {
        if( this.table.isDisposed() )
        {
            return;
        }
        
        if( this.tableViewer.getSelection().isEmpty() && this.table.getItemCount() > 0 )
        {
            final TableItem firstTableItem = this.table.getItem( 0 );
            
            if( firstTableItem.isDisposed() )
            {
                return;
            }
            
            final Object firstItem = firstTableItem.getData();
            this.tableViewer.setSelection( new StructuredSelection( firstItem ) );
        }
    }
    
    private void handleTableTraverseEvent( final TraverseEvent event )
    {
        if( event.detail == SWT.TRAVERSE_RETURN )
        {
            event.doit = false;

            final IStructuredSelection selection = (IStructuredSelection) this.tableViewer.getSelection();
            
            if( selection.size() == 1 )
            {
                final TableRow row = (TableRow) selection.getFirstElement();
                int firstEditableColumn = -1;
                
                for( int i = 0, n = getColumnCount(); i < n; i++ )
                {
                    final ColumnHandler handler = getColumnHandler( i );
                    
                    if( handler.getEditingSupport().canEdit( row ) )
                    {
                        firstEditableColumn = i;
                        break;
                    }
                }
                
                if( firstEditableColumn != -1 )
                {
                    this.tableViewer.editElement( row, firstEditableColumn );
                }
            }
        }
    }

    @Override
    protected void handleFocusReceivedEvent()
    {
        this.table.setFocus();
    }
    
    private ColumnHandler createColumnHandler( final List<ColumnHandler> allColumnHandlers,
                                               final ValueProperty property,
                                               final boolean showImages,
                                               final PropertyEditorDef childPropertyEditorDef )
    {
        final ColumnHandler columnHandler;
        
        if( property.isOfType( Boolean.class ) )
        {
            columnHandler = new BooleanPropertyColumnHandler( this.context, this.tableViewer, this.selectionProvider, getPart(), 
                                                              allColumnHandlers, property, showImages );
        }
        else
        {
            PopUpListFieldStyle popUpListFieldPresentationStyle = null;
            
            if( property.isOfType( Enum.class ) )
            {
                popUpListFieldPresentationStyle = PopUpListFieldStyle.STRICT;
            }
            else if( childPropertyEditorDef != null )
            {
                final String style = childPropertyEditorDef.getStyle().getText();
                
                if( style != null )
                {
                    if( style.startsWith( "Sapphire.PropertyEditor.PopUpListField" ) )
                    {
                        if( style.equals( "Sapphire.PropertyEditor.PopUpListField" ) )
                        {
                            final PossibleValues possibleValuesAnnotation = property.getAnnotation( PossibleValues.class );
                            
                            if( possibleValuesAnnotation != null )
                            {
                                popUpListFieldPresentationStyle 
                                    = ( possibleValuesAnnotation.invalidValueSeverity() == Severity.ERROR 
                                        ? PopUpListFieldStyle.STRICT : PopUpListFieldStyle.EDITABLE );
                            }
                            else
                            {
                                popUpListFieldPresentationStyle = PopUpListFieldStyle.EDITABLE;
                            }
                        }
                        else if( style.equals( "Sapphire.PropertyEditor.PopUpListField.Editable" ) )
                        {
                            popUpListFieldPresentationStyle = PopUpListFieldStyle.EDITABLE;
                        }
                        else if( style.equals( "Sapphire.PropertyEditor.PopUpListField.Strict" ) )
                        {
                            popUpListFieldPresentationStyle = PopUpListFieldStyle.STRICT;
                        }
                    }
                }
            }
            
            if( popUpListFieldPresentationStyle != null )
            {
                columnHandler = new PopUpListFieldColumnPresentation( this.context, this.tableViewer, this.selectionProvider, getPart(), 
                                                                      allColumnHandlers, property, showImages, popUpListFieldPresentationStyle );
            }
            else
            {
                columnHandler = new ColumnHandler( this.context, this.tableViewer, this.selectionProvider, getPart(), 
                                                   allColumnHandlers, property, showImages );
            }
        }
        
        allColumnHandlers.add( columnHandler );
        
        return columnHandler;
    }
    
    private int getColumnCount()
    {
        return this.table.getColumnCount();
    }
    
    private ColumnHandler getColumnHandler( final int column )
    {
        return this.columnHandlers.get( column );
    }
    
    private void update( final IModelElement element )
    {
        if( element == null )
        {
            throw new IllegalArgumentException();
        }
        
        final TableRow row = this.rows.get( element );
        
        if( row != null )
        {
            update( row );
        }
    }

    private void update( final TableRow row )
    {
        if( row == null )
        {
            throw new IllegalArgumentException();
        }
        
        this.tableViewer.update( row, null );
    }
    
    public static final class Factory extends PropertyEditorRendererFactory
    {
        @Override
        public boolean isApplicableTo( final PropertyEditorPart propertyEditorDefinition )
        {
            return ( propertyEditorDefinition.getProperty() instanceof ListProperty );
        }
        
        @Override
        public PropertyEditorRenderer create( final SapphireRenderingContext context,
                                              final PropertyEditorPart part )
        {
            return new DefaultListPropertyEditorRenderer( context, part );
        }
    }

    private class DefaultColumnLabelProvider extends ColumnLabelProvider
    {
        private final ColumnHandler columnHandler;
        private final ValueLabelProvider valueLabelProvider;
        
        public DefaultColumnLabelProvider( final ColumnHandler columnHandler )
        {
            this.columnHandler = columnHandler;
            this.valueLabelProvider = new ValueLabelProvider( getPart(), columnHandler.getProperty() );
        }
    
        @Override
        public String getText( final Object obj )
        {
            final IModelElement element = ( (TableRow) obj ).element();
            final Value<?> value = this.columnHandler.getPropertyValue( element );
            String str = this.valueLabelProvider.getText( value.getText() );
            
            if( str == null )
            {
                if( this.columnHandler.isEmptyTextLabelDesired( element ) )
                {
                    str = Resources.emptyRowIndicator;
                }
                else
                {
                    str = MiscUtil.EMPTY_STRING;
                }
            }
            
            return str;
        }
        
        @Override
        public Image getImage( final Object obj )
        {
            if( this.columnHandler.isElementImageDesired() )
            {
                final TableRow row = (TableRow) obj;
                
                Image image = row.image();
                
                if( image == null )
                {
                    final IModelElement element = row.element();
                    final Value<?> value = this.columnHandler.getPropertyValue( element );
                    final ImageData valueImageData = this.valueLabelProvider.getImageData( value.getText() );
                    
                    if( valueImageData != null )
                    {
                        image = getPart().getImageCache().getImage( valueImageData, element.validation().severity() );
                    }
                }
                
                return image;
            }
            else
            {
                return null;
            }
        }
    
        @Override
        public Color getForeground( final Object obj )
        {
            final Value<?> value = ( (TableRow) obj ).element().read( this.columnHandler.getProperty() );
            
            if( value.getText( false ) == null )
            {
                return Display.getCurrent().getSystemColor( SWT.COLOR_DARK_GRAY );
            }
            else
            {
                return null;
            }
        }
    }
    
    private static abstract class AbstractColumnEditingSupport extends EditingSupport
    {
        protected final ColumnHandler columnHandler;
        
        public AbstractColumnEditingSupport( final ColumnHandler columnHandler )
        {
            super( columnHandler.getTableViewer() );
            
            this.columnHandler = columnHandler;
        }

        @Override
        public boolean canEdit( final Object obj )
        {
            final IModelElement element = ( (TableRow) obj ).element();
            final ValueProperty property = this.columnHandler.getProperty();
            
            boolean canEdit;
            
            if( element.enabled( property ) )
            {
                final PropertyEditorPart propertyEditor = this.columnHandler.getListPropertyEditor().getChildPropertyEditor( element, property );
                canEdit = ( ! propertyEditor.isReadOnly() );
            }
            else
            {
                canEdit = false;
            }
            
            return canEdit;
        }
        
        public abstract CellEditor getCellEditor( Object element );
        public abstract Object getValue( Object element );
        public abstract void setValue( Object element, Object value );
    }
    
    private class ColumnHandler
    {
        protected final SapphireRenderingContext context;
        protected final Table table;
        protected final TableViewer tableViewer;
        protected final SelectionProvider selectionProvider;
        protected final PropertyEditorPart listPropertyEditor;
        protected final List<ColumnHandler> allColumnHandlers;
        protected final ValueProperty property;
        protected final boolean showElementImage;
        protected final Collator collator;
        private CellLabelProvider labelProvider;
        private AbstractColumnEditingSupport editingSupport;
        
        public ColumnHandler( final SapphireRenderingContext context,
                              final TableViewer tableViewer,
                              final SelectionProvider selectionProvider,
                              final PropertyEditorPart listPropertyEditor,
                              final List<ColumnHandler> allColumnHandlers,
                              final ValueProperty property,
                              final boolean showElementImage )
        {
            this.context = context;
            this.table = tableViewer.getTable();
            this.tableViewer = tableViewer;
            this.selectionProvider = selectionProvider;
            this.listPropertyEditor = listPropertyEditor;
            this.allColumnHandlers = allColumnHandlers;
            this.property = property;
            this.showElementImage = showElementImage;
            this.collator = Collator.getInstance();
        }
        
        public final SapphireRenderingContext getContext()
        {
            return this.context;
        }
        
        public final Table getTable()
        {
            return this.table;
        }
        
        public final TableViewer getTableViewer()
        {
            return this.tableViewer;
        }
        
        public final SelectionProvider getSelectionProvider()
        {
            return this.selectionProvider;
        }
        
        public final PropertyEditorPart getListPropertyEditor()
        {
            return this.listPropertyEditor;
        }
        
        public final SapphireImageCache getImageCache()
        {
            return this.listPropertyEditor.getImageCache();
        }
        
        public final boolean isElementImageDesired()
        {
            return this.showElementImage;
        }
        
        public final ValueProperty getProperty()
        {
            return this.property;
        }
        
        public final Value<?> getPropertyValue( final IModelElement element )
        {
            return element.read( this.property );
        }
        
        public final void setPropertyValue( final IModelElement element,
                                            final String value )
        {
            element.write( this.property, value );
        }
        
        public final CellLabelProvider getLabelProvider()
        {
            if( this.labelProvider == null )
            {
                this.labelProvider = createLabelProvider();
            }
            
            return this.labelProvider;
        }
        
        protected CellLabelProvider createLabelProvider()
        {
            return new DefaultColumnLabelProvider( this );
        }
        
        public final AbstractColumnEditingSupport getEditingSupport()
        {
            if( this.editingSupport == null && ! this.property.isReadOnly() )
            {
                this.editingSupport = createEditingSupport();
            }
            
            return this.editingSupport;
        }
        
        protected AbstractColumnEditingSupport createEditingSupport()
        {
            return new AbstractColumnEditingSupport( this )
            {
                private SapphireTextCellEditor cellEditor;
    
                @Override
                public CellEditor getCellEditor( final Object obj )
                {
                    if( this.cellEditor != null )
                    {
                        this.cellEditor.dispose();
                    }
                    
                    final IModelElement element = ( (TableRow) obj ).element();
                    final ValueProperty property = ColumnHandler.this.property;
                    final PropertyEditorPart propertyEditor = getListPropertyEditor().getChildPropertyEditor( element, property );
                    final SapphireActionGroup actions = propertyEditor.getActions();

                    final int style = ( getTable().getLinesVisible() ? SWT.NONE : SWT.BORDER );
                    
                    this.cellEditor 
                        = new SapphireTextCellEditor( getContext(), getTableViewer(), getSelectionProvider(), 
                                                      element, getProperty(), actions, style );

                    if( isElementImageDesired() )
                    {
                        this.cellEditor.setHorizonalIndent( 3 );
                    }
                    
                    return this.cellEditor;
                }
    
                @Override
                public Object getValue( final Object obj )
                {
                    return getPropertyValue( ( (TableRow) obj ).element() );
                }
    
                @Override
                public void setValue( final Object obj,
                                      final Object value )
                {
                    setPropertyValue( ( (TableRow) obj ).element(), (String) value );
                }
            };
        }
        
        public boolean isEditorActivationEvent( final ColumnViewerEditorActivationEvent event ) 
        {
            return event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION ||
                   event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC ||
                   event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL;
        }
        
        public int comparePropertyValues( final IModelElement x,
                                          final IModelElement y )
        {
            final String a = getPropertyValue( x ).getText();
            final String b = getPropertyValue( y ).getText();
            
            final boolean aEmpty = ( a == null || a.trim().length() == 0 );
            final boolean bEmpty = ( b == null || b.trim().length() == 0 );
            
            if( aEmpty && bEmpty )
            {
                return 0;
            }
            else if( aEmpty )
            {
                return 1;
            }
            else if( bEmpty )
            {
                return -1;
            }
            else
            {
                return this.collator.compare( a, b );
            }
        }
        
        public boolean isEmptyTextLabelDesired( final IModelElement element )
        {
            if( this.allColumnHandlers.get( 0 ) == this )
            {
                for( ColumnHandler handler : this.allColumnHandlers )
                {
                    if( handler instanceof BooleanPropertyColumnHandler )
                    {
                        return false;
                    }
                }
                
                for( ColumnHandler handler : this.allColumnHandlers )
                {
                    if( handler.getPropertyValue( element ).getText() != null )
                    {
                        return false;
                    }
                }
                
                return true;
            }
            
            return false;
        }
    }
    
    private final class PopUpListFieldColumnPresentation extends ColumnHandler
    {
        private final PopUpListFieldStyle popUpListFieldStyle;
        
        public PopUpListFieldColumnPresentation( final SapphireRenderingContext context,
                                                 final TableViewer tableViewer,
                                                 final SelectionProvider selectionProvider,
                                                 final PropertyEditorPart listPropertyEditor,
                                                 final List<ColumnHandler> allColumnHandlers,
                                                 final ValueProperty property,
                                                 final boolean showElementImage,
                                                 final PopUpListFieldStyle popUpListFieldStyle )
        {
            super( context, tableViewer, selectionProvider, listPropertyEditor, allColumnHandlers, property, showElementImage );
            
            this.popUpListFieldStyle = popUpListFieldStyle;
        }
        
        @Override
        protected AbstractColumnEditingSupport createEditingSupport()
        {
            return new AbstractColumnEditingSupport( this )
            {
                private PopUpListFieldCellEditorPresentation cellEditor;

                @Override
                public CellEditor getCellEditor( final Object obj )
                {
                    if( this.cellEditor != null )
                    {
                        this.cellEditor.dispose();
                    }
                    
                    final int style = ( getTable().getLinesVisible() ? SWT.NONE : SWT.BORDER );
                    this.cellEditor = new PopUpListFieldCellEditorPresentation( getTableViewer(), getSelectionProvider(), ( (TableRow) obj ).element(), getProperty(), PopUpListFieldColumnPresentation.this.popUpListFieldStyle, style );
                    
                    return this.cellEditor;
                }

                @Override
                public Object getValue( final Object obj )
                {
                    return getPropertyValue( ( (TableRow) obj ).element() );
                }
    
                @Override
                public void setValue( final Object obj,
                                      final Object value )
                {
                    setPropertyValue( ( (TableRow) obj ).element(), (String) value );
                }
            };
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    private static final ImageDescriptor IMG_CHECKBOX_ON
        = SwtRendererUtil.createImageDescriptor( BooleanPropertyColumnHandler.class, "CheckBoxOn.gif" );
    
    private static final ImageDescriptor IMG_CHECKBOX_OFF
        = SwtRendererUtil.createImageDescriptor( BooleanPropertyColumnHandler.class, "CheckBoxOff.gif" );
    
    private final class BooleanPropertyColumnHandler extends ColumnHandler
    {
        private static final int CHECKBOX_IMAGE_WIDTH = 16;
        private static final int CHECKBOX_IMAGE_HEIGHT = 16;
        
        public BooleanPropertyColumnHandler( final SapphireRenderingContext context,
                                             final TableViewer tableViewer,
                                             final SelectionProvider selectionProvider,
                                             final PropertyEditorPart listPropertyEditor,
                                             final List<ColumnHandler> allColumnHandlers,
                                             final ValueProperty property,
                                             final boolean showElementImage )
        {
            super( context, tableViewer, selectionProvider, listPropertyEditor, allColumnHandlers, property, showElementImage );
        }

        @Override
        protected CellLabelProvider createLabelProvider()
        {
            return new OwnerDrawLabelProvider()
            {
                @Override
                protected void erase( final org.eclipse.swt.widgets.Event event,
                                      final Object element )
                {
                    // The default implementation causes non-native behavior on some platforms and
                    // is not desired. Nothing needs to happen on this event.
                }
    
                @Override
                protected void measure( final org.eclipse.swt.widgets.Event event,
                                        final Object element )
                {
                }
    
                @Override
                protected void paint( final org.eclipse.swt.widgets.Event event,
                                      final Object object )
                {
                    final TableItem item = (TableItem) event.item;
                    final IModelElement element = ( (TableRow) item.getData() ).element();
                    
                    if( element.enabled( getProperty() ) )
                    {
                        final boolean value = getPropertyValueAsBoolean( element );
                        
                        final Image image = getImageCache().getImage( value ? IMG_CHECKBOX_ON : IMG_CHECKBOX_OFF );
                        final Rectangle cellBounds = item.getBounds( event.index );
                        final Rectangle imageBounds = image.getBounds();
                        final int x = event.x + ( cellBounds.width - imageBounds.width ) / 2;
                        final int y = event.y + ( cellBounds.height - imageBounds.height ) / 2;
                        
                        event.gc.drawImage( image, x, y );
                    }
                }
            };
        }
        
        @Override
        protected AbstractColumnEditingSupport createEditingSupport()
        {
            return new AbstractColumnEditingSupport( this )
            {
                private CheckboxCellEditor cellEditor;

                @Override
                public CellEditor getCellEditor( final Object element )
                {
                    if( this.cellEditor == null )
                    {
                        this.cellEditor = new CheckboxCellEditor( getTable() );
                    }
                    
                    return this.cellEditor;
                }

                @Override
                @SuppressWarnings( "unchecked" )
                
                public Object getValue( final Object obj )
                {
                    final Value<Boolean> value = (Value<Boolean>) getPropertyValue( ( (TableRow) obj ).element() );
                    final Boolean val = value.getContent();
                    return ( val != null ? val : Boolean.FALSE );
                }

                @Override
                public void setValue( final Object obj,
                                      final Object value )
                {
                    final String str = String.valueOf( ( (Boolean) value ).booleanValue() );
                    setPropertyValue( ( (TableRow) obj ).element(), str );
                }
            };
        }
        
        @Override
        public boolean isEditorActivationEvent( final ColumnViewerEditorActivationEvent event ) 
        {
            if( event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION )
            {
                final Rectangle cellBounds = ( (ViewerCell) event.getSource() ).getBounds();
                
                final Rectangle checkBoxBounds 
                    = new Rectangle( cellBounds.x + ( cellBounds.width - CHECKBOX_IMAGE_WIDTH ) / 2, 
                                     cellBounds.y + ( cellBounds.height - CHECKBOX_IMAGE_HEIGHT ) / 2, 
                                     CHECKBOX_IMAGE_WIDTH, CHECKBOX_IMAGE_HEIGHT );
                
                final MouseEvent evt = (MouseEvent) event.sourceEvent;

                return checkBoxBounds.contains( evt.x, evt.y );
            }
            
            return false;
        }

        @Override
        public int comparePropertyValues( final IModelElement x,
                                          final IModelElement y )
        {
            final boolean a = getPropertyValueAsBoolean( x );
            final boolean b = getPropertyValueAsBoolean( y );
            
            if( a == b )
            {
                return 0;
            }
            else if( a )
            {
                return -1;
            }
            else
            {
                return 1;
            }
        }
        
        @SuppressWarnings( "unchecked" )
        
        private boolean getPropertyValueAsBoolean( final IModelElement element )
        {
            final Value<Boolean> value = (Value<Boolean>) getPropertyValue( element );
            final Boolean val = value.getContent();
            return ( val != null && val.booleanValue() == true );
        }
    }
    
    private static final class TableSorter extends ViewerComparator
    {
        private final ColumnHandler columnHandler;
        private final int direction;
        
        public TableSorter( final ColumnHandler columnHandler,
                            final int direction )
        {
            this.columnHandler = columnHandler;
            this.direction = direction;
        }
        
        @Override
        public int compare( final Viewer viewer,
                            final Object x,
                            final Object y )
        {
            int result = this.columnHandler.comparePropertyValues( ( (TableRow) x ).element(), ( (TableRow) y ).element() );
            
            if( this.direction == SWT.UP )
            {
                result = result * -1;
            }
            
            return result;
        }
    }
    
    private final class AddActionHandler extends SapphirePropertyEditorActionHandler
    {
        private final ModelElementType type;
        
        public AddActionHandler( final ModelElementType type )
        {
            this.type = type;
        }
        
        @Override
        public void init( final SapphireAction action,
                          final ActionHandlerDef def )
        {
            super.init( action, def );
            
            final ImageData typeSpecificAddImage = this.type.image();
            
            if( typeSpecificAddImage != null )
            {
                addImage( typeSpecificAddImage );
            }

            setLabel( this.type.getLabel( false, CapitalizationType.TITLE_STYLE, false ) );
        }

        @Override
        protected Object run( final SapphireRenderingContext context )
        {
            return getList().insert( this.type );
        }
    }
    
    private static abstract class SelectionBasedActionHandler extends SapphirePropertyEditorActionHandler
    {
        @Override
        public void init( final SapphireAction action, 
                          final ActionHandlerDef def )
        {
            super.init( action, def );
            
            final ListSelectionService selectionService = action.getPart().service( ListSelectionService.class );
            
            final Listener selectionListener = new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    refreshEnablementState();
                }
            };
            
            selectionService.attach( selectionListener );
            
            attach
            (
                new Listener()
                {
                    @Override
                    public void handle( final Event event )
                    {
                        if( event instanceof DisposeEvent )
                        {
                            selectionService.detach( selectionListener );
                        }
                    }
                }
            );
        }
    }
    
    private final class DeleteActionHandler extends SelectionBasedActionHandler
    {
        @Override
        protected final Object run( final SapphireRenderingContext context )
        {
            final List<TableRow> rowsToDelete = getSelectedRows();
            
            final TableRow selectionPostDelete 
                = findSelectionPostDelete( DefaultListPropertyEditorRenderer.this.rows.values(), rowsToDelete );
            
            final ModelElementList<IModelElement> list = getList();

            for( TableRow row : rowsToDelete )
            {
                list.remove( row.element() );
            }
            
            DefaultListPropertyEditorRenderer.this.refreshOperation.run();
            
            if( selectionPostDelete != null )
            {
                DefaultListPropertyEditorRenderer.this.tableViewer.setSelection( new StructuredSelection( selectionPostDelete ) );
            }
            
            return null;
        }
        
        @Override
        protected boolean computeEnablementState()
        {
            if( super.computeEnablementState() == true )
            {
                return ! getSelectedElements().isEmpty();
            }
            
            return false;
        }
    }

    private final class MoveUpActionHandler extends SelectionBasedActionHandler
    {
        @Override
        protected boolean computeEnablementState()
        {
            if( super.computeEnablementState() == true )
            {
                if( getSelectedElements().size() == 1 && DefaultListPropertyEditorRenderer.this.tableViewer.getComparator() == null )
                {
                    final IModelElement modelElement = getSelectedElement();
                    return ( getList().indexOf( modelElement ) > 0 );
                }
            }
            
            return false;
        }

        @Override
        protected final Object run( final SapphireRenderingContext context )
        {
            final IModelElement element = getSelectedElement();

            getList().moveUp( element );
            DefaultListPropertyEditorRenderer.this.tableViewer.reveal( element );
            
            return null;
        }
    }
    
    private final class MoveDownActionHandler extends SelectionBasedActionHandler
    {
        @Override
        protected boolean computeEnablementState()
        {
            if( super.computeEnablementState() == true )
            {
                if( getSelectedElements().size() == 1 && DefaultListPropertyEditorRenderer.this.tableViewer.getComparator() == null )
                {
                    final IModelElement modelElement = getSelectedElement();
                    final ModelElementList<?> list = getList();
                    return ( list.indexOf( modelElement ) < ( list.size() - 1 ) );
                }
            }
            
            return false;
        }
    
        @Override
        protected final Object run( final SapphireRenderingContext context )
        {
            final IModelElement element = getSelectedElement();

            getList().moveDown( element );
            DefaultListPropertyEditorRenderer.this.tableViewer.reveal( element );
            
            return null;
        }
    }
    
    public static final class SelectionProvider implements ISelectionProvider
    {
        private final TableViewer tableViewer;
        private final Set<ISelectionChangedListener> listeners;
        private ISelection fakeSelection;
        
        public SelectionProvider( final TableViewer tableViewer )
        {
            this.tableViewer = tableViewer;
            this.listeners = new CopyOnWriteArraySet<ISelectionChangedListener>();
            this.fakeSelection = null;
            
            this.tableViewer.addSelectionChangedListener
            (
                new ISelectionChangedListener()
                {
                    public void selectionChanged( final SelectionChangedEvent event )
                    {
                        notifySelectionChangedListeners();
                    }
                }
            );
        }
        
        public IStructuredSelection getSelectedRows()
        {
            return (IStructuredSelection) ( this.fakeSelection != null ? this.fakeSelection : this.tableViewer.getSelection() );
        }

        public ISelection getSelection()
        {
            final ReadOnlyListFactory<IModelElement> elements = ReadOnlyListFactory.create();
            
            for( Iterator<?> itr = getSelectedRows().iterator(); itr.hasNext(); )
            {
                final TableRow row = (TableRow) itr.next();
                elements.add( row.element() );
            }
            
            return new StructuredSelection( elements.export() );
        }
        
        public void setSelection( final ISelection selection )
        {
            throw new UnsupportedOperationException();
        }

        public void setFakeSelection( final ISelection selection )
        {
            this.fakeSelection = selection;
            notifySelectionChangedListeners();
        }
        
        public final void addSelectionChangedListener( final ISelectionChangedListener listener )
        {
            this.listeners.add( listener );
        }

        public final void removeSelectionChangedListener( final ISelectionChangedListener listener )
        {
            this.listeners.remove( listener );
        }
        
        private final void notifySelectionChangedListeners()
        {
            final SelectionChangedEvent event = new SelectionChangedEvent( this, getSelection() );
            
            for( ISelectionChangedListener listener : this.listeners )
            {
                listener.selectionChanged( event );
            }
        }
    }
    
    private final class TableRow
    {
        private final IModelElement element;
        private final ImageService imageService;
        private final org.eclipse.sapphire.Listener listener;
        
        public TableRow( final IModelElement element )
        {
            this.element = element;
            this.imageService = element.service( ImageService.class );
            
            if( this.imageService == null )
            {
                this.listener = null;
            }
            else
            {
                this.listener = new org.eclipse.sapphire.Listener()
                {
                    @Override
                    public void handle( final org.eclipse.sapphire.Event event )
                    {
                        update( TableRow.this );
                    }
                };
                
                this.imageService.attach( this.listener );
            }
        }
        
        public IModelElement element()
        {
            return this.element;
        }
        
        public Image image()
        {
            if( this.imageService == null )
            {
                return null;
            }
            else
            {
                return getPart().getImageCache().getImage( this.imageService.image(), this.element.validation().severity() );
            }
        }
        
        public void dispose()
        {
            if( this.imageService != null )
            {
                this.imageService.detach( this.listener );
            }
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String emptyRowIndicator;
        
        static
        {
            initializeMessages( DefaultListPropertyEditorRenderer.class.getName(), Resources.class );
        }
    }
    
}
