/*
 * ------------------------------------------------------------------------
 *
 * Copyright (C) 2014 Eli Lilly and Company Limited
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * ------------------------------------------------------------------------
*/
package org.erlwood.knime.utils.gui.togglebutton;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.ToolTipManager;

import org.erlwood.knime.utils.gui.togglepanel.TogglePanel.InputType;
import org.knime.core.node.NodeLogger;

/**
 * Component designed to display an iOS type toggle slider, with a small amount of
 * text on both the left and right side.
 * Derived from https://code.google.com/p/petersoft-java-style-2/wiki/OnOffButton
 * @author Luke Bullard
 *
 */
public class ToggleButton extends JToggleButton implements ActionListener, MouseMotionListener, MouseListener {
	
	/**
	 * Serial Id.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Logger.
	 */
	private static final NodeLogger LOG = NodeLogger.getLogger(ToggleButton.class);
	
	/**
	 * The slider to use.
	 */
	private static final Image SLIDER = new ImageIcon(ToggleButton.class.getResource("slider.png")).getImage();
	
	/**
	 * Executor to run the slider animation in.
	 */
	private final Executor executor = Executors.newSingleThreadExecutor();
	
	/**
	 * XPosition of the slider during drag & animation.
	 */
	private int buttonX;
	
	/**
	 * Delta value during dragging.
	 */
	private int deltaX = -1;

	/**
	 * Are we selected ?
	 */
	private boolean selected;
	
	/**
	 * Are we dragging ?
	 */
	private boolean drag;
	
	/**
	 * Are we animating ?
	 */
	private boolean animating;

	/**
	 * Text for the left hand side of the toggle
	 */	
	private final InputType leftType;
	
	/**
	 * Text for the right hand side of the toggle
	 */
	private final InputType rightType;
	
	/**
	 * Constructor.
	 * @param leftType The type for the left hand side of the toggle
	 * @param rightType The type for the left hand side of the toggle
	 */
	public ToggleButton(InputType leftType, InputType rightType) {
		this.leftType = leftType;
		this.rightType = rightType;
		this.addActionListener(this);
		this.addMouseMotionListener(this);
		this.addMouseListener(this);	
		ToolTipManager.sharedInstance( ).registerComponent(this);
		setToolTipText("P");
		setPreferredSize(new Dimension(52, 20));
	}
	
	/**
	 * @return Whether we are selected or not.
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Sets the selection and kicks off the animation.
	 * @param selected Selection state
	 */
	public void setSelected(boolean selected) {
		super.setSelected(selected);
		this.selected = selected;
		
		//	If we are visible then execute the animation
		if (isVisible()) {
			executor.execute(new SliderAnimation());
		}		
	}
	
	@Override
	public String getToolTipText(final MouseEvent event) {
		// return the correct tool tip text based on the cursor position
		if(event.getX( ) < this.getWidth( ) / 2) {
			return leftType.getToolTipText( );
		} else {
			return rightType.getToolTipText( );
		}
	}	
	
	/**
	 * {@inheritDoc}
	 */
	public void paint(Graphics g) {
		
		paintBackground(g);
		
		//	If we are either animating or dragging then use the buttonX value to
		//	determine slider x pos.
		int sliderXPos = 0;
		if (drag || animating) {
			sliderXPos = buttonX; 
		} else {			
			if (isSelected()) {
				sliderXPos = (getWidth() / 2);
			} 
		}
		
		g.drawImage(SLIDER, sliderXPos, 0, getWidth() / 2, this.getHeight(),	null, null);
	}

	/**
	 * Draws the background image on the Graphics context.
	 * @param g The Graphics context.
	 */
	private void paintBackground(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		RenderingHints rh = new RenderingHints(
	             RenderingHints.KEY_TEXT_ANTIALIASING,
	             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		g2d.setRenderingHints(rh);
	    
		GradientPaint blueFill = new GradientPaint(0, 0, new Color(57, 124, 241), 0, getHeight() - 1, new Color(109, 166, 249));
		g2d.setPaint(blueFill);
		g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight(), 5, 5);
		
		
		g2d.setColor(new Color(57,99, 172));
		g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() -1, 5, 5);
		
		g2d.setColor(Color.white);		
		g2d.setFont(g2d.getFont().deriveFont(14f).deriveFont(Font.BOLD));
		FontMetrics fm = g2d.getFontMetrics();
		
		// draw the left icon/text
		Image icon = leftType.getIcon( );
		if(icon != null) {
			int x1 = (int)((getWidth( ) * .25) - (icon.getWidth(null) / 2) + 1);
			int y1 = (getHeight( ) - icon.getHeight(null)) / 2;			
			g2d.drawImage(icon, x1, y1, null);
		} else {
			String text = leftType.getText( );
			int x1 = (int)(((getWidth() * .25) - (fm.stringWidth(text) / 2)));
		    int y1 = (fm.getAscent() + (getHeight() - (fm.getAscent() + fm.getDescent())) / 2);
		    g2d.drawString(text,  x1, y1);
		}
		
		// draw the right icon/text
		icon = rightType.getIcon( );
		if(icon != null) {
			int x2 = (int)((getWidth( ) * .75) - (icon.getWidth(null) / 2));
			int y2 = (getHeight( ) - icon.getHeight(null)) / 2;
			g2d.drawImage(icon, x2, y2, null);
		} else {
			String text = rightType.getText( );
			int x2 = (int)(((getWidth() * .75) - (fm.stringWidth(text) / 2)));
			int y2 = (fm.getAscent() + (getHeight() - (fm.getAscent() + fm.getDescent())) / 2);
			g2d.drawString(text, x2, y2);
		}
	}
		
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (!drag) {
			selected = !selected;
			setSelected(selected);			
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseDragged(MouseEvent evt) {
		drag = true;
		
		if (deltaX == -1) {
			deltaX = evt.getX() - buttonX;
		}

		buttonX = evt.getX() - deltaX;

		if (buttonX < 0) {
			buttonX = 0;
		}
		if (buttonX > this.getWidth() / 2) {
			buttonX = this.getWidth() / 2;
		}

		repaint();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseMoved(MouseEvent arg0) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseClicked(MouseEvent arg0) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mousePressed(MouseEvent arg0) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseReleased(MouseEvent arg0) {
		deltaX = -1;
		if (drag) {
			if (buttonX < this.getWidth() / 4) {
				this.setSelected(false);
			} else {
				this.setSelected(true);
			}
		}
		drag = false;
	}

	/**
	 * 
	 * @author Luke Bullard
	 *
	 */
	private final class SliderAnimation implements Runnable {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
				
			animating = true;
			try {
				if (isSelected()) {
					for (; buttonX <= (getWidth() / 2) - 1; buttonX++) {
						repaint();
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							LOG.info(e);							
						}				
					}
					buttonX = (getWidth() / 2);
	
				} else {
					for (; buttonX > 0; buttonX--) {
						repaint();
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							LOG.info(e);
						}			
					}
					buttonX = 0;
				}
			} finally {
				animating = false;
			}
			repaint();		
		}
	}
}