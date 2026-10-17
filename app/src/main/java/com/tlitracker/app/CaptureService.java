package com.tlitracker.app;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.graphics.*;
import android.hardware.display.*;
import android.media.*;
import android.media.projection.*;
import android.os.*;
import android.util.*;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.*;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.nio.*;
import java.util.*;

public class CaptureService extends Service {
 MediaProjection projection;
 VirtualDisplay display;
 ImageReader reader;
 HandlerThread thread;
 Handler handler;
 TextRecognizer recognizer;
 String mode="live";
 Set<String> baseline=new HashSet<>(),recent=new HashSet<>();
 long last=0;
 boolean busy=false;
 int width,height,density;

 @Override public void onCreate(){
  super.onCreate();
  NotificationChannel ch=new NotificationChannel("scan","Screen scan",NotificationManager.IMPORTANCE_LOW);
  ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);
  recognizer=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
 }

 @Override public int onStartCommand(Intent intent,int flags,int id){
  if(intent==null)return START_NOT_STICKY;
  if(projection!=null){stopSelf();return START_NOT_STICKY;}
  mode=intent.getStringExtra("mode");
  if(mode==null)mode="live";
  Notification note=new Notification.Builder(this,"scan")
   .setContentTitle("TLI Loot Scanner running")
   .setContentText("Screen text is processed locally; stop in the app")
   .setSmallIcon(android.R.drawable.ic_menu_view).build();
  startForeground(2,note,ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
  try{
   MediaProjectionManager m=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
   projection=m.getMediaProjection(intent.getIntExtra("result",-1),
    intent.getParcelableExtra("permission"));
   projection.registerCallback(new MediaProjection.Callback(){
    @Override public void onStop(){stopSelf();}
   },new Handler(Looper.getMainLooper()));
   DisplayMetrics metrics=getResources().getDisplayMetrics();
   width=metrics.widthPixels;
   height=metrics.heightPixels;
   density=metrics.densityDpi;
   thread=new HandlerThread("tli-frames");
   thread.start();
   handler=new Handler(thread.getLooper());
   reader=ImageReader.newInstance(width,height,PixelFormat.RGBA_8888,2);
   reader.setOnImageAvailableListener(r->{
    Image image=r.acquireLatestImage();
    if(image==null)return;
    try{
     long now=System.currentTimeMillis();
     if(busy||now-last<2000)return;
     last=now;
     Image.Plane p=image.getPlanes()[0];
     ByteBuffer buffer=p.getBuffer();
     int pixelStride=p.getPixelStride(),rowStride=p.getRowStride();
     Bitmap padded=Bitmap.createBitmap(rowStride/pixelStride,height,Bitmap.Config.ARGB_8888);
     padded.copyPixelsFromBuffer(buffer);
     Bitmap bmp=Bitmap.createBitmap(padded,0,0,width,height);
     padded.recycle();
     busy=true;
     recognizer.process(InputImage.fromBitmap(bmp,0))
      .addOnSuccessListener(result->analyze(result))
      .addOnCompleteListener(task->{busy=false;bmp.recycle();});
    }catch(Exception e){busy=false;}
    finally{image.close();}
   },handler);
   display=projection.createVirtualDisplay("TLI scanner",width,height,density,
    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,reader.getSurface(),null,handler);
  }catch(Exception e){stopSelf();}
  return START_NOT_STICKY;
 }

 void analyze(Text result){
  Set<String> lines=new HashSet<>();
  for(Text.TextBlock block:result.getTextBlocks())
   for(Text.Line line:block.getLines()){
    String s=line.getText().trim();
    if(s.length()<4||s.length()>70||s.matches("[0-9 .,/%+:-]+"))continue;
    lines.add(s);
   }
  if("baseline".equals(mode)){
   baseline.clear();
   baseline.addAll(lines);
   getSharedPreferences("scan",0).edit()
    .putStringSet("baseline",new HashSet<>(baseline)).apply();
   stopSelf();
   return;
  }
  if("compare".equals(mode)){
   baseline=getSharedPreferences("scan",0)
    .getStringSet("baseline",new HashSet<>());
   for(String s:lines)
    if(!baseline.contains(s))
     Ledger.candidate(this,s,"inventory comparison (unverified)");
   stopSelf();
   return;
  }
  
if (!"live".equals(mode)) return;
for (String s : lines) {
    String name = s.trim();
    String lower = name.toLowerCase(java.util.Locale.ROOT);

    boolean loot = lower.contains("beacon")
        || lower.contains("flame sand")
        || lower.contains("winding key")
        || lower.contains("memory of")
        || lower.contains("outerwear")
        || lower.contains("starshard");

    boolean unwanted = lower.contains("[world]")
        || lower.contains("life:")
        || lower.contains("energy shield")
        || lower.contains("fps");

    if (loot && !unwanted && recent.add(name)) {
        Ledger.candidate(this, name, "visible loot (unverified)");
    }
}
 }
 @Override public void onDestroy(){
  if(display!=null)display.release();
  if(reader!=null)reader.close();
  if(projection!=null)projection.stop();
  if(thread!=null)thread.quitSafely();
  if(recognizer!=null)recognizer.close();
  super.onDestroy();
 }

 @Override public IBinder onBind(Intent i){return null;}
}
