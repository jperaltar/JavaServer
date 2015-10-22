package lab15.jperalta.server;

import java.util.ArrayList;

import lab15.jperalta.server.Srv.CliSrv;

public class CliPool {
	protected ArrayList<CliSrv> pool;
	
	public CliPool(){
		pool = new ArrayList<CliSrv>();
	}
	
	public synchronized void add(CliSrv cli) {
		pool.add(cli);
	}
	
	public synchronized boolean remove(CliSrv cli) {
		return pool.remove(cli);
	}
	
	public synchronized int abort() {
		int deleted = pool.size();
				
		for(CliSrv cli: pool) {
			cli.abort();
		}
		
		return deleted;
	}
}
