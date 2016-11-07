import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
class ShopManagerImpl implements ShopManager{
	
	public ShopManagerImpl() {
		// TODO Auto-generated constructor stub
	}
	
	public Map<String,Address> constructAddressMap(String jsonString) throws Exception{
		List<Address> addressList = null;	
		try {
			ObjectMapper mapper = new ObjectMapper();
			addressList = Arrays.asList(mapper.readValue(jsonString, Address[].class));
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(addressList != null && addressList.size() > 0){
			return findAndUpdateLatLongToAddress(addressList);
		}else{
			return null;
		}
	}
	
	public Map<String,Address> findAndUpdateLatLongToAddress(List<Address> addresses){
		String addressKey = null;
		String[] latlong = null;
		Map<String,Address> map = new HashMap<String, Address>();
		for(Address address : addresses){
			addressKey = address.getShopName()+address.getShopNumber()+address.getShopPostCode();
			try {
				latlong = getLatLongPositions(addressKey);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(latlong!=null && latlong.length > 1){
				address.setLat(latlong[0]);
				address.setLon(latlong[1]);
				map.put(addressKey, address);

			}
		}
		return map;
	}
		
	public static String[] getLatLongPositions(String address) throws Exception{
		int responseCode = 0;
		String api = "http://maps.googleapis.com/maps/api/geocode/xml?address=" + URLEncoder.encode(address, "UTF-8") + "&sensor=true";
		URL url = new URL(api);
		HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection();
		httpConnection.connect();
		responseCode = httpConnection.getResponseCode();
		if(responseCode == 200){
		  DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();;
		  Document document = builder.parse(httpConnection.getInputStream());
		  XPathFactory xPathfactory = XPathFactory.newInstance();
		  XPath xpath = xPathfactory.newXPath();
		  XPathExpression expr = xpath.compile("/GeocodeResponse/status");
		  String status = (String)expr.evaluate(document, XPathConstants.STRING);
		  if(status.equals("OK")){
			 expr = xpath.compile("//geometry/location/lat");
			 String latitude = (String)expr.evaluate(document, XPathConstants.STRING);
			 expr = xpath.compile("//geometry/location/lng");
			 String longitude = (String)expr.evaluate(document, XPathConstants.STRING);
			 return new String[] {latitude, longitude};
		  }else{
			throw new Exception("Error from the API - response status: "+status);
		  }
		}
		return null;
	}
	//finding nearest addresses
	
	public String findNearestAddresses(Map<String,Address> addressMap,double currentLat,double currentLon){
		List<Address> addresses = new ArrayList<Address>();
		String jsonresponse = null;
		for(Map.Entry<String, Address> entry : addressMap.entrySet()){
			Address address = entry.getValue();
			double lat = Double.parseDouble(address.getLat());
			double lon = Double.parseDouble(address.getLon());
			String unit = "M";
			if(distance(lat,lon,currentLat, currentLon,unit) < 1){
				addresses.add(address);
			}
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			jsonresponse = mapper.writeValueAsString(addresses);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonresponse;	
	}
	//distance
	private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == "K") {
			dist = dist * 1.609344;
		}		return (dist);
	}

	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}
	private static double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}
		
	
}
