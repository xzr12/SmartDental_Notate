package com.edu.thss.smartdental;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.R.integer;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.InputFilter.LengthFilter;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;

//【画图】
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.View;

/**
 * 一张图片浏览页面，实现缩放、拖动、自动居中
 * */
public class OneImageActivity extends Activity implements OnTouchListener,OnClickListener{

	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();
	DisplayMetrics dm;
	//ImageView imgView;
	//Bitmap bitmap;
	Button leftRotate;
	Button rightRotate;
	
	float minScaleR; //最小缩放比例
	static final float MAX_SCALE = 4f; //最大缩放比例
	
	static final int  NONE = 0;  //初始状态
	static final int DRAG = 1; //拖动
	static final int ZOOM = 2; // 缩放
	int mode = NONE;
	
	PointF prev = new PointF();
	PointF mid = new PointF();
	float dist = 1f;
	
	int rotate; //旋转角度
	DownloadImageTask download;

	private ImageView MarkedRect;
	private int[][] rectPoint;
	private Handler handler;
	private String[] notate_info;
	private int notate_num;
	
	
	public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		ImageView bmImage;
	    Bitmap bitmap;

	    public DownloadImageTask(ImageView bmImage, Bitmap bitmap) {
	        this.bmImage = bmImage;
	        this.bitmap = bitmap;
	        //this.bmImage.bitmap = bitmap;
	    }

	    protected Bitmap doInBackground(String... urls) {
	        String urldisplay = urls[0];
	        Bitmap mIcon11 = null;
	        try {
	            InputStream in = new java.net.URL(urldisplay).openStream();
	            mIcon11 = BitmapFactory.decodeStream(in);
	            this.bitmap = mIcon11;
	        } catch (Exception e) {
	            Log.e("Error", e.getMessage());
	            e.printStackTrace();
	        }
	        return mIcon11;
	    }
	    
	    

	    protected void onPostExecute(Bitmap result) {
	    	Bitmap tmp = result.copy(result.getConfig(), true);
	    	
	    	for (int num = 0; num < notate_num; num++)
	    	{
	    		int thickness = 2; //笔画粗度为2*thickness+1
	    		int x1 = rectPoint[num][0];//(int)(rectPoint[num][0]*width);
	    		int y1 = rectPoint[num][1];//(int)(rectPoint[num][1]*height);
	    		int x2 = rectPoint[num][2];//(int)(rectPoint[num][2]*width);
	    		int y2 = rectPoint[num][3];//(int)(rectPoint[num][3]*height);
	    		
	    		for(int i = x1+thickness; i <= x2-thickness; i++)
	    		{
	    			for(int j = y1-thickness; j <= y1+thickness; j++)
	    				tmp.setPixel(i, j, Color.RED);
	    			for(int j = y2-thickness; j <= y2+thickness; j++)
	    				tmp.setPixel(i, j, Color.RED);
	    		}
	    		for(int j = y1+thickness; j <= y2-thickness; j++)
	    		{
	    			for(int i = x1-thickness; i <= x1+thickness; i++)
	    				tmp.setPixel(i, j, Color.RED);
	    			for(int i = x2-thickness; i <= x2+thickness; i++)
	    				tmp.setPixel(i, j, Color.RED);
	    		}
	    		for(int i = x1-thickness; i <= x1+thickness; i++)
	    			for(int j = y1-thickness; j <= y1+thickness; j++)
	    				tmp.setPixel(i, j, Color.RED);
	    		for(int i = x2-thickness; i <= x2+thickness; i++)
	    			for(int j = y2-thickness; j <= y2+thickness; j++)
	    				tmp.setPixel(i, j, Color.RED);
	    		for(int i = x1-thickness; i <= x1+thickness; i++)
	    			for(int j = y2-thickness; j <= y2+thickness; j++)
	    				tmp.setPixel(i, j, Color.RED);
	    		for(int i = x2-thickness; i <= x2+thickness; i++)
	    			for(int j = y1-thickness; j <= y1+thickness; j++)
	    				tmp.setPixel(i, j, Color.RED);
	    		
	    	}
	    	
	        bmImage.setImageBitmap(tmp);
			
	        matrix.set(savedMatrix);
			CheckView();
			bmImage.setImageMatrix(matrix);
	    }
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_one_image);
		
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_bg));
		MarkedRect = (ImageView)findViewById(R.id.one_image_view);
		//bitmap = BitmapFactory.decodeResource(getResources(), this.getIntent().getExtras().getInt("IMG")); //获取图片资源
		final int tempclass = getIntent().getExtras().getInt("imageclass");
		
		handler = new Handler(){
		    @Override
		    public void handleMessage(Message msg) {
		        super.handleMessage(msg);
		        Bundle data = msg.getData();
		        String JSON = data.getString("result");
		        String id=null, caseid=null, picurl = null;
				//int notate_num;
		        //int[][] notate_location = null;
		        //String[] notate_info = null;		
				try {
		            JSONTokener jsonParser = new JSONTokener(JSON);
		            JSONObject jsonObj = (JSONObject) jsonParser.nextValue(); 
		            id = jsonObj.getString("_id");
		            caseid = jsonObj.getString("caseid");
		            picurl = jsonObj.getString("picurl");
		            JSONArray notate = jsonObj.getJSONArray("notate");
		            notate_num = notate.length();
		            rectPoint = new int [notate_num][4];
		            notate_info = new String [notate_num];
		            for (int i = 0; i < notate_num; i++) {
		                JSONObject jo = (JSONObject)notate.optJSONObject(i);
		                rectPoint[i][0] = (int)jo.getDouble("x1");
		                rectPoint[i][1] = (int)jo.getDouble("y1");
		                rectPoint[i][2] = (int)jo.getDouble("x2");
		                rectPoint[i][3] = (int)jo.getDouble("y2");
		                notate_info[i] = jo.getString("data");
		            }
		            
		            Log.v("debug", rectPoint[0][0] + " " + rectPoint[0][1] + " " + rectPoint[0][2] + " " + rectPoint[0][3]);
		            
		    		download.execute("http://166.111.80.119/" + picurl);
		        } catch (Exception e) {
		            e.printStackTrace();
		        }

		    }
		};
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				HttpClient client = new DefaultHttpClient();  
		        String getURL = "http://166.111.80.119/getinfo?caseid=asd";
		        HttpGet get = new HttpGet(getURL);
		        String tString = "";
		        try {
					HttpResponse responseGet = client.execute(get);  
			        HttpEntity resEntityGet = responseGet.getEntity();  
			        if (resEntityGet != null) {  
			                    //do something with the response
			        	tString = EntityUtils.toString(resEntityGet);
			        	
			        }
				} catch (Exception e) {
					e.printStackTrace();
				}
		        Message message = new Message();
		        Bundle data = new Bundle();
		        data.putString("result", tString);
		        message.setData(data);
		        handler.sendMessage(message);
			}
		};
		new Thread(runnable).start();
		

		Bitmap bitmap = BitmapFactory.decodeResource(OneImageActivity.this.getResources(),R.drawable.loading); //获取图片资源
		download = new DownloadImageTask(MarkedRect, bitmap);
		
		MarkedRect.setImageBitmap(download.bitmap); //填充控件
		MarkedRect.setOnTouchListener(this); //触屏监听
		dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm); //获取分辨率
		minZoom();
		center();
		MarkedRect.setImageMatrix(matrix);
		leftRotate = (Button)findViewById(R.id.image_leftRotate);
		rightRotate = (Button)findViewById(R.id.image_rightRotate);
		leftRotate.setOnClickListener(this);
		this.rightRotate.setOnClickListener(this);
		
		//imgView.setImageBitmap(mDrawCG.drawRect());
		
		//MarkedRect.showRect(rectPoint);
		
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == leftRotate.getId()){
			//matrix.postRotate(-90,imgView.getWidth()/2,imgView.getHeight()/2);
			matrix.postRotate(-90,dm.widthPixels/2,dm.heightPixels/2);
			MarkedRect.setImageMatrix(matrix);
			savedMatrix.set(matrix);
			mode = NONE;
			CheckView();
		}
		else if(v.getId() == rightRotate.getId()){
			matrix.postRotate(90,dm.widthPixels/2,dm.heightPixels/2);
			MarkedRect.setImageMatrix(matrix);
			savedMatrix.set(matrix);
			mode = NONE;
			CheckView();
		}
		
	}
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch(event.getAction()&MotionEvent.ACTION_MASK){
		case MotionEvent.ACTION_DOWN: //主点按下
			savedMatrix.set(matrix);
			prev.set(event.getX(),event.getY());
			mode = DRAG;
			break;
		case MotionEvent.ACTION_POINTER_DOWN: //副点按下
			dist = spacing(event);
			if(dist > 10f){
				//连续两点的距离大于10，多点模式
				savedMatrix.set(matrix);
				midPoint(mid,event);
				mode = ZOOM;
			}
			break;
		
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			break;
		case MotionEvent.ACTION_MOVE:
			if(mode == DRAG){
				matrix.set(savedMatrix);
				matrix.postTranslate(event.getX()-prev.x, event.getY()-prev.y);
			}
			else if(mode == ZOOM){
				float newDist = spacing(event);
				if(newDist >10f){
					matrix.set(savedMatrix);
					float tScale = newDist/dist;
					matrix.postScale(tScale, tScale,mid.x,mid.y);
				}
			}
			break;
		}
		MarkedRect.setImageMatrix(matrix);
		CheckView();
		return true;
		
	}
	/**
	 * 限制最大最小缩放比例，自动居中
	 * */
	private void CheckView(){
		float p[] = new float[9];
		matrix.getValues(p);
		/*if(mode == ZOOM){
			if(p[0]<minScaleR){
				matrix.setScale(minScaleR, minScaleR);
			}
			if(p[0]>MAX_SCALE){
				matrix.set(savedMatrix);
			}
		}*/
		//else matrix.set(savedMatrix);
		/*if(p[0]<minScaleR){
			matrix.setScale(minScaleR, minScaleR);
		}*/
		if(p[0]>MAX_SCALE){
			matrix.set(savedMatrix);
			//matrix.setScale(MAX_SCALE, MAX_SCALE);
		}
		center();
	}
	/**
	 * 最小缩放比例
	 * */
	private void minZoom(){
		minScaleR = Math.min(
				(float)dm.widthPixels/(float)download.bitmap.getWidth(), 
				(float)dm.heightPixels/(float)download.bitmap.getHeight());
		if(minScaleR < 1.0){
			matrix.postScale(minScaleR, minScaleR);
		}
	}
	private void center(){
		center(true,true);
	}
	
	/**
	 * 横向、纵向居中
	 * */
	protected void center(boolean horizontal, boolean vertical){
		
		Matrix m = new Matrix();
		m.set(matrix);
		RectF rect = new RectF(0,0,download.bitmap.getWidth(),download.bitmap.getHeight());
		m.mapRect(rect);
		
		float height = rect.height();
		float width = rect.width();
		
		float deltaX = 0;
		float deltaY = 0;
		
		// 图片小于屏幕大小，则居中显示。大于屏幕，上方留空则往上移，下方留空则往下移
		if(vertical){
			int screenHeight = dm.heightPixels;
			if(height<screenHeight){
				deltaY = (screenHeight - height)/2 - rect.top;
			}
			else if(rect.top>0){
				deltaY = -rect.top;
			}
			else if(rect.bottom<screenHeight){
				deltaY = MarkedRect.getHeight() - rect.bottom;
			}
		}
		if(horizontal){
			int screenWidth = dm.widthPixels;
			if(width<screenWidth){
				deltaX = (screenWidth-width)/2 - rect.left;
			}
			else if(rect.left>0){
				deltaX = - rect.left;
			}
			else if(rect.right < screenWidth){
				deltaX = screenWidth - rect.right;
			}
		}
		matrix.postTranslate(deltaX, deltaY);
	}
	
	/**
	 * 两点的距离
	 * */
	private float spacing(MotionEvent event){
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x*x+y*y);
	}
	/**
	 * 两点的中点
	 * */
	private void midPoint(PointF point, MotionEvent event){
		float x = event.getX(0)+event.getX(1);
		float y = event.getY(0)+event.getY(1);
		point.set(x/2,y/2);
	}

	
	

}
