package com.action.controller;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.action.entity.Trade;
import com.action.exception.userCredetailsException;

@RestController
@RequestMapping(value = "/v1")
public class TradeActionRestController {
	private static Logger logger = LogManager.getLogger(TradeActionRestController.class);

	@Autowired
	RestTemplate restTemplate;

	@Bean
	// @LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@PostMapping(path = "/action/review/trades", consumes = "application/json", produces = "application/json")
	public ResponseEntity<Trade[]> reviewTradeAction(@RequestBody List<Trade> listTrades,
			@RequestHeader HttpHeaders securitydetails) {

		logger.info("inside reviewTradeAction(@RequestBody List<Trade> listTrades) method ");
		String userName = securitydetails(securitydetails);

		if (!userName.equals("review"))
			throw new userCredetailsException("you are not authorized to perfrom this action");

		ResponseEntity<Trade[]> updateTrades = updateTrades(listTrades, "reviewed");

		return updateTrades;

	}

	@PostMapping(path = "action/approve/trades", consumes = "application/json", produces = "application/json")
	public ResponseEntity<Trade[]> approveTradeAction(@RequestBody List<Trade> listTrades,
			@RequestHeader HttpHeaders securitydetails) {

		logger.info("inside approveTradeAction(@RequestBody List<Trade> listTrades) method ");

		if (!securitydetails(securitydetails).equals("admin"))
			throw new userCredetailsException("you are not authorized to perfrom this action");

		return updateTrades(listTrades, "approved");
	}

	@PostMapping(path = "action/reject/trades", consumes = "application/json", produces = "application/json")
	public ResponseEntity<Trade[]> rejectTradeAction(@RequestBody List<Trade> listTrades,
			@RequestHeader HttpHeaders securitydetails) {

		logger.info("inside rejectTradeAction(@RequestBody List<Trade> listTrades) method ");

		if (!securitydetails(securitydetails).equals("admin"))
			throw new userCredetailsException("you are not authorized to perfrom this action");

		return updateTrades(listTrades, "rejected");
	}

	@PostMapping(path = "action/cancel/trades", consumes = "application/json", produces = "application/json")
	public ResponseEntity<Trade[]> cancelTradeAction(@RequestBody List<Trade> listTrades,
			@RequestHeader HttpHeaders securitydetails) {

		logger.info("inside rejectTradeAction(@RequestBody List<Trade> listTrades) method ");

		if (!securitydetails(securitydetails).equals("admin"))
			throw new userCredetailsException("you are not authorized to perfrom this action");

		return updateTrades(listTrades, "cancel");
	}

	private ResponseEntity<Trade[]> updateTrades(List<Trade> listTrades, String action) {
		logger.info("inside updateTrades(List<Trade> listTrades, String action) method ");

		MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
		mappingJackson2HttpMessageConverter
				.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM));
		restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

		String username = "user";
		String password = "1234";

		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth(username, password);
		HttpEntity<List<Trade>> request = new HttpEntity<>(listTrades, headers);

		String url = "http://localhost:8005/v2/trades/listoftrades";

		restTemplate.postForEntity(url, request, Trade[].class);

		listTrades.forEach(trade -> {
			trade.setStatus(action);
		});

		HttpEntity<List<Trade>> request1 = new HttpEntity<>(listTrades, headers);
		String url1 = "http://localhost:8005/v1/saveMultiTrades";

		ResponseEntity<Trade[]> postForObject = restTemplate.postForEntity(url1, request1, Trade[].class);

		return postForObject;
	}

	public String securitydetails(@RequestHeader HttpHeaders headers) throws userCredetailsException {
		String[] usercredentials = null;
		try {
			final List<String> authorization1 = headers.get("Authorization");
			String authorization = authorization1.get(0);
			if (authorization != null && authorization.toLowerCase().startsWith("basic")) {

				String base64Credentials = authorization.substring("Basic".length()).trim();
				byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
				String credentials = new String(credDecoded, StandardCharsets.UTF_8);
				usercredentials = credentials.split(":", 2);
			}
		} catch (Exception e) {
			throw new userCredetailsException("Please provide user credentials");
		}
		if (usercredentials[0].isEmpty() || usercredentials[1].isEmpty())
			throw new userCredetailsException("Please provide user credentials");
		if ((!usercredentials[0].contains("user") || !usercredentials[0].contains("admin")
				|| !usercredentials[0].contains("review") || !usercredentials[0].contains("trader"))
				&& !usercredentials[1].equals("1234"))
			throw new userCredetailsException("bad user credentials");
		return usercredentials[0];
	}

	// for testing purpose
	@GetMapping("/test")
	public String test(@RequestHeader HttpHeaders securitydetails) {
		String username = securitydetails(securitydetails);
		System.out.println(username);
		return "Test String";
	}
}
