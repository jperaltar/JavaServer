package lab15.jperalta.client;

import lab15.jperalta.cache.Cache;
import lab15.jperalta.client.Cli;

public class Jclient {

	public static void main(String[] args) {
		int port = 9090;
		int policy;
		
		Cli client = new Cli("localhost", port);
		
		if("flush".equals(args[0])) {
			client.flush();
		}else if("del".equals(args[0])) {
			client.delete(args[1]);
		}else if("ls".equals(args[0])) {
			client.list();
		}else if("policy".equals(args[0])) {
			if("fifo".equals(args[1])) {
				policy = Cache.FIFO;
				client.policy(policy);
			}else if("lru".equals(args[1])) {
				policy = Cache.LRU;
				client.policy(policy);
			}else if("random".equals(args[1])) {
				policy = Cache.RND;
				client.policy(policy);
			}else {
				System.err.println("Policy not allowed");
			}
		}else if("quit".equals(args[0])) {
			client.quit();
		}else {
			System.err.println("Function not avaliable");
		}
		client.close();
	}
}
