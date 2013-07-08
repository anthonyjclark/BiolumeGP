/**
 * ColorPicker.java
 * @author: http://www.julapy.com/processing/ColorPicker.pde
 * @adaptors: Tony J. Clark, Adam Ford
 * @date: August 3, 2011
 * @description: Color Picker class for Processing.  
 */

import java.awt.Color;

import processing.core.*;

public class ColorPicker 
{
  int x, y, w, h, c;
  PImage cpImage;
  Simulator sim;
	
  public ColorPicker ( int x, int y, int w, int h, int c, Simulator sim )
  {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.c = c;
    this.sim = sim;
		
    cpImage = new PImage( w, h );
		
    init();
  }
	
  private void init ()
  {
    // draw color.
    sim.colorMode(PConstants.RGB, 255);
      int cw = w - 60;
    for( int i=0; i<cw; i++ ) 
    {
      float nColorPercent = i / (float)cw;
      float rad = (-360 * nColorPercent) * (PConstants.PI / 180);
      int nR = (int)(PApplet.cos(rad) * 127 + 128) << 16;
      int nG = (int)(PApplet.cos(rad + 2 * PConstants.PI / 3) * 127 + 128) << 8;
      int nB = (int)(Math.cos(rad + 4 * PConstants.PI / 3) * 127 + 128);
      int nColor = nR | nG | nB;
			
      setGradient( i, 0, 1, h/2, 0xFFFFFF, nColor );
      setGradient( i, (h/2), 1, h/2, nColor, 0x000000 );
    }
		
    // draw black/white.
    drawRect( cw, 0,   30, h/2, 0xFFFFFF );
    drawRect( cw, h/2, 30, h/2, 0 );
		
    // draw grey scale.
    for( int j=0; j<h; j++ )
    {
      int g = 255 - (int)(j/(float)(h-1) * 255 );
      drawRect( w-30, j, 30, 1, sim.color( g, g, g ) );
    }
  }

  private void setGradient(int x, int y, float w, float h, int c1, int c2 )
  {
    float deltaR = sim.red(c2) - sim.red(c1);
    float deltaG = sim.green(c2) - sim.green(c1);
    float deltaB = sim.blue(c2) - sim.blue(c1);

    for (int j = y; j<(y+h); j++)
    {
      int c = sim.color( sim.red(c1)+(j-y)*(deltaR/h), sim.green(c1)+(j-y)*(deltaG/h), sim.blue(c1)+(j-y)*(deltaB/h) );
      cpImage.set( x, j, c );
    }
  }
	
  private void drawRect( int rx, int ry, int rw, int rh, int rc )
  {
    for(int i=rx; i<rx+rw; i++) 
    {
      for(int j=ry; j<ry+rh; j++) 
      {
        cpImage.set( i, j, rc );
      }
    }
  }
	
  public void render ()
  {
      sim.image( cpImage, x, y );
    if( sim.mousePressed &&
	    sim.mouseX >= x && 
		    sim.mouseX < x + w &&
		    sim.mouseY >= y &&
			    sim.mouseY < y + h )
    {
      c = sim.get( sim.mouseX, sim.mouseY );
    }
    
  }

  public Color getColor() { 
    return new Color(c);
  }
}

