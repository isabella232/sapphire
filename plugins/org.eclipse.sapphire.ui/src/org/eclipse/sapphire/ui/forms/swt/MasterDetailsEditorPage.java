/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [329114] rewrite context help binding feature
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt;

import static org.eclipse.sapphire.modeling.util.MiscUtil.escapeForXml;
import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_OUTLINE_HIDE;
import static org.eclipse.sapphire.ui.SapphireActionSystem.CONTEXT_EDITOR_PAGE;
import static org.eclipse.sapphire.ui.SapphireActionSystem.CONTEXT_EDITOR_PAGE_OUTLINE;
import static org.eclipse.sapphire.ui.SapphireActionSystem.CONTEXT_EDITOR_PAGE_OUTLINE_HEADER;
import static org.eclipse.sapphire.ui.SapphireActionSystem.CONTEXT_EDITOR_PAGE_OUTLINE_NODE;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhhint;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.util.MiscUtil.findSelectionPostDelete;
import static org.eclipse.sapphire.util.CollectionsUtil.findPrecedingItem;
import static org.eclipse.sapphire.util.CollectionsUtil.findTrailingItem;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
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
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.EditFailedException;
import org.eclipse.sapphire.modeling.localization.LabelTransformer;
import org.eclipse.sapphire.services.PossibleTypesService;
import org.eclipse.sapphire.ui.ISapphireEditorActionContributor;
import org.eclipse.sapphire.ui.PartVisibilityEvent;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireEditor;
import org.eclipse.sapphire.ui.SapphireEditorPagePart;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphirePart.ImageChangedEvent;
import org.eclipse.sapphire.ui.SapphirePart.LabelChangedEvent;
import org.eclipse.sapphire.ui.SapphirePart.PartEvent;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.def.EditorPageDef;
import org.eclipse.sapphire.ui.def.ISapphireDocumentation;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationDef;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationRef;
import org.eclipse.sapphire.ui.forms.MasterDetailsContentNodeList;
import org.eclipse.sapphire.ui.forms.MasterDetailsContentNodePart;
import org.eclipse.sapphire.ui.forms.MasterDetailsContentNodePart.NodeListEvent;
import org.eclipse.sapphire.ui.forms.MasterDetailsContentOutline;
import org.eclipse.sapphire.ui.forms.MasterDetailsEditorPageDef;
import org.eclipse.sapphire.ui.forms.MasterDetailsEditorPagePart;
import org.eclipse.sapphire.ui.forms.MasterDetailsEditorPagePart.OutlineHeaderTextEvent;
import org.eclipse.sapphire.ui.forms.SectionPart;
import org.eclipse.sapphire.ui.forms.swt.internal.ElementsTransfer;
import org.eclipse.sapphire.ui.forms.swt.internal.SapphireToolTip;
import org.eclipse.sapphire.ui.forms.swt.internal.SectionPresentation;
import org.eclipse.sapphire.ui.forms.swt.internal.text.SapphireFormText;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.sapphire.util.MutableReference;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class MasterDetailsEditorPage extends SapphireEditorFormPage implements ISapphireEditorActionContributor
{
    @Text( "Additional {0} problems not shown..." )
    private static LocalizableText problemsOverflowMessage;
    
    @Text( "two" )
    private static LocalizableText two;
    
    @Text( "three" )
    private static LocalizableText three;
    
    @Text( "four" )
    private static LocalizableText four;
    
    @Text( "five" )
    private static LocalizableText five;
    
    @Text( "six" )
    private static LocalizableText six;
    
    @Text( "seven" )
    private static LocalizableText seven;
    
    @Text( "eight" )
    private static LocalizableText eight;
    
    @Text( "nine" )
    private static LocalizableText nine;
    
    static
    {
        LocalizableText.init( MasterDetailsEditorPage.class );
    }

    private SwtPresentation presentation;
    private RootSection mainSection;
    private ContentOutline contentOutlinePage;
    private IPartListener2 partListener;
    
    public MasterDetailsEditorPage( final SapphireEditor editor,
                                    final Element element,
                                    final DefinitionLoader.Reference<EditorPageDef> definition ) 
    {
        this( editor, element, definition, null );
    }

    public MasterDetailsEditorPage( final SapphireEditor editor,
                                    final Element element,
                                    final DefinitionLoader.Reference<EditorPageDef> definition,
                                    final String pageName ) 
    {
        super( editor, element, definition );
        
        final MasterDetailsEditorPagePart part = getPart();
        
        this.presentation = new SwtPresentation( part, null, editor.getSite().getShell() )
        {
            @Override
            public void render()
            {
                throw new UnsupportedOperationException();
            }
        };
        
        String partName = pageName;
        
        if( partName == null )
        {
            partName = part.definition().getPageName().localized( CapitalizationType.TITLE_STYLE, false );
        }
        
        setPartName( partName );
        
        // Content Outline
        
        final SapphireAction outlineHideAction = getPart().getActions( CONTEXT_EDITOR_PAGE ).getAction( ACTION_OUTLINE_HIDE );
        
        final SapphireActionHandler outlineHideActionHandler = new SapphireActionHandler()
        {
            @Override
            protected Object run( final Presentation context )
            {
                setDetailsMaximized( ! isDetailsMaximized() );
                return null;
            }
        };
        
        outlineHideActionHandler.init( outlineHideAction, null );
        outlineHideActionHandler.setChecked( isDetailsMaximized() );
        outlineHideAction.addHandler( outlineHideActionHandler );
    }

    @Override
    public MasterDetailsEditorPagePart getPart()
    {
        return (MasterDetailsEditorPagePart) super.getPart();
    }
    
    public MasterDetailsEditorPageDef getDefinition()
    {
        return getPart().definition();
    }
    
    @Override
    public String getId()
    {
        return getPartName();
    }

    public MasterDetailsContentOutline outline()
    {
        return getPart().outline();
    }
    
    public IDetailsPage getCurrentDetailsPage()
    {
        return this.mainSection.getCurrentDetailsSection();
    }

    @Override
    protected void createFormContent( final IManagedForm managedForm ) 
    {
        final SapphireEditorPagePart part = getPart();
        final ScrolledForm form = managedForm.getForm();
        
        try
        {
            FormToolkit toolkit = managedForm.getToolkit();
            toolkit.decorateFormHeading(managedForm.getForm().getForm());
            
            this.mainSection = new RootSection();
            this.mainSection.createContent( managedForm );
            
            final ISapphireDocumentation doc = getDefinition().getDocumentation().content();
            
            if( doc != null )
            {
                ISapphireDocumentationDef docdef = null;
                
                if( doc instanceof ISapphireDocumentationDef )
                {
                    docdef = (ISapphireDocumentationDef) doc;
                }
                else
                {
                    docdef = ( (ISapphireDocumentationRef) doc ).resolve();
                }
                
                if( docdef != null )
                {
                    HelpSystem.setHelp( managedForm.getForm().getBody(), docdef );
                }
            }
            
            final SapphireActionGroup actions = part.getActions( CONTEXT_EDITOR_PAGE );
            
            final SapphireActionPresentationManager actionPresentationManager = new SapphireActionPresentationManager( this.presentation, actions );
            
            final SapphireToolBarManagerActionPresentation toolbarActionPresentation = new SapphireToolBarManagerActionPresentation( actionPresentationManager );
            final IToolBarManager toolbarManager = form.getToolBarManager();
            toolbarActionPresentation.setToolBarManager( toolbarManager );
            toolbarActionPresentation.render();
            
            final SapphireKeyboardActionPresentation keyboardActionPresentation = new SapphireKeyboardActionPresentation( actionPresentationManager );
            keyboardActionPresentation.attach( toolbarActionPresentation.getToolBar() );
            keyboardActionPresentation.render();
            
            part.attach
            (
                new FilteredListener<MasterDetailsEditorPagePart.DetailsFocusRequested>()
                {
                    @Override
                    protected void handleTypedEvent( final MasterDetailsEditorPagePart.DetailsFocusRequested event )
                    {
                        setFocusOnDetails();
                    }
                }
            );
            
            this.partListener = new IPartListener2()
            {
                public void partActivated( final IWorkbenchPartReference partRef )
                {
                }

                public void partBroughtToTop( final IWorkbenchPartReference partRef )
                {
                }

                public void partClosed( final IWorkbenchPartReference partRef )
                {
                    if( ! isDetailsMaximized() )
                    {
                        getPart().state().getContentOutlineState().setRatio( MasterDetailsEditorPage.this.mainSection.getOutlineRatio() );
                    }
                }

                public void partDeactivated( final IWorkbenchPartReference partRef )
                {
                }

                public void partOpened( final IWorkbenchPartReference partRef )
                {
                }

                public void partHidden( final IWorkbenchPartReference partRef )
                {
                }

                public void partVisible( final IWorkbenchPartReference partRef )
                {
                }

                public void partInputChanged( final IWorkbenchPartReference partRef )
                {
                }
            };
            
            getSite().getPage().addPartListener( this.partListener );
        }
        catch( final Exception e )
        {
            if( this.mainSection != null )
            {
                this.mainSection.dispose();
                this.mainSection = null;
                
                final Composite body = (Composite) ( (Form) form.getChildren()[ 0 ] ).getChildren()[ 1 ];
                
                for( Control control : body.getChildren() )
                {
                    control.dispose();
                }
                
                final Color bgcolor = body.getDisplay().getSystemColor( SWT.COLOR_WHITE );
                
                final Composite composite = new Composite( body, SWT.NONE );
                composite.setLayoutData( gdfill() );
                composite.setLayout( glayout( 1, 5, 5, 10, 5 ) );
                composite.setBackground( bgcolor );
                
                final Composite msgAndShowStackTraceLinkComposite = new Composite( composite, SWT.NONE );
                msgAndShowStackTraceLinkComposite.setLayoutData( gdhfill() );
                msgAndShowStackTraceLinkComposite.setLayout( glayout( 2, 0, 0 ) );
                msgAndShowStackTraceLinkComposite.setBackground( bgcolor );
                
                String message = e.getMessage();
                
                if( message == null )
                {
                    message = e.getClass().getName();
                }
                else
                {
                    message = message.replace( "&", "&amp;" );
                    message = message.replace( "<", "&lt;" );
                }
                
                final SapphireFormText text = new SapphireFormText( msgAndShowStackTraceLinkComposite, SWT.NONE );
                text.setLayoutData( gdhfill() );
                text.setText( "<form><li style=\"image\" value=\"error\">" + message + "</li></form>", true, false );
                text.setImage( "error", ImageData.readFromClassLoader( SwtResourceCache.class, "Error.png" ).required() );
                text.setBackground( bgcolor );

                final SapphireFormText showStackTraceLink = new SapphireFormText( msgAndShowStackTraceLinkComposite, SWT.NONE );
                showStackTraceLink.setLayoutData( gd() );
                showStackTraceLink.setText( "<form><p><a href=\"show-stack\">Show stack trace...</a></p></form>", true, false );
                showStackTraceLink.setBackground( bgcolor );
                
                showStackTraceLink.addHyperlinkListener
                (
                    new HyperlinkAdapter()
                    {
                        @Override
                        public void linkActivated( final HyperlinkEvent event )
                        {
                            showStackTraceLink.setVisible( false );
                            
                            final Label separator = new Label( composite, SWT.SEPARATOR | SWT.HORIZONTAL );
                            separator.setLayoutData( gdhfill() );
                            separator.setBackground( bgcolor );
                            
                            final org.eclipse.swt.widgets.Text stack 
                                = new org.eclipse.swt.widgets.Text( composite, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.V_SCROLL );
                            
                            stack.setLayoutData( gdfill() );
                            stack.setBackground( bgcolor );
                            
                            final StringWriter w = new StringWriter();
                            e.printStackTrace( new PrintWriter( w ) );
                            stack.setText( w.getBuffer().toString() );
                            
                            body.layout( true, true );
                        }
                    }
                );
            }
        }
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
        return ! getPart().state().getContentOutlineState().getVisible().content();
    }
    
    public void setDetailsMaximized( final boolean maximized )
    {
        this.mainSection.setDetailsMaximized( maximized );
        getPart().state().getContentOutlineState().setVisible( ! maximized );
    }
    
    public double getOutlineRatio()
    {
        double contentOutlineRatio = getPart().state().getContentOutlineState().getRatio().content();
        
        if( contentOutlineRatio < 0 || contentOutlineRatio > 1 )
        {
            contentOutlineRatio = 0.3d;
        }
        
        return contentOutlineRatio;
    }
    
    public void setOutlineRatio( final Double ratio )
    {
        if( ratio < 0 || ratio > 1 )
        {
            throw new IllegalArgumentException();
        }
        
        this.mainSection.setOutlineRatio( ratio );
        getPart().state().getContentOutlineState().setRatio( ratio );
    }
    
    @Override
    public void setActive( final boolean active )
    {
        if( this.mainSection != null )
        {
            super.setActive( active );
        }
    }
    
    @Override
    public boolean isDirty()
    {
        return false;
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

        if( this.mainSection != null )
        {
            this.mainSection.masterSection.tree.setFocus();
        }
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
        if( control instanceof Combo || control instanceof Link ||
            control instanceof List<?> || control instanceof Table || control instanceof Tree )
        {
            return control;
        }
        else if( control instanceof org.eclipse.swt.widgets.Text )
        {
            if( ( ( (org.eclipse.swt.widgets.Text) control ).getStyle() & SWT.READ_ONLY ) == 0 )
            {
                return control;
            }
        }
        else if( control instanceof Button )
        {
            final Button button = (Button) control;
            final int style = button.getStyle();
            
            if( ( style & SWT.CHECK ) != 0 || ( ( style & SWT.RADIO ) != 0 && button.getSelection() == true ) )
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
                                               final MasterDetailsContentOutline outline,
                                               final boolean addBorders )
    {
        final int treeStyle = ( addBorders ? SWT.BORDER : SWT.NONE ) | SWT.MULTI;
        
        final ContentOutlineFilteredTree filteredTree = new ContentOutlineFilteredTree( parent, treeStyle, outline );
        final TreeViewer treeViewer = filteredTree.getViewer();
        final Tree tree = treeViewer.getTree();
        
        final ITreeContentProvider contentProvider = new ITreeContentProvider()
        {
            private final Listener listener = new Listener()
            {
                @Override
                public void handle( final org.eclipse.sapphire.Event event )
                {
                    if( event instanceof PartEvent )
                    {
                        final SapphirePart part = ( (PartEvent) event ).part();
                        
                        if( part instanceof MasterDetailsContentNodePart )
                        {
                            final MasterDetailsContentNodePart node = (MasterDetailsContentNodePart) part;
                            
                            if( event instanceof PartVisibilityEvent )
                            {
                                final MasterDetailsContentNodePart parent = node.getParentNode();
                                
                                if( parent == outline.getRoot() )
                                {
                                    treeViewer.refresh();
                                }
                                else
                                {
                                    treeViewer.refresh( parent );
                                }
                            }
                            else
                            {
                                if( node.visible() )
                                {
                                    if( event instanceof LabelChangedEvent || event instanceof ImageChangedEvent )
                                    {
                                        Display.getCurrent().asyncExec( new TreeViewerUpdateJob( treeViewer, node ) );
                                    }
                                    else if( event instanceof NodeListEvent )
                                    {
                                        treeViewer.refresh( node );
                                    }
                                }
                            }
                        }
                    }
                }
            };
            
            private void attach( final List<MasterDetailsContentNodePart> nodes )
            {
                for( MasterDetailsContentNodePart node : nodes )
                {
                    node.attach( this.listener );
                }
            }

            private void detach( final List<MasterDetailsContentNodePart> nodes )
            {
                for( MasterDetailsContentNodePart node : nodes )
                {
                    node.detach( this.listener );
                    detach( node.nodes() );
                }
            }
            
            public Object[] getElements( final Object inputElement )
            {
                final MasterDetailsContentNodeList nodes = outline.getRoot().nodes();
                attach( nodes );
                return nodes.visible().toArray();
            }
        
            public Object[] getChildren( final Object parentElement )
            {
                final MasterDetailsContentNodeList nodes = ( (MasterDetailsContentNodePart) parentElement ).nodes();
                attach( nodes );
                return nodes.visible().toArray();
            }
        
            public Object getParent( final Object element )
            {
                return ( (MasterDetailsContentNodePart) element ).getParentNode();
            }
        
            public boolean hasChildren( final Object parentElement )
            {
                final MasterDetailsContentNodeList nodes = ( (MasterDetailsContentNodePart) parentElement ).nodes();
                attach( nodes );
                return ! nodes.visible().isEmpty();
            }
        
            public void inputChanged( final Viewer viewer,
                                      final Object oldInput,
                                      final Object newInput )
            {
            }

            public void dispose()
            {
                detach( outline.getRoot().nodes() );
            }
        };
        
        final LabelProvider labelProvider = new LabelProvider()
        {
            private final Map<ImageDescriptor,Image> images = new HashMap<ImageDescriptor,Image>();
            
            @Override
            public String getText( final Object element ) 
            {
                return ( (MasterDetailsContentNodePart) element ).getLabel();
            }
        
            @Override
            public Image getImage( final Object element ) 
            {
                return getImage( (MasterDetailsContentNodePart) element );
            }
            
            private Image getImage( final MasterDetailsContentNodePart node )
            {
                final ImageDescriptor imageDescriptor = node.getImage();
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
        
        new SapphireToolTip( tree ) 
        {
            protected Object getToolTipArea( final Event event ) 
            {
                return treeViewer.getCell( new Point( event.x, event.y ) );
            }

            protected boolean shouldCreateToolTip(Event event) 
            {
                if( ! super.shouldCreateToolTip( event ) ) 
                {
                    return false;
                }

                setShift( new Point( 0, 20 ) );
                tree.setToolTipText( "" );
                
                boolean res = false;
                
                final MasterDetailsContentNodePart node = getNode( event );
                
                if( node != null )
                {
                    res = ! node.validation().ok();
                }

                return res;
            }
            
            private MasterDetailsContentNodePart getNode( final Event event )
            {
                final TreeItem item = tree.getItem( new Point(event.x, event.y) );

                if( item == null )
                {
                    return null;
                }
                else
                {
                    return (MasterDetailsContentNodePart) item.getData();
                }
            }

            protected void afterHideToolTip(Event event) {
                super.afterHideToolTip(event);
                // Clear the restored value else this could be a source of a leak
                if (event != null && event.widget != treeViewer.getControl()) {
                    treeViewer.getControl().setFocus();
                }
            }

            @Override
            protected void createContent( final Event event,
                                          final Composite parent )
            {
                final MasterDetailsContentNodePart node = getNode( event );
                
                parent.setLayout( glayout( 1 ) );
                
                SapphireFormText text = new SapphireFormText( parent, SWT.NO_FOCUS );
                text.setLayoutData( gdfill() );
                
                final org.eclipse.sapphire.modeling.Status validation = node.validation();
                final List<org.eclipse.sapphire.modeling.Status> items = gather( validation );
                
                final StringBuffer buffer = new StringBuffer();
                buffer.append( "<form>" );
                
                final int count = items.size();
                int i = 0;
                
                for( org.eclipse.sapphire.modeling.Status item : items )
                {
                    final String imageKey = ( item.severity() == org.eclipse.sapphire.modeling.Status.Severity.ERROR ? "error" : "warning" );
                    buffer.append( "<li style=\"image\" value=\"" + imageKey + "\">" + escapeForXml( item.message() ) + "</li>" );
                    
                    i++;
                    
                    if( count > 10 && i == 9 )
                    {
                        break;
                    }
                }
                
                if( count > 10 )
                {
                    final String msg = problemsOverflowMessage.format( numberToString( count - 9 ) );
                    final String imageKey = ( validation.severity() == org.eclipse.sapphire.modeling.Status.Severity.ERROR ? "error" : "warning" );
                    buffer.append( "<br/><li style=\"image\" value=\"" + imageKey + "\">" + msg + "</li>" );
                }
                
                buffer.append( "</form>" );
                
                text.setText( buffer.toString(), true, false );
                text.setImage( "error", ImageData.readFromClassLoader( SwtResourceCache.class, "Error.png" ).required() );
                text.setImage( "warning", ImageData.readFromClassLoader( SwtResourceCache.class, "Warning.png" ).required() );
            }
            
            private String numberToString( final int number )
            {
                switch( number )
                {
                    case 2  : return two.text();
                    case 3  : return three.text();
                    case 4  : return four.text();
                    case 5  : return five.text();
                    case 6  : return six.text();
                    case 7  : return seven.text();
                    case 8  : return eight.text();
                    case 9  : return nine.text();
                    default : return String.valueOf( number );
                }
            }
            
            private List<org.eclipse.sapphire.modeling.Status> gather( final org.eclipse.sapphire.modeling.Status status )
            {
                final List<org.eclipse.sapphire.modeling.Status> items = new ArrayList<org.eclipse.sapphire.modeling.Status>();
                gather( status, items );
                return items;
            }
            
            private void gather( final org.eclipse.sapphire.modeling.Status status,
                                 final List<org.eclipse.sapphire.modeling.Status> items )
            {
                if( status.children().isEmpty() )
                {
                    items.add( status );
                }
                else
                {
                    for( org.eclipse.sapphire.modeling.Status child : status.children() )
                    {
                        gather( child, items );
                    }
                }
            }
        };
        
        treeViewer.setContentProvider( contentProvider );
        treeViewer.setLabelProvider( labelProvider );
        treeViewer.setInput( new Object() );
        
        final MutableReference<Boolean> ignoreSelectionChange = new MutableReference<Boolean>( false );
        final MutableReference<Boolean> ignoreExpandedStateChange = new MutableReference<Boolean>( false );
        
        final Listener contentTreeListener = new Listener()
        {
            @Override
            public void handle( final org.eclipse.sapphire.Event event )
            {
                if( event instanceof MasterDetailsContentOutline.SelectionChangedEvent )
                {
                    if( ignoreSelectionChange.get() == true )
                    {
                        return;
                    }
                    
                    final MasterDetailsContentOutline.SelectionChangedEvent evt = (MasterDetailsContentOutline.SelectionChangedEvent) event;
                    final List<MasterDetailsContentNodePart> selection = evt.selection();
                    
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
                        for( MasterDetailsContentNodePart node : selection )
                        {
                            treeViewer.reveal( node );
                        }
                        
                        treeViewer.setSelection( sel );
                    }
                }
                else if( event instanceof MasterDetailsContentOutline.NodeExpandedStateChangedEvent )
                {
                    if( ignoreExpandedStateChange.get() == true )
                    {
                        return;
                    }
                    
                    final MasterDetailsContentOutline.NodeExpandedStateChangedEvent evt = (MasterDetailsContentOutline.NodeExpandedStateChangedEvent) event;
                    final MasterDetailsContentNodePart node = evt.node();

                    final boolean expandedState = node.isExpanded();
                    
                    if( treeViewer.getExpandedState( node ) != expandedState )
                    {
                        treeViewer.setExpandedState( node, expandedState );
                    }
                }
                else if( event instanceof MasterDetailsContentOutline.FilterChangedEvent )
                {
                    final MasterDetailsContentOutline.FilterChangedEvent evt = (MasterDetailsContentOutline.FilterChangedEvent) event;
                    filteredTree.changeFilterText( evt.filter() );
                }
            }
        };

        outline.attach( contentTreeListener );
        
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
                        final List<MasterDetailsContentNodePart> nodes = new ArrayList<MasterDetailsContentNodePart>();
                        
                        for( Iterator<?> itr = selection.iterator(); itr.hasNext(); )
                        {
                            nodes.add( (MasterDetailsContentNodePart) itr.next() );
                        }
                        
                        outline.setSelectedNodes( nodes );
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
                        final MasterDetailsContentNodePart node = (MasterDetailsContentNodePart) event.getElement();
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
                        final MasterDetailsContentNodePart node = (MasterDetailsContentNodePart) event.getElement();
                        node.setExpanded( false );
                    }
                    finally
                    {
                        ignoreExpandedStateChange.set( false );
                    }
                }
            }
        );
        
        final ContentOutlineActionSupport actionSupport = new ContentOutlineActionSupport( outline, tree );
        
        treeViewer.setExpandedElements( outline.getExpandedNodes().toArray() );
        contentTreeListener.handle( new MasterDetailsContentOutline.SelectionChangedEvent( outline.getSelectedNodes() ) );
        
        filteredTree.changeFilterText( outline.getFilterText() );
        
        final ElementsTransfer transfer = new ElementsTransfer( getModelElement().type().getModelElementClass().getClassLoader() );
        final Transfer[] transfers = new Transfer[] { transfer };
        
        final DragSource dragSource = new DragSource( tree, DND.DROP_COPY | DND.DROP_MOVE );
        dragSource.setTransfer( transfers );

        final List<Element> dragElements = new ArrayList<Element>();
        
        dragSource.addDragListener
        (
            new DragSourceListener()
            {
                public void dragStart( final DragSourceEvent event )
                {
                    final TreeItem[] selection = tree.getSelection();
                    final String filter = outline().getFilterText();
                    
                    if( ( filter == null || filter.length() == 0 ) && draggable( selection ) )
                    {
                        event.doit = true;
                        
                        for( TreeItem item : selection )
                        {
                            final MasterDetailsContentNodePart node = (MasterDetailsContentNodePart) item.getData();
                            dragElements.add( node.getModelElement() );
                        }
                    }
                    else
                    {
                        event.doit = false;
                    }
                }
                
                protected boolean draggable( final TreeItem[] selection )
                {
                    if( selection.length > 0 )
                    {
                        for( TreeItem item : selection )
                        {
                            final MasterDetailsContentNodePart node = (MasterDetailsContentNodePart) item.getData();
                            
                            if( ! draggable( node ) )
                            {
                                return false;
                            }
                        }
                    
                        return true;
                    }
                    
                    return false;
                }
                
                protected boolean draggable( final MasterDetailsContentNodePart node )
                {
                    final Element element = node.getModelElement();
                    
                    if( element.parent() instanceof ElementList && node.controls( element ) )
                    {
                        return true;
                    }
                    
                    return false;
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
                            final TreeItem[] selection = tree.getSelection();
                            final List<MasterDetailsContentNodePart> dragNodes = new ArrayList<MasterDetailsContentNodePart>();
                            
                            for( TreeItem item : selection )
                            {
                                dragNodes.add( (MasterDetailsContentNodePart) item.getData() );
                            }
                            
                            final MasterDetailsContentNodePart parentNode = dragNodes.get( 0 ).getParentNode();
                            
                            MasterDetailsContentNodePart selectionPostDelete = findSelectionPostDelete( parentNode.nodes().visible(), dragNodes );
                            
                            if( selectionPostDelete == null )
                            {
                                selectionPostDelete = parentNode;
                            }
                            
                            try
                            {
                                outline.listeners().suspend( MasterDetailsContentOutline.SelectionChangedEvent.class );
                            
                                for( Element dragElement : dragElements )
                                {
                                    final ElementList<?> dragElementContainer = (ElementList<?>) dragElement.parent();
                                    dragElementContainer.remove( dragElement );
                                }
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
                            finally
                            {
                                outline.listeners().resume( MasterDetailsContentOutline.SelectionChangedEvent.class );
                            }
                            
                            parentNode.getContentTree().setSelectedNode( selectionPostDelete );
                        }
                    }
                    
                    dragElements.clear();
                }
            }
        );
        
        final DropTarget target = new DropTarget( tree, DND.DROP_COPY | DND.DROP_MOVE );
        target.setTransfer( transfers );
        
        target.addDropListener
        (
            new DropTargetAdapter()
            {
                public void dragOver( final DropTargetEvent event )
                {
                    if( event.item != null )
                    {
                        final TreeItem dragOverItem = (TreeItem) event.item;
                        final MasterDetailsContentNodePart dragOverNode = (MasterDetailsContentNodePart) dragOverItem.getData();
                        final MasterDetailsContentNodePart parentNode = dragOverNode.getParentNode();
                        final List<MasterDetailsContentNodePart> siblingNodes = parentNode.nodes().visible();

                        final Point pt = dragOverItem.getDisplay().map( null, tree, event.x, event.y );
                        final Rectangle bounds = dragOverItem.getBounds();
                        
                        boolean dragOverNodeAcceptedDrop = false;
                        
                        if( pt.y > bounds.y + bounds.height / 3 && pt.y < bounds.y + bounds.height - bounds.height / 3 )
                        {
                            for( final PropertyDef dragOverTargetChildProperty : dragOverNode.getChildNodeFactoryProperties() )
                            {
                                if( dragOverTargetChildProperty instanceof ListProperty && ! dragOverTargetChildProperty.isReadOnly() )
                                {
                                    dragOverNodeAcceptedDrop = true;
                                    event.feedback = DND.FEEDBACK_SELECT;
                                    
                                    break;
                                }
                            }
                        }
                        
                        if( ! dragOverNodeAcceptedDrop )
                        {
                            MasterDetailsContentNodePart precedingNode = null;
                            MasterDetailsContentNodePart trailingNode = null;
    
                            if( pt.y < bounds.y + bounds.height / 2 )
                            {
                                precedingNode = findPrecedingItem( siblingNodes, dragOverNode );
                                trailingNode = dragOverNode;
                                
                                event.feedback = DND.FEEDBACK_INSERT_BEFORE;
                            }
                            else
                            {
                                precedingNode = dragOverNode;
                                trailingNode = findTrailingItem( siblingNodes, dragOverNode );
    
                                event.feedback = DND.FEEDBACK_INSERT_AFTER;
                            }
                            
                            boolean ok = false;
                            
                            if( precedingNode != null )
                            {
                                final Element precedingElement = precedingNode.getModelElement();
                                
                                if( precedingElement.parent() instanceof ElementList && precedingNode.controls( precedingElement ) )
                                {
                                    ok = true;
                                }
                            }
                            
                            if( ! ok && trailingNode != null )
                            {
                                final Element trailingElement = trailingNode.getModelElement();
                                
                                if( trailingElement.parent() instanceof ElementList && trailingNode.controls( trailingElement ) )
                                {
                                    ok = true;
                                }
                            }
                            
                            if( ! ok )
                            {
                                event.feedback = DND.FEEDBACK_NONE;
                            }
                        }
                    }
                    
                    event.feedback |= DND.FEEDBACK_SCROLL;
                }

                @SuppressWarnings( "unchecked" )
                
                public void drop( final DropTargetEvent event ) 
                {
                    if( event.data == null || event.item == null)
                    {
                        event.detail = DND.DROP_NONE;
                        return;
                    }
                    
                    // Determine where something was dropped.
                    
                    final List<Element> droppedElements = (List<Element>) event.data;
                    final TreeItem dropTargetItem = (TreeItem) event.item;
                    final MasterDetailsContentNodePart dropTargetNode = (MasterDetailsContentNodePart) dropTargetItem.getData();
                    final MasterDetailsContentNodePart parentNode = dropTargetNode.getParentNode();
                    final List<MasterDetailsContentNodePart> siblingNodes = parentNode.nodes().visible();
                    
                    final Point pt = tree.getDisplay().map( null, tree, event.x, event.y );
                    final Rectangle bounds = dropTargetItem.getBounds();
                    
                    MasterDetailsContentNodePart precedingNode = null;
                    MasterDetailsContentNodePart trailingNode = null;
                    
                    boolean dropTargetNodeAcceptedDrop = false; 

                    if( pt.y > bounds.y + bounds.height / 3 && pt.y < bounds.y + bounds.height - bounds.height / 3 )
                    {
                        for( final PropertyDef dropTargetChildProperty : dropTargetNode.getChildNodeFactoryProperties() )
                        {
                            if( dropTargetChildProperty instanceof ListProperty && ! dropTargetChildProperty.isReadOnly() )
                            {
                                dropTargetNodeAcceptedDrop = true;
                                break;
                            }
                        }
                    }
                    
                    if( ! dropTargetNodeAcceptedDrop )
                    {
                        if( pt.y < bounds.y + bounds.height / 2 ) 
                        {
                            precedingNode = findPrecedingItem( siblingNodes, dropTargetNode );
                            trailingNode = dropTargetNode;
                        }
                        else
                        {
                            precedingNode = dropTargetNode;
                            trailingNode = findTrailingItem( siblingNodes, dropTargetNode );
                        }
                    }
                    
                    // Determine whether the drop was valid from model standpoint and figure out
                    // where in the model the dropped elements are to be inserted.
                    
                    ElementList<?> list = null;
                    int position = -1;
                    
                    if( precedingNode != null )
                    {
                        final Element precedingElement = precedingNode.getModelElement();
                        
                        if( precedingElement.parent() instanceof ElementList && ! precedingElement.parent().definition().isReadOnly() &&
                            precedingNode.controls( precedingElement ) )
                        {
                            list = (ElementList<?>) precedingElement.parent();
                            
                            final Set<ElementType> possibleListElementTypes = list.definition().service( PossibleTypesService.class ).types();
                            
                            for( Element droppedElement : droppedElements )
                            {
                                if( ! possibleListElementTypes.contains( droppedElement.type() ) )
                                {
                                    list = null;
                                    break;
                                }
                            }
                            
                            if( list != null )
                            {
                                position = list.indexOf( precedingElement ) + 1;
                            }
                        }
                    }
                    
                    if( list == null && trailingNode != null )
                    {
                        final Element trailingElement = trailingNode.getModelElement();
                        
                        if( trailingElement.parent() instanceof ElementList && ! trailingElement.parent().definition().isReadOnly() &&
                            trailingNode.controls( trailingElement ) )
                        {
                            list = (ElementList<?>) trailingElement.parent();
                            
                            final Set<ElementType> possibleListElementTypes = list.definition().service( PossibleTypesService.class ).types();
                            
                            for( Element droppedElement : droppedElements )
                            {
                                if( ! possibleListElementTypes.contains( droppedElement.type() ) )
                                {
                                    list = null;
                                    break;
                                }
                            }
                            
                            if( list != null )
                            {
                                position = list.indexOf( trailingElement );
                            }
                        }
                    }
                    
                    if( list == null )
                    {
                        for( PropertyDef dropTargetChildProperty : dropTargetNode.getChildNodeFactoryProperties() )
                        {
                            if( dropTargetChildProperty instanceof ListProperty && ! dropTargetChildProperty.isReadOnly() )
                            {
                                final ListProperty dropTargetChildListProperty = (ListProperty) dropTargetChildProperty;
                                
                                boolean compatible = true;
                                
                                final Set<ElementType> possibleListElementTypes = dropTargetChildListProperty.service( PossibleTypesService.class ).types();
                                
                                for( Element droppedElement : droppedElements )
                                {
                                    if( ! possibleListElementTypes.contains( droppedElement.type() ) )
                                    {
                                        compatible = false;
                                        break;
                                    }
                                }
                                
                                if( compatible )
                                {
                                    list = dropTargetNode.getLocalModelElement().property( dropTargetChildListProperty );
                                    position = list.size();
                                }
                            }
                        }
                    }
                    
                    if( list == null )
                    {
                        event.detail = DND.DROP_NONE;
                        return;
                    }
                    
                    // Prevent a drop within a drag element.
                    
                    for( Property p = list; p != null; p = p.element().parent() )
                    {
                        for( final Element dragElement : dragElements )
                        {
                            if( p.element() == dragElement )
                            {
                                event.detail = DND.DROP_NONE;
                                return;
                            }
                        }
                    }
                    
                    // Perform the removal and insertion into the new location.
                    
                    try
                    {
                        outline.listeners().suspend( MasterDetailsContentOutline.SelectionChangedEvent.class );
                    
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
    
                        final List<MasterDetailsContentNodePart> newSelection = new ArrayList<MasterDetailsContentNodePart>();
                        
                        for( Element droppedElement : droppedElements )
                        {
                            final Element insertedElement = list.insert( droppedElement.type(), position );
                            insertedElement.copy( droppedElement );
                            
                            newSelection.add( parentNode.findNode( insertedElement ) );
                            
                            position++;
                        }
                        
                        parentNode.getContentTree().setSelectedNodes( newSelection );
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
                    finally
                    {
                        outline.listeners().resume( MasterDetailsContentOutline.SelectionChangedEvent.class );
                    }
                }
            }
        );
        
        tree.addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    outline.detach( contentTreeListener );
                    actionSupport.dispose();
                }
            }
        );

        return filteredTree;
    }
    
    private static void updateExpandedState( final MasterDetailsContentOutline contentTree,
                                             final Tree tree )
    {
        final Set<MasterDetailsContentNodePart> expandedNodes = new HashSet<MasterDetailsContentNodePart>();
        gatherExpandedNodes( tree.getItems(), expandedNodes );
        contentTree.setExpandedNodes( expandedNodes );
    }
    
    private static void gatherExpandedNodes( final TreeItem[] items,
                                             final Set<MasterDetailsContentNodePart> result )
    {
        for( TreeItem item : items )
        {
            if( item.getExpanded() == true )
            {
                result.add( (MasterDetailsContentNodePart) item.getData() );
                gatherExpandedNodes( item.getItems(), result );
            }
        }
    }
    
    public void dispose() 
    {
        super.dispose();
        
        if( this.mainSection != null ) 
        {
            this.mainSection.dispose();
        }
        
        if( this.partListener != null )
        {
            getSite().getPage().removePartListener( this.partListener );
        }
    }
    
    private static final class ContentOutlineFilteredTree extends FilteredTree
    {
        private final MasterDetailsContentOutline contentTree;
        private WorkbenchJob refreshJob;

        public ContentOutlineFilteredTree( final Composite parent,
                                           final int treeStyle,
                                           final MasterDetailsContentOutline contentTree )
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
        private SapphireActionPresentationManager actionPresentationManager;
        
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
            
            this.filteredTree = createContentOutline( this.outerComposite, outline(), false );
            this.filteredTree.setLayoutData( gdfill() );
            
            this.treeViewer = this.filteredTree.getViewer();
            
            final SapphireEditorPagePart part = getPart();
            
            final SapphireActionGroup actions = part.getActions( CONTEXT_EDITOR_PAGE_OUTLINE_HEADER );
            
            this.actionPresentationManager = new SapphireActionPresentationManager( MasterDetailsEditorPage.this.presentation, actions );
            
            final SapphireToolBarManagerActionPresentation toolbarActionsPresentation = new SapphireToolBarManagerActionPresentation( this.actionPresentationManager );
            
            toolbarActionsPresentation.setToolBarManager( getSite().getActionBars().getToolBarManager() );
            toolbarActionsPresentation.render();
            
            final SapphireKeyboardActionPresentation keyboardActionsPresentation = new SapphireKeyboardActionPresentation( this.actionPresentationManager );
            keyboardActionsPresentation.attach( this.filteredTree.getFilterControl() );
            keyboardActionsPresentation.render();
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

        @Override
        public void dispose()
        {
            super.dispose();
            
            this.actionPresentationManager.dispose();
            this.actionPresentationManager = null;
        }
    }
    
    private final class ContentOutlineActionSupport
    {
        private final MasterDetailsContentOutline contentTree;
        private final Listener contentOutlineListener;
        private final Tree tree;
        private final Menu menu;
        private SapphireActionPresentationManager actionPresentationManager;
        private SapphireActionGroup tempActions;
        
        private ContentOutlineActionSupport( final MasterDetailsContentOutline contentOutline,
                                             final Tree tree )
        {
            this.contentTree = contentOutline;
            this.tree = tree;
            
            this.menu = new Menu( tree );
            this.tree.setMenu( this.menu );
            
            this.contentOutlineListener = new Listener()
            {
                @Override
                public void handle( final org.eclipse.sapphire.Event event )
                {
                    if( event instanceof MasterDetailsContentOutline.SelectionChangedEvent )
                    {
                        final MasterDetailsContentOutline.SelectionChangedEvent evt = (MasterDetailsContentOutline.SelectionChangedEvent) event;
                        handleSelectionChangedEvent( evt.selection() );
                    }
                }
            };

            this.contentTree.attach( this.contentOutlineListener );
            
            handleSelectionChangedEvent( contentOutline.getSelectedNodes() );
        }
        
        private void handleSelectionChangedEvent( final List<MasterDetailsContentNodePart> selection )
        {
            for( MenuItem item : this.menu.getItems() )
            {
                item.dispose();
            }
            
            if( this.tempActions != null )
            {
                this.tempActions.dispose();
                this.tempActions = null;
            }
            
            if( this.actionPresentationManager != null )
            {
                this.actionPresentationManager.dispose();
                this.actionPresentationManager = null;
            }
            
            final SapphireEditorPagePart part = getPart();
            final SapphireActionGroup actions;
            
            if( selection.size() == 1 )
            {
                final MasterDetailsContentNodePart node = selection.get( 0 );
                actions = node.getActions( CONTEXT_EDITOR_PAGE_OUTLINE_NODE );
            }
            else
            {
                this.tempActions = new SapphireActionGroup( part, CONTEXT_EDITOR_PAGE_OUTLINE );
                actions = this.tempActions;
            }
            
            this.actionPresentationManager 
                = new SapphireActionPresentationManager( MasterDetailsEditorPage.this.presentation, actions );
            
            final SapphireMenuActionPresentation menuActionPresentation = new SapphireMenuActionPresentation( this.actionPresentationManager );
            menuActionPresentation.setMenu( this.menu );
            menuActionPresentation.render();
            
            final SapphireKeyboardActionPresentation keyboardActionPresentation = new SapphireKeyboardActionPresentation( this.actionPresentationManager );
            keyboardActionPresentation.attach( this.tree );
            keyboardActionPresentation.render();
        }
        
        public void dispose()
        {
            this.contentTree.detach( this.contentOutlineListener );
            
            if( this.tempActions != null )
            {
                this.tempActions.dispose();
                this.tempActions = null;
            }
            
            if( this.actionPresentationManager != null )
            {
                this.actionPresentationManager.dispose();
                this.actionPresentationManager = null;
            }
        }
    }
        
    public IAction getAction(String actionId)
    {
    	// TODO return action handlers for the global actions such as Delete, Select All
    	return null;
    }
    
    private final class RootSection 
    
        extends MasterDetailsBlock
        
    {
        private MasterSection masterSection;
        private List<IDetailsPage> detailsSections;
        private Control detailsSectionControl;
        
        public RootSection() 
        {
            this.detailsSections = new ArrayList<IDetailsPage>();
            this.detailsSectionControl = null;
        }

        @Override
        public void createContent( final IManagedForm managedForm ) 
        {
            super.createContent( managedForm );
            
            setOutlineRatio( MasterDetailsEditorPage.this.getOutlineRatio() );
            
            try
            {
                final Field field = this.detailsPart.getClass().getDeclaredField( "pageBook" );
                field.setAccessible( true );
                this.detailsSectionControl = (Control) field.get( this.detailsPart );
            }
            catch( Exception e )
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
            
            this.masterSection.handleSelectionChangedEvent( outline().getSelectedNodes() );
            
            setDetailsMaximized( isDetailsMaximized() );
        }
        
        @Override
        protected void createMasterPart( final IManagedForm managedForm, 
                                         final Composite parent ) 
        {
            this.masterSection = new MasterSection( managedForm, parent );
            final org.eclipse.ui.forms.SectionPart spart = new org.eclipse.ui.forms.SectionPart(this.masterSection);
            managedForm.addPart(spart);
        }

        @Override
        protected void registerPages( final DetailsPart detailsPart ) 
        {
            final IDetailsPage detailsPage = new DetailsSection();
            detailsPart.registerPage( MasterDetailsContentNodePart.class, detailsPage );
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
        
        public double getOutlineRatio()
        {
            final Control[] children = this.sashForm.getChildren();
            
            final int outline = children[ 0 ].getSize().x;
            final int details = children[ 1 ].getSize().x;
            final int total = outline + details;
            final double ratio = ( (double) outline ) / ( (double) total );
            
            return ratio;
        }
        
        public void setOutlineRatio( final double ratio )
        {
            final int total = Integer.MAX_VALUE;
            final int outline = (int) ( total * ratio );
            final int details = total - outline;
            
            this.sashForm.setWeights( new int[] { outline, details } );
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
    
    private final class MasterSection extends Section
    {
        private IManagedForm managedForm;
        private org.eclipse.ui.forms.SectionPart sectionPart;
        private TreeViewer treeViewer;
        private Tree tree;
        
        private void refreshOutlineHeaderText()
        {
            setText( LabelTransformer.transform( getPart().getOutlineHeaderText(), CapitalizationType.TITLE_STYLE, false ) );
        }
        
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
            
            final SapphireEditorPagePart part = getPart();
            
            final Listener pagePartListener = new FilteredListener<OutlineHeaderTextEvent>()
            {
                @Override
                protected void handleTypedEvent( final OutlineHeaderTextEvent event )
                {
                    refreshOutlineHeaderText();
                }
            };
            
            part.attach( pagePartListener );
            
            refreshOutlineHeaderText();
            
            final Composite client = toolkit.createComposite( this );
            client.setLayout( glayout( 1, 0, 0 ) );
            
            this.managedForm = managedForm;
            
            final MasterDetailsContentOutline contentTree = outline();
            
            final FilteredTree filteredTree = createContentOutline( client, contentTree, true );
            this.treeViewer = filteredTree.getViewer();
            this.tree = this.treeViewer.getTree();
            
            this.sectionPart = new org.eclipse.ui.forms.SectionPart( this );
            this.managedForm.addPart( this.sectionPart );
    
            contentTree.attach
            (
                new FilteredListener<MasterDetailsContentOutline.SelectionChangedEvent>()
                {
                    @Override
                    protected void handleTypedEvent( final MasterDetailsContentOutline.SelectionChangedEvent event )
                    {
                        handleSelectionChangedEvent( event.selection() );
                    }
                }
            );
            
            final ToolBar toolbar = new ToolBar( this, SWT.FLAT | SWT.HORIZONTAL );
            setTextClient( toolbar );
            
            final SapphireActionGroup actions = part.getActions( CONTEXT_EDITOR_PAGE_OUTLINE_HEADER );
            
            final SapphireActionPresentationManager actionPresentationManager 
                = new SapphireActionPresentationManager( MasterDetailsEditorPage.this.presentation, actions );
            
            final SapphireToolBarActionPresentation toolbarActionsPresentation = new SapphireToolBarActionPresentation( actionPresentationManager );
            
            toolbarActionsPresentation.setToolBar( toolbar );
            toolbarActionsPresentation.render();
            
            final SapphireKeyboardActionPresentation keyboardActionsPresentation = new SapphireKeyboardActionPresentation( actionPresentationManager );
            keyboardActionsPresentation.attach( filteredTree.getFilterControl() );
            keyboardActionsPresentation.render();
            
            toolkit.paintBordersFor( this );
            setClient( client );
            
            this.tree.addDisposeListener
            (
                new DisposeListener()
                {
                    public void widgetDisposed( final DisposeEvent event )
                    {
                        part.detach( pagePartListener );
                    }
                }
            );
        }
        
        private Font createBoldFont(Display display, Font regularFont) {
            FontData[] fontDatas = regularFont.getFontData();
            for (int i = 0; i < fontDatas.length; i++) {
                fontDatas[i].setStyle(fontDatas[i].getStyle() | SWT.BOLD);
            }
            return new Font(display, fontDatas);
        }
        
        private void handleSelectionChangedEvent( final List<MasterDetailsContentNodePart> selection )
        {
            final IStructuredSelection sel
                = ( selection.isEmpty() ? StructuredSelection.EMPTY : new StructuredSelection( selection.get( 0 ) ) );
            
            this.managedForm.fireSelectionChanged( this.sectionPart, sel );
        }
    }
    
    private class DetailsSection implements IDetailsPage
    {
        private MasterDetailsContentNodePart node;
        private Composite composite;
        private final Listener listener;
        private List<SectionPart> sections;
        private List<SectionPresentation> presentations;
        
        public DetailsSection()
        {
            this.listener = new FilteredListener<PartVisibilityEvent>()
            {
                @Override
                protected void handleTypedEvent( final PartVisibilityEvent event )
                {
                    refreshSections();
                }
            };
            
            this.sections = Collections.emptyList();
        }
        
        public void initialize( final IManagedForm form ) 
        {
        }
    
        public final void createContents( final Composite parent ) 
        {
            this.composite = parent;
            
            this.composite.setLayout( glayout( 2, 0, 0 ) );
            this.composite.setBackground( getPart().getSwtResourceCache().color( org.eclipse.sapphire.Color.WHITE ) );
            this.composite.setBackgroundMode( SWT.INHERIT_DEFAULT );
            
            refreshSections();
        }
        
        public void commit(boolean onSave) {
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
    
        public void selectionChanged( final IFormPart part, 
                                      final ISelection selection ) 
        {
            final IStructuredSelection ssel = (IStructuredSelection) selection;
            
            if( ssel.size() == 1 ) 
            {
                this.node = (MasterDetailsContentNodePart) ssel.getFirstElement();
            }
            else
            {
                this.node = null;
            }
            
            refreshSections();
        }
        
        private void refreshSections()
        {
            if( this.presentations != null )
            {
                for( final Presentation presentation : this.presentations )
                {
                    presentation.dispose();
                }
            }
            
            if( this.composite.getChildren().length > 0 )
            {
                throw new IllegalStateException();
            }
            
            for( final SectionPart section : this.sections )
            {
                section.detach( this.listener );
            }
            
            if( this.node != null )
            {
                this.sections = this.node.getSections();
            }
            else
            {
                this.sections = ListFactory.empty();
            }
            
            final ListFactory<SectionPresentation> presentationsListFactory = ListFactory.start();
            
            for( final SectionPart section : this.sections )
            {
                section.attach( this.listener );
                
                if( section.visible() )
                {
                    final SectionPresentation presentation = (SectionPresentation) section.createPresentation( MasterDetailsEditorPage.this.presentation, this.composite );
                    presentationsListFactory.add( presentation );
                    presentation.render();
                }
            }
            
            this.presentations = presentationsListFactory.result();
            
            this.composite.getParent().layout( true, true );
        }
        
        public void dispose()
        {
            for( SectionPart section : this.sections )
            {
                section.detach( this.listener );
            }
        }
    }
    
    private static final class TreeViewerUpdateJob implements Runnable
    {
        private final TreeViewer tree;
        private final Object element;
        
        public TreeViewerUpdateJob( final TreeViewer tree,
                                    final Object element )
        {
            this.tree = tree;
            this.element = element;
        }

        public void run()
        {
            this.tree.update( this.element, null );
        }
    }
    
}
