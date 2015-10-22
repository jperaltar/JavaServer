package lab15.jperalta.server;

/**
 * Regular concurrent server
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import lab15.jperalta.messages.Msg;
import lab15.jperalta.cache.Cache;
import lab15.jperalta.messages.Petition;

public class Srv {
	private String srvname;
	
	private int port;
	private ServerSocket sk;
	private boolean stop;
	
	private Thread servethread;
	private Socket fd;
	
	/** Pool of clients being served */
	private CliPool pool;
	
	/** Directory where we are going to look for
	 * the different files
	 */
	public String directory;
	
	/** Cache */
	public static Cache cache;
	
	/** Control client parameters */
	private ServerSocket ctrlSk;
	private int ctrlPort;
	private boolean ctrlStop;
	
	private Thread ctrlThread;
	private Socket ctrlFd;
	
	public Srv(String name, String directory){		
		this.directory = directory;
		srvname = name;
		
		cache = new Cache();
		pool = new CliPool();
		stop = false;
		ctrlStop = false;		
	}
	
	/**
	 * Class server for every client (Concurrent)
	 */
	
	class CliSrv implements Runnable {
		Socket clisk;
		DataInputStream dis;
		BufferedReader disreader;
		DataOutputStream dos;
		BufferedWriter doswriter;
		
		boolean exit;
		
		public CliSrv(Socket sk) {
			clisk = sk;
			pool.add(this);
			exit = false;
		}
		
		/**
		 * Attends GET petitions and replies with
		 * the correspondent resource.
		 * Previously the demanded resource is sought
		 * in cache.
		 */
		protected  void serveClient(Petition p){
			byte [] bytearray;
			String resource = p.getResource();
			
			try {
				System.err.println(resource);
				bytearray = cache.get(resource, p);
				if(bytearray == null) {
					System.err.println("No esta en cache");
					bytearray = p.processPetition(dos);
					cache.add(resource, bytearray);
				} else {
					System.err.println("Esta en cache");
				}
				
				dos.write(bytearray);
				dos.flush();
			} catch(Exception e){
				throw new RuntimeException(e);
			}
		}
		
		public void run() {
			String resource;
			
			try {
				//Create reader and writer
				dis = new DataInputStream(
					new BufferedInputStream(clisk.getInputStream()));
				disreader = new BufferedReader(new InputStreamReader(dis));
				dos = new DataOutputStream(
					new BufferedOutputStream(clisk.getOutputStream()));
				doswriter = new BufferedWriter(new OutputStreamWriter(dos));
				
				while(!exit) {
					resource = Petition.rcvPetition(disreader);
					if(resource != null) {
						resource = directory + "/" + resource;
						Petition p = new Petition(resource);
						serveClient(p);
					}else {
						System.err.println("Operation not supported");
					}
					
					dos.close();
				}
			}catch(Exception e) {
				//
			}finally {
				closeall();
				pool.remove(this);
					
			}
		}
		
		public void abort() {
			exit = true;
			closeall();
		}
		
		private void closeall() {
			try {
				if(clisk != null)
					clisk.close();
				if(dis != null)
					dis.close();
				if(dos != null)
					dos.close();
			}catch(Exception e) {
				//We don't do anything
			}
		}
	}
	
	class CtrlCliSrv implements Runnable {
		Socket clisk;
		DataInputStream ctrlDis;
		DataOutputStream ctrlDos;
		
		boolean exit;
		
		public CtrlCliSrv(Socket sk) {
			clisk = sk;
			exit = false;
		}
		
		/**
		 * Attends GET petitions for the control server
		 */
		public void serveClient(Msg m) {
			Msg maux = null;
			byte type;
			
			try {
				type = m.getType();
										
				switch(type) {
				case Msg.FLUSH: ((Msg.Flush) m).process(cache);
				break;
				case Msg.DEL: maux = ((Msg.Del) m).process(cache);
				break;
				case Msg.LIST: maux = ((Msg.List) m).process(cache);
				break;
				case Msg.QUIT: close();
				break;
				case Msg.POLICY: maux = ((Msg.Policy) m).process(cache);
				}		
				if(maux != null) {
					System.err.println("Enviamos: " + maux);
					maux.sendTo(ctrlDos);
					ctrlDos.flush();
				}
			} catch(Exception e){
				throw new RuntimeException(e);
			}
		}
		
		public void run() {
			Msg m;
			
			try {
				//Create reader and writer
				ctrlDis = new DataInputStream(
					new BufferedInputStream(clisk.getInputStream()));
				ctrlDos = new DataOutputStream(
					new BufferedOutputStream(clisk.getOutputStream()));
				
				while(!exit) {
					m = Msg.rcvFrom(ctrlDis);
					
					if(m == null){
						break;
					}else {
						serveClient(m);
					}
				}
			}catch(Exception e) {
				//
			}finally {
				closeall();					
			}
		}
		
		private void closeall() {
			try {
				if(clisk != null)
					clisk.close();
				if(ctrlDis != null)
					ctrlDis.close();
				if(ctrlDos != null)
					ctrlDos.close();
			}catch(Exception e) {
				//We don't do anything
			}
		}
	}
	
	public String toString() {
		return srvname + " [" + port + "] + control [" + ctrlPort + "]";
	}
	
	/**
	 * Makes the binding to the given port and starts the service in a new thread.  
	 */
	public void start(int port, int ctrlPort){
		stop = false;
		ctrlStop = false;
		if(servethread != null){
			throw new RuntimeException("Thread failure");
		}
		
		this.port = port;
		this.ctrlPort = ctrlPort;
		
		try {
			sk = new ServerSocket(port);
			ctrlSk = new ServerSocket(ctrlPort);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		ctrlThread = new Thread(){
			public void run(){
				ctrlServe();
			}
		};
		ctrlThread.start();
		
		servethread = new Thread(){
			public void run(){
				serve();
			}
		};
		servethread.start();
	}
	
	/**
	 * Opens input and output stream and starts the default service
	 * after the service is over closes the socket and input and output stream.
	 */
	private void serve() {
		while(!stop && sk != null){
			try {
				fd = sk.accept();
				new Thread(new CliSrv(fd)).start();;
			} catch (Exception e) {
				stop = true;
			}
		}
		System.err.println("END OF SERVICE");
	}
	
	private void ctrlServe() {
		while(!ctrlStop && ctrlSk != null){
			try {
				ctrlFd = ctrlSk.accept();
				new Thread(new CtrlCliSrv(ctrlFd)).start();;
			} catch (Exception e) {
				ctrlStop = true;
			}
		}
		System.err.println("END OF CONTROL SERVICE");
	}
	
	// Control client methods
	
	private void endThread() {
		if (servethread != null) {
			try {
				servethread.join(5000);
				if (servethread.isAlive()) {
					System.out.println("Cerramos el thread");
					servethread.interrupt();
					servethread.join();
				}
				servethread = null;
			} catch (Exception e) {
				//Don't do anything
			}
		}
	}
	
	public void close() {
		int deleted;
	
		stop = true;
		deleted = pool.abort();
		System.err.println(deleted + " clients eliminated");
		try {
			sk.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally {
			sk = null;
			System.err.println("ok");
			endThread();
		}
	}
}
