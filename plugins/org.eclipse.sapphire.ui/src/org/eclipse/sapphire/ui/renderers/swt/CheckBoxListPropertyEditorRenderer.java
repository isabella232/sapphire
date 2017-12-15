/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.renderers.swt;

import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glspacing;
import static org.eclipse.sapphire.ui.swt.renderer.SwtUtil.makeTableSortable;
import static org.eclipse.sapphire.ui.swt.renderer.SwtUtil.suppressDashedTableEntryBorder;
import static org.eclipse.sapphire.util.CollectionsUtil.equalsBasedOnEntryIdentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImageData;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.NoDuplicates;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.ImageService;
import org.eclipse.sapphire.services.PossibleTypesService;
import org.eclipse.sapphire.services.PossibleValuesService;
import org.eclipse.sapphire.services.ValueImageService;
import org.eclipse.sapphire.services.ValueLabelService;
import org.eclipse.sapphire.ui.ListSelectionService;
import org.eclipse.sapphire.ui.ListSelectionService.ListSelectionChangedEvent;
import org.eclipse.sapphire.ui.PropertyEditorPart;
import org.eclipse.sapphire.ui.SapphireImageCache;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.def.PropertyEditorDef;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.sapphire.util.ReadOnlyListFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class CheckBoxListPropertyEditorRenderer extends ListPropertyEditorRenderer
{
    private Table table;
    private CheckboxTableViewer tableViewer;
    private ModelElementType memberType;
    private ValueProperty memberProperty;

    public CheckBoxListPropertyEditorRenderer( final SapphireRenderingContext context,
                                               final PropertyEditorPart part )
    {
        super( context, part );
    }

    @Override
    protected void createContents( final Composite parent )
    {
        // Initialize
        
        final PropertyEditorPart part = getPart();
        final IModelElement element = part.getLocalModelElement();
        final ListProperty listProperty = (ListProperty) part.getProperty();
        
        this.memberType = listProperty.getType();
        
        final List<ModelProperty> allMemberProperties = this.memberType.properties();
        
        if( allMemberProperties.size() == 1 )
        {
            final ModelProperty prop = allMemberProperties.get( 0 );
            
            if( prop instanceof ValueProperty )
            {
                this.memberProperty = (ValueProperty) prop;
            }
            else
            {
                throw new IllegalStateException();
            }
        }
        else
        {
            throw new IllegalStateException();
        }
        
        final PossibleValuesService possibleValuesService = element.service( this.memberProperty, PossibleValuesService.class );
        
        // Create Controls
        
        final Composite mainComposite = createMainComposite( parent );
        mainComposite.setLayout( glspacing( glayout( 2, 0, 0 ), 2 ) );
        
        final PropertyEditorAssistDecorator decorator = createDecorator( mainComposite );
        decorator.control().setLayoutData( gdvalign( gd(), SWT.TOP ) );
        
        // Setting the whint in the following code is a hacky workaround for the problem
        // tracked by the following JFace bug:
        //
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=215997
        //
        
        final Composite tableComposite = new Composite( mainComposite, SWT.NONE );
        tableComposite.setLayoutData( gdwhint( gdfill(), 1 ) );
        
        final TableColumnLayout tableColumnLayout = new TableColumnLayout();
        tableComposite.setLayout( tableColumnLayout );
        
        this.tableViewer = CheckboxTableViewer.newCheckList( tableComposite, SWT.BORDER | SWT.FULL_SELECTION );
        this.table = this.tableViewer.getTable();
        
        final TableViewerColumn viewerColumn = new TableViewerColumn( this.tableViewer, SWT.NONE );
        final TableColumn column = viewerColumn.getColumn();
        column.setText( this.memberProperty.getLabel( false, CapitalizationType.TITLE_STYLE, false ) );
        tableColumnLayout.setColumnData( column, new ColumnWeightData( 1, 100, true ) );
        
        this.context.adapt( mainComposite );
        decorator.addEditorControl( mainComposite );
        
        suppressDashedTableEntryBorder( this.table );
        
        // Bind to Model
        
        final ColumnSortComparator comparator = new ColumnSortComparator()
        {
            @Override
            protected String convertToString( final Object obj )
            {
                return ( (Entry) obj ).value;
            }
        };
        
        final IStructuredContentProvider contentProvider = new IStructuredContentProvider()
        {
            private List<Entry> entries = new ArrayList<Entry>();
            
            public Object[] getElements( final Object input )
            {
                if( this.entries != null )
                {
                    for( Entry entry : this.entries )
                    {
                        entry.dispose();
                    }
                    
                    this.entries = null;
                }
                
                // To preserve selection in the table, it is important to re-use the entry that has the
                // current selection, if appropriate. For other entries, it is quicker to re-create.
                
                Entry selection = (Entry) ( (IStructuredSelection) CheckBoxListPropertyEditorRenderer.this.tableViewer.getSelection() ).getFirstElement();
                
                // First add entries for values listed in the model.
                
                this.entries = new ArrayList<Entry>();
                final Set<String> checked = new HashSet<String>();
                
                for( IModelElement element : getList() )
                {
                    final String value = readMemberProperty( element );
                    final Entry entry;
                    
                    if( selection != null && selection.value.equals( value ) )
                    {
                        entry = selection;
                        entry.element = element;
                        selection = null;
                    }
                    else
                    {
                        entry = new Entry( value, element );
                    }
                    
                    this.entries.add( entry );
                    checked.add( value );
                }
                
                // Then add the rest of possible values.
                
                Set<String> possibleValues = null;
                
                try
                {
                    possibleValues = possibleValuesService.values();
                }
                catch( Exception e )
                {
                    SapphireUiFrameworkPlugin.log( e );
                }

                if( possibleValues != null )
                {
                    for( String value : possibleValues )
                    {
                        if( ! checked.contains( value ) )
                        {
                            final Entry entry;
                            
                            if( selection != null && selection.value.equals( value ) )
                            {
                                entry = selection;
                                entry.element = null;
                                selection = null;
                            }
                            else
                            {
                                entry = new Entry( value, null );
                            }

                            this.entries.add( entry );
                        }
                    }
                }
                
                // Sort in order to not make the above two pass algorithm evident to the user.
                
                Collections.sort( this.entries, comparator );
                
                return this.entries.toArray();
            }
            
            public void dispose()
            {
                for( Entry entry : this.entries )
                {
                    entry.dispose();
                }
                
                this.entries = null;
            }

            public void inputChanged( final Viewer viewer,
                                      final Object oldInput,
                                      final Object newInput )
            {
            }
        };
        
        this.tableViewer.setContentProvider( contentProvider );
        
        final ColumnLabelProvider labelProvider = new ColumnLabelProvider()
        {
            @Override
            public String getText( final Object element )
            {
                return ( (Entry) element ).label();
            }

            @Override
            public Image getImage( final Object element )
            {
                return ( (Entry) element ).image();
            }
            
            @Override
            public Color getForeground( final Object element )
            {
                return ( (Entry) element ).foreground();
            }
        };
        
        viewerColumn.setLabelProvider( labelProvider );
        
        final ICheckStateProvider checkStateProvider = new ICheckStateProvider()
        {
            public boolean isChecked( final Object element )
            {
                return ( (Entry) element ).selected();
            }
            
            public boolean isGrayed( final Object element )
            {
                return false;
            }
        };
        
        this.tableViewer.setCheckStateProvider( checkStateProvider );
        
        if( part.getRenderingHint( PropertyEditorDef.HINT_SHOW_HEADER, true ) == true )
        {
            this.table.setHeaderVisible( true );
            makeTableSortable( this.tableViewer, Collections.<TableColumn,Comparator<Object>>singletonMap( column, comparator ) );
        }
        
        final ListSelectionService selectionService = part.service( ListSelectionService.class );
        
        this.tableViewer.addSelectionChangedListener
        (
            new ISelectionChangedListener()
            {
                public void selectionChanged( final SelectionChangedEvent event )
                {
                    selectionService.select( getSelectedElements() );
                }
            }
        );
        
        final Listener selectionServiceListener = new Listener()
        {
            @Override
            public void handle( final Event event )
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
        
        this.tableViewer.addCheckStateListener
        (
            new ICheckStateListener()
            {
                public void checkStateChanged( final CheckStateChangedEvent event )
                {
                    final Entry entry = (Entry) event.getElement();
                    
                    entry.flip();
                    selectionService.select( getSelectedElements() );
                }
            }
        );
        
        this.table.addMouseListener
        (
            new MouseAdapter()
            {
                public void mouseDoubleClick( final MouseEvent event )
                {
                    Entry entry = null;
                    
                    for( TableItem item : CheckBoxListPropertyEditorRenderer.this.table.getItems() )
                    {
                        if( item.getBounds().contains( event.x, event.y ) )
                        {
                            entry = (Entry) item.getData();
                            break;
                        }
                    }
                    
                    if( entry != null )
                    {
                        entry.flip();
                        selectionService.select( getSelectedElements() );
                    }
                }
            }
        );
        
        // Finish Up
        
        this.tableViewer.setInput( new Object() );
        addControl( this.table );
    }

    @Override
    protected boolean canScaleVertically()
    {
        return true;
    }
    
    @Override
    protected void handleFocusReceivedEvent()
    {
        this.table.setFocus();
    }
    
    @Override
    protected void handlePropertyChangedEvent()
    {
        super.handlePropertyChangedEvent();
        refresh();
    }
    
    @Override
    protected void handleListElementChangedEvent( final Event event )
    {
        super.handleListElementChangedEvent( event );
        refresh();
    }
    
    public final IModelElement getSelectedElement()
    {
        final IStructuredSelection sel = (IStructuredSelection) CheckBoxListPropertyEditorRenderer.this.tableViewer.getSelection();
        
        if( sel == null )
        {
            return null;
        }
        else
        {
            return ( (Entry) sel.getFirstElement() ).element;
        }
    }
    
    public final List<IModelElement> getSelectedElements()
    {
        final IStructuredSelection sel = (IStructuredSelection) CheckBoxListPropertyEditorRenderer.this.tableViewer.getSelection();
        final ReadOnlyListFactory<IModelElement> elements = ReadOnlyListFactory.create();
        
        if( sel != null )
        {
            for( Iterator<?> itr = sel.iterator(); itr.hasNext(); )
            {
                final IModelElement element = ( (Entry) itr.next() ).element;
                
                if( element != null )
                {
                    elements.add( element );
                }
            }
        }
        
        return elements.export();
    }
    
    public final void setSelectedElements( final List<IModelElement> elements )
    {
        if( ! equalsBasedOnEntryIdentity( getSelectedElements(), elements ) )
        {
            final ReadOnlyListFactory<Entry> entries = ReadOnlyListFactory.create();
            
            for( IModelElement element : elements )
            {
                for( TableItem item : this.table.getItems() )
                {
                    final Entry entry = (Entry) item.getData();
                    
                    if( entry.element == element )
                    {
                        entries.add( entry );
                    }
                }
            }
            
            this.tableViewer.setSelection( new StructuredSelection( entries.export() ) );
        }
    }
    
    private void refresh()
    {
        final int oldItemCount = this.table.getItemCount();
        this.tableViewer.refresh();
        final int newItemCount = this.table.getItemCount();
        
        if( oldItemCount != newItemCount )
        {
            this.table.getParent().layout( true, true );
        }
    }

    private ModelElementType getMemberType()
    {
        return this.memberType;
    }
    
    private ValueProperty getMemberProperty()
    {
        return this.memberProperty;
    }
    
    private String readMemberProperty( final IModelElement element )
    {
        final String text = element.read( this.memberProperty ).getText();
        return ( text == null ? "" : text );
    }
    
    public final class Entry
    {
        private final LocalizationService localizationService;
        private String value;
        private IModelElement element;
        private ValueLabelService valueLabelService;
        private ValueImageService valueImageService;
        private ImageService elementImageService;
        private Listener listener;
        
        public Entry( final String value,
                      final IModelElement element )
        {
            this.localizationService = getPart().definition().adapt( LocalizationService.class );
            this.value = value;
            this.element = element;
            
            final IModelElement parent = getModelElement();
            
            this.valueLabelService = parent.service( CheckBoxListPropertyEditorRenderer.this.memberProperty, ValueLabelService.class );
            this.valueImageService = parent.service( CheckBoxListPropertyEditorRenderer.this.memberProperty, ValueImageService.class );
            
            this.listener = new Listener()
            {
                @Override
                public void handle( final Event event )
                {
                    CheckBoxListPropertyEditorRenderer.this.tableViewer.update( Entry.this, null );
                }
            };
            
            if( this.element != null )
            {
                this.elementImageService = this.element.service( ImageService.class );
                
                if( this.elementImageService != null )
                {
                    this.elementImageService.attach( this.listener );
                }
            }
        }
        
        public String label()
        {
            String label = null;
        
            if( this.value.length() == 0 )
            {
                label = Resources.emptyIndicator;
            }
            else
            {
                try
                {
                    label = this.valueLabelService.provide( this.value );
                }
                catch( Exception e )
                {
                    LoggingService.log( e );
                }
                
                if( label == null )
                {
                    label = this.value;
                }
                else if( ! label.equals( this.value ) )
                {
                    label = this.localizationService.transform( label, CapitalizationType.FIRST_WORD_ONLY, false );
                }
            }
            
            return label;
        }

        public Image image()
        {
            final SapphireImageCache cache = getPart().getImageCache();
            final Image image;
            
            if( this.element == null || this.elementImageService == null )
            {
                ImageData imageData = null;
                
                try
                {
                    imageData = this.valueImageService.provide( this.value );
                }
                catch( Exception e )
                {
                    LoggingService.log( e );
                }
                
                if( imageData == null )
                {
                    imageData = getMemberType().image();
                }
                
                image = cache.getImage( imageData );
            }
            else if( this.elementImageService != null )
            {
                final Status st = this.element.read( getMemberProperty() ).validation();
                image = cache.getImage( this.elementImageService.image(), st.severity() );
            }
            else
            {
                image = null;
            }
            
            return image;
        }
        
        public Color foreground()
        {
            Color color = null;
            
            if( this.value.length() == 0 )
            {
                color = Display.getCurrent().getSystemColor( SWT.COLOR_DARK_GRAY );
            }
            
            return color;
        }
        
        public boolean selected()
        {
            return ( this.element != null );
        }
        
        public void flip()
        {
            if( this.element == null )
            {
                this.element = getList().insert();
                this.element.write( getMemberProperty(), this.value );
                
                this.elementImageService = this.element.service( ImageService.class );
                
                if( this.elementImageService != null )
                {
                    this.elementImageService.attach( this.listener );
                }
            }
            else
            {
                if( this.elementImageService != null )
                {
                    this.elementImageService.detach( this.listener );
                    this.elementImageService = null;
                }
                
                // Must null the element field before trying to remove the element as remove will 
                // trigger property change event and it is possible for the resulting refresh to 
                // set the element field to a new value before returning.
                
                final IModelElement el = this.element;
                this.element = null;
                getList().remove( el );
            }
        }
        
        public void dispose()
        {
            if( this.elementImageService != null )
            {
                this.elementImageService.detach( this.listener );
            }
        }
    }
    
    public static final class Factory extends PropertyEditorRendererFactory
    {
        @Override
        public boolean isApplicableTo( final PropertyEditorPart propertyEditorDefinition )
        {
            return true;
        }
        
        @Override
        public PropertyEditorRenderer create( final SapphireRenderingContext context,
                                              final PropertyEditorPart part )
        {
            return new CheckBoxListPropertyEditorRenderer( context, part );
        }
    }
    
    public static final class EnumFactory extends PropertyEditorRendererFactory
    {
        @Override
        public boolean isApplicableTo( final PropertyEditorPart propertyEditorDefinition )
        {
            final IModelElement element = propertyEditorDefinition.getLocalModelElement();
            final ModelProperty property = propertyEditorDefinition.getProperty();
            
            if( property instanceof ListProperty )
            {
                final ListProperty listProperty = (ListProperty) property;
                
                if( element.service( listProperty, PossibleTypesService.class ).types().size() == 1 )
                {
                    final ModelElementType memberType = listProperty.getType();
                    final List<ModelProperty> properties = memberType.properties();
                    
                    if( properties.size() == 1 )
                    {
                        final ModelProperty memberProperty = properties.get( 0 );
                        
                        if( memberProperty instanceof ValueProperty &&
                            memberProperty.hasAnnotation( NoDuplicates.class ) &&
                            Enum.class.isAssignableFrom( memberProperty.getTypeClass() ) )
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
                                              final PropertyEditorPart part )
        {
            return new CheckBoxListPropertyEditorRenderer( context, part );
        }
    }
    
    private static final class Resources extends NLS
    {
        public static String emptyIndicator;
        
        static
        {
            initializeMessages( CheckBoxListPropertyEditorRenderer.class.getName(), Resources.class );
        }
    }
    
}
