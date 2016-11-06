import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
@RestController
@EnableAutoConfiguration

public class Example{
	//private static final String URL = "http://maps.googleapis.com/maps/api/geocode/json";
	Map<String, Address> shopDetailsMap = new HashMap<String, Address>();
	//@Autowired 
	//ShopManager shopManager;



    @RequestMapping("/")
    String home(){
        return "Hello World!";
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Example.class, args);

    }
	
	@RequestMapping(value = "/addShops" ,method = RequestMethod.POST)
    String addShops(@RequestBody String shopList) throws Exception{
		if(shopList!=null){
			//idealy it should be autowired and call, calling by creating object temporally
			ShopManager shopManager = new ShopManagerImpl();
			if(shopDetailsMap!=null && shopDetailsMap.size() > 0){
				shopDetailsMap.putAll(shopManager.constructAddressMap(shopList));
			}else{
				shopDetailsMap = shopManager.constructAddressMap(shopList);
			}
			return "Added Suceessfully";
		}
		return "Not Added";
    }
	
	@RequestMapping("/findShops")
    String addShops(String currentLat,String currentLon,HttpServletRequest request) {
		if(shopDetailsMap!=null && shopDetailsMap.size() > 0){
			ShopManager shopManager = new ShopManagerImpl();
			double lat = Double.parseDouble(currentLat);
			double lon = Double.parseDouble(currentLon);
			return shopManager.findNearestAdresses(shopDetailsMap,lat,lon);
		}
		return "Hello World! add ssdfsfds";
    }
}
