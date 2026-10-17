package com.tlitracker.app;

import android.content.*;
import org.json.*;

public class Ledger {
 static synchronized JSONObject data(Context c){
  try {
   return new JSONObject(c.getSharedPreferences("ledger",0)
    .getString("data","{}"));
  } catch(Exception e) {
   return new JSONObject();
  }
 }

 static synchronized void save(Context c,JSONObject d){
  c.getSharedPreferences("ledger",0).edit()
   .putString("data",d.toString()).apply();
 }

 static synchronized void candidate(Context c,String name,String mode){
  try {
   JSONObject d=data(c);
   JSONArray a=d.optJSONArray("candidates");
   if(a==null)a=new JSONArray();
   JSONObject o=new JSONObject();
   o.put("name",name);
   o.put("mode",mode);
   o.put("time",System.currentTimeMillis());
   o.put("confirmed",false);
   a.put(o);
   d.put("candidates",a);
   save(c,d);
  } catch(Exception ignored){}
 }

 static synchronized void confirm(Context c,String name,double fe,int qty){
  try {
   JSONObject d=data(c);
   JSONArray a=d.optJSONArray("drops");
   if(a==null)a=new JSONArray();
   a.put(new JSONObject()
    .put("name",name)
    .put("fe",fe)
    .put("qty",qty)
    .put("time",System.currentTimeMillis()));
   d.put("drops",a);
   save(c,d);
  } catch(Exception ignored){}
 }

 static synchronized void markReviewed(Context c,int ix){
  try {
   JSONObject d=data(c);
   d.getJSONArray("candidates").getJSONObject(ix)
    .put("confirmed",true);
   save(c,d);
  } catch(Exception ignored){}
 }

 static synchronized void cost(Context c,double v){
  try {
   JSONObject d=data(c);
   d.put("cost",v);
   save(c,d);
  } catch(Exception ignored){}
 }

 static synchronized void clock(Context c){
  try {
   JSONObject d=data(c);
   d.put("start",System.currentTimeMillis());
   save(c,d);
  } catch(Exception ignored){}
 }
}
