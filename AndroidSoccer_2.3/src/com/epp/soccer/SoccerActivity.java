package com.epp.soccer;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class SoccerActivity extends Activity {

	private FrameLayout frameLayout;
	private SoccerField field;
	private SeekBar seekBar;
	/**
	 * Called when the activity is first created.
	 */
	private Btn shootButton;
	private Btn passButton;
    //private TextView welcomeTxt;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
     getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		try {
			setFrameLayout(new FrameLayout(this));
			field = new SoccerField(this);
			passButton = new Btn(this, "Yellow"){
				@Override
				  protected void onDraw(Canvas canvas) {
				//    canvas.rotate(90, canvas.getClipBounds().right/2, canvas.getClipBounds().bottom/2);
				    super.onDraw(canvas);
				  }
				
			};
			shootButton = new Btn(this, "Blue"){
				@Override
				  protected void onDraw(Canvas canvas) {
				//    canvas.rotate(90, canvas.getClipBounds().right/2, canvas.getClipBounds().bottom/2);
				    super.onDraw(canvas);
				  }
				
			};
		//	welcomeTxt= new TextView(this);
			seekBar = new SeekBar(this);
		//	welcomeTxt.setText("Welcome to Soccer Game ");
			seekBar.setMax(20);
			ShapeDrawable thumb = new ShapeDrawable(new OvalShape());
			thumb.setIntrinsicHeight(10);
			thumb.setIntrinsicWidth(5);
			seekBar.setThumb(thumb);
			seekBar.setBackgroundColor(Color.TRANSPARENT);
			seekBar.setProgress(Player.startingSpeed);
			
			
			
			shootButton.setText("shoot");
			passButton.setText("pass");
			
			field.generateTeams();
			field.addplayersAndBallIntoField();
			getFrameLayout().addView(field); // i defteri parametros me LayoutParams mou dixnei to megethos tou View			

			getFrameLayout().addView(shootButton);
			getFrameLayout().addView(passButton);
			getFrameLayout().addView(seekBar);
			//getFrameLayout().addView(welcomeTxt);
			//welcomeTxt.setBackgroundColor(Color.RED);
			//welcomeTxt.setGravity(Gravity.CENTER);
			seekBar.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					Team myTeam = null;
					if (field.getTeam1().isMyTeam()) {
						myTeam = field.getTeam1();
					} else {
						myTeam = field.getTeam2();
					}
					myTeam.getSelectedPlayer().setCurrentSpeed(seekBar.getProgress());
			Log.e("cur speed", Integer.toString(myTeam.getSelectedPlayer().getCurrentSpeed()));
					return false;
				}

			});
			
			setContentView(getFrameLayout());
			new BasicControlLocator().start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public SeekBar getSeekBar(){
		return seekBar;
	}
	class BasicControlLocator extends Thread {
		boolean wait = true;
		public void run() {
			while (wait) {
				try {
					sleep(2500);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if(locateComponents()){
								wait = false;
							}
						}
					});
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean locateComponents() {
		//getFrameLayout().removeView(welcomeTxt);
		shootButton.layout(00, 00, getWindow().getWindowManager().getDefaultDisplay().getWidth()/6, getWindow().getWindowManager().getDefaultDisplay().getWidth()/6);
		passButton.layout(0, getWindowManager().getDefaultDisplay().getHeight()-getWindowManager().getDefaultDisplay().getHeight()/30- getWindow().getWindowManager().getDefaultDisplay().getWidth()/6-getWindowManager().getDefaultDisplay().getHeight()/30, getWindow().getWindowManager().getDefaultDisplay().getWidth()/6+5,getWindowManager().getDefaultDisplay().getHeight()-getWindowManager().getDefaultDisplay().getHeight()/30-getWindowManager().getDefaultDisplay().getHeight()/30);
		seekBar.layout(0, getWindowManager().getDefaultDisplay().getHeight()-getWindowManager().getDefaultDisplay().getHeight()/30, getWindowManager().getDefaultDisplay().getWidth(), getWindowManager().getDefaultDisplay().getHeight());
		passButton.defaultJoysticLocation();
		if (passButton.getBottom() == getFrameLayout().getHeight() - 100) {
			return true;
		} else {
			return false;
		}
	}
	
	public FrameLayout getFrameLayout() {
		return frameLayout;
	}

	public void setFrameLayout(FrameLayout frameLayout2) {
		this.frameLayout = frameLayout2;
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	public Button getShootButton() {
		return shootButton;
	}

	public Btn getPassButton() {
		return passButton;
	}
}
