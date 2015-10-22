package lab15.jperalta.server;

public class Jweb {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final int port = 8181;
		final int ctrlport = 9090;
						
		final Srv srv = new Srv("svc", args[0]);
		srv.start(port, ctrlport);
	}
}
