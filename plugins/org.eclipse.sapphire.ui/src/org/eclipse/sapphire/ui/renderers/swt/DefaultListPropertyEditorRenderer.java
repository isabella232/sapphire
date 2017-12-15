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

package org.eclipse.sapphire.ui.renderers.swt;

import static org.eclipse.sapphire.ui.SapphirePropertyEditor.DATA_BINDING;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_COLUMN_WIDTHS;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_EXPAND_VERTICALLY;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_READ_ONLY;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_SHOW_HEADER;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_SHOW_LABEL;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_SHOW_LABEL_ABOVE;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.RELATED_CONTROLS;
import static org.eclipse.sapphire.ui.util.SwtUtil.gd;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdfill;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdhfill;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdhhint;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdhindent;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdvfill;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdwhint;
import static org.eclipse.sapphire.ui.util.SwtUtil.glayout;
import static org.eclipse.sapphire.ui.util.SwtUtil.glspacing;
import static org.eclipse.sapphire.ui.util.SwtUtil.hspan;
import static org.eclipse.sapphire.ui.util.SwtUtil.valign;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.EnumValueType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.IRemovable;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementListener;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.FixedOrderList;
import org.eclipse.sapphire.modeling.serialization.ValueSerializationService;
import org.eclipse.sapphire.modeling.util.internal.MiscUtil;
import org.eclipse.sapphire.ui.SapphireCommands;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.sapphire.ui.SapphirePropertyEditor;
import org.eclipse.sapphire.ui.SapphirePropertyEditor.ChildPropertyHelper;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.actions.Action;
import org.eclipse.sapphire.ui.actions.ActionGroup;
import org.eclipse.sapphire.ui.actions.ActionsCommandBridge;
import org.eclipse.sapphire.ui.actions.ActionsRenderer;
import org.eclipse.sapphire.ui.assist.BrowseHandler;
import org.eclipse.sapphire.ui.assist.JumpHandler;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.def.ISapphirePartDef;
import org.eclipse.sapphire.ui.internal.EnhancedComposite;
import org.eclipse.sapphire.ui.internal.ReadOnlyComboBoxCellEditor;
import org.eclipse.sapphire.ui.internal.binding.AbstractBinding;
import org.eclipse.sapphire.ui.util.HyperlinkTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class DefaultListPropertyEditorRenderer

    extends PropertyEditorRenderer
    
{
    public static final String DATA_SELECTION_PROVIDER = "selection.provider";
    
    private PropertyEditorAssistDecorator decorator;
    private boolean exposeAddAction;
    private boolean exposeDeleteAction;
    private Table table;
    private TableViewer tableViewer;
    private SelectionProvider selectionProvider;
    private Runnable refreshOperation;
    private ModelElementListener listElementListener;
    
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
        final IModelElement element = part.getModelElement();
        final ListProperty property = (ListProperty) part.getProperty();
        final boolean isReadOnly = ( property.isReadOnly() || part.getRenderingHint( HINT_READ_ONLY, false ) );

        final boolean showLabelAbove = part.getRenderingHint( HINT_SHOW_LABEL_ABOVE, false );
        final boolean showLabelInline = part.getRenderingHint( HINT_SHOW_LABEL, ( suppressLabel ? false : ! showLabelAbove ) );
        final int leftMargin = ( ignoreLeftMarginHint ? 0 : part.getLeftMarginHint() );
        final boolean showHeader = part.getRenderingHint( HINT_SHOW_HEADER, true );
        
        final Label label;

        if( showLabelInline || showLabelAbove )
        {
            final String labelText = property.getLabel( false, CapitalizationType.FIRST_WORD_ONLY, true ) + ":";
            label = new Label( parent, SWT.NONE );
            label.setLayoutData( gdhindent( hspan( valign( gd(), SWT.TOP ), showLabelAbove ? 2 : 1 ), leftMargin + 9 ) );
            label.setText( labelText );
            this.context.adapt( label );
        }
        else
        {
            label = null;
        }
        
        final boolean expandVertically = part.getRenderingHint( HINT_EXPAND_VERTICALLY, false );
        final int heightHint = part.getRenderingHint( ISapphirePartDef.HINT_HEIGHT, 10 ) * 15;
        
        GridData gd = ( expandVertically ? gdfill() : gdhhint( gdhfill(), heightHint ) );
        gd = gdhindent( hspan( gd, showLabelInline ? 1 : 2 ), showLabelInline ? 0 : leftMargin );
        
        final Composite tableComposite = new EnhancedComposite( parent, SWT.NONE );
        tableComposite.setLayoutData( gd );
        tableComposite.setLayout( glayout( ( isReadOnly ? 1 : 2 ), 0, 0, 0, 0 ) );
        this.context.adapt( tableComposite );
        
        final Composite innerComposite;
        
        if( this.decorator == null )
        {
            innerComposite = new Composite( tableComposite, SWT.NULL );
            innerComposite.setLayoutData( gdfill() );
            innerComposite.setLayout( glspacing( glayout( 2, 0, 0 ), 2 ) );
            this.context.adapt( innerComposite );
            
            this.decorator = new PropertyEditorAssistDecorator( part, this.context, innerComposite );
            this.decorator.getControl().setLayoutData( valign( gd(), SWT.TOP ) );

            this.decorator.addEditorControl( innerComposite );
        }
        else
        {
            innerComposite = tableComposite;
        }
        
        this.decorator.addEditorControl( tableComposite );
        
        final List<ChildPropertyHelper> columnProperties = new ArrayList<ChildPropertyHelper>();
        
        for( ChildPropertyHelper childProperty : part.getChildProperties() )
        {
            if( childProperty.getProperty() instanceof ValueProperty )
            {
                columnProperties.add( childProperty );
            }
        }
        
        final boolean singleColumnTable = ( columnProperties.size() == 1 );
        final boolean renderAsList = ( singleColumnTable && ! showHeader && ! columnProperties.get( 0 ).getProperty().isOfType( Boolean.class ) );
        
        final Composite tableParentComposite;
        final TableColumnLayout tableColumnLayout;
        
        if( renderAsList )
        {
            tableParentComposite = innerComposite;
            tableColumnLayout = null;
        }
        else
        {
            // Setting the whint in the following code is a hacky workaround for the problem
            // tracked by the following JFace bug:
            //
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=215997
            //
            
            tableParentComposite = new Composite( innerComposite, SWT.NULL );
            tableParentComposite.setLayoutData( gdwhint( gdfill(), 1 ) );
            tableColumnLayout = new TableColumnLayout();
            tableParentComposite.setLayout( tableColumnLayout );
        }
        
        this.tableViewer = new TableViewer( tableParentComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI );
        this.table = this.tableViewer.getTable();
        this.table.setLayoutData( renderAsList ? gdwhint( gdfill(), 200 ) : null );
        this.table.setData( SapphirePropertyEditor.DATA_ASSIST_DECORATOR, this.decorator );
        this.context.adapt( this.table );
        this.decorator.addEditorControl( this.table );
        
        final List<Control> relatedControls = new ArrayList<Control>();
        this.table.setData( RELATED_CONTROLS, relatedControls );
        
        if( label != null )
        {
            relatedControls.add( label );
        }
        
        SapphireCommands.configurePropertyEditorContext( this.table );
        
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
        
        final List<ColumnHandler> columnHandlers = new ArrayList<ColumnHandler>();
        
        final ColumnViewerEditorActivationStrategy activationStrategy = new ColumnViewerEditorActivationStrategy( this.tableViewer ) 
        {
            protected boolean isEditorActivationEvent( final ColumnViewerEditorActivationEvent event ) 
            {
                final int columnIndex = ( (ViewerCell) event.getSource() ).getColumnIndex();
                final ColumnHandler columnHandler = columnHandlers.get( columnIndex );
                return columnHandler.isEditorActivationEvent( event );
            }
        };
        
        TableViewerEditor.create( this.tableViewer, null, activationStrategy, ColumnViewerEditor.DEFAULT );

        this.table.setHeaderVisible( showHeader );
        //this.table.setLinesVisible( showHeader && ! singleColumnTable );
        
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
                }
                finally
                {
                    this.running = false;
                }
            }
        };
        
        this.binding = new AbstractBinding( element, property, this.context, this.table )
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
        
        final List<ActionGroup> actions = new ArrayList<ActionGroup>();
        
        if( renderAsList )
        {
            // Treat single column tables differently. For a single column table, we want
            // a list-like behavior. The header is to be hidden and the column is to use
            // as much width as it needs (growing automatically). There should also not
            // be a vertical column separator at the end of the column (only an issue on
            // some operating systems, such as Vista). 
            
            // To achieve this, we have to create a table with no columns. This triggers
            // a special mode in SWT. Not having a column object forces the use of older 
            // TableViewer API for managing label providers and cell modification. 
            // Unfortunate and ugly, but true.
            
            final ChildPropertyHelper memberPropertyHelper = columnProperties.get( 0 );
            final ValueProperty memberProperty = (ValueProperty) memberPropertyHelper.getProperty();
            
            final ColumnHandler columnHandler = createColumnHandler( columnHandlers, memberPropertyHelper, true );
            
            final ITableLabelProvider labelProvider = new TableLabelProvider( (ColumnLabelProvider) columnHandler.getLabelProvider() );
            this.tableViewer.setLabelProvider( labelProvider );
            
            if( ! memberProperty.isReadOnly() )
            {
                final AbstractColumnEditingSupport editingSupport = columnHandler.getEditingSupport(); 
                
                this.tableViewer.setColumnProperties( new String[] { "a" } );
                
                final ICellModifier cellModifier = new ICellModifier()
                {
                    public boolean canModify( final Object element,
                                              final String property )
                    {
                        return editingSupport.canEdit( element );
                    }
    
                    public Object getValue( final Object element,
                                            final String property )
                    {
                        return editingSupport.getValue( element );
                    }
    
                    public void modify( final Object element,
                                        final String property,
                                        final Object value )
                    {
                        final TableItem tableItem = (TableItem) element;
                        editingSupport.setValue( tableItem.getData(), value );
                    }
                };
                
                this.tableViewer.setCellModifier( cellModifier );
            
                this.tableViewer.addSelectionChangedListener
                (
                    new ISelectionChangedListener()
                    {
                        public void selectionChanged( final SelectionChangedEvent event )
                        {
                            final IStructuredSelection ssel = (IStructuredSelection) DefaultListPropertyEditorRenderer.this.tableViewer.getSelection();
                            final Object sel = ssel.getFirstElement();
                            
                            if( sel != null )
                            {
                                final CellEditor cellEditor = editingSupport.getCellEditor( sel );
                                DefaultListPropertyEditorRenderer.this.tableViewer.setCellEditors( new CellEditor[] { cellEditor } );
                            }
                        }
                    }
                );
            }
        }
        else
        {
            boolean showImages = true;
            
            for( ModelElementType modelElementType : property.getAllPossibleTypes() )
            {
                if( modelElementType.getAnnotation( org.eclipse.sapphire.modeling.annotations.Image.class ) == null )
                {
                    showImages = false;
                    break;
                }
            }
            
            final String columnWidthsHint = part.getRenderingHint( HINT_COLUMN_WIDTHS, "" );
            final StringTokenizer columnWidthsHintTokenizer = new StringTokenizer( columnWidthsHint, "," );
            
            for( final ChildPropertyHelper memberPropertyHelper : columnProperties )
            {
                final ValueProperty memberProperty = (ValueProperty) memberPropertyHelper.getProperty();
                
                final TableViewerColumn col2 = new TableViewerColumn( this.tableViewer, SWT.NONE );
                col2.getColumn().setText( memberProperty.getLabel( false, CapitalizationType.TITLE_STYLE, false ) );
                
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
                
                final ColumnHandler columnHandler = createColumnHandler( columnHandlers, memberPropertyHelper, showImages );
                
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
                            
                            for( ActionGroup group : actions )
                            {
                                for( Action action : group.getActions() )
                                {
                                    action.notifyChangeListeners();
                                }
                            }
                        }
                    }
                );
            }
        }
        
        this.listElementListener = new ModelElementListener()
        {
            @Override
            public void propertyChanged( final ModelPropertyChangeEvent event )
            {
                handleListElementChangedEvent( event );
            }
        };
        
        attachListElementListener();
        
        final IStructuredContentProvider contentProvider = new IStructuredContentProvider()
        {
            public Object[] getElements( final Object inputElement )
            {
                final ModelElementList<IModelElement> list = getList();

                if( list != null )
                {
                    return list.toArray();
                }
                else
                {
                    return new Object[ 0 ];
                }
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
        
        this.tableViewer.setContentProvider( contentProvider );
        this.tableViewer.setInput( contentProvider );
        
        if( ! isReadOnly )
        {
            Action addAction = null;
            Action deleteAction = null;
            
            if( this.exposeAddAction )
            {
                final Action.Listener addActionListener = new Action.Listener()
                {
                    @Override
                    public void handleActionExecuted( final Action action, final Object result )
                    {
                        if( DefaultListPropertyEditorRenderer.this.table.isDisposed() )
                        {
                            return;
                        }
                        
                        final IModelElement newListElement = (IModelElement) result;
    
                        if( newListElement != null )
                        {
                            DefaultListPropertyEditorRenderer.this.refreshOperation.run();
                            DefaultListPropertyEditorRenderer.this.tableViewer.setSelection( new StructuredSelection( newListElement ), true );
                            DefaultListPropertyEditorRenderer.this.tableViewer.editElement( newListElement, 0 );
                            DefaultListPropertyEditorRenderer.this.table.notifyListeners( SWT.Selection, null );
                        }
                    }
                };
                
                addAction = new AddAction( addActionListener );
                addAction.setPart( part );
            }
            
            if( this.exposeDeleteAction )
            {
                deleteAction = new DeleteAction();
                deleteAction.setPart( part );
            }

            if( addAction != null || deleteAction != null )
            {
                final ActionGroup addDeleteActionGroup = new ActionGroup();
                actions.add( addDeleteActionGroup );

                if( addAction != null )
                {
                    addDeleteActionGroup.addAction( addAction );
                }
                
                if( deleteAction != null )
                {
                    addDeleteActionGroup.addAction( deleteAction );
                }
            }
            
            if( ! property.hasAnnotation( FixedOrderList.class ) )
            {
                final ActionGroup moveActionGroup = new ActionGroup();
                actions.add( moveActionGroup );
                
                final Action.Listener moveActionListener = new Action.Listener()
                {
                    public void handleActionExecuted( final Action action, final Object result )
                    {
                        DefaultListPropertyEditorRenderer.this.refreshOperation.run();
                    }
                };
                
                final Action moveUpAction = new MoveUpAction( moveActionListener );
                moveUpAction.setPart( part );
                moveActionGroup.addAction( moveUpAction );
                
                final Action moveDownAction = new MoveDownAction( moveActionListener );
                moveDownAction.setPart( part );
                moveActionGroup.addAction( moveDownAction );
            }
            
            final ToolBar toolbar = new ToolBar( tableComposite, SWT.FLAT | SWT.VERTICAL );
            toolbar.setLayoutData( gdvfill() );
            this.context.adapt( toolbar );
            this.decorator.addEditorControl( toolbar );
            ActionsRenderer.fillToolBar( toolbar, actions );
            
            final ActionsCommandBridge actionsCommandBridge = new ActionsCommandBridge( this.table );
            actionsCommandBridge.setActions( actions );
            
            final Menu menu = new Menu( this.table );
            this.table.setMenu( menu );
            ActionsRenderer.fillMenu( menu, actions );
        }
        
        final HyperlinkTable hyperlinkTable = new HyperlinkTable( this.table );
        
        hyperlinkTable.setController
        (
            new HyperlinkTable.Controller()
            {
                @Override
                public boolean isHyperlinkEnabled( final TableItem item,
                                                   final int column )
                {
                    final IModelElement element = (IModelElement) item.getData();
                    final ChildPropertyHelper propertyHelper = columnProperties.get( column );
                    final JumpHandler jumpHandler = propertyHelper.createJumpHandler( element );
                    
                    if( jumpHandler != null )
                    {
                        return jumpHandler.canLocateJumpTarget( part, DefaultListPropertyEditorRenderer.this.context, 
                                                                element, (ValueProperty) propertyHelper.getProperty() );
                    }
                    else
                    {
                        return false;
                    }
                }

                @Override
                public void handleHyperlinkEvent( final TableItem item,
                                                  final int column )
                {
                    final IModelElement element = (IModelElement) item.getData();
                    final ChildPropertyHelper propertyHelper = columnProperties.get( column );
                    final JumpHandler jumpHandler = propertyHelper.createJumpHandler( element );
                    
                    if( jumpHandler != null )
                    {
                        jumpHandler.jump( part, DefaultListPropertyEditorRenderer.this.context, 
                                          element, (ValueProperty) propertyHelper.getProperty() );
                    }
                }
            }
        );
        
        addControl( this.table );
        
        return this.table;
    }
    
    @Override
    public final ListProperty getProperty()
    {
        return (ListProperty) super.getProperty();
    }
    
    @SuppressWarnings("unchecked")
    public final ModelElementList<IModelElement> getList()
    {
        final IModelElement modelElement = getModelElement();
        
        if( modelElement != null )
        {
            return (ModelElementList<IModelElement>) getProperty().invokeGetterMethod( modelElement );
        }
        
        return null;
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
        DefaultListPropertyEditorRenderer.this.refreshOperation.run();
        attachListElementListener();
    }
    
    protected void handleListElementChangedEvent( final ModelPropertyChangeEvent event )
    {
        this.table.getDisplay().asyncExec
        (
            new Runnable()
            {
                public void run()
                {
                    DefaultListPropertyEditorRenderer.this.tableViewer.update( event.getModelElement(), null );
                    
                    // Cause the overall list editor decorator to be updated.
                    
                    DefaultListPropertyEditorRenderer.this.binding.updateTargetAttributes();
                }
            }
        );
    }
    
    protected void handleTableFocusGainedEvent()
    {
        if( this.tableViewer.getSelection().isEmpty() && this.table.getItemCount() > 0 )
        {
            final IModelElement firstItem = (IModelElement) this.table.getItem( 0 ).getData();
            this.tableViewer.setSelection( new StructuredSelection( firstItem ) );
        }
    }
    
    @Override
    protected void handleFocusReceivedEvent()
    {
        this.table.setFocus();
    }

    @Override
    protected void handleDisposeEvent()
    {
        final ModelElementList<IModelElement> list = getList();

        if( list != null )
        {
            for( IModelElement entry : list )
            {
                entry.removeListener( this.listElementListener );
            }
        }
    }
    
    private void attachListElementListener()
    {
        final ModelElementList<IModelElement> list = getList();
        
        if( list != null )
        {
            for( IModelElement entry : list )
            {
                entry.addListener( this.listElementListener );
            }
        }
    }
    
    private ColumnHandler createColumnHandler( final List<ColumnHandler> allColumnHandlers,
                                               final ChildPropertyHelper childPropertyHelper,
                                               final boolean showImages )
    {
        final ModelProperty property = childPropertyHelper.getProperty();
        final ColumnHandler columnHandler;
        
        if( property.isOfType( Boolean.class ) )
        {
            columnHandler = new BooleanPropertyColumnHandler( this.context, this.tableViewer, this.selectionProvider, getPart(), 
                                                              allColumnHandlers, childPropertyHelper, showImages );
        }
        else if( property.isOfType( Enum.class ) )
        {
            columnHandler = new EnumPropertyColumnHandler( this.context, this.tableViewer, this.selectionProvider, getPart(), 
                                                           allColumnHandlers, childPropertyHelper, showImages );
        }
        else
        {
            columnHandler = new ColumnHandler( this.context, this.tableViewer, this.selectionProvider, getPart(), 
                                               allColumnHandlers, childPropertyHelper, showImages );
        }
        
        allColumnHandlers.add( columnHandler );
        
        return columnHandler;
    }

    public static final class Factory
    
        extends PropertyEditorRendererFactory
        
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

    private static final class TableLabelProvider
    
        implements ITableLabelProvider, ITableColorProvider
        
    {
        private ColumnLabelProvider columnLabelProvider;
        
        public TableLabelProvider( final ColumnLabelProvider columnLabelProvider )
        {
            this.columnLabelProvider = columnLabelProvider;
        
        }
        public String getColumnText( final Object element,
                                     final int columnIndex )
        {
            return this.columnLabelProvider.getText( element );
        }

        public Image getColumnImage( final Object element,
                                     final int columnIndex )
        {
            return this.columnLabelProvider.getImage( element );
        }

        public Color getForeground( final Object element,
                                    final int columnIndex )
        {
            return this.columnLabelProvider.getForeground( element );
        }

        public Color getBackground( final Object element,
                                    final int columnIndex )
        {
            return this.columnLabelProvider.getBackground( element );
        }

        public boolean isLabelProperty( final Object element,
                                        final String property )
        {
            return true;
        }

        public void addListener( final ILabelProviderListener listener )
        {
        }

        public void removeListener( final ILabelProviderListener listener )
        {
        }
        
        public void dispose()
        {
        }
    }

    private static class DefaultColumnLabelProvider
    
        extends ColumnLabelProvider
        
    {
        private final ColumnHandler columnHandler;
        
        public DefaultColumnLabelProvider( final ColumnHandler columnHandler )
        {
            this.columnHandler = columnHandler;
        }
    
        @Override
        public String getText( final Object element )
        {
            String str = null;

            final IModelElement modelElement = (IModelElement) element;
            
            if( modelElement.isPropertyEnabled( this.columnHandler.getProperty() ) )
            {
                str = getTextInternal( this.columnHandler.getPropertyValue( modelElement ) );
            }
            
            if( str == null )
            {
                if( this.columnHandler.isEmptyTextLabelDesired( modelElement ) )
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
        
        protected String getTextInternal( final Value<?> value )
        {
            return value.getText();
        }
        
        @Override
        public Image getImage( final Object element )
        {
            if( this.columnHandler.isElementImageDesired() )
            {
                final IModelElement modelElement = (IModelElement) element;
                final SapphireImageCache imageCache = this.columnHandler.getListPropertyEditor().getImageCache();
                return imageCache.getImage( modelElement, modelElement.validate().getSeverity() );
            }
            else
            {
                return null;
            }
        }
    
        @Override
        public Color getForeground( final Object element )
        {
            final Value<?> value = (Value<?>) this.columnHandler.getProperty().invokeGetterMethod( element );
            
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
    
    private static abstract class AbstractColumnEditingSupport
    
        extends EditingSupport
        
    {
        protected final ColumnHandler columnHandler;
        
        public AbstractColumnEditingSupport( final ColumnHandler columnHandler )
        {
            super( columnHandler.getTableViewer() );
            
            this.columnHandler = columnHandler;
        }

        @Override
        public boolean canEdit( final Object element )
        {
            final ValueProperty property = this.columnHandler.getProperty();
            return ( ! property.isReadOnly() && ( (IModelElement) element ).isPropertyEnabled( property ) );
        }
        
        public abstract CellEditor getCellEditor( Object element );
        public abstract Object getValue( Object element );
        public abstract void setValue( Object element, Object value );

    }
    
    private static class ColumnHandler
    {
        protected final SapphireRenderingContext context;
        protected final Table table;
        protected final TableViewer tableViewer;
        protected final SelectionProvider selectionProvider;
        protected final SapphirePropertyEditor listPropertyEditor;
        protected final List<ColumnHandler> allColumnHandlers;
        protected final ChildPropertyHelper childPropertyHelper;
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
                              final ChildPropertyHelper childPropertyHelper,
                              final boolean showElementImage )
        {
            this.context = context;
            this.table = tableViewer.getTable();
            this.tableViewer = tableViewer;
            this.selectionProvider = selectionProvider;
            this.listPropertyEditor = listPropertyEditor;
            this.allColumnHandlers = allColumnHandlers;
            this.childPropertyHelper = childPropertyHelper;
            this.property = (ValueProperty) childPropertyHelper.getProperty();
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
            return (Value<?>) this.property.invokeGetterMethod( element );
        }
        
        public final void setPropertyValue( final IModelElement element,
                                            final String value )
        {
            this.property.invokeSetterMethod( element, value );            
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
                private BrowseableTextCellEditor cellEditor;
    
                @Override
                public CellEditor getCellEditor( final Object element )
                {
                    if( this.cellEditor == null )
                    {
                        final int style = ( getTable().getLinesVisible() ? SWT.NONE : SWT.BORDER );
                        this.cellEditor = new BrowseableTextCellEditor( getListPropertyEditor(), getTableViewer(), getSelectionProvider(), style );
    
                        if( isElementImageDesired() )
                        {
                            this.cellEditor.setHorizonalIndent( 3 );
                        }
                    }
                        
                    final List<BrowseHandler> browseHandlers 
                        = ColumnHandler.this.childPropertyHelper.createBrowseHandlers( (IModelElement) element );
                    
                    this.cellEditor.setBrowseHandlers( browseHandlers );
                    this.cellEditor.setContext( getContext() );
    
                    return this.cellEditor;
                }
    
                @Override
                public Object getValue( final Object element )
                {
                    return getPropertyValue( (IModelElement) element );
                }
    
                @Override
                public void setValue( final Object element,
                                      final Object value )
                {
                    setPropertyValue( (IModelElement) element, (String) value );
                }
            };
        }
        
        public boolean isEditorActivationEvent( final ColumnViewerEditorActivationEvent event ) 
        {
            return event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION ||
                   event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
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
    
    private static final class EnumPropertyColumnHandler
    
        extends ColumnHandler
        
    {
        private final EnumValueType annotatedEnumeration;
        private final Enum<?>[] enumValues;
        
        public EnumPropertyColumnHandler( final SapphireRenderingContext context,
                                          final TableViewer tableViewer,
                                          final SelectionProvider selectionProvider,
                                          final SapphirePropertyEditor listPropertyEditor,
                                          final List<ColumnHandler> allColumnHandlers,
                                          final ChildPropertyHelper childPropertyHelper,
                                          final boolean showElementImage )
        {
            super( context, tableViewer, selectionProvider, listPropertyEditor, allColumnHandlers, childPropertyHelper, showElementImage );
            
            this.annotatedEnumeration = new EnumValueType( childPropertyHelper.getProperty().getTypeClass() );
            this.enumValues = this.annotatedEnumeration.getItems();
        }
        
        @Override
        protected CellLabelProvider createLabelProvider()
        {
            return new DefaultColumnLabelProvider( this )
            {
                @Override
                protected String getTextInternal( final Value<?> value )
                {
                    String str = null;
                    
                    if( EnumPropertyColumnHandler.this.annotatedEnumeration != null )
                    {
                        final Enum<?> enumItem = (Enum<?>) value.getContent( true );
                        
                        if( enumItem != null )
                        {
                            str = EnumPropertyColumnHandler.this.annotatedEnumeration.getLabel( enumItem, false, CapitalizationType.FIRST_WORD_ONLY, false );
                        }
                    }
                    
                    if( str == null )
                    {
                        str = value.getText( true );
                    }
                    
                    return str;
                }
            };
        }
        
        @Override
        protected AbstractColumnEditingSupport createEditingSupport()
        {
            return new AbstractColumnEditingSupport( this )
            {
                private ReadOnlyComboBoxCellEditor cellEditor;
                private List<String> cellEditorItems;

                @Override
                public CellEditor getCellEditor( final Object element )
                {
                    if( this.cellEditor == null )
                    {
                        this.cellEditor = new ReadOnlyComboBoxCellEditor( getTable(), new String[ 0 ], SWT.DROP_DOWN | SWT.READ_ONLY );
                        this.cellEditorItems = new ArrayList<String>();
                    }
                    
                    final EnumValueType annotatedEnumeration = EnumPropertyColumnHandler.this.annotatedEnumeration;
                    final Enum<?>[] enumValues = EnumPropertyColumnHandler.this.enumValues;
                    
                    final Value<?> value = (Value<?>) getProperty().invokeGetterMethod( element );
                    final String stringValue = value.getText( false );
                    
                    boolean needExtraEntry = false;
                    
                    if( stringValue != null && value.getContent( false ) == null )
                    {
                        needExtraEntry = true;
                    }
                    
                    final String[] items = new String[ enumValues.length + ( needExtraEntry ? 1 : 0 ) ];
                    this.cellEditorItems.clear();
                    
                    final IModelElement modelElement = (IModelElement) element;
                    
                    for( int i = 0; i < enumValues.length; i++ )
                    {
                        final Enum<?> enumValue = enumValues[ i ];
                        final String enumValueText = modelElement.service( ValueSerializationService.class ).encode( getProperty(), enumValue );
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
                public Object getValue( final Object element )
                {
                    String str = getPropertyValue( (IModelElement) element ).getText( false );
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
                public void setValue( final Object element,
                                      final Object value )
                {
                    final int index = (Integer) value;
                    final String str = ( index == -1 ? null : this.cellEditorItems.get( index ) );
                    setPropertyValue( (IModelElement) element, str );
                }
            };
        }
    }
    
    private static final class BooleanPropertyColumnHandler
    
        extends ColumnHandler
        
    {
        private static final int CHECKBOX_IMAGE_WIDTH = 16;
        private static final int CHECKBOX_IMAGE_HEIGHT = 16;
        
        public BooleanPropertyColumnHandler( final SapphireRenderingContext context,
                                             final TableViewer tableViewer,
                                             final SelectionProvider selectionProvider,
                                             final SapphirePropertyEditor listPropertyEditor,
                                             final List<ColumnHandler> allColumnHandlers,
                                             final ChildPropertyHelper childPropertyHelper,
                                             final boolean showElementImage )
        {
            super( context, tableViewer, selectionProvider, listPropertyEditor, allColumnHandlers, childPropertyHelper, showElementImage );
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
                    final IModelElement element = (IModelElement) item.getData();
                    
                    if( element.isPropertyEnabled( getProperty() ) )
                    {
                        final boolean value = getPropertyValueAsBoolean( element );
                        
                        final Image image = getImageCache().getImage( value ? SapphireImageCache.OBJECT_CHECK_ON : SapphireImageCache.OBJECT_CHECK_OFF );
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
                
                public Object getValue( final Object element )
                {
                    final Value<Boolean> value = (Value<Boolean>) getPropertyValue( (IModelElement) element );
                    final Boolean val = value.getContent();
                    return ( val != null ? val : Boolean.FALSE );
                }

                @Override
                public void setValue( final Object element,
                                      final Object value )
                {
                    final String str = String.valueOf( ( (Boolean) value ).booleanValue() );
                    setPropertyValue( (IModelElement) element, str );
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
    
    private static final class TableSorter
    
        extends ViewerComparator
        
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
            int result = this.columnHandler.comparePropertyValues( (IModelElement) x, (IModelElement) y );
            
            if( this.direction == SWT.UP )
            {
                result = result * -1;
            }
            
            return result;
        }
    }
    
    private abstract class AbstractAction

        extends Action
        
    {
        private final ModelPropertyListener listPropertyListener;
        
        public AbstractAction()
        {
            this.listPropertyListener = new ModelPropertyListener()
            {
                @Override
                public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event )
                {
                    notifyChangeListeners();
                }
            };
            
            getModelElement().addListener( this.listPropertyListener, getProperty().getName() );
        }
        
        @Override
        public boolean isEnabled()
        {
            final IModelElement modelElement = getModelElement();
            
            if( modelElement != null )
            {
                if( modelElement.isPropertyEnabled( getProperty() ) )
                {
                    return true;
                }
            }
            
            return false;
        }

        @Override
        public void dispose()
        {
            super.dispose();
            
            getModelElement().removeListener( this.listPropertyListener, getProperty().getName() );
        }
    }

    private final class AddAction

        extends AbstractAction
        
    {
        public AddAction( final Action.Listener actionListener )
        {
            setLabel( Resources.addActionLabel );
            setImageDescriptor( SapphireImageCache.ACTION_ADD );
            addListener( actionListener );
            
            if( getProperty().getAllPossibleTypes().size() > 1 )
            {
                final ActionGroup mainAddActionGroup = new ActionGroup();
                addChildActionGroup( mainAddActionGroup );
                
                for( final ModelElementType memberType : getProperty().getAllPossibleTypes() )
                {
                    final TypeSpecificAddAction typeSpecificAddAction = new TypeSpecificAddAction( memberType );
                    typeSpecificAddAction.addListener( actionListener );
                    mainAddActionGroup.addAction( typeSpecificAddAction );
                }
            }
        }
        
        @Override
        protected Object run( final Shell shell )
        {
            if( ! getChildActionGroups().isEmpty() )
            {
                throw new UnsupportedOperationException();
            }
            else
            {
                return getList().addNewElement();
            }
        }
        
        private final class TypeSpecificAddAction
        
            extends Action
            
        {
            private final ModelElementType type;
            
            public TypeSpecificAddAction( final ModelElementType type )
            {
                this.type = type;
                
                ImageDescriptor typeSpecificAddImage 
                    = DefaultListPropertyEditorRenderer.this.getPart().getImageCache().getImageDescriptor( this.type );
                
                if( typeSpecificAddImage == null )
                {
                    typeSpecificAddImage = getImageDescriptor();
                }
    
                setLabel( this.type.getLabel( false, CapitalizationType.TITLE_STYLE, false ) );
                setImageDescriptor( typeSpecificAddImage );
            }
    
            @Override
            protected Object run( final Shell shell )
            {
                return getList().addNewElement( this.type );
            }
        }
    }
    
    private abstract class SelectionBasedAction

        extends AbstractAction
        
    {
        public SelectionBasedAction()
        {
            DefaultListPropertyEditorRenderer.this.tableViewer.addSelectionChangedListener
            (
                new ISelectionChangedListener()
                {
                    public void selectionChanged( final SelectionChangedEvent event )
                    {
                        notifyChangeListeners();
                    }
                }
            );
        }
        
        public IModelElement getSelectedElement()
        {
            final IStructuredSelection sel = (IStructuredSelection) DefaultListPropertyEditorRenderer.this.tableViewer.getSelection();
            
            if( sel == null )
            {
                return null;
            }
            else
            {
                return (IModelElement) sel.getFirstElement();
            }
        }
        
        public List<IModelElement> getSelectedElements()
        {
            final IStructuredSelection sel = (IStructuredSelection) DefaultListPropertyEditorRenderer.this.tableViewer.getSelection();
            final List<IModelElement> elements = new ArrayList<IModelElement>();
            
            if( sel != null )
            {
                for( Iterator<?> itr = sel.iterator(); itr.hasNext(); )
                {
                    elements.add( (IModelElement) itr.next() );
                }
            }
            
            return elements;
        }
    }
    
    private final class DeleteAction

        extends SelectionBasedAction
        
    {
        public DeleteAction()
        {
            setLabel( Resources.deleteActionLabel );
            setImageDescriptor( SapphireImageCache.ACTION_DELETE );
            setCommandId( "org.eclipse.ui.edit.delete" );
        }
        
        @Override
        protected final Object run( final Shell shell )
        {
            final IStructuredContentProvider contentProvider 
                = (IStructuredContentProvider) DefaultListPropertyEditorRenderer.this.tableViewer.getContentProvider();
            
            final List<IModelElement> elementsToDelete = getSelectedElements();
            IModelElement elementToSelectAfterDelete = null;
            boolean preferNextElement = true;

            for( Object element : contentProvider.getElements( null ) )
            {
                boolean toBeDeleted = false;
                
                for( IModelElement x : elementsToDelete )
                {
                    if( element == x )
                    {
                        toBeDeleted = true;
                        preferNextElement = true;
                        break;
                    }
                }
                
                if( ! toBeDeleted && preferNextElement )
                {
                    elementToSelectAfterDelete = (IModelElement) element;
                    preferNextElement = false;
                }
            }

            for( IModelElement element : elementsToDelete )
            {
                ( (IRemovable) element ).remove();
            }
            
            DefaultListPropertyEditorRenderer.this.refreshOperation.run();
            
            if( elementToSelectAfterDelete != null )
            {
                DefaultListPropertyEditorRenderer.this.tableViewer.setSelection( new StructuredSelection( elementToSelectAfterDelete ) );
            }
            
            return null;
        }
        
        public boolean isEnabled()
        {
            if( ! super.isEnabled() )
            {
                return false;
            }

            return ! getSelectedElements().isEmpty(); 
        }
    }

    private final class MoveUpAction

        extends SelectionBasedAction
        
    {
        public MoveUpAction( final Action.Listener actionListener )
        {
            setLabel( Resources.moveUpActionLabel );
            setImageDescriptor( SapphireImageCache.ACTION_MOVE_UP );
            setCommandId( SapphireCommands.COMMAND_MOVE_UP );
            addListener( actionListener );
        }
        
        public boolean isEnabled()
        {
            if( ! super.isEnabled() || getSelectedElements().size() != 1 || 
                DefaultListPropertyEditorRenderer.this.tableViewer.getComparator() != null  )
            {
                return false;
            }
            
            final IModelElement modelElement = getSelectedElement();
            return ( getList().indexOf( modelElement ) > 0 );
        }

        @Override
        protected final Object run( final Shell shell )
        {
            getList().moveUp( getSelectedElement() );
            return null;
        }
    }
    
    private final class MoveDownAction

        extends SelectionBasedAction
        
    {
        public MoveDownAction( final Action.Listener actionListener )
        {
            setLabel( Resources.moveDownActionLabel );
            setImageDescriptor( SapphireImageCache.ACTION_MOVE_DOWN );
            setCommandId( SapphireCommands.COMMAND_MOVE_DOWN );
            addListener( actionListener );
        }
        
        public boolean isEnabled()
        {
            if( ! super.isEnabled() || getSelectedElements().size() != 1 || 
                DefaultListPropertyEditorRenderer.this.tableViewer.getComparator() != null )
            {
                return false;
            }
            
            final IModelElement modelElement = getSelectedElement();
            final ModelElementList<?> list = getList();
            return ( list.indexOf( modelElement ) < ( list.size() - 1 ) );
        }
    
        @Override
        protected final Object run( final Shell shell )
        {
            getList().moveDown( getSelectedElement() );
            return null;
        }
    }
    
    public static final class SelectionProvider
    
        implements ISelectionProvider
        
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
    
    private static final class Resources
    
        extends NLS
    
    {
        public static String addActionLabel;
        public static String deleteActionLabel;
        public static String moveUpActionLabel;
        public static String moveDownActionLabel;
        public static String emptyRowIndicator;
        
        static
        {
            initializeMessages( DefaultListPropertyEditorRenderer.class.getName(), Resources.class );
        }
    }
    
}
