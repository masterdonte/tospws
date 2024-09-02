import java.io.UnsupportedEncodingException;
import java.util.Base64;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.Assert;
import org.junit.Test;

public class TestTOS {
	private final static String host = "http://172.10.2.25/tosp";
	private final static String user = "externo";
	private final static String pass = "*******";
	private String sessionId;
	
	public TestTOS() {
		tryAuthenticate();
	}
	
	private void tryAuthenticate() {
		HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(user, pass);
		Client client = ClientBuilder.newClient();
		client.register(feature);
		WebTarget target = client.target(host).queryParam("portal", "ORG");
		Builder builder = target.request();
		Response response = builder.get(Response.class);		
		String output = response.readEntity(String.class);					
		if(output == null || output.toLowerCase().contains("bad credentials")) {
			sessionId =  null;
		}else {
			NewCookie newCookie = response.getCookies().get("JSESSIONID");			
			sessionId = newCookie.getValue();			
		}
	}
	
	private String getBasicAuthentication() {
		String token = user + ":" + pass;
		try {
			String encoded = Base64.getEncoder().encodeToString(token.getBytes("UTF-8"));
			return "Basic " + encoded;
		} catch (UnsupportedEncodingException ex) {
			throw new IllegalStateException("Cannot encode with UTF-8", ex);
		}
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public String executarPost(String jsessionid, String resourceUrl, String json) {
		Client client = ClientBuilder.newClient();				
		Entity<String> entity = Entity.entity(json, MediaType.APPLICATION_JSON);
		WebTarget target = client.target(host).path(resourceUrl);
		Builder builder = target.request();			
		builder.header("Authorization", getBasicAuthentication());
        builder.header("Content-Type",  "application/json");
        //builder.cookie("JSESSIONID", jsessionid);			
		String response = builder.post(entity, String.class);
		client.close();
		return response;
	}
	
	@Test
	public void testManutenirEmpresa() {
		if(getSessionId() == null) {
			Assert.assertNotNull("Não foi possivel fazer o login", getSessionId());
			return;
		}
		String json = "[\"tosp.foundation.core.kernel.coqueryobject.CoQueryObjectDynamic\"," + 
				"        {\"Empresa\":[\"java.util.HashMap\",	" + 
				"	   	   	  {" + 
				"	   	   	   	  \"tipo\":2," + 
				"	   	   	   	  \"page\":0" + 
				"	   	   	  }" + 
				"	 ]}]";
		String result = executarPost(getSessionId(), "ManutenirEmpresaEmap/filtrar", json);
		System.out.println(result);
		Assert.assertFalse(result.toLowerCase().contains("bad credentials"));	
	}
	
	@Test
	public void testManutenirOperacaoNavio() {
		if(getSessionId() == null) {
			Assert.assertNotNull("Não foi possivel fazer o login", getSessionId());
			return;
		}
		String json = "[\"tosp.foundation.core.kernel.coqueryobject.CoQueryObjectDynamic\"," + 
				"        {\"OperacaoNavio\":[\"java.util.HashMap\",	" + 
				"	   	   	  {" + 
				"	   	   	   	  \"viagemId\":1499905 ," + 
				"	   	   	   	  \"page\":0" + 
				"	   	   	  }" + 
				"	 ]}]";
		String result = executarPost(getSessionId(), "ManutenirOperacaoNavio/filtrar", json);
		System.out.println(result);
		Assert.assertFalse(result.toLowerCase().contains("bad credentials"));
	}
	
	@Test
	public void testManutenirPerfil() {
		if(getSessionId() == null) {
			Assert.assertNotNull("Não foi possivel fazer o login", getSessionId());
			return;
		}
		String json = "[\"tosp.foundation.core.kernel.coqueryobject.CoQueryObjectDynamic\"," + 
				"        {\"Perfil\":[\"java.util.HashMap\",	" + 
				"	   	   	  {" + 
				"	   	   	   	  \"page\":0" + 
				"	   	   	  }" + 
				"	 ]}]";
		String result = executarPost(getSessionId(), "ManutenirPerfil/filtrar", json);
		System.out.println(result);
		Assert.assertFalse(result.toLowerCase().contains("bad credentials"));
	}
	
}
