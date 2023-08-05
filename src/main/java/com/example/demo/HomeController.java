package com.example.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping("/")
    public String test() {
        return "login";
    }

    @PostMapping("/processlogin")
    public String processLogin(@RequestParam("email") String email, @RequestParam("password") String pass, HttpSession session) throws IOException, InterruptedException {
        System.out.println(email);
        System.out.println(pass);

        var url = "https://qa2.sunbasedata.com/sunbase/portal/api/assignment_auth.jsp";

        String jsonData = String.format("{\"login_id\":\"%s\", \"password\":\"%s\"}", email, pass);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                .header("Content-Type", "application/json")
                .build();

        var cli = HttpClient.newBuilder().build();

        var res = cli.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(res.body());

        String jsonString = res.body();

        int len = jsonString.length();
        System.out.println(len);

        String[] a = jsonString.split(":", 2);
        String str = a[1];
        String token = str.substring(1, 41);
        session.setAttribute("token", token);
        System.out.println(token);
        return "login";
    }

    @GetMapping("/getc")
    public String getconsumerdata(HttpSession session, Model model) throws IOException, InterruptedException {
        var url = "https://qa2.sunbasedata.com/sunbase/portal/api/assignment.jsp";
        String cmd = "get_customer_list";

        // Construct the URL with the query parameter.
        String fullUrl = url + "?cmd=" + cmd;

        String token = (String) session.getAttribute("token");
        String t = "Bearer " + token;
        System.out.println(token);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(fullUrl))
                .header("Authorization", t)
                .build();

        var cli = HttpClient.newBuilder().build();

        var res = cli.send(request, HttpResponse.BodyHandlers.ofString());
        
        if(res.statusCode() != 200)
        {
            return "redirect:/";
        }
        
        String jsonData = res.body();
        System.out.println(jsonData);

        try {
            // Create an ObjectMapper
            ObjectMapper mapper = new ObjectMapper();

            // Parse JSON data to JsonNode
            JsonNode jsonNode = mapper.readTree(jsonData);

            // Convert JsonNode to a list of dictionaries (JSON objects)
            /*for (JsonNode node : jsonNode) {
                System.out.println(node.toString());
            }*/
            List<Consumer> peopleList = new ArrayList<>();
            for (JsonNode node : jsonNode) {
                Consumer person = mapper.readValue(node.toString(), Consumer.class);
                peopleList.add(person);
            }

            // Now you have a list of Person objects
            for (Consumer person : peopleList) {
                System.out.println(person);
            }

            model.addAttribute("peopleList", peopleList);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return "display";

    }

    @GetMapping("/add")
    public String addCustomer() {
        return "add";
    }

    @PostMapping("/processcontact")
    public String processContact(@RequestParam("firstname") String firstname,
            @RequestParam("lastname") String lastname,
            @RequestParam("street") String street,
            @RequestParam("address") String address,
            @RequestParam("city") String city,
            @RequestParam("state") String state,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            HttpSession session) throws IOException, InterruptedException {

        // Check if the mandatory parameters are provided
        if (firstname.isEmpty() || lastname.isEmpty()) {
            return "Error: First Name or Last Name is missing";
        }

        // Retrieve the token from the session
        String token = (String) session.getAttribute("token");

        // Construct the URL with the query parameter.
        String url = "https://qa2.sunbasedata.com/sunbase/portal/api/assignment.jsp";
        String cmd = "create";
        String fullUrl = url + "?cmd=" + cmd;

        // Create the JSON data with all the parameters
        String jsonData = String.format("{\"first_name\":\"%s\", \"last_name\":\"%s\", \"street\":\"%s\", \"address\":\"%s\", \"city\":\"%s\", \"state\":\"%s\", \"email\":\"%s\", \"phone\":\"%s\"}",
                firstname, lastname, street, address, city, state, email, phone);

        // Set the Authorization header with the token
        String authHeader = "Bearer " + token;

        // Send the POST request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json") // Add Content-Type header for JSON data
                .build();

        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Check the response status code
        if (response.statusCode() == 201) {
            // Successfully Created
            return "redirect:/getc";
        } else {
            // Print the error message if available

            return "redirect:/add";
        }
    }
    
    @PostMapping("/delete/{uuid}")
    public String deleteCustomer(@PathVariable("uuid") String uuid, HttpSession session) throws IOException, InterruptedException
    {
        System.out.println(uuid);
       String token = (String) session.getAttribute("token");
       
       // Construct the URL with the query parameter.
        String url = "https://qa2.sunbasedata.com/sunbase/portal/api/assignment.jsp";
        String cmd = "delete";
        String fullUrl = url + "?cmd=" + cmd + "&uuid=" + uuid;


        // Set the Authorization header with the token
        String authHeader = "Bearer " + token;
        
        
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(fullUrl))
            .POST(HttpRequest.BodyPublishers.noBody())
            .header("Authorization", authHeader)
            .build();

    HttpClient httpClient = HttpClient.newBuilder().build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    
    System.out.println(response.statusCode());

        return "redirect:/getc";
    }

    @GetMapping("/edit/{uuid}/{firstname}/{lastname}/{street}/{address}/{city}/{state}/{email}/{phone}")
    public String UpdateCustomer(@PathVariable("uuid") String uuid,@PathVariable("firstname") String firstname,
            @PathVariable("lastname") String lastname,@PathVariable("street") String street,@PathVariable("address") String address,
            @PathVariable("city") String city,@PathVariable("state") String state,@PathVariable("email") String email,@PathVariable("phone") String phone,
            Model model)
    {
      
        model.addAttribute("uuid",uuid);
        model.addAttribute("firstname",firstname);
        model.addAttribute("lastname",lastname);
        model.addAttribute("street",street);
        model.addAttribute("address",address);
        model.addAttribute("city",city);
        model.addAttribute("state",state);
        model.addAttribute("email",email);
        model.addAttribute("phone",phone);
        return "update";
    }
    
    @PostMapping("/processupdate/{uuid}")
    public String updateCustomer(@PathVariable("uuid") String uuid,  @RequestParam("firstname") String firstname, @RequestParam("lastname") String lastname,@RequestParam("street") String street,   @RequestParam("address") String address,  @RequestParam("city") String city,
            @RequestParam("state") String state,   @RequestParam("email") String email,  @RequestParam("phone") String phone,HttpSession session) throws IOException, InterruptedException
    {
        String token = (String) session.getAttribute("token");
       
       // Construct the URL with the query parameter.
        String url = "https://qa2.sunbasedata.com/sunbase/portal/api/assignment.jsp";
        String cmd = "update";
        String fullUrl = url + "?cmd=" + cmd + "&uuid=" + uuid;

         String jsonData = String.format("{\"first_name\":\"%s\", \"last_name\":\"%s\", \"street\":\"%s\", \"address\":\"%s\", \"city\":\"%s\", \"state\":\"%s\", \"email\":\"%s\", \"phone\":\"%s\"}",
                firstname, lastname, street, address, city, state, email, phone);

        // Set the Authorization header with the token
        String authHeader = "Bearer " + token;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(fullUrl))
            .POST(HttpRequest.BodyPublishers.ofString(jsonData))
            .header("Authorization", authHeader)
            .build();

    HttpClient httpClient = HttpClient.newBuilder().build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    
    System.out.println(response.statusCode());
        
        return "redirect:/getc";
        
        
        
    }
}
