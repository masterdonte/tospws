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
	
	private final static String host = "http://tos.emap.ma.gov.br/tosp";
	private final static String user = "usuario";
	private final static String pass = "senha";
		
	public String getStringCookie() {						
		HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(user, pass);
		Client client = ClientBuilder.newClient();
		client.register(feature);
		WebTarget target = client.target(host).queryParam("portal", "ORG");
		Builder builder = target.request();
		Response response = builder.get(Response.class);		
		String output = response.readEntity(String.class);					
		if(output == null || output.toLowerCase().contains("bad credentials")) {
			return null;
		}		
		NewCookie newCookie = response.getCookies().get("JSESSIONID");			
		return newCookie.getValue();
	}

	@Test
	public void testarPesagemWebService() {
		//Fazer esta requisição apenas uma vez e utilizá-la para as requisições posteriores
		String jsessionid = getStringCookie();
		
		if(jsessionid != null) {
			Client client = ClientBuilder.newClient();				
			Entity<String> entity = Entity.entity(getJson(), MediaType.APPLICATION_JSON);		
			WebTarget target = client.target(host).path("/PESAGEMWS/filtrar");			                 
			Builder builder = target.request();			
			builder.header("Authorization", getBasicAuthentication());
	        builder.header("Content-Type",  "application/json");
	        builder.cookie("JSESSIONID", jsessionid);			
			String response = builder.post(entity, String.class);      
			System.out.println(response);
			client.close();
			Assert.assertTrue(true);			
		}else {
			Assert.assertNotNull("Não foi possivel fazer o login", jsessionid);			
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
	//Exemplo de JSON	
	private String getJson() {
		String json = "[\"tosp.foundation.core.kernel.coqueryobject.CoQueryObjectDynamic\"," + 
				" {\"PesagemDTO\":[\"java.util.HashMap\",	" + 
				"						{" + 
				"							\"placa\":\"\"," + 
				"							\"imo\":\"9514389\"," + 
				"							\"cnpjCliente\":\"\"," + 
				"							\"pesagemIni\":\"25/04/2021 05:00\"," + 
				"							\"pesagemFim\":\"25/04/2021 08:00\"," + 
				"							\"page\":0" + 
				"						}" + 
				"					]" + 
				" }"+
				"]" ;
		return json;
	}

}
