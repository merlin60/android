package com.jonma.service;

public interface BluetoothCom {
	
    public boolean sendData(String str);  
  
    public String getData();  
  
    public boolean syncStatus(int mode, int value);  
   
    public String generateCommand(int mode, int value); 

}
