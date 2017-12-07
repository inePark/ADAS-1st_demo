package com.example.android.bluetoothlegatt.com;


public class CarouselDataItem {
	public String m_szImgPath;
	public long m_nDocDate;
	public String m_szDocName;
	
	public CarouselDataItem(String path, long date, String name) {
		m_szImgPath = path;
		m_nDocDate = date;
		m_szDocName = name;
		
		/*System.out.println("path : " + path);
		System.out.println("date : " + date);
		System.out.println("name : " + name);
		*/
	}

	
	public String getImgPath() {
		return m_szImgPath;
	}
	public String getDocText() {
		return m_szDocName;
	}

}
