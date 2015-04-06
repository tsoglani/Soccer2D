package com.epp.soccer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.widget.Button;
import android.widget.LinearLayout;

public class Btn extends Button{
	
private Point pressPoint= new Point();
private Paint paint;
private String backGroundColor;
private Point touchLocation;
private final int radiusJoystic=20;
	public Btn(Context context,String backGroundColor) {
		super(context);
		touchLocation= new Point(getWidth()/2,getHeight()/2);
		this.backGroundColor= backGroundColor;
	    setWillNotDraw(false);
	    paint= new Paint();
	    this.setBackgroundColor(Color.TRANSPARENT);
	     LinearLayout.LayoutParams param=new LinearLayout.LayoutParams(100,100);
	     setLayoutParams(param);
	    
	}

	@Override
    protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);
       
	     //canvas.drawPaint(paint);
		if( backGroundColor.equals("Blue")){
        paint.setColor(Color.MAGENTA);
        }else if( backGroundColor.equals("Yellow")){
            paint.setColor(Color.YELLOW);
        }
		canvas.drawCircle(getWidth()/2, getHeight()/2, getWidth()/2, paint);
		
	   
		if(getText().toString().equalsIgnoreCase("pass")){
			paint.setColor(Color.CYAN);
			canvas.drawCircle(touchLocation.x, touchLocation.y, radiusJoystic, paint)	;
		}
		paint.setColor(Color.BLACK);
		paint.setTextSize(20);canvas.rotate(90, getWidth()/2, getHeight()/2);
		 canvas.drawText(getText().toString(), 20, getHeight()/2, paint);
	}
	
	public void defaultJoysticLocation(){
		touchLocation.x=getWidth()/2;
		touchLocation.y=getHeight()/2;
	}
	
	public void setJoysticLocation(float posX,float posY){
		
		touchLocation.x= (int)posX;
		touchLocation.y=(int)posY;
	}
public void setJoysticLocation(double posX,double posY){
		
	touchLocation.x= (int)posX;
	touchLocation.y= (int)posY;
	}


public Point getJoysticLocation(){
	return touchLocation;
}
	
	public void setPressPoint(Point point){
		pressPoint=point;
	}
	public Point getPressPoint(){
		return pressPoint;
	}
}
