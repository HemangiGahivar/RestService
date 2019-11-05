package com.wipro.demo.controller;

import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

import java.util.HashMap;
import java.util.Map;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.wipro.demo.bean.User;
import com.wipro.demo.service.UserService;

import com.google.cloud.MonitoredResource;
import com.google.cloud.logging.HttpRequest;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.LoggingOptions;
import com.google.cloud.logging.Payload.StringPayload;
import com.google.cloud.logging.Severity;
import java.util.Collections;


//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;


@RestController
@RequestMapping(value={"/user"})
@Timed
public class UserController 
{

  static Logging logging = LoggingOptions.getDefaultInstance().getService();
	//Logger logger = LoggerFactory.getLogger(UserController.class);
	
	static String logName = "error-log";
	
	static String error="HttpStatus.NOT_FOUND";
	
	static String ok="HttpStatus.OK";

	static LogEntry entry = LogEntry.newBuilder(StringPayload.of(error))
	        .setSeverity(Severity.ERROR)
	        .setLogName(logName)
	        .setHttpRequest(HttpRequest.newBuilder().setStatus(404).build())
            .setResource(MonitoredResource.newBuilder("k8s_container").addLabel("project_id","wipro-gcp-parsnet-poc").addLabel("location","us-central1-a").addLabel("cluster_name","latency-metric").build())
	        .build();
	
	static LogEntry entry1 = LogEntry.newBuilder(StringPayload.of(ok))
	        .setSeverity(Severity.INFO)
	        .setLogName(logName)
	        .setHttpRequest(HttpRequest.newBuilder().setStatus(200).build())
            .setResource(MonitoredResource.newBuilder("k8s_container").build())
	        .build();

@Autowired
        @GetMapping("/")
        public String greet() {
            logging.write(Collections.singleton(entry1)); 
                return "Hello!";
        }



	@Autowired
	UserService userService;
    

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
     @Timed(value = "users.ById",percentiles = {0.5, 0.95, 0.999}, histogram = true)
    public ResponseEntity<User> getUserById(@PathVariable("id") int id) {
        System.out.println("Fetching User with id " + id);
        User user = userService.findById(id);
        if (user == null) {
        	
        	  ResponseEntity<User> responseEntity=new ResponseEntity<User>(HttpStatus.NOT_FOUND);
            //return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
            
         // Writes the log entry asynchronously
            logging.write(Collections.singleton(entry));         
            return responseEntity;
        }
        logging.write(Collections.singleton(entry1));
        return new ResponseEntity<User>(user, HttpStatus.OK);
    }
    
	 @PostMapping(value="/create",headers="Accept=application/json")
     @Timed(value = "add.users",percentiles = {0.5, 0.95, 0.999}, histogram = true)
	 public ResponseEntity<Void> createUser(@RequestBody User user, UriComponentsBuilder ucBuilder){
	     System.out.println("Creating User "+user.getName());
	     userService.createUser(user);
	     HttpHeaders headers = new HttpHeaders();
	     headers.setLocation(ucBuilder.path("/user/{id}").buildAndExpand(user.getId()).toUri());
	     return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	 }

	 @GetMapping(value="/get", headers="Accept=application/json")
     @Timed(value = "users.list",percentiles = {0.5, 0.95, 0.999}, histogram = true)
	 public List<User> getAllUser() {	 
	  List<User> tasks=userService.getUser();
      logging.write(Collections.singleton(entry));
	  return tasks;
	
	 }

	@PutMapping(value="/update", headers="Accept=application/json")
	public ResponseEntity<String> updateUser(@RequestBody User currentUser)
	{
	User user = userService.findById(currentUser.getId());
	if (user==null) {
		
		logging.write(Collections.singleton(entry));
		return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
		
	}
	user.setId(currentUser.getId());
	user.setName(currentUser.getName());
	user.setCountry(currentUser.getCountry());
	userService.update(user);
	logging.write(Collections.singleton(entry1));
	return new ResponseEntity<String>(HttpStatus.OK);
	}
	
	@DeleteMapping(value="/{id}", headers ="Accept=application/json")
    @Timed(value = "delete.users",percentiles = {0.5, 0.95, 0.999}, histogram = true)
	public ResponseEntity<User> deleteUser(@PathVariable("id") int id){
		User user = userService.findById(id);
		if (user == null) {
			logging.write(Collections.singleton(entry));
			return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
		}
		userService.deleteUserById(id);
		return new ResponseEntity<User>(HttpStatus.NO_CONTENT);
	}
	
	@PatchMapping(value="/{id}", headers="Accept=application/json")
	public ResponseEntity<User> updateUserPartial(@PathVariable("id") int id, @RequestBody User currentUser){
		User user = userService.findById(id);
		if(user ==null){
			logging.write(Collections.singleton(entry));
			return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
		}
		
		userService.updatePartially(currentUser, id);
		logging.write(Collections.singleton(entry1));
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}
}
