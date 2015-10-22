package lab15.jperalta.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.text.Format;
import java.text.SimpleDateFormat;

import lab15.jperalta.cache.Cache;
import lab15.jperalta.messages.Msg;
import lab15.jperalta.messages.Msg.*;

public class Cli {
	private String name;
	private int port;
	
	private Socket fd;
	private DataInputStream in;
	private DataOutputStream out;
	
	public Cli(String hostname, int port) {
		this.name = hostname;
		this.port = port;
		
		try {
			fd = new Socket(name, this.port);
			
			in = new DataInputStream(new BufferedInputStream(
						fd.getInputStream()));
			out = new DataOutputStream(new BufferedOutputStream(
						fd.getOutputStream()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void closeall() {
		try {
			if(fd != null)
				fd.close();
			if(in != null)
				in.close();
			if(out != null)
				out.close();
		}catch(Exception e) {
			//We don't do anything
		}
	}
	
	// Client control methods
	
	public void flush() {
		Msg m = new Msg.Flush();
		
		m.sendTo(out);
		try {
			out.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void delete(String name) {
		Msg m = new Msg.Del(name);
		Rdel maux;
		
		m.sendTo(out);
		try {
			out.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		maux = (Rdel) Msg.rcvFrom(in);
		System.err.println(maux.msg);
		if("".equals(maux.msg))
			System.err.println("ok");
		else
			System.err.println("not found");
	}
	
	private void printList(Rlist maux) {
	    Format format = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");
		
		for(int i = 0; i < (maux.names).length; i++) {
			System.err.println(maux.names[i] + " " 
						+ maux.sizes[i] + " bytes " 
						+ format.format(maux.times[i]));
		}
	}
	
	public void list() {
		Msg m = new Msg.List();
		Rlist maux;
		
		m.sendTo(out);
		try {
			out.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		//Error
		maux = (Rlist) Msg.rcvFrom(in);
		printList(maux);
		
	}
	
	public void policy(int policy) {
		Msg m = new Msg.Policy(policy);
		Rpolicy maux;
		
		m.sendTo(out);
		try {
			out.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		maux = (Rpolicy) Msg.rcvFrom(in);
		System.err.println(Cache.getPolicy(maux.oldPolicy) 
					+ " to " + Cache.getPolicy(policy));
	}
	
	public void quit() {
		Msg m = new Msg.Quit();
		
		m.sendTo(out);
		try {
			out.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void close() {
		closeall();
		System.out.println("Closing...");
	}
}