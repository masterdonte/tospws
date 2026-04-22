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
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TestTOS {
	private final static String host = "https://tos-homolog.portodoitaqui.com/tosp";
	private final static String user = "externo";
	private final static String pass = "******";
	private String sessionId;
	
	public TestTOS() {
		tryAuthenticate();
	}
	
	private void tryAuthenticate() {
		HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(user, pass);
		Client client = ClientBuilder.newClient();
		client.register(feature);
		WebTarget target = client.target(host).queryParam("portal", "AMC");
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
	
	private String getJsonConsulta(String entityName, JSONObject object) {
		JSONArray item = new JSONArray();
		item.put("java.util.HashMap");
		item.put(object);
		JSONObject type = new JSONObject();
		type.put(entityName, item);
		JSONArray root = new JSONArray();
		root.put("tosp.foundation.core.kernel.coqueryobject.CoQueryObjectDynamic");
		root.put(type);
		return root.toString();
	}
	
	private String getJsonCoQueryObject(String fullPathDTO, JSONObject object) {
		JSONArray item = new JSONArray();
		item.put(fullPathDTO);
		item.put(object);
		JSONArray innerArray = new JSONArray();
		innerArray.put(item);
		JSONArray root = new JSONArray();
		root.put("tosp.foundation.core.kernel.coqueryobject.CoQueryObject");
		root.put(innerArray);
		return root.toString();
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
		JSONObject object = new JSONObject();
		object.put("nome", "STARMAR");
		object.put("tipo", 2);
		object.put("page", 0);
		String result = executarPost(getSessionId(), "ManutenirEmpresaEmap/filtrar", getJsonConsulta("Empresa", object));
		System.out.println(result);
		Assert.assertFalse(result.toLowerCase().contains("bad credentials"));	
	}
	
	//@Test
	public void testManutenirOperacaoNavio() {
		if(getSessionId() == null) {
			Assert.assertNotNull("Não foi possivel fazer o login", getSessionId());
			return;
		}
		JSONObject object = new JSONObject();
		object.put("viagemId", 1499905);
		object.put("page", 0);
		String result = executarPost(getSessionId(), "ManutenirOperacaoNavio/filtrar", getJsonConsulta("OperacaoNavio", object));
		System.out.println(result);
		Assert.assertFalse(result.toLowerCase().contains("bad credentials"));
	}
	
	//@Test
	public void testManutenirPerfil() {
		if(getSessionId() == null) {
			Assert.assertNotNull("Não foi possivel fazer o login", getSessionId());
			return;
		}
		JSONObject object = new JSONObject();
		String result = executarPost(getSessionId(), "ManutenirPerfil/filtrar", getJsonConsulta("Perfil", object));
		System.out.println(result);
		Assert.assertFalse(result.toLowerCase().contains("bad credentials"));
	}
	
	//@Test
	public void testManutenirEmpresaIncluir() {
		if(getSessionId() == null) {
			Assert.assertNotNull("Não foi possivel fazer o login", getSessionId());
			return;
		}
		String fullPath = "tosp.plugin.comun.generic.empresa.model.EmpresaDTO";
		JSONObject empresa = new JSONObject();
		empresa.put("nome", "JUREMACENTER");
		empresa.put("cnpj", "03.743.193/0001-69");
		empresa.put("email", "JUREMACENTER@GMAIL.COM");
		empresa.put("kdPratico", false);
		empresa.put("kdRebocador", true);
		empresa.put("kdAmarracao", false);

		String json = getJsonCoQueryObject(fullPath, empresa);		
		String result = executarPost(getSessionId(), "ManutenirEmpresaEmap/incluir", json);
		System.out.println(result);
		Assert.assertFalse(result.toLowerCase().contains("bad credentials"));
	}
	
	public void testManutenirEmpresaAlterar() {
		if(getSessionId() == null) {
			Assert.assertNotNull("Não foi possivel fazer o login", getSessionId());
			return;
		}
		String fullPath = "tosp.plugin.comun.generic.empresa.model.EmpresaDTO";
		JSONObject empresa = new JSONObject();
		empresa.put("id", 1000L);
		empresa.put("nome", "XXXXX");
		empresa.put("cnpj", "03.743.193/0001-69");
		empresa.put("email", "qualquer@email.com");
		
		String json = getJsonCoQueryObject(fullPath, empresa);		
		String result = executarPost(getSessionId(), "ManutenirEmpresaEmap/alterar", json);
		System.out.println(result);
		Assert.assertFalse(result.toLowerCase().contains("bad credentials"));
	}
	
	@Test
	public void testManutenirEmpresaDeletar() {
		if(getSessionId() == null) {
			Assert.assertNotNull("Não foi possivel fazer o login", getSessionId());
			return;
		}
		String fullPath = "tosp.plugin.comun.generic.empresa.model.EmpresaDTO";
		JSONObject empresa = new JSONObject();
		empresa.put("id", 10446L);
		empresa.put("nome", "XXXXX");
		empresa.put("cnpj", "03.743.193/0001-69");
		empresa.put("email", "qualquer@email.com");
		String json = getJsonCoQueryObject(fullPath, empresa);	
		String result = executarPost(getSessionId(), "ManutenirEmpresaEmap/delete", json);
		System.out.println(result);
		Assert.assertFalse(result.toLowerCase().contains("bad credentials"));
	}
}
/* String json = "[\"tosp.foundation.core.kernel.coqueryobject.CoQueryObjectDynamic\"," + 
		"        {\"Perfil\":[\"java.util.HashMap\",	" + 
		"	   	   	  {" + 
		"	   	   	   	  \"page\":0" + 
		"	   	   	  }" + 
		"	 ]}]";
*/
