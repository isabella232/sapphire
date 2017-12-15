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

import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_SHOW_LABEL;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.HINT_SHOW_LABEL_ABOVE;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhindent;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhspan;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glspacing;
import static org.eclipse.sapphire.ui.swt.renderer.SwtUtil.makeTableSortable;
import static org.eclipse.sapphire.ui.swt.renderer.SwtUtil.suppressDashedTableEntryBorder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.PossibleValuesService;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.ui.SapphirePropertyEditor;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class CheckBoxListPropertyEditorRenderer

    extends ListPropertyEditorRenderer
    
{
    private Table table;
    private CheckboxTableViewer tableViewer;
    private ModelElementType memberType;
    private ValueProperty memberProperty;

    public CheckBoxListPropertyEditorRenderer( final SapphireRenderingContext context,
                                               final SapphirePropertyEditor part )
    {
        super( context, part );
    }

    @Override
    protected void createContents( final Composite parent )
    {
        // Initialize
        
        final SapphirePropertyEditor part = getPart();
        final IModelElement element = part.getModelElement();
        final ListProperty listProperty = (ListProperty) part.getProperty();
        
        this.memberType = listProperty.getType();
        
        final List<ModelProperty> allMemberProperties = this.memberType.getProperties();
        
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
        
        final PossibleValuesService possibleValuesProvider = element.service( this.memberProperty, PossibleValuesService.class );
        
        // Create Controls
        
        final boolean showLabelAbove = part.getRenderingHint( HINT_SHOW_LABEL_ABOVE, false );
        final boolean showLabelInline = part.getRenderingHint( HINT_SHOW_LABEL, ! showLabelAbove );
        Label label = null;
        
        final int baseIndent = part.getLeftMarginHint() + 9;
        
        if( showLabelInline || showLabelAbove )
        {
            label = new Label( parent, SWT.NONE );
            label.setText( listProperty.getLabel( false, CapitalizationType.FIRST_WORD_ONLY, true ) + ":" );
            label.setLayoutData( gdhindent( gdhspan( gdvalign( gd(), SWT.TOP ), showLabelAbove ? 2 : 1 ), baseIndent ) );
            this.context.adapt( label );
        }
        
        setSpanBothColumns( ! showLabelInline );
        
        final Composite mainComposite = createMainComposite( parent );
        mainComposite.setLayout( glspacing( glayout( 2, 0, 0 ), 2 ) );
        
        final PropertyEditorAssistDecorator decorator = createDecorator( mainComposite );
        decorator.getControl().setLayoutData( gdvalign( gd(), SWT.TOP ) );
        
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
        this.table.setHeaderVisible( true );
        
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
            public Object[] getElements( final Object input )
            {
                // To preserve selection in the table, it is important to re-use the entry that has the
                // current selection, if appropriate. For other entries, it is quicker to re-create.
                
                Entry selection = (Entry) ( (IStructuredSelection) CheckBoxListPropertyEditorRenderer.this.tableViewer.getSelection() ).getFirstElement();
                
                // First add entries for values listed in the model.
                
                final List<Entry> entries = new ArrayList<Entry>();
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
                    
                    entries.add( entry );
                    checked.add( value );
                }
                
                // Then add the rest of possible values.
                
                Set<String> possibleValues = null;
                
                try
                {
                    possibleValues = possibleValuesProvider.getPossibleValues();
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

                            entries.add( entry );
                        }
                    }
                }
                
                // Sort in order to not make the above two pass algorithm evident to the user.
                
                Collections.sort( entries, comparator );
                
                return entries.toArray();
            }
            
            public void dispose()
            {
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
                return ( (Entry) element ).text();
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
        
        makeTableSortable( this.tableViewer, Collections.<TableColumn,Comparator<Object>>singletonMap( column, comparator ) );
        
        this.tableViewer.addCheckStateListener
        (
            new ICheckStateListener()
            {
                public void checkStateChanged( final CheckStateChangedEvent event )
                {
                    handleCheckStateChangedEvent( event );
                }
            }
        );
        
        this.table.addMouseListener
        (
            new MouseAdapter()
            {
                public void mouseDoubleClick( final MouseEvent event )
                {
                    handleDoubleClickEvent( event );
                }
            }
        );
        
        // Finish Up
        
        this.tableViewer.setInput( new Object() );
        addControl( this.table );
    }

    @Override
    protected boolean canExpandVertically()
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
        this.tableViewer.refresh();
    }
    
    @Override
    protected void handleListElementChangedEvent( final ModelPropertyChangeEvent event )
    {
        super.handleListElementChangedEvent( event );
        this.tableViewer.refresh();
    }

    private void handleCheckStateChangedEvent( final CheckStateChangedEvent event )
    {
        final Entry entry = (Entry) event.getElement();
        
        entry.flip();
    }
    
    private void handleDoubleClickEvent( final MouseEvent event )
    {
        Entry entry = null;
        
        for( TableItem item : this.table.getItems() )
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
        private String value;
        private IModelElement element;
        
        public Entry( final String value,
                      final IModelElement element )
        {
            this.value = value;
            this.element = element;
        }
        
        public String text()
        {
            String text = this.value;
            
            if( text.length() == 0 )
            {
                text = Resources.emptyIndicator;
            }
            
            return text;
        }

        public Image image()
        {
            final Image image;
            
            if( this.element == null )
            {
                image = getPart().getImageCache().getImage( getMemberType() );
            }
            else
            {
                final IStatus st = this.element.read( getMemberProperty() ).validate();
                image = getPart().getImageCache().getImage( getMemberType(), st.getSeverity() );
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
                this.element = getList().addNewElement();
                this.element.write( getMemberProperty(), this.value );
            }
            else
            {
                // Must null the element field before trying to remove the element as remove will 
                // trigger property change event and it is possible for the resulting refresh to 
                // set the element field to a new value before returning.
                
                final IModelElement el = this.element;
                this.element = null;
                getList().remove( el );
            }
        }
    }
    
    public static final class Factory
    
        extends PropertyEditorRendererFactory
        
    {
        @Override
        public boolean isApplicableTo( final SapphirePropertyEditor propertyEditorDefinition )
        {
            return true;
        }
        
        @Override
        public PropertyEditorRenderer create( final SapphireRenderingContext context,
                                              final SapphirePropertyEditor part )
        {
            return new CheckBoxListPropertyEditorRenderer( context, part );
        }
    }
    
    private static final class Resources
    
        extends NLS
    
    {
        public static String emptyIndicator;
        
        static
        {
            initializeMessages( CheckBoxListPropertyEditorRenderer.class.getName(), Resources.class );
        }
    }
    
}
