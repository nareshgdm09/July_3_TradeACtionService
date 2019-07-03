package com.action.restcontroller;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.http.HttpHeaders;

import com.action.controller.TradeActionRestController;

public class TradeServiceImplTest {

	@InjectMocks
	TradeActionRestController tradeActionRestController = new TradeActionRestController();

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testAction() {
		HttpHeaders securitydetails = new HttpHeaders();
		OngoingStubbing<String> thenReturn = when(tradeActionRestController.securitydetails(securitydetails))
				.thenReturn("admin");

		System.out.println(thenReturn);
	}

}
