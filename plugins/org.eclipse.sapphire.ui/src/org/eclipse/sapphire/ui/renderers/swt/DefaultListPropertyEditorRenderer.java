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

package org.eclipse.sapphire.ui.renderers.swt;

import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_ADD;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_ASSIST;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_DELETE;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_JUMP;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_MOVE_DOWN;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_MOVE_UP;
import static org.eclipse.sapphire.ui.SapphireActionSystem.createFilterByActionId;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.DATA_BINDING;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_COLUMN_WIDTHS;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_SHOW_HEADER;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.RELATED_CONTROLS;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdvfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glspacing;
import static org.eclipse.sapphire.ui.swt.renderer.SwtUtil.suppressDashedTableEntryBorder;
import static org.eclipse.sapphire.ui.util.MiscUtil.findSelectionPostDelete;

import java.text.Collator;
import java.util.ArrayList;
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
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.EnumValueType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImageData;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.FixedOrderList;
import org.eclipse.sapphire.modeling.util.MiscUtil;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.ImageService;
import org.eclipse.sapphire.services.PossibleTypesService;
import org.eclipse.sapphire.services.ValueSerializationService;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionHandler.PostExecuteEvent;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.sapphire.ui.SapphirePropertyEditor;
import org.eclipse.sapphire.ui.SapphirePropertyEditorActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerDef;
import org.eclipse.sapphire.ui.def.PropertyEditorDef;
import org.eclipse.sapphire.ui.internal.ReadOnlyComboBoxCellEditor;
import org.eclipse.sapphire.ui.internal.binding.AbstractBinding;
import org.eclipse.sapphire.ui.swt.renderer.HyperlinkTable;
import org.eclipse.sapphire.ui.swt.renderer.SapphireActionPresentationManager;
import org.eclipse.sapphire.ui.swt.renderer.SapphireMenuActionPresentation;
import org.eclipse.sapphire.ui.swt.renderer.SapphireTextCellEditor;
import org.eclipse.sapphire.ui.swt.renderer.SapphireToolBarActionPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class DefaultListPropertyEditorRenderer extends ListPropertyEditorRenderer
{
    public static final String DATA_SELECTION_PROVIDER = "selection.provider";
    
    private boolean exposeAddAction;
    private boolean exposeDeleteAction;
    private Map<IModelElement,TableRow> rows;
    private Table table;
    private TableViewer tableViewer;
    private SelectionProvider selectionProvider;
    private List<ColumnHandler> columnHandlers;
    private Runnable refreshOperation;
    
    public DefaultListPropertyEditorRenderer( final SapphireRenderingContext context,
                                              final SapphirePropertyEditor part )
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
        final SapphirePropertyEditor part = getPart();
        final IModelElement element = part.getLocalModelElement();
        final ListProperty property = (ListProperty) part.getProperty();
        final boolean isReadOnly = part.isReadOnly();
        final boolean showHeader = part.getRenderingHint( HINT_SHOW_HEADER, true );
        
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
            new Listener() 
            {
                public void handleEvent( final Event event ) 
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
        this.table.setData( DATA_SELECTION_PROVIDER, this.selectionProvider );
        
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
        
        this.binding = new AbstractBinding( getPart(), this.context, this.table )
        {
            @Override
            protected void doUpdateModel()
            {
                // Don't do anything here. Changes to the model are pushed at cell-level.
            }

            @Override
            protected void doUpdateTarget()
            {
                // Changes are mostly synchronized at cell-level. This is only here to
                // catch the case where a full page refresh is performed perhaps because user
                // has visited the source page.
                
                DefaultListPropertyEditorRenderer.this.refreshOperation.run();
            }
        };
        
        this.table.setData( DATA_BINDING, this.binding );
        
        boolean showImages = true;
        
        final String columnWidthsHint = part.getRenderingHint( HINT_COLUMN_WIDTHS, "" );
        final StringTokenizer columnWidthsHintTokenizer = new StringTokenizer( columnWidthsHint, "," );
        
        for( final ValueProperty memberProperty : columnProperties )
        {
            final PropertyEditorDef childPropertyEditorDef = part.getDefinition().getChildPropertyEditor( memberProperty );
            
            final TableViewerColumn col2 = new TableViewerColumn( this.tableViewer, SWT.NONE );
            col2.getColumn().setText( SapphirePropertyEditor.getLabel( memberProperty, childPropertyEditorDef, CapitalizationType.TITLE_STYLE, false ) );
            
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
            
            final ColumnHandler columnHandler = createColumnHandler( this.columnHandlers, memberProperty, showImages );
            
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
            }
            
            final ToolBar toolbar = new ToolBar( mainComposite, SWT.FLAT | SWT.VERTICAL );
            toolbar.setLayoutData( gdvfill() );
            toolBarActionsPresentation.setToolBar( toolbar );
            toolBarActionsPresentation.render();
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
                    final SapphirePropertyEditor propertyEditor = getPart().getChildPropertyEditor( element, property );
                    final SapphireActionGroup actions = propertyEditor.getActions();
                    return actions.getAction( ACTION_JUMP ).getFirstActiveHandler();
                }
            }
        );
        
        suppressDashedTableEntryBorder( this.table );
        
        addControl( this.table );
        
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
        final IStructuredSelection sel = (IStructuredSelection) DefaultListPropertyEditorRenderer.this.tableViewer.getSelection();
        
        if( sel == null )
        {
            return null;
        }
        else
        {
            return ( (TableRow) sel.getFirstElement() ).element();
        }
    }
    
    public final List<IModelElement> getSelectedElements()
    {
        final IStructuredSelection sel = (IStructuredSelection) DefaultListPropertyEditorRenderer.this.tableViewer.getSelection();
        final List<IModelElement> elements = new ArrayList<IModelElement>();
        
        if( sel != null )
        {
            for( Iterator<?> itr = sel.iterator(); itr.hasNext(); )
            {
                elements.add( ( (TableRow) itr.next() ).element() );
            }
        }
        
        return elements;
    }
    
    private final List<TableRow> getSelectedRows()
    {
        final IStructuredSelection sel = (IStructuredSelection) DefaultListPropertyEditorRenderer.this.tableViewer.getSelection();
        final List<TableRow> rows = new ArrayList<TableRow>();
        
        if( sel != null )
        {
            for( Iterator<?> itr = sel.iterator(); itr.hasNext(); )
            {
                rows.add( (TableRow) itr.next() );
            }
        }
        
        return rows;
    }
    
    public final void setSelection( final List<IModelElement> selection )
    {
        final IStructuredSelection sel = new StructuredSelection( selection );
        this.tableViewer.setSelection( sel );
    }
    
    public final void setFocusOnTable()
    {
        this.table.setFocus();
    }
    
    @Override
    protected void handlePropertyChangedEvent()
    {
        super.handlePropertyChangedEvent();
        this.refreshOperation.run();
    }
    
    @Override
    protected void handleListElementChangedEvent( final ModelPropertyChangeEvent event )
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
                        update( event.getModelElement() );
                        
                        // Cause the overall list editor decorator to be updated.
                        
                        DefaultListPropertyEditorRenderer.this.binding.updateTargetAttributes();
                    }
                }
            );
        }
    }
    
    protected void handleTableFocusGainedEvent()
    {
        if( this.tableViewer.getSelection().isEmpty() && this.table.getItemCount() > 0 )
        {
            final Object firstItem = this.table.getItem( 0 ).getData();
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
                                               final boolean showImages )
    {
        final ColumnHandler columnHandler;
        
        if( property.isOfType( Boolean.class ) )
        {
            columnHandler = new BooleanPropertyColumnHandler( this.context, this.tableViewer, this.selectionProvider, getPart(), 
                                                              allColumnHandlers, property, showImages );
        }
        else if( property.isOfType( Enum.class ) )
        {
            columnHandler = new EnumPropertyColumnHandler( this.context, this.tableViewer, this.selectionProvider, getPart(), 
                                                           allColumnHandlers, property, showImages );
        }
        else
        {
            columnHandler = new ColumnHandler( this.context, this.tableViewer, this.selectionProvider, getPart(), 
                                               allColumnHandlers, property, showImages );
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
        public boolean isApplicableTo( final SapphirePropertyEditor propertyEditorDefinition )
        {
            return ( propertyEditorDefinition.getProperty() instanceof ListProperty );
        }
        
        @Override
        public PropertyEditorRenderer create( final SapphireRenderingContext context,
                                              final SapphirePropertyEditor part )
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
                        image = getPart().getImageCache().getImage( valueImageData, element.validate().severity() );
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
            
            if( element.isPropertyEnabled( property ) )
            {
                final SapphirePropertyEditor propertyEditor = this.columnHandler.getListPropertyEditor().getChildPropertyEditor( element, property );
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
        protected final SapphirePropertyEditor listPropertyEditor;
        protected final List<ColumnHandler> allColumnHandlers;
        protected final ValueProperty property;
        protected final boolean showElementImage;
        protected final Collator collator;
        private CellLabelProvider labelProvider;
        private AbstractColumnEditingSupport editingSupport;
        
        public ColumnHandler( final SapphireRenderingContext context,
                              final TableViewer tableViewer,
                              final SelectionProvider selectionProvider,
                              final SapphirePropertyEditor listPropertyEditor,
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
        
        public final SapphirePropertyEditor getListPropertyEditor()
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
                    final SapphirePropertyEditor propertyEditor = getListPropertyEditor().getChildPropertyEditor( element, property );
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
    
    private final class EnumPropertyColumnHandler extends ColumnHandler
    {
        private final EnumValueType annotatedEnumeration;
        private final Enum<?>[] enumValues;
        
        public EnumPropertyColumnHandler( final SapphireRenderingContext context,
                                          final TableViewer tableViewer,
                                          final SelectionProvider selectionProvider,
                                          final SapphirePropertyEditor listPropertyEditor,
                                          final List<ColumnHandler> allColumnHandlers,
                                          final ValueProperty property,
                                          final boolean showElementImage )
        {
            super( context, tableViewer, selectionProvider, listPropertyEditor, allColumnHandlers, property, showElementImage );
            
            this.annotatedEnumeration = new EnumValueType( property.getTypeClass() );
            this.enumValues = this.annotatedEnumeration.getItems();
        }
        
        @Override
        protected AbstractColumnEditingSupport createEditingSupport()
        {
            return new AbstractColumnEditingSupport( this )
            {
                private ReadOnlyComboBoxCellEditor cellEditor;
                private List<String> cellEditorItems;

                @Override
                public CellEditor getCellEditor( final Object obj )
                {
                    if( this.cellEditor == null )
                    {
                        this.cellEditor = new ReadOnlyComboBoxCellEditor( getTable(), new String[ 0 ], SWT.DROP_DOWN | SWT.READ_ONLY );
                        this.cellEditorItems = new ArrayList<String>();
                    }
                    
                    final IModelElement element = ( (TableRow) obj ).element();
                    
                    final EnumValueType annotatedEnumeration = EnumPropertyColumnHandler.this.annotatedEnumeration;
                    final Enum<?>[] enumValues = EnumPropertyColumnHandler.this.enumValues;
                    
                    final Value<?> value = element.read( getProperty() );
                    final String stringValue = value.getText( false );
                    
                    boolean needExtraEntry = false;
                    
                    if( stringValue != null && value.getContent( false ) == null )
                    {
                        needExtraEntry = true;
                    }
                    
                    final String[] items = new String[ enumValues.length + ( needExtraEntry ? 1 : 0 ) ];
                    this.cellEditorItems.clear();
                    
                    for( int i = 0; i < enumValues.length; i++ )
                    {
                        final Enum<?> enumValue = enumValues[ i ];
                        final String enumValueText = element.service( getProperty(), ValueSerializationService.class ).encode( enumValue );
                        this.cellEditorItems.add( enumValueText );
                        items[ i ] = annotatedEnumeration.getLabel( enumValue, false, CapitalizationType.FIRST_WORD_ONLY, false );
                    }
                    
                    if( needExtraEntry )
                    {
                        items[ items.length ] = stringValue;
                    }
                    
                    this.cellEditor.setItems( items );
                    
                    return this.cellEditor;
                }

                @Override
                public Object getValue( final Object obj )
                {
                    String str = getPropertyValue( ( (TableRow) obj ).element() ).getText( false );
                    str = ( str != null ? str : MiscUtil.EMPTY_STRING );
                    
                    for( int i = 0, n = this.cellEditorItems.size(); i < n; i++ )
                    {
                        if( this.cellEditorItems.get( i ).equals( str ) )
                        {
                            return i;
                        }
                    }
                    
                    return -1;
                }

                @Override
                public void setValue( final Object obj,
                                      final Object value )
                {
                    final int index = (Integer) value;
                    final String str = ( index == -1 ? null : this.cellEditorItems.get( index ) );
                    setPropertyValue( ( (TableRow) obj ).element(), str );
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
                                             final SapphirePropertyEditor listPropertyEditor,
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
                protected void erase( final Event event,
                                      final Object element )
                {
                    // The default implementation causes non-native behavior on some platforms and
                    // is not desired. Nothing needs to happen on this event.
                }
    
                @Override
                protected void measure( final Event event,
                                        final Object element )
                {
                }
    
                @Override
                protected void paint( final Event event,
                                      final Object object )
                {
                    final TableItem item = (TableItem) event.item;
                    final IModelElement element = ( (TableRow) item.getData() ).element();
                    
                    if( element.isPropertyEnabled( getProperty() ) )
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
                          final ISapphireActionHandlerDef def )
        {
            super.init( action, def );
            
            final ImageDescriptor typeSpecificAddImage = getPart().getImageCache().getImageDescriptor( this.type );
            
            if( typeSpecificAddImage != null )
            {
                addImage( typeSpecificAddImage );
            }

            setLabel( this.type.getLabel( false, CapitalizationType.TITLE_STYLE, false ) );
        }

        @Override
        protected Object run( final SapphireRenderingContext context )
        {
            return getList().addNewElement( this.type );
        }
    }
    
    private abstract class SelectionBasedActionHandler extends SapphirePropertyEditorActionHandler
    {
        public SelectionBasedActionHandler()
        {
            DefaultListPropertyEditorRenderer.this.tableViewer.addSelectionChangedListener
            (
                new ISelectionChangedListener()
                {
                    public void selectionChanged( final SelectionChangedEvent event )
                    {
                        refreshEnablementState();
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
                        handleSelectionChangedEvent( getSelection() );
                    }
                }
            );
        }

        public ISelection getSelection()
        {
            if( this.fakeSelection != null )
            {
                return this.fakeSelection;
            }
            else
            {
                return this.tableViewer.getSelection();
            }
        }

        public void setSelection( final ISelection selection )
        {
            throw new UnsupportedOperationException();
        }
        
        public void setFakeSelection( final ISelection selection )
        {
            this.fakeSelection = selection;
            handleSelectionChangedEvent( getSelection() );
        }
        
        public void addSelectionChangedListener( final ISelectionChangedListener listener )
        {
            this.listeners.add( listener );
        }

        public void removeSelectionChangedListener( final ISelectionChangedListener listener )
        {
            this.listeners.remove( listener );
        }
        
        private void handleSelectionChangedEvent( final ISelection selection )
        {
            final SelectionChangedEvent event = new SelectionChangedEvent( this, selection );
            
            for( ISelectionChangedListener listener : this.listeners )
            {
                listener.selectionChanged( event );
            }
        }
    }
    
    public final class TableRow
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
                return getPart().getImageCache().getImage( this.imageService.image(), this.element.validate().severity() );
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
