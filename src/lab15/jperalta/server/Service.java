package lab15.jperalta.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public interface Service {
	public void serveClient(DataInputStream dis, DataOutputStream dos);
}
