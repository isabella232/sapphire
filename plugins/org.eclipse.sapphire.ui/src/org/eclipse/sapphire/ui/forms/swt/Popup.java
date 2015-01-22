/******************************************************************************
 * Copyright (c) 2015 Tasktop Technologies, Oracle and Other Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Benjamin Pasero - initial implementation in Mylyn as AbstractNotificationPopup
 *    Tasktop Technologies - initial implementation in Mylyn as AbstractNotificationPopup
 *    Ling Hao - adaptation to Sapphire requirements
 *    Konstantin Komissarchik - [357714] Display validation messages for content outline nodes
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.IFormColors;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public abstract class Popup extends Window 
{
    private FormColors colors;

    private Shell shell;

    private Region lastUsedRegion;

    private Image lastUsedBgImage;
    
    /**
     * Flags indicating whether we are listening for shell deactivate events,
     * either those or our parent's. Used to prevent closure when a menu command
     * is chosen or a secondary popup is launched.
     */
    private boolean listenToDeactivate;

    private boolean listenToParentDeactivate;

    private Listener parentDeactivateListener;
    
    /**
     * The control representing the main dialog area.
     */
    private Control contentArea;
    
    /**
     * The initial position
     */
    private final Point position;

    public Popup(Shell shell, Point position) {
        this(shell, position, SWT.NO_TRIM | SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL);
    }

    public Popup(Shell shell, Point position, int style) {
        super(shell);
        
        setShellStyle(style);
        this.position = position;
        this.colors = new FormColors(shell.getDisplay());
    }

    /**
     * Override to populate with notifications.
     * 
     * @param parent
     */
    protected Control createContentArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults()
                .margins(PopupDialog.POPUP_MARGINWIDTH, PopupDialog.POPUP_MARGINHEIGHT)
                .spacing(PopupDialog.POPUP_HORIZONTALSPACING, PopupDialog.POPUP_VERTICALSPACING)
                .applyTo(composite);
        GridDataFactory.fillDefaults()
                .grab(true, true)
                .applyTo(composite);
        return composite;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);

        this.shell = newShell;
        newShell.setBackground(this.colors.getBorderColor());
        
        this.shell.addListener(SWT.Deactivate, new Listener() {
            public void handleEvent(Event event) {
                /*
                 * Close if we are deactivating and have no child shells. If we
                 * have child shells, we are deactivating due to their opening.
                 * On X, we receive this when a menu child (such as the system
                 * menu) of the shell opens, but I have not found a way to
                 * distinguish that case here. Hence bug #113577 still exists.
                 */
                if (Popup.this.listenToDeactivate && event.widget == getShell()
                        && getShell().getShells().length == 0) {
                    asyncClose();
                } else {
                    /*
                     * We typically ignore deactivates to work around
                     * platform-specific event ordering. Now that we've ignored
                     * whatever we were supposed to, start listening to
                     * deactivates. Example issues can be found in
                     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=123392
                     */
                    Popup.this.listenToDeactivate = true;
                }
            }
        });
        // Set this true whenever we activate. It may have been turned
        // off by a menu or secondary popup showing.
        this.shell.addListener(SWT.Activate, new Listener() {
            public void handleEvent(Event event) {
                // ignore this event if we have launched a child
                if (event.widget == getShell()
                        && getShell().getShells().length == 0) {
                    Popup.this.listenToDeactivate = true;
                    // Typically we start listening for parent deactivate after
                    // we are activated, except on the Mac, where the deactivate
                    // is received after activate.
                    // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=100668
                    Popup.this.listenToParentDeactivate = !Util.isMac();
                }
            }
        });

        if ((getShellStyle() & SWT.ON_TOP) != 0 && this.shell.getParent() != null) {
            this.parentDeactivateListener = new Listener() {
                public void handleEvent(Event event) {
                    if (Popup.this.listenToParentDeactivate) {
                        asyncClose();
                    } else {
                        // Our first deactivate, now start listening on the Mac.
                        Popup.this.listenToParentDeactivate = Popup.this.listenToDeactivate;
                    }
                }
            };
            this.shell.getParent().addListener(SWT.Deactivate, this.parentDeactivateListener);
        }

    }
    
    private void asyncClose() {
        // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=152010
        getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                close();
            }
        });
    }

    @Override
    public void create() {
        super.create();
        addRegion(this.shell);
    }

    private void addRegion(Shell shell) {
        Region region = new Region();
        Point s = shell.getSize();

        /* Add entire Shell */
        region.add(0, 0, s.x, s.y);

        /* Subtract Top-Left Corner */
        region.subtract(0, 0, 1, 1);

        /* Subtract Top-Right Corner */
        region.subtract(s.x - 1, 0, 1, 1);

        /* Subtract Bottom-Left Corner */
        region.subtract(0, s.y - 1, 1, 1);

        /* Subtract Bottom-Right Corner */
        region.subtract(s.x - 1, s.y - 1, 1, 1);

        /* Dispose old first */
        if (shell.getRegion() != null) {
            shell.getRegion().dispose();
        }

        /* Apply Region */
        shell.setRegion(region);

        /* Remember to dispose later */
        this.lastUsedRegion = region;
    }

    @Override
    public int open() {
        if (this.shell == null || this.shell.isDisposed()) {
            this.shell = null;
            create();
            this.shell = getShell();
        }

        // limit the shell size to the display size
        constrainShellSize();
        
        // initialize flags for listening to deactivate
        this.listenToDeactivate = false;
        this.listenToParentDeactivate = false;

        // open the window
        
        final Control initialFocusControl = getFocusControl();
        
        if( initialFocusControl == null )
        {
            // Opening the shell in this way ensures that it does not take focus from the active shell.
            
            this.shell.setVisible( true );
        }
        else
        {
            this.shell.open();
            initialFocusControl.setFocus();
        }
        
        Display display = this.shell.getDisplay();

        while (!this.shell.isDisposed()) {
            try {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if (!display.isDisposed()) display.update();

        return Window.OK;
    }

    @Override
    protected Point getInitialLocation( Point size ) {
        if( this.position == null ) {
            return this.shell.getLocation();
        }
        
        return this.position;
    }
    

    /**
     * Returns the control that should get initial focus. Subclasses may
     * override this method.
     * 
     * @return the Control that should receive focus when the popup opens.
     */
    protected Control getFocusControl() {
        return this.contentArea;
    }

    @Override
    protected Control createContents(Composite parent) {
        ((GridLayout) parent.getLayout()).marginWidth = 1;
        ((GridLayout) parent.getLayout()).marginHeight = 1;

        /* Outer Composite holding the controls */
        final Composite outerCircle = new Composite(parent, SWT.NO_FOCUS);
        outerCircle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        outerCircle.setBackgroundMode(SWT.INHERIT_FORCE);

        outerCircle.addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(ControlEvent e) {
                Rectangle clArea = outerCircle.getClientArea();
                Popup.this.lastUsedBgImage = new Image(outerCircle.getDisplay(), clArea.width, clArea.height);
                GC gc = new GC(Popup.this.lastUsedBgImage);

                /* Gradient */
                drawGradient(gc, clArea);

                gc.dispose();

                Image oldBGImage = outerCircle.getBackgroundImage();
                outerCircle.setBackgroundImage(Popup.this.lastUsedBgImage);

                if (oldBGImage != null) {
                    oldBGImage.dispose();
                }
            }

            private void drawGradient(GC gc, Rectangle clArea) {
                gc.setForeground(Popup.this.colors.getBackground());
                gc.setBackground(Popup.this.colors.getColor(IFormColors.TB_BG));
                gc.fillGradientRectangle(clArea.x, clArea.y, clArea.width, clArea.height, true);
            }
        });

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;

        outerCircle.setLayout(layout);

        /* Outer composite to hold content controls */
        Composite outerContentCircle = new Composite(outerCircle, SWT.NONE);
        outerContentCircle.setBackgroundMode(SWT.INHERIT_FORCE);

        layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;

        outerContentCircle.setLayout(layout);
        outerContentCircle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        outerContentCircle.setBackground(outerCircle.getBackground());

        /* Middle composite to show a 1px black line around the content controls */
        Composite middleContentCircle = new Composite(outerContentCircle, SWT.NO_FOCUS);
        middleContentCircle.setBackgroundMode(SWT.INHERIT_FORCE);

        layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.marginTop = 1;

        middleContentCircle.setLayout(layout);
        middleContentCircle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        middleContentCircle.setBackground(this.colors.getBorderColor());

        /* Inner composite containing the content controls */
        Composite innerContent = new Composite(middleContentCircle, SWT.NO_FOCUS);
        innerContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        innerContent.setBackgroundMode(SWT.INHERIT_FORCE);

        layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 5;
        layout.marginLeft = 5;
        layout.marginRight = 5;
        innerContent.setLayout(layout);

        innerContent.setBackground(this.shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));

        /* Content Area */
        this.contentArea = createContentArea(innerContent);

        setNullBackground(outerCircle);

        return outerCircle;
    }

    private void setNullBackground(final Composite outerCircle) {
        for (Control c : outerCircle.getChildren()) {
            c.setBackground(null);
            if (c instanceof Composite) {
                setNullBackground((Composite) c);
            }
        }
    }

    @Override
    public boolean close() {
        // If already closed, there is nothing to do.
        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=127505
        if (getShell() == null || getShell().isDisposed()) {
            return true;
        }

        if (this.lastUsedRegion != null) {
            this.lastUsedRegion.dispose();
        }
        if (this.lastUsedBgImage != null && !this.lastUsedBgImage.isDisposed()) {
            this.lastUsedBgImage.dispose();
        }

        if (this.parentDeactivateListener != null) {
            getShell().getParent().removeListener(SWT.Deactivate, this.parentDeactivateListener);
            this.parentDeactivateListener = null;
        }
        return super.close();
    }

}
