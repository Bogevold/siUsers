package no.sismo;

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
//import java.security.Security;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import weblogic.security.Security;
import weblogic.security.principal.WLSGroupImpl;
import weblogic.security.principal.WLSUserImpl;
import weblogic.servlet.security.ServletAuthentication;


@Named
@SessionScoped
public class SiBrukereBean implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8012137007774100272L;
	private String bid;
	private String msg;
	private List<String> roller;
	
	
	public List<String> getRoller()  {
		Subject sub = Security.getCurrentSubject();
		Set<Principal> allPrincipals = sub.getPrincipals();
		roller = new ArrayList<String>(); 
		for (Principal principal : allPrincipals){
			System.out.println("class: " + principal.getClass().getName());
			if (principal instanceof WLSUserImpl) {
				System.out.println("public found user: " + principal.getName());
			} else if (principal instanceof WLSGroupImpl) {
				roller.add(principal.getName());
			}
			
		}
		return roller;
	    //return Arrays.asList(weblogic.security.SubjectUtils.getPrincipalNames(Security.getCurrentSubject()).split("/"));
	}

	final static Logger logger = LogManager.getLogger(SiBrukereBean.class);
	
	/*
	@Resource(name = "ds", mappedName = "jdbc/siAuth")
	private DataSource ds;
	*/
	
	public String getBid() {
		return bid;
	}
	public void setBid(String bid) {
		this.bid = bid;
	}
	public String getMsg() {
		return msg;
	}
	
	public void sjekkBidTest() {
		msg = "Ja så langt så bra";
	}

	public void sjekkBid() {	
		try {
			Context initContext = new InitialContext();
			DataSource ds = (DataSource)initContext.lookup("jdbc/siAuthPG");
			Connection conn = ds.getConnection();
			String sql = "select count('x') ant from bruker where sbr_kortnavn = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, bid);
			
			ResultSet rs = ps.executeQuery();
			logger.info("Etter executeQuery");
				
			while (rs.next()) {
				Integer i1 = rs.getInt("ant");
				logger.info("Verdi i ant ="+ i1);
				if (i1==1) {
					msg = "Fant bruker, bestilt passord";
				} else {
					msg = "Ingen bruker funnet, sjekk bid eller bestill via admin";
				}
			}
			
		} catch (SQLException sex) {
			sex.printStackTrace();
			logger.error(sex.getMessage());
		} catch (NamingException ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage());
		}
	}
	
	public void logout()
	{
		boolean ret = true;
		ExternalContext ex =  FacesContext.getCurrentInstance().getExternalContext();
		HttpServletRequest req = (HttpServletRequest)ex.getRequest();
		if (req == null) {
			ret = false;
		}
	    ServletAuthentication.invalidateAll(req);
	    if (ret) {
	    	try {
				ex.redirect("/index.xhtml");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}	
}
