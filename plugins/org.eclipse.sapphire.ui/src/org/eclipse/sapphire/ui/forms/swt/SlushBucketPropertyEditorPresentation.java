/******************************************************************************
 * Copyright (c) 2014 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [364098] Slush bucket property editor issue with case-insensitive possible values
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt;

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.forms.swt.SwtUtil.makeTableSortable;
import static org.eclipse.sapphire.ui.forms.swt.SwtUtil.suppressDashedTableEntryBorder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

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
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Status.Severity;
import org.eclipse.sapphire.modeling.annotations.NoDuplicates;
import org.eclipse.sapphire.services.PossibleTypesService;
import org.eclipse.sapphire.services.PossibleValuesService;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.PropertyEditorDef;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.swt.internal.ValueLabelProvider;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.sapphire.util.SetFactory;
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
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public final class SlushBucketPropertyEditorPresentation extends AbstractSlushBucketPropertyEditorPresentation
{
    private ElementType memberType;
    private ValueProperty memberProperty;
    private PossibleValuesService possibleValuesService;
    private Listener possibleValuesServiceListener;
    private TableViewer sourceTableViewer;
    private Table sourceTable;
    private MoveRightActionHandler moveRightActionHandler;
    
    public SlushBucketPropertyEditorPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
        
        final Property property = property();
        
        this.memberType = property.definition().getType();
        this.memberProperty = (ValueProperty) this.memberType.properties().first();
        this.possibleValuesService = property.service( PossibleValuesService.class );
        
        final Status.Severity invalidValueSeverity = this.possibleValuesService.getInvalidValueSeverity( null );
        
        setAddActionDesired( invalidValueSeverity == Severity.ERROR ? false : true );
    }

    public Control createSourceControl( final Composite parent )
    {
        final PropertyEditorPart part = part();
        
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
        
        final boolean showHeader = part.getRenderingHint( PropertyEditorDef.HINT_SHOW_HEADER, true );
        this.sourceTable.setHeaderVisible( showHeader );
        
        final TableViewerColumn viewerColumn = new TableViewerColumn( this.sourceTableViewer, SWT.NONE );
        final TableColumn column = viewerColumn.getColumn();
        column.setText( this.memberProperty.getLabel( false, CapitalizationType.TITLE_STYLE, false ) );
        tableColumnLayout.setColumnData( column, new ColumnWeightData( 1, 100, true ) );
        
        final IStructuredContentProvider contentProvider = new IStructuredContentProvider()
        {
            public Object[] getElements( final Object inputElement )
            {
                if( SlushBucketPropertyEditorPresentation.this.possibleValuesService == null )
                {
                    return new Object[ 0 ];
                }
                
                final ElementList<?> list = list();
                
                if( list == null )
                {
                    return new Object[ 0 ];
                }

                final Set<String> possibleValues = SlushBucketPropertyEditorPresentation.this.possibleValuesService.values();
                final SetFactory<String> unusedPossibleValues = SetFactory.<String>start().add( possibleValues );
                
                for( Element member : list )
                {
                    unusedPossibleValues.remove( member.property( SlushBucketPropertyEditorPresentation.this.memberProperty ).text() );
                }
                
                return unusedPossibleValues.result().toArray();
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
        
        final ValueLabelProvider valueLabelProvider = new ValueLabelProvider( part, this.memberProperty );
        
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
        
        makeTableSortable
        (
            this.sourceTableViewer,
            Collections.<TableColumn,Comparator<Object>>emptyMap(),
            this.possibleValuesService.ordered() ? null : column 
        );
        
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
                SlushBucketPropertyEditorPresentation.this.sourceTableViewer.refresh();
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
                    if( SlushBucketPropertyEditorPresentation.this.possibleValuesService != null )
                    {
                        SlushBucketPropertyEditorPresentation.this.possibleValuesService.detach( SlushBucketPropertyEditorPresentation.this.possibleValuesServiceListener );
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
                
                SlushBucketPropertyEditorPresentation.this.moveRightActionHandler.setInput( input );
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
    protected void handleChildPropertyEvent( final PropertyContentEvent event )
    {
        super.handleChildPropertyEvent( event );
        
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
            this.moveRightActionHandler.execute( this );
        }
    }
    
    private void handleSourceTableFocusGainedEvent()
    {
        setSelectedElements( Collections.<Element>emptyList() );
        
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
            this.moveRightActionHandler.execute( this );
        }
    }
    
    public static final class Factory extends PropertyEditorPresentationFactory
    {
        @Override
        public PropertyEditorPresentation create( final PropertyEditorPart part, final SwtPresentation parent, final Composite composite )
        {
            final Property property = part.property();
            
            if( property.definition() instanceof ListProperty &&
                property.service( PossibleValuesService.class ) != null &&
                property.service( PossibleTypesService.class ).types().size() == 1 )
            {
                final SortedSet<PropertyDef> properties = property.definition().getType().properties();
                
                if( properties.size() == 1 )
                {
                    final PropertyDef memberProperty = properties.first();
                    
                    if( memberProperty instanceof ValueProperty && memberProperty.hasAnnotation( NoDuplicates.class ) )
                    {
                        return new SlushBucketPropertyEditorPresentation( part, parent, composite );
                    }
                }
            }
    
            return null;
        }
    }
    
    private final class MoveRightActionHandler extends SapphireActionHandler
    {
        private Collection<String> input = Collections.emptyList();
        
        @Override
        public void init( final SapphireAction action,
                          final ActionHandlerDef def )
        {
            super.init( action, def );
            setEnabled( false );
        }

        public void setInput( final Collection<String> input )
        {
            this.input = input;
            setEnabled( list() != null && ! this.input.isEmpty() );
        }

        @Override
        protected Object run( final Presentation context )
        {
            final ElementList<?> list = list();
            
            if( list != null )
            {
                final ListFactory<Element> elements = ListFactory.start();
                
                for( String str : this.input )
                {
                    final Element element = list.insert();
                    element.property( SlushBucketPropertyEditorPresentation.this.memberProperty ).write( str );
                    elements.add( element );
                }
                
                setSelectedElements( elements.result() );
                setFocusOnTable();
            }
            
            return null;
        }
    };

}
