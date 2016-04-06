package pt.upa.transporter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TransporterClientApplication {
	static private final Logger log = LogManager.getRootLogger();

	public static void main(String[] args) throws Exception {
		System.out.println(TransporterClientApplication.class.getSimpleName() + " starting...");
	}
}
