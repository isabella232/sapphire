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

package org.eclipse.sapphire.ui.swt.renderer.actions;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.FileSystemResourceType;
import org.eclipse.sapphire.modeling.annotations.ValidFileSystemResourceType;
import org.eclipse.sapphire.modeling.util.MiscUtil;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.FileExtensionsService;
import org.eclipse.sapphire.services.RelativePathService;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireBrowseActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerDef;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.sapphire.ui.renderers.swt.SwtRendererUtil;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class RelativePathBrowseActionHandler extends SapphireBrowseActionHandler
{
    private static final ImageDescriptor IMG_FILE
        = SwtRendererUtil.createImageDescriptor( RelativePathBrowseActionHandler.class, "File.png" );
    
    private static final ImageDescriptor IMG_FOLDER
        = SwtRendererUtil.createImageDescriptor( RelativePathBrowseActionHandler.class, "Folder.png" );
    
    public static final String ID = "Sapphire.Browse.Path.Relative";
    
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_EXTENSIONS = "extensions";
    public static final String PARAM_LEADING_SLASH = "leading-slash";
    
    private FileExtensionsService fileExtensionService;
    private List<String> staticFileExtensionsList;
    private FileSystemResourceType type;
    private boolean includeLeadingSlash;
    
    @Override
    public void init( final SapphireAction action,
                      final ISapphireActionHandlerDef def )
    {
        super.init( action, def );

        setId( ID );
        setLabel( Resources.label );
        addImage( PlatformUI.getWorkbench().getSharedImages().getImageDescriptor( ISharedImages.IMG_OBJ_FILE ) );
        
        final IModelElement element = getModelElement();
        final ValueProperty property = getProperty();
        
        this.type = null;
        
        final String paramType = def.getParam( PARAM_TYPE );
        
        if( paramType != null )
        {
            if( paramType.equalsIgnoreCase( "file" ) )
            {
                this.type = FileSystemResourceType.FILE;
            }
            else if( paramType.equalsIgnoreCase( "folder" ) )
            {
                this.type = FileSystemResourceType.FOLDER;
            }
        }
        else
        {
            final ValidFileSystemResourceType validFileSystemResourceTypeAnnotation
                = property.getAnnotation( ValidFileSystemResourceType.class );
        
            if( validFileSystemResourceTypeAnnotation != null )
            {
                this.type = validFileSystemResourceTypeAnnotation.value();
            }
        }

        final String staticFileExtensions = def.getParam( PARAM_EXTENSIONS );
        
        if( staticFileExtensions == null )
        {
            this.fileExtensionService = element.service( property, FileExtensionsService.class );
            
            if( this.fileExtensionService == null )
            {
                this.staticFileExtensionsList = Collections.emptyList();
            }
        }
        else
        {
            this.staticFileExtensionsList = new ArrayList<String>();
            
            for( String extension : staticFileExtensions.split( "," ) )
            {
                extension = extension.trim();
                
                if( extension.length() > 0 )
                {
                    this.staticFileExtensionsList.add( extension );
                }
            }
        }

        final String paramLeadingSlash = def.getParam( PARAM_LEADING_SLASH );
        
        if( paramLeadingSlash != null )
        {
            this.includeLeadingSlash = Boolean.parseBoolean( paramLeadingSlash );
        }
        else
        {
            this.includeLeadingSlash = false;
        }
    }
    
    @Override
    protected String browse( final SapphireRenderingContext context )
    {
        final ValueProperty property = getProperty();
        final List<Path> roots = getBasePaths();
        String selectedAbsolutePath = null;
        
        final List<String> extensions;
        
        if( this.fileExtensionService == null )
        {
            extensions = this.staticFileExtensionsList;
        }
        else
        {
            extensions = this.fileExtensionService.extensions();
        }
        
        if( enclosed() )
        {
            final List<IContainer> baseContainers = new ArrayList<IContainer>();
            
            for( Path path : roots )
            {
                final IContainer baseContainer = getWorkspaceContainer( path.toFile() );
                
                if( baseContainer != null )
                {
                    baseContainers.add( baseContainer );
                }
                else
                {
                    break;
                }
            }
            
            final ITreeContentProvider contentProvider;
            final ILabelProvider labelProvider;
            final ViewerComparator viewerComparator;
            final Object input;

            if( roots.size() == baseContainers.size() )
            {
                // All paths are in the Eclipse Workspace. Use the available content and label
                // providers.
            
                contentProvider = new WorkspaceContentProvider( baseContainers );
                labelProvider = new WorkbenchLabelProvider();
                viewerComparator = new ResourceComparator();
                input = ResourcesPlugin.getWorkspace().getRoot();
            }
            else
            {
                // At least one of the roots is not in the Eclipse Workspace. Use custom file
                // system content and label providers.
                
                contentProvider = new FileSystemContentProvider( roots );
                labelProvider = new FileSystemLabelProvider( context );
                viewerComparator = new FileSystemNodeComparator();
                input = new Object();
            }
        
            final ElementTreeSelectionDialog dialog
                = new ElementTreeSelectionDialog( context.getShell(), labelProvider, contentProvider );
            
            dialog.setTitle( property.getLabel( false, CapitalizationType.TITLE_STYLE, false ) );
            dialog.setMessage( createBrowseDialogMessage( property.getLabel( true, CapitalizationType.NO_CAPS, false ) ) );
            dialog.setAllowMultiple( false );
            dialog.setHelpAvailable( false );
            dialog.setInput( input );
            dialog.setComparator( viewerComparator );
            
            final Path currentPathAbsolute = convertToAbsolute( getModelElement().<Path>read( property ).getContent() );
            
            if( currentPathAbsolute != null )
            {
                Object initialSelection = null;
                
                if( contentProvider instanceof WorkspaceContentProvider )
                {
                    final URI uri = currentPathAbsolute.toFile().toURI();
                    final IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();
                    
                    final IFile[] files = wsroot.findFilesForLocationURI( uri );
                    
                    if( files.length > 0 )
                    {
                        final IFile file = files[ 0 ];
                        
                        if( file.exists() )
                        {
                            initialSelection = file;
                        }
                    }
                    
                    if( initialSelection == null )
                    {
                        final IContainer[] containers = wsroot.findContainersForLocationURI( uri );
                        
                        if( containers.length > 0 )
                        {
                            final IContainer container = containers[ 0 ];
                            
                            if( container.exists() )
                            {
                                initialSelection = container;
                            }
                        }                       
                    }
                }
                else
                {
                    initialSelection = ( (FileSystemContentProvider) contentProvider ).find( currentPathAbsolute );
                }
                
                if( initialSelection != null )
                {
                    dialog.setInitialSelection( initialSelection );
                }
            }
            
            if( this.type == FileSystemResourceType.FILE )
            {
                dialog.setValidator( new FileSelectionStatusValidator() );
            }
            else if( this.type == FileSystemResourceType.FOLDER )
            {
                dialog.addFilter( new ContainersOnlyViewerFilter() );
            }
            
            if( ! extensions.isEmpty() )
            {
                dialog.addFilter( new ExtensionBasedViewerFilter( extensions ) );
            }
            
            if( dialog.open() == Window.OK )
            {
                final Object firstResult = dialog.getFirstResult();
                
                if( firstResult instanceof IResource )
                {
                    selectedAbsolutePath = ( (IResource) firstResult ).getLocation().toString();
                }
                else
                {
                    selectedAbsolutePath = ( (FileSystemNode) firstResult ).getFile().getPath();
                }
            }
        }
        else if( this.type == FileSystemResourceType.FOLDER )
        {
            final DirectoryDialog dialog = new DirectoryDialog( context.getShell() );
            dialog.setText( property.getLabel( true, CapitalizationType.FIRST_WORD_ONLY, false ) );
            dialog.setMessage( createBrowseDialogMessage( property.getLabel( true, CapitalizationType.NO_CAPS, false ) ) );
            
            final Value<Path> value = getModelElement().read( property );
            final Path path = value.getContent();
            
            if( path != null )
            {
                dialog.setFilterPath( path.toOSString() );
            }
            else if( roots.size() > 0 )
            {
                dialog.setFilterPath( roots.get( 0 ).toOSString() );
            }
            
            selectedAbsolutePath = dialog.open();
        }
        else
        {
            final FileDialog dialog = new FileDialog( context.getShell() );
            dialog.setText( property.getLabel( true, CapitalizationType.FIRST_WORD_ONLY, false ) );
            
            final Value<Path> value = getModelElement().read( property );
            final Path path = value.getContent();
            
            if( path != null && path.segmentCount() > 1 )
            {
                dialog.setFilterPath( path.removeLastSegments( 1 ).toOSString() );
                dialog.setFileName( path.lastSegment() );
            }
            else if( roots.size() > 0 )
            {
                dialog.setFilterPath( roots.get( 0 ).toOSString() );
            }
            
            if( ! extensions.isEmpty() )
            {
                final StringBuilder buf = new StringBuilder();
                
                for( String extension : extensions )
                {
                    if( buf.length() > 0 )
                    {
                        buf.append( ';' );
                    }
                    
                    buf.append( "*." );
                    buf.append( extension );
                }
                
                dialog.setFilterExtensions( new String[] { buf.toString() } );
            }
            
            selectedAbsolutePath = dialog.open();
        }
    
        if( selectedAbsolutePath != null )
        {
            final Path relativePath = convertToRelative( new Path( selectedAbsolutePath ) );
            
            if( relativePath != null )
            {
                String result = relativePath.toPortableString();
    
                if( this.includeLeadingSlash )
                {
                    result = "/" + result;
                }
                        
                return result;
            }
        }

        return null;
    }
    
    protected List<Path> getBasePaths()
    {
        return getModelElement().service( getProperty(), RelativePathService.class ).roots();
    }
    
    protected boolean enclosed()
    {
        final RelativePathService service = getModelElement().service( getProperty(), RelativePathService.class );
        
        if( service == null )
        {
            return true;
        }
        else
        {
            return service.enclosed();
        }
    }
    
    protected Path convertToRelative( final Path path )
    {
        if( path != null )
        {
            final RelativePathService service = getModelElement().service( getProperty(), RelativePathService.class );
            
            if( service == null )
            {
                if( enclosed() )
                {
                    for( Path root : getBasePaths() )
                    {
                        if( root.isPrefixOf( path ) )
                        {
                            return path.makeRelativeTo( root );
                        }
                    }
                }
                else
                {
                    final String pathDevice = path.getDevice();
                    
                    for( Path root : getBasePaths() )
                    {
                        if( MiscUtil.equal( pathDevice, root.getDevice() ) )
                        {
                            return path.makeRelativeTo( root );
                        }
                    }
                }
            }
            else
            {
                return service.convertToRelative( path );
            }
        }
        
        return null;
    }
    
    protected Path convertToAbsolute( final Path path )
    {
        if( path != null )
        {
            final RelativePathService service = getModelElement().service( getProperty(), RelativePathService.class );
            
            if( service == null )
            {
                if( enclosed() && path.segmentCount() > 0 && path.segment( 0 ).equals( ".." ) )
                {
                    return null;
                }
                
                Path absolute = null;
                
                for( Path root : getBasePaths() )
                {
                    try
                    {
                        final File file = root.append( path ).toFile().getCanonicalFile();
                        absolute = new Path( file.getPath() );
                        
                        if( file.exists() )
                        {
                            break;
                        }
                    }
                    catch( IOException e )
                    {
                        // Intentionally ignoring to continue to the next root. If none of the roots
                        // produce a viable absolute path, a null return from this method signifies
                        // being unable to convert the relative path. That is sufficient.
                    }
                }
                
                return absolute;
            }
            else
            {
                return service.convertToAbsolute( path );
            }
        }
        
        return null;
    }
    
    private static String getFileExtension( final String fileName ) 
    {
        if( fileName == null ) 
        {
            return null;
        }
        
        int dotIndex = fileName.lastIndexOf( '.' );
        
        if( dotIndex < 0 )
        {
            return null;
        }
        
        return fileName.substring( dotIndex + 1 );
    }
    
    private static IContainer getWorkspaceContainer( final File f )
    {
        final IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();
        final IContainer[] wsContainers = wsroot.findContainersForLocationURI( f.toURI() );
        
        if( wsContainers.length > 0 )
        {
            return wsContainers[ 0 ];
        }
        
        return null;
    }
    
    public static final class ContainersOnlyViewerFilter extends ViewerFilter
    {
        public boolean select( final Viewer viewer, 
                               final Object parent, 
                               final Object element ) 
        {
            return ( element instanceof IContainer || 
                     ( element instanceof FileSystemNode && ( (FileSystemNode) element ).getFile().isDirectory() ) ); 
        }
    }

    public static final class ExtensionBasedViewerFilter extends ViewerFilter
    {
        private List<String> extensions;
        
        public ExtensionBasedViewerFilter( final List<String> extensions )
        {
            change( extensions );
        }
        
        public void change( final List<String> extensions )
        {
            this.extensions = new ArrayList<String>( extensions );
        }

        public boolean select( final Viewer viewer, 
                               final Object parent, 
                               final Object element ) 
        {
            if( element instanceof IFile || 
                ( element instanceof FileSystemNode && ( (FileSystemNode) element ).getFile().isFile() ) ) 
            {
                final String extension;
                
                if( element instanceof IFile )
                {
                    extension = ( (IFile) element ).getFileExtension();
                }
                else
                {
                    extension = getFileExtension( ( (FileSystemNode) element ).getFile().getName() );
                }
                
                if( extension != null && extension.length() != 0 )
                {
                    for( String ext : this.extensions )
                    {
                        if( extension.equalsIgnoreCase( ext ) )
                        {
                            return true;
                        }
                    }
                }
                
                return false;
            } 
            else if( element instanceof IContainer ) 
            {
                if( element instanceof IProject && ! ( (IProject) element ).isOpen() )
                {
                    return false;
                }
                
                return true;
            }
            
            return true;
        }
    }

    private static final class FileSelectionStatusValidator implements ISelectionStatusValidator
    {
        private static final IStatus ERROR_STATUS 
            = new Status( IStatus.ERROR, SapphireUiFrameworkPlugin.PLUGIN_ID, MiscUtil.EMPTY_STRING );
        
        private static final IStatus OK_STATUS 
            = new Status( IStatus.OK, SapphireUiFrameworkPlugin.PLUGIN_ID, MiscUtil.EMPTY_STRING );
    
        public IStatus validate( final Object[] selection )
        {
            if( selection.length == 1 )
            {
                final Object sel = selection[ 0 ];
                
                if( sel instanceof IFile || 
                    ( sel instanceof FileSystemNode && ( (FileSystemNode) sel ).getFile().isFile() ) )
                {
                    return OK_STATUS;
                }
            }
            
            return ERROR_STATUS;
        }
    }

    private static final class WorkspaceContentProvider extends WorkbenchContentProvider
    {
        private final List<IContainer> roots;
        
        public WorkspaceContentProvider( final List<IContainer> roots )
        {
            this.roots = roots;
        }
        
        @Override
        public Object[] getElements( final Object element )
        {
            final List<IResource> elements = new ArrayList<IResource>();
            
            if( this.roots.size() == 1 )
            {
                final IContainer root = this.roots.get( 0 );
                
                try
                {
                    for( IResource child : root.members() )
                    {
                        if( child.isAccessible() )
                        {
                            elements.add( child );
                        }
                    }
                }
                catch( CoreException e )
                {
                    SapphireUiFrameworkPlugin.log( e );
                }
            }
            else
            {
                elements.addAll( this.roots );
            }

            return elements.toArray( new IResource[ elements.size() ] );
        }

        @Override
        public Object getParent( final Object element )
        {
            if( ( this.roots.contains( element ) ) || 
                ( this.roots.size() == 1 && this.roots.contains( ( (IResource) element ).getParent() ) ) )
            {
                return null;
            }
            else
            {
                return super.getParent( element );
            }
        }
    }
    
    private static final class FileSystemNode
    {
        private final File file;
        private final FileSystemNode parent;
        private Map<File,FileSystemNode> children; 
        
        public FileSystemNode( final File file,
                               final FileSystemNode parent )
        {
            this.file = file;
            this.parent = parent;
            this.children = Collections.emptyMap();
        }
        
        public File getFile()
        {
            return this.file;
        }
        
        public FileSystemNode getParent()
        {
            return this.parent;
        }
        
        public boolean hasChildren()
        {
            return this.file.isDirectory();
        }
        
        public FileSystemNode[] getChildren()
        {
            if( this.file.isDirectory() )
            {
                final File[] directoryListing = this.file.listFiles();
                
                if( directoryListing != null && directoryListing.length > 0 )
                {
                    final FileSystemNode[] result = new FileSystemNode[ directoryListing.length ];
                    final Map<File,FileSystemNode> newChildrenMap = new HashMap<File,FileSystemNode>();
                    
                    for( int i = 0, n = directoryListing.length; i < n; i++ )
                    {
                        final File f = directoryListing[ i ];
                        
                        FileSystemNode node = this.children.get( f );
                        
                        if( node == null )
                        {
                            node = new FileSystemNode( f, this );
                        }
                        
                        newChildrenMap.put( f, node );
                        result[ i ] = node;
                    }
                    
                    this.children = newChildrenMap;
                    
                    return result;
                }
            }

            return new FileSystemNode[ 0 ];
        }
        
        public FileSystemNode find( final Path path )
        {
            final int pathSegmentCount = path.segmentCount();
            
            if( pathSegmentCount == 0 )
            {
                return this;
            }
            else
            {
                final String firstSegment = path.segment( 0 );
                
                for( FileSystemNode child : getChildren() )
                {
                    if( child.getFile().getName().equals( firstSegment ) )
                    {
                        return child.find( path.removeFirstSegments( 1 ) );
                    }
                }
                
                return null;
            }
        }
    }
    
    private static final class FileSystemContentProvider implements ITreeContentProvider
    {
        private final FileSystemNode[] roots;
        
        public FileSystemContentProvider( final List<Path> roots )
        {
            this.roots = new FileSystemNode[ roots.size() ];
            
            for( int i = 0, n = roots.size(); i < n; i++ )
            {
                this.roots[ i ] = new FileSystemNode( roots.get( i ).toFile(), null );
            }
        }
        
        public FileSystemNode find( final Path path )
        {
            for( FileSystemNode root : this.roots )
            {
                final Path rootPath = new Path( root.getFile().getPath() );
                
                if( rootPath.isPrefixOf( path ) )
                {
                    return root.find( path.makeRelativeTo( rootPath ) );
                }
            }
            
            return null;
        }
        
        public Object[] getElements( final Object element )
        {
            return this.roots;
        }
    
        public Object getParent( final Object element )
        {
            return ( (FileSystemNode) element ).getParent();
        }

        public Object[] getChildren( final Object element )
        {
            return ( (FileSystemNode) element ).getChildren();
        }

        public boolean hasChildren( Object element )
        {
            return ( (FileSystemNode) element ).hasChildren();
        }

        public void inputChanged( final Viewer viewer,
                                  final Object oldInput,
                                  final Object newInput )
        {
        }

        public void dispose()
        {
        }
    }
    
    private static final class FileSystemLabelProvider extends LabelProvider
    {
        private final SapphireRenderingContext context;
        
        public FileSystemLabelProvider( final SapphireRenderingContext context )
        {
            this.context = context;
        }
        
        @Override
        public String getText( final Object element )
        {
            return ( (FileSystemNode) element ).getFile().getName();
        }

        @Override
        public Image getImage( final Object element )
        {
            if( ( (FileSystemNode) element ).getFile().isDirectory() )
            {
                return this.context.getImageCache().getImage( IMG_FOLDER );
            }
            else
            {
                return this.context.getImageCache().getImage( IMG_FILE );
            }
        }
    }
    
    private static final class FileSystemNodeComparator extends ViewerComparator 
    {
        @SuppressWarnings( "unchecked" )
        
        public int compare( final Viewer viewer, 
                            final Object obj1, 
                            final Object obj2 ) 
        {
            final File f1 = ( (FileSystemNode) obj1 ).getFile();
            final File f2 = ( (FileSystemNode) obj2 ).getFile();
            
            final boolean isFile1Directory = f1.isDirectory();
            final boolean isFile2Directory = f2.isDirectory();
            
            if( isFile1Directory == isFile2Directory )
            {
                return Policy.getComparator().compare( f1.getName(), f2.getName() );
            }
            else if( isFile1Directory )
            {
                return -1;
            }
            else
            {
                return 1;
            }
        }
    }
    
    public static final class ResourceComparator extends ViewerComparator 
    {
        @SuppressWarnings( "unchecked" )
        
        public int compare( final Viewer viewer, 
                            final Object obj1, 
                            final Object obj2 ) 
        {
            final IResource r1 = (IResource) obj1;
            final IResource r2 = (IResource) obj2;
            
            final boolean isResource1Container = ( r1 instanceof IContainer );
            final boolean isResource2Container = ( r2 instanceof IContainer );
            
            if( isResource1Container == isResource2Container )
            {
                return Policy.getComparator().compare( r1.getName(), r2.getName() );
            }
            else if( isResource1Container )
            {
                return -1;
            }
            else
            {
                return 1;
            }
        }
    }
    
    private static final class Resources extends NLS 
    {
        public static String label;

        static 
        {
            initializeMessages( RelativePathBrowseActionHandler.class.getName(), Resources.class );
        }
    }

}