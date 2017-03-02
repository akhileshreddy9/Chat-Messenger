package org.arkaaya;

import java.awt.DisplayMode;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

import org.arkaaya.Server.ClientThread;

// this class is designed extremely to perform read and write operations on passwd.txt file 
public class AuthenticationUtil {
	public static HashMap  <String , String>authenticationMap = new HashMap<String , String> ();
	static{
		readAuthDatafromTxtFile();
		System.out.println("from static block"+ authenticationMap.size());
	}
	// a map to maintain authentication data
	
	
	public static void readAuthDatafromTxtFile(){
		BufferedReader br = null;
		try{
			
			//URL fileUrl = getClass().getResource("passwd.txt");
              //     String fqfileName = fileUrl.getFile();
			//br = new BufferedReader(new FileReader(fqfileName));//InputStreamReader(getClass().getResourceAsStream("passwd.txt")));
			br = new BufferedReader(new FileReader(".\\passwd.txt"));
			//BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("passwd.txt")));
			String str=null;
			while((str =br.readLine())!=null){
				
				if(str.length()==0) continue; // to avoid empty lines
				//if(!str.contains("#"))
				String tempAry [] = str.trim().split("#");
				authenticationMap.put(tempAry[0],tempAry[1]);  //populating the authentication map 
				System.out.println(tempAry[0]+" "+tempAry[1]);
			}
			
			
		}catch(FileNotFoundException fnf){
			fnf.printStackTrace();
		}
		catch (IOException IOe) {
		IOe.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}finally{
			try{
				br.close();
			}catch(IOException ioe){
				ioe.printStackTrace();				
			}
		}
		
	} // EOM
	
	
	/* ------------------------------       ------------------------------------------------------*/
	
	private static void writeAuthDatatoTxtFile(String hostName, String password){
		
		String writableStr = hostName.trim()+"#"+password.trim();
		BufferedWriter bw = null;
		try{
		
		bw = new BufferedWriter(new FileWriter(".\\passwd.txt",true));
		bw.write("\n"+writableStr);
		bw.flush();
		
		// calling read again to update the authentication map
		
		}catch(IOException ioe){ioe.printStackTrace();}
		catch(Exception e){ e.printStackTrace();}
		finally{
			try{
				bw.close();
			}catch(IOException ioe){
				ioe.printStackTrace();				
			}
		}
		
	}
	
	/* ------------------------------       ------------------------------------------------------*/
	    public static void updateAuthMap(){
	    	readAuthDatafromTxtFile();
	    }
	/* ------------------------------       ------------------------------------------------------*/
	    
	   public static void diplayAuthMap(){
		   System.out.println(authenticationMap);
	   }
    /* ------------------------------       ------------------------------------------------------*/  
            public static String validateUser(String username, String password){
            	if(authenticationMap.size()!=0){
            	if(authenticationMap.containsKey(username)&& verifyLiveClient(username)){
            		return "user_duplicate";
            	} else if(authenticationMap.containsKey(username)&&((authenticationMap.get(username)).equals(password)) ){
            		return "valid_exist";
            	}else{
            		writeAuthDatatoTxtFile(username, password);  // writing user info on file
            		updateAuthMap(); // updating dynamic object
            		return "valid_new";
            	}
            	}else{
            		
            		System.out.println("auth Map size = "+authenticationMap.size());
            		return "-1";
            	}
            	
            }	
	/* ------------------------------       ------------------------------------------------------*/  
            public static boolean verifyLiveClient(String username){
            	System.out.println(" Live Client size is =  "+Server.liveClientList.size());
            	for(ClientThread cthread:Server.liveClientList){
            		
            		if((cthread.username).equals(username)) {return true;}
            	}
            	return false;
            }
            /* ------------------------------       ------------------------------------------------------*/        
	
	public static void main(String[] args) {
		//new AuthenticationUtil().readAuthDatafromTxtFile();
		//new AuthenticationUtil().writeAuthDatatoTxtFile("ram", "java");
		diplayAuthMap();
		//new AuthenticationUtil().readAuthDatafromTxtFile();
	}
	
	/* ------------------------------       ------------------------------------------------------*/
	
	
}
