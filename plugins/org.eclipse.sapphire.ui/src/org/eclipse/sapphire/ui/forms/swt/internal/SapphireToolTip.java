/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation, Oracle and Other Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Tom Schindl - initial implementation in JFace as ToolTip
 *    Konstantin Komissarchik - adaptation to Sapphire requirements
 *    Konstantin Komissarchik - [357714] Display validation messages for content outline nodes
 *******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.sapphire.ui.forms.swt.Popup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

/**
 * This class gives implementors to provide customized tooltips for any control.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class SapphireToolTip
{
    private Control control;

    private int xShift = 3;

    private int yShift = 0;

    private int popupDelay = 0;

    private int hideDelay = 0;

    private ToolTipOwnerControlListener listener;

    private Map<String,Object> data;

    // Ensure that only one tooltip is active in time
    private static Popup CURRENT_TOOLTIP;
    private static String DATA_SAPPHIRE_TOOLTIP = "Sapphire.ToolTip";

    /**
     * Recreate the tooltip on every mouse move
     */
    public static final int RECREATE = 1;

    /**
     * Don't recreate the tooltip as long the mouse doesn't leave the area
     * triggering the tooltip creation
     */
    public static final int NO_RECREATE = 1 << 1;

    private TooltipHideListener hideListener = new TooltipHideListener();

    private Listener shellListener;

    private boolean hideOnMouseDown = true;

    private boolean respectDisplayBounds = true;

    private boolean respectMonitorBounds = true;

    private int style;

    private Object currentArea;

    /**
     * Create new instance which add TooltipSupport to the widget
     * 
     * @param control
     *            the control on whose action the tooltip is shown
     */
    public SapphireToolTip(Control control) {
        this(control, RECREATE, false);
    }

    /**
     * @param control
     *            the control to which the tooltip is bound
     * @param style
     *            style passed to control tooltip behavior
     * 
     * @param manualActivation
     *            <code>true</code> if the activation is done manually using
     *            {@link #show(Point)}
     * @see #RECREATE
     * @see #NO_RECREATE
     */
    public SapphireToolTip(Control control, int style, boolean manualActivation) {
        this.control = control;
        this.style = style;
        this.control.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                SapphireToolTip.this.data = null;
                deactivate();
            }

        });

        this.listener = new ToolTipOwnerControlListener();
        this.shellListener = new Listener() {
            public void handleEvent(final Event event) {
                if (SapphireToolTip.this.control != null
                        && !SapphireToolTip.this.control.isDisposed()) {
                    SapphireToolTip.this.control.getDisplay().asyncExec(new Runnable() {

                        public void run() 
                        {
                            if( CURRENT_TOOLTIP != null && SapphireToolTip.this.control.getDisplay().getActiveShell() != CURRENT_TOOLTIP.getShell() )
                            {
                                toolTipHide( CURRENT_TOOLTIP, event );
                            }
                        }

                    });
                }
            }
        };

        if (!manualActivation) {
            activate();
        }
    }

    /**
     * Restore arbitrary data under the given key
     * 
     * @param key
     *            the key
     * @param value
     *            the value
     */
    public void setData(String key, Object value) {
        if (this.data == null) {
            this.data = new HashMap<String,Object>();
        }
        this.data.put(key, value);
    }

    /**
     * Get the data restored under the key
     * 
     * @param key
     *            the key
     * @return data or <code>null</code> if no entry is restored under the key
     */
    public Object getData(String key) {
        if (this.data != null) {
            return this.data.get(key);
        }
        return null;
    }

    /**
     * Set the shift (from the mouse position triggered the event) used to
     * display the tooltip.
     * <p>
     * By default the tooltip is shifted 3 pixels to the right.
     * </p>
     * 
     * @param p
     *            the new shift
     */
    public void setShift(Point p) {
        this.xShift = p.x;
        this.yShift = p.y;
    }

    /**
     * Activate tooltip support for this control
     */
    public void activate() {
        deactivate();
        this.control.addListener(SWT.Dispose, this.listener);
        this.control.addListener(SWT.MouseHover, this.listener);
        this.control.addListener(SWT.MouseMove, this.listener);
        this.control.addListener(SWT.MouseExit, this.listener);
        this.control.addListener(SWT.MouseDown, this.listener);
        this.control.addListener(SWT.MouseWheel, this.listener);
    }

    /**
     * Deactivate tooltip support for the underlying control
     */
    public void deactivate() {
        this.control.removeListener(SWT.Dispose, this.listener);
        this.control.removeListener(SWT.MouseHover, this.listener);
        this.control.removeListener(SWT.MouseMove, this.listener);
        this.control.removeListener(SWT.MouseExit, this.listener);
        this.control.removeListener(SWT.MouseDown, this.listener);
        this.control.removeListener(SWT.MouseWheel, this.listener);
    }

    /**
     * Return whether the tooltip respects bounds of the display.
     * 
     * @return <code>true</code> if the tooltip respects bounds of the display
     */
    public boolean isRespectDisplayBounds() {
        return this.respectDisplayBounds;
    }

    /**
     * Set to <code>false</code> if display bounds should not be respected or
     * to <code>true</code> if the tooltip is should repositioned to not
     * overlap the display bounds.
     * <p>
     * Default is <code>true</code>
     * </p>
     * 
     * @param respectDisplayBounds
     */
    public void setRespectDisplayBounds(boolean respectDisplayBounds) {
        this.respectDisplayBounds = respectDisplayBounds;
    }

    /**
     * Return whether the tooltip respects bounds of the monitor.
     * 
     * @return <code>true</code> if tooltip respects the bounds of the monitor
     */
    public boolean isRespectMonitorBounds() {
        return this.respectMonitorBounds;
    }

    /**
     * Set to <code>false</code> if monitor bounds should not be respected or
     * to <code>true</code> if the tooltip is should repositioned to not
     * overlap the monitors bounds. The monitor the tooltip belongs to is the
     * same is control's monitor the tooltip is shown for.
     * <p>
     * Default is <code>true</code>
     * </p>
     * 
     * @param respectMonitorBounds
     */
    public void setRespectMonitorBounds(boolean respectMonitorBounds) {
        this.respectMonitorBounds = respectMonitorBounds;
    }

    /**
     * Should the tooltip displayed because of the given event.
     * <p>
     * <b>Subclasses may overwrite this to get custom behavior</b>
     * </p>
     * 
     * @param event
     *            the event
     * @return <code>true</code> if tooltip should be displayed
     */
    protected boolean shouldCreateToolTip(Event event) {
        if ((this.style & NO_RECREATE) != 0) {
            Object tmp = getToolTipArea(event);

            // No new area close the current tooltip
            if (tmp == null) {
                hide();
                return false;
            }

            boolean rv = !tmp.equals(this.currentArea);
            return rv;
        }

        return true;
    }

    /**
     * This method is called before the tooltip is hidden
     * 
     * @param event
     *            the event trying to hide the tooltip
     * @return <code>true</code> if the tooltip should be hidden
     */
    private boolean shouldHideToolTip(Event event) {
        if (event != null && event.type == SWT.MouseMove
                && (this.style & NO_RECREATE) != 0) {
            Object tmp = getToolTipArea(event);

            // No new area close the current tooltip
            if (tmp == null) {
                hide();
                return false;
            }

            boolean rv = !tmp.equals(this.currentArea);
            return rv;
        }

        return true;
    }

    /**
     * This method is called to check for which area the tooltip is
     * created/hidden for. In case of {@link #NO_RECREATE} this is used to
     * decide if the tooltip is hidden recreated.
     * 
     * <code>By the default it is the widget the tooltip is created for but could be any object. To decide if
     * the area changed the {@link Object#equals(Object)} method is used.</code>
     * 
     * @param event
     *            the event
     * @return the area responsible for the tooltip creation or
     *         <code>null</code> this could be any object describing the area
     *         (e.g. the {@link Control} onto which the tooltip is bound to, a
     *         part of this area e.g. for {@link ColumnViewer} this could be a
     *         {@link ViewerCell})
     */
    protected Object getToolTipArea(Event event) {
        return this.control;
    }

    /**
     * Start up the tooltip programmatically
     * 
     * @param location
     *            the location relative to the control the tooltip is shown
     */
    public void show(Point location) {
        Event event = new Event();
        event.x = location.x;
        event.y = location.y;
        event.widget = this.control;
        toolTipCreate(event);
    }

    private Popup toolTipCreate(final Event event) {
        if (shouldCreateToolTip(event)) {
            final Popup popup = new Popup(this.control.getShell(), null)
            {
                @Override
                protected Control createContentArea( final Composite parent )
                {
                    getShell().setData( DATA_SAPPHIRE_TOOLTIP, this );

                    final Composite composite = (Composite) super.createContentArea( parent );
                    SapphireToolTip.this.createContent( event, composite );
                    
                    parent.pack();
                    
                    return composite;
                }

                @Override
                protected Control getFocusControl()
                {
                    return null;
                }
            };
            
            toolTipOpen(popup, event);

            return popup;
        }

        return null;
    }
    
    protected abstract void createContent( Event event, Composite parent );

    private void toolTipShow(Popup tip, Event event) {
        if (tip.getShell() == null || !tip.getShell().isDisposed()) {
            this.currentArea = getToolTipArea(event);
            tip.create();
            final Shell shell = tip.getShell();
            if (isHideOnMouseDown()) {
                toolTipHookBothRecursively(shell);
            } else {
                toolTipHookByTypeRecursively(shell, true, SWT.MouseExit);
            }

            shell.pack();
            Point size = shell.getSize();
            Point location = fixupDisplayBounds(size, getLocation(size, event));

            // Need to adjust a bit more if the mouse cursor.y == tip.y and
            // the cursor.x is inside the tip
            Point cursorLocation = shell.getDisplay().getCursorLocation();

            if (cursorLocation.y == location.y && location.x < cursorLocation.x
                    && location.x + size.x > cursorLocation.x) {
                location.y -= 2;
            }

            shell.setLocation(location);
            tip.open();
        }
    }

    private Point fixupDisplayBounds(Point tipSize, Point location) {
        if (this.respectDisplayBounds || this.respectMonitorBounds) {
            Rectangle bounds;
            Point rightBounds = new Point(tipSize.x + location.x, tipSize.y
                    + location.y);

            Monitor[] ms = this.control.getDisplay().getMonitors();

            if (this.respectMonitorBounds && ms.length > 1) {
                // By default present in the monitor of the control
                bounds = this.control.getMonitor().getBounds();
                Point p = new Point(location.x, location.y);

                // Search on which monitor the event occurred
                Rectangle tmp;
                for (int i = 0; i < ms.length; i++) {
                    tmp = ms[i].getBounds();
                    if (tmp.contains(p)) {
                        bounds = tmp;
                        break;
                    }
                }

            } else {
                bounds = this.control.getDisplay().getBounds();
            }

            if (!(bounds.contains(location) && bounds.contains(rightBounds))) {
                if (rightBounds.x > bounds.x + bounds.width) {
                    location.x -= rightBounds.x - (bounds.x + bounds.width);
                }

                if (rightBounds.y > bounds.y + bounds.height) {
                    location.y -= rightBounds.y - (bounds.y + bounds.height);
                }

                if (location.x < bounds.x) {
                    location.x = bounds.x;
                }

                if (location.y < bounds.y) {
                    location.y = bounds.y;
                }
            }
        }

        return location;
    }

    /**
     * Get the display relative location where the tooltip is displayed.
     * Subclasses may overwrite to implement custom positioning.
     * 
     * @param tipSize
     *            the size of the tooltip to be shown
     * @param event
     *            the event triggered showing the tooltip
     * @return the absolute position on the display
     */
    public Point getLocation(Point tipSize, Event event) {
        return this.control.toDisplay(event.x + this.xShift, event.y + this.yShift);
    }

    private void toolTipHide(Popup tip, Event event) {
        if (tip != null && tip.getShell() != null && !tip.getShell().isDisposed() && shouldHideToolTip(event)) {
            this.control.getShell().removeListener(SWT.Deactivate, this.shellListener);
            this.currentArea = null;
            passOnEvent(tip, event);
            tip.close();
            CURRENT_TOOLTIP = null;
            afterHideToolTip(event);
        }
    }

    private void passOnEvent(Popup tip, Event event) {
        if (this.control != null && !this.control.isDisposed() && event != null
                && event.widget != this.control && event.type == SWT.MouseDown) {
            // the following was left in order to fix bug 298770 with minimal change. In 3.7, the complete method should be removed.
            tip.close();
        }
    }

    private void toolTipOpen(final Popup shell, final Event event) {
        // Ensure that only one Tooltip is shown in time
        if (CURRENT_TOOLTIP != null) {
            toolTipHide(CURRENT_TOOLTIP, null);
        }

        CURRENT_TOOLTIP = shell;

        this.control.getShell().addListener(SWT.Deactivate, this.shellListener);

        if (this.popupDelay > 0) {
            this.control.getDisplay().timerExec(this.popupDelay, new Runnable() {
                public void run() {
                    toolTipShow(shell, event);
                }
            });
        } else {
            toolTipShow(CURRENT_TOOLTIP, event);
        }

        if (this.hideDelay > 0) {
            this.control.getDisplay().timerExec(this.popupDelay + this.hideDelay,
                    new Runnable() {

                        public void run() {
                            toolTipHide(shell, null);
                        }
                    });
        }
    }

    private void toolTipHookByTypeRecursively(Control c, boolean add, int type) {
        if (add) {
            c.addListener(type, this.hideListener);
        } else {
            c.removeListener(type, this.hideListener);
        }

        if (c instanceof Composite) {
            Control[] children = ((Composite) c).getChildren();
            for (int i = 0; i < children.length; i++) {
                toolTipHookByTypeRecursively(children[i], add, type);
            }
        }
    }

    private void toolTipHookBothRecursively(Control c) {
        c.addListener(SWT.MouseDown, this.hideListener);
        c.addListener(SWT.MouseExit, this.hideListener);

        if (c instanceof Composite) {
            Control[] children = ((Composite) c).getChildren();
            for (int i = 0; i < children.length; i++) {
                toolTipHookBothRecursively(children[i]);
            }
        }
    }

    /**
     * This method is called after a tooltip is hidden.
     * <p>
     * <b>Subclasses may override to clean up requested system resources</b>
     * </p>
     * 
     * @param event
     *            event triggered the hiding action (may be <code>null</code>
     *            if event wasn't triggered by user actions directly)
     */
    protected void afterHideToolTip(Event event) {

    }

    /**
     * Set the hide delay.
     * 
     * @param hideDelay
     *            the delay before the tooltip is hidden. If <code>0</code>
     *            the tooltip is shown until user moves to other item
     */
    public void setHideDelay(int hideDelay) {
        this.hideDelay = hideDelay;
    }

    /**
     * Set the popup delay.
     * 
     * @param popupDelay
     *            the delay before the tooltip is shown to the user. If
     *            <code>0</code> the tooltip is shown immediately
     */
    public void setPopupDelay(int popupDelay) {
        this.popupDelay = popupDelay;
    }

    /**
     * Return if hiding on mouse down is set.
     * 
     * @return <code>true</code> if hiding on mouse down in the tool tip is on
     */
    public boolean isHideOnMouseDown() {
        return this.hideOnMouseDown;
    }

    /**
     * If you don't want the tool tip to be hidden when the user clicks inside
     * the tool tip set this to <code>false</code>. You maybe also need to
     * hide the tool tip yourself depending on what you do after clicking in the
     * tooltip (e.g. if you open a new {@link Shell})
     * 
     * @param hideOnMouseDown
     *            flag to indicate of tooltip is hidden automatically on mouse
     *            down inside the tool tip
     */
    public void setHideOnMouseDown(final boolean hideOnMouseDown) {
        // Only needed if there's currently a tooltip active
        if (CURRENT_TOOLTIP != null && CURRENT_TOOLTIP.getShell() != null && !CURRENT_TOOLTIP.getShell().isDisposed()) {
            // Only change if value really changed
            if (hideOnMouseDown != this.hideOnMouseDown) {
                this.control.getDisplay().syncExec(new Runnable() {

                    public void run() {
                        if (CURRENT_TOOLTIP != null
                                && CURRENT_TOOLTIP.getShell() != null && CURRENT_TOOLTIP.getShell().isDisposed()) {
                            toolTipHookByTypeRecursively(CURRENT_TOOLTIP.getShell(),
                                    hideOnMouseDown, SWT.MouseDown);
                        }
                    }

                });
            }
        }

        this.hideOnMouseDown = hideOnMouseDown;
    }

    /**
     * Hide the currently active tool tip
     */
    public void hide() {
        toolTipHide(CURRENT_TOOLTIP, null);
    }

    private class ToolTipOwnerControlListener implements Listener {
        public void handleEvent(Event event) {
            switch (event.type) {
            case SWT.Dispose:
            case SWT.KeyDown:
            case SWT.MouseDown:
            case SWT.MouseMove:
            case SWT.MouseWheel:
                toolTipHide(CURRENT_TOOLTIP, event);
                break;
            case SWT.MouseHover:
                toolTipCreate(event);
                break;
            case SWT.MouseExit:
                /*
                 * Check if the mouse exit happened because we move over the
                 * tooltip
                 */
                if (CURRENT_TOOLTIP != null && CURRENT_TOOLTIP.getShell() != null && !CURRENT_TOOLTIP.getShell().isDisposed()) {
                    if (CURRENT_TOOLTIP.getShell().getBounds().contains(
                            SapphireToolTip.this.control.toDisplay(event.x, event.y))) {
                        break;
                    }
                }

                toolTipHide(CURRENT_TOOLTIP, event);
                break;
            default:
                // Ignore all other events.
            }
        }
    }

    private class TooltipHideListener implements Listener {
        public void handleEvent(Event event) {
            if (event.widget instanceof Control) {

                Control c = (Control) event.widget;
                Shell shell = c.getShell();

                switch (event.type) {
                case SWT.MouseDown:
                    if (isHideOnMouseDown()) {
                        toolTipHide((Popup)shell.getData( DATA_SAPPHIRE_TOOLTIP ), event);
                    }
                    break;
                case SWT.MouseExit:
                    /*
                     * Give some insets to ensure we get exit informations from
                     * a wider area ;-)
                     */
                    Rectangle rect = shell.getBounds();
                    rect.x += 5;
                    rect.y += 5;
                    rect.width -= 10;
                    rect.height -= 10;

                    if (!rect.contains(c.getDisplay().getCursorLocation())) {
                        toolTipHide((Popup)shell.getData( DATA_SAPPHIRE_TOOLTIP ), event);
                    }

                    break;
                default:
                    // Ignore all other events.
                }
            }
        }
    }
}
