/******************************************************************************
 * Copyright (c) 2012 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [364098] Slush bucket property editor issue with case-insensitive possible values
 ******************************************************************************/

package org.eclipse.sapphire.ui.renderers.swt;

import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.swt.renderer.SwtUtil.makeTableSortable;
import static org.eclipse.sapphire.ui.swt.renderer.SwtUtil.suppressDashedTableEntryBorder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.NoDuplicates;
import org.eclipse.sapphire.services.PossibleTypesService;
import org.eclipse.sapphire.services.PossibleValuesService;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphirePropertyEditor;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerDef;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public final class SlushBucketPropertyEditor extends AbstractSlushBucketPropertyEditorRenderer
{
    private ModelElementType memberType;
    private ValueProperty memberProperty;
    private PossibleValuesService possibleValuesService;
    private Listener possibleValuesServiceListener;
    private TableViewer sourceTableViewer;
    private Table sourceTable;
    private MoveRightActionHandler moveRightActionHandler;
    
    public SlushBucketPropertyEditor( final SapphireRenderingContext context,
                                      final SapphirePropertyEditor part )
    {
        super( context, part );
        
        this.memberType = getProperty().getType();
        this.memberProperty = (ValueProperty) this.memberType.getProperties().get( 0 );
        this.possibleValuesService = part.getLocalModelElement().service( this.memberProperty, PossibleValuesService.class );
        
        setAddActionDesired( false );
    }

    public Control createSourceControl( final Composite parent )
    {
        final Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayout( glayout( 1, 0, 0 ) );
        
        // Setting the whint in the following code is a hacky workaround for the problem
        // tracked by the following JFace bug:
        //
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=215997
        //
        
        final Composite innerComposite = new Composite( composite, SWT.NONE );
        innerComposite.setLayoutData( gdwhint( gdfill(), 1 ) );
        
        final TableColumnLayout tableColumnLayout = new TableColumnLayout();
        innerComposite.setLayout( tableColumnLayout );
        
        this.sourceTableViewer = new TableViewer( innerComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI );
        this.sourceTable = this.sourceTableViewer.getTable();
        this.sourceTable.setHeaderVisible( true );
        
        final TableViewerColumn viewerColumn = new TableViewerColumn( this.sourceTableViewer, SWT.NONE );
        final TableColumn column = viewerColumn.getColumn();
        column.setText( this.memberProperty.getLabel( false, CapitalizationType.TITLE_STYLE, false ) );
        tableColumnLayout.setColumnData( column, new ColumnWeightData( 1, 100, true ) );
        
        final IStructuredContentProvider contentProvider = new IStructuredContentProvider()
        {
            public Object[] getElements( final Object inputElement )
            {
                if( SlushBucketPropertyEditor.this.possibleValuesService == null )
                {
                    return new Object[ 0 ];
                }
                
                final ModelElementList<IModelElement> list = getList();
                
                if( list == null )
                {
                    return new Object[ 0 ];
                }

                final Collection<String> allValues;
                
                try
                {
                    allValues = SlushBucketPropertyEditor.this.possibleValuesService.values();
                }
                catch( Exception e )
                {
                    SapphireUiFrameworkPlugin.log( e );
                    return new Object[ 0 ];
                }
                
                final Set<String> valuesToRemove = new HashSet<String>();
                
                for( IModelElement member : list )
                {
                    final String str = member.read( SlushBucketPropertyEditor.this.memberProperty ).getText();
                    
                    if( str != null )
                    {
                        valuesToRemove.add( str );
                    }
                }
                
                final Collection<String> values;
                
                if( SlushBucketPropertyEditor.this.possibleValuesService.isCaseSensitive() )
                {
                    values = new HashSet<String>( allValues );
                    values.removeAll( valuesToRemove );
                }
                else
                {
                    final Map<String,String> valuesLowerCaseToOriginalCase = new HashMap<String,String>();

                    for( String value : allValues )
                    {
                        valuesLowerCaseToOriginalCase.put( value.toLowerCase(), value );
                    }

                    for( String valueToRemove : valuesToRemove )
                    {
                        valuesLowerCaseToOriginalCase.remove( valueToRemove.toLowerCase() );
                    }
                    
                    values = valuesLowerCaseToOriginalCase.values();
                }

                return values.toArray();
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
        
        this.sourceTableViewer.setContentProvider( contentProvider );
        
        final ValueLabelProvider valueLabelProvider = new ValueLabelProvider( getPart(), this.memberProperty );
        
        final ColumnLabelProvider labelProvider = new ColumnLabelProvider()
        {
            @Override
            public String getText( final Object element )
            {
                return valueLabelProvider.getText( element );
            }
            
            @Override
            public Image getImage( final Object element )
            {
                return valueLabelProvider.getImage( element );
            }

            @Override
            public void dispose()
            {
                super.dispose();
                valueLabelProvider.dispose();
                
            }
        };
        
        viewerColumn.setLabelProvider( labelProvider );
        
        makeTableSortable( this.sourceTableViewer );
        suppressDashedTableEntryBorder( this.sourceTable );
        
        this.sourceTable.addMouseListener
        (
            new MouseAdapter()
            {
                public void mouseDoubleClick( final MouseEvent event )
                {
                    handleSourceTableDoubleClickEvent( event );
                }
            }
        );
        
        this.sourceTable.addFocusListener
        (
            new FocusAdapter()
            {
                @Override
                public void focusGained( final FocusEvent event )
                {
                    handleSourceTableFocusGainedEvent();
                }
            }
        );
        
        this.sourceTable.addKeyListener
        (
            new KeyAdapter()
            {
                @Override
                public void keyPressed( final KeyEvent event )
                {
                    if( event.character == ' ' )
                    {
                        handleSourceTableEnterKeyPressEvent();
                    }
                }
            }
        );
        
        this.possibleValuesServiceListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                SlushBucketPropertyEditor.this.sourceTableViewer.refresh();
            }
        };
        
        this.possibleValuesService.attach( this.possibleValuesServiceListener );
        
        this.sourceTableViewer.setInput( new Object() );
        
        addOnDisposeOperation
        (
            new Runnable()
            {
                public void run()
                {
                    if( SlushBucketPropertyEditor.this.possibleValuesService != null )
                    {
                        SlushBucketPropertyEditor.this.possibleValuesService.detach( SlushBucketPropertyEditor.this.possibleValuesServiceListener );
                    }
                }
            }
        );
        
        return composite;
    }

    @Override
    public SapphireActionHandler createMoveRightActionHandler()
    {
        this.moveRightActionHandler = new MoveRightActionHandler();
        
        final ISelectionChangedListener listener = new ISelectionChangedListener()
        {
            public void selectionChanged( final SelectionChangedEvent event )
            {
                final List<String> input = new ArrayList<String>();
                
                for( Iterator<?> itr = ( (IStructuredSelection) event.getSelection() ).iterator(); itr.hasNext(); )
                {
                    input.add( (String) itr.next() );
                }
                
                SlushBucketPropertyEditor.this.moveRightActionHandler.setInput( input );
            }
        };
        
        this.sourceTableViewer.addSelectionChangedListener( listener );
        
        return this.moveRightActionHandler;
    }

    @Override
    protected void handlePropertyChangedEvent()
    {
        super.handlePropertyChangedEvent();
        
        this.sourceTableViewer.refresh();
    }

    @Override
    protected void handleListElementChangedEvent( final ModelPropertyChangeEvent event )
    {
        super.handleListElementChangedEvent( event );
        
        this.sourceTableViewer.refresh();
    }
    
    @Override
    protected void handleTableFocusGainedEvent()
    {
        super.handleTableFocusGainedEvent();
        
        this.sourceTableViewer.setSelection( StructuredSelection.EMPTY );
    }
    
    private void handleSourceTableDoubleClickEvent( final MouseEvent event )
    {
        String doubleClickedItem = null;
        
        for( TableItem item : this.sourceTable.getItems() )
        {
            if( item.getBounds().contains( event.x, event.y ) )
            {
                doubleClickedItem = (String) item.getData();
                break;
            }
        }
        
        if( doubleClickedItem != null )
        {
            this.moveRightActionHandler.setInput( Collections.singleton( doubleClickedItem ) );
            this.moveRightActionHandler.execute( getUiContext() );
        }
    }
    
    private void handleSourceTableFocusGainedEvent()
    {
        setSelection( Collections.<IModelElement>emptyList() );
        
        if( this.sourceTableViewer.getSelection().isEmpty() && this.sourceTable.getItemCount() > 0 )
        {
            final String firstItem = (String) this.sourceTable.getItem( 0 ).getData();
            this.sourceTableViewer.setSelection( new StructuredSelection( firstItem ) );
        }
    }
    
    private void handleSourceTableEnterKeyPressEvent()
    {
        if( ! this.sourceTableViewer.getSelection().isEmpty() )
        {
            this.moveRightActionHandler.execute( getUiContext() );
        }
    }
    
    public static final class Factory extends PropertyEditorRendererFactory
    {
        @Override
        public boolean isApplicableTo( final SapphirePropertyEditor propertyEditorPart )
        {
            final ModelProperty property = propertyEditorPart.getProperty();
            
            if( property instanceof ListProperty )
            {
                final IModelElement element = propertyEditorPart.getLocalModelElement();
                final ListProperty listProperty = (ListProperty) property;
                
                if( element.service( listProperty, PossibleTypesService.class ).types().size() == 1 )
                {
                    final ModelElementType memberType = listProperty.getType();
                    final List<ModelProperty> properties = memberType.getProperties();
                    
                    if( properties.size() == 1 )
                    {
                        final ModelProperty memberProperty = properties.get( 0 );
                        
                        if( memberProperty instanceof ValueProperty &&
                            memberProperty.hasAnnotation( NoDuplicates.class ) &&
                            propertyEditorPart.getLocalModelElement().service( memberProperty, PossibleValuesService.class ) != null )
                        {
                            return true;
                        }
                    }
                }
            }
    
            return false;
        }
        
        @Override
        public PropertyEditorRenderer create( final SapphireRenderingContext context,
                                              final SapphirePropertyEditor part )
        {
            return new SlushBucketPropertyEditor( context, part );
        }
    }
    
    private final class MoveRightActionHandler extends SapphireActionHandler
    {
        private Collection<String> input = Collections.emptyList();
        
        @Override
        public void init( final SapphireAction action,
                          final ISapphireActionHandlerDef def )
        {
            super.init( action, def );
            setEnabled( false );
        }

        public void setInput( final Collection<String> input )
        {
            this.input = input;
            setEnabled( getList() != null && ! this.input.isEmpty() );
        }

        @Override
        protected Object run( final SapphireRenderingContext context )
        {
            final ModelElementList<IModelElement> list = getList();
            
            if( list != null )
            {
                final List<IModelElement> items = new ArrayList<IModelElement>();
                
                for( String str : this.input )
                {
                    final IModelElement item = list.addNewElement();
                    item.write( SlushBucketPropertyEditor.this.memberProperty, str );
                    items.add( item );
                }
                
                setSelection( items );
                setFocusOnTable();
            }
            
            return null;
        }
    };

}
