/******************************************************************************
 * Copyright (c) 2014 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [358295] Need access to selection in list property editor
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt;

import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_ADD;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_ASSIST;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_DELETE;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_JUMP;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_MOVE_DOWN;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_MOVE_UP;
import static org.eclipse.sapphire.ui.SapphireActionSystem.createFilterByActionId;
import static org.eclipse.sapphire.ui.forms.PropertyEditorPart.RELATED_CONTROLS;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdvfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glspacing;
import static org.eclipse.sapphire.ui.forms.swt.SwtUtil.suppressDashedTableEntryBorder;
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
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.ImageService;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.PropertyEvent;
import org.eclipse.sapphire.PropertyValidationEvent;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.EditFailedException;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Status.Severity;
import org.eclipse.sapphire.modeling.annotations.FixedOrderList;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.modeling.el.FunctionResult;
import org.eclipse.sapphire.modeling.el.Literal;
import org.eclipse.sapphire.modeling.localization.LabelTransformer;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.util.MiscUtil;
import org.eclipse.sapphire.services.PossibleTypesService;
import org.eclipse.sapphire.services.ValueImageService;
import org.eclipse.sapphire.services.ValueLabelService;
import org.eclipse.sapphire.ui.ListSelectionService;
import org.eclipse.sapphire.ui.ListSelectionService.ListSelectionChangedEvent;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionHandler.PostExecuteEvent;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.PropertyEditorActionHandler;
import org.eclipse.sapphire.ui.forms.PropertyEditorDef;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.swt.internal.ElementsTransfer;
import org.eclipse.sapphire.ui.forms.swt.internal.HyperlinkTable;
import org.eclipse.sapphire.ui.forms.swt.internal.PopUpListFieldCellEditorPresentation;
import org.eclipse.sapphire.ui.forms.swt.internal.PopUpListFieldStyle;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.sapphire.util.MutableReference;
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
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public class TablePropertyEditorPresentation extends ListPropertyEditorPresentation
{
    @Text( "<empty>" )
    private static LocalizableText emptyRowIndicator;
    
    static
    {
        LocalizableText.init( TablePropertyEditorPresentation.class );
    }

    private boolean exposeAddAction;
    private boolean exposeDeleteAction;
    private Map<Element,TableRow> rows;
    private Table table;
    private CustomTableViewer tableViewer;
    private SelectionProvider selectionProvider;
    private List<ColumnHandler> columnHandlers;
    private Runnable refreshOperation;
    
    public TablePropertyEditorPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
        
        this.exposeAddAction = true;
        this.exposeDeleteAction = true;
    }

    @Override
    protected void createContents( final Composite parent )
    {
        createContents( parent, false );
    }
    
    @SuppressWarnings( "unchecked" ) // TreeViewer is parameterized since Eclipse 4.4
    
    protected Control createContents( final Composite parent, final boolean embedded )
    {
        final PropertyEditorPart part = part();
        final Property property = part.property();
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
        
        addOnDisposeOperation
        (
            new Runnable()
            {
                public void run()
                {
                    actionPresentationManager.dispose();
                }
            }
        );
        
        final Composite mainComposite = createMainComposite
        (
            parent,
            new CreateMainCompositeDelegate( part )
            {
                @Override
                public boolean getShowLabel()
                {
                    return ( embedded ? false : super.getShowLabel() );
                }

                @Override
                public int getLeftMargin()
                {
                    return ( embedded ? 0 : super.getLeftMargin() );
                }

                @Override
                public boolean getSpanBothColumns()
                {
                    return ( embedded ? true : super.getSpanBothColumns() );
                }
            }
        );
        
        final Composite tableComposite;
        
        if( this.decorator == null )
        {
            tableComposite = new Composite( mainComposite, SWT.NULL );
            tableComposite.setLayoutData( gdfill() );
            tableComposite.setLayout( glspacing( glayout( 2, 0, 0 ), 2 ) );
            
            this.decorator = createDecorator( tableComposite );
            this.decorator.control().setLayoutData( gdvalign( gd(), SWT.TOP ) );

            this.decorator.addEditorControl( tableComposite );
        }
        else
        {
            tableComposite = mainComposite;
        }
        
        this.decorator.addEditorControl( mainComposite );
        
        // Setting the whint in the following code is a hacky workaround for the problem
        // tracked by the following JFace bug:
        //
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=215997
        //
        
        final Composite tableParentComposite = new Composite( tableComposite, SWT.NULL );
        tableParentComposite.setLayoutData( gdwhint( gdfill(), 1 ) );
        final TableColumnLayout tableColumnLayout = new TableColumnLayout();
        tableParentComposite.setLayout( tableColumnLayout );
        
        this.tableViewer = new CustomTableViewer( tableParentComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI );
        this.table = this.tableViewer.getTable();
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
                if( TablePropertyEditorPresentation.this.table.isDisposed() )
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
                    TablePropertyEditorPresentation.this.tableViewer.refresh();

                    if( TablePropertyEditorPresentation.this.table.isDisposed() )
                    {
                        return;
                    }
                    
                    TablePropertyEditorPresentation.this.table.notifyListeners( SWT.Selection, null );
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
                TablePropertyEditorPresentation.this.refreshOperation.run();
            }
        };
        
        property.attach( listener );
        
        addOnDisposeOperation
        (
            new Runnable()
            {
                public void run()
                {
                    property.detach( listener );
                }
            }
        );
        
        boolean showImages = true;
        
        final String columnWidthsHint = part.getRenderingHint( PropertyEditorDef.HINT_COLUMN_WIDTHS, "" );
        final StringTokenizer columnWidthsHintTokenizer = new StringTokenizer( columnWidthsHint, "," );
        
        for( final ModelPath childPropertyPath : part.getChildProperties() )
        {
            final PropertyDef childProperty = property.definition().getType().property( childPropertyPath );
            final PropertyEditorDef childPropertyEditorDef = part.definition().getChildPropertyEditor( childPropertyPath );
            final TableViewerColumn tableViewerColumn = new TableViewerColumn( this.tableViewer, SWT.NONE );
            
            if( childPropertyEditorDef == null )
            {
                final String label = childProperty.getLabel( false, CapitalizationType.TITLE_STYLE, false );
                tableViewerColumn.getColumn().setText( label );
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
                        tableViewerColumn.getColumn().setText( label );
                    }
                };
                
                final FunctionResult labelFunctionResult = part.initExpression
                (
                    childPropertyEditorDef.getLabel().content(), 
                    String.class,
                    Literal.create( childProperty.getLabel( false, CapitalizationType.NO_CAPS, true ) ),
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
            
            tableColumnLayout.setColumnData( tableViewerColumn.getColumn(), columnWeightData );
            
            final ColumnHandler columnHandler = createColumnHandler( this.columnHandlers, childPropertyPath, showImages, childPropertyEditorDef );
            
            showImages = false; // Only the first column should ever show the image.
            
            tableViewerColumn.setLabelProvider( columnHandler.getLabelProvider() );
            tableViewerColumn.setEditingSupport( columnHandler.getEditingSupport() );
            
            final TableColumn tableColumn = tableViewerColumn.getColumn();
            
            tableColumn.addSelectionListener
            (
                new SelectionAdapter()
                {
                    @Override
                    public void widgetSelected( final SelectionEvent event )
                    {
                        final TableColumn currentSortColumn = TablePropertyEditorPresentation.this.table.getSortColumn();
                        
                        if( currentSortColumn != tableColumn )
                        {
                            TablePropertyEditorPresentation.this.table.setSortColumn( tableColumn );
                            TablePropertyEditorPresentation.this.table.setSortDirection( SWT.DOWN );
                            TablePropertyEditorPresentation.this.tableViewer.setComparator( new TableSorter( columnHandler, SWT.DOWN ) );
                        }
                        else
                        {
                            final int currentSortDirection = TablePropertyEditorPresentation.this.table.getSortDirection();
                            
                            if( currentSortDirection == SWT.DOWN )
                            {
                                TablePropertyEditorPresentation.this.table.setSortDirection( SWT.UP );
                                TablePropertyEditorPresentation.this.tableViewer.setComparator( new TableSorter( columnHandler, SWT.UP ) );
                            }
                            else
                            {
                                TablePropertyEditorPresentation.this.table.setSortColumn( null );
                                TablePropertyEditorPresentation.this.tableViewer.setComparator( null );
                            }
                        }
                        
                        for( SapphireAction action : actions.getActions() )
                        {
                            for( SapphireActionHandler handler : action.getActiveHandlers() )
                            {
                                if( handler instanceof PropertyEditorActionHandler )
                                {
                                    ( (PropertyEditorActionHandler) handler ).refreshEnablementState();
                                }
                            }
                        }
                    }
                }
            );
        }
        
        @SuppressWarnings( "rawtypes" ) // IStructuredContentProvider is parameterized since Eclipse 4.4
        
        final IStructuredContentProvider contentProvider = new IStructuredContentProvider()
        {
            public Object[] getElements( final Object inputElement )
            {
                final ElementList<?> list = property();
                final Map<Element,TableRow> rows = new LinkedHashMap<Element,TableRow>();
                
                for( Element element : list )
                {
                    TableRow row = null;
                    
                    if( TablePropertyEditorPresentation.this.rows != null )
                    {
                        row = TablePropertyEditorPresentation.this.rows.remove( element );
                    }
                    
                    if( row == null )
                    {
                        ImageProvider imageProvider = null;
                        
                        final ImageService imageService = element.service( ImageService.class );
                        
                        if( imageService != null )
                        {
                            imageProvider = new ImageProvider()
                            {
                                private Listener imageServiceListener;
                                
                                @Override
                                public ImageData image()
                                {
                                    if( this.imageServiceListener == null )
                                    {
                                        this.imageServiceListener = new Listener()
                                        {
                                            @Override
                                            public void handle( final Event event )
                                            {
                                                update( row() );
                                            }
                                        };
                                        
                                        imageService.attach( this.imageServiceListener );
                                    }
                                    
                                    return imageService.image();
                                }

                                @Override
                                public void dispose()
                                {
                                    if( this.imageServiceListener != null )
                                    {
                                        imageService.detach( this.imageServiceListener );
                                    }
                                }
                            };
                        }
                        else if( getColumnCount() == 1 )
                        {
                            final Value<?> value = (Value<?>) element.property( getColumnHandler( 0 ).property() );
                            final ValueImageService valueImageService = value.service( ValueImageService.class );
                            
                            if( valueImageService != null )
                            {
                                imageProvider = new ImageProvider()
                                {
                                    @Override
                                    public ImageData image()
                                    {
                                        return valueImageService.provide( value.text() );
                                    }
                                };
                            }
                        }
                        
                        row = new TableRow( element, imageProvider );
                    }
                    
                    rows.put( element, row );
                }
                
                if( TablePropertyEditorPresentation.this.rows != null )
                {
                    for( TableRow row : TablePropertyEditorPresentation.this.rows.values() )
                    {
                        row.dispose();
                    }
                }
                
                TablePropertyEditorPresentation.this.rows = rows;

                return rows.values().toArray();
            }

            public void inputChanged( final Viewer viewer,
                                      final Object oldInput,
                                      final Object newInput )
            {
            }

            public void dispose()
            {
                for( TableRow row : TablePropertyEditorPresentation.this.rows.values() )
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
                            if( TablePropertyEditorPresentation.this.table.isDisposed() )
                            {
                                return;
                            }
                            
                            final Element newListElement = (Element) ( (PostExecuteEvent) event ).getResult();
        
                            if( newListElement != null )
                            {
                                TablePropertyEditorPresentation.this.refreshOperation.run();
                                
                                final TableRow row = TablePropertyEditorPresentation.this.rows.get( newListElement );
                                
                                TablePropertyEditorPresentation.this.tableViewer.setSelection( new StructuredSelection( row ), true );
                                
                                if( TablePropertyEditorPresentation.this.table.isDisposed() )
                                {
                                    return;
                                }
                                
                                TablePropertyEditorPresentation.this.tableViewer.editElement( row, 0 );
                                TablePropertyEditorPresentation.this.table.notifyListeners( SWT.Selection, null );
                            }
                        }
                    }
                };
                
                final PossibleTypesService possibleTypesService = property.service( PossibleTypesService.class );

                final Runnable refreshAddActionHandlersOp = new Runnable()
                {
                    public void run()
                    {
                        addAction.removeHandlers( addActionHandlers );
                        
                        for( SapphireActionHandler addActionHandler : addActionHandlers )
                        {
                            addActionHandler.dispose();
                        }
                        
                        for( ElementType memberType : possibleTypesService.types() )
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

            if( ! property.definition().hasAnnotation( FixedOrderList.class ) )
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
                            TablePropertyEditorPresentation.this.refreshOperation.run();
                            
                            // This is a workaround for a weird problem on SWT on Windows. If modifier keys are pressed
                            // when the list is re-ordered (as in when issuing move up or move down command from the
                            // keyboard), the focused row can detached from selected row.
                            
                            final Element element = getSelectedElement();
                            final TableItem[] items = TablePropertyEditorPresentation.this.table.getItems();
                            
                            for( int i = 0; i < items.length; i++ )
                            {
                                if( items[ i ].getData() == element )
                                {
                                    TablePropertyEditorPresentation.this.table.setSelection( i );
                                    break;
                                }
                            }
                        }
                    }
                };
                
                moveUpAction.attach( moveActionHandlerListener );
                moveDownAction.attach( moveActionHandlerListener );
                
                final ElementsTransfer transfer = new ElementsTransfer( element().type().getModelElementClass().getClassLoader() );
                final Transfer[] transfers = new Transfer[] { transfer };
                
                final DragSource dragSource = new DragSource( this.table, DND.DROP_COPY | DND.DROP_MOVE );
                dragSource.setTransfer( transfers );

                final List<Element> dragElements = new ArrayList<Element>();
                
                dragSource.addDragListener
                (
                    new DragSourceListener()
                    {
                        public void dragStart( final DragSourceEvent event )
                        {
                            if( TablePropertyEditorPresentation.this.tableViewer.getComparator() == null )
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
                        
                        public void dragFinished( final DragSourceEvent event )
                        {
                            if( event.detail == DND.DROP_MOVE )
                            {
                                // When drop target is the same editor as drag source, the drop handler takes care of removing
                                // elements from their original location. The following block of code accounts for the case when 
                                // dropping into another editor.
                                
                                boolean droppedIntoAnotherEditor = false;
                                
                                for( Element dragElement : dragElements )
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
                                        final Element selectionPostDelete = findSelectionPostDelete( property(), dragElements );
                                        
                                        for( Element dragElement : dragElements )
                                        {
                                            final ElementList<?> dragElementContainer = (ElementList<?>) dragElement.parent();
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
                                            Sapphire.service( LoggingService.class ).log( e );
                                        }
                                    }
                                }
                            }
                            
                            dragElements.clear();
                        }
                    }
                );
                
                final DropTarget target = new DropTarget( this.table, DND.DROP_COPY | DND.DROP_MOVE );
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

                                final Point pt = dragOverItem.getDisplay().map( null, TablePropertyEditorPresentation.this.table, event.x, event.y );
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

                        public void drop( final DropTargetEvent event ) 
                        {
                            if( event.data == null )
                            {
                                event.detail = DND.DROP_NONE;
                                return;
                            }
                            
                            final List<Element> droppedElements = (List<Element>) event.data;
                            final Set<ElementType> possibleTypesService = property.service( PossibleTypesService.class ).types();
                            
                            for( Element droppedElement : droppedElements )
                            {
                                if( ! possibleTypesService.contains( droppedElement.type() ) )
                                {
                                    event.detail = DND.DROP_NONE;
                                    return;
                                }
                            }
                            
                            final ElementList<?> list = property();
                            
                            int position;
                            
                            if( event.item == null )
                            {
                                position = list.size();
                            }
                            else
                            {
                                final TableItem dropTargetItem = (TableItem) event.item;
                                final TableRow dropTargetRow = (TableRow) dropTargetItem.getData();
                                final Element dropTargetElement = dropTargetRow.element();
                                
                                final Point pt = TablePropertyEditorPresentation.this.table.getDisplay().map( null, TablePropertyEditorPresentation.this.table, event.x, event.y );
                                final Rectangle bounds = dropTargetItem.getBounds();
                                
                                position = list.indexOf( dropTargetElement );
                                
                                if( pt.y >= bounds.y + bounds.height / 2 ) 
                                {
                                    position++;
                                }
                            }
                            
                            try
                            {
                                if( event.detail == DND.DROP_MOVE )
                                {
                                    for( Element dragElement : dragElements )
                                    {
                                        final ElementList<?> dragElementContainer = (ElementList<?>) dragElement.parent();
                                        
                                        if( dragElementContainer == list && dragElementContainer.indexOf( dragElement ) < position )
                                        {
                                            position--;
                                        }
                                        
                                        dragElementContainer.remove( dragElement );
                                    }
                                }
            
                                final List<Element> newSelection = new ArrayList<Element>();
                                
                                for( Element droppedElement : droppedElements )
                                {
                                    final Element insertedElement = list.insert( droppedElement.type(), position );
                                    insertedElement.copy( droppedElement );
                                    
                                    newSelection.add( insertedElement );
                                    
                                    position++;
                                }
                                
                                if( TablePropertyEditorPresentation.this.table.isDisposed() )
                                {
                                    return;
                                }
                                
                                TablePropertyEditorPresentation.this.tableViewer.refresh();
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
                                    Sapphire.service( LoggingService.class ).log( e );
                                }
                                
                                event.detail = DND.DROP_NONE;
                            }
                        }
                    }
                );
            }
        }
        
        final boolean toolBarNeeded = toolBarActionsPresentation.hasActions();
        
        mainComposite.setLayout( glayout( ( toolBarNeeded ? 2 : 1 ), 0, 0, 0, 0 ) );
        
        if( toolBarNeeded )
        {
            final ToolBar toolbar = new ToolBar( mainComposite, SWT.FLAT | SWT.VERTICAL );
            toolbar.setLayoutData( gdvfill() );
            toolBarActionsPresentation.setToolBar( toolbar );
            toolBarActionsPresentation.render();
            addControl( toolbar );
            this.decorator.addEditorControl( toolbar );
        }
        
        if( menuActionsPresentation.hasActions() )
        {
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
                        jumpHandler.execute( TablePropertyEditorPresentation.this );
                    }
                }
                
                private SapphireActionHandler getJumpHandler( final TableItem item,
                                                              final int column )
                {
                    final Element element = ( (TableRow) item.getData() ).element();
                    final ModelPath property = part.getChildProperties().get( column );
                    final PropertyEditorPart propertyEditor = part.getChildPropertyEditor( element, property );
                    final SapphireActionGroup actions = propertyEditor.getActions();
                    return actions.getAction( ACTION_JUMP ).getFirstActiveHandler();
                }
            }
        );
        
        suppressDashedTableEntryBorder( this.table );
        
        addControl( this.table );
        
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
    
    public final Element getSelectedElement()
    {
        final IStructuredSelection sel = (IStructuredSelection) this.selectionProvider.getSelection();
        return (Element) sel.getFirstElement();
    }
    
    public final List<Element> getSelectedElements()
    {
        final IStructuredSelection sel = (IStructuredSelection) this.selectionProvider.getSelection();
        final ListFactory<Element> elements = ListFactory.start();
        
        for( Iterator<?> itr = sel.iterator(); itr.hasNext(); )
        {
            elements.add( (Element) itr.next() );
        }
        
        return elements.result();
    }
    
    public final void setSelectedElement( final Element element )
    {
        setSelectedElements( element == null ? Collections.<Element>emptyList() : Collections.singletonList( element ) );
    }
    
    public final void setSelectedElements( final List<Element> elements )
    {
        if( ! equalsBasedOnEntryIdentity( getSelectedElements(), elements ) )
        {
            final ListFactory<TableRow> rows = ListFactory.start();
            
            for( Element element : elements )
            {
                final TableRow row = this.rows.get( element );
                
                if( row != null )
                {
                    rows.add( row );
                }
            }
            
            this.tableViewer.setSelection( new StructuredSelection( rows.result() ) );
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
    public PropertyEditorPresentation2 createChildPropertyEditorPresentation( final PropertyEditorPart part )
    {
        final Table table = this.table;
        final Property property = part.property();
        final Element element = findTableRowElement( property.element() );
        
        ColumnHandler handler = null;
        
        for( final ColumnHandler h : this.columnHandlers )
        {
            if( element.property( h.property() ) == property )
            {
                handler = h;
                break;
            }
        }
        
        if( handler == null )
        {
            throw new IllegalStateException();
        }
        
        final int column = this.columnHandlers.indexOf( handler );
        
        return new PropertyEditorPresentation2( part, this, table )
        {
            @Override
            public Rectangle bounds()
            {
                Rectangle bounds = null;
                
                for( int i = 0, n = table.getItemCount(); i < n && bounds == null; i++ )
                {
                    final TableItem item = table.getItem( i );
                    
                    if( ( (TableRow) item.getData() ).element() == element )
                    {
                        bounds = item.getBounds( column );
                    }
                }
                
                if( bounds == null )
                {
                    throw new IllegalStateException();
                }
                else
                {
                    final Point position = table.toDisplay( bounds.x, bounds.y );
                    bounds = new Rectangle( position.x, position.y, bounds.width, bounds.height );
                }
                
                return bounds;
            }

            @Override
            public void render()
            {
            }
        };
    }
    
    @Override
    protected void handleChildPropertyEvent( final PropertyContentEvent event )
    {
        super.handleChildPropertyEvent( event );
        
        if( ! this.table.isDisposed() )
        {
            this.table.getDisplay().asyncExec
            (
                new Runnable()
                {
                    public void run()
                    {
                        update( ( (PropertyEvent) event ).property().element() );
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
                                               final ModelPath childPropertyPath,
                                               final boolean showImages,
                                               final PropertyEditorDef childPropertyEditorDef )
    {
        final PropertyDef childProperty = property().definition().getType().property( childPropertyPath );
        final ColumnHandler columnHandler;
        
        if( childProperty.isOfType( Boolean.class ) )
        {
            columnHandler = new BooleanPropertyColumnHandler( this.tableViewer, this.selectionProvider, part(), 
                                                              allColumnHandlers, childPropertyPath, showImages );
        }
        else
        {
            PopUpListFieldStyle popUpListFieldPresentationStyle = null;
            
            if( childProperty.isOfType( Enum.class ) )
            {
                popUpListFieldPresentationStyle = PopUpListFieldStyle.STRICT;
            }
            else if( childPropertyEditorDef != null )
            {
                final String style = childPropertyEditorDef.getStyle().text();
                
                if( style != null )
                {
                    if( style.startsWith( "Sapphire.PropertyEditor.PopUpListField" ) )
                    {
                        if( style.equals( "Sapphire.PropertyEditor.PopUpListField" ) )
                        {
                            final PossibleValues possibleValuesAnnotation = childProperty.getAnnotation( PossibleValues.class );
                            
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
                columnHandler = new PopUpListFieldColumnPresentation( this.tableViewer, this.selectionProvider, part(), 
                                                                      allColumnHandlers, childPropertyPath, showImages, popUpListFieldPresentationStyle );
            }
            else
            {
                columnHandler = new ColumnHandler( this.tableViewer, this.selectionProvider, part(), 
                                                   allColumnHandlers, childPropertyPath, showImages );
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
    
    private void update( final Element element )
    {
        if( element == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( ! disposed() )
        {
            final Element root = part().getLocalModelElement();
            
            Element el = element;
            TableRow row = null;
            
            while( row == null && el != null && el != root )
            {
                row = this.rows.get( el );
                
                if( row == null )
                {
                    final Property parent = el.parent();
                    
                    if( parent != null )
                    {
                        el = parent.element();
                    }
                }
            }
            
            if( row != null )
            {
                update( row );
            }
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
    
    private Element findTableRowElement( final Element element )
    {
        final ElementList<?> list = list();
        Element result = element;
        
        for( Property parent = element.parent(); parent != list; parent = result.parent() )
        {
            result = parent.element();
        }
        
        return result;
    }
    
    public static final class Factory extends PropertyEditorPresentationFactory
    {
        @Override
        public PropertyEditorPresentation create( final PropertyEditorPart part, final SwtPresentation parent, final Composite composite )
        {
            if( part.property().definition() instanceof ListProperty )
            {
                return new TablePropertyEditorPresentation( part, parent, composite );
            }
            
            return null;
        }
    }
    
    private static final class CustomTableViewer extends TableViewer
    {
        public CustomTableViewer( final Composite parent, final int style )
        {
            super( parent, style );
        }

        @Override
        public void applyEditorValue()
        {
            super.applyEditorValue();
        }
    }

    private final class DefaultColumnLabelProvider extends ColumnLabelProvider
    {
        private final ColumnHandler columnHandler;
        
        public DefaultColumnLabelProvider( final ColumnHandler columnHandler )
        {
            this.columnHandler = columnHandler;
        }
    
        @Override
        public String getText( final Object obj )
        {
            final Element element = ( (TableRow) obj ).element();
            final Value<?> value = this.columnHandler.property( element );
            
            final String text = value.text();
            String label = null;
            
            try
            {
                label = value.service( ValueLabelService.class ).provide( text );
            }
            catch( Exception e )
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
            
            if( label == null )
            {
                label = text;
            }
            else if( ! label.equals( text ) )
            {
                final LocalizationService localizationService = part().definition().adapt( LocalizationService.class );
                label = localizationService.transform( label, CapitalizationType.FIRST_WORD_ONLY, false );
            }
            
            if( label == null )
            {
                if( this.columnHandler.isEmptyTextLabelDesired( element ) )
                {
                    label = emptyRowIndicator.text();
                }
                else
                {
                    label = MiscUtil.EMPTY_STRING;
                }
            }
            
            return label;
        }
        
        @Override
        public Image getImage( final Object obj )
        {
            if( this.columnHandler.isElementImageDesired() )
            {
                return ( (TableRow) obj ).image();
            }
            else
            {
                return null;
            }
        }
    
        @Override
        public Color getForeground( final Object obj )
        {
            final Value<?> value = this.columnHandler.property( ( (TableRow) obj ).element() );
            
            if( value.text( false ) == null )
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
            final Element element = ( (TableRow) obj ).element();
            
            boolean canEdit;
            
            if( this.columnHandler.property( element ).enabled() )
            {
                canEdit = ! this.columnHandler.editor( element ).isReadOnly();
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
        protected final Table table;
        protected final TableViewer tableViewer;
        protected final SelectionProvider selectionProvider;
        protected final PropertyEditorPart listPropertyEditor;
        protected final List<ColumnHandler> allColumnHandlers;
        protected final ModelPath property;
        protected final boolean showElementImage;
        protected final Collator collator;
        private CellLabelProvider labelProvider;
        private AbstractColumnEditingSupport editingSupport;
        
        public ColumnHandler( final TableViewer tableViewer,
                              final SelectionProvider selectionProvider,
                              final PropertyEditorPart listPropertyEditor,
                              final List<ColumnHandler> allColumnHandlers,
                              final ModelPath property,
                              final boolean showElementImage )
        {
            this.table = tableViewer.getTable();
            this.tableViewer = tableViewer;
            this.selectionProvider = selectionProvider;
            this.listPropertyEditor = listPropertyEditor;
            this.allColumnHandlers = allColumnHandlers;
            this.property = property;
            this.showElementImage = showElementImage;
            this.collator = Collator.getInstance();
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
        
        public final SwtResourceCache getImageCache()
        {
            return this.listPropertyEditor.getSwtResourceCache();
        }
        
        public final boolean isElementImageDesired()
        {
            return this.showElementImage;
        }
        
        public final ModelPath property()
        {
            return this.property;
        }
        
        public final Value<?> property( final Element element )
        {
            return (Value<?>) element.property( this.property );
        }
        
        public final PropertyEditorPart editor( final Element element )
        {
            return getListPropertyEditor().getChildPropertyEditor( element, property() );
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
            if( this.editingSupport == null && ! TablePropertyEditorPresentation.this.property().definition().getType().property( property() ).isReadOnly() )
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
                    
                    final PropertyEditorPart propertyEditor = editor( ( (TableRow) obj ).element() );
                    final SapphireActionGroup actions = propertyEditor.getActions();

                    final int style = ( getTable().getLinesVisible() ? SWT.NONE : SWT.BORDER );
                    
                    this.cellEditor = new SapphireTextCellEditor
                    (
                        createChildPropertyEditorPresentation( propertyEditor ),
                        getTableViewer(),
                        getSelectionProvider(),
                        propertyEditor.getLocalModelElement(),
                        (ValueProperty) propertyEditor.property().definition(),
                        actions,
                        style
                    );

                    if( isElementImageDesired() )
                    {
                        this.cellEditor.setHorizonalIndent( 3 );
                    }
                    
                    return this.cellEditor;
                }
    
                @Override
                public Object getValue( final Object obj )
                {
                    return property( ( (TableRow) obj ).element() );
                }
    
                @Override
                public void setValue( final Object obj,
                                      final Object value )
                {
                    property( ( (TableRow) obj ).element() ).write( value );
                }
            };
        }
        
        public boolean isEditorActivationEvent( final ColumnViewerEditorActivationEvent event ) 
        {
            return event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION ||
                   event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC ||
                   event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL;
        }
        
        @SuppressWarnings( { "unchecked", "rawtypes" } )
        
        public final int comparePropertyValues( final Element aElement, final Element bElement )
        {
            final Value<?> aValue = property( aElement );
            final Value<?> bValue = property( bElement );
            
            final boolean aEmpty = aValue.empty();
            final boolean bEmpty = bValue.empty();
            
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
                if( ! aValue.malformed() && ! bValue.malformed() )
                {
                    final Object aContent = aValue.content();
                    final Object bContent = bValue.content();
                    
                    if( aContent instanceof Boolean && bContent instanceof Boolean )
                    {
                        // The boolean comparison is intentionally reversed so that the following
                        // statement is true:
                        //
                        // compare( true, false ) == compare( true, null )
                        
                        return ( (Boolean) bContent ).compareTo( (Boolean) aContent );
                    }
                    else if( aContent instanceof Comparable && bContent instanceof Comparable )
                    {
                        final Comparable aComparable = (Comparable) aContent;
                        final Comparable bComparable = (Comparable) bContent;
                        
                        return aComparable.compareTo( bComparable );
                    }
                }
                
                return this.collator.compare( aValue.text(), bValue.text() );
            }
        }
        
        public boolean isEmptyTextLabelDesired( final Element element )
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
                    if( handler.property( element ).text() != null )
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
        
        public PopUpListFieldColumnPresentation( final TableViewer tableViewer,
                                                 final SelectionProvider selectionProvider,
                                                 final PropertyEditorPart listPropertyEditor,
                                                 final List<ColumnHandler> allColumnHandlers,
                                                 final ModelPath property,
                                                 final boolean showElementImage,
                                                 final PopUpListFieldStyle popUpListFieldStyle )
        {
            super( tableViewer, selectionProvider, listPropertyEditor, allColumnHandlers, property, showElementImage );
            
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
                    
                    final PropertyEditorPart propertyEditor = editor( ( (TableRow) obj ).element() );
                    
                    this.cellEditor = new PopUpListFieldCellEditorPresentation
                    (
                        getTableViewer(),
                        getSelectionProvider(),
                        propertyEditor.property(),
                        PopUpListFieldColumnPresentation.this.popUpListFieldStyle,
                        getTable().getLinesVisible() ? SWT.NONE : SWT.BORDER
                    );
                    
                    return this.cellEditor;
                }

                @Override
                public Object getValue( final Object obj )
                {
                    return property( ( (TableRow) obj ).element() );
                }
    
                @Override
                public void setValue( final Object obj,
                                      final Object value )
                {
                    property( ( (TableRow) obj ).element() ).write( value );
                }
            };
        }
    }
    
    private static final ImageDescriptor IMG_CHECKBOX_ON
        = SwtUtil.createImageDescriptor( BooleanPropertyColumnHandler.class, "CheckBoxOn.gif" );
    
    private static final ImageDescriptor IMG_CHECKBOX_OFF
        = SwtUtil.createImageDescriptor( BooleanPropertyColumnHandler.class, "CheckBoxOff.gif" );
    
    private final class BooleanPropertyColumnHandler extends ColumnHandler
    {
        private static final int CHECKBOX_IMAGE_WIDTH = 16;
        private static final int CHECKBOX_IMAGE_HEIGHT = 16;
        
        public BooleanPropertyColumnHandler( final TableViewer tableViewer,
                                             final SelectionProvider selectionProvider,
                                             final PropertyEditorPart listPropertyEditor,
                                             final List<ColumnHandler> allColumnHandlers,
                                             final ModelPath property,
                                             final boolean showElementImage )
        {
            super( tableViewer, selectionProvider, listPropertyEditor, allColumnHandlers, property, showElementImage );
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
                    final Element element = ( (TableRow) item.getData() ).element();

                    if( property( element ).enabled() )
                    {
                        final boolean value = getPropertyValueAsBoolean( element );
                        
                        final Image image = getImageCache().image( value ? IMG_CHECKBOX_ON : IMG_CHECKBOX_OFF );
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
                public Object getValue( final Object obj )
                {
                    final Boolean val = (Boolean) property( ( (TableRow) obj ).element() ).content();
                    return ( val != null ? val : Boolean.FALSE );
                }

                @Override
                public void setValue( final Object obj,
                                      final Object value )
                {
                    final String str = String.valueOf( ( (Boolean) value ).booleanValue() );
                    property( ( (TableRow) obj ).element() ).write( str );
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

        private boolean getPropertyValueAsBoolean( final Element element )
        {
            final Boolean val = (Boolean) property( element ).content();
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
        @SuppressWarnings( "rawtypes" ) // Viewer is parameterized since Eclipse 4.4
        
        public int compare( final Viewer viewer, final Object x, final Object y )
        {
            int result = this.columnHandler.comparePropertyValues( ( (TableRow) x ).element(), ( (TableRow) y ).element() );
            
            if( this.direction == SWT.UP )
            {
                result = result * -1;
            }
            
            return result;
        }
    }
    
    private abstract class TablePropertyEditorActionHandler extends PropertyEditorActionHandler
    {
        @Override
        protected final Object run( final Presentation context )
        {
            TablePropertyEditorPresentation.this.tableViewer.applyEditorValue();
            return executeTablePropertyEditorAction( context );
        }
        
        protected abstract Object executeTablePropertyEditorAction( Presentation context );
    }
    
    private final class AddActionHandler extends TablePropertyEditorActionHandler
    {
        private final ElementType type;
        
        public AddActionHandler( final ElementType type )
        {
            this.type = type;
            
            setId( "Sapphire.Add." + this.type.getSimpleName() );
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
        protected Object executeTablePropertyEditorAction( final Presentation context )
        {
            TablePropertyEditorPresentation.this.tableViewer.applyEditorValue();
            return list().insert( this.type );
        }
    }
    
    private abstract class SelectionBasedActionHandler extends TablePropertyEditorActionHandler
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
        protected final Object executeTablePropertyEditorAction( final Presentation context )
        {
            final List<TableRow> rowsToDelete = getSelectedRows();
            
            final TableRow selectionPostDelete 
                = findSelectionPostDelete( TablePropertyEditorPresentation.this.rows.values(), rowsToDelete );
            
            final ElementList<?> list = list();

            for( TableRow row : rowsToDelete )
            {
                list.remove( row.element() );
            }
            
            TablePropertyEditorPresentation.this.refreshOperation.run();
            
            if( selectionPostDelete != null )
            {
                TablePropertyEditorPresentation.this.tableViewer.setSelection( new StructuredSelection( selectionPostDelete ) );
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
                if( getSelectedElements().size() == 1 && TablePropertyEditorPresentation.this.tableViewer.getComparator() == null )
                {
                    final Element modelElement = getSelectedElement();
                    return ( list().indexOf( modelElement ) > 0 );
                }
            }
            
            return false;
        }

        @Override
        protected final Object executeTablePropertyEditorAction( final Presentation context )
        {
            final Element element = getSelectedElement();

            list().moveUp( element );
            TablePropertyEditorPresentation.this.tableViewer.reveal( element );
            
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
                if( getSelectedElements().size() == 1 && TablePropertyEditorPresentation.this.tableViewer.getComparator() == null )
                {
                    final Element modelElement = getSelectedElement();
                    final ElementList<?> list = list();
                    return ( list.indexOf( modelElement ) < ( list.size() - 1 ) );
                }
            }
            
            return false;
        }
    
        @Override
        protected final Object executeTablePropertyEditorAction( final Presentation context )
        {
            final Element element = getSelectedElement();

            list().moveDown( element );
            TablePropertyEditorPresentation.this.tableViewer.reveal( element );
            
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
            final ListFactory<Element> elements = ListFactory.start();
            
            for( Iterator<?> itr = getSelectedRows().iterator(); itr.hasNext(); )
            {
                final TableRow row = (TableRow) itr.next();
                elements.add( row.element() );
            }
            
            return new StructuredSelection( elements.result() );
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
    
    private abstract class ImageProvider
    {
        private TableRow row;
        
        public final void init( final TableRow row )
        {
            this.row = row;
        }
        
        protected final TableRow row()
        {
            return this.row;
        }
        
        public abstract ImageData image();
        public void dispose() {}
    }
    
    private final class TableRow
    {
        private final Element element;
        private final ImageProvider imageProvider;
        private Listener validationListener;
        private Status.Severity validationSeverity;
        
        public TableRow( final Element element, final ImageProvider imageProvider )
        {
            this.element = element;
            
            this.imageProvider = imageProvider;
            
            if( this.imageProvider != null )
            {
                this.imageProvider.init( this );
            }
        }
        
        public Element element()
        {
            return this.element;
        }
        
        public Image image()
        {
            final ImageData image;
            
            if( this.imageProvider == null )
            {
                image = null;
            }
            else
            {
                image = this.imageProvider.image();
            }
            
            if( image == null )
            {
                return null;
            }
            else
            {
                if( this.validationListener == null )
                {
                    this.validationListener = new FilteredListener<PropertyValidationEvent>()
                    {
                        @Override
                        protected void handleTypedEvent( final PropertyValidationEvent event )
                        {
                            refreshValidation();
                        }
                    };
                    
                    this.element.attach( this.validationListener, "*" );
                    
                    this.validationSeverity = this.element.validation().severity();
                }
                
                return part().getSwtResourceCache().image( image, this.validationSeverity );
            }
        }
        
        private void refreshValidation()
        {
            final Status.Severity freshValidationSeverity = this.element.validation().severity();
            
            if( this.validationSeverity != freshValidationSeverity )
            {
                this.validationSeverity = freshValidationSeverity;
                update( this );
            }
        }
        
        public void dispose()
        {
            if( this.imageProvider != null )
            {
                this.imageProvider.dispose();
            }
            
            if( this.validationListener != null )
            {
                this.element.detach( this.validationListener, "*" );
            }
        }
    }
    
}
