/******************************************************************************
 * Copyright (c) 2015 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [329114] rewrite context help binding feature
 *    Shenxue Zhou - [365019] SapphireDiagramEditor does not work on non-workspace files 
 *    Gregory Amerson - [372816] Provide adapt mechanism for SapphirePart
 *    Gregory Amerson - [346172] Support zoom, print and save as image actions in the diagram editor
 *    Gregory Amerson - [444202] Lazy loading of editor pages
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.help.IContext;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.CorruptedResourceExceptionInterceptor;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.def.DefinitionLoader.Reference;
import org.eclipse.sapphire.ui.def.EditorPageDef;
import org.eclipse.sapphire.ui.def.PartDef;
import org.eclipse.sapphire.ui.diagram.def.DiagramEditorPageDef;
import org.eclipse.sapphire.ui.forms.FormEditorPageDef;
import org.eclipse.sapphire.ui.forms.MasterDetailsEditorPageDef;
import org.eclipse.sapphire.ui.forms.PropertiesViewContributionPart;
import org.eclipse.sapphire.ui.forms.swt.EditorPagePresentation;
import org.eclipse.sapphire.ui.forms.swt.FormEditorPage;
import org.eclipse.sapphire.ui.forms.swt.MasterDetailsEditorPage;
import org.eclipse.sapphire.ui.forms.swt.SapphireEditorFormPage;
import org.eclipse.sapphire.ui.forms.swt.SwtResourceCache;
import org.eclipse.sapphire.ui.forms.swt.internal.SapphirePropertySheetPage;
import org.eclipse.sapphire.ui.forms.swt.internal.text.SapphireFormText;
import org.eclipse.sapphire.ui.internal.PartServiceContext;
import org.eclipse.sapphire.ui.internal.SapphireActionManager;
import org.eclipse.sapphire.ui.internal.SapphireEditorContentOutline;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.internal.EditorActionBars;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public abstract class SapphireEditor

    extends FormEditor
    implements ISapphirePart
    
{
    @Text( "Associated resources are not accessible" )
    private static LocalizableText resourceNotAccessible;
    
    @Text( "Editor {0} failed to instantiate its model" )
    private static LocalizableText failedToCreateModel;
    
    @Text( "Error" )
    private static LocalizableText errorPageTitle;
    
    @Text( "Failed to find a definition '{0}'" )
    private static LocalizableText failedToFindDefinition;
    
    static
    {
        LocalizableText.init( SapphireEditor.class );
    }

    private static class SapphireEditorActionBarContributor extends MultiPageEditorActionBarContributor 
	{
		private MultiPageEditorPart multiPageEditor = null;
		
		public void setActiveEditor(IEditorPart targetEditor) 
		{
			if (targetEditor instanceof MultiPageEditorPart) 
			{
				this.multiPageEditor = (MultiPageEditorPart) targetEditor;
			}

			super.setActiveEditor(targetEditor);		
		}
		
		@Override
		public void setActivePage(IEditorPart activeEditor) 
		{
			ISapphireEditorActionContributor actionContributor = null;
			ITextEditor textEditor = null;
			if (this.multiPageEditor != null) 
			{
				if (activeEditor instanceof ISapphireEditorActionContributor)
				{
					actionContributor = (ISapphireEditorActionContributor)activeEditor;
				}
				else if (activeEditor instanceof ITextEditor)
				{
					textEditor = (ITextEditor)activeEditor;
				}
				else if (activeEditor == null)
				{
					Object obj = this.multiPageEditor.getSelectedPage();
					if (obj instanceof ISapphireEditorActionContributor)
					{
						actionContributor = (ISapphireEditorActionContributor)obj;
					}
				}
			}

			IActionBars actionBars = getActionBars();
			if (actionBars != null && (actionContributor != null || textEditor != null))
			{
				/** The global actions to be connected with editor actions */
				if (actionContributor != null)
				{				
					actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), 
							actionContributor.getAction(ActionFactory.DELETE.getId()));
					actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), 
							actionContributor.getAction(ActionFactory.SELECT_ALL.getId()));
					actionBars.setGlobalActionHandler(ActionFactory.PRINT.getId(), 
							actionContributor.getAction(ActionFactory.PRINT.getId()));
				}
				else if (textEditor != null)
				{
					actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), 
							textEditor.getAction(ActionFactory.DELETE.getId()));
					actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), 
							textEditor.getAction(ActionFactory.SELECT_ALL.getId()));
					actionBars.setGlobalActionHandler(ActionFactory.PRINT.getId(), 
							textEditor.getAction(ActionFactory.PRINT.getId()));					
				}
				actionBars.updateActionBars();
			}

		}
	}	
	
    private static final String PREFS_LAST_ACTIVE_PAGE = "LastActivePage"; //$NON-NLS-1$
    private static final String PREFS_GLOBAL = "Global"; //$NON-NLS-1$
    private static final String PREFS_INSTANCE_BY_URI = "InstanceByUri"; //$NON-NLS-1$
    private static final String PREFS_INSTANCE_BY_EDITOR_INPUT_TYPE = "InstanceByEditorInputType"; //$NON-NLS-1$
    
    private final String pluginId;
    private Element model;
    private IResourceChangeListener fileChangeListener;
    private SwtResourceCache imageCache;
    private SapphireEditorContentOutline outline;
    private SapphireActionManager actionsManager;
    private SapphirePropertySheetPage propertiesViewPage;
    private Listener propertiesViewContributionChangeListener;
    private PartServiceContext serviceContext;
    private boolean ignorePageChange;

    public SapphireEditor()
    {
        this.pluginId = FrameworkUtil.getBundle( getClass() ).getSymbolicName();
        this.imageCache = new SwtResourceCache();
        this.outline = null;
        this.actionsManager = new SapphireActionManager( this, getActionContexts() );
    }
    
    @Override
    public Composite getContainer()
    {
        return super.getContainer();
    }

    public PartDef definition()
    {
        return null;
    }
    
    public final Element getModelElement()
    {
        if( this.model == null )
        {
            this.model = createModel();
            adaptModel( this.model );
        }

        return this.model;
    }

    public final Element getLocalModelElement()
    {
        return getModelElement();
    }

    protected abstract Element createModel();
    
    protected void adaptModel( final Element model )
    {
        final CorruptedResourceExceptionInterceptor interceptor 
            = new CorruptedResourceExceptionInterceptorImpl( getEditorSite().getShell() );
        
        this.model.resource().setCorruptedResourceExceptionInterceptor( interceptor );
    }
    
    public final Preferences getGlobalPreferences( final boolean createIfNecessary )
    
        throws BackingStoreException
        
    {
        final Preferences prefs = getPreferencesRoot( createIfNecessary );
        
        if( prefs != null && ( prefs.nodeExists( PREFS_GLOBAL ) || createIfNecessary ) )
        {
            return prefs.node( PREFS_GLOBAL );
        }
        
        return null;
    }
    
    public final Preferences getInstancePreferences( final boolean createIfNecessary )
    
        throws BackingStoreException
        
    {
        final IEditorInput editorInput = getEditorInput();
        final String level1;
        final String level2;
        
        if( editorInput instanceof IURIEditorInput )
        {
            level1 = PREFS_INSTANCE_BY_URI;
            
            final URI uri = ( (IURIEditorInput) editorInput ).getURI();
            
            if( uri != null )
            {
                level2 = ( (IURIEditorInput) editorInput ).getURI().toString();
            }
            else
            {
                level2 = "$#%**invalid**%#$";
            }
        }
        else
        {
            level1 = PREFS_INSTANCE_BY_EDITOR_INPUT_TYPE;
            level2 = editorInput.getClass().getName();
        }
        
        Preferences prefs = getPreferencesRoot( createIfNecessary );
        
        if( prefs != null && ( prefs.nodeExists( level1 ) || createIfNecessary ) )
        {
            prefs = prefs.node( level1 );
            
            if( prefs.nodeExists( level2 ) || createIfNecessary )
            {
                return prefs.node( level2 );
            }
        }
        
        return null;
    }
    
    private final Preferences getPreferencesRoot( final boolean createIfNecessary )
    
        throws BackingStoreException
        
    {
        /*
         * Replace "new InstanceScope()" with "InstanceScope.INSTANCE" once Sapphire no longer needs to
         * support Eclipse 3.6.x releases.
         */
        
        @SuppressWarnings( "deprecation" )
        final IScopeContext scope = new InstanceScope();
        
        final Preferences prefs = scope.getNode( this.pluginId );
        final String editorId = getClass().getName();
        
        if( prefs.nodeExists( editorId ) || createIfNecessary )
        {
            return prefs.node( editorId );
        }
        
        return null;
    }

    private final int getLastActivePage()
    {
        int lastActivePage = 0;
        
        try
        {
            final Preferences prefs = getInstancePreferences( false );
            
            if( prefs != null )
            {
                lastActivePage = prefs.getInt( PREFS_LAST_ACTIVE_PAGE, lastActivePage );
            }
        }
        catch( BackingStoreException e )
        {
            Sapphire.service( LoggingService.class ).log( e );
        }
        
        return lastActivePage;
    }

    private final void setLastActivePage( final int index )
    {
        try
        {
            final Preferences prefs = getInstancePreferences( true );
            
            if( prefs != null )
            {
                prefs.putInt( PREFS_LAST_ACTIVE_PAGE, index );
                prefs.flush();
            }
        }
        catch( BackingStoreException e )
        {
            Sapphire.service( LoggingService.class ).log( e );
        }
    }

    public IFile getFile()
    {
        final IEditorInput editorInput = getEditorInput();
        
        if( editorInput instanceof FileEditorInput )
        {
            return ( (FileEditorInput) editorInput ).getFile();
        }
        else
        {
            return null;
        }
    }

    public final IProject getProject()
    {
        final IFile ifile = getFile();
        return ( ifile == null ? null : ifile.getProject() );
    }
    
    public final void init( final IEditorSite site, 
                            final IEditorInput input )
    
        throws PartInitException
        
    {
        super.init( site, input );
        
        doSetInput( input );
    }
    
    protected final void setInput( final IEditorInput input ) 
    {
        doSetInput( input );
        super.setInput( input );
    }

    @Override
    protected final void setInputWithNotify( final IEditorInput input ) 
    {
        doSetInput( input );
        super.setInputWithNotify( input );
    }

    private void doSetInput( final IEditorInput input )
    {
        setPartName( input.getName() );
    }

    public int addEditorPage(IEditorPart page) throws PartInitException {
        return addPage(page, getEditorInput());
    }

    public void addEditorPage(int index, IEditorPart page) throws PartInitException {
        addPage(index, page, getEditorInput());
    }
    
    @Override
    
    public int addPage( final IEditorPart page, 
                        final IEditorInput input )
    
        throws PartInitException
        
    {
        int index = super.addPage(page, input);
        setPageText( index, page.getTitle() );
        return index;
    }

    @Override
    
    public void addPage( final int index, 
                         final IEditorPart page, 
                         final IEditorInput input) 
    
        throws PartInitException
        
    {
        super.addPage(index, page, input);
        setPageText( index, page.getTitle() );
    }

    /**
     * Adds a page that will be loaded from its definition when the user first opens it. When this method
     * is used, the editor will load the page definition by calling {@link #getDefinition(String)}. By default,
     * the definitions are loaded from an sdef file with the same name as the editor class. If the default
     * behavior is inadequate, either {@link #getDefinitionLoader()} or {@link #getDefinition(String)} should be
     * overridden.
     *
     * @since 8.1
     * @param index the position of the page in the editor's page list
     * @param pageName the localizable name of the page
     * @param pageDefinitionId the id of the page definition
     * @throws IllegalArgumentException if index is less than -1 or more than current page count
     * @throws IllegalArgumentException if pageName is null
     */
    
    protected final void addDeferredPage( final int index, final String pageName, final String pageDefinitionId )
    {
        if( index < -1 )
        {
            throw new IllegalArgumentException();
        }
        
        if( pageName == null )
        {
            throw new IllegalArgumentException();
        }
        
        final DeferredPage page = new DeferredPage( getContainer(), pageDefinitionId );
        
        if( index == -1 )
        {
            addPage( page );
            setPageText( this.pages.size() - 1, pageName );
        }
        else
        {
            addPage( index, page );
            setPageText( index, pageName );
        }
    }
    
    /**
     * Adds a page that will be loaded from its definition when the user first opens it. When this method
     * is used, the editor will load the page definition by calling {@link #getDefinition(String)}. By default,
     * the definitions are loaded from an sdef file with the same name as the editor class. If the default
     * behavior is inadequate, either {@link #getDefinitionLoader()} or {@link #getDefinition(String)} should be
     * overridden.
     *
     * @since 8.1
     * @param pageName the localizable name of the page
     * @param pageDefinitionId the id of the page definition
     * @throws IllegalArgumentException if pageName is null
     */
    
    protected final void addDeferredPage( final String pageName, final String pageDefinitionId )
    {
        addDeferredPage( -1, pageName, pageDefinitionId );
    }
    
    /**
     * Called when the editor should create its pages. The default implementation calls {@link #createSourcePages()},
     * {@link #createFormPages()} and {@link #createDiagramPages()} methods, in that order.
     * 
     * @since 8.1
     * @throws PartInitException if a page could not be created
     */
    
    protected void createEditorPages() throws PartInitException
    {
        createSourcePages();
        
        // For backwards compatibility, if createModel() is overridden, we need to ensure that the method is
        // called before createFormPages() and createDiagramPages(). A concrete example of why a subclass may
        // depend on this order is to store the model in a field as part of createModel() and then later use
        // it when creating pages. Since this approach does not invoke getModelElement(), the lazy creation of
        // the model is not triggered.
        
        if( isCreateModelOverridden() )
        {
            getModelElement();
        }
        
        createFormPages();
        createDiagramPages();
    }
    
    private boolean isCreateModelOverridden()
    {
        try
        {
            if( getClass().getDeclaredMethod( "createModel" ).getDeclaringClass() != SapphireEditor.class )
            {
                return true;
            }
        }
        catch( final Exception e ) {}
        
        return false;
    }

    protected void createSourcePages() throws PartInitException
    {
    }

    protected void createFormPages() throws PartInitException
    {
    }
    
    protected void createDiagramPages() throws PartInitException
    {
    }

    @Override
    protected final void addPages() 
    {
    	// Insert an action bar contributor if none is specified in the editor
		if (getEditorSite().getActionBarContributor() == null)
		{
			IActionBars actionBars = getEditorSite().getActionBars();
			EditorActionBars editorActionBars = (EditorActionBars)actionBars;
			SapphireEditorActionBarContributor actionBarContributor = new SapphireEditorActionBarContributor();
			actionBarContributor.init(actionBars, this.getSite().getPage());
			editorActionBars.setEditorContributor(actionBarContributor);
		}    	

		String error = null;
        
        final IFile file = getFile();
        
        if( file != null && ! file.isAccessible() )
        {
            error = resourceNotAccessible.text();
        }

        if( error == null )
        {
            try
            {
                createEditorPages();
            }
            catch( PartInitException e )
            {
                Sapphire.service( LoggingService.class ).log( e );
            }

            createFileChangeListener();

            setActivePage( getLastActivePage() );
        }
        else
        {
            final Composite page = new Composite( getContainer(), SWT.NONE );
            page.setLayout( glayout( 1 ) );
            page.setBackground( getSite().getShell().getDisplay().getSystemColor( SWT.COLOR_WHITE ) );

            final SapphireFormText message = new SapphireFormText( page, SWT.NONE );
            message.setLayoutData( gd() );
            message.setBackground( getSite().getShell().getDisplay().getSystemColor( SWT.COLOR_WHITE ) );
            message.setText( error, false, false );
            
            addPage( page );
            setPageText( 0, errorPageTitle.text() );
        }
    }
    
    /**
     * Returns the definition loader to be used with this editor. The default implementation calls
     * <code>DefinitionLoader.sdef( getClass() )</code> and returns the result. This will load the definition
     * from an sdef file with the same name as the editor class.
     * 
     * @since 8.1
     */
    
    protected DefinitionLoader getDefinitionLoader()
    {
        return DefinitionLoader.sdef( getClass() );
    }

    /**
     * Returns the page definition corresponding to the specified id. The default implementation relies on
     * the {@link #getDefinitionLoader()} method.
     * 
     * @since 8.1
     * @param pageDefinitionId the page definition id or null to load the first page definition that's found
     */
    
    protected Reference<EditorPageDef> getDefinition( final String pageDefinitionId )
    {
        return getDefinitionLoader().page( pageDefinitionId );
    }

    /**
     * Creates an editor page based on the page definition.
     *
     * @since 8.1
     * @param pageDefinitionId the page definition id
     * @return the created page
     * @throws IllegalArgumentException if the definition is not found
     */
    
    protected IEditorPart createPage( final String pageDefinitionId )
    {
        IEditorPart page = null;

        final Reference<EditorPageDef> definition = getDefinition( pageDefinitionId );

        if( definition != null )
        {
            page = createPage( definition );
        }
        else
        {
            throw new IllegalArgumentException( failedToFindDefinition.format( pageDefinitionId ) );
        }

        return page;
    }

    /**
     * Creates an editor page based on the page definition.
     *
     * @since 8.1
     * @param definition the page definition
     * @return the created page
     */
    
    protected IEditorPart createPage( final Reference<EditorPageDef> definition )
    {
        IEditorPart page = null;

        final EditorPageDef def = definition.resolve();

        if( def instanceof MasterDetailsEditorPageDef )
        {
            page = new MasterDetailsEditorPage( this, getModelElement(), definition );
        }
        else if( def instanceof FormEditorPageDef )
        {
            page = new FormEditorPage( this, getModelElement(), definition );
        }
        else if( def instanceof DiagramEditorPageDef )
        {
            final Bundle bundle = Platform.getBundle( "org.eclipse.sapphire.ui.swt.gef" );
            
            if( bundle != null )
            {
                try
                {
                    final Class<?> cl = bundle.loadClass( "org.eclipse.sapphire.ui.swt.gef.SapphireDiagramEditor" );
                    final Constructor<?> constructor = cl.getConstructors()[ 0 ];
                    page = (IEditorPart) constructor.newInstance( this, getModelElement(), definition );
                }
                catch( final Exception e )
                {
                    Sapphire.service( LoggingService.class ).log( e );
                }
            }
        }
        else
        {
            throw new IllegalStateException();
        }

        return page;
    }

    public final Object getPage()
    {
        final int pageIndex = getActivePage();
        
        if( pageIndex == -1 )
        {
            return null;
        }
        else
        {
            return this.pages.get( pageIndex );
        }
    }
    
    public final void showPage( final Object page )
    {
        final int index = this.pages.indexOf( page );
        setActivePage( index );
    }
    
    public final void showPage( final SapphireEditorPagePart editorPagePart )
    {
        for( int i = 0, n = getPageCount(); i < n; i++ )
        {
            final Object page = this.pages.get( i );
            
            if( page instanceof EditorPagePresentation && ( (EditorPagePresentation) page ).getPart() == editorPagePart )
            {
                setActivePage( i );
                return;
            }
        }
    }

    @Override
    protected void pageChange( final int pageIndex )
    {
        if( this.ignorePageChange )
        {
            return;
        }

        final Object newPage = this.pages.get( pageIndex );

        if( newPage instanceof DeferredPage )
        {
            BusyIndicator.showWhile
            (
                getContainer().getDisplay(),
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final IEditorPart page = createPage( ( (DeferredPage) newPage ).getDefinitionId() );
                        
                        if( page != null )
                        {
                            try
                            {
                                if( page instanceof IFormPage )
                                {
                                    addPage( pageIndex, (IFormPage) page );
                                }
                                else
                                {
                                    addPage( pageIndex, page, getEditorInput() );
                                }
    
                                for( int i = 0; i < SapphireEditor.this.pages.size(); i++ )
                                {
                                    final Object p = SapphireEditor.this.pages.get( i );
    
                                    if( p == newPage )
                                    {
                                        SapphireEditor.this.ignorePageChange = true;
                                        removePage( i );
                                        SapphireEditor.this.ignorePageChange = false;
                                        break;
                                    }
                                }
    
                                setActivePage( pageIndex );
                            }
                            catch( final PartInitException e )
                            {
                                Sapphire.service( LoggingService.class ).log( e );
                            }
                        }
                    }
                }
            );
        }

        super.pageChange( pageIndex );
        
        setLastActivePage( pageIndex );
        
        if( this.outline != null && ! this.outline.isDisposed() )
        {
            this.outline.refresh();
        }
        
        refreshPropertiesViewContribution();
        
        final Object page = this.pages.get( pageIndex );
        
        if( page instanceof SapphireEditorFormPage )
        {
            ( (SapphireEditorFormPage) page ).setFocus();
        }
    }

    public void doSave( final IProgressMonitor monitor )
    {
        if( this.model != null )
        {
            try
            {
                this.model.resource().save();
            }
            catch( ResourceStoreException e )
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
        }
        
        final Iterator<?> pages = this.pages.iterator();

        while( pages.hasNext() )
        {
            final Object page = pages.next();

            if( page instanceof IEditorPart )
            {
                ( (IEditorPart) page ).doSave( new NullProgressMonitor() );;
            }
        }
    }

    public void doSaveAs() 
    {
        throw new UnsupportedOperationException();
    }
    
    public boolean isSaveAsAllowed() 
    {
        return false;
    }
    
    protected final void createFileChangeListener()
    {
        this.fileChangeListener = new IResourceChangeListener()
        {
            public void resourceChanged( final IResourceChangeEvent event )
            {
                handleFileChangedEvent( event );
            }
        };
        
        ResourcesPlugin.getWorkspace().addResourceChangeListener( this.fileChangeListener, IResourceChangeEvent.POST_CHANGE );
    }

    protected final void handleFileChangedEvent( final IResourceChangeEvent event )
    {
        final IResourceDelta delta = event.getDelta();
        
        if( delta != null && getFile() != null )
        {
            final IResourceDelta localDelta = delta.findMember( getFile().getFullPath() );
            
            if( localDelta != null )
            {
                PlatformUI.getWorkbench().getDisplay().asyncExec
                (
                    new Runnable()
                    {
                        public void run()
                        {
                            if( localDelta.getKind() == IResourceDelta.REMOVED )
                            {
                                getSite().getPage().closeEditor( SapphireEditor.this, false );
                            }
                        }
                    }
                );
            }
        }
    }

    @Override
    public void dispose() 
    {
        super.dispose();
        
        if( this.fileChangeListener != null )
        {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener( this.fileChangeListener );
            this.fileChangeListener = null;
        }
        
        this.imageCache.dispose();
        this.imageCache = null;
        
        this.actionsManager.dispose();
        this.actionsManager = null;
        
        if( this.model != null )
        {
            this.model.dispose();
            this.model = null;
        }
        
        if( this.serviceContext != null )
        {
            this.serviceContext.dispose();
            this.serviceContext = null;
        }
        
        this.outline = null;
        this.propertiesViewPage = null;
        this.propertiesViewContributionChangeListener = null;
    }
    
    @Override
    @SuppressWarnings( "rawtypes" )
    
    public Object getAdapter( final Class type ) 
    {
        if( type == IContentOutlinePage.class )
        {
            if( this.outline == null || this.outline.isDisposed() )
            {
                this.outline = new SapphireEditorContentOutline( this );
            }
            
            return this.outline;
        }
        else if( type == IPropertySheetPage.class )
        {
            if( this.propertiesViewPage == null )
            {
                this.propertiesViewPage = new SapphirePropertySheetPage();
                
                this.propertiesViewContributionChangeListener = new Listener()
                {
                    @Override
                    public void handle( final Event event )
                    {
                        if( event instanceof SapphireEditorPagePart.PropertiesViewContributionChangedEvent )
                        {
                            final SapphireEditorPagePart.PropertiesViewContributionChangedEvent evt
                                = (SapphireEditorPagePart.PropertiesViewContributionChangedEvent) event;
                            
                            SapphireEditor.this.propertiesViewPage.setPart( evt.contribution() );
                        }
                    }
                };
                
                refreshPropertiesViewContribution();
            }
            
            return this.propertiesViewPage;
        }

        return super.getAdapter( type );
    }
    
    public final List<SapphireEditorPagePart> getEditorPageParts()
    {
        final ListFactory<SapphireEditorPagePart> parts = ListFactory.start();
        
        for( Object page : this.pages )
        {
            if( page instanceof EditorPagePresentation )
            {
                parts.add( ( (EditorPagePresentation) page  ).getPart() );
            }
        }
        
        return parts.result();
    }

    public final SapphireEditorPagePart getEditorPagePart( final String name )
    {
        for( Object page : this.pages )
        {
            if( page instanceof EditorPagePresentation && ( (EditorPagePresentation) page ).getPart().definition().getPageName().content().equalsIgnoreCase( name ) )
            {
                return ( (EditorPagePresentation) page ).getPart();
            }
        }
        
        return null;
    }
    
    private void refreshPropertiesViewContribution()
    {
        if( this.propertiesViewPage != null )
        {
            for( SapphireEditorPagePart editorPagePart : getEditorPageParts() )
            {
                editorPagePart.detach( this.propertiesViewContributionChangeListener );
            }
            
            PropertiesViewContributionPart contribution = null;
            
            final Object page = getPage();

            if( page instanceof EditorPagePresentation )
            {
                final SapphireEditorPagePart editorPagePart = ( (EditorPagePresentation) page ).getPart();
                
                editorPagePart.attach( this.propertiesViewContributionChangeListener );
                contribution = editorPagePart.getPropertiesViewContribution();
            }
            
            this.propertiesViewPage.setPart( contribution );
        }
    }
    
    public final IContentOutlinePage getContentOutlineForActivePage()
    {
        final int activePageIndex = getActivePage();
        final Object page = this.pages.get( activePageIndex );
        return getContentOutline( page );
    }
    
    public IContentOutlinePage getContentOutline( final Object page )
    {
        if( page instanceof MasterDetailsEditorPage )
        {
            final MasterDetailsEditorPage mdpage = (MasterDetailsEditorPage) page;
            return mdpage.getContentOutlinePage();
        }
        else if (page instanceof IEditorPart)
        {
        	if (((IEditorPart)page).getAdapter( IContentOutlinePage.class ) != null)
        	{
        		return (IContentOutlinePage)((IEditorPart)page).getAdapter( IContentOutlinePage.class );
        	}
        }
        return null;
    }
    
    // *********************
    // ISapphirePart Methods
    // *********************
    
    public ISapphirePart parent()
    {
        return null;
    }
    
    @SuppressWarnings( "unchecked" )
    public <T> T nearest( final Class<T> partType )
    {
        if( partType.isAssignableFrom( getClass() ) )
        {
            return (T) this;
        }
        else
        {
            return null;
        }
    }
    
    public Set<String> getActionContexts()
    {
        return Collections.emptySet();
    }
    
    public final String getMainActionContext()
    {
        return this.actionsManager.getMainActionContext();
    }
    
    public final SapphireActionGroup getActions()
    {
        return this.actionsManager.getActions();
    }
    
    public final SapphireActionGroup getActions( final String context )
    {
        return this.actionsManager.getActions( context );
    }

    public final SapphireAction getAction( final String id )
    {
        return this.actionsManager.getAction( id );
    }
    
    public Status validation()
    {
        throw new UnsupportedOperationException();
    }
    
    public IContext getDocumentationContext()
    {
        return null;
    }

    public SwtResourceCache getSwtResourceCache()
    {
        return this.imageCache;
    }
    
    public void collectAllReferencedProperties( final Set<PropertyDef> collection )
    {
        throw new UnsupportedOperationException();
    }
    
    public <A> A adapt( final Class<A> adapterType )
    {
        A result = null;

        if( adapterType == IEditorPart.class )
        {
            result = adapterType.cast( getActiveEditor() );
        }

        if( result == null && adapterType == IEditorSite.class )
        {
            result = adapterType.cast( getEditorSite() );
        }

        if( result == null )
        {
            result = adapterType.cast( getAdapter( adapterType ) );
        }

        if( result == null && parent() != null )
        {
            result = parent().adapt( adapterType );
        }

        return result;
    }
    
    public final <S extends Service> S service( final Class<S> serviceType )
    {
        final List<S> services = services( serviceType );
        return ( services.isEmpty() ? null : services.get( 0 ) );
    }

    public final <S extends Service> List<S> services( final Class<S> serviceType )
    {
        if( this.serviceContext == null )
        {
            this.serviceContext = new PartServiceContext( this );
        }
        
        return this.serviceContext.services( serviceType );
    }
    
    private static final class DeferredPage extends Composite
    {
        private final String definitionId;
        
        public DeferredPage( final Composite parent, final String definitionId )
        {
            super( parent, SWT.NONE );
            
            this.definitionId = definitionId;
        }
        
        public String getDefinitionId()
        {
            return this.definitionId;
        }
    }
    
}
