package be.wide.test;

import processing.core.*;

public class Particle 
{
	  private PVector pos;
	  private PVector v;
	  public int ttl;
	  public int maxttl = 256;
	  
	  public Particle( PVector pos ) {
	    this.pos = pos;
	    this.ttl = maxttl;
	    this.v = new PVector( 0,0,0 );
	  }
	  
	  public Particle( PVector pos, PVector v ) {
	    this.pos = pos;
	    this.ttl = maxttl;
	    this.v = v;
	  }
	 
	  public void update() {
	    //if ( pos.y < 58 ) {
	      if (ttl > 0) {
	      pos.add(v);
	      ttl -= 1;
	      }
	   //   v.y += 1;
	   // } else {
	   //   v.mult( 0.3 );
	    //  v.y = -v.y;
	    //  pos.add(v);
	   // }
	  }
	  
	  public void draw(PApplet parent) {
	    if (ttl > 0) {
	      parent.fill((int) (PApplet.map( ttl, 0, maxttl, 0, 255)*Math.random()));
	      parent.noStroke();
	      parent.pushMatrix();
	      parent.translate( pos.x, pos.y, pos.z );
	      parent.box(2);
	      parent.popMatrix();
	    }
	  }
	}
