package com.vutbr.fit.tam.acceleroscroll;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class AcceleroScrollImages extends ListActivity {

	private static final String TAG = "AcceleroScrollImages";
	
	private List<String> item = null;
	private List<String> path = null;
	private String root="/mnt/sdcard/";
	private TextView myPath;
	 
	    /** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.browser_list);
	        myPath = (TextView)findViewById(R.id.path);
	        getDir(root);
	    }
	    
	    private void getDir(String dirPath)
	    {
	     myPath.setText("Location: " + dirPath);
	     
	     item = new ArrayList<String>();
	     path = new ArrayList<String>();
	     
	     File f = new File(dirPath);
	     File[] files = f.listFiles();
	     
	     if(!dirPath.equals(root))
	     {

	      item.add(root);
	      path.add(root);
	      
	      item.add("../");
	      path.add(f.getParent());
	            
	     }
	     
	     for(int i=0; i < files.length; i++)
	     {
	       File file = files[i];
	       path.add(file.getPath());
	       if(file.isDirectory())
	        item.add(file.getName() + "/");
	       else
	        item.add(file.getName());
	     }

	     ArrayAdapter<String> fileList =
	      new ArrayAdapter<String>(this, R.layout.browser_row, item);
	     setListAdapter(fileList);
	    }

	 @Override
	 protected void onListItemClick(ListView l, View v, int position, long id) {
	  
	  final String imagePath = path.get(position);	 
	  File file = new File(imagePath);
	  
	  if (file.isDirectory())
	  {
	   if(file.canRead())
	    getDir(imagePath);
	   else
	   {
	    new AlertDialog.Builder(this)
	    .setIcon(R.drawable.icon)
	    .setTitle("[" + file.getName() + "] folder can't be read!")
	    .setPositiveButton("OK", 
	      new DialogInterface.OnClickListener() {
	       
	       public void onClick(DialogInterface dialog, int which) {
	        // TODO Auto-generated method stub
	       }
	      }).show();
	   }
	  }
	  else
	  {
		Bundle bundle = new Bundle();
		bundle.putString("imagePath", imagePath);
		
		Intent resultImageIntent = new Intent();
    	resultImageIntent.putExtras(bundle);
    	Log.v(TAG, imagePath);
    	setResult(RESULT_OK, resultImageIntent);  	
        finish();
		  
	/*
	   new AlertDialog.Builder(this)
	    .setIcon(R.drawable.icon)
	    .setTitle("Load [" + file.getName() + "]?")
	    .setPositiveButton("OK", 
	      new DialogInterface.OnClickListener() {
	       
	       @Override
	       public void onClick(DialogInterface dialog, int which) {
	    	   	Intent resultImageIntent = new Intent();
	    	   	resultImageIntent.putExtra("imagePath", imagePath);
	    	   	setResult(RESULT_OK, resultImageIntent);
               	finish();
               	
	       }
	      }).show();*/
	  }
	 }
	 
}
