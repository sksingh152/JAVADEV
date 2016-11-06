import java.util.Map;

interface ShopManager{
	public Map<String,Address> constructAddressMap(String jsonString) throws Exception;
	public String findNearestAdresses(Map<String,Address> addressMap,double currentLat,double currentLon);
}