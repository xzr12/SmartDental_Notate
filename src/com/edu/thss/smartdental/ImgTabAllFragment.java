package com.edu.thss.smartdental;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.edu.thss.smartdental.adapter.EMRListAdapter;
import com.edu.thss.smartdental.adapter.ImgListAdapter;
import com.edu.thss.smartdental.model.EMRElement;
import com.edu.thss.smartdental.model.ImageElement;

import android.R.id;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class ImgTabAllFragment extends Fragment{
	private ImgListAdapter listAdapter;
	private EditText editText;
	private ListView list;
	private ArrayList<ImageElement> images;
	
	//test JSON
	String JSON = "{" +
			"\"_id\" : \"54831b2b711cb3292d74de5c\"," +
			"\"pic_info\" : [" +
							"{ \"pic_name\" : \"活检1_*40\", \"position\" : \"活体组织第一次检查，镜下*40\", \"date\" : \"2011-1-15\", \"read\" : 1, \"delete\" : 1, \"record\": 1, \"caseid\" : \"huahua\"}," +
							"{ \"pic_name\" : \"活检1_*100\", \"position\" : \"活体组织第一次检查，镜下*100\", \"date\" : \"2011-1-15\", \"read\" : 1, \"delete\" : 1, \"record\": 1, \"caseid\" : \"huahua\"}," +
							"{ \"pic_name\" : \"活检2_*40\", \"position\" : \"活体组织第二次检查，镜下*40\", \"date\" : \"2012-1-25\", \"read\" : 1, \"delete\" : 1, \"record\": 1, \"caseid\" : \"huahua\"}," +
							"{ \"pic_name\" : \"活检2_*100\", \"position\" : \"活体组织第二次检查，镜下*100\", \"date\" : \"2012-1-25\", \"read\" : 0, \"delete\" : 1, \"record\": 1, \"caseid\" : \"huahua\"}," +
							"{ \"pic_name\" : \"活检3_*100\", \"position\" : \"活体组织第三次检查，镜下*100\", \"date\" : \"2013-1-25\", \"read\" : 0, \"delete\" : 1, \"record\": 1, \"caseid\" : \"huahua\"}" +
							"]}";	
	
	private String id = null;
	private int num_of_pic = 0;
	public class picture {
		private String name;
		private String position;
		private String date;
		//1表示已读，删除和标记，0表示未读，未删除和未标记
		private Boolean read;
		private Boolean delete;
		private Boolean record;
		private String caseid;
		
		
		public picture() {
			this.name = null;
			this.position = null;
			this.date = null;
			this.read = false;
			this.delete = false;
			this.record = false;
			this.caseid = null;
		}
		
		//set方法
		public void setName(String name) {
			this.name = name;
		}
		
		public void setPosition(String position) {
			this.position = position;
		}
		
		public void setDate(String date) {
			this.date = date;
		}
		
		public void setRead(int read) {
			if (read == 0) {
				this.read = false;
			}
			else {
				this.read = true;
			}
		}
		
		public void setDelete(int delete) {
			if (delete == 0) {
				this.delete = false;
			}
			else {
				this.delete = true;
			}
		}
		
		public void setRecord(int record) {
			if (record == 0) {
				this.record = false;
			}
			else {
				this.record = true;
			}
		}
		
		public void setCaseid(String caseid) {
			this.caseid = caseid;
		}
		
		//get方法
		public String getName() {
			return this.name;
		}
				
		public String getPosition() {
			return this.position;
		}

		public String getDate() {
			return this.date;
		}

		public Boolean getRead() {
			return this.read;
		}

		public Boolean getDelete() {
			return this.delete;
		}

		public Boolean getRecord() {
			return this.record;
		}

		public String getCaseid() {
			return this.caseid;
		}
	};
	public picture[] pic;
	
	public void getJSON(){
		//从网上获取的JSON中解析
		try {
			JSONTokener jsonParser = new JSONTokener(JSON);
			JSONObject jsonObj = (JSONObject) jsonParser.nextValue();
			id = jsonObj.getString("_id");
			JSONArray info = jsonObj.getJSONArray("pic_info");
			num_of_pic = info.length();
			pic = new picture[num_of_pic];
			for (int i = 0; i < num_of_pic; i++) {
				JSONObject jo = (JSONObject)info.optJSONObject(i);
				pic[i] = new picture();
				pic[i].setName(jo.getString("pic_name"));
				pic[i].setPosition(jo.getString("position"));
				pic[i].setDate(jo.getString("date"));
				pic[i].setRead(jo.getInt("read"));
				pic[i].setDelete(jo.getInt("delete"));
				pic[i].setRecord(jo.getInt("record"));
				pic[i].setCaseid(jo.getString("caseid"));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_image_all, container,false);
		editText = (EditText)rootView.findViewById(R.id.image_searchbox);
		editText.addTextChangedListener(filterTextWatcher);
		list = (ListView)rootView.findViewById(R.id.image_list);
		initImages();
		listAdapter = new ImgListAdapter(images,this.getActivity().getApplicationContext());
		list.setAdapter(listAdapter);
		
		return rootView;
	}

	private TextWatcher filterTextWatcher = new TextWatcher(){

		@Override
		public void afterTextChanged(Editable s) {
			
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			listAdapter.getFilter().filter(s);	
		}
	};


	private void initImages(){
		getJSON();
		images = new ArrayList<ImageElement>();
		ImageElement i;
		for (int j = 0; j < num_of_pic; j++) {
			i = new ImageElement(pic[j].getName(), pic[j].getPosition(), pic[j].getDate(), pic[j].getDelete(), pic[j].getRecord(), pic[j].getRead(), pic[j].getCaseid());
			images.add(i);
		}
//		i = new ImageElement("活检1_*40","活体组织第一次检查，镜下*40","2011-1-15");
//		images.add(i);
//		i = new ImageElement("活检1_*100","活体组织第一次检查，镜下*100","2011-1-15");
//		images.add(i);
//		i = new ImageElement("活检2_*40","活体组织第二次检查，镜下*40","2012-1-25");
//		images.add(i);
//		i = new ImageElement("活检2_*100","活体组织第二次检查，镜下*100","2013-1-25");
//		images.add(i);
	}
}
