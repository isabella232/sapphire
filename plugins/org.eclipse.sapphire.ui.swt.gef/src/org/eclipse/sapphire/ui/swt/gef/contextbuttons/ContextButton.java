/******************************************************************************
 * Copyright (c) 2015 SAP and Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP - initial implementation
 *    Shenxue Zhou - adaptation for Sapphire and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.contextbuttons;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.localization.LabelTransformer;
import org.eclipse.sapphire.ui.DefaultActionImage;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.forms.swt.SwtUtil;
import org.eclipse.sapphire.ui.swt.gef.SapphireDiagramEditor;
import org.eclipse.sapphire.ui.swt.gef.figures.FigureUtil;
import org.eclipse.sapphire.ui.swt.gef.parts.DiagramConnectionEditPart;
import org.eclipse.sapphire.ui.swt.gef.parts.DiagramNodeEditPart;
import org.eclipse.sapphire.ui.swt.gef.parts.SapphireDiagramEditorPageEditPart;
import org.eclipse.sapphire.ui.swt.gef.parts.ShapeEditPart;
import org.eclipse.sapphire.ui.swt.gef.presentation.DiagramPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.widgets.Display;

/**
 * A context button, which is used for example in the context button pad. It
 * does not extend button, but is an implementation from scratch, because it has
 * a quite specific look and behavior.
 * 
 * @author SAP
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class ContextButton extends Clickable implements MouseMotionListener, ActionListener {
	private double getCurrentTransparency = 0;

	/**
	 * The {@link PositionedContextButton} contains the visual information for
	 * the context button (colors, position, size, opacity, ...). It is set in
	 * the constructor.
	 */
	private PositionedContextButton positionedContextButton;

	private SapphireAction sapphireAction;

	/**
	 * The {@link ContextButtonPad} is used to access the environment (editor,
	 * ...). It is set in the constructor.
	 */
	private ContextButtonPad contextButtonPad;

	/**
	 * The current opacity of the context button. It is changed for example on
	 * mouse-events.
	 */
	private double opacity;

	/**
	 * The tooltip of the context button. It never changes, but it is sometimes
	 * shortly disabled (e.g. on button-pressed).
	 */
	private Tooltip tooltip;
	
	// ============================ inner classes =============================

	/**
	 * The label provider for the popup-menu, which appears when the
	 * context-button is clicked.
	 */
	private class PopupMenuLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			SapphireActionHandler handler  = (SapphireActionHandler) element;
			return getLabel(handler);
		}

		@Override
		public Image getImage(Object element) {
			SapphireActionHandler handler  = (SapphireActionHandler) element;
			return getActionHandlerImage(handler);
		}
	};

	// ============================ constructors ==============================

	/**
	 * Creates a new ContextButton.
	 * 
	 * @param positionedContextButton
	 *            The {@link PositionedContextButton} contains the visual
	 *            information for the context button (colors, position, size,
	 *            opacity, ...).
	 * @param contextButtonPad
	 *            The {@link ContextButtonPad} is used to access the environment
	 *            (editor, ...).
	 */
	public ContextButton(PositionedContextButton positionedContextButton, ContextButtonPad contextButtonPad) {
		this.positionedContextButton = positionedContextButton;
		this.contextButtonPad = contextButtonPad;
		this.sapphireAction = positionedContextButton.getSapphireAction();

		setBorder(null); // get rid of default border
		setCurrentTransparency(contextButtonPad.getCurrentTransparency());
		setOpacity(getPositionedContextButton().getDefaultOpacity());

		String label = getLabel(getSapphireAction());
		if (label != null && label.length() > 0) {
			if (tooltip == null) {
				tooltip = new Tooltip();
			}			
			tooltip.setHeader(label);
		}
		if (getSapphireAction().getToolTip() != null && getSapphireAction().getToolTip().length() > 0) {
			if (tooltip == null) {
				tooltip = new Tooltip();
			}
			tooltip.setDescription(getSapphireAction().getToolTip());
		}
		setToolTip(tooltip);

		addMouseMotionListener(this);
		addActionListener(this);

		// disable the context button, if it is not executable.
		// Note, that this has to be done dependent on the context button functionality
		// (drag&drop, click with popup, single click)
		if (getSapphireAction().getActiveHandlers().size() > 1) {
			setEnabled(getExecutableMenuEntries().size() > 0);
		} else {
			setEnabled(getSapphireAction().isEnabled());
		}
	}

	// ========================= getter and setter ============================

	/**
	 * Returns the {@link PositionedContextButton} which contains the visual
	 * information for the context button (colors, position, size, opacity, ...)
	 * 
	 * @return The {@link PositionedContextButton} which contains the visual
	 *         information for the context button (colors, position, size,
	 *         opacity, ...)
	 */
	public final PositionedContextButton getPositionedContextButton() {
		return positionedContextButton;
	}

	public final SapphireAction getSapphireAction() {
		return sapphireAction;
	}

	/**
	 * Returns the {@link ContextButtonPad} which is used to access the
	 * environment (editor, ...).
	 * 
	 * @return The {@link ContextButtonPad} which is used to access the
	 *         environment (editor, ...).
	 */
	public final ContextButtonPad getContextButtonPad() {
		return contextButtonPad;
	}

	/**
	 * Returns the zoom-level for which the context-button shall be painted.
	 * 
	 * @return The zoom-level for which the context-button shall be painted.
	 */
	public final double getZoomLevel() {
		return getContextButtonPad().getZoomLevel();
	}

	/**
	 * Returns the {@link DiagramEditorInternal} for which the context button is
	 * displayed.
	 * 
	 * @return The {@link DiagramEditorInternal} for which the context button is
	 *         displayed.
	 */
	public final SapphireDiagramEditor getEditor() {
		return getContextButtonPad().getEditor();
	}

	/**
	 * Sets the opacity of this figure.
	 * 
	 * @param opacity
	 *            The opacity to set.
	 */
	private void setOpacity(double opacity) {
		this.opacity = opacity;
	}

	/**
	 * Returns the opacity adjusted by the current transparency. Concretely this
	 * means "opacity * transparency".
	 * 
	 * @return The opacity adjusted by the current transparency.
	 */
	private double getAdjustedOpacity() {
		if (getCurrentTransparency() != 0) {
			return opacity * getCurrentTransparency();
		}

		return opacity;
	}

	// ============================== painting ================================

	/**
	 * Paints the context button (lines, filling, image, ...).
	 */
	@Override
	protected void paintFigure(Graphics graphics) {
		int lw = ((int) (getPositionedContextButton().getLineWidth() * getZoomLevel()));
		graphics.setLineWidth(lw);
		graphics.setAntialias(SWT.ON);
		graphics.setAlpha((int) (getAdjustedOpacity() * 255));

		Path pathOuterLine = createPath(1);
		Path pathMiddleLine = createPath(2);
		Path pathFill = createPath(3);

		graphics.setBackgroundColor(getAdjustedColor(getPositionedContextButton().getFillColor()));
		graphics.fillPath(pathFill);
		graphics.setForegroundColor(getAdjustedColor(getPositionedContextButton().getMiddleLineColor()));
		graphics.drawPath(pathMiddleLine);
		graphics.setForegroundColor(getAdjustedColor(getPositionedContextButton().getOuterLineColor()));
		graphics.drawPath(pathOuterLine);

		pathOuterLine.dispose();
		pathMiddleLine.dispose();
		pathFill.dispose();
		pathOuterLine = null;
		pathMiddleLine = null;
		pathFill = null;

		// change opacity for image (never transparent)
		double imageOpacity = 1;
		graphics.setAlpha((int) (imageOpacity * 255));

		// create image
		Image originalImage = getActionImage(getSapphireAction());
		if (originalImage == null) {
			return;
		}
		
		Image image;
		if (!isEnabled()) {
			image = new Image(originalImage.getDevice(), originalImage, SWT.IMAGE_DISABLE);
		} else {
			image = new Image(originalImage.getDevice(), originalImage, SWT.IMAGE_COPY);
		}

		// draw image
		org.eclipse.swt.graphics.Rectangle rect = image.getBounds();
		Rectangle newRect = new Rectangle(0, 0, rect.width, rect.height);
		newRect.scale(getZoomLevel());
		newRect.x = getBounds().x + ((getBounds().width - newRect.width) / 2);
		newRect.y = getBounds().y + ((getBounds().height - newRect.height) / 2);
		graphics.drawImage(image, rect.x, rect.y, rect.width, rect.height, newRect.x, newRect.y, newRect.width, newRect.height);
		image.dispose();

		// paint indicators
		List<SapphireActionHandler> menuEntries = getSapphireAction().getActiveHandlers();
		boolean isSubmenuButton = menuEntries != null && menuEntries.size() > 1;
		if (isSubmenuButton) {
			paintSubmenuIndicator(graphics, newRect);
		}

	}

	/**
	 * Paints a sub-menu indicator on the context button (opens sub-menu on
	 * click).
	 */
	private void paintSubmenuIndicator(Graphics graphics, Rectangle newRect) {
		int x = newRect.x;
		int y = newRect.y;
		int w = newRect.width;
		int h = newRect.height;
		PointList pl = new PointList();
		pl.addPoint(x + w, y + h * 3 / 4);
		pl.addPoint(x + w, y + h);
		pl.addPoint(x + w * 3 / 4, y + h);

		preparePaintIndicator(graphics);
		graphics.drawPolygon(pl);
		graphics.fillPolygon(pl);
	}


	/**
	 * Prepares the given graphics for painting an indicator (sets colors,
	 * line-width, ...).
	 */
	private void preparePaintIndicator(Graphics graphics) {
		graphics.setLineWidth(1);
		org.eclipse.sapphire.Color outerLineColor = getPositionedContextButton().getOuterLineColor();
		Color adjustedColor = getAdjustedColor(outerLineColor);
		graphics.setForegroundColor(adjustedColor);
		graphics.setBackgroundColor(adjustedColor);
	}

	/**
	 * Returns the adjusted SWT color for the given IColorConstant. Note, that
	 * this method also adjusts the color, in case that this figure is disabled.
	 * 
	 * @param color
	 *            The IColorConstant for which to return the SWT color.
	 * @return The adjusted SWT color for the given IColorConstant.
	 */
	private Color getAdjustedColor(org.eclipse.sapphire.Color color) {
		SapphireDiagramEditor editor = getContextButtonPad().getEditor();
		if (!isEnabled()) {
			int disabledAdjustment = 80;
			int r = Math.min(255, color.red() + disabledAdjustment);
			int g = Math.min(255, color.green() + disabledAdjustment);
			int b = Math.min(255, color.blue() + disabledAdjustment);
			color = new org.eclipse.sapphire.Color(r, g, b);
		}
		Color swtColor = editor.getResourceCache().getColor(color);
		return swtColor;
	}

	/**
	 * Creates and returns the path, which defines the outer lines and filling
	 * area of the context button.
	 * 
	 * @param shrinkLines
	 *            The number of lines, by which the path shall be shrinked. This
	 *            allows to use this method to create the outer line, middle
	 *            line and inner line of the context button.
	 * @return The path, which defines the outer lines and filling area of the
	 *         context button.
	 */
	protected Path createPath(int shrinkLines) {
		double zoom = getZoomLevel();
		int lw = (int) (getPositionedContextButton().getLineWidth() * zoom);
		Rectangle r = FigureUtil.getAdjustedRectangle(getBounds(), 1.0, shrinkLines * lw);

		// adjust corner for the inner path (formula found by experimenting)
		double zoomedCorner = (getPositionedContextButton().getCornerRadius() * zoom);
		int corner = (int) Math.max(1, zoomedCorner - (((shrinkLines - 1) * lw) + zoomedCorner / 64));

		Path path = new Path(null);
		path.moveTo(r.x, r.y);
		path.addArc(r.x, r.y, corner, corner, 90, 90);
		path.addArc(r.x, r.y + r.height - corner, corner, corner, 180, 90);
		path.addArc(r.x + r.width - corner, r.y + r.height - corner, corner, corner, 270, 90);
		path.addArc(r.x + r.width - corner, r.y, corner, corner, 0, 90);
		path.close();

		return path;
	}

	// ============================== eventing ================================

	/**
	 * Changes the opacity of the context button.
	 */
	@Override
	public void handleMouseEntered(MouseEvent event) {
		setOpacity(getPositionedContextButton().getMouseOverOpacity());
		repaint();
		super.handleMouseEntered(event);

	}

	/**
	 * Changes the opacity of the context button.
	 */
	@Override
	public void handleMouseExited(MouseEvent event) {
		setOpacity(getPositionedContextButton().getDefaultOpacity());
		repaint();
		super.handleMouseExited(event);
	}

	/**
	 * Changes the opacity of the context button.
	 * <p>
	 * Additionally it disables the tooltip. Especially in the middle of the
	 * drag & drop procedure no tooltip shall appear.
	 */
	@Override
	public void handleMousePressed(MouseEvent event) {
		// disable tooltip
		// This does not hide an already showing tooltip (which would be
		// preferred), but at least it prevents showing a tooltip after
		// mouse-pressed. This is especially important when dragging starts
		// after mouse-pressed.
		setToolTip(null);

		setOpacity(getPositionedContextButton().getMouseDownOpacity());
		repaint();
		super.handleMousePressed(event);
	}

	/**
	 * Changes the opacity of the context button.
	 * <p>
	 * Additionally it enables the tooltip again.
	 */
	@Override
	public void handleMouseReleased(MouseEvent event) {
		// enable tooltip, which was disabled on mouse-pressed
		setToolTip(tooltip);

		// still entered the button, so go back to mouse-over
		setOpacity(getPositionedContextButton().getMouseOverOpacity());
		repaint();
		super.handleMouseReleased(event);
	}

	/**
	 * Creates a connection with the connection-tool when dragging the context
	 * button.
	 */
	public void mouseDragged(MouseEvent me) {
	}

	public void mouseEntered(MouseEvent me) {
	}

	public void mouseExited(MouseEvent me) {
	}

	public void mouseHover(MouseEvent me) {
	}

	public void mouseMoved(MouseEvent me) {
	}

	/**
	 * Performs the command of the context button or opens a context-menu with a
	 * selection of multiple commands to perform.
	 */
	public void actionPerformed(ActionEvent event) {
		if (getSapphireAction().getActiveHandlers().size() > 1) {
			List<SapphireActionHandler> menuEntries = getExecutableMenuEntries();
			if (menuEntries.size() == 0) {
				return;
			}

			ILabelProvider labelProvider = new PopupMenuLabelProvider();
			PopupMenu popupMenu = new PopupMenu(menuEntries, labelProvider);

			boolean b = popupMenu.show(Display.getCurrent().getActiveShell());
			if (b) {
				SapphireActionHandler handler = (SapphireActionHandler)popupMenu.getResult();
				executeActionHandler(handler);
			}
		} else if (getSapphireAction().isEnabled()) {
			// has no ContextButtonMenuEntries -> execute ContextButton
			executeActionHandler(getSapphireAction().getFirstActiveHandler());
			
		}

		getEditor().getContextButtonManager().refresh();		
	}

	/**
	 * Returns all action handlers, which are executable.
	 * 
	 * @return All action handlers, which are executable.
	 */
	private List<SapphireActionHandler> getExecutableMenuEntries() {
		// has ContextButtonMenuEntries -> create popup
		List<SapphireActionHandler> activeHandlers = getSapphireAction().getActiveHandlers();
		List<SapphireActionHandler> menuEntries = new ArrayList<SapphireActionHandler>();
		for (SapphireActionHandler handler : activeHandlers) {
			if (handler.isEnabled()) {
				menuEntries.add(handler);
			}
		}
		return menuEntries;
	}
	
	private Image getActionHandlerImage(SapphireActionHandler handler)
	{
	    ImageData imageData = handler.getImage(16);
	    
        ImageDescriptor imageDescriptor;
		if (imageData == null)
		{
			return DefaultActionImage.getDefaultActionImage();
		}
		else
		{
			imageDescriptor = SwtUtil.toImageDescriptor(imageData);
			return imageDescriptor.createImage();
		}
		
	}

	private Image getActionImage(SapphireAction action)
	{
	    ImageData imageData = action.getImage(16);
	    
        ImageDescriptor imageDescriptor;
		if (imageData == null)
		{
			return DefaultActionImage.getDefaultActionImage();
		}
		else
		{
			imageDescriptor = SwtUtil.toImageDescriptor(imageData);
			return imageDescriptor.createImage();
		}		
	}
	
	private String getLabel(SapphireAction action)
	{
		String label = action.getLabel();
	    label = LabelTransformer.transform( label, CapitalizationType.TITLE_STYLE, false );
	    return label;		
	}
	
	private String getLabel(SapphireActionHandler handler)
	{
		String label = handler.getLabel();
	    label = LabelTransformer.transform( label, CapitalizationType.TITLE_STYLE, false );
	    return label;		
	}
	
	private void executeActionHandler(SapphireActionHandler handler)
	{
		List<GraphicalEditPart> editParts = this.contextButtonPad.getEditParts();
		for (GraphicalEditPart editPart : editParts)
		{
			DiagramPresentation presentation;
			if( editPart instanceof SapphireDiagramEditorPageEditPart )
			{
				presentation = ((SapphireDiagramEditorPageEditPart)editPart).getPresentation();
			}
			else if( editPart instanceof DiagramNodeEditPart )
			{
				presentation = ((DiagramNodeEditPart)editPart).getPresentation();
			}
			else if (editPart instanceof ShapeEditPart)
			{
				presentation = ((ShapeEditPart)editPart).getShapePresentation();
			}
			else if( editPart instanceof DiagramConnectionEditPart )
			{
				presentation = ((DiagramConnectionEditPart)editPart).getPresentation();
			}
			else
			{
			    throw new IllegalStateException();
			}
			
	        handler.execute(presentation);
		}
	}

	public double getCurrentTransparency() {
		return getCurrentTransparency;
	}

	public void setCurrentTransparency(double getCurrentTransparency) {
		this.getCurrentTransparency = getCurrentTransparency;
	}
}
