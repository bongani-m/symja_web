package org.matheclipse.gwt.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Keep alive for cron job
 * 
 */
public class CronKeepaliveServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2907546068369032728L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	}

}
